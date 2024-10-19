package me.sudodios.orangeplayer.ui.sections.list

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.sudodios.orangeplayer.ui.components.EIconButton
import me.sudodios.orangeplayer.ui.components.ESearchBox
import me.sudodios.orangeplayer.ui.components.menu.NestedPopupMenu
import me.sudodios.orangeplayer.ui.components.menu.dropDownMenu
import me.sudodios.orangeplayer.ui.dialogs.DialogEqualizer
import me.sudodios.orangeplayer.ui.theme.ColorBox
import me.sudodios.orangeplayer.ui.theme.Fonts
import me.sudodios.orangeplayer.utils.Events
import me.sudodios.orangeplayer.utils.Pref

@Composable
fun PageHead() {
    var showMenu by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,verticalAlignment = Alignment.CenterVertically) {
            ESearchBox(
                modifier = Modifier.padding(start = 16.dp, end = 12.dp, top = 12.dp).widthIn(max = 460.dp).weight(1f),
                value = PageSection.searchKeyword.value,
                onValueChange = {
                    PageSection.updateSearchKeyword(it)
                }
            )
            EIconButton(
                padding = PaddingValues(end = 8.dp, top = 12.dp),
                background = ColorBox.primary.copy(0.3f),
                size = 38.dp,
                contentPadding = 8.dp,
                icon = "icons/playlist.svg",
                onClick = {
                    Events.showCurrentPlaylist.value = true
                }
            )
            EIconButton(
                padding = PaddingValues(end = 16.dp, top = 12.dp),
                background = ColorBox.text.copy(0.1f),
                size = 38.dp,
                contentPadding = 8.dp,
                icon = "icons/hor-menu.svg",
                onClick = {
                    showMenu = true
                }
            )
        }
        Row(Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically) {
            EIconButton(
                modifier = Modifier.padding(start = 12.dp, end = 12.dp),
                icon = if (PageSection.pageIsChild.value) "icons/arrow-left.svg" else PageSection.pageIcon.value,
                enabled = PageSection.pageIsChild.value,
                onClick = {
                    PageSection.closeChild()
                }
            )
            DrawTexts(modifier = Modifier.fillMaxWidth())
        }

    }

    var showEqualizerDialog by remember { mutableStateOf(false) }
    NestedPopupMenu(
        expanded = showMenu,
        onDismissRequested = {
            showMenu = false
        },
        menuItems = dropDownMenu {
            item("theme","Theme", icon = "icons/theme-light-dark.svg") {
                item("dark","Dark Theme", selected = Pref.isDarkTheme, onClicked = {
                    showMenu = false
                    ColorBox.darkTheme()
                })
                item("light","Light Theme", selected = !Pref.isDarkTheme, onClicked = {
                    showMenu = false
                    ColorBox.lightTheme()
                })
            }
            item("fps","FPS counter","icons/fps.svg") {
                item("show","Show", selected = Pref.showFpsCounter, onClicked = {
                    showMenu = false
                    Pref.showFpsCounter = true
                })
                item("hide","Hide", selected = !Pref.showFpsCounter, onClicked = {
                    showMenu = false
                    Pref.showFpsCounter = false
                })
            }
            item("layer-list","View type","icons/layer.svg") {
                item("list","List View","icons/list.svg", selected = Pref.viewIsList, onClicked = {
                    showMenu = false
                    Pref.viewIsList = true
                })
                item("grid","A-Grid View","icons/grid.svg", selected = !Pref.viewIsList, onClicked = {
                    showMenu = false
                    Pref.viewIsList = false
                })
            }
            item("scan","Re-Scan Disk", icon = "icons/ssd.svg", onClicked = {
                showMenu = false
                Events.openScanDialog.value = true
            })
            item("equalizer","Equalizer",icon = "icons/sound-wave.svg", onClicked = {
                showMenu = false
                showEqualizerDialog = true
            })
        }
    )

    DialogEqualizer(
        expanded = showEqualizerDialog,
        onDismissRequest = {
            showEqualizerDialog = false
        }
    )

}

@Composable
private fun DrawTexts(modifier: Modifier) {
    val textMeasurer = rememberTextMeasurer()

    val titleText = textMeasurer.measure(
        text = PageSection.pageTitle.value,
        style = MaterialTheme.typography.titleLarge.copy(fontFamily = Fonts.mainFont),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    val descText = textMeasurer.measure(
        text = PageSection.pageDesc.value,
        style = MaterialTheme.typography.labelMedium.copy(fontFamily = Fonts.mainFont),
        maxLines = 1
    )

    Canvas(modifier = modifier) {
        drawText(
            textLayoutResult = titleText,
            color = ColorBox.text,
            topLeft = Offset(0f, size.height / 2 - titleText.size.height / 2 - (descText.size.height / 2))
        )
        drawText(
            textLayoutResult = descText,
            color = ColorBox.text.copy(0.7f),
            topLeft = Offset(0f, size.height / 2 + titleText.size.height / 5)
        )
    }
}