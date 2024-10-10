package me.sudodios.orangeplayer.ui.sections.players

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.sudodios.orangeplayer.core.media.Player
import me.sudodios.orangeplayer.ui.components.*
import me.sudodios.orangeplayer.ui.components.menu.RepeatModePopup
import me.sudodios.orangeplayer.ui.components.menu.SpeedPlaybackPopup
import me.sudodios.orangeplayer.ui.theme.ColorBox
import me.sudodios.orangeplayer.ui.theme.Fonts
import me.sudodios.orangeplayer.utils.Utils.formatToDuration
import me.sudodios.orangeplayer.utils.imageloader.transform.ResizeTransformation
import org.jetbrains.skia.Point

var progressSeekValue = mutableStateOf(0f)
var isSeeking = mutableStateOf(false)

@Composable
private fun PlaybackProgress(modifier: Modifier) {
    CustomSlider(
        modifier = modifier,
        value = if (isSeeking.value) progressSeekValue.value else Player.Live.progressCallback.value,
        valueChanged = {
            progressSeekValue.value = it
            isSeeking.value = true
        },
        valueChangedFinished = {
            isSeeking.value = false
            Player.seekTo(progressSeekValue.value)
        }
    )
}

@Composable
private fun PositionText() {
    val total = Player.Live.currentMedia.value?.duration ?: 0
    val progress = if (isSeeking.value) progressSeekValue.value else Player.Live.progressCallback.value
    val current = (progress * total).toLong()
    EText(
        modifier = Modifier.padding(top = 4.dp),
        text = "${current.formatToDuration()} / ${total.formatToDuration()}",
        color = ColorBox.text.copy(0.5f),
        style = MaterialTheme.typography.labelMedium.copy(fontFamily = Fonts.numbersFont)
    )
}

@Composable
private fun BoxWaveform(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {

    val points = remember { ArrayList<Point>() }
    val conPoint1 = remember { ArrayList<Point>() }
    val conPoint2 = remember { ArrayList<Point>() }

    val path = remember { Path() }
    var canvasSize = remember { Size.Zero }

    fun calcPoints(size: Size, data: List<Float>, points : ArrayList<Point>) {
        points.clear()
        val bottomY = size.height
        val xDiff = size.width / (data.size - 1)
        val maxData = data.max()
        for (i in data.indices) {
            val y = (data[i] / maxData * bottomY)
            points.add(Point(xDiff * i, y))
        }
    }
    fun calcCons(points: ArrayList<Point>, conPoint1 : ArrayList<Point>, conPoint2 : ArrayList<Point>) {
        conPoint1.clear()
        conPoint2.clear()
        for (i in 1 until points.size) {
            conPoint1.add(Point((points[i].x + points[i - 1].x) / 2, points[i - 1].y))
            conPoint2.add(Point((points[i].x + points[i - 1].x) / 2, points[i].y))
        }
    }

    val waveform = Player.Live.waveform.value
    LaunchedEffect(waveform) {
        if (waveform != null && waveform.isNotEmpty()) {
            calcPoints(canvasSize,waveform.toList(),points)
            calcCons(points,conPoint1,conPoint2)
        } else {
            points.clear()
            conPoint1.clear()
            conPoint2.clear()
        }
    }

    Box(modifier
        .onSizeChanged {
            canvasSize = Size(it.width.toFloat(),it.height.toFloat() * 0.3f)
            if (waveform != null && waveform.isNotEmpty()) {
                calcPoints(canvasSize,waveform.toList(),points)
                calcCons(points,conPoint1,conPoint2)
            } else {
                points.clear()
                conPoint1.clear()
                conPoint2.clear()
            }
        }
        .drawBehind {
            path.reset()
            if (points.isNotEmpty()) {
                path.moveTo(0f,0f)
                path.lineTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    path.cubicTo(
                        conPoint1[i - 1].x, conPoint1[i - 1].y, conPoint2[i - 1].x, conPoint2[i - 1].y,
                        points[i].x, points[i].y
                    )
                }
                path.lineTo(size.width,0f)
                path.lineTo(0f, 0f)
                drawPath(path, color = ColorBox.primary.copy(0.4f), style = Fill)
            }
        },
        content = content)
}

