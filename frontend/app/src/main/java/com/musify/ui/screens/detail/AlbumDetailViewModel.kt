package com.musify.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musify.data.api.MusifyApiService
import com.musify.data.models.AlbumDetails
import com.musify.data.models.Song
import com.musify.player.MusicPlayerManager
import com.musify.player.SongInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlbumDetailState(
    val isLoading: Boolean = false,
    val albumDetails: AlbumDetails? = null,
    val error: String? = null
)

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    private val apiService: MusifyApiService,
    private val musicPlayerManager: MusicPlayerManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val albumId: Int = savedStateHandle.get<Int>("albumId") ?: -1

    private val _state = MutableStateFlow(AlbumDetailState())
    val state: StateFlow<AlbumDetailState> = _state.asStateFlow()

    init {
        loadAlbumDetails()
    }

    private fun loadAlbumDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.getAlbumDetails(albumId)
                if (response.isSuccessful) {
                    _state.update { it.copy(albumDetails = response.body(), isLoading = false) }
                } else {
                    _state.update { it.copy(error = "Failed to load album details", isLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Unknown error", isLoading = false) }
            }
        }
    }

    fun playAll() {
        val songs = _state.value.albumDetails?.songs ?: return
        if (songs.isEmpty()) return
        musicPlayerManager.playQueue(songs.map { it.toSongInfo() }, 0)
    }

    private fun Song.toSongInfo() = SongInfo(
        id = id,
        title = title,
        artistName = artistName,
        coverArt = coverArt,
        streamUrl = streamUrl,
        duration = duration
    )

    fun retry() {
        loadAlbumDetails()
    }
}
