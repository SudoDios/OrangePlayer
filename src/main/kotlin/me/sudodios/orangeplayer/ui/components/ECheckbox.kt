package me.sudodios.orangeplayer.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.sudodios.orangeplayer.ui.theme.ColorBox

@Composable
fun ECheckbox(
    modifier: Modifier = Modifier,
    isChecked : Boolean,
    bgColor : Color = ColorBox.primary,
    iconColor : Color = ColorBox.window,
    borderWidth : Dp = 2.dp
) {

    val animation = animateFloatAsState(if (isChecked) 0f else 1f, animationSpec = tween(durationMillis = 140))
    val checkCache = remember { CheckDrawingCache() }

    Canvas(modifier.requiredSize(22.dp).graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)) {
        val validSize = size.width - borderWidth.toPx()
        drawCircle(
            color = bgColor.copy(0.6f),
            radius = validSize / 2f,
            style = Stroke(width = borderWidth.toPx())
        )
        drawCircle(
            color = bgColor,
            radius = (validSize - 1.dp.toPx()) / 2f,
            style = Fill
        )
        drawCircle(
            color = Color.Transparent,
            radius = ((validSize - 1.dp.toPx()) / 2f) * animation.value,
            blendMode = BlendMode.Clear,
            style = Fill
        )
        drawCheck(
            iconColor,
            1f - animation.value,
            animation.value,
            2.4f.dp.toPx(),
            drawingCache = checkCache
        )
    }

}

@Immutable
private class CheckDrawingCache(
    val checkPath: Path = Path(),
    val pathMeasure: PathMeasure = PathMeasure(),
    val pathToDraw: Path = Path()
)

private fun DrawScope.drawCheck(
    checkColor: Color,
    checkFraction: Float,
    crossCenterGravitation: Float,
    strokeWidthPx: Float,
    drawingCache: CheckDrawingCache
) {
    val stroke = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
    val width = size.width
    val checkCrossX = 0.4f
    val checkCrossY = 0.7f
    val leftX = 0.2f
    val leftY = 0.5f
    val rightX = 0.8f
    val rightY = 0.3f

    val gravitatedCrossX = androidx.compose.ui.util.lerp(checkCrossX, 0.5f, crossCenterGravitation)
    val gravitatedCrossY = androidx.compose.ui.util.lerp(checkCrossY, 0.5f, crossCenterGravitation)
    val gravitatedLeftY = androidx.compose.ui.util.lerp(leftY, 0.5f, crossCenterGravitation)
    val gravitatedRightY = androidx.compose.ui.util.lerp(rightY, 0.5f, crossCenterGravitation)

    with(drawingCache) {
        checkPath.reset()
        checkPath.moveTo(width * leftX, width * gravitatedLeftY)
        checkPath.lineTo(width * gravitatedCrossX, width * gravitatedCrossY)
        checkPath.lineTo(width * rightX, width * gravitatedRightY)
        pathMeasure.setPath(checkPath, false)
        pathToDraw.reset()
        pathMeasure.getSegment(
            0f, pathMeasure.length * checkFraction, pathToDraw, true
        )
    }
    drawPath(drawingCache.pathToDraw, checkColor, style = stroke)
}