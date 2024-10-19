package me.sudodios.orangeplayer

import it.sauronsoftware.junique.AlreadyLockedException
import it.sauronsoftware.junique.JUnique
import java.util.*

private const val APP_ID = "me.sudodios.orangeplayer"
private class ArgFileHandler(var onResult: (List<String>) -> Unit) {
    private var timer = Timer()
    private var listFiles = mutableListOf<String>()
    fun addFile(path : String) {
        listFiles.add(path)
        try {
            timer.cancel()
            timer = Timer()
        } catch (_ : Exception) {}
        timer.schedule(object : TimerTask() {
            override fun run() {
                onResult.invoke(listFiles)
                listFiles.clear()
            }
        },150)
    }
}

class Application(var onRecFiles : (List<String>) -> Unit,var onInitWindow : () -> Unit) {
    private var argFileHandler = ArgFileHandler(onResult = onRecFiles)
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
            if (args.isNotEmpty()) argFileHandler.addFile(args[0])
            onInitWindow.invoke()
        } else {
            JUnique.sendMessage(APP_ID,args[0])
        }
    }
}