package me.sudodios.orangeplayer.utils.imageloader.cache.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

class DiskLruCache private constructor(
    private val directory: File,
    maxSize: Long,
    private val creationScope: CoroutineScope,
    private val keyTransformer: KeyTransformer?,
) {
    private val journalFile = File(directory, JOURNAL_FILE)
    private val tempJournalFile = File(directory, JOURNAL_FILE_TEMP)
    private val backupJournalFile = File(directory, JOURNAL_FILE_BACKUP)

    private val lruCache = LruCache<String, File>(
        maxSize = maxSize,
        sizeCalculator = { _, file -> file.length() },
        onEntryRemoved = { _, key, oldValue, _ -> onEntryRemoved(key, oldValue) },
        creationScope = creationScope,
    )

    private var redundantOpCount = 0
    private lateinit var journalWriter: JournalWriter
    private val journalMutex = Mutex()

    /**
     * Returns the file for [key] if it exists in the cache or wait for its creation if it is currently in progress.
     * This returns `null` if a file is not cached and isn't in creation or cannot be created.
     *
     * It may even throw exceptions for unhandled exceptions in the currently in-progress creation block.
     */
    suspend fun get(key: String): File? = lruCache.get(key.transform())?.let { CachedFile(it) }

    /**
     * Closes the journal file and cancels any in-progress creation.
     */
    private suspend fun close() {
        lruCache.mapMutex.withLock {
            if (::journalWriter.isInitialized) {
                journalMutex.withLock {
                    journalWriter.close()
                }
            }

            for (deferred in lruCache.creationMap.values) deferred.cancel()
        }
    }


    private suspend fun String.transform() =
        keyTransformer?.transform(this) ?: this

    private fun onEntryRemoved(key: String, oldValue: File) {
        creationScope.launch {
            File(oldValue.path).deleteOrThrow()
            File(oldValue.path + TEMP_EXT).deleteOrThrow()

            if (::journalWriter.isInitialized) {
                journalMutex.withLock {
                    redundantOpCount += 2
                    journalWriter.writeRemove(key)
                }

                rebuildJournalIfRequired()
            }
        }
    }

    private suspend fun clearDirectory() = lruCache.mapMutex.withLock {
        val files = lruCache.map.values
        for (file in directory.listFiles() ?: emptyArray()) {
            if (file !in files) file.deleteOrThrow()
        }
    }

    private suspend fun rebuildJournalIfRequired() {
        if (isJournalRebuildRequired) rebuildJournal()
    }

    // We only rebuild the journal when it will halve the size of the journal and eliminate at least 2000 ops.
    private val isJournalRebuildRequired: Boolean
        get() {
            val redundantOpCompactThreshold = 2000
            return (redundantOpCount >= redundantOpCompactThreshold
                    && redundantOpCount >= lruCache.map.size)
        }

    private suspend fun rebuildJournal() = lruCache.mapMutex.withLock {
        journalMutex.withLock {
            if (::journalWriter.isInitialized) journalWriter.close()

            tempJournalFile.deleteOrThrow()
            JournalWriter(tempJournalFile).use { tempWriter ->
                tempWriter.writeHeader()

                lruCache.map.forEach { (key, _) ->
                    tempWriter.writeClean(key)
                }

                lruCache.creationMap.forEach { (key, _) -> tempWriter.writeDirty(key) }
            }

            if (journalFile.exists()) journalFile.renameToOrThrow(backupJournalFile, true)
            tempJournalFile.renameToOrThrow(journalFile, false)
            backupJournalFile.delete()

            journalWriter = JournalWriter(journalFile)
            redundantOpCount = 0
        }
    }

    companion object {
        private const val TEMP_EXT = ".tmp"
        private const val JOURNAL_FILE = "journal"
        private const val JOURNAL_FILE_TEMP = JOURNAL_FILE + TEMP_EXT
        private const val JOURNAL_FILE_BACKUP = "$JOURNAL_FILE.bkp"
    }
}