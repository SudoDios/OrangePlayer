package me.sudodios.orangeplayer.ui.dialogs

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.sudodios.orangeplayer.core.media.Player
import me.sudodios.orangeplayer.models.MediaItem
import me.sudodios.orangeplayer.ui.components.*
import me.sudodios.orangeplayer.ui.components.reorderable.ReorderableCollectionItemScope
import me.sudodios.orangeplayer.ui.components.reorderable.ReorderableItem
import me.sudodios.orangeplayer.ui.components.reorderable.rememberReorderableLazyListState
import me.sudodios.orangeplayer.ui.theme.ColorBox
import me.sudodios.orangeplayer.utils.Utils.formatToDuration
import me.sudodios.orangeplayer.utils.painterResource

@Composable
fun DialogCurrentPlaylist(
    show : Boolean,
    onDismiss : () -> Unit,
) {

    BaseDialog(
        expanded = show,
        onDismissRequest = onDismiss
    ) {

        var listCopy by remember { mutableStateOf(Player.playList) }
        val lazyListState = rememberLazyListState()
        val reorderLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
            listCopy = listCopy.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
            Player.reorderPlaylist(to.index,from.index)
        }

        Box(modifier = Modifier.width(410.dp)) {
            Row(modifier = Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
                EText(
                    modifier = Modifier.padding(start = 20.dp),
                    text = "Current Playlist",
                    color = ColorBox.text,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(top = 56.dp, bottom = 80.dp),
                state = lazyListState,
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp)
            ) {
                itemsIndexed(listCopy,key = { _,item -> item.path }) { index, item ->
                    ReorderableItem(reorderLazyListState, key = item.path) { isDragging ->
                        listItem(isDragging,index + 1,item, onRemove = {
                            listCopy = listCopy.toMutableList().apply {
                                removeAt(index)
                            }
                            Player.playList.removeAt(index)
                        })
                    }
                }
            }
            if (lazyListState.canScrollForward || lazyListState.canScrollBackward) {
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd).padding(top = 56.dp, bottom = 80.dp),
                    style = LocalScrollbarStyle.current
                        .copy(thickness = 6.dp, hoverColor = ColorBox.text.copy(0.6f), unhoverColor = ColorBox.text.copy(0.1f)),
                    adapter = rememberScrollbarAdapter(lazyListState)
                )
            }
            EButton(
                modifier = Modifier.align(Alignment.BottomCenter).padding(start = 16.dp, end = 16.dp, bottom = 16.dp).fillMaxWidth(),
                text = "Done !",
                centerText = true,
                backgroundColor = ColorBox.primary.copy(0.1f),
                textColor = ColorBox.primary,
                onClick = {
                    onDismiss.invoke()
                }
            )
        }

    }

}

@Composable
private fun ReorderableCollectionItemScope.listItem(isDragging : Boolean,index : Int,item : MediaItem,onRemove : () -> Unit) {
    Row(modifier = Modifier
        .padding(4.dp)
        .fillMaxWidth()
        .shadow(if (isDragging) 3.dp else 0.dp, shape = RoundedCornerShape(12.dp))
        .clip(RoundedCornerShape(12.dp))
        .background(ColorBox.card2)
        .clickable {
            Player.startPlay(item)
        }
        .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically) {
        if (Player.Live.currentMedia.value?.path == item.path) {
            Icon(
                modifier = Modifier.size(40.dp).padding(10.dp),
                painter = painterResource("icons/play.svg"),
                tint = ColorBox.primary,
                contentDescription = null
            )
        } else {
            EText(
                modifier = Modifier.width(40.dp),
                text = index.toString(),
                color = ColorBox.text,
                style = MaterialTheme.typography.labelSmall.copy(textAlign = TextAlign.Center)
            )
        }
        SmoothImage(
            modifier = Modifier.padding(start = 3.dp).size(44.dp).padding(2.dp).clip(RoundedCornerShape(50)),
            image = item.coverPath,
            placeHolder = if (item.isVideo) "icons/video-placeholder.svg" else "icons/audio-placeholder.svg"
        )
        Column(modifier = Modifier.padding(start = 8.dp,end = 4.dp).weight(1f)) {
            EText(
                text = item.name,
                color = ColorBox.text,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            EText(
                modifier = Modifier.padding(top = 3.dp),
                text = item.duration.formatToDuration(),
                color = ColorBox.text.copy(0.6f),
                style = MaterialTheme.typography.labelSmall
            )
        }
        EIconButton(
            icon = "icons/trash.svg",
            size = 32.dp,
            contentPadding = 7.dp,
            onClick = {
                onRemove.invoke()
            }
        )
        EIconButton(
            modifier = Modifier.draggableHandle(),
            icon = "icons/drag.svg",
            size = 32.dp,
            contentPadding = 7.dp,
            onClick = {}
        )
    }
}