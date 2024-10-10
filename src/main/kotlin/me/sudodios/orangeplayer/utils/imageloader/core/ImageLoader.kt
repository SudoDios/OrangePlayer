package me.sudodios.orangeplayer.utils.imageloader.core

import androidx.compose.ui.res.loadImageBitmap
import kotlinx.coroutines.*
import me.sudodios.orangeplayer.core.Native
import me.sudodios.orangeplayer.utils.imageloader.cache.LruUtil
import me.sudodios.orangeplayer.utils.imageloader.cache.MemoryCache
import me.sudodios.orangeplayer.utils.imageloader.transform.ITransformation
import java.io.File

enum class SaveStrategy { Original }

val ImageInLoading = ImageResponse(null, null, true)

internal fun List<ITransformation>?.transformationKey(): String {
    if (this.isNullOrEmpty()) {
        return ""
    }
    return this.joinToString("-") { it.tag() }
}

class ImageLoader(maxMemoryCacheSize: Long) {

    companion object {
        private const val CACHE_DEFAULT_MEMORY_SIZE = 1024 * 1024 * 300L

        @Volatile
        var instance: ImageLoader? = null

        fun instance(): ImageLoader {
            val i = instance
            return if (i == null) {
                synchronized(ImageLoader::class.java) {
                    instance ?: ImageLoader(CACHE_DEFAULT_MEMORY_SIZE).apply {
                        instance = this
                    }
                }
            } else {
                return i
            }
        }
    }

    private val job = SupervisorJob()
    @OptIn(DelicateCoroutinesApi::class)
    private val dispatcher: CoroutineDispatcher = newFixedThreadPoolContext(2, "caching-image-loader")
    private val scope = CoroutineScope(job)
    private var memoryLruCache = MemoryCache(maxMemoryCacheSize)

    fun newRequest(): Request {
        return Request()
    }

    private suspend fun runRequest(request: Request): ImageResponse {
        val loadFile = request.file
        return runFileLoad(loadFile!!, request.transformers)
    }

    private suspend fun runFileLoad(file: File, transformers: MutableList<ITransformation>): ImageResponse {
        return scope.async(dispatcher) {
            val key = Native.fastFileMD5(file.absolutePath) + transformers.transformationKey()
            val hasCache = memoryLruCache.getBitmap(key)
            if (hasCache != null) {
                ImageResponse(hasCache.toBitmapPainter(), null)
            } else {
                try {
                    var imageBitmap = file.inputStream().buffered().use(::loadImageBitmap)
                    for (transformer in transformers) {
                        imageBitmap = transformer.transform(imageBitmap)
                    }
                    memoryLruCache.putBitmap(key, imageBitmap)
                    ImageResponse(imageBitmap.toBitmapPainter(), null)
                } catch (e: Exception) {
                    ImageResponse(null, e)
                }
            }
        }.await()
    }

    private fun shutdown() {
        job.cancel()
    }

    suspend fun clearCacheFile(filePath : String) {
        val key = LruUtil.hashKey(filePath)
        memoryLruCache.clearCacheKey(key)
    }

    inner class Request {
        internal var file: File? = null

        private var saveStrategy = SaveStrategy.Original
        internal var transformers = mutableListOf<ITransformation>()

        fun load(file: File): Request {
            this.file = file
            return this
        }

        fun transformations(transformations: List<ITransformation>?): Request {
            if (!transformations.isNullOrEmpty()) {
                transformers.addAll(transformations)
            }
            return this
        }

        fun saveStrategy(strategy: SaveStrategy): Request {
            saveStrategy = strategy
            return this
        }

        suspend fun get(): ImageResponse {
            return try {
                runRequest(this)
            } catch (e: Exception) {
                ImageResponse(null, e)
            }
        }
    }
}