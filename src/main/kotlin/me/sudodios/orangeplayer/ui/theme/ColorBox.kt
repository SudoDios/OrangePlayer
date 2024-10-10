package me.sudodios.orangeplayer.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import me.sudodios.orangeplayer.utils.Pref

object ColorBox {

    private val PRIMARY_COLOR_LIGHT = Color(0xFFF78D27)
    private val SUCCESS_COLOR_LIGHT = Color(0xFF1b8553)
    private val WARNING_COLOR_LIGHT = Color(0xFF9c6e03)
    private val ERROR_COLOR_LIGHT = Color(0xFFc01c28)
    private val WINDOW_COLOR_LIGHT = Color(0XFFfafafa)
    private val CARD_COLOR_LIGHT = Color(0XFFEBEBEB)
    private val CARD2_COLOR_LIGHT = Color(0XFFF0F0F0)
    private val VIEW_COLOR_LIGHT = Color(0XFFd8d8d8)
    private val TEXT_COLOR_LIGHT = Color(0xFF000000)

    private val PRIMARY_COLOR_DARK = Color(0xFFF78D27)
    private val SUCCESS_COLOR_DARK = Color(0xFF8ff0a4)
    private val WARNING_COLOR_DARK = Color(0xFFf8e45c)
    private val ERROR_COLOR_DARK = Color(0xFFff7b63)
    private val WINDOW_COLOR_DARK = Color(0XFF242424)
    private val CARD_COLOR_DARK = Color(0xFF363636)
    private val CARD2_COLOR_DARK = Color(0XFF303030)
    private val VIEW_COLOR_DARK = Color(0XFF424242)
    private val TEXT_COLOR_DARK = Color(0XFFFFFFFF)

    //theming
    var isDarkMode by mutableStateOf(Pref.isDarkTheme)

    var primary by mutableStateOf(if (isDarkMode) PRIMARY_COLOR_DARK else PRIMARY_COLOR_LIGHT)
    var success by mutableStateOf(if (isDarkMode) SUCCESS_COLOR_DARK else SUCCESS_COLOR_LIGHT)
    var error by mutableStateOf(if (isDarkMode) ERROR_COLOR_DARK else ERROR_COLOR_LIGHT)
    var warning by mutableStateOf(if (isDarkMode) WARNING_COLOR_DARK else WARNING_COLOR_LIGHT)
    var window by mutableStateOf(if (isDarkMode) WINDOW_COLOR_DARK else WINDOW_COLOR_LIGHT)
    var card by mutableStateOf(if (isDarkMode) CARD_COLOR_DARK else CARD_COLOR_LIGHT)
    var card2 by mutableStateOf(if (isDarkMode) CARD2_COLOR_DARK else CARD2_COLOR_LIGHT)
    var view by mutableStateOf(if (isDarkMode) VIEW_COLOR_DARK else VIEW_COLOR_LIGHT)
    var text by mutableStateOf(if (isDarkMode) TEXT_COLOR_DARK else TEXT_COLOR_LIGHT)

    fun lightTheme() {
        primary = PRIMARY_COLOR_LIGHT
        success = SUCCESS_COLOR_LIGHT
        error = ERROR_COLOR_LIGHT
        warning = WARNING_COLOR_LIGHT
        window = WINDOW_COLOR_LIGHT
        card = CARD_COLOR_LIGHT
        card2 = CARD2_COLOR_LIGHT
        view = VIEW_COLOR_LIGHT
        text = TEXT_COLOR_LIGHT
        Pref.isDarkTheme = false
        isDarkMode = false
    }

    fun darkTheme() {
        primary = PRIMARY_COLOR_DARK
        success = SUCCESS_COLOR_DARK
        error = ERROR_COLOR_DARK
        warning = WARNING_COLOR_DARK
        window = WINDOW_COLOR_DARK
        card = CARD_COLOR_DARK
        card2 = CARD2_COLOR_DARK
        view = VIEW_COLOR_DARK
        text = TEXT_COLOR_DARK
        Pref.isDarkTheme = true
        isDarkMode = true
    }

}