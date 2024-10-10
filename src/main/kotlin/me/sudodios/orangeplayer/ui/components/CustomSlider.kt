package me.sudodios.orangeplayer.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.sudodios.orangeplayer.ui.theme.ColorBox
import me.sudodios.orangeplayer.utils.Utils
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CustomSlider(
    modifier: Modifier,
    waveformData : ArrayList<Float> = arrayListOf(),
    value: Float,
    trackColor: Color = ColorBox.primary,
    thumbColor: Color = ColorBox.primary,
    thumbSize: Dp = 10.dp,
    trackHeight: Dp = 4.dp,
    alwaysShowThumb : Boolean = false,
    valueChanged: (Float) -> Unit,
    valueChangedFinished: () -> Unit
) {

    var currentWidth = 0

    var isPressed by remember { mutableStateOf(false) }
    var offsetX by remember { mutableStateOf(0f) }

    var showThumb by remember { mutableStateOf(alwaysShowThumb) }
    val animateThumb = animateFloatAsState(
        targetValue = if (showThumb) 1f else 0f,
        animationSpec = tween(250)
    )

    //waveform
    val paint = Paint().apply {
        style = PaintingStyle.Stroke
        strokeWidth = 3f
        strokeCap = StrokeCap.Round
    }
    var invalidations by remember { mutableStateOf(0) }
    val barWidth = 3f
    var waveformTime by remember { mutableStateOf(0L) }
    var waveData by remember { mutableStateOf(ArrayList<Float>()) }
    LaunchedEffect(waveformData) {
        waveformTime = System.currentTimeMillis()
        waveData = waveformData
    }

    Canvas(
        modifier = modifier.height(thumbSize * 3)
            .onGloballyPositioned {
                currentWidth = it.size.width
            }
            .draggable(
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
            ).onPointerEvent(PointerEventType.Enter) {
                if (!alwaysShowThumb)
                    showThumb = true
            }.onPointerEvent(PointerEventType.Exit) {
                if (!alwaysShowThumb)
                    showThumb = false
            }.pointerInput(Unit) {
                detectTapGestures { offset ->
                    offsetX = offset.x
                    val progress = (offsetX / currentWidth.toFloat())
                    valueChanged.invoke(progress)
                    isPressed = false
                    valueChangedFinished.invoke()
                }
            }
    ) {
        val width = size.width
        val height = size.height

        if (waveData.isNotEmpty()) {
            //waveform
            invalidations.let{
                val midpoint = size.height / 2f
                val maxHeight = size.height
                val barGap = (size.width - waveData.size * barWidth) / (waveData.size - 1).toFloat()

                var hasMoreFrames = false

                for (bar in waveData.indices) {
                    val x = bar * (barWidth + barGap) + barWidth / 2f
                    val y = waveData[bar] * maxHeight
                    val progress: Float = x / size.width
                    paint.color = if (progress * 1f < value) trackColor else trackColor.copy(0.4f)
                    val time = System.currentTimeMillis() - bar * 12 - waveformTime
                    val timeX = max(0.0, min(1.0, (time / 450f).toDouble())).toFloat()
                    val interpolatedTime: Float = Utils.getOvershotInterpolator(t = timeX)
                    val interpolatedY = y * interpolatedTime
                    drawIntoCanvas {
                        it.drawLine(
                            p1 = Offset(x, midpoint - interpolatedY),
                            p2 = Offset(x, midpoint + interpolatedY),
                            paint = paint
                        )
                    }
                    if (time < 450) {
                        hasMoreFrames = true
                    }
                }

                if (hasMoreFrames) {
                    invalidations++
                }

            }

        } else {

            //track
            drawLine(
                color = trackColor.copy(0.4f),
                start = Offset(0f, height / 2),
                end = Offset(width, height / 2),
                strokeWidth = trackHeight.toPx(),
                cap = StrokeCap.Round
            )

            //progress
            if (value > 0f) {
                drawLine(
                    color = trackColor,
                    start = Offset(0f, height / 2),
                    end = Offset((width * value) - (trackHeight.toPx() / 2f), height / 2),
                    strokeWidth = trackHeight.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        //thumb
        drawCircle(
            color = thumbColor,
            center = Offset(width * value, height / 2),
            radius = (thumbSize * animateThumb.value).toPx()
        )

    }

}

