package me.sudodios.orangeplayer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import me.sudodios.orangeplayer.utils.painterResource
import androidx.compose.ui.unit.dp
import me.sudodios.orangeplayer.ui.theme.ColorBox
import org.jetbrains.skia.FilterBlurMode
import org.jetbrains.skia.MaskFilter
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.PaintStrokeCap

@Composable
fun SSDScanAnimation(isSearching : Boolean,modifier: Modifier) {

    val painter = painterResource("/icons/ssd.svg")

    if (!isSearching) {
        Canvas(modifier) {
            with(painter) {
                draw(size, colorFilter = ColorFilter.tint(ColorBox.primary))
            }
        }
    } else {
        val linePaint = Paint().apply {
            asFrameworkPaint().color = ColorBox.primary.toArgb()
            asFrameworkPaint().mode = PaintMode.STROKE
            asFrameworkPaint().strokeCap = PaintStrokeCap.ROUND
            asFrameworkPaint().strokeWidth = 4.dp.value
            asFrameworkPaint().maskFilter = MaskFilter.makeBlur(FilterBlurMode.SOLID, 8f)
        }

        val clipPath = Path()

        val transition = rememberInfiniteTransition()
        val animFraction by transition.animateFloat(
            initialValue = 1f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000),
                repeatMode = RepeatMode.Reverse
            )
        )

        Canvas(modifier) {
            drawIntoCanvas {
                with(painter) {
                    draw(size, colorFilter = ColorFilter.tint(ColorBox.primary.copy(0.2f)))
                }
                it.save()
                clipPath.reset()
                clipPath.addRect(Rect(0f,0f,size.width,animFraction * size.height))
                it.clipPath(clipPath)
                with(painter) {
                    draw(size, colorFilter = ColorFilter.tint(ColorBox.primary))
                }
                it.restore()
                it.drawLine(
                    p1 = Offset(0f,size.height * animFraction),
                    p2 = Offset(size.width,size.height * animFraction),
                    paint = linePaint
                )
            }
        }
    }



}