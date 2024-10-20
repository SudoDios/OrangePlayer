package me.sudodios.orangeplayer

import androidx.compose.ui.input.key.*
import me.sudodios.orangeplayer.core.media.Player
import me.sudodios.orangeplayer.utils.Events
import java.io.File

object Global {

    val userHome : String get() = System.getProperty("user.home")
    val LIB_CORE_PATH = "$userHome${File.separator}.orangeplayer"
    val COVER_PATH = "$userHome${File.separator}.orangeplayer${File.separator}covers"
    val PREFS_PATH = "$userHome${File.separator}.orangeplayer${File.separator}orange-prefs.db"
    val DB_PATH = "$userHome${File.separator}.orangeplayer${File.separator}orange-player.db"
    val SNAPSHOT_PATH = "$userHome${File.separator}Pictures"

    /*
    * Other key events
    * */
    fun keyEvents(it: KeyEvent) : Boolean {
        if (!Events.anyTextFiledFocus.value) {
            when {
                (it.isCtrlPressed && it.key == Key.G && it.type == KeyEventType.KeyUp) -> {
                    Events.goPlayingItemInList.value = !Events.goPlayingItemInList.value
                    return true
                }

                (it.key == Key.Spacebar && it.type == KeyEventType.KeyUp) -> {
                    Player.autoPlayPause()
                    return true
                }

                (it.key == Key.DirectionRight && it.type == KeyEventType.KeyUp) -> {
                    Player.forward(15000)
                    return true
                }

                (it.key == Key.DirectionLeft && it.type == KeyEventType.KeyUp) -> {
                    Player.backward(10000)
                    return true
                }

                (it.key == Key.DirectionUp && it.type == KeyEventType.KeyUp) -> {
                    Player.incVolume()
                    return true
                }

                (it.key == Key.DirectionDown && it.type == KeyEventType.KeyUp) -> {
                    Player.decVolume()
                    return true
                }
            }
        } else {
            return false
        }
        return false
    }

    enum class DragState {
        DRAG_ENTER,
        DRAG_EXIT,
        DROPPED
    }

}