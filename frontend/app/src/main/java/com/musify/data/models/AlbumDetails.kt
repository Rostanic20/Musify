package com.musify.data.models

/**
 * Response model for album details endpoint
 */
data class AlbumDetails(
    val album: Album,
    val songs: List<Song>
)