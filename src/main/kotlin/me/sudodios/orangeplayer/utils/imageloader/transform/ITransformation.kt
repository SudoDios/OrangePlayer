package me.sudodios.orangeplayer.utils.imageloader.transform

import androidx.compose.ui.graphics.ImageBitmap

interface ITransformation {

    fun tag(): String

    fun transform(inputImage: ImageBitmap): ImageBitmap
}