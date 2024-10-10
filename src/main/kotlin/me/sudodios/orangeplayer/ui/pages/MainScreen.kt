package me.sudodios.orangeplayer.ui.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.onExternalDrag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.sudodios.orangeplayer.Global
import me.sudodios.orangeplayer.core.media.Player
import me.sudodios.orangeplayer.ui.dialogs.DialogDropFiles
import me.sudodios.orangeplayer.ui.sections.list.PageHead
import me.sudodios.orangeplayer.ui.sections.list.PageList
import me.sudodios.orangeplayer.ui.sections.list.SideMenu
import me.sudodios.orangeplayer.ui.sections.players.LargePlayer
import me.sudodios.orangeplayer.ui.sections.players.MiniPlayer
import me.sudodios.orangeplayer.ui.theme.ColorBox
import me.sudodios.orangeplayer.utils.Events
import me.sudodios.orangeplayer.utils.imageloader.ImageAsyncImageFile
import me.sudodios.orangeplayer.utils.imageloader.core.ImageCallback
import me.sudodios.orangeplayer.utils.imageloader.transform.ResizeTransformation

@Composable
private fun BgImage() {
    val imagePath = Player.Live.currentMedia.value?.coverPath
    if (imagePath.isNullOrBlank()) {
        Image(painter = painterResource("icons/app-icon.png"), contentDescription = "bg",
            modifier = Modifier.fillMaxSize().alpha(0.2f).blur(100.dp), contentScale = ContentScale.Crop)
    } else {
        ImageAsyncImageFile(imagePath, transformations = listOf(ResizeTransformation(50,null)), imageCallback = ImageCallback(
            errorView = {
                Image(painter = painterResource("icons/app-icon.png"), contentDescription = "bg",
                    modifier = Modifier.fillMaxSize().alpha(0.2f).blur(100.dp), contentScale = ContentScale.Crop)
            },
            imageView = {
                Image(painter = it, contentDescription = "bg",
                    modifier = Modifier.fillMaxSize().alpha(0.2f).blur(100.dp), contentScale = ContentScale.Crop)
            }
        ))
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainScreen() {

    var showLoading by remember { mutableStateOf(false) }

    var dragState by remember { mutableStateOf(Global.DragState.DRAG_EXIT) }
    var dialogDragState by remember { mutableStateOf(Global.DragState.DRAG_EXIT) }

    DisposableEffect(Unit) {
        val observer : (Boolean) -> Unit = {
            showLoading = it
        }
        Events.showProgressLoading.addObserver(observer)
        onDispose {
            Events.showProgressLoading.removeObserver(observer)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()
        .onExternalDrag(
            enabled = dialogDragState != Global.DragState.DROPPED,
            onDragStart = {
                dragState = Global.DragState.DRAG_ENTER
            },
            onDragExit = {
                dragState = Global.DragState.DRAG_EXIT
            },
            onDrop = {
                if (dialogDragState == Global.DragState.DRAG_EXIT) {
                    dragState = Global.DragState.DRAG_EXIT
                }
            }
        )
        .background(ColorBox.window)) {
        BgImage()
        AnimatedVisibility(
            visible = !Player.Live.showLargePlayer.value,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                val maxWidth = this@BoxWithConstraints.maxWidth.value.toInt()
                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    SideMenu(maxWidth < 680)
                    Column(modifier = Modifier.fillMaxSize()) {
                        PageHead()
                        PageList()
                    }
                }
                AnimatedVisibility(
                    visible = Player.Live.currentMedia.value != null
                ) {
                    MiniPlayer(maxWidth)
                }
            }
            if (showLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp), strokeCap = StrokeCap.Round)
            }
        }
        AnimatedVisibility(
            visible = Player.Live.showLargePlayer.value,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LargePlayer()
        }
    }

    DialogDropFiles(
        show = dragState == Global.DragState.DRAG_ENTER,
        fromDragFiles = true,
        onDragState = {
            dialogDragState = it
        },
        onClose = {
            dragState = Global.DragState.DRAG_EXIT
            dialogDragState = Global.DragState.DRAG_EXIT
        }
    )

}