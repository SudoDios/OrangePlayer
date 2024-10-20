package me.sudodios.orangeplayer.utils

import cafe.adriel.satchel.Satchel
import cafe.adriel.satchel.ktx.getOrDefault
import cafe.adriel.satchel.storer.file.FileSatchelStorer
import me.sudodios.orangeplayer.Global
import me.sudodios.orangeplayer.core.media.Player
import java.io.File

object Pref {

    private val preferences = Satchel.with(FileSatchelStorer(File(Global.PREFS_PATH)))

    var playbackSpeed : Float = 0.33f

    var initializedLibs : Boolean
        get() { return preferences.getOrDefault("initializedLibs",false) }
        set(value) { preferences["initializedLibs"] = value }

    var repeatMode : Int
        get() { return preferences.getOrDefault("repeatMode",Player.REPEAT_MODE_ALL) }
        set(value) {
            Player.Live.repeatModeCallback.value = value
            preferences["repeatMode"] = value
        }

    var equalizerOn : Boolean
        get() { return preferences.getOrDefault("equalizerOn",false) }
        set(value) {
            preferences["equalizerOn"] = value
        }

    var equalizerPreset : String
        get() { return preferences.getOrDefault("equalizerPreset","Club") }
        set(value) {
            preferences["equalizerPreset"] = value
        }

    var isDarkTheme : Boolean
        get() { return preferences.getOrDefault("isDarkTheme",true) }
        set(value) {
            preferences["isDarkTheme"] = value
        }

    var showFpsCounter : Boolean
        get() { return preferences.getOrDefault("showFpsCounter",false) }
        set(value) {
            preferences["showFpsCounter"] = value
            Events.showFpsCounter.value = value
        }

    var viewIsList : Boolean
        get() { return preferences.getOrDefault("viewIsList",true) }
        set(value) {
            preferences["viewIsList"] = value
            Events.viewIsList.value = value
        }

}
