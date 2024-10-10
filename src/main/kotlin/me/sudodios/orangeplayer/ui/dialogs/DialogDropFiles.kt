package me.sudodios.orangeplayer.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.sudodios.orangeplayer.Global
import me.sudodios.orangeplayer.core.Platform
import me.sudodios.orangeplayer.core.media.MediaStore
import me.sudodios.orangeplayer.ui.components.BaseDialog
import me.sudodios.orangeplayer.ui.components.EButton
import me.sudodios.orangeplayer.ui.components.EText
import me.sudodios.orangeplayer.ui.sections.list.PageSection
import me.sudodios.orangeplayer.ui.theme.ColorBox
import java.net.URLDecoder

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DialogDropFiles(
    show : Boolean,
    fromDragFiles : Boolean,
    listPaths : List<String>? = null,
    onDragState: ((Global.DragState) -> Unit)? = null,
    onClose : () -> Unit
) {

    BaseDialog(expanded = show) {

        var dragState by remember { mutableStateOf(Global.DragState.DRAG_EXIT) }
        var droppedList by remember { mutableStateOf(listOf<String>()) }

        val animateDropColor = animateColorAsState(if (dragState == Global.DragState.DRAG_ENTER) ColorBox.primary.copy(0.4f) else ColorBox.text.copy(0.1f))

        var preparing by remember { mutableStateOf(false) }
        var analyzed by remember { mutableStateOf(false) }
        var statusText by remember { mutableStateOf("Drop files here") }

        fun analyzeFiles(paths : List<String>) {
            preparing = true
            statusText = "Analyzing ${paths.size} paths ..."
            MediaStore.scanPaths(paths.toTypedArray()) { vidCount, audioCount ->
                preparing = false
                analyzed = true
                statusText = "Analyze completed\nFinded $vidCount Videos & $audioCount Aduios"
                if (vidCount > 0 || audioCount > 0) {
                    PageSection.changePage(PageSection.selectedMenu.value,true)
                }
            }
        }

        fun parseDroppedPaths(input : List<String>) : List<String> {
            return input.map {
                val fixStart = it.replace(if (Platform.isUnix()) "file:" else "file:/", "")
                var decode = URLDecoder.decode(fixStart, "UTF-8")
                if (Platform.isWin()) {
                    decode = decode.replace("/","\\")
                }
                decode
            }
        }

        //for drag method
        LaunchedEffect(dragState) {
            if (dragState == Global.DragState.DROPPED) {
                analyzeFiles(droppedList)
            }
        }

        //for hand method
        LaunchedEffect(listPaths) {
            if (!listPaths.isNullOrEmpty()) {
                analyzeFiles(listPaths)
            }
        }

        Column(
            modifier = Modifier.width(370.dp).clip(RoundedCornerShape(16.dp))
                .onExternalDrag(
                    enabled = dragState != Global.DragState.DROPPED && fromDragFiles,
                    onDragStart = {
                        dragState = Global.DragState.DRAG_ENTER
                        onDragState?.invoke(Global.DragState.DRAG_ENTER)
                    },
                    onDragExit = {
                        dragState = Global.DragState.DRAG_EXIT
                        onDragState?.invoke(Global.DragState.DRAG_EXIT)
                    },
                    onDrop = { dropData ->
                        dragState = Global.DragState.DROPPED
                        onDragState?.invoke(Global.DragState.DROPPED)
                        if (dropData.dragData is DragData.FilesList) {
                            droppedList = parseDroppedPaths((dropData.dragData as DragData.FilesList).readFiles())
                        }
                    }
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.padding(16.dp).fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)).background(animateDropColor.value)) {
                Column(Modifier.align(Alignment.Center), verticalArrangement = Arrangement.Center,horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        modifier = Modifier.size(58.dp),
                        painter = painterResource("icons/box-search.svg"),
                        contentDescription = null,
                        tint = ColorBox.text.copy(0.8f)
                    )
                    EText(
                        modifier = Modifier.padding(top = 12.dp),
                        text = statusText,
                        color = ColorBox.text,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                        textAlign = TextAlign.Center
                    )
                }
                this@Column.AnimatedVisibility(
                    modifier = Modifier.align(Alignment.TopCenter),
                    visible = preparing
                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
            AnimatedVisibility(
                visible = analyzed
            ) {
                EButton(
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp,bottom = 20.dp),
                    text = "Done !",
                    backgroundColor = ColorBox.primary.copy(0.1f),
                    textColor = ColorBox.primary,
                    centerText = true,
                    onClick = {
                        onClose.invoke()
                    }
                )
            }
        }

    }

}
