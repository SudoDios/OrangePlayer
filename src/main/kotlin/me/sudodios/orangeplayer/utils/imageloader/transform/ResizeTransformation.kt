package me.sudodios.orangeplayer.utils.imageloader.transform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.awt.Image
import java.awt.image.BufferedImage

class ResizeTransformation(private val width: Int?, private val height: Int?) : ITransformation {

    override fun tag(): String {
        return TransformationTag.ResizeTransformation + width.toString()
    }

    override fun transform(inputImage: ImageBitmap): ImageBitmap {
        val aspectRatio = inputImage.width.toFloat() / inputImage.height.toFloat()
        val (thumbnailWidth, thumbnailHeight) = when {
            width != null -> {
                val height = (width / aspectRatio).toInt()
                Pair(width, height)
            }
            height != null -> {
                val width = (height * aspectRatio).toInt()
                Pair(width, height)
            }
            else -> throw IllegalArgumentException("You must specify either maxWidth or maxHeight")
        }
        if (width == inputImage.width && height == inputImage.height) {
            return inputImage
        }
        val tmp = inputImage.toAwtImage().getScaledInstance(thumbnailWidth, thumbnailHeight, Image.SCALE_SMOOTH)
        val resizedImage = BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_ARGB)
        val g2d = resizedImage.createGraphics()
        try {
            g2d.drawImage(tmp, 0, 0, null)
        } finally {
            g2d.dispose()
        }
        return resizedImage.toComposeImageBitmap()
    }
}