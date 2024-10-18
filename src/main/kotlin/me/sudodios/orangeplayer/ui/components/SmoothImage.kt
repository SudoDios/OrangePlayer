package me.sudodios.orangeplayer.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import me.sudodios.orangeplayer.utils.painterResource
import me.sudodios.orangeplayer.utils.imageloader.ImageAsyncImageFile
import me.sudodios.orangeplayer.utils.imageloader.core.ImageCallback
import me.sudodios.orangeplayer.utils.imageloader.transform.ITransformation

@Composable
fun SmoothImage(
    modifier: Modifier,
    image : String?,
    fadeOnChange : Boolean = false,
    placeHolder : String? = "icons/audio-square.svg",
    contentScale: ContentScale = ContentScale.Crop,
    transformer : ITransformation? = null,
    placeTint: Color? = null
) {
    val placeHolderColor = if (placeTint == null) null else ColorFilter.tint(placeTint)
    if (fadeOnChange) {
        Crossfade(modifier = modifier,targetState = image) {
            CustomImage(modifier, it, placeHolder, contentScale,transformer,placeHolderColor)
        }
    } else {
        CustomImage(modifier, image, placeHolder, contentScale,transformer,placeHolderColor)
    }
}

@Composable
private fun CustomImage (modifier: Modifier,image: String?,placeHolder: String?,contentScale: ContentScale,transformer: ITransformation?,placeTint: ColorFilter?) {
    if (image.isNullOrBlank()) {
        if (placeHolder != null) {
            Image(
                modifier = modifier,
                painter = painterResource(placeHolder),
                contentDescription = null,
                contentScale = contentScale,
                colorFilter = placeTint
            )
        }
    } else {
        val transformers = if (transformer != null) { listOf(transformer) } else { null }
        Box(modifier) {
            ImageAsyncImageFile(image,transformations = transformers, imageCallback = ImageCallback(
                imageView = {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        painter = it,
                        contentScale = contentScale,
                        contentDescription = null,
                    )
                },
                errorView = {
                    if (placeHolder != null) {
                        Image(
                            modifier = modifier,
                            painter = painterResource(placeHolder),
                            contentDescription = null,
                            contentScale = contentScale,
                            colorFilter = placeTint
                        )
                    }
                }
            ))
        }
    }
}