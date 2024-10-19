package me.sudodios.orangeplayer.ui.sections.players

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import me.sudodios.orangeplayer.utils.painterResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.sudodios.orangeplayer.core.media.AspectRatio
import me.sudodios.orangeplayer.core.media.Player
import me.sudodios.orangeplayer.ui.components.*
import me.sudodios.orangeplayer.ui.components.menu.NestedPopupMenu
import me.sudodios.orangeplayer.ui.components.menu.RepeatModePopup
import me.sudodios.orangeplayer.ui.components.menu.SpeedPlaybackPopup
import me.sudodios.orangeplayer.ui.components.menu.dropDownMenu
import me.sudodios.orangeplayer.ui.dialogs.DialogEqualizer
import me.sudodios.orangeplayer.ui.dialogs.DialogPlayerStatistics
import me.sudodios.orangeplayer.ui.dialogs.DialogTrackSync
import me.sudodios.orangeplayer.ui.hideableCursor
import me.sudodios.orangeplayer.ui.theme.ColorBox
import me.sudodios.orangeplayer.ui.theme.Fonts
import me.sudodios.orangeplayer.utils.Events
import me.sudodios.orangeplayer.utils.Utils
import me.sudodios.orangeplayer.utils.Utils.formatToDuration

private var forceShowCursor = mutableStateOf(false)

