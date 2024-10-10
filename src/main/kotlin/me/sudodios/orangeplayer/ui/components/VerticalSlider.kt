package me.sudodios.orangeplayer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.sudodios.orangeplayer.ui.theme.ColorBox

@Composable
fun VerticalSlider(
    modifier: Modifier = Modifier,
    value: Float,
    trackColor: Color = ColorBox.primary,
    thumbColor: Color = ColorBox.primary,
    trackHeight: Dp = 3.dp,
    thumbSize: Dp = 8.dp,
    valueChanged: (Float) -> Unit,
    valueChangedFinished: () -> Unit
) {

    var currentHeight = 0
    var isPressed by remember { mutableStateOf(false) }
    var offsetY by remember { mutableStateOf(0f) }

    Canvas(
        modifier = modifier.width(thumbSize * 3)
            .onGloballyPositioned {
                currentHeight = it.size.height
            }
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    offsetY += delta
                    offsetY = offsetY.coerceIn(0f, currentHeight.toFloat())
                    val progress = 1f - (offsetY / currentHeight.toFloat())
                    valueChanged.invoke(progress)
                },
                onDragStopped = {
                    isPressed = false
                    valueChangedFinished.invoke()
                },
                onDragStarted = {
                    offsetY = it.y
                    isPressed = true
                },
            )
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    offsetY = offset.y
                    val progress = (offsetY / currentHeight.toFloat())
                    valueChanged.invoke(progress)
                    isPressed = false
                    valueChangedFinished.invoke()
                }
            }
    ) {
        val width = size.width
        val height = size.height
        //track
        drawLine(
            color = trackColor.copy(0.4f),
            start = Offset(width / 2, 0f),
            end = Offset(width / 2, height),
            strokeWidth = trackHeight.toPx(),
            cap = StrokeCap.Round
        )

        //progress
        if (value > 0f) {
            drawLine(
                color = trackColor,
                start = Offset(width / 2, height),
                end = Offset(width / 2, (height * (1f - value)) - trackHeight.toPx() / 2f),
                strokeWidth = trackHeight.toPx(),
                cap = StrokeCap.Round
            )
        }

        //thumb
        drawCircle(
            color = thumbColor,
            center = Offset(width / 2, height * (1f - value)),
            radius = thumbSize.toPx()
        )
    }
}