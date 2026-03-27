package com.musify.domain.entity

/**
 * Domain entity for User
 * This is independent of any data layer or framework
 */
data class User(
    val id: Int,
    val email: String,
    val username: String,
    val displayName: String,
    val bio: String?,
    val profilePictureUrl: String?,
    val isPremium: Boolean,
    val isVerified: Boolean,
    val isEmailVerified: Boolean,
    val has2FAEnabled: Boolean,
    val isArtist: Boolean = false
)

data class AuthResult(
    val user: User,
    val accessToken: String,
    val refreshToken: String?,
    val requires2FA: Boolean = false
)

/**
 * Domain entity representing a user profile with additional info
 */
data class UserProfile(
    val user: User,
    val followersCount: Int,
    val followingCount: Int,
    val playlistCount: Int,
    val isFollowing: Boolean
)