package me.sudodios.orangeplayer.ui.sections.list

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.sudodios.orangeplayer.core.Native
import me.sudodios.orangeplayer.core.media.Player
import me.sudodios.orangeplayer.models.MediaItem
import me.sudodios.orangeplayer.models.ModelFolderRead
import me.sudodios.orangeplayer.models.ModelPlaylistsRead
import me.sudodios.orangeplayer.ui.components.EIconButton
import me.sudodios.orangeplayer.ui.components.EText
import me.sudodios.orangeplayer.ui.components.SmoothImage
import me.sudodios.orangeplayer.ui.components.menu.NestedPopupMenu
import me.sudodios.orangeplayer.ui.components.menu.dropDownMenu
import me.sudodios.orangeplayer.ui.dialogs.*
import me.sudodios.orangeplayer.ui.sections.list.PageSection.suffixItems
import me.sudodios.orangeplayer.ui.theme.ColorBox
import me.sudodios.orangeplayer.ui.theme.Fonts
import me.sudodios.orangeplayer.utils.Events
import me.sudodios.orangeplayer.utils.Utils.formatToDuration
import me.sudodios.orangeplayer.utils.Utils.openLocation
import me.sudodios.orangeplayer.utils.imageloader.transform.ResizeTransformation
import me.sudodios.orangeplayer.utils.painterResource
import java.io.File

@Composable
fun PageList() {

    val emptyIcon = painterResource("icons/box-search.svg")
    val emptyText = rememberTextMeasurer().measure("No Item Found !!", style = MaterialTheme.typography.headlineSmall)
    val columnCellType =
        if ((PageSection.selectedMenu.value == PageSection.Page.FOLDERS ||
            PageSection.selectedMenu.value == PageSection.Page.PLAYLISTS) && !PageSection.pageIsChild.value && PageSection.searchKeyword.value.isEmpty()
        ) GridCells.Adaptive(150.dp) else {
            if (Events.viewIsList.value) {
                GridCells.Fixed(1)
            } else {
                GridCells.Adaptive(430.dp)
            }
        }

    val scope = rememberCoroutineScope()
    DisposableEffect(Unit) {
        val observer : (Boolean) -> Unit = {
            val playingMedia = Player.Live.currentMedia.value
            if (playingMedia != null) {
                val findItemIndex = PageSection.pageList.indexOf(playingMedia)
                if (findItemIndex != -1) {
                    scope.launch {
                        PageSection.lazyGridState.scrollToItem(findItemIndex)
                    }
                }
            }
        }
        Events.goPlayingItemInList.addObserver(observer)
        onDispose {
            Events.goPlayingItemInList.removeObserver(observer)
        }
    }

    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize().drawBehind {
            drawEmptyState(PageSection.pageList.isEmpty(), emptyIcon, emptyText)
        }.draggable(
            orientation = Orientation.Vertical,
            state = rememberDraggableState { delta ->
                scope.launch {
                    PageSection.lazyGridState.scrollBy(-delta)
                }
            },
        ),
        columns = columnCellType,
        contentPadding = PaddingValues(8.dp),
        state = PageSection.lazyGridState
    ) {
        items(PageSection.pageList) {
            when (it) {
                is MediaItem -> {
                    MediaItemRow(it, onClicked = {
                        @Suppress("UNCHECKED_CAST")
                        Player.startPlay(it, playList = PageSection.pageList as List<MediaItem>)
                    })
                }
                is ModelFolderRead -> {
                    FolderItemRow(it, onClicked = {
                        PageSection.openFolder(it)
                    })
                }
                is ModelPlaylistsRead -> {
                    PlaylistItemRow(it, onClicked = {
                        PageSection.openPlaylist(it)
                    })
                }
            }
        }
    }

}

private fun DrawScope.drawEmptyState(show: Boolean, icon: Painter, text: TextLayoutResult) {
    if (show) {
        translate(size.width / 2 - 55f.dp.toPx(), size.height / 2 - 75f.dp.toPx() - text.size.height) {
            with(icon) {
                draw(Size(110f.dp.toPx(), 110f.dp.toPx()), colorFilter = ColorFilter.tint(ColorBox.primary.copy(0.7f)))
            }
        }
        drawText(
            textLayoutResult = text,
            topLeft = Offset(size.width / 2 - text.size.width / 2, size.height / 2 + 38f.dp.toPx()),
            color = ColorBox.text.copy(0.7f)
        )
    }
}

