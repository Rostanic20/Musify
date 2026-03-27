package com.musify.ui.screens.player

import androidx.lifecycle.ViewModel
import com.musify.player.MusicPlayerManager
import com.musify.player.PlayerState
import com.musify.player.SongInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class QueueViewModel @Inject constructor(
    private val musicPlayerManager: MusicPlayerManager
) : ViewModel() {

    val playerState: StateFlow<PlayerState> = musicPlayerManager.playerState

    fun playSongAt(index: Int) {
        val queue = playerState.value.queue
        if (index in queue.indices) {
            musicPlayerManager.playQueue(queue, index)
        }
    }

    fun clearQueue() = musicPlayerManager.clearQueue()
}
