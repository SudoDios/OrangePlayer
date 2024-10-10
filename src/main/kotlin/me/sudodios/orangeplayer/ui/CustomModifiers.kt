package me.sudodios.orangeplayer.ui

import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import kotlinx.coroutines.delay
import org.jetbrains.skiko.Cursor
import java.awt.Point
import java.awt.Toolkit
import java.awt.image.BufferedImage

private fun createEmptyCursor(): Cursor {
    return Toolkit.getDefaultToolkit().createCustomCursor(
        BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), Point(0, 0), "Empty Cursor"
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Modifier.hideableCursor(forceShowCursor : Boolean,onVisibility : (Boolean) -> Unit) = composed {
    var mousePosition by remember { mutableStateOf<Offset?>(null) }
    var pointerIcon by remember { mutableStateOf(PointerIcon.Default) }
    LaunchedEffect(mousePosition,forceShowCursor) {
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