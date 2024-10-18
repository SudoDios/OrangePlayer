package me.sudodios.orangeplayer.utils

import androidx.compose.runtime.mutableStateOf

object Events {

    val showProgressLoading = mutableStateOf(false)

    val windowFullscreen = mutableStateOf(false)
    val openScanDialog = mutableStateOf(false)

    val goPlayingItemInList = EventLiveData(false)
    val anyTextFiledFocus = mutableStateOf(false)
    val showFpsCounter = mutableStateOf(Pref.showFpsCounter)
    val viewIsList = mutableStateOf(Pref.viewIsList)

}