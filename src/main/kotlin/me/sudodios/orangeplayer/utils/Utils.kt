package me.sudodios.orangeplayer.utils

import androidx.compose.ui.awt.ComposeDialog
import me.sudodios.orangeplayer.Global
import me.sudodios.orangeplayer.core.Platform
import java.awt.FileDialog
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.*
import javax.swing.SwingUtilities
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

object Utils {

    fun File.openLocation() {
        when {
            Platform.isWin() -> {
                Runtime.getRuntime().exec(arrayOf("explorer.exe", "/select,",this.absolutePath))
            }
            Platform.isUnix() -> {
                Runtime.getRuntime().exec(arrayOf("nautilus",this.absolutePath))
            }
        }
    }

    fun getOvershotInterpolator (tension : Float = 2f,t : Float) : Float {
        var s = t
        s -= 1.0f
        return s * s * ((tension + 1) * s + tension) + 1.0f
    }

    fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
    }

    fun Long.formatToDuration(): String {
        val mFormatBuilder: StringBuilder = StringBuilder()
        val mFormatter = Formatter(mFormatBuilder, Locale.ENGLISH)
        val totalSeconds = this / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        mFormatBuilder.setLength(0)
        return if (hours > 0) {
            mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            mFormatter.format("%02d:%02d", minutes, seconds).toString()
        }
    }

    fun Long.formatToDurationInfo(): String {
        val totalSeconds = this / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        val strBuilder = StringBuilder()
        if (hours > 0) if (hours == 1L) strBuilder.append("$hours Hour ") else strBuilder.append("$hours Hours ")
        if (minutes > 0) if (minutes == 1L) strBuilder.append("$minutes Minute ") else strBuilder.append("$minutes Minutes ")
        if (seconds > 0) if (seconds == 1L) strBuilder.append("$seconds Second ") else strBuilder.append("$seconds Seconds ")
        return strBuilder.toString()
    }

    fun Long.formatToSizeFile(): String {
        val symbols = DecimalFormatSymbols(Locale.ENGLISH)
        if (this <= 0) return "0"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(this.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#", symbols).format(
            this / 1024.0.pow(digitGroups.toDouble())
        ) + " " + units[digitGroups]
    }

    fun Long.formatToCount(): String {
        val fmt = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT)
        return fmt.format(this)
    }

    fun Double.roundTo(numFractionDigits: Int): Double {
        val factor = 10.0.pow(numFractionDigits.toDouble())
        return (this * factor).roundToInt() / factor
    }

    fun Float.roundTo(numFractionDigits: Int): Float {
        val factor = 10.0f.pow(numFractionDigits.toFloat())
        return (this * factor).roundToInt() / factor
    }

    fun postDelayed (delay : Long,callback : () -> Unit) {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                SwingUtilities.invokeLater {
                    callback.invoke()
                }
            }
        },delay)
    }

    fun writeThumbImage(array: ByteArray,dst : String) : String {
        val fileSave = File(dst)
        fileSave.createNewFile()
        fileSave.writeBytes(array)
        return fileSave.absolutePath
    }

    fun openFilePicker (title : String = "Choice an image",vararg filterFormats: String) : File? {
        val fileGet = FileDialog(ComposeDialog(),title, FileDialog.LOAD)
        fileGet.directory = Global.userHome
        fileGet.setFilenameFilter { _, name -> filterFormats.contains(name.substringAfterLast(".")) }
        fileGet.title = title
        fileGet.isVisible = true
        fileGet.isMultipleMode = false
        return if (fileGet.files.isEmpty()) {
            null
        } else {
            fileGet.files[0]
        }
    }

}