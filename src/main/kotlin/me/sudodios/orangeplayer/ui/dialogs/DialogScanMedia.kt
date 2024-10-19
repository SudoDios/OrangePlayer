package me.sudodios.orangeplayer.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.sudodios.orangeplayer.Global
import me.sudodios.orangeplayer.core.Native
import me.sudodios.orangeplayer.core.media.MediaStore
import me.sudodios.orangeplayer.ui.components.*
import me.sudodios.orangeplayer.ui.theme.ColorBox

@Composable
fun DialogScanMedia(onClose: () -> Unit,onFinished : () -> Unit) {

    var searching by remember { mutableStateOf(false) }
    var resultString by remember { mutableStateOf("") }

    fun startScan () {
        searching = true
        resultString = "scanning ..."
        MediaStore.scanPaths(returnItems = false,arrayOf(Global.userHome), result = { vidCount,audioCount,_ ->
            searching = false
            resultString = "$vidCount videos, $audioCount audios"
            onFinished.invoke()
        })
    }

    Column(modifier = Modifier.width(410.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        SSDScanAnimation(searching, modifier = Modifier.padding(top = 30.dp).size(95.dp))
        EText(
            modifier = Modifier.padding(top = 20.dp),
            text = "Scan media files",
            style = MaterialTheme.typography.titleMedium,
            color = ColorBox.text
        )
        AnimatedText(
            modifier = Modifier.padding(top = 6.dp),
            text = resultString,
            color = ColorBox.text.copy(0.7f),
            style = MaterialTheme.typography.labelSmall
        )
        EActionRow(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            title = "Target directory",
            value = Global.userHome,
            enabled = false,
            icon = "icons/folder.svg"
        )
        Row(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp).fillMaxWidth()) {
            EButton(
                modifier = Modifier.weight(1f).padding(end = 10.dp),
                centerText = true,
                enabled = !searching && Native.dbCountMediaItems() != 0,
                text = "Close",
                onClick = {
                    onClose.invoke()
                }
            )
            EButton(
                modifier = Modifier.weight(1f).padding(start = 10.dp),
                centerText = true,
                enabled = !searching,
                backgroundColor = ColorBox.primary.copy(0.1f),
                textColor = ColorBox.primary,
                text = "Start scan !",
                onClick = {
                    startScan()
                }
            )
        }
    }

}