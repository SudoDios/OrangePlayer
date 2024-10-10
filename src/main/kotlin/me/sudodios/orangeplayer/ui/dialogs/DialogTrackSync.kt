package me.sudodios.orangeplayer.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.sudodios.orangeplayer.core.media.Player
import me.sudodios.orangeplayer.ui.components.BaseDialog
import me.sudodios.orangeplayer.ui.components.EButton
import me.sudodios.orangeplayer.ui.components.EIconButton
import me.sudodios.orangeplayer.ui.components.EText
import me.sudodios.orangeplayer.ui.theme.ColorBox
import me.sudodios.orangeplayer.ui.theme.Fonts
import me.sudodios.orangeplayer.utils.Utils.roundTo

@Composable
fun DialogTrackSync(
    show : Boolean,
    onDismissRequest: () -> Unit,
) {
    BaseDialog(
        expanded = show,
        onDismissRequest = { onDismissRequest.invoke() }
    ) {

        var audioToVideo by remember { mutableStateOf("") }
        var subtitleToVideo by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            audioToVideo = Player.getSyncAudioToVideo().roundTo(1).toString()
            subtitleToVideo = Player.getSyncSubtitleToVideo().roundTo(1).toString()
        }

        Column(modifier = Modifier.width(390.dp)) {
            EText(
                modifier = Modifier.padding(16.dp),
                text = "Track Synchronization",
                color = ColorBox.text,
                style = MaterialTheme.typography.bodyLarge
            )


            SyncItem(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                title = "Audio -> Video",
                value = audioToVideo,
                onValueChanged = {
                    audioToVideo = it
                    if (it != "-" && it.isNotEmpty()) {
                        Player.syncAudioToVideo(it.toDouble())
                    }
                }
            )
            SyncItem(
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                title = "Subtitle -> Video",
                value = subtitleToVideo,
                onValueChanged = {
                    subtitleToVideo = it
                    if (it != "-" && it.isNotEmpty()) {
                        Player.syncSubtitleToVideo(it.toDouble())
                    }
                }
            )
            EButton(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                text = "Done !",
                backgroundColor = ColorBox.primary.copy(0.1f),
                centerText = true,
                textColor = ColorBox.primary,
                onClick = {
                    onDismissRequest.invoke()
                }
            )
        }
    }


}

@Composable
private fun SyncItem(
    modifier: Modifier = Modifier,
    title : String,
    value : String,
    onValueChanged : (String) -> Unit,
) {

    val onlyNumberDotsReg = remember { Regex("^-?\\d*\\.?\\d*\$") }

    Row(modifier = modifier.fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(ColorBox.card2)
        .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        EText(
            text = title,
            color = ColorBox.text,
            style = MaterialTheme.typography.bodyMedium
        )
        BasicTextField(
            modifier = Modifier.padding(start = 16.dp).weight(1f),
            textStyle = LocalTextStyle.current.copy(fontFamily = Fonts.numbersFont, textAlign = TextAlign.End, color = ColorBox.text),
            value = value,
            cursorBrush = SolidColor(ColorBox.text),
            onValueChange = {
                if (it.isEmpty() || it.matches(onlyNumberDotsReg) && it.length < 7) {
                    onValueChanged.invoke(it)
                }
            }
        )
        EText(
            modifier = Modifier.padding(start = 4.dp),
            text = "s",
            color = ColorBox.text,
            style = MaterialTheme.typography.labelMedium
        )
        EIconButton(
            size = 32.dp,
            padding = PaddingValues(start = 12.dp,end = 8.dp),
            contentPadding = 7.dp,
            background = ColorBox.card,
            icon = "icons/minus.svg",
            onClick = {
                onValueChanged.invoke((value.toDouble() - 0.1).roundTo(1).toString())
            }
        )
        EIconButton(
            size = 32.dp,
            contentPadding = 7.dp,
            background = ColorBox.card,
            icon = "icons/add.svg",
            onClick = {
                onValueChanged.invoke((value.toDouble() + 0.1).roundTo(1).toString())
            }
        )
    }
}