@Composable
private fun MediaItemRow(mediaItem: MediaItem, onClicked: () -> Unit) {
    Row(
        modifier = Modifier.padding(2.dp).height(58.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (Player.Live.currentMedia.value?.path == mediaItem.path) ColorBox.primary.copy(0.3f) else Color.Transparent)
            .clickable {
                onClicked.invoke()
            }.padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SmoothImage(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)),
            image = mediaItem.coverPath,
            fadeOnChange = false,
            transformer = ResizeTransformation(60,null),
            placeHolder = if (mediaItem.isVideo) "icons/video-placeholder.svg" else "icons/audio-placeholder.svg",
            placeTint = ColorBox.text.copy(0.7f)
        )
        Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp).weight(1f)) {
            val nameText = buildAnnotatedString {
                if (mediaItem.isFav) {
                    withStyle(style = SpanStyle(color = Color(0xFFfe251b))) {
                        append("♥ ")
                    }
                }
                append(mediaItem.name)
            }
            EText(
                text = nameText,
                color = ColorBox.text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val stringBuilder = StringBuilder()
            if (mediaItem.isVideo) {
                stringBuilder.append("${mediaItem.width} x ${mediaItem.height} • ")
            } else {
                if (mediaItem.artist.isNotBlank()) {
                    stringBuilder.append("${mediaItem.artist} • ")
                }
            }
            stringBuilder.append(mediaItem.extension)
            EText(
                modifier = Modifier.padding(top = 4.dp),
                text = stringBuilder.toString(),
                color = ColorBox.text.copy(0.5f),
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
        EText(
            modifier = Modifier.padding(end = 12.dp),
            text = mediaItem.duration.formatToDuration(),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = Fonts.numbersFont,
            color = ColorBox.text.copy(0.8f)
        )

        var showMenu by remember { mutableStateOf(false) }
        var showEditDialog by remember { mutableStateOf(false) }
        var showRemoveDialog by remember { mutableStateOf(false) }
        var showMIeDialog by remember { mutableStateOf(false) }
        var showPlaylistDialog by remember { mutableStateOf(false) }

        EIconButton(
            size = 34.dp,
            icon = "icons/hor-menu.svg",
            contentPadding = 7.dp,
            onClick = {
                showMenu = true
            }
        )

        NestedPopupMenu(
            expanded = showMenu,
            onDismissRequested = {showMenu = false},
            menuItems = dropDownMenu {
                item("info","Media Information","icons/info.svg", onClicked = {
                    showMenu = false
                    showMIeDialog = true
                })
                item("fav",if (mediaItem.isFav) "Remove from favorites" else "Add to favorites",
                    icon = if (mediaItem.isFav) "icons/heart.svg" else "icons/heart-bold.svg"
                    , onClicked = {
                        showMenu = false
                        val itemIndex = PageSection.pageList.indexOf(mediaItem)
                        if (mediaItem.isFav) {
                            Native.dbRemoveMediaFromFav(mediaItem.path)
                            PageSection.pageList[itemIndex] = mediaItem.copy(isFav = false)
                        } else {
                            Native.dbAddMediaToFav(mediaItem.path)
                            PageSection.pageList[itemIndex] = mediaItem.copy(isFav = true)
                        }
                    }
                )
                if (mediaItem.extension == "mp3" || mediaItem.extension == "m4a") {
                    item("edit","Edit Audio","icons/edit.svg", onClicked = {
                        showMenu = false
                        showEditDialog = true
                    })
                }
                item("playlist","Edit Playlist","icons/playlist.svg", onClicked = {
                    showMenu = false
                    showPlaylistDialog = true
                })
                item("delete","Remove File","icons/trash.svg", onClicked = {
                    showMenu = false
                    showRemoveDialog = true
                })
                item("location","Open File Location","icons/location.svg", onClicked = {
                    showMenu = false
                    File(mediaItem.path).openLocation()
                })
            }
        )

        DialogRawMediaInfo(
            show = showMIeDialog,
            onDismiss = {
                showMIeDialog = false
            },
            path = mediaItem.path
        )

        DialogEditAudioTags(
            show = showEditDialog,
            onDismiss = {
                showEditDialog = false
            },
            mediaItem = mediaItem,
            onUpdated = { updatedMediaItem ->
                showEditDialog = false
                val index = PageSection.pageList.indexOf(mediaItem)
                PageSection.pageList[index] = updatedMediaItem
                if (Player.Live.currentMedia.value?.path == updatedMediaItem.path) {
                    Player.Live.currentMedia.value = updatedMediaItem
                }
            }
        )

        DialogAddToPlaylist(
            mediaItemPath = mediaItem.path,
            show = showPlaylistDialog,
            onDismiss = {
                showPlaylistDialog = false
                if (PageSection.selectedMenu.value == PageSection.Page.PLAYLISTS) {
                    val mediaPlaylists = Native.dbMediaPlaylists(mediaItem.path)
                    if (!mediaPlaylists.contains(PageSection.currentPlaylistId)) {
                        PageSection.removeMediaItemFromList(mediaItem)
                    }
                }
            }
        )

        DialogRemove(
            show = showRemoveDialog,
            item = mediaItem,
            onDismiss = {
                showRemoveDialog = false
            },
            onRemoved = {
                showRemoveDialog = false
                PageSection.removeMediaItemFromList(mediaItem)
                if (Player.Live.currentMedia.value == mediaItem) {
                    Player.stop()
                    Player.Live.currentMedia.value = null
                }
            }
        )
    }
}

@Composable
private fun FolderItemRow(modelFolderRead: ModelFolderRead,onClicked: () -> Unit) {

    val textMeasurer = rememberTextMeasurer()
    val iconFolder = painterResource("icons/folder.svg")
    val folderNameText = textMeasurer.measure(
        modelFolderRead.folder.substringAfterLast(File.separator),
        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = Fonts.mainFont),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    val folderCountText = textMeasurer.measure(
        modelFolderRead.count.suffixItems(),
        style = MaterialTheme.typography.labelSmall.copy(fontFamily = Fonts.mainFont)
    )

    Canvas(modifier = Modifier.padding(2.dp).fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp)).clickable { onClicked.invoke() }) {
        translate(size.width / 2 - 30f.dp.toPx(),20f.dp.toPx()) {
            with(iconFolder) {
                draw(Size(60f.dp.toPx(),60f.dp.toPx()), colorFilter = ColorFilter.tint(ColorBox.text.copy(0.8f)))
            }
        }
        drawText(
            textLayoutResult = folderNameText,
            color = ColorBox.text,
            topLeft = Offset(size.width / 2 - folderNameText.size.width / 2f,92f.dp.toPx())
        )
        drawText(
            textLayoutResult = folderCountText,
            color = ColorBox.text.copy(0.6f),
            topLeft = Offset(size.width / 2 - folderCountText.size.width / 2f,98f.dp.toPx() + folderNameText.size.height)
        )
    }
}

