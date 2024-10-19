package me.sudodios.orangeplayer.utils

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList

object Events {

    val receivedFiles = SnapshotStateList<String>()
    val showProgressLoading = mutableStateOf(false)
    val reqWindowToFront = mutableStateOf(false)
    val showCurrentPlaylist = mutableStateOf(false)

    val windowFullscreen = mutableStateOf(false)
    val openScanDialog = mutableStateOf(false)

    val goPlayingItemInList = EventLiveData(false)
    val anyTextFiledFocus = mutableStateOf(false)
    val showFpsCounter = mutableStateOf(Pref.showFpsCounter)
    val viewIsList = mutableStateOf(Pref.viewIsList)

}