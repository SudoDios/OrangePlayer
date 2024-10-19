package me.sudodios.orangeplayer.ui.sections.list

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.sudodios.orangeplayer.ui.components.EText
import me.sudodios.orangeplayer.ui.theme.ColorBox
import me.sudodios.orangeplayer.ui.theme.Fonts
import me.sudodios.orangeplayer.utils.painterResource

@Composable
@Preview
fun SideMenu(compact : Boolean) {
    var selectedIndex by remember { mutableStateOf(0) }
    LaunchedEffect(PageSection.selectedMenu.value) {
        selectedIndex = PageSection.Page.entries.toTypedArray().indexOfFirst { it.title == PageSection.selectedMenu.value.title }
    }
    val handleDivider = if (selectedIndex > 2) 17f else 0f
    val animBar = animateFloatAsState(((selectedIndex * 48f) + handleDivider + 10f + 100f), animationSpec = tween(200))

    Column(
        modifier = Modifier.fillMaxHeight().width(if (compact) 56.dp else 250.dp).background(ColorBox.window.copy(0.2f))
            .drawBehind {
                drawRoundRect(
                    color = ColorBox.primary,
                    topLeft = Offset(0f,animBar.value.dp.toPx()),
                    size = Size(3.dp.toPx(),28.dp.toPx()),
                    cornerRadius = CornerRadius(50f.dp.toPx(),50f.dp.toPx())
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Header()
        MenuMaker(compact)
    }
}

@Composable
private fun Header() {
    val appIcon = painterResource("icons/app-icon.png")
    val headerText = rememberTextMeasurer().measure(
        "Orange",
        style = MaterialTheme.typography.titleMedium.copy(fontFamily = Fonts.mainFont, fontSize = 20.sp)
    )
    Canvas(
        modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(0))
    ) {
        translate(56f.dp.toPx() / 2f - 24f.dp.toPx(),100f.dp.toPx() / 2f - 24f.dp.toPx()) {
            with(appIcon) {
                draw(size = Size(48f.dp.toPx(),48f.dp.toPx()))
            }
        }
        drawText(
            textLayoutResult = headerText,
            color = ColorBox.primary,
            topLeft = Offset(64f.dp.toPx(),center.y - headerText.size.height / 2)
        )
    }
}

@Composable
private fun MenuMaker(compact: Boolean) {
    PageSection.Page.entries.forEach {
        MenuItem(compact,it == PageSection.selectedMenu.value,it) { selected ->
            PageSection.changePage(selected,false)
        }
        if (it == PageSection.Page.AUDIOS) {
            HorizontalDivider(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp).fillMaxWidth(), color = ColorBox.text.copy(0.2f))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MenuItem(compact : Boolean,selected : Boolean,item: PageSection.Page,onClicked: (PageSection.Page) -> Unit) {
    val icon = painterResource(item.icon)
    val label = rememberTextMeasurer().measure(item.title, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = Fonts.mainFont))
    TooltipArea(
        tooltip = {
            if (compact) {
                EText(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(ColorBox.window.copy(0.2f)).padding(6.dp),
                    text = item.title,
                    color = ColorBox.text,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    ) {
        Canvas(
            modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(12)).clickable { onClicked.invoke(item) }
        ) {
            translate(16f.dp.toPx(), 12f.dp.toPx()) {
                with(icon) {
                    draw(Size(24f.dp.toPx(), 24f.dp.toPx()), colorFilter = ColorFilter.tint(if (selected) ColorBox.primary else ColorBox.text.copy(0.6f)))
                }
            }
            drawText(
                textLayoutResult = label,
                color = if (selected) ColorBox.primary else ColorBox.text.copy(0.6f),
                topLeft = Offset(56f.dp.toPx(),size.height / 2 - label.size.height / 2)
            )
        }
    }

}