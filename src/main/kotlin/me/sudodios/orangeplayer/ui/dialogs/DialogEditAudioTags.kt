package me.sudodios.orangeplayer.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.sudodios.orangeplayer.Global
import me.sudodios.orangeplayer.core.Native
import me.sudodios.orangeplayer.models.MediaItem
import me.sudodios.orangeplayer.ui.components.*
import me.sudodios.orangeplayer.ui.theme.ColorBox
import me.sudodios.orangeplayer.utils.Utils
import me.sudodios.orangeplayer.utils.Utils.md5
import me.sudodios.orangeplayer.utils.painterResource
import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.ArtworkFactory
import java.io.File

@Composable
fun DialogEditAudioTags(
    show : Boolean,
    onDismiss: () -> Unit,
    mediaItem: MediaItem,
    onUpdated : (MediaItem) -> Unit
) {

    BaseDialog(expanded = show) {

        var audioFile by remember{ mutableStateOf<AudioFile?>(null) }

        LaunchedEffect(Unit) {
            readAudioFile(mediaItem.path) {
                if (it != null) {
                    audioFile = it
                } else {
                    onDismiss.invoke()
                }
            }
        }

        var title by remember { mutableStateOf(mediaItem.name) }
        var artist by remember { mutableStateOf(mediaItem.artist) }
        var album by remember { mutableStateOf(mediaItem.album) }
        var cover by remember { mutableStateOf(mediaItem.coverPath) }
        var isChangedCover by remember { mutableStateOf(false) }

        var saving by remember { mutableStateOf(false) }

        fun saveTags() {
            saving = true
            CoroutineScope(Dispatchers.IO).launch {
                val coverPath = "${Global.COVER_PATH}/${mediaItem.path.md5()}.${cover.substringAfterLast(".")}"
                if (isChangedCover) {
                    audioFile?.tag?.deleteArtworkField()
                    audioFile?.tag?.setField(ArtworkFactory.createArtworkFromFile(File(cover)))
                    cover = Utils.writeThumbImage(File(cover).readBytes(),coverPath)
                }
                audioFile?.tag?.setField(FieldKey.TITLE,title.trim())
                audioFile?.tag?.setField(FieldKey.ARTIST,artist.trim())
                audioFile?.tag?.setField(FieldKey.ALBUM,album.trim())
                AudioFileIO.write(audioFile!!)
                val fileSize = File(mediaItem.path).length()
                val updatedMediaItem = mediaItem.copy(
                    name = title.trim(),
                    artist = artist.trim(),
                    album = album.trim(),
                    size = fileSize,
                    coverPath = coverPath
                )
                Native.dbInsertItems(arrayOf(updatedMediaItem))
                withContext(Dispatchers.Main) {
                    onUpdated.invoke(updatedMediaItem)
                }
            }
        }

        Column(
            modifier = Modifier.width(410.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EText(
                modifier = Modifier.padding(20.dp),
                text = "Edit Audio",
                color = ColorBox.text,
                style = MaterialTheme.typography.titleLarge
            )
            EditCoverView(cover,enabled = !saving, onCoverSelected = {
                cover = it
                isChangedCover = true
            })
            ETextField(
                modifier = Modifier.padding(start = 30.dp, top = 16.dp, bottom = 12.dp, end = 30.dp).fillMaxWidth(),
                label = { EText("Name") },
                value = title,
                enabled = !saving,
                textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Content),
                singleLine = true,
                onValueChange = {
                    title = it
                }
            )
            ETextField(
                modifier = Modifier.padding(start = 30.dp, bottom = 12.dp, end = 30.dp).fillMaxWidth(),
                label = { EText("Artist") },
                value = artist,
                enabled = !saving,
                textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Content),
                singleLine = true,
                onValueChange = {
                    artist = it
                }
            )
            ETextField(
                modifier = Modifier.padding(start = 30.dp, bottom = 16.dp, end = 30.dp).fillMaxWidth(),
                label = { EText("Album") },
                value = album,
                enabled = !saving,
                textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Content),
                singleLine = true,
                onValueChange = {
                    album = it
                }
            )
            Row(modifier = Modifier.padding(start = 30.dp, end = 30.dp, bottom = 16.dp).fillMaxWidth()) {
                EButton(
                    modifier = Modifier.weight(1f).padding(end = 10.dp),
                    centerText = true,
                    enabled = !saving,
                    text = "Close",
                    onClick = {
                        onDismiss.invoke()
                    }
                )
                EButton(
                    modifier = Modifier.weight(1f).padding(start = 10.dp),
                    centerText = true,
                    enabled = !saving,
                    backgroundColor = ColorBox.primary.copy(0.1f),
                    textColor = ColorBox.primary,
                    text = "Save metadata",
                    onClick = {
                        saveTags()
                    }
                )
            }
        }
    }

}

private suspend fun readAudioFile(path : String,onResult : (AudioFile?) -> Unit){
    withContext(Dispatchers.IO) {
        try {
            val result = AudioFileIO.read(File(path))
            withContext(Dispatchers.Main) {
                onResult.invoke(result)
            }
        } catch (e : Exception) {
            withContext(Dispatchers.Main) {
                onResult.invoke(null)
            }
        }
    }

}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun EditCoverView(cover: String?,enabled : Boolean, onCoverSelected: (String) -> Unit) {

    var onMouseHover by remember { mutableStateOf(false) }

    Box(modifier = Modifier.size(100.dp)
        .onPointerEvent(PointerEventType.Enter) {
            onMouseHover = true
        }
        .onPointerEvent(PointerEventType.Exit) {
            onMouseHover = false
        }.clip(RoundedCornerShape(16.dp)).background(ColorBox.window)
    )
    {
        SmoothImage(modifier = Modifier.fillMaxSize(), placeTint = ColorBox.primary.copy(0.4f), image = cover)
        AnimatedVisibility(
            modifier = Modifier.align(Alignment.BottomCenter),
            visible = onMouseHover,
            enter = slideInVertically(animationSpec = tween(120), initialOffsetY = {
                it
            }),
            exit = slideOutVertically(animationSpec = tween(120), targetOffsetY = {
                it
            })
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(ColorBox.window.copy(0.5f))
                    .clickable(enabled) {
                        val imageSel = Utils.openFilePicker(title = "Choice Image", "jpg", "jpeg", "png")
                        if (imageSel != null) {
                            onCoverSelected.invoke(imageSel.absolutePath)
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    modifier = Modifier.size(22.dp),
                    painter = painterResource("icons/gallery-edit.svg"),
                    contentDescription = null,
                    tint = ColorBox.text
                )
            }
        }
    }
}