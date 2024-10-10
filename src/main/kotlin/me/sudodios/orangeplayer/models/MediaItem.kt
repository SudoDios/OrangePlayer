package me.sudodios.orangeplayer.models

data class MediaItem(
    var id : Int = -1,
    var name : String = "",
    var album : String = "",
    var artist: String = "",
    var path : String = "",
    var folder : String = "",
    var hash : String = "",
    var coverPath : String = "",
    var width : Int = 0,
    var height : Int = 0,
    var isFav : Boolean = false,
    var isVideo : Boolean = false,
    var duration : Long = 0,
    var size : Long = 0,
    var extension : String = ""
)