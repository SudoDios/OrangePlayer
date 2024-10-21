package me.sudodios.orangeplayer

import it.sauronsoftware.junique.AlreadyLockedException
import it.sauronsoftware.junique.JUnique
import java.util.*
import kotlin.system.exitProcess

private const val APP_ID = "me.sudodios.orangeplayer"
private class ArgFileHandler(var onResult: (List<String>) -> Unit) {
    private var timer = Timer()
    private var listFiles = mutableListOf<String>()
    fun addFile(path : String) {
        listFiles.add(path)
        try {
            timer.cancel()
            timer.purge()
            timer = Timer()
        } catch (_ : Exception) {} finally {
            timer.schedule(object : TimerTask() {
                override fun run() {
                    onResult.invoke(listFiles)
                    listFiles.clear()
                }
            },600)
        }
    }
}

class Application(var onRecFiles : (List<String>) -> Unit,var onInitWindow : () -> Unit) {
    private lateinit var argFileHandler: ArgFileHandler
    fun run(args : Array<String>) {
        var alreadyRunning: Boolean
        try {
            JUnique.acquireLock(APP_ID) {
                argFileHandler.addFile(it)
                null
            }
            alreadyRunning = false
        } catch (e : AlreadyLockedException) {
            alreadyRunning = true
        }
        if (!alreadyRunning) {
            argFileHandler = ArgFileHandler(onRecFiles)
            if (args.isNotEmpty()) argFileHandler.addFile(args[0])
            onInitWindow()
        } else {
            JUnique.sendMessage(APP_ID,args[0])
            exitProcess(0)
        }
    }
}