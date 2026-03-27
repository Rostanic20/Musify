package com.musify.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Artist(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("bio")
    val bio: String? = null,
    
    @SerializedName("profileImage")
    val profileImage: String? = null,
    
    @SerializedName("coverImage")
    val coverImage: String? = null,
    
    @SerializedName("genres")
    val genres: List<String> = emptyList(),
    
    @SerializedName("monthlyListeners")
    val monthlyListeners: Long = 0,
    
    @SerializedName("followersCount")
    val followersCount: Long = 0,
    
    @SerializedName("isVerified")
    val isVerified: Boolean = false,
    
    @SerializedName("createdAt")
    val createdAt: String
) : Parcelable {
    
    val formattedMonthlyListeners: String
        get() = when {
            monthlyListeners >= 1_000_000 -> String.format("%.1fM", monthlyListeners / 1_000_000.0)
            monthlyListeners >= 1_000 -> String.format("%.1fK", monthlyListeners / 1_000.0)
            else -> monthlyListeners.toString()
        }
}