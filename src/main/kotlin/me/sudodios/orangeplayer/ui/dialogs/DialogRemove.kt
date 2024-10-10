package me.sudodios.orangeplayer.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.sudodios.orangeplayer.core.Native
import me.sudodios.orangeplayer.models.MediaItem
import me.sudodios.orangeplayer.models.ModelPlaylistsRead
import me.sudodios.orangeplayer.ui.components.BaseDialog
import me.sudodios.orangeplayer.ui.components.EButton
import me.sudodios.orangeplayer.ui.components.ECheckbox
import me.sudodios.orangeplayer.ui.components.EText
import me.sudodios.orangeplayer.ui.theme.ColorBox
import java.io.File

@Composable
fun DialogRemove(
    show : Boolean,
    item : Any,
    onDismiss: () -> Unit,
    onRemoved : () -> Unit,
) {

    BaseDialog(expanded = show) {

        var delFromDisk by remember { mutableStateOf(false) }
        var removing by remember { mutableStateOf(false) }

        fun removeItem() {
            removing = true
            CoroutineScope(Dispatchers.IO).launch {
                if (item is MediaItem) {
                    Native.dbDeleteItemByPath(item.path)
                    if (delFromDisk) {
                        File(item.path).delete()
                    }
                    onRemoved.invoke()
                } else if (item is ModelPlaylistsRead) {
                    Native.dbPlaylistsDelete(item.id)
                    onRemoved.invoke()
                }
            }
        }

        Column(modifier = Modifier.width(350.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            EText(
                modifier = Modifier.padding(20.dp),
                text = "Deletion !",
                color = ColorBox.text,
                style = MaterialTheme.typography.titleLarge
            )
            val descText = buildAnnotatedString {
                append("Are you sure to delete item\n")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = ColorBox.text.copy(0.8f))) {
                    if (item is MediaItem) {
                        append(item.name)
                    } else if (item is ModelPlaylistsRead) {
                        append(item.title)
                    }
                }
                append(" ?")
            }
            EText(
                modifier = Modifier.padding(start = 20.dp, bottom = 16.dp, end = 20.dp),
                text = descText,
                color = ColorBox.text.copy(0.7f),
                style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
            )
            if (item is MediaItem) {
                Row(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp).clip(RoundedCornerShape(50))
                    .clickable(enabled = !removing) { delFromDisk = !delFromDisk }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    ECheckbox(
                        bgColor = ColorBox.error,
                        isChecked = delFromDisk,
                        iconColor = ColorBox.card
                    )
                    EText(
                        modifier = Modifier.padding(start = 12.dp),
                        text = "Also delete from disk !!!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ColorBox.text.copy(0.8f)
                    )
                }
            }
            Row(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp).fillMaxWidth()) {
                EButton(
                    modifier = Modifier.weight(1f).padding(end = 10.dp),
                    centerText = true,
                    enabled = !removing,
                    text = "Close",
                    onClick = {
                        onDismiss.invoke()
                    }
                )
                EButton(
                    modifier = Modifier.weight(1f).padding(start = 10.dp),
                    centerText = true,
                    backgroundColor = ColorBox.error.copy(0.1f),
                    textColor = ColorBox.error,
                    enabled = !removing,
                    text = "Remove",
                    onClick = {
                        removeItem()
                    }
                )
            }
        }
    }

}