package me.sudodios.orangeplayer.core.media

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.sudodios.orangeplayer.Global
import me.sudodios.orangeplayer.core.Native
import me.sudodios.orangeplayer.models.MediaItem
import me.sudodios.orangeplayer.utils.Pref
import me.sudodios.orangeplayer.utils.Utils
import me.sudodios.orangeplayer.utils.Utils.formatToDuration
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Pixmap
import uk.co.caprica.vlcj.media.MediaStatistics
import uk.co.caprica.vlcj.player.base.*
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurface
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat
import java.io.File
import java.nio.ByteBuffer

class SkiaImageVideoSurface : VideoSurface(VideoSurfaceAdapters.getVideoSurfaceAdapter()) {

    private val videoSurface = SkiaImageCallbackVideoSurface()
    private lateinit var pixmap: Pixmap

    private var sourceWidth: Int = 0
    private var sourceHeight: Int = 0
    val skiaImage = mutableStateOf<Image?>(null)

    fun aspectRatio(): Float = sourceWidth.toFloat() / sourceHeight.toFloat()

    private inner class SkiaImageBufferFormatCallback : BufferFormatCallback {
        override fun getBufferFormat(sourceWidth: Int, sourceHeight: Int): BufferFormat {
            this@SkiaImageVideoSurface.sourceWidth = sourceWidth
            this@SkiaImageVideoSurface.sourceHeight = sourceHeight
            return RV32BufferFormat(sourceWidth, sourceHeight)
        }
        override fun allocatedBuffers(buffers: Array<ByteBuffer>) {
            val pointer = Native.getBufferAddr(buffers[0])
            val imageInfo = ImageInfo.makeN32Premul(sourceWidth, sourceHeight, ColorSpace.sRGB)
            pixmap = Pixmap.make(imageInfo, pointer, sourceWidth * 4,null)
        }
    }
    private inner class SkiaImageRenderCallback : RenderCallback {
        override fun display(mediaPlayer: MediaPlayer, nativeBuffers: Array<ByteBuffer>, bufferFormat: BufferFormat) {
            skiaImage.value = Image.makeFromPixmap(pixmap)
        }
    }
    private inner class SkiaImageCallbackVideoSurface : CallbackVideoSurface(SkiaImageBufferFormatCallback(),
        SkiaImageRenderCallback(), true, videoSurfaceAdapter,)
    override fun attach(mediaPlayer: MediaPlayer) {
        videoSurface.attach(mediaPlayer)
    }
}

enum class AspectRatio(var ratio : Float?,var desc : String) {
    DEFAULT(null,"Default"),
    R16_9(16f / 9f,"16:9"),
    R4_3(4f / 3f,"4:3"),
    R1_1(1f / 1f,"1:1"),
    R16_10(16f / 10f,"16:10"),
    R221_100(221f / 100f,"2.21:1"),
    R235_100(235f / 100f,"2.35:1"),
    R239_100(239f / 100f,"2.39:1"),
    R5_4(5f / 4f,"5:4"),
}

object Player {

    const val REPEAT_MODE_ALL = 0
    const val REPEAT_MODE_ONE = 1
    const val REPEAT_MODE_SHUFFLE = 2
    const val REPEAT_MODE_STOP = 3

    object Live {

        var showLargePlayer = mutableStateOf(false)
        var currentMedia = mutableStateOf<MediaItem?>(null)
        var progressCallback = mutableStateOf(0f)
        var repeatModeCallback = mutableStateOf(Pref.repeatMode)
        var playPauseCallback = mutableStateOf(false)
        var isMutedCallback = mutableStateOf(false)
        var speedCallback = mutableStateOf(1.0f)
        var volumeCallback = mutableStateOf(100)
        var brightnessCallback = mutableStateOf(1f)
        var aspectRatio = mutableStateOf(AspectRatio.DEFAULT)

        var waveform = mutableStateOf<FloatArray?>(null)

        var equalizerOn = mutableStateOf(Pref.equalizerOn)
        var equalizerPreset = mutableStateOf(Pref.equalizerPreset)

        var statistics = mutableStateOf<MediaStatistics?>(null)

    }

    private val mediaPlayer = EmbeddedMediaPlayerComponent()
    private val playList = mutableListOf<MediaItem>()
    val videoSurface = SkiaImageVideoSurface()

