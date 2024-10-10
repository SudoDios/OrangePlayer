package me.sudodios.orangeplayer.ui.dialogs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import me.sudodios.mediainfo.MediaInfoLib
import me.sudodios.orangeplayer.core.Native
import me.sudodios.orangeplayer.ui.components.BaseDialog
import me.sudodios.orangeplayer.ui.components.EActionRow
import me.sudodios.orangeplayer.ui.components.EButton
import me.sudodios.orangeplayer.ui.theme.ColorBox
import me.sudodios.orangeplayer.utils.Utils.formatToDurationInfo
import me.sudodios.orangeplayer.utils.Utils.formatToSizeFile
import me.sudodios.orangeplayer.utils.Utils.roundTo

@Composable
fun DialogRawMediaInfo(
    show : Boolean,
    path : String,
    onDismiss : () -> Unit,
) {

    BaseDialog(expanded = show, onDismissRequest = onDismiss) {

        val listInfo = remember { mutableStateListOf<Pair<String,String>>() }

        LaunchedEffect(Unit) {
            val pointer = Native.mi?.New()
            Native.mi?.Open(pointer,path)
            val duration = Native.mi?.Get(pointer,MediaInfoLib.StreamKind.General,0,"Duration")
            val format = Native.mi?.Get(pointer,MediaInfoLib.StreamKind.General,0,"Format")
            val size = Native.mi?.Get(pointer,MediaInfoLib.StreamKind.General,0,"FileSize")
            val title = Native.mi?.Get(pointer,MediaInfoLib.StreamKind.General,0,"Title")
            val artist = Native.mi?.Get(pointer,MediaInfoLib.StreamKind.General,0,"Artist")
            val album = Native.mi?.Get(pointer,MediaInfoLib.StreamKind.General,0,"Album")
            val framerate = Native.mi?.Get(pointer,MediaInfoLib.StreamKind.General,0,"FrameRate")
            val videoCodecId = Native.mi?.Get(pointer,MediaInfoLib.StreamKind.Video,0,"CodecID")
            val videoWidth = Native.mi?.Get(pointer,MediaInfoLib.StreamKind.Video,0,"Width")
            val videoHeight = Native.mi?.Get(pointer,MediaInfoLib.StreamKind.Video,0,"Height")
            val videoBitrate = Native.mi?.Get(pointer,MediaInfoLib.StreamKind.Video,0,"BitRate")
            val audioCodecId = Native.mi?.Get(pointer,MediaInfoLib.StreamKind.Audio,0,"CodecID")
            val audioBitrate = Native.mi?.Get(pointer,MediaInfoLib.StreamKind.Audio,0,"BitRate")
            val sampleRate = Native.mi?.Get(pointer,MediaInfoLib.StreamKind.Audio,0,"SamplingRate")
            val channels = Native.mi?.Get(pointer,MediaInfoLib.StreamKind.Audio,0,"Channel(s)")

            if (!duration.isNullOrBlank()) listInfo.add(Pair("Duration",duration.toLong().formatToDurationInfo()))
            if (!format.isNullOrBlank()) listInfo.add(Pair("Format",format))
            if (!size.isNullOrBlank()) listInfo.add(Pair("Size",size.toLong().formatToSizeFile()))
            if (!title.isNullOrBlank()) listInfo.add(Pair("Title",title))
            if (!artist.isNullOrBlank()) listInfo.add(Pair("Artist",artist))
            if (!album.isNullOrBlank()) listInfo.add(Pair("Album",album))
            if (!framerate.isNullOrBlank()) listInfo.add(Pair("Frame Rate","${framerate.toDouble().roundTo(2)} frames per second"))
            if (!videoCodecId.isNullOrBlank()) listInfo.add(Pair("Video Codec",videoCodecId))
            if (!videoWidth.isNullOrBlank()) listInfo.add(Pair("Video Dimension","$videoWidth x $videoHeight"))
            if (!videoBitrate.isNullOrBlank()) listInfo.add(Pair("Video Bitrate","${(videoBitrate.toInt() / 1024)} kbps"))
            if (!audioCodecId.isNullOrBlank()) listInfo.add(Pair("Audio Codec",audioCodecId))
            if (!audioBitrate.isNullOrBlank()) listInfo.add(Pair("Audio Bitrate","${(audioBitrate.toInt() / 1024)} kbps"))
            if (!sampleRate.isNullOrBlank()) {
                val formattedSampleRate = String.format("%.2f kHz", sampleRate.toDouble() / 1000.0)
                listInfo.add(Pair("Sample rate",formattedSampleRate))
            }
            if (!channels.isNullOrBlank()) listInfo.add(Pair("Channels",channels))

        }

        Box(modifier = Modifier.width(390.dp)) {
            LazyColumn(modifier = Modifier.fillMaxWidth().padding(bottom = 80.dp, start = 16.dp, end = 16.dp, top = 16.dp).clip(
                RoundedCornerShape(12.dp))) {
                items(listInfo) {
                    EActionRow(
                        modifier = Modifier.fillMaxWidth(),
                        title = it.first,
                        enabled = false,
                        value = it.second,
                        clip = RoundedCornerShape(0),
                        reverse = true
                    )
                }
            }
            EButton(
                modifier = Modifier.align(Alignment.BottomCenter).padding(start = 16.dp, end = 16.dp, bottom = 16.dp).fillMaxWidth(),
                text = "Done !",
                centerText = true,
                backgroundColor = ColorBox.primary.copy(0.1f),
                textColor = ColorBox.primary,
                onClick = {
                    onDismiss.invoke()
                }
            )
        }
    }

}