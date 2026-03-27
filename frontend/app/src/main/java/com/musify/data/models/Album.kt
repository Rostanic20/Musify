package com.musify.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Album(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("artistId")
    val artistId: Int,
    
    @SerializedName("artistName")
    val artistName: String,
    
    @SerializedName("coverArt")
    val coverArt: String? = null,
    
    @SerializedName("releaseDate")
    val releaseDate: String,
    
    @SerializedName("trackCount")
    val trackCount: Int = 0,
    
    @SerializedName("totalDuration")
    val totalDuration: Int = 0, // in seconds
    
    @SerializedName("genre")
    val genre: String? = null,
    
    @SerializedName("label")
    val label: String? = null,
    
    @SerializedName("type")
    val type: AlbumType = AlbumType.ALBUM
) : Parcelable {
    
    val releaseYear: String
        get() = releaseDate.take(4)
    
    val formattedDuration: String
        get() {
            val minutes = totalDuration / 60
            return "$minutes min"
        }
}

enum class AlbumType {
    @SerializedName("album")
    ALBUM,
    
    @SerializedName("single")
    SINGLE,
    
    @SerializedName("ep")
    EP,
    
    @SerializedName("compilation")
    COMPILATION
}