@Composable
fun LargePlayer() {
    var showOverlay by remember { mutableStateOf(false) }
    val isVideo = Player.Live.currentMedia.value?.isVideo ?: false

    Box(
        modifier = Modifier.fillMaxSize().background(if (isVideo) Color.Black else Color.Transparent)
            .hideableCursor(forceShowCursor.value, onVisibility = {
                showOverlay = it
            })
    ) {
        SurfaceView(isVideo)
        AnimatedVisibility(
            visible = showOverlay,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Overlay(
                mediaIsVideo = isVideo,
                onForceShowCursor = {
                    forceShowCursor.value = it
                }
            )
        }
    }

}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun BoxScope.SurfaceView(mediaIsVideo : Boolean) {
    if (mediaIsVideo) {
        VideoSurface(
            modifier = Modifier.fillMaxSize().onPointerEvent(PointerEventType.Press) {
                when {
                    it.buttons.isPrimaryPressed -> {
                        if (it.awtEventOrNull?.clickCount == 2) {
                            Events.windowFullscreen.value = !Events.windowFullscreen.value
                        }
                    }
                }
            },
            aspectRatio = Player.Live.aspectRatio.value
        )
    } else {
        SmoothImage(
            modifier = Modifier.align(Alignment.Center).size(160.dp).clip(RoundedCornerShape(12.dp)),
            image = Player.Live.currentMedia.value?.coverPath,
            placeHolder = "icons/app-icon.png",
            fadeOnChange = true
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Overlay(mediaIsVideo: Boolean,onForceShowCursor: (Boolean) -> Unit) {

    val textMeasurer = rememberTextMeasurer()
    val durationStyle = MaterialTheme.typography.labelLarge.copy(color = Color.White, fontFamily = Fonts.numbersFont)

    var showRepeatModePopup by remember { mutableStateOf(false) }
    var showSpeedPopup by remember { mutableStateOf(false) }
    var showOtherOptions by remember { mutableStateOf(false) }
    var showEqualizerDialog by remember { mutableStateOf(false) }
    var showDialogTrackSync by remember { mutableStateOf(false) }
    var showDialogStatistics by remember { mutableStateOf(false) }

    fun cannotHideCursor() : Boolean {
        return showSpeedPopup || showRepeatModePopup || showOtherOptions || showEqualizerDialog || showDialogTrackSync
    }

    Box(Modifier
        .drawWithContent {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(0.9f),
                        Color.Transparent,
                        Color.Transparent,
                        Color.Black.copy(0.9f)
                    )
                )
            )
            drawTexts(textMeasurer,durationStyle)
            drawContent()
        }
        .fillMaxSize()
    ) {
        EIconButton(
            padding = PaddingValues(16.dp),
            icon = "icons/arrow-left.svg",
            size = 44.dp,
            contentPadding = 9.dp,
            colorFilter = Color.White,
            background = Color.Black.copy(0.3f),
            onClick = {
                Player.Live.showLargePlayer.value = false
                Events.windowFullscreen.value = false
            }
        )
        EIconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            padding = PaddingValues(end = 76.dp,top = 16.dp),
            icon = "icons/playlist.svg",
            size = 44.dp,
            contentPadding = 9.dp,
            colorFilter = Color.White,
            background = Color.Black.copy(0.3f),
            onClick = {
                Events.showCurrentPlaylist.value = true
            }
        )
        EIconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            padding = PaddingValues(16.dp),
            enabled = Player.Live.currentMedia.value != null,
            icon = "icons/tuning.svg",
            size = 44.dp,
            contentPadding = 9.dp,
            colorFilter = Color.White,
            background = Color.Black.copy(0.3f),
            onClick = {
                showOtherOptions = true
                onForceShowCursor.invoke(true)
            }
        )

        NestedPopupMenu(
            expanded = showOtherOptions,
            onDismissRequested = {
                showOtherOptions = false
                onForceShowCursor.invoke(false)
            },
            menuItems = dropDownMenu {
                item("2","Audio tracks") {
                    Player.getAudioTracks().forEach {
                        item(it.id().toString(),it.description(), selected = Player.getSelectedAudioTrack() == it.id(), onClicked = {
                            Player.changeAudioTrack(it.id())
                            showOtherOptions = false
                            onForceShowCursor.invoke(false)
                        })
                    }
                }
                item("3","Audio devices") {
                    Player.getAudioDevices().forEach {
                        item(it.deviceId.toString(),it.longName, selected = Player.getSelectedAudioDevice() == it.deviceId, onClicked = {
                            Player.changeAudioDevice(it.deviceId)
                            showOtherOptions = false
                            onForceShowCursor.invoke(false)
                        })
                    }
                }
                item("4","Stereo mode") {
                    Player.getStereoModes().forEach {
                        item(it.intValue().toString(),it.name, selected = Player.getSelectedStereoMode() == it, onClicked = {
                            Player.changeStereoMode(it)
                            showOtherOptions = false
                            onForceShowCursor.invoke(false)
                        })
                    }
                }
                item("5","Video tracks", enabled = mediaIsVideo) {
                    Player.getVideoTracks().forEach {
                        item(it.id().toString(),it.description(), selected = Player.getSelectedVideoTrack() == it.id(), onClicked = {
                            Player.changeVideoTrack(it.id())
                            showOtherOptions = false
                            onForceShowCursor.invoke(false)
                        })
                    }
                }
                item("6","Aspect ratio", enabled = mediaIsVideo) {
                    AspectRatio.entries.forEach {
                        item(it.desc,it.desc, selected = Player.Live.aspectRatio.value == it, onClicked = {
                            Player.changeAspectRatio(it)
                            showOtherOptions = false
                            onForceShowCursor.invoke(false)
                        })
                    }
                }
                item("7","Subtitles", enabled = mediaIsVideo) {
                    item("addSub","Add subtitle file", icon = "icons/add-circle.svg", onClicked = {
                        val file = Utils.openFilePicker("Pick subtitle","srt", "ass", "vtt")
                        if (file != null) {
                            Player.addSubtitleFile(file)
                            showOtherOptions = false
                            onForceShowCursor.invoke(false)
                        }
                    })
                    Player.getSubtitles().forEach {
                        item(it.id().toString(),it.description(), selected = Player.getSelectedSubtitle() == it.id(), onClicked = {
                            Player.changeSubtitle(it.id())
                            showOtherOptions = false
                            onForceShowCursor.invoke(false)
                        })
                    }
                }
                item("1","Take snapshot", icon = "icons/screenshot.svg", enabled = mediaIsVideo, onClicked = {
                    Player.takeSnapshot()
                })
                item("sync","Track Sync", icon = "icons/sync.svg",enabled = mediaIsVideo, onClicked = {
                    showOtherOptions = false
                    showDialogTrackSync = true
                })
                item("statistics","Statistics", icon = "icons/chart-square.svg", onClicked = {
                    showOtherOptions = false
                    showDialogStatistics = true
                })
            }
        )

        DialogTrackSync(
            show = showDialogTrackSync,
            onDismissRequest = {
                showDialogTrackSync = false
                onForceShowCursor.invoke(false)
            }
        )

        DialogPlayerStatistics(
            show = showDialogStatistics,
            onDismiss = {
                showDialogStatistics = false
                onForceShowCursor.invoke(false)
            }
        )

        Column(modifier = Modifier
            .onPointerEvent(PointerEventType.Enter) {
                onForceShowCursor.invoke(true)
            }.onPointerEvent(PointerEventType.Exit) {
                if (cannotHideCursor()) {
                    onForceShowCursor.invoke(true)
                } else {
                    onForceShowCursor.invoke(false)
                }
            }.align(Alignment.BottomCenter).fillMaxWidth()
        ) {
            PlaybackProgress()
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 12.dp, bottom = 12.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayAnimationView(
                    color = ColorBox.window,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(50))
                        .background(ColorBox.primary)
                        .clickable {
                            Player.autoPlayPause()
                        }.padding(8.dp),
                    play = Player.Live.playPauseCallback.value
                )
                EIconButton(
                    padding = PaddingValues(start = 12.dp),
                    icon = "icons/previous.svg",
                    size = 38.dp,
                    contentPadding = 9.dp,
                    colorFilter = Color.White,
                    background = Color.White.copy(0.1f),
                    onClick = {
                        Player.previous()
                    }
                )
                EIconButton(
                    padding = PaddingValues(start = 12.dp),
                    icon = "icons/next.svg",
                    size = 38.dp,
                    contentPadding = 9.dp,
                    colorFilter = Color.White,
                    background = Color.White.copy(0.1f),
                    onClick = {
                        Player.next()
                    }
                )
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp).weight(1f)) {
                    EText(
                        text = Player.Live.currentMedia.value?.name.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                EIconButton(
                    padding = PaddingValues(end = 14.dp),
                    icon = "icons/sound-wave.svg",
                    size = 38.dp,
                    contentPadding = 9.dp,
                    colorFilter = Color.White,
                    background = Color.White.copy(0.1f),
                    onClick = {
                        showEqualizerDialog = true
                    }
                )
                DialogEqualizer(expanded = showEqualizerDialog, onDismissRequest = {
                    showEqualizerDialog = false
                    onForceShowCursor.invoke(false)
                })
                EIconButton(
                    padding = PaddingValues(end = 14.dp),
                    icon = when (Player.Live.repeatModeCallback.value) {
                        Player.REPEAT_MODE_ALL -> "icons/repeat-all.svg"
                        Player.REPEAT_MODE_ONE -> "icons/repeat-one.svg"
                        Player.REPEAT_MODE_SHUFFLE -> "icons/shuffle.svg"
                        Player.REPEAT_MODE_STOP -> "icons/stop.svg"
                        else -> "icons/repeat-all.svg"
                    },
                    size = 38.dp,
                    contentPadding = 9.dp,
                    colorFilter = Color.White,
                    background = Color.White.copy(0.1f),
                    onClick = {
                        showRepeatModePopup = true
                    }
                )
                RepeatModePopup(show = showRepeatModePopup, onDismissRequest = {
                    showRepeatModePopup = false
                    onForceShowCursor.invoke(false)
                })
                ESpeedButton(
                    padding = PaddingValues(end = 14.dp),
                    size = 38.dp,
                    value = Player.Live.speedCallback.value,
                    background = Color.White.copy(0.1f),
                    onClick = {
                        showSpeedPopup = true
                    }
                )
                SpeedPlaybackPopup(show = showSpeedPopup, onDismissRequest = {
                    showSpeedPopup = false
                    onForceShowCursor.invoke(false)
                })
                EIconButton(
                    icon = if (!Events.windowFullscreen.value) "icons/full-screen.svg" else "icons/exit-full-screen.svg",
                    size = 38.dp,
                    contentPadding = 9.dp,
                    colorFilter = Color.White,
                    background = Color.White.copy(0.1f),
                    onClick = {
                        Events.windowFullscreen.value = !Events.windowFullscreen.value
                    }
                )
            }
        }
        VolumeBrightnessViews(mediaIsVideo) {
            if (cannotHideCursor()) {
                onForceShowCursor.invoke(true)
            } else {
                onForceShowCursor.invoke(it)
            }
        }
    }

}

