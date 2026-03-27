package com.musify.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("artistId")
    val artistId: Int,
    
    @SerializedName("artistName")
    val artistName: String,
    
    @SerializedName("albumId")
    val albumId: Int? = null,
    
    @SerializedName("albumTitle")
    val albumTitle: String? = null,
    
    @SerializedName("duration")
    val duration: Int, // in seconds
    
    @SerializedName("filePath")
    val filePath: String,
    
    @SerializedName("streamUrl")
    val streamUrl: String? = null,
    
    @SerializedName("coverArt")
    val coverArt: String? = null,
    
    @SerializedName("genre")
    val genre: String? = null,
    
    @SerializedName("playCount")
    val playCount: Long = 0,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    // Audio features for recommendations
    @SerializedName("energy")
    val energy: Double = 0.5,
    
    @SerializedName("valence")
    val valence: Double = 0.5,
    
    @SerializedName("danceability")
    val danceability: Double = 0.5,
    
    @SerializedName("acousticness")
    val acousticness: Double = 0.5,
    
    @SerializedName("instrumentalness")
    val instrumentalness: Double = 0.0,
    
    @SerializedName("tempo")
    val tempo: Double = 120.0,
    
    @SerializedName("loudness")
    val loudness: Double = -10.0,
    
    @SerializedName("popularity")
    val popularity: Double = 0.5
) : Parcelable {
    
    val formattedDuration: String
        get() {
            val minutes = duration / 60
            val seconds = duration % 60
            return String.format("%d:%02d", minutes, seconds)
        }
}

@Parcelize
data class SongDetails(
    @SerializedName("song")
    val song: Song,
    
    @SerializedName("artist")
    val artist: Artist,
    
    @SerializedName("album")
    val album: Album? = null,
    
    @SerializedName("isFavorite")
    val isFavorite: Boolean = false
) : Parcelable