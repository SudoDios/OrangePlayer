package me.sudodios.orangeplayer.ui.components

import androidx.compose.animation.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

@Composable
fun AnimatedText(
    modifier: Modifier,
    text : String,
    color: Color = Color.Unspecified,
    style : TextStyle = MaterialTheme.typography.bodyMedium
) {
    AnimatedContent(
        targetState = text,
        transitionSpec = {
            (slideInVertically { height -> height } + fadeIn() togetherWith
                    slideOutVertically { height -> -height } + fadeOut()).using(SizeTransform(false))
        }
    ) {
        EText(
            text = text,
            modifier = modifier,
            color = color,
            style = style
        )
    }
}