private fun ContentDrawScope.drawTexts(textMeasurer: TextMeasurer,durationStyle : TextStyle) {
    val total = Player.Live.currentMedia.value?.duration ?: 0
    val progress = if (isSeeking.value) progressSeekValue.value else Player.Live.progressCallback.value
    val current = (progress * total).toLong()
    val totalText = total.formatToDuration()

    drawText(
        textMeasurer = textMeasurer,
        text = current.formatToDuration(),
        topLeft = Offset(16f.dp.toPx(), size.height - 110f.dp.toPx()),
        style = durationStyle
    )
    drawText(
        textMeasurer = textMeasurer,
        text = totalText,
        topLeft = Offset(size.width - textMeasurer.measure(totalText, style = durationStyle).size.width - 16f.dp.toPx(), size.height - 110f.dp.toPx()),
        style = durationStyle
    )
}

@Composable
private fun PlaybackProgress() {
    CustomSlider(
        modifier = Modifier.fillMaxWidth(),
        value = if (isSeeking.value) progressSeekValue.value else Player.Live.progressCallback.value,
        valueChanged = {
            progressSeekValue.value = it
            isSeeking.value = true
        },
        valueChangedFinished = {
            isSeeking.value = false
            Player.seekTo(progressSeekValue.value)
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun BoxScope.VolumeBrightnessViews(mediaIsVideo: Boolean,onForceShowCursor: (Boolean) -> Unit) {
    Column(
        modifier = Modifier.align(Alignment.CenterEnd)
            .padding(start = 12.dp, end = 12.dp, bottom = 64.dp)
            .width(48.dp).height(210.dp)
            .drawBehind {
                drawRoundRect(color = Color.Black, cornerRadius = CornerRadius(50f))
            }.onPointerEvent(PointerEventType.Enter) {
                onForceShowCursor.invoke(true)
            }.onPointerEvent(PointerEventType.Exit) {
                onForceShowCursor.invoke(false)
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        VerticalSlider(
            modifier = Modifier.padding(top = 20.dp).height(130.dp),
            value = (Player.Live.volumeCallback.value / 1.2f / 100f),
            valueChanged = {
                Player.changeVolume((it * 1.2 * 100).toInt())
            },
            valueChangedFinished = {}
        )
        EIconButton(
            padding = PaddingValues(top = 12.dp),
            icon = if (Player.Live.isMutedCallback.value) "icons/sound-mute.svg" else "icons/volume-high.svg",
            colorFilter = Color.White,
            onClick = {
                Player.autoMute()
            }
        )
    }
    if (mediaIsVideo) {
        Column(
            modifier = Modifier.align(Alignment.CenterStart)
                .padding(start = 12.dp, end = 12.dp, bottom = 64.dp)
                .width(48.dp).height(210.dp)
                .drawBehind {
                    drawRoundRect(color = Color.Black, cornerRadius = CornerRadius(50f))
                }.onPointerEvent(PointerEventType.Enter) {
                    onForceShowCursor.invoke(true)
                }.onPointerEvent(PointerEventType.Exit) {
                    onForceShowCursor.invoke(false)
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            VerticalSlider(
                modifier = Modifier.padding(top = 20.dp).height(130.dp),
                value = Player.Live.brightnessCallback.value,
                valueChanged = {
                    Player.brightness(it)
                },
                valueChangedFinished = {}
            )
            Icon(
                modifier = Modifier.padding(top = 12.dp).size(48.dp).padding(12.dp),
                painter = painterResource("icons/sun.svg"),
                tint = Color.White,
                contentDescription = null,
            )
        }
    }
}