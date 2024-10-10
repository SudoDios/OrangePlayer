package me.sudodios.orangeplayer.ui.dialogs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import me.sudodios.orangeplayer.core.media.Player
import me.sudodios.orangeplayer.ui.components.BaseDialog
import me.sudodios.orangeplayer.ui.components.EButton
import me.sudodios.orangeplayer.ui.components.EText
import me.sudodios.orangeplayer.ui.theme.ColorBox
import me.sudodios.orangeplayer.ui.theme.Fonts

@Composable
fun DialogPlayerStatistics(
    show : Boolean,
    onDismiss: () -> Unit,
) {

    BaseDialog(
        expanded = show,
        onDismissRequest = onDismiss,
    ) {

        val statistics = Player.Live.statistics.value

        Column(modifier = Modifier.width(390.dp)) {
            EText(
                modifier = Modifier.padding(16.dp),
                text = "Stream Statistics",
                color = ColorBox.text,
                style = MaterialTheme.typography.bodyLarge
            )
            Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp)
                .drawBehind {
                    drawRoundRect(
                        color = ColorBox.card2,
                        cornerRadius = CornerRadius(12f.dp.toPx(),12f.dp.toPx())
                    )
                }.padding(12.dp)) {
                EText(
                    text = "Audio",
                    color = ColorBox.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
                DataText(
                    modifier = Modifier,
                    key = "Decoded",
                    value = "${statistics?.decodedAudio()} Blocks"
                )
                DataText(
                    modifier = Modifier,
                    key = "Played",
                    value = "${statistics?.audioBuffersPlayed()} Buffers"
                )
                DataText(
                    modifier = Modifier,
                    key = "Lost",
                    value = "${statistics?.audioBuffersLost()} Buffers"
                )
            }
            Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 16.dp, end = 16.dp)
                .drawBehind {
                    drawRoundRect(
                        color = ColorBox.card2,
                        cornerRadius = CornerRadius(12f.dp.toPx(),12f.dp.toPx())
                    )
                }.padding(12.dp)) {
                EText(
                    text = "Video",
                    color = ColorBox.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
                DataText(
                    modifier = Modifier,
                    key = "Decoded",
                    value = "${statistics?.decodedVideo()} Blocks"
                )
                DataText(
                    modifier = Modifier,
                    key = "Played",
                    value = "${statistics?.picturesDisplayed()} Frames"
                )
                DataText(
                    modifier = Modifier,
                    key = "Lost",
                    value = "${statistics?.picturesLost()} Frames"
                )
            }

            EButton(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
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

}

@Composable
private fun DataText(modifier: Modifier,key : String,value : String) {

    val textMeasurer = rememberTextMeasurer()

    val keyText = textMeasurer.measure(key, style = MaterialTheme.typography.bodySmall.copy(fontFamily = Fonts.mainFont))
    val valueText = textMeasurer.measure(value, style = MaterialTheme.typography.bodySmall.copy(fontFamily = Fonts.numbersFont))

    Canvas(modifier.height((keyText.size.height + 12).dp).fillMaxWidth()) {
        drawText(
            textLayoutResult = keyText,
            color = ColorBox.text,
            topLeft = Offset(0f,size.height /2 - valueText.size.height / 2)
        )
        drawText(
            textLayoutResult = valueText,
            color = ColorBox.text,
            topLeft = Offset(size.width - valueText.size.width,size.height /2 - valueText.size.height / 2)
        )
    }
}