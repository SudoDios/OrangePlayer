package me.sudodios.orangeplayer.core

object Platform {

    fun isUnix () : Boolean {
        val os = System.getProperty("os.name").lowercase()
        return os.contains("nix") || os.contains("nux") || os.contains("aix")
    }

    fun isWin () : Boolean {
        val os = System.getProperty("os.name").lowercase()
        return os.contains("win")
    }

    fun isSupported() : Boolean {
        return isUnix() || isWin()
    }

    fun getResFiles () : List<String>? {
        val resFiles = mutableListOf<String>()
        when {
            isUnix() -> {
                resFiles += "liborange_player.so"
                resFiles += "mi-linux-64.tar.xz"
                resFiles += "vlc-linux-64.tar.xz"
            }
            isWin() -> {
                resFiles += "orange_player.dll"
                resFiles += "mi-win-64.tar.xz"
                resFiles += "vlc-win-64.tar.xz"
            }
            else -> {
                return null
            }
        }
        return resFiles
    }

    fun getCoreLibName () : String {
        return when {
            isUnix() -> "liborange_player.so"
            isWin() -> "orange_player.dll"
            else -> ""
        }
    }

    fun getVlcLibName () : String {
        return when {
            isUnix() -> "vlc-linux-64"
            isWin() -> "vlc-win-64"
            else -> ""
        }
    }

    fun getMiLibName () : String {
        return when {
            isUnix() -> "mi-linux-64"
            isWin() -> "mi-win-64"
            else -> ""
        }
    }

}