package me.sudodios.orangeplayer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import me.sudodios.orangeplayer.ui.theme.ColorBox
import me.sudodios.orangeplayer.ui.theme.Fonts
import me.sudodios.orangeplayer.utils.Events
import me.sudodios.orangeplayer.utils.painterResource

@Composable
fun ESearchBox(
    modifier: Modifier = Modifier,
    value : String = "",
    onValueChange: (String) -> Unit,
) {

    Row(modifier = modifier
        .drawBehind {
            drawRoundRect(color = ColorBox.text.copy(0.1f), cornerRadius = CornerRadius(50f,50f))
        },verticalAlignment = Alignment.CenterVertically) {
        Icon(
            modifier = Modifier.size(48.dp).padding(12.dp),
            painter = painterResource("icons/search-normal.svg"),
            contentDescription = null,
            tint = ColorBox.text.copy(0.8f)
        )
        Box(modifier = Modifier.weight(1f)) {
            BasicTextField(
                modifier = Modifier.fillMaxWidth().onFocusChanged {
                    Events.anyTextFiledFocus.value = it.hasFocus
                },
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = ColorBox.text, fontFamily = Fonts.mainFont),
                cursorBrush = SolidColor(ColorBox.primary),
            )
            this@Row.AnimatedVisibility(
                visible = value.isEmpty(),
            ) {
                EText(
                    text = "Search media files",
                    color = ColorBox.text.copy(0.4f),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        AnimatedVisibility(
            visible = value.isNotEmpty(),
        ) {
            EButton(
                icon = "icons/close.svg",
                backgroundColor = Color.Transparent,
                onClick = {
                    onValueChange.invoke("")
                }
            )
        }

    }

}