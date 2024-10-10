package me.sudodios.orangeplayer.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import me.sudodios.orangeplayer.core.Native
import me.sudodios.orangeplayer.models.ModelPlaylistsRead
import me.sudodios.orangeplayer.ui.components.*
import me.sudodios.orangeplayer.ui.theme.ColorBox

@Composable
fun DialogAddToPlaylist(
    mediaItemPath : String,
    show : Boolean,
    onDismiss: () -> Unit,
) {
    BaseDialog(
        expanded = show,
        onDismiss
    ) {

        val playlists = remember { mutableStateListOf<ModelPlaylistsRead>() }
        val mediaPlaylists = remember { mutableStateListOf<Int>() }
        var showDialogCreate by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            playlists.clear()
            mediaPlaylists.clear()
            playlists.addAll(Native.dbPlaylistsRead())
            mediaPlaylists.addAll(Native.dbMediaPlaylists(mediaItemPath).toList())
        }

        Column(modifier = Modifier.width(410.dp).background(ColorBox.card)) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    EText(
                        text = "Playlists",
                        color = ColorBox.text,
                        style = MaterialTheme.typography.titleMedium
                    )
                    EText(
                        modifier = Modifier.padding(top = 4.dp),
                        text = "Add media to ...",
                        color = ColorBox.text.copy(0.6f),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                EButton(
                    icon = "icons/add-circle.svg",
                    text = "Add playlist",
                    textColor = ColorBox.primary,
                    iconColor = ColorBox.primary,
                    backgroundColor = Color.Transparent,
                    onClick = {
                        showDialogCreate = true
                    }
                )
            }
            Box {
                if (playlists.isEmpty()) {
                    EText(
                        modifier = Modifier.align(Alignment.Center).padding(top = 8.dp,bottom = 90.dp),
                        text = "No playlists found !",
                        color = ColorBox.text.copy(0.6f),
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    LazyColumn(modifier = Modifier.padding(bottom = 80.dp)) {
                        items(playlists) {
                            PlaylistItem(it, isSelected = mediaPlaylists.contains(it.id), onClicked = {
                                if (mediaPlaylists.contains(it.id)) {
                                    Native.dbPlaylistsDelMediaItem(it.id,mediaItemPath)
                                    mediaPlaylists.remove(it.id)
                                } else {
                                    Native.dbPlaylistsAddMediaItem(it.id,mediaItemPath)
                                    mediaPlaylists.add(it.id)
                                }
                            })
                        }
                    }
                }
                EButton(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(),
                    text = "Done !",
                    backgroundColor = ColorBox.primary.copy(0.1f),
                    centerText = true,
                    textColor = ColorBox.primary,
                    onClick = {
                        onDismiss.invoke()
                    }
                )
            }
        }

        DialogCreatePlaylist(
            showDialogCreate,
            onDismiss = {
                showDialogCreate = false
            },
            onCreateClicked = {
                showDialogCreate = false
                val pId = Native.dbPlaylistsCreate(it)
                playlists.add(0,ModelPlaylistsRead(pId,it,0,""))
            }
        )

    }

}

@Composable
private fun PlaylistItem(playlist : ModelPlaylistsRead,isSelected : Boolean,onClicked : () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().height(44.dp).clickable { onClicked.invoke() }.padding(start = 20.dp, end = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ECheckbox(isChecked = isSelected)
        EText(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp).weight(1f),
            text = playlist.title,
            color = ColorBox.text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun DialogCreatePlaylist(
    show : Boolean,
    value : String = "",
    onDismiss: () -> Unit,
    onCreateClicked : (String) -> Unit
) {
    BaseDialog(show,onDismiss) {
        var playlistTitle by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(value) {
            playlistTitle = value
        }

        Column(modifier = Modifier.width(380.dp).background(ColorBox.card), horizontalAlignment = Alignment.CenterHorizontally) {
            EText(
                modifier = Modifier.padding(top = 20.dp),
                text = if (value.isEmpty()) "Create new playlist" else "Rename playlist",
                color = ColorBox.text,
                style = MaterialTheme.typography.titleMedium
            )
            ETextField(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp).fillMaxWidth().focusRequester(focusRequester),
                value = playlistTitle,
                textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Content),
                label = {
                    EText("Playlist name")
                },
                onValueChange = {
                    playlistTitle = it
                }
            )
            Row(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                EButton(
                    modifier = Modifier.weight(1f).padding(end = 10.dp),
                    centerText = true,
                    text = "Cancel",
                    onClick = {
                        onDismiss.invoke()
                    }
                )
                EButton(
                    modifier = Modifier.weight(1f).padding(start = 10.dp),
                    centerText = true,
                    enabled = playlistTitle.isNotBlank(),
                    backgroundColor = ColorBox.primary.copy(0.1f),
                    textColor = ColorBox.primary,
                    text = if (value.isEmpty()) "Create" else "Rename",
                    onClick = {
                        onCreateClicked.invoke(playlistTitle.trim())
                    }
                )
            }
        }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}