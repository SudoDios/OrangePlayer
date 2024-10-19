package me.sudodios.orangeplayer.core.media

import kotlinx.coroutines.*
import me.sudodios.mediainfo.MediaInfoLib
import me.sudodios.orangeplayer.Global
import me.sudodios.orangeplayer.core.Native
import me.sudodios.orangeplayer.models.MediaItem
import me.sudodios.orangeplayer.models.ModelFolderRead
import me.sudodios.orangeplayer.models.ModelMediaRead
import me.sudodios.orangeplayer.utils.Utils.md5
import java.io.File
import java.io.FileOutputStream
import java.util.*

object MediaStore {

    class Filter {

        private var isVideo : Boolean? = null
        private var isFav : Boolean? = null
        private var searchKeyword : String = ""
        private var folder : String = ""

        fun isVideo(isVideo: Boolean) : Filter {
            this.isVideo = isVideo
            return this
        }

        fun isFav(isFav: Boolean) : Filter {
            this.isFav = isFav
            return this
        }

        fun folder(folder : String) : Filter {
            this.folder = folder
            return this
        }

        fun searchKeyword(searchKeyword: String) : Filter {
            this.searchKeyword = searchKeyword
            return this
        }

        fun build() : String {
            val stringBuilder = StringBuilder()
            if (isVideo != null || isFav != null || searchKeyword.isNotBlank() || folder.isNotBlank()) {
                stringBuilder.append(" WHERE ")
                if (isVideo != null) {
                    stringBuilder.append("is_video=$isVideo AND ")
                }
                if (isFav != null) {
                    stringBuilder.append("is_fav=$isFav AND ")
                }
                if (folder.isNotBlank()) {
                    stringBuilder.append("folder='$folder' AND ")
                }
                if (searchKeyword.isNotBlank()) {
                    stringBuilder.append("name LIKE '%$searchKeyword%' OR artist LIKE '%$searchKeyword%' OR album LIKE '%$searchKeyword%'")
                } else {
                    if (stringBuilder.isNotEmpty()) {
                        stringBuilder.setLength(stringBuilder.length - 5)
                    }
                }
            }
            return stringBuilder.toString()
        }

    }

    class Sort {

        enum class SortValue(var index : String) {
            Name("name"),
            Duration("duration")
        }

        private var order : String? = "ASC"
        private var value : SortValue? = null

        fun order(order : String) : Sort {
            this.order = order
            return this
        }

        fun value(value: SortValue) : Sort {
            this.value = value
            return this
        }

        fun build() : String {
            val stringBuilder = StringBuilder()
            if (value != null) {
                stringBuilder.append(" ORDER BY ${value!!.index} $order")
            }
            return stringBuilder.toString()
        }

    }

    private fun durationToLong(input : String?) : Long {
        return if (input.isNullOrEmpty()) return 0L else input.toLong()
    }

    private fun coverFileExtension(mimeType : String?) : String {
        return if (mimeType.isNullOrEmpty()) {
            ""
        } else {
            when {
                mimeType.contains("jpeg") -> ".jpg"
                mimeType.contains("png") -> ".png"
                else -> ""
            }
        }
    }

    private fun removeExtension(fileName: String): String {
        val lastIndexOfDot = fileName.lastIndexOf(".")
        return if (lastIndexOfDot != -1) {
            fileName.substring(0, lastIndexOfDot)
        } else {
            fileName
        }
    }

