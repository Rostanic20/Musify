package com.musify.domain.entity

/**
 * Domain entity for Song
 */
data class Song(
    val id: Int,
    val title: String,
    val artist: Artist,
    val album: Album?,
    val durationSeconds: Int,
    val coverArtUrl: String?,
    val genre: String?,
    val playCount: Long,
    val isFavorite: Boolean = false
) {
    val formattedDuration: String
        get() {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return String.format("%d:%02d", minutes, seconds)
        }
}

data class Artist(
    val id: Int,
    val name: String,
    val bio: String?,
    val profileImageUrl: String?,
    val isVerified: Boolean,
    val monthlyListeners: Long,
    val followersCount: Long
)

data class Album(
    val id: Int,
    val title: String,
    val artist: Artist,
    val coverArtUrl: String?,
    val releaseYear: Int,
    val trackCount: Int,
    val genre: String?
)

data class Playlist(
    val id: Int,
    val name: String,
    val description: String?,
    val ownerId: Int,
    val ownerName: String,
    val isPublic: Boolean,
    val isCollaborative: Boolean,
    val coverImageUrl: String?,
    val songCount: Int,
    val totalDurationSeconds: Int,
    val followersCount: Int
)