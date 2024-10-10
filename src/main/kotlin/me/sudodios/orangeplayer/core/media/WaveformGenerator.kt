package me.sudodios.orangeplayer.core.media

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.sudodios.orangeplayer.Global
import me.sudodios.orangeplayer.core.Native
import me.sudodios.orangeplayer.utils.Utils
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import java.io.File
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import kotlin.math.abs

object WaveformGenerator {

    private const val BUFFER_SIZE = 4096
    private lateinit var threadScope: Job


    private fun FloatArray.toStr() : String {
        var stringArray = this.contentToString()
        stringArray = stringArray.substring(1, stringArray.length - 1)
        stringArray = stringArray.replace(" ","")
        return stringArray
    }
    private fun String.toFloatArray(): FloatArray {
        val parts = split(",")
        val numbers = FloatArray(parts.size)
        for (i in parts.indices) {
            val number = parts[i].toFloat()
            numbers[i] = number
        }
        return numbers
    }

    fun generate (path : String,callback : (FloatArray?) -> Unit) {
        if (this::threadScope.isInitialized && threadScope.isActive) {
            threadScope.cancel()
        }
        threadScope = CoroutineScope(Dispatchers.IO).launch {
            val saveWaveform = Native.dbGetMediaWaveform(path)
            if (saveWaveform.isEmpty()) {
                val outputWav = "${Global.LIB_CORE_PATH}${File.separator}output.wav"
                val mediaPlayerFactory = MediaPlayerFactory("--verbose=-1")
                val mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer()
                val opts = "sout=#transcode{acodec=s16l,channels=2,samplerate=44100}:std{access=file,mux=wav,dst=$outputWav}"
                mediaPlayer.media().prepare(path, opts)
                mediaPlayer.events().addMediaPlayerEventListener(object : MediaPlayerEventAdapter() {
                    override fun finished(mediaPlayer: MediaPlayer) {
                        Utils.postDelayed(1000) {
                            mediaPlayer.controls().stop()
                            mediaPlayer.release()
                            generateWaveform(outputWav) {
                                Native.dbSaveMediaWaveform(path, waveData = it.toStr())
                                callback.invoke(it)
                            }
                        }
                    }
                    override fun error(mediaPlayer: MediaPlayer?) {
                        callback.invoke(null)
                        super.error(mediaPlayer)
                    }
                })
                mediaPlayer.controls().start()
                mediaPlayer.audio().mute()
            } else {
                callback.invoke(saveWaveform.toFloatArray())
            }
        }
    }

    private fun generateWaveform (file: String,callback: (FloatArray) -> Unit) {
        try {
            val samples = getWavSamples(File(file))
            val amps = processAmplitudes(samples)
            callback.invoke(amps)
        } catch (_ : Exception) { }
    }

    private fun getWavSamples(file: File): IntArray {
        try {
            AudioSystem.getAudioInputStream(file).use { input ->

                val baseFormat = input.format

                val encoding = AudioFormat.Encoding.PCM_UNSIGNED
                val sampleRate = baseFormat.sampleRate
                val numChannels = baseFormat.channels
                val decodedFormat = AudioFormat(encoding, sampleRate, 16, numChannels, numChannels * 2, sampleRate, false)
                val available = input.available()

                try {
                    AudioSystem.getAudioInputStream(decodedFormat, input).use { pcmDecodedInput ->
                        val buffer = ByteArray(BUFFER_SIZE)

                        val maximumArrayLength = 100000
                        val finalAmplitudes = IntArray(maximumArrayLength)
                        val samplesPerPixel = available / maximumArrayLength

                        var currentSampleCounter = 0
                        var arrayCellPosition = 0
                        var currentCellValue = 0.0f

                        var arrayCellValue: Int

                        while (pcmDecodedInput.readNBytes(buffer, 0, BUFFER_SIZE) > 0) {
                            var i = 0
                            while (i < buffer.size - 1) {
                                arrayCellValue = ((buffer[i + 1].toInt() shl 8 or (buffer[i]
                                    .toInt() and 0xff) shl 16) / 32767 * 1.3).toInt()
                                if (currentSampleCounter != samplesPerPixel) {
                                    ++currentSampleCounter
                                    currentCellValue += abs(arrayCellValue)
                                } else {
                                    if (arrayCellPosition != maximumArrayLength) {
                                        finalAmplitudes[arrayCellPosition + 1] = currentCellValue.toInt() / samplesPerPixel
                                        finalAmplitudes[arrayCellPosition] = finalAmplitudes[arrayCellPosition + 1]
                                    }
                                    currentSampleCounter = 0
                                    currentCellValue = 0f
                                    arrayCellPosition += 2
                                }
                                i += 2
                            }
                        }
                        return finalAmplitudes
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return IntArray(1)
    }

    private fun processAmplitudes(sourcePcmData: IntArray): FloatArray {
        val size = 60
        val waveData = FloatArray(size)
        val samplesPerPixel = sourcePcmData.size / size
        var nValue: Float
        for (w in 0 until size) {
            val c = w * samplesPerPixel
            nValue = 0.0f
            for (s in 0 until samplesPerPixel) {
                nValue += abs(sourcePcmData[c + s].toFloat()) / 65536.0f
            }
            waveData[w] = nValue / samplesPerPixel
        }
        return waveData
    }

}