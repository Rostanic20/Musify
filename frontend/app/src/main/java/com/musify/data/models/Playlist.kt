package com.musify.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Playlist(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("userId")
    val userId: Int,
    
    @SerializedName("ownerName")
    val ownerName: String? = null,
    
    @SerializedName("isPublic")
    val isPublic: Boolean = true,
    
    @SerializedName("isCollaborative")
    val isCollaborative: Boolean = false,
    
    @SerializedName("coverImage")
    val coverImage: String? = null,
    
    @SerializedName("songCount")
    val songCount: Int = 0,
    
    @SerializedName("totalDuration")
    val totalDuration: Int = 0, // in seconds
    
    @SerializedName("followersCount")
    val followersCount: Int = 0,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String
) : Parcelable {
    
    val formattedDuration: String
        get() {
            val hours = totalDuration / 3600
            val minutes = (totalDuration % 3600) / 60
            return if (hours > 0) {
                String.format("%d hr %d min", hours, minutes)
            } else {
                String.format("%d min", minutes)
            }
        }
}

data class CreatePlaylistRequest(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("isPublic")
    val isPublic: Boolean = true
)

