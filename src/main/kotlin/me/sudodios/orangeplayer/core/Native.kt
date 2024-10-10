package me.sudodios.orangeplayer.core

import com.sun.jna.NativeLibrary
import me.sudodios.mediainfo.MediaInfoLib
import me.sudodios.orangeplayer.Global
import me.sudodios.orangeplayer.models.*
import me.sudodios.orangeplayer.utils.Pref
import uk.co.caprica.vlcj.binding.support.runtime.RuntimeUtil
import java.io.File
import java.nio.ByteBuffer

object Native {

    /*
    * Native methods
    * */

    external fun version() : String
    external fun searchMedia(folders : Array<String>) : Array<ModelScanResult>
    private external fun extractArchive(sourceDir : String,destDir : String)

    external fun ffmpegMakeVidThumbnail(vidPath : String,thumbnailPath : String) : Boolean

    /*
    * Database
    * */

    external fun initDatabase(dbPath : String)
    external fun dbInsertItems(mediaItems : Array<MediaItem>)
    external fun dbAddMediaToFav(path : String)
    external fun dbRemoveMediaFromFav(path : String)
    external fun dbReset()
    external fun dbDeleteItemByPath(path : String)
    external fun dbReadMediaItems(rawQuery : String) : ModelMediaRead
    external fun dbCountMediaItems() : Int
    external fun dbReadFolders() : Array<ModelFolderRead>

    //playlists
    external fun dbPlaylistsCreate(title : String) : Int
    external fun dbPlaylistsDelete(id : Int)
    external fun dbPlaylistsUpdate(id : Int, title : String)
    external fun dbPlaylistsRead() : Array<ModelPlaylistsRead>
    external fun dbPlaylistsReadItems(pId : Int) : ModelMediaRead
    external fun dbPlaylistsAddMediaItem(pId : Int,mPath : String)
    external fun dbPlaylistsDelMediaItem(pId : Int,mPath : String)
    external fun dbMediaPlaylists(mPath: String) : IntArray

    //waveform
    external fun dbSaveMediaWaveform(mHash : String,waveData : String)
    external fun dbGetMediaWaveform(mHash: String) : String

    external fun getBufferAddr(buffer : ByteBuffer) : Long
    external fun fastFileMD5(path: String) : String

    fun init(status : (String) -> Unit,callback : (isOK : Boolean) -> Unit) {
        File(Global.LIB_CORE_PATH).mkdirs()
        File(Global.COVER_PATH).mkdirs()
        if (!Pref.initializedLibs) {
            if (!Platform.isSupported()) {
                callback.invoke(false)
            } else {
                status.invoke("Copy resources ...")
                copyResources()
                status.invoke("Extract resources ...")
                initNativeLib()
                extractVLCLib()
                extractMILib()
                status.invoke("Init natives ...")
                Pref.initializedLibs = true
                initDatabase(Global.DB_PATH)
                callback.invoke(true)
            }
        } else {
            status.invoke("Copy resources ...")
            copyResources()
            status.invoke("Init natives ...")
            initNativeLib()
            initVLCLib()
            initMILib()
            Pref.initializedLibs = true
            initDatabase(Global.DB_PATH)
            callback(true)
        }
    }

    private fun copyResources () {
        Platform.getResFiles()?.forEach {
            val res = javaClass.classLoader.getResourceAsStream(it)!!
            File("${Global.LIB_CORE_PATH}/$it").writeBytes(res.readAllBytes())
        }
    }

    private fun initNativeLib() {
        val nativeLibName = Platform.getCoreLibName()
        System.load("${Global.LIB_CORE_PATH}/$nativeLibName")
    }

    private fun extractVLCLib () {
        val archiveName = "${Global.LIB_CORE_PATH}/${Platform.getVlcLibName()}.tar.xz"
        val libFolder = "${Global.LIB_CORE_PATH}/${Platform.getVlcLibName()}"
        extractArchive(archiveName,libFolder)
        File(archiveName).delete()
        initVLCLib()
    }
    private fun initVLCLib() {
        val libFolder = "${Global.LIB_CORE_PATH}/${Platform.getVlcLibName()}"
        NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(),libFolder)
    }

    var mi : MediaInfoLib? = null
    private fun extractMILib () {
        val archiveName = "${Global.LIB_CORE_PATH}/${Platform.getMiLibName()}.tar.xz"
        val libFolder = "${Global.LIB_CORE_PATH}/${Platform.getMiLibName()}"
        extractArchive(archiveName,libFolder)
        File(archiveName).delete()
        initMILib()
    }
    private fun initMILib() {
        val libFolder = "${Global.LIB_CORE_PATH}/${Platform.getMiLibName()}"
        try {
            mi = if (Platform.isWin()) {
                MediaInfoLib(
                    "$libFolder/MediaInfo.dll",
                    null,
                    null
                )
            } else if (Platform.isUnix()) {
                MediaInfoLib(
                    "$libFolder/libmediainfo.so",
                    "$libFolder/libzen.so",
                    "$libFolder/libmms.so"
                )
            } else {
                null
            }
        } catch (e : UnsatisfiedLinkError) {
            Pref.initializedLibs = false
        }
    }

}