package com.musify.data.models

import com.google.gson.annotations.SerializedName

// Request models
data class LoginRequest(
    @SerializedName("username")
    val username: String,
    
    @SerializedName("password")
    val password: String,
    
    @SerializedName("totpCode")
    val totpCode: String? = null
)

data class RegisterRequest(
    @SerializedName("email")
    val email: String?,

    @SerializedName("phoneNumber")
    val phoneNumber: String?,

    @SerializedName("username")
    val username: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("displayName")
    val displayName: String,

    @SerializedName("isArtist")
    val isArtist: Boolean = false,

    @SerializedName("verificationType")
    val verificationType: String = "email"
)

data class RefreshTokenRequest(
    @SerializedName("refreshToken")
    val refreshToken: String
)

// Response models
data class AuthResponse(
    @SerializedName("token")
    val token: String,
    
    @SerializedName("refreshToken")
    val refreshToken: String? = null,
    
    @SerializedName("user")
    val user: User,
    
    @SerializedName("expiresIn")
    val expiresIn: Long,
    
    @SerializedName("requires2FA")
    val requires2FA: Boolean = false,
    
    @SerializedName("message")
    val message: String? = null
)

data class MessageResponse(
    @SerializedName("message")
    val message: String
)

