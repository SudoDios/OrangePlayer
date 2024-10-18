package me.sudodios.orangeplayer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import me.sudodios.orangeplayer.utils.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.sudodios.orangeplayer.ui.theme.ColorBox
import me.sudodios.orangeplayer.ui.theme.Fonts
import me.sudodios.orangeplayer.utils.Utils.roundTo

@Composable
fun EButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    backgroundColor: Color = ColorBox.text.copy(0.07f),
    enabled : Boolean = true,
    text : String? = null,
    icon : String? = null,
    centerText : Boolean = false,
    clip : Shape = RoundedCornerShape(50),
    textColor : Color = ColorBox.text,
    iconColor : Color = ColorBox.text,
    textStyle : TextStyle = MaterialTheme.typography.bodyMedium,
) {

    val textLabel = if (text != null) rememberTextMeasurer().measure(text.toString(), textStyle.copy(fontFamily = Fonts.mainFont)) else null
    val iconPainter = if (icon != null) painterResource(icon) else null

    fun calcWidth () : Dp {
        var width = 0
        if (iconPainter != null) {
            width += 48
        }
        if (textLabel != null) {
            width += textLabel.size.width + if (iconPainter == null) 32 else 16
        }
        return width.dp
    }

    Canvas(modifier
        .width(calcWidth())
        .height(48.dp)
        .alpha(if (enabled) 1f else 0.8f)
        .clip(clip)
        .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
        .clickable(enabled = enabled) { onClick.invoke() }) {
        drawRect(color = backgroundColor)
        if (iconPainter != null) {
            translate(left = 12f.dp.toPx(), top = 12f.dp.toPx()) {
                with(iconPainter) {
                    draw(Size(24f.dp.toPx(),24f.dp.toPx()), colorFilter = ColorFilter.tint(iconColor))
                }
            }
        }
        if (textLabel != null) {
            val offsetText = if (centerText) {
                Offset(size.width / 2 - textLabel.size.width / 2, size.height / 2 - textLabel.size.height / 2)
            } else {
                Offset(if (iconPainter != null) 48f.dp.toPx() else 16f.dp.toPx(),size.height / 2 - textLabel.size.height / 2)
            }
            drawText(
                textLayoutResult = textLabel,
                color = textColor,
                topLeft = offsetText
            )
        }
    }

}

@Composable
fun EIconButton(
    modifier: Modifier = Modifier,
    size : Dp = 48.dp,
    enabled: Boolean = true,
    rotate : Float = 0f,
    icon : String,
    background : Color = Color.Transparent,
    padding : PaddingValues = PaddingValues(0.dp),
    contentPadding : Dp = 12.dp,
    colorFilter : Color = ColorBox.text.copy(0.8f),
    onClick: () -> Unit,
) {

    val iconPainter = painterResource(icon)

    Canvas(modifier = modifier.padding(padding)
        .size(size = size)
        .rotate(rotate)
        .clip(RoundedCornerShape(50))
        .background(background)
        .pointerHoverIcon(icon = if (enabled) PointerIcon.Hand else PointerIcon.Default)
        .clickable(
            enabled = enabled,
            onClick = onClick,
            role = Role.Button
        )
    ) {
        val iconSize = size.toPx() - (contentPadding.toPx() * 2)
        translate(left = this.size.width / 2 - iconSize / 2, top = this.size.height / 2 - iconSize / 2) {
            with(iconPainter) {
                draw(Size(iconSize ,iconSize), colorFilter = ColorFilter.tint(colorFilter))
            }
        }
    }
}

@Composable
fun ESpeedButton(
    size : Dp = 48.dp,
    value : Float,
    fontSize : TextUnit = 12.sp,
    textColor : Color = Color.White,
    padding : PaddingValues = PaddingValues(0.dp),
    enabled: Boolean = true,
    background : Color = Color.Transparent,
    onClick: () -> Unit,
) {

    val speedLabel = rememberTextMeasurer()
        .measure("${value.roundTo(1)}x", style =
        MaterialTheme.typography.bodySmall.copy(fontSize = fontSize, lineHeight = 0.sp, fontFamily = Fonts.mainFont))

    Canvas(modifier = Modifier.padding(padding)
        .size(size = size)
        .clip(RoundedCornerShape(50))
        .background(background)
        .pointerHoverIcon(icon = if (enabled) PointerIcon.Hand else PointerIcon.Default)
        .clickable(
            enabled = enabled,
            onClick = onClick,
            role = Role.Button
        )
    ) {
        drawText(
            textLayoutResult = speedLabel,
            topLeft = Offset(this.size.width / 2 - speedLabel.size.width / 2, this.size.height / 2 - speedLabel.size.height / 2),
            color = textColor
        )
    }
}