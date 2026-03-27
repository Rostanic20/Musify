package com.musify.player

data class PlayerState(
    val currentSong: SongInfo? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val queue: List<SongInfo> = emptyList(),
    val currentIndex: Int = -1,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF
)

data class SongInfo(
    val id: Int,
    val title: String,
    val artistName: String,
    val coverArt: String? = null,
    val streamUrl: String? = null,
    val duration: Int = 0
)

enum class RepeatMode { OFF, ONE, ALL }
