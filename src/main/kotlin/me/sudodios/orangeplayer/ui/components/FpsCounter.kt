package me.sudodios.orangeplayer.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import me.sudodios.orangeplayer.ui.theme.ColorBox

@Composable
fun FpsCounter() {
    var lastUpdate by remember { mutableStateOf(0L) }
    var time by remember { mutableStateOf(0L) }
    var fpsCount by remember { mutableStateOf(0) }
    var fps by remember { mutableStateOf(0) }
    EText(
        text = fps.toString(),
        color = ColorBox.text,
        style = MaterialTheme.typography.titleLarge
    )
    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis { ms ->
                time = ms
                fpsCount++
                if (fpsCount == 10) {
                    fps = (10000 / (ms - lastUpdate)).toInt()
                    lastUpdate = ms
                    fpsCount = 0
                }
            }
        }
    }
}