    fun init() {
        mediaPlayer.mediaPlayer().events().addMediaPlayerEventListener(object : MediaPlayerEventAdapter() {
            override fun playing(mediaPlayer: MediaPlayer?) {
                Live.playPauseCallback.value = true
                super.playing(mediaPlayer)
            }

            override fun paused(mediaPlayer: MediaPlayer?) {
                Live.playPauseCallback.value = false
                super.paused(mediaPlayer)
            }

            override fun finished(mediaPlayer: MediaPlayer?) {
                when (Pref.repeatMode) {
                    REPEAT_MODE_ALL -> {
                        Utils.postDelayed(200) {
                            next()
                        }
                    }

                    REPEAT_MODE_ONE -> {
                        Utils.postDelayed(200) {
                            startPlay(Live.currentMedia.value!!)
                        }
                    }

                    REPEAT_MODE_SHUFFLE -> {
                        Utils.postDelayed(200) {
                            shuffle()
                        }
                    }

                    REPEAT_MODE_STOP -> {
                        Utils.postDelayed(200) {
                            stop()
                        }
                    }
                }
                super.finished(mediaPlayer)
            }

            override fun positionChanged(mediaPlayer: MediaPlayer?, newPosition: Float) {
                Live.progressCallback.value = newPosition
                Live.statistics.value = mediaPlayer?.media()?.info()?.statistics()
                super.positionChanged(mediaPlayer, newPosition)
            }

            override fun muted(mediaPlayer: MediaPlayer?, muted: Boolean) {
                Live.isMutedCallback.value = muted
                super.muted(mediaPlayer, muted)
            }

            override fun volumeChanged(mediaPlayer: MediaPlayer?, volume: Float) {
                if (volume in 0f..1.2f) {
                    Live.volumeCallback.value = (volume * 100).toInt()
                }
                super.volumeChanged(mediaPlayer, volume)
            }

            override fun error(mediaPlayer: MediaPlayer?) {
                Live.currentMedia.value = null
                Live.showLargePlayer.value = false
                Live.progressCallback.value = 0f
                Live.playPauseCallback.value = false
                super.error(mediaPlayer)
            }
        })
        mediaPlayer.mediaPlayer().videoSurface().set(videoSurface)
        if (Pref.equalizerOn) {
            turnOnEqualizer(Pref.equalizerPreset)
        } else {
            turnOffEqualizer()
        }
    }

    fun startPlay(mediaItem: MediaItem, position: Float = 0f, playList: List<MediaItem>? = null) {
        Live.currentMedia.value = mediaItem
        Live.waveform.value = null
        if (playList != null) {
            this@Player.playList.clear()
            this@Player.playList.addAll(playList)
        }
        mediaPlayer.mediaPlayer().controls().stop()
        mediaPlayer.mediaPlayer().media().prepare(mediaItem.path)
        mediaPlayer.mediaPlayer().controls().play()
        mediaPlayer.mediaPlayer().controls().setPosition(position)
        if (!mediaItem.isVideo) {
            WaveformGenerator.generate(mediaItem.path, callback = {
                Live.waveform.value = it
            })
        } else {
            Live.waveform.value = null
        }
    }

    fun autoPlayPause() {
        if (Live.playPauseCallback.value) {
            mediaPlayer.mediaPlayer().controls().pause()
        } else {
            mediaPlayer.mediaPlayer().controls().play()
        }
    }

    fun next() {
        if (playList.isNotEmpty()) {
            val currentIndex = playList.indexOfFirst { it.path == Live.currentMedia.value?.path }
            if (currentIndex == -1 || currentIndex == (playList.size - 1)) {
                startPlay(playList[0])
            } else {
                startPlay(playList[currentIndex + 1])
            }
        }
    }

    private fun shuffle() {
        if (playList.isNotEmpty()) {
            val shuffleItem = playList.random()
            startPlay(shuffleItem)
        }
    }

    fun previous() {
        if (mediaPlayer.mediaPlayer().status().time() > 5000) {
            seekTo(0f)
        } else {
            if (playList.isNotEmpty()) {
                val currentIndex = playList.indexOfFirst { it.path == Live.currentMedia.value?.path }
                if (currentIndex == -1) {
                    startPlay(playList[0])
                } else {
                    if (currentIndex == 0) {
                        startPlay(playList.last())
                    } else {
                        startPlay(playList[currentIndex - 1])
                    }
                }
            }
        }
    }

    fun incVolume() {
        var currentVolume = mediaPlayer.mediaPlayer().audio().volume()
        currentVolume += 5
        if (currentVolume > 120) {
            mediaPlayer.mediaPlayer().audio().setVolume(120)
        } else {
            mediaPlayer.mediaPlayer().audio().setVolume(currentVolume)
        }
    }

    fun decVolume() {
        var currentVolume = mediaPlayer.mediaPlayer().audio().volume()
        currentVolume -= 5
        if (currentVolume < 0) {
            mediaPlayer.mediaPlayer().audio().setVolume(0)
        } else {
            mediaPlayer.mediaPlayer().audio().setVolume(currentVolume)
        }
    }

    fun changeSpeed(speed: Float) {
        mediaPlayer.mediaPlayer().controls().setRate(speed)
        Live.speedCallback.value = speed
    }

    fun changeVolume(volume: Int) {
        mediaPlayer.mediaPlayer().audio().setVolume(volume)
    }

    fun seekTo(pos: Float) {
        mediaPlayer.mediaPlayer().controls().setPosition(pos)
        Live.progressCallback.value = pos
    }

    fun forward(timeMillis: Long) {
        mediaPlayer.mediaPlayer().controls().skipTime(timeMillis)
    }

    fun backward(timeMillis: Long) {
        mediaPlayer.mediaPlayer().controls().skipTime(-timeMillis)
    }

    fun autoMute() {
        mediaPlayer.mediaPlayer().audio().mute()
    }

