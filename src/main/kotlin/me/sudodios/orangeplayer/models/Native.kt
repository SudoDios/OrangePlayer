package me.sudodios.orangeplayer.models

data class ModelScanResult(val fileName : String,val path : String,val folder : String,val hash : String,val extension : String,val size : Long)
data class ModelMediaRead(val mediaList : Array<MediaItem>, val count : Int, val duration : Long)
data class ModelFolderRead(val folder : String,val count : Int)
data class ModelPlaylistsRead(val id : Int,val title : String,val itemCount : Int,val coverArt : String)