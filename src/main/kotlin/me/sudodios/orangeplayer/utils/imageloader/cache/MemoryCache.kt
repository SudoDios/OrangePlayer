package me.sudodios.orangeplayer.utils.imageloader.cache

import androidx.compose.ui.graphics.ImageBitmap
import me.sudodios.orangeplayer.utils.imageloader.cache.core.LruCache

class MemoryCache(size: Long) {

    private val lruCache: LruCache<String, ImageBitmap> = LruCache(size, sizeCalculator = { _, value ->
        (value.width * value.height).toLong()
    })

    suspend fun clearCacheKey(key: String) {
        lruCache.remove(key)
    }

    suspend fun getBitmap(key: String): ImageBitmap? {
        return lruCache.get(key)
    }

    suspend fun putBitmap(key: String?, bitmap: ImageBitmap?) {
        if (key.isNullOrEmpty() || bitmap == null) {
            return
        }
        lruCache.put(key, bitmap)
    }
}