    private fun mediaInfoExtraction(mediaItem: MediaItem) {
        //set options
        val mediaInfo = Native.mi?.New()
        Native.mi?.Option(mediaInfo,"ParseSpeed",if (mediaItem.extension == "aac") "1.0" else "0.5")
        Native.mi?.Option(mediaInfo,"Cover_Data","base64")
        Native.mi?.Option(mediaInfo,"Language","raw")
        //open file
        Native.mi?.Open(mediaInfo,mediaItem.path)
        //get info
        val trackName = Native.mi?.Get(mediaInfo,MediaInfoLib.StreamKind.General,0,"Track")
        val albumName = Native.mi?.Get(mediaInfo,MediaInfoLib.StreamKind.General,0,"Album")
        val artistName = Native.mi?.Get(mediaInfo,MediaInfoLib.StreamKind.General,0,"Artist")
        val duration = Native.mi?.Get(mediaInfo,MediaInfoLib.StreamKind.General,0,"Duration")
        val isVideo = Native.mi?.Count_Get(mediaInfo,MediaInfoLib.StreamKind.Video)!! > 0
        if (!trackName.isNullOrEmpty()) {
            mediaItem.name = trackName.trim()
        }
        mediaItem.album = albumName.orEmpty().trim()
        mediaItem.artist = artistName.orEmpty().trim()
        mediaItem.duration = durationToLong(duration)
        mediaItem.isVideo = isVideo
        if (isVideo) {
            val width = Native.mi?.Get(mediaInfo,MediaInfoLib.StreamKind.Video,0,"Width")
            val height = Native.mi?.Get(mediaInfo,MediaInfoLib.StreamKind.Video,0,"Height")
            if (!width.isNullOrBlank()) {
                mediaItem.width = width.toInt()
            }
            if (!height.isNullOrBlank()) {
                mediaItem.height = height.toInt()
            }
        } else {
            //get cover of audio
            val haveCover = Native.mi?.Get(mediaInfo,MediaInfoLib.StreamKind.General,0,"Cover").toString().lowercase() == "yes"
            if (haveCover) {
                val coverData = Native.mi?.Get(mediaInfo,MediaInfoLib.StreamKind.General,0,"Cover_Data").toString()
                val coverMime = Native.mi?.Get(mediaInfo,MediaInfoLib.StreamKind.General,0,"Cover_Mime")
                val coverExt = coverFileExtension(coverMime)
                val data: ByteArray = Base64.getDecoder().decode(coverData)
                val file = File("${Global.COVER_PATH}/${mediaItem.path.md5()}$coverExt")
                file.createNewFile()
                FileOutputStream(file).use { stream ->
                    stream.write(data)
                }
                mediaItem.coverPath = file.absolutePath
            }
        }
        Native.mi?.Close(mediaInfo)
        Native.mi?.Delete(mediaInfo)
    }

    fun scanPaths(returnItems : Boolean,paths : Array<String>,result : (vidCount : Int,audioCount : Int,List<MediaItem>?) -> Unit) {
        CoroutineScope(Dispatchers.Default).launch {
            val searchedPaths = Native.searchMedia(paths)
            val scannedItems = searchedPaths.mapParallel {
                val mediaItem = MediaItem().apply {
                    name = removeExtension(it.fileName).trim()
                    path = it.path
                    size = it.size
                    extension = it.extension
                    folder = it.folder
                    hash = it.hash
                }
                mediaInfoExtraction(mediaItem)
                mediaItem
            }
            Native.dbInsertItems(scannedItems.toTypedArray())
            val videoCount = scannedItems.count { it.isVideo }
            val audiosCount = scannedItems.count { !it.isVideo }
            withContext(Dispatchers.Default) {
                result.invoke(videoCount,audiosCount,if (returnItems) scannedItems else null)
            }
        }
    }

    fun readMediaFiles(filter: Filter,sort: Sort) : ModelMediaRead {
        val filterStr = filter.build()
        val sortStr = sort.build()
        return Native.dbReadMediaItems("$filterStr$sortStr")
    }

    fun readMediaFolders() : Array<ModelFolderRead> {
        return Native.dbReadFolders()
    }

    private suspend inline fun <T, R> Array<out T>.mapParallel(
        crossinline transform: suspend (T) -> R
    ): List<R> = coroutineScope {
        map { async { transform(it) } }.map { it.await() }
    }

}