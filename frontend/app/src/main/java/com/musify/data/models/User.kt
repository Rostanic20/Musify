package com.musify.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("displayName")
    val displayName: String,
    
    @SerializedName("bio")
    val bio: String? = null,
    
    @SerializedName("profilePicture")
    val profilePicture: String? = null,
    
    @SerializedName("isPremium")
    val isPremium: Boolean = false,
    
    @SerializedName("isVerified")
    val isVerified: Boolean = false,
    
    @SerializedName("emailVerified")
    val emailVerified: Boolean = false,
    
    @SerializedName("twoFactorEnabled")
    val twoFactorEnabled: Boolean = false,
    
    @SerializedName("isArtist")
    val isArtist: Boolean = false,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String
) : Parcelable