    fun brightness(brightness: Float) {
        mediaPlayer.mediaPlayer().video().isAdjustVideo = true
        mediaPlayer.mediaPlayer().video().setBrightness(brightness)
        Live.brightnessCallback.value = brightness
    }

    //equalizer
    fun turnOffEqualizer () {
        mediaPlayer.mediaPlayer().audio().setEqualizer(null)
        Pref.equalizerOn = false
        Live.equalizerOn.value = false
    }
    fun turnOnEqualizer (name : String) {
        val equalizer = mediaPlayer.mediaPlayerFactory().equalizer().newEqualizer(name)
        mediaPlayer.mediaPlayer().audio().setEqualizer(equalizer)
        Pref.equalizerOn = true
        Pref.equalizerPreset = name
        Live.equalizerOn.value = true
        Live.equalizerPreset.value = name
    }
    fun getEqualizerList () : Map<String,FloatArray> {
        return mediaPlayer.mediaPlayerFactory().equalizer().allPresetEqualizers().mapValues { it.value.amps() }
    }

    //snapshot
    fun takeSnapshot() {
        if (Live.currentMedia.value?.isVideo == true) {
            CoroutineScope(Dispatchers.IO).launch {
                val duration = (Live.progressCallback.value * (Live.currentMedia.value?.duration ?: 0)).toLong().formatToDuration()
                val snapshotFile = File("${Global.SNAPSHOT_PATH}${File.separator}${Live.currentMedia.value?.name}(${duration.replace(":","-")}).jpg")
                mediaPlayer.mediaPlayer().snapshots().save(snapshotFile)
            }
        }
    }

    //audio tracks
    fun getAudioTracks(): List<TrackDescription> = mediaPlayer.mediaPlayer().audio().trackDescriptions()
    fun getSelectedAudioTrack(): Int = mediaPlayer.mediaPlayer().audio().track()
    fun changeAudioTrack(id: Int) = mediaPlayer.mediaPlayer().audio().setTrack(id)

    //audio output devices
    fun getAudioDevices(): List<AudioDevice> = mediaPlayer.mediaPlayer().audio().outputDevices()
    fun getSelectedAudioDevice(): String? = mediaPlayer.mediaPlayer().audio().outputDevice()
    fun changeAudioDevice(id: String) = mediaPlayer.mediaPlayer().audio().setOutputDevice(null, id)

    //audio stereo mode
    fun getStereoModes(): List<AudioChannel> = listOf(
        AudioChannel.STEREO,
        AudioChannel.MONO,
        AudioChannel.RIGHT,
        AudioChannel.LEFT,
        AudioChannel.RSTEREO,
        AudioChannel.DOLBYS
    )
    fun getSelectedStereoMode(): AudioChannel = mediaPlayer.mediaPlayer().audio().channel()
    fun changeStereoMode(audioChannel: AudioChannel) = mediaPlayer.mediaPlayer().audio().setChannel(audioChannel)

    //video tracks
    fun getVideoTracks(): List<TrackDescription> = mediaPlayer.mediaPlayer().video().trackDescriptions()
    fun getSelectedVideoTrack(): Int = mediaPlayer.mediaPlayer().video().track()
    fun changeVideoTrack(id: Int) = mediaPlayer.mediaPlayer().video().setTrack(id)

    //aspect ratio
    fun changeAspectRatio(aspectRatio : AspectRatio) { Live.aspectRatio.value = aspectRatio }

    //subtitle
    fun getSubtitles(): List<TrackDescription> = mediaPlayer.mediaPlayer().subpictures().trackDescriptions()
    fun getSelectedSubtitle() : Int = mediaPlayer.mediaPlayer().subpictures().track()
    fun changeSubtitle(id : Int) = mediaPlayer.mediaPlayer().subpictures().setTrack(id)
    fun addSubtitleFile(file: File) : Boolean = mediaPlayer.mediaPlayer().subpictures().setSubTitleFile(file)

    //sync
    fun syncAudioToVideo(delay : Double) {
        val delayTime = (delay * 1000000).toLong()
        mediaPlayer.mediaPlayer().audio().setDelay(delayTime)
    }
    fun getSyncAudioToVideo() : Double {
        return (mediaPlayer.mediaPlayer().audio().delay() / 1000000.0)
    }

    fun syncSubtitleToVideo(delay : Double) {
        val delayTime = (delay * 1000000).toLong()
        mediaPlayer.mediaPlayer().subpictures().setDelay(delayTime)
    }
    fun getSyncSubtitleToVideo() : Double {
        return (mediaPlayer.mediaPlayer().subpictures().delay() / 1000000.0)
    }


    fun stop() {
        mediaPlayer.mediaPlayer().controls().pause()
        mediaPlayer.mediaPlayer().controls().stop()
        Live.currentMedia.value = null
        Live.showLargePlayer.value = false
        Live.progressCallback.value = 0f
        Live.playPauseCallback.value = false
    }

    fun release() {
        mediaPlayer.mediaPlayer().controls().pause()
        mediaPlayer.mediaPlayer().controls().stop()
        mediaPlayer.mediaPlayer().release()
    }

}