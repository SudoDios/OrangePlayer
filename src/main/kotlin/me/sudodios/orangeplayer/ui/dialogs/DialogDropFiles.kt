package me.sudodios.orangeplayer.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.sudodios.orangeplayer.core.media.MediaStore
import me.sudodios.orangeplayer.ui.components.BaseDialog
import me.sudodios.orangeplayer.ui.components.EButton
import me.sudodios.orangeplayer.ui.components.EText
import me.sudodios.orangeplayer.ui.sections.list.PageSection
import me.sudodios.orangeplayer.ui.theme.ColorBox
import me.sudodios.orangeplayer.utils.painterResource

@Composable
fun DialogDropFiles(
    show : Boolean,
    listPaths : List<String>? = null,
    onClose : () -> Unit
) {

    BaseDialog(expanded = show) {

        var preparing by remember { mutableStateOf(false) }
        var analyzed by remember { mutableStateOf(false) }
        var statusText by remember { mutableStateOf("Preparing files ...") }

        fun analyzeFiles(paths : List<String>) {
            preparing = true
            statusText = "Analyzing ${paths.size} paths ..."
            MediaStore.scanPaths(returnItems = true,paths = paths.toTypedArray()) { vidCount, audioCount,items ->
                preparing = false
                analyzed = true
                statusText = "Analyze completed\nFinded $vidCount Videos & $audioCount Aduios"
                if (vidCount > 0 || audioCount > 0) {
                    PageSection.changePage(PageSection.selectedMenu.value,true)
                }
            }
        }

        //for hand method
        LaunchedEffect(listPaths) {
            if (!listPaths.isNullOrEmpty()) {
                analyzeFiles(listPaths)
            }
        }

        Column(
            modifier = Modifier.width(370.dp).clip(RoundedCornerShape(16.dp)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.padding(16.dp).fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)).background(ColorBox.text.copy(0.1f))) {
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
