package com.musify.ui.screens.player

import androidx.lifecycle.ViewModel
import com.musify.player.MusicPlayerManager
import com.musify.player.PlayerState
import com.musify.player.SongInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val musicPlayerManager: MusicPlayerManager
) : ViewModel() {

    val playerState: StateFlow<PlayerState> = musicPlayerManager.playerState

    fun play(song: SongInfo) = musicPlayerManager.play(song)

    fun pause() = musicPlayerManager.pause()

    fun resume() = musicPlayerManager.resume()

    fun togglePlayPause() {
        if (playerState.value.isPlaying) pause() else resume()
    }

    fun next() = musicPlayerManager.next()

    fun previous() = musicPlayerManager.previous()

    fun seekTo(position: Long) = musicPlayerManager.seekTo(position)

    fun toggleShuffle() = musicPlayerManager.toggleShuffle()

    fun cycleRepeatMode() = musicPlayerManager.cycleRepeatMode()

    fun playQueue(songs: List<SongInfo>, startIndex: Int = 0) =
        musicPlayerManager.playQueue(songs, startIndex)

    fun addToQueue(song: SongInfo) = musicPlayerManager.addToQueue(song)
}
