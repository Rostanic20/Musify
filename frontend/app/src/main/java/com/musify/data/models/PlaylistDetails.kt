package com.musify.data.models

/**
 * Response model for playlist details endpoint
 */
data class PlaylistDetails(
    val playlist: Playlist,
    val songs: List<Song>
)