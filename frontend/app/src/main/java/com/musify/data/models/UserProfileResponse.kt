package com.musify.data.models

/**
 * Response model for user profile endpoint
 */
data class UserProfileResponse(
    val user: User,
    val followersCount: Int,
    val followingCount: Int,
    val playlistCount: Int,
    val isFollowing: Boolean
)