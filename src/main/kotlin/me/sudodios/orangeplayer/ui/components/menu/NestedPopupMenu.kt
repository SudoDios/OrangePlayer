package me.sudodios.orangeplayer.ui.components.menu

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.sudodios.orangeplayer.ui.components.AutoResizeText
import me.sudodios.orangeplayer.ui.components.FontSizeRange
import me.sudodios.orangeplayer.ui.theme.ColorBox
import me.sudodios.orangeplayer.utils.painterResource

data class NestedMenuItem(
    val id: String? = null,
    var title: String = "",
    var icon: String? = null,
    var enabled: Boolean = true,
    var selected: Boolean = false,
    var onClicked: () -> Unit = {}
) {
    var children: MutableList<NestedMenuItem>? = null
    var parent: NestedMenuItem? = null

    fun hasChildren(): Boolean = !children.isNullOrEmpty()
    fun hasParent(): Boolean = parent != null

    fun getChild(id: String): NestedMenuItem? =
        children?.let { items -> items.find { item -> item.id == id } }
}

class DropDownMenuBuilder {
    var menu: NestedMenuItem = NestedMenuItem()
    fun item(
        id: String,
        title: String,
        icon: String? = null,
        enabled: Boolean = true,
        selected: Boolean = false,
        onClicked: () -> Unit = {},
        init: (DropDownMenuBuilder.() -> Unit)? = null
    ) {
        val newItem = NestedMenuItem(id, title, icon, enabled, selected, onClicked)
        newItem.parent = menu
        init?.invoke(DropDownMenuBuilder().apply { menu = newItem })
        if (menu.children == null) {
            menu.children = mutableListOf()
        }
        menu.children?.add(newItem)
    }
}

fun dropDownMenu(init: DropDownMenuBuilder.() -> Unit): NestedMenuItem {
    val menuBuilder = DropDownMenuBuilder()
    menuBuilder.init()
    return menuBuilder.menu
}


@Composable
fun NestedMenuItem(menuItem: NestedMenuItem, onClicked: () -> Unit) {
    Row(
        modifier = Modifier.width(210.dp).height(38.dp)
            .alpha(if (menuItem.enabled) 1f else 0.4f)
            .clip(RoundedCornerShape(8.dp))
            .clickable(menuItem.enabled) { onClicked.invoke() }
            .pointerHoverIcon(if (menuItem.enabled) PointerIcon.Hand else PointerIcon.Default)
            .padding(start = 8.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (menuItem.icon != null) {
            Icon(
                modifier = Modifier.size(22.dp),
                painter = painterResource(menuItem.icon!!),
                contentDescription = null,
                tint = ColorBox.text.copy(0.7f)
            )
            Spacer(Modifier.padding(6.dp))
        }
        AutoResizeText(
            modifier = Modifier.padding(4.dp).weight(1f),
            text = menuItem.title,
            color = ColorBox.text,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 0.sp),
            fontSizeRange = FontSizeRange(10.sp,14.sp),
            maxLines = 2
        )
        if (menuItem.hasChildren()) {
            Icon(
                modifier = Modifier.size(19.dp).rotate(180f),
                painter = painterResource("icons/arrow-left-min.svg"),
                contentDescription = null,
                tint = ColorBox.text.copy(0.7f)
            )
        } else {
            if (menuItem.selected) {
                Icon(
                    modifier = Modifier.size(19.dp),
                    painter = painterResource("icons/tick-circle-tone.svg"),
                    contentDescription = null,
                    tint = ColorBox.text.copy(0.7f)
                )
            }
        }
    }
}

@Composable
fun NestedPopupMenu(
    expanded: Boolean,
    onDismissRequested: () -> Unit,
    menuItems: NestedMenuItem
) {

    CustomDropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequested,
    ) {

        var currentMenu by remember { mutableStateOf(menuItems) }
        var isReverse by remember { mutableStateOf(false) }

        AnimatedContent(currentMenu, transitionSpec = {
            slideInHorizontally(
                animationSpec = tween(300),
                initialOffsetX = { fullWidth -> if (isReverse) fullWidth else -fullWidth }
            ) togetherWith
                    slideOutHorizontally(
                        animationSpec = tween(300),
                        targetOffsetX = { fullWidth -> if (isReverse) -fullWidth else fullWidth }
                    )
        }) { targetMenu ->
            Column {
                if (targetMenu.hasParent()) {
                    NestedMenuItem(
                        NestedMenuItem(
                            "Back${targetMenu.id}",
                            icon = "icons/arrow-left-min.svg",
                            title = targetMenu.title,
                            onClicked = {}), onClicked = {
                            isReverse = false
                            currentMenu = menuItems
                        })
                }
                if (targetMenu.hasChildren()) {
                    targetMenu.children?.forEach {
                        NestedMenuItem(it, onClicked = {
                            if (it.hasChildren()) {
                                isReverse = true
                                currentMenu = targetMenu.getChild(it.id!!)!!
                            } else {
                                it.onClicked.invoke()
                            }
                        })
                    }
                }
            }
        }
    }

}