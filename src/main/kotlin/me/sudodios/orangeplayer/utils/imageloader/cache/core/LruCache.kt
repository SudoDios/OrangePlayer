package me.sudodios.orangeplayer.utils.imageloader.cache.core

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

typealias SizeCalculator<K, V> = (key: K, value: V) -> Long

typealias EntryRemovedListener<K, V> = (evicted: Boolean, key: K, oldValue: V, newValue: V?) -> Unit

class LruCache<K : Any, V : Any>(
    maxSize: Long,
    private val creationScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val sizeCalculator: SizeCalculator<K, V> = { _, _ -> 1 },
    private val onEntryRemoved: EntryRemovedListener<K, V> = { _, _, _, _ -> },
) {
    init {
        require(maxSize > 0) { "maxSize must be positive value" }
    }

    internal val creationMap = ConcurrentHashMap<K, Deferred<V?>>(0, 0.75F, 1)
    private val creationMutex = Mutex()

    internal val map = LinkedHashMap<K, V>(0, 0.75F, true)
    internal val mapMutex = Mutex()

    /**
     * The max size of this cache in units calculated by [sizeCalculator]. This represents the max number of entries
     * if [sizeCalculator] used the default implementation (returning 1 for each entry),
     */
    private var maxSize = maxSize
        private set

    /**
     * The current size of this cache in units calculated by [sizeCalculator]. This represents the current number of
     * entries if [sizeCalculator] used the default implementation (returning 1 for each entry),
     */
    var size = 0L
        private set

    /**
     * Returns the value for [key] if it exists in the cache or wait for its creation if it is currently in progress.
     * This returns `null` if a value is not cached and wasn't in creation or cannot be created.
     *
     * It may even throw exceptions for unhandled exceptions in the currently in-progress creation block.
     */
    suspend fun get(key: K): V? =
        getFromCreation(key) ?: getIfAvailable(key)


    /**
     * Returns the value for [key] if it already exists in the cache or `null` if it doesn't exist or creation is still
     * in progress.
     */
    private suspend fun getIfAvailable(key: K): V? =
        mapMutex.withLock { map[key] }


    /**
     * Returns the value for [key] if it exists in the cache, its creation is in progress or can be created by
     * [creationFunction]. If a value was returned, it is moved to the head of the queue. This returns `null` if a
     * value is not cached and cannot be created. You can imply that the creation has failed by returning `null`.
     * Any unhandled exceptions inside [creationFunction] won't be handled.
     */
    suspend fun getOrPut(key: K, creationFunction: suspend (key: K) -> V?) =
        creationMutex.withLock {
            get(key) ?: getFromCreation(key, internalPutAsync(key, creationFunction))
        }

    /**
     * Creates a new entry for [key] using [creationFunction] and returns a [Deferred]. Any existing value or
     * in-progress creation of [key] would be replaced by the new function. If a value was created, it is moved to the
     * head of the queue. You can imply that the creation has failed by returning `null`.
     */
    private suspend fun putAsync(key: K, creationFunction: suspend (key: K) -> V?): Deferred<V?> =
        creationMutex.withLock { internalPutAsync(key, creationFunction) }

    private fun internalPutAsync(
        key: K,
        mappingFunction: suspend (key: K) -> V?,
    ): Deferred<V?> {
        val deferred = creationScope.async {
            val value = try {
                mappingFunction(key)
            } catch (cancellation: CancellationException) {
                null
            }

            if (value != null) {
                // All operations inside the lock to prevent cancellation before trimming or
                // invoking listener
                mapMutex.withLock {
                    val oldValue = map.put(key, value)

                    size += safeSizeOf(key, value) - (oldValue?.let { safeSizeOf(key, it) } ?: 0)
                    nonLockedTrimToSize(maxSize)

                    oldValue?.let { onEntryRemoved(false, key, it, value) }
                }
            }

            value
        }

        deferred.invokeOnCompletion {
            @Suppress("DeferredResultUnused")
            creationMap.remove(key)
        }

        creationMap[key] = deferred
        return deferred
    }

    /**
     * Caches [value] for [key]. The value is moved to the head of the queue. If there is a previous value or
     * in-progress creation, it will be removed/cancelled. It returns the previous value if it already exists,
     * or `null`
     */
    suspend fun put(key: K, value: V): V? {
        val oldValue = mapMutex.withLock {
            val oldValue = map.put(key, value)

            size += safeSizeOf(key, value) - (oldValue?.let { safeSizeOf(key, it) } ?: 0)
            removeCreation(key, CODE_VALUE)

            oldValue
        }

        trimToSize(maxSize)

        oldValue?.let { onEntryRemoved(false, key, it, value) }
        return oldValue
    }

    /**
     * Removes the entry and in-progress creation for [key] if it exists. It returns the previous value for [key].
     */
    suspend fun remove(key: K): V? {
        removeCreation(key)

        return mapMutex.withLock {
            val oldValue = map.remove(key)
            if (oldValue != null) size -= safeSizeOf(key, oldValue)
            oldValue
        }?.let { oldValue ->
            onEntryRemoved(false, key, oldValue, null)
            oldValue
        }
    }


    /**
     * Remove the eldest entries until the total of remaining entries is/at/or below [size]. It won't affect the max
     * size of the cache, allowing it to grow again.
     */
    private suspend fun trimToSize(size: Long) {
        mapMutex.withLock {
            nonLockedTrimToSize(size)
        }
    }

    private fun nonLockedTrimToSize(size: Long) {
        with(map.iterator()) {
            forEach { (key, value) ->
                if (this@LruCache.size <= size) return@forEach
                remove()
                this@LruCache.size -= safeSizeOf(key, value)
                onEntryRemoved(true, key, value, null)
            }
        }

        check(this.size >= 0 || (map.isEmpty() && this.size != 0L)) {
            "sizeCalculator is reporting inconsistent results!"
        }
    }

    private fun safeSizeOf(key: K, value: V): Long {
        val size = sizeCalculator(key, value)
        check(size >= 0) { "Negative size: $key = $value" }
        return size
    }

    private suspend fun getFromCreation(key: K): V? =
        creationMap[key]?.let { deferred -> getFromCreation(key, deferred) }

    private suspend fun getFromCreation(key: K, creation: Deferred<V?>): V? {
        return try {
            creation.await()
        } catch (ex: CancellationException) {
            val cause = ex.cause
            if (cause is DeferredReplacedException) {
                when (cause.replacedWith) {
                    CODE_CREATION -> getFromCreation(key)
                    CODE_VALUE -> getIfAvailable(key)
                    else -> null
                }
            } else null
        }
    }

    private fun removeCreation(key: K, replacedWith: Int? = null) {
        val deferred = creationMap.remove(key)
        deferred?.cancel(
            message = "The cached element was removed before creation",
            cause = replacedWith?.let { DeferredReplacedException(it) },
        )
    }
}

private const val CODE_CREATION = 1
private const val CODE_VALUE = 2

private class DeferredReplacedException(val replacedWith: Int) : CancellationException()