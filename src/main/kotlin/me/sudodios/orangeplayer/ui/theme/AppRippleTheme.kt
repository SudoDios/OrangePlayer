package me.sudodios.orangeplayer.ui.theme

import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor(): Color {
        return ColorBox.text.copy(0.8f)
    }
    @Composable
    override fun rippleAlpha(): RippleAlpha {
        return DefaultRippleAlpha
    }
    private val DefaultRippleAlpha = RippleAlpha(
        pressedAlpha = 0.10f,
        focusedAlpha = 0.12f,
        draggedAlpha = 0.16f,
        hoveredAlpha = 0.08f
    )
}