@Composable
private fun PlaylistItemRow(modelPlaylistsRead: ModelPlaylistsRead,onClicked: () -> Unit) {

    val textMeasurer = rememberTextMeasurer()
    val folderNameText = textMeasurer.measure(
        modelPlaylistsRead.title.substringAfterLast(File.separator),
        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = Fonts.mainFont),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    val folderCountText = textMeasurer.measure(
        modelPlaylistsRead.itemCount.suffixItems(),
        style = MaterialTheme.typography.labelSmall.copy(fontFamily = Fonts.mainFont)
    )

    Box(modifier = Modifier.padding(2.dp).fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp))
        .drawBehind {
            drawText(
                textLayoutResult = folderNameText,
                color = ColorBox.text,
                topLeft = Offset(size.width / 2 - folderNameText.size.width / 2f,92f.dp.toPx())
            )
            drawText(
                textLayoutResult = folderCountText,
                color = ColorBox.text.copy(0.6f),
                topLeft = Offset(size.width / 2 - folderCountText.size.width / 2f,98f.dp.toPx() + folderNameText.size.height)
            )
        }
        .clickable { onClicked.invoke() }) {
        SmoothImage(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 20.dp).size(60.dp).clip(RoundedCornerShape(50)),
            image = modelPlaylistsRead.coverArt,
            placeHolder = "icons/playlist.svg",
            placeTint = ColorBox.text
        )

        var showMenu by remember { mutableStateOf(false) }
        var showDialogDelete by remember { mutableStateOf(false) }
        var showDialogUpdate by remember { mutableStateOf(false) }
        EIconButton(
            modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
            size = 34.dp,
            contentPadding = 6.dp,
            background = ColorBox.text.copy(0.02f),
            icon = "icons/hor-menu.svg",
            onClick = {
                showMenu = true
            }
        )

        NestedPopupMenu(expanded = showMenu,
            onDismissRequested = {showMenu = false},
            menuItems = dropDownMenu {
                item("rename","Rename","icons/edit.svg",onClicked = {
                    showMenu = false
                    showDialogUpdate = true
                })
                item("del","Delete","icons/trash.svg",onClicked = {
                    showMenu = false
                    showDialogDelete = true
                })
            }
        )

        DialogRemove(show = showDialogDelete,
            onDismiss = {showDialogDelete = false},
            item = modelPlaylistsRead,
            onRemoved = {
                showDialogDelete = false
                PageSection.pageList.remove(modelPlaylistsRead)
            }
        )

        DialogCreatePlaylist(
            show = showDialogUpdate,
            value = modelPlaylistsRead.title,
            onDismiss = {showDialogUpdate = false},
            onCreateClicked = {
                showDialogUpdate = false
                Native.dbPlaylistsUpdate(modelPlaylistsRead.id,it)
                val index = PageSection.pageList.indexOf(modelPlaylistsRead)
                if (index != -1) {
                    PageSection.pageList[index] = modelPlaylistsRead.copy(title = it)
                }
            }
        )

    }

}