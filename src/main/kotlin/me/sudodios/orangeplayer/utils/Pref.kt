package me.sudodios.orangeplayer.utils

import cafe.adriel.pufferdb.core.PufferDB
import me.sudodios.orangeplayer.Global
import me.sudodios.orangeplayer.core.media.Player
import java.io.File

object Pref {

    private val preferences = PufferDB.with(File(Global.PREFS_PATH))

    var playbackSpeed : Float = 0.33f

    var initializedLibs : Boolean
        get() { return preferences.get("initializedLibs",false) }
        set(value) { preferences.put("initializedLibs",value) }

    var repeatMode : Int
        get() { return preferences.get("repeatMode",Player.REPEAT_MODE_ALL) }
        set(value) {
            Player.Live.repeatModeCallback.value = value
            preferences.put("repeatMode",value)
        }

    var equalizerOn : Boolean
        get() { return preferences.get("equalizerOn",false) }
        set(value) { preferences.put("equalizerOn",value) }

    var equalizerPreset : String
        get() { return preferences.get("equalizerPreset","Club") }
        set(value) { preferences.put("equalizerPreset",value) }

    var isDarkTheme : Boolean
        get() { return preferences.get("isDarkTheme",true) }
        set(value) { preferences.put("isDarkTheme",value) }

    var showFpsCounter : Boolean
        get() { return preferences.get("showFpsCounter",false) }
        set(value) {
            preferences.put("showFpsCounter",value)
            Events.showFpsCounter.value = value
        }

    var viewIsList : Boolean
        get() { return preferences.get("viewIsList",true) }
        set(value) {
            preferences.put("viewIsList",value)
            Events.viewIsList.value = value
        }

}
