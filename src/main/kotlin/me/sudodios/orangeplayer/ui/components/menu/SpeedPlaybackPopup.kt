package me.sudodios.orangeplayer.ui.components.menu

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.sudodios.orangeplayer.core.media.Player
import me.sudodios.orangeplayer.ui.components.EText
import me.sudodios.orangeplayer.ui.theme.ColorBox
import me.sudodios.orangeplayer.utils.Pref
import me.sudodios.orangeplayer.utils.Utils.roundTo

@Composable
fun SpeedPlaybackPopup(
    show: Boolean = false,
    onDismissRequest: () -> Unit,
) {

    var speedSeekValue by remember { mutableStateOf(Pair(false, Pref.playbackSpeed)) }

    CustomDropdownMenu(
        expanded = show,
        onDismissRequest = onDismissRequest
    ) {
        EText(
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
            text = "Playback Speed",
            fontSize = 10.sp,
            color = ColorBox.text.copy(0.6f)
        )
        Box(modifier = Modifier.width(210.dp)) {
            SpeedSeekbar(
                modifier = Modifier.fillMaxWidth().height(40.dp),
                value = speedSeekValue,
                valueChanged = {
                    speedSeekValue = Pair(false, it)
                    Pref.playbackSpeed = speedSeekValue.second
                },
                valueChangedFinished = {
                    Player.changeSpeed((speedSeekValue.second * 1.5f) + 0.5f)
                }
            )
            EText(
                modifier = Modifier.padding(start = 16.dp).align(Alignment.CenterStart),
                text = "${((speedSeekValue.second * 1.5) + 0.5).roundTo(1)}x",
                color = ColorBox.text,
                fontSize = 13.sp
            )
        }
        HorizontalDivider(Modifier.padding(top = 6.dp, bottom = 2.dp).fillMaxWidth(), color = ColorBox.text.copy(0.1f))
        val convertSpeedStr = ((speedSeekValue.second * 1.5) + 0.5).roundTo(1).toString()
        NestedMenuItem(
            menuItem = NestedMenuItem(
                title = "0.5x",
                selected = convertSpeedStr == "0.5"
            ),
            onClicked = {
                speedSeekValue = Pair(true, 0f)
                Pref.playbackSpeed = 0f
                Player.changeSpeed(0.5f)
            }
        )
        NestedMenuItem(
            menuItem = NestedMenuItem(
                title = "1.0x",
                selected = convertSpeedStr == "1.0"
            ),
            onClicked = {
                speedSeekValue = Pair(true, 0.33f)
                Pref.playbackSpeed = 0.33f
                Player.changeSpeed(1f)
            }
        )
        NestedMenuItem(
            menuItem = NestedMenuItem(
                title = "1.5x",
                selected = convertSpeedStr == "1.5"
            ),
            onClicked = {
                speedSeekValue = Pair(true, 0.66f)
                Pref.playbackSpeed = 0.66f
                Player.changeSpeed(1.5f)
            }
        )
        NestedMenuItem(
            menuItem = NestedMenuItem(
                title = "2.0x",
                selected = convertSpeedStr == "2.0"
            ),
            onClicked = {
                speedSeekValue = Pair(true, 1f)
                Pref.playbackSpeed = 1f
                Player.changeSpeed(2f)
            }
        )
    }

}

@Composable
private fun SpeedSeekbar(
    modifier: Modifier,
    value: Pair<Boolean, Float> = Pair(false, 0f),
    valueChanged: (Float) -> Unit,
    valueChangedFinished: () -> Unit
) {

    var currentWidth = 0

    var isPressed by remember { mutableStateOf(false) }
    var offsetX by remember { mutableStateOf(0f) }

    val animateProgress = animateFloatAsState(value.second)

    Canvas(
        modifier = modifier.clip(RoundedCornerShape(8.dp)).onGloballyPositioned {
            currentWidth = it.size.width
        }.draggable(
            orientation = Orientation.Horizontal,
            state = rememberDraggableState { delta ->
                offsetX += delta
                offsetX = offsetX.coerceIn(0f, currentWidth.toFloat())
                val progress = (offsetX / currentWidth.toFloat())
                valueChanged.invoke(progress)
            },
            onDragStopped = {
                isPressed = false
                valueChangedFinished.invoke()
            },
            onDragStarted = {
                offsetX = it.x
                isPressed = true
            },
        ).pointerHoverIcon(icon = PointerIcon.Hand)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    offsetX = offset.x
                    val progress = (offsetX / currentWidth.toFloat())
                    valueChanged.invoke(progress)
                    isPressed = false
                    valueChangedFinished.invoke()
                }
            }
    ) {
        drawRect(
            color = ColorBox.primary.copy(0.2f),
            size = Size(size.width * if (value.first) animateProgress.value else value.second, size.height)
        )
    }

}