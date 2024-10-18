package me.sudodios.orangeplayer.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.draganddrop.dragData
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import me.sudodios.orangeplayer.core.Platform
import me.sudodios.orangeplayer.ui.theme.ColorBox
import me.sudodios.orangeplayer.ui.theme.Fonts
import org.jetbrains.skiko.Cursor
import java.awt.Point
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.net.URLDecoder

private fun createEmptyCursor(): Cursor {
    return Toolkit.getDefaultToolkit().createCustomCursor(
        BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), Point(0, 0), "Empty Cursor"
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Modifier.hideableCursor(forceShowCursor: Boolean, onVisibility: (Boolean) -> Unit) = composed {
    var mousePosition by remember { mutableStateOf<Offset?>(null) }
    var pointerIcon by remember { mutableStateOf(PointerIcon.Default) }
    LaunchedEffect(mousePosition, forceShowCursor) {
        if (forceShowCursor) {
            pointerIcon = PointerIcon.Default
        } else {
            pointerIcon = PointerIcon.Default
            if (mousePosition != null) {
                delay(1000)
                pointerIcon = PointerIcon(createEmptyCursor())
            }
        }
    }
    LaunchedEffect(pointerIcon) {
        onVisibility.invoke(pointerIcon == PointerIcon.Default)
    }
    pointerHoverIcon(pointerIcon).onPointerEvent(PointerEventType.Move) { mousePosition = it.changes.first().position }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun Modifier.appDropFiles(onFilesDropped : (paths : List<String>) -> Unit) = composed {
    var showDragMoveLayout by remember { mutableStateOf(false) }
    val showDragMoveLayoutAnim = animateFloatAsState(if (showDragMoveLayout) 1f else 0f, animationSpec = tween(200))
    val dragTextLayout = rememberTextMeasurer().measure(
        "Drop files here ...",
        style = MaterialTheme.typography.headlineMedium.copy(fontFamily = Fonts.mainFont)
    )
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
    val dragAndDropTarget = remember {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                showDragMoveLayout = true
                super.onStarted(event)
            }
            override fun onEnded(event: DragAndDropEvent) {
                showDragMoveLayout = false
                super.onEnded(event)
            }
            override fun onDrop(event: DragAndDropEvent): Boolean {
                onFilesDropped.invoke(parseDroppedPaths((event.dragData() as DragData.FilesList).readFiles()))
                return true
            }
        }
    }
    dragAndDropTarget(
        shouldStartDragAndDrop = {
            it.dragData() is DragData.FilesList
        },
        target = dragAndDropTarget
    ).drawWithContent {
        drawContent()
        drawRect(
            color = Color.Black.copy(0.7f),
            alpha = showDragMoveLayoutAnim.value
        )
        drawRect(
            color = ColorBox.primary,
            style = Stroke(width = 2f.dp.toPx()),
            alpha = showDragMoveLayoutAnim.value
        )
        drawText(
            textLayoutResult = dragTextLayout,
            color = Color.White,
            topLeft = Offset(
                size.width / 2 - dragTextLayout.size.width / 2,
                size.height / 2 - dragTextLayout.size.height / 2
            ),
            alpha = showDragMoveLayoutAnim.value
        )
    }
}