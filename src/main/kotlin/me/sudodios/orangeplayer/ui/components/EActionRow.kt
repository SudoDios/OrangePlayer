package me.sudodios.orangeplayer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.sudodios.orangeplayer.ui.theme.ColorBox
import me.sudodios.orangeplayer.ui.theme.Fonts

@Composable
fun EActionRow(
    modifier: Modifier = Modifier,
    backgroundColor : Color = ColorBox.card2,
    clip : Shape = RoundedCornerShape(16.dp),
    reverse : Boolean = false,
    enabled: Boolean = true,
    title: String,
    value: String? = null,
    icon : String? = null,
) {

    val textMeasurer = rememberTextMeasurer()
    val titleStyle = MaterialTheme.typography.bodyMedium.copy(color = ColorBox.text, fontFamily = Fonts.mainFont)
    val valueStyle = MaterialTheme.typography.labelSmall.copy(color = ColorBox.text.copy(0.6f), fontFamily = Fonts.mainFont)
    val iconPainter = if (icon != null) painterResource(icon) else null

    Canvas(
        modifier = modifier.width(180.dp).height(58.dp).clip(clip).background(backgroundColor)
            .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
            .clickable(enabled = enabled) {  }
    ) {
        val titleSize = textMeasurer.measure(title, titleStyle).size
        drawText(
            textMeasurer = textMeasurer,
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = if (reverse) valueStyle else titleStyle,
            topLeft = Offset(12f.dp.toPx(),if (value == null) size.height / 2 - titleSize.height / 2 else 12f.dp.toPx()),
        )
        if (value != null) {
            drawText(
                textMeasurer = textMeasurer,
                text = value,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = if (reverse) titleStyle else valueStyle,
                topLeft = Offset(12f.dp.toPx(),14f.dp.toPx() + titleSize.height),
            )
        }
        if (iconPainter != null) {
            translate(size.width - 36f.dp.toPx(),size.height / 2 - 12f.dp.toPx()) {
                with(iconPainter) {
                    draw(Size(24f.dp.toPx(),24f.dp.toPx()), colorFilter = ColorFilter.tint(ColorBox.text.copy(0.8f)))
                }
            }
        }
    }
}