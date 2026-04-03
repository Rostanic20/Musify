package com.musify.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musify.domain.entity.Song
import com.musify.domain.repository.MusicRepository
import com.musify.player.MusicPlayerManager
import com.musify.player.SongInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SongDetailState(
    val isLoading: Boolean = false,
    val song: Song? = null,
    val isFavorite: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SongDetailViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val musicPlayerManager: MusicPlayerManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val songId: Int = savedStateHandle.get<Int>("songId") ?: -1

    private val _state = MutableStateFlow(SongDetailState())
    val state: StateFlow<SongDetailState> = _state.asStateFlow()

    init {
        loadSongDetails()
    }

    private fun loadSongDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            musicRepository.getSongDetails(songId)
                .onSuccess { detail ->
                    if (detail != null) {
                        _state.update { it.copy(song = detail.song, isFavorite = detail.isFavorite, isLoading = false) }
                    } else {
                        _state.update { it.copy(error = "Song not found", isLoading = false) }
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message ?: "Unknown error", isLoading = false) }
                }
        }
    }

    fun toggleFavorite() {
        val currentSong = _state.value.song ?: return
        viewModelScope.launch {
            musicRepository.toggleFavorite(currentSong.id)
                .onSuccess { isFavorite ->
                    _state.update { it.copy(isFavorite = isFavorite) }
                }
        }
    }

    fun playSong() {
        val currentSong = _state.value.song ?: return
        musicPlayerManager.play(currentSong.toSongInfo())
    }

    fun addToQueue() {
        val currentSong = _state.value.song ?: return
        musicPlayerManager.addToQueue(currentSong.toSongInfo())
    }

    private fun Song.toSongInfo() = SongInfo(
        id = id,
        title = title,
        artistName = artist.name,
        coverArt = coverArtUrl,
        streamUrl = null,
        duration = durationSeconds
    )

    fun retry() {
        loadSongDetails()
    }
}
