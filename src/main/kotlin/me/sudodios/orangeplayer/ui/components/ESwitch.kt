package me.sudodios.orangeplayer.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import me.sudodios.orangeplayer.ui.theme.ColorBox
import me.sudodios.orangeplayer.ui.theme.Fonts

@Composable
@Preview
fun ESwitch(
    modifier: Modifier = Modifier,
    isChecked: Boolean,
    onClicked : (() -> Unit)? = null,
    text: String,
    enabled: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium
) {

    val textLabel = rememberTextMeasurer().measure(text, textStyle.copy(fontFamily = Fonts.mainFont))
    val animator = animateFloatAsState(if (isChecked) 1f else 0f)

    Canvas(modifier = modifier
        .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
        .alpha(if (enabled) 1f else 0.7f).height(42.dp).clickable(enabled = enabled) { onClicked?.invoke() }) {
        drawText(
            textLayoutResult = textLabel,
            color = ColorBox.text,
            topLeft = Offset(16f.dp.toPx(),(size.height / 2) - textLabel.size.height / 2)
        )

        drawRoundRect(
            color = androidx.compose.ui.graphics.lerp(ColorBox.text.copy(0.1f),ColorBox.primary,animator.value),
            topLeft = Offset(size.width - 56f.dp.toPx() - 5f.dp.toPx(),size.height / 2 - 16f.dp.toPx()),
            size = Size(56f.dp.toPx(),32f.dp.toPx()),
            cornerRadius = CornerRadius(50f,50f)
        )
        drawCircle(
            color = androidx.compose.ui.graphics.lerp(ColorBox.text.copy(0.7f),ColorBox.window,animator.value),
            center = Offset(size.width - lerp(44f.dp.toPx(),21f.dp.toPx(),animator.value),size.height / 2),
            radius = 12f.dp.toPx()
        )
    }
}