package me.sudodios.orangeplayer.ui.sections.list

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.*
import me.sudodios.orangeplayer.core.Native
import me.sudodios.orangeplayer.core.media.MediaStore
import me.sudodios.orangeplayer.models.MediaItem
import me.sudodios.orangeplayer.models.ModelFolderRead
import me.sudodios.orangeplayer.models.ModelPlaylistsRead
import me.sudodios.orangeplayer.utils.Events
import me.sudodios.orangeplayer.utils.Utils.formatToDurationInfo
import java.io.File

object PageSection {

    enum class Page(var title : String,var icon : String) {
        ALL_MEDIA("All Media","icons/media-library.svg"),
        VIDEOS("Videos","icons/video-cam.svg"),
        AUDIOS("Audios","icons/music-note.svg"),
        FAVORITES("Favorites","icons/heart.svg"),
        FOLDERS("Folders","icons/folder.svg"),
        PLAYLISTS("Playlists","icons/playlist.svg"),
    }

    val selectedMenu = mutableStateOf(Page.PLAYLISTS)

    var pageTitle = mutableStateOf(Page.ALL_MEDIA.title)
    var pageIcon = mutableStateOf(Page.ALL_MEDIA.icon)
    var pageDesc = mutableStateOf("")
    val pageList = SnapshotStateList<Any>()
    var pageIsChild = mutableStateOf(false)
    var searchKeyword = mutableStateOf("")

    init {
        changePage(Page.ALL_MEDIA,false)
    }

    private fun <T: Any> updateList(list : Array<T>) {
        pageList.clear()
        pageList.addAll(list)
    }

    fun Int.suffixItems() : String {
        return if (this == 1) "1 item" else if (this == 0) "No items" else "$this items"
    }
    fun Int.suffixFolders() : String {
        return if (this == 1) "1 folder" else if (this == 0) "No folder" else "$this folders"
    }
    fun Int.suffixPlaylists() : String {
        return if (this == 1) "1 playlist" else if (this == 0) "No playlist" else "$this playlists"
    }

    private fun formatMediaDescPage(count : Int,duration : Long) : String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(count.suffixItems())
        if (duration > 0) {
            stringBuilder.append(" • ")
            stringBuilder.append(duration.formatToDurationInfo())
        }
        return stringBuilder.toString()
    }
    private fun readMedia(filter: MediaStore.Filter) {
        Events.showProgressLoading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            delay(100)
            val result = MediaStore.readMediaFiles(filter,MediaStore.Sort().value(MediaStore.Sort.SortValue.Name))
            withContext(Dispatchers.Main) {
                pageDesc.value = formatMediaDescPage(result.count,result.duration)
                updateList(result.mediaList)
                Events.showProgressLoading.value = false
            }
        }
    }
    private fun readMediaPlaylist(pId : Int) {
        Events.showProgressLoading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            delay(100)
            val result = Native.dbPlaylistsReadItems(pId)
            withContext(Dispatchers.Main) {
                pageDesc.value = formatMediaDescPage(result.count,result.duration)
                updateList(result.mediaList)
                Events.showProgressLoading.value = false
            }
        }
    }

    fun changePage(page : Page,force : Boolean) {
        if (page != selectedMenu.value || force) {
            pageList.clear()
            pageTitle.value = page.title
            pageIcon.value = page.icon
            pageIsChild.value = false

            searchKeyword.value = ""

            when (page) {
                Page.ALL_MEDIA -> {
                    readMedia(MediaStore.Filter())
                }
                Page.VIDEOS -> {
                    readMedia(MediaStore.Filter().isVideo(true))
                }
                Page.AUDIOS -> {
                    readMedia(MediaStore.Filter().isVideo(false))
                }
                Page.FAVORITES -> {
                    readMedia(MediaStore.Filter().isFav(true))
                }
                Page.FOLDERS -> {
                    val folders = MediaStore.readMediaFolders()
                    pageDesc.value = folders.size.suffixFolders()
                    updateList(folders)
                }
                Page.PLAYLISTS -> {
                    val playlists = Native.dbPlaylistsRead()
                    pageDesc.value = playlists.size.suffixPlaylists()
                    updateList(playlists)
                }
            }

            selectedMenu.value = page
        }
    }

    fun updateSearchKeyword(keyword : String) {
        if (keyword.isEmpty()) {
            changePage(selectedMenu.value,true)
        } else {
            searchKeyword.value = keyword
            pageTitle.value = "Search"
            pageIcon.value = "icons/search-normal.svg"
            readMedia(MediaStore.Filter().searchKeyword(keyword))
        }
    }

    fun openFolder(folder : ModelFolderRead) {
        pageList.clear()
        pageIsChild.value = true
        pageTitle.value = folder.folder.substringAfterLast(File.separator)
        readMedia(MediaStore.Filter().folder(folder.folder))
    }

    fun removeMediaItemFromList(model : MediaItem) {
        if (pageList[0] is MediaItem) {
            pageList.remove(model)
            var duration = 0L
            for (mediaItem in pageList) {
                duration += (mediaItem as MediaItem).duration
            }
            pageDesc.value = formatMediaDescPage(pageList.size,duration)
        }
    }

    var currentPlaylistId : Int = -1
    fun openPlaylist(playlist : ModelPlaylistsRead) {
        currentPlaylistId = playlist.id
        pageList.clear()
        pageIsChild.value = true
        pageTitle.value = playlist.title
        readMediaPlaylist(playlist.id)
    }

    fun closeChild() {
        pageIsChild.value = false
        changePage(selectedMenu.value,force = true)
    }

}