@Composable
fun MiniPlayer(maxWidth: Int) {
    BoxWaveform(modifier = Modifier.fillMaxWidth()) {
        PlaybackProgress(modifier = Modifier.fillMaxWidth().offset(y = -(16).dp))
        Row(modifier = Modifier.fillMaxWidth().height(84.dp).clickable { Player.Live.showLargePlayer.value = true }, verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.padding(start = 12.dp))
            SmoothImage(
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).background(ColorBox.card),
                image = Player.Live.currentMedia.value?.coverPath,
                placeHolder = "icons/app-icon.png",
                transformer = ResizeTransformation(80,null),
                fadeOnChange = true
            )
            Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp).weight(1f)) {
                EText(
                    text = Player.Live.currentMedia.value?.name.toString(),
                    color = ColorBox.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge
                )
                PositionText()
            }
            EIconButton(
                padding = PaddingValues(end = 12.dp),
                icon = "icons/previous.svg",
                size = 38.dp,
                contentPadding = 9.dp,
                colorFilter = ColorBox.text,
                background = ColorBox.text.copy(0.07f),
                onClick = {
                    Player.previous()
                }
            )
            PlayAnimationView(
                color = ColorBox.window,
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(50))
                    .background(ColorBox.primary)
                    .clickable {
                        Player.autoPlayPause()
                    }.padding(8.dp),
                play = Player.Live.playPauseCallback.value
            )
            EIconButton(
                padding = PaddingValues(start = 12.dp, end = 12.dp),
                icon = "icons/next.svg",
                size = 38.dp,
                contentPadding = 9.dp,
                colorFilter = ColorBox.text,
                background = ColorBox.text.copy(0.07f),
                onClick = {
                    Player.next()
                }
            )
            if (maxWidth > 570) {
                var showSpeedPopup by remember { mutableStateOf(false) }
                ESpeedButton(
                    padding = PaddingValues(end = 14.dp),
                    size = 38.dp,
                    value = Player.Live.speedCallback.value,
                    background = ColorBox.text.copy(0.07f),
                    textColor = ColorBox.text,
                    onClick = {
                        showSpeedPopup = true
                    }
                )
                SpeedPlaybackPopup(show = showSpeedPopup, onDismissRequest = { showSpeedPopup = false })
            }
            if (maxWidth > 620) {
                var showRepeatModePopup by remember { mutableStateOf(false) }
                EIconButton(
                    padding = PaddingValues(end = 12.dp),
                    icon = when (Player.Live.repeatModeCallback.value) {
                        Player.REPEAT_MODE_ALL -> "icons/repeat-all.svg"
                        Player.REPEAT_MODE_ONE -> "icons/repeat-one.svg"
                        Player.REPEAT_MODE_SHUFFLE -> "icons/shuffle.svg"
                        Player.REPEAT_MODE_STOP -> "icons/stop.svg"
                        else -> "icons/repeat-all.svg"
                    },
                    size = 38.dp,
                    contentPadding = 9.dp,
                    background = ColorBox.text.copy(0.07f),
                    onClick = {
                        showRepeatModePopup = true
                    }
                )
                RepeatModePopup(show = showRepeatModePopup, onDismissRequest = { showRepeatModePopup = false })
            }
            if (maxWidth > 900) {
                Row(
                    modifier = Modifier.padding(end = 12.dp).width(220.dp).height(38.dp)
                        .clip(RoundedCornerShape(50))
                        .background(ColorBox.text.copy(0.07f)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EIconButton(
                        icon = if (Player.Live.isMutedCallback.value) "icons/sound-mute.svg" else "icons/volume-high.svg",
                        size = 38.dp,
                        contentPadding = 9.dp,
                        onClick = {
                            Player.autoMute()
                        }
                    )
                    CustomSlider(
                        modifier = Modifier.padding(start = 8.dp, end = 20.dp).fillMaxWidth(),
                        value = (Player.Live.volumeCallback.value / 1.2f / 100f),
                        thumbSize = 6.dp,
                        trackHeight = 2.6.dp,
                        valueChanged = {
                            Player.changeVolume((it * 1.2 * 100).toInt())
                        },
                        valueChangedFinished = {}
                    )
                }
            }
        }
    }
}