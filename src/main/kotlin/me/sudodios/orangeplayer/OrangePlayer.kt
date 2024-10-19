package me.sudodios.orangeplayer

import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.*
import me.sudodios.orangeplayer.core.InitWindow
import me.sudodios.orangeplayer.core.Native
import me.sudodios.orangeplayer.core.media.Player
import me.sudodios.orangeplayer.ui.components.BaseDialog
import me.sudodios.orangeplayer.ui.components.FpsCounter
import me.sudodios.orangeplayer.ui.dialogs.DialogCurrentPlaylist
import me.sudodios.orangeplayer.ui.dialogs.DialogDropFiles
import me.sudodios.orangeplayer.ui.dialogs.DialogScanMedia
import me.sudodios.orangeplayer.ui.pages.MainScreen
import me.sudodios.orangeplayer.ui.sections.list.PageSection
import me.sudodios.orangeplayer.ui.theme.ColorBox
import me.sudodios.orangeplayer.ui.theme.Fonts
import me.sudodios.orangeplayer.utils.Events
import me.sudodios.orangeplayer.utils.painterResource
import org.jaudiotagger.audio.AudioFile
import java.awt.Dimension
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger

private fun initialize () {
    val initWindow = InitWindow()
    initWindow.show()
    Native.init(
        status = {
            initWindow.changeStateText(it)
        },
        callback = {
            if (it) {
                Player.init()
                initWindow.hide()
            } else {
                initWindow.notSupport()
            }
        }
    )
}

private fun disableSpamLogging() {
    LogManager.getLogManager().reset()
    Logger.getLogger(AudioFile::class.java.getPackage().name).apply { level = Level.OFF }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun App () {
    MaterialTheme(
        colorScheme = if (ColorBox.isDarkMode) darkColorScheme(
            surface = ColorBox.card,
            primary = ColorBox.primary,
            primaryContainer = ColorBox.window,
        ) else lightColorScheme(
            surface = ColorBox.card,
            primary = ColorBox.primary,
            primaryContainer = ColorBox.window
        ),
        typography = Fonts.getTypography()
    ) {
        val rippleConfiguration = RippleConfiguration(color = ColorBox.text.copy(0.8f), RippleAlpha(
            pressedAlpha = 0.10f,
            focusedAlpha = 0.12f,
            draggedAlpha = 0.16f,
            hoveredAlpha = 0.08f
        ))
        CompositionLocalProvider(LocalRippleConfiguration provides rippleConfiguration) {
            LaunchedEffect(Unit) {
                if (Native.dbCountMediaItems() == 0) {
                    Events.openScanDialog.value = true
                }
            }
            MainScreen()
            if (Events.showFpsCounter.value) {
                FpsCounter()
            }
            BaseDialog(expanded = Events.openScanDialog.value) {
                DialogScanMedia(
                    onClose = {
                        Events.openScanDialog.value = false
                    },
                    onFinished = {
                        PageSection.changePage(PageSection.selectedMenu.value,true)
                    }
                )
            }
            DialogDropFiles(
                show = Events.receivedFiles.isNotEmpty(),
                listPaths = Events.receivedFiles.toList(),
                onClose = {
                    Events.receivedFiles.clear()
                }
            )
            DialogCurrentPlaylist(
                show = Events.showCurrentPlaylist.value,
                onDismiss = {
                    Events.showCurrentPlaylist.value = false
                }
            )
        }
    }
}

fun main(args : Array<String>) {
    Application(
        onRecFiles = {
            Events.receivedFiles.clear()
            Events.receivedFiles.addAll(it)
            Events.reqWindowToFront.value = !Events.reqWindowToFront.value
        },
        onInitWindow = {
            initialize()
            disableSpamLogging()
            application {
                val state = rememberWindowState(position = WindowPosition(alignment = Alignment.Center))
                LaunchedEffect(Events.windowFullscreen.value) {
                    state.placement = if (Events.windowFullscreen.value) WindowPlacement.Fullscreen else WindowPlacement.Floating
                }
                val playingMediaName = if (Player.Live.currentMedia.value != null) " - ${Player.Live.currentMedia.value?.name}" else ""
                Window(
                    icon = painterResource("icons/app-icon.png"),
                    title = "Orange Player$playingMediaName",
                    onCloseRequest = ::exitApplication,
                    state = state,
                    onKeyEvent = {
                        Global.keyEvents(it)
                    },
                ) {
                    LaunchedEffect(Events.reqWindowToFront.value) {
                        window.toFront()
                    }
                    window.minimumSize = Dimension(450,550)
                    App()
                }
            }
        }
    ).run(args = args)
}