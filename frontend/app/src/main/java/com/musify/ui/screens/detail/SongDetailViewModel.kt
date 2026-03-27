package com.musify.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musify.data.api.MusifyApiService
import com.musify.data.models.SongDetails
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
    val song: SongDetails? = null,
    val error: String? = null
)

@HiltViewModel
class SongDetailViewModel @Inject constructor(
    private val apiService: MusifyApiService,
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
            try {
                val response = apiService.getSongDetails(songId)
                if (response.isSuccessful) {
                    _state.update { it.copy(song = response.body(), isLoading = false) }
                } else {
                    _state.update { it.copy(error = "Failed to load song details", isLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Unknown error", isLoading = false) }
            }
        }
    }

    fun toggleFavorite() {
        val currentSong = _state.value.song ?: return
        viewModelScope.launch {
            try {
                val response = apiService.toggleFavorite(currentSong.song.id)
                if (response.isSuccessful) {
                    val isFavorite = response.body()?.get("isFavorite") ?: !currentSong.isFavorite
                    _state.update {
                        it.copy(song = currentSong.copy(isFavorite = isFavorite))
                    }
                }
            } catch (_: Exception) { }
        }
    }

    fun playSong() {
        val currentSong = _state.value.song ?: return
        val songInfo = currentSong.toSongInfo()
        musicPlayerManager.play(songInfo)
    }

    fun addToQueue() {
        val currentSong = _state.value.song ?: return
        val songInfo = currentSong.toSongInfo()
        musicPlayerManager.addToQueue(songInfo)
    }

    private fun SongDetails.toSongInfo() = SongInfo(
        id = song.id,
        title = song.title,
        artistName = artist.name,
        coverArt = song.coverArt,
        streamUrl = song.streamUrl,
        duration = song.duration
    )

    fun retry() {
        loadSongDetails()
    }
}
