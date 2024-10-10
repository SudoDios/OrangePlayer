package me.sudodios.orangeplayer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import me.sudodios.orangeplayer.core.media.AspectRatio
import me.sudodios.orangeplayer.core.media.Player
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.SamplingMode

@Composable
fun VideoSurface(
    modifier: Modifier = Modifier,
    aspectRatio : AspectRatio = AspectRatio.DEFAULT,
) {
    Canvas(modifier) {
        val drawArea = if (aspectRatio == AspectRatio.DEFAULT) {
            aspectRatioCalculation(Player.videoSurface.aspectRatio(),size.width,size.height)
        } else {
            aspectRatioCalculation(aspectRatio.ratio!!,size.width,size.height)
        }
        drawIntoCanvas { nCanvas ->
            nCanvas.nativeCanvas.drawScaledImageRect(Player.videoSurface.skiaImage.value, drawArea)
        }
    }
}

private fun aspectRatioCalculation(ratio: Float, canvasWidth: Float, canvasHeight: Float): Rect {
    val canvasAspectRatio = canvasWidth / canvasHeight
    val imageWidth: Float
    val imageHeight: Float
    if (ratio > canvasAspectRatio) {
        imageWidth = canvasWidth
        imageHeight = imageWidth / ratio
    } else {
        imageHeight = canvasHeight
        imageWidth = imageHeight * ratio
    }
    val left = (canvasWidth - imageWidth) / 2
    val top = (canvasHeight - imageHeight) / 2
    return Rect(left, top, (left + imageWidth), (top + imageHeight))
}

private fun NativeCanvas.drawScaledImageRect(image : Image?,dst : Rect) {
    image?.let {
        drawImageRect(
            image = image,
            src = Rect.makeWH(image.width.toFloat(), image.height.toFloat()),
            dst = dst,
            samplingMode = SamplingMode.LINEAR,
            paint = Paint(),
            strict = true
        )
    }
}