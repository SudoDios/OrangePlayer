package me.sudodios.orangeplayer.utils.imageloader

import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import me.sudodios.orangeplayer.utils.imageloader.core.*
import me.sudodios.orangeplayer.utils.imageloader.transform.ITransformation
import java.io.File

@Composable
fun ImageRes(resName: String, imageCallback: ImageCallback) {
    ImageComposeLoad(get = { ImageResponse(painterResource(resName), null) }, imageCallback)
}

@Composable
fun ImageAsyncImageFile(
    filePath: String, imageCallback: ImageCallback
) {
    ImageAsyncImageFile(filePath, null, imageCallback)
}

@Composable
fun ImageAsyncImageFile(
    filePath: String, transformations: List<ITransformation>?, imageCallback: ImageCallback
) {
    ImageSuspendLoad(key = filePath, get = {
        ImageLoader.instance().newRequest().load(File(filePath)).transformations(transformations)
            .saveStrategy(SaveStrategy.Original).get()
    }, imageCallback)
}

@Composable
private fun ImageSuspendLoad(key: String, get: suspend () -> ImageResponse, imageCallback: ImageCallback) {
    var imageResponse by remember { mutableStateOf(ImageInLoading) }
    LaunchedEffect(key) {
        imageResponse = ImageInLoading
        imageResponse = get()
    }
    if (imageResponse.exception != null) {
        imageCallback.errorView?.invoke()
    } else {
        val painter = imageResponse.imagePainter
        if (painter != null) {
            imageCallback.imageView.invoke(painter)
        } else {
            if (imageResponse.isLoading) {
                imageCallback.placeHolderView?.invoke()
            } else {
                imageCallback.errorView?.invoke()
            }
        }
    }
}

@Composable
private fun ImageComposeLoad(get: @Composable () -> ImageResponse, imageCallback: ImageCallback) {
    val imageResponse = get.invoke()
    if (imageResponse.exception != null) {
        imageCallback.errorView?.invoke()
    } else if (imageResponse.imagePainter == null) {
        imageCallback.errorView?.invoke()
    } else {
        imageCallback.imageView.invoke(imageResponse.imagePainter)
    }
}
