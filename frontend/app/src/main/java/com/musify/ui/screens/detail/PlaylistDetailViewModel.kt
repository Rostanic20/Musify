package com.musify.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musify.data.api.MusifyApiService
import com.musify.data.models.Playlist
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

data class PlaylistDetailState(
    val isLoading: Boolean = false,
    val playlist: Playlist? = null,
    val songs: List<Song> = emptyList(),
    val currentUserId: Int? = null,
    val error: String? = null
)

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val apiService: MusifyApiService,
    private val musicPlayerManager: MusicPlayerManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val playlistId: Int = savedStateHandle.get<Int>("playlistId") ?: -1

    private val _state = MutableStateFlow(PlaylistDetailState())
    val state: StateFlow<PlaylistDetailState> = _state.asStateFlow()

    init {
        loadPlaylistDetails()
    }

    private fun loadPlaylistDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val userResponse = apiService.getCurrentUser()
                val currentUserId = if (userResponse.isSuccessful) userResponse.body()?.id else null

                val response = apiService.getPlaylistDetails(playlistId)
                if (response.isSuccessful) {
                    val details = response.body()
                    _state.update {
                        it.copy(
                            playlist = details?.playlist,
                            songs = details?.songs ?: emptyList(),
                            currentUserId = currentUserId,
                            isLoading = false
                        )
                    }
                } else {
                    _state.update { it.copy(error = "Failed to load playlist", isLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Unknown error", isLoading = false) }
            }
        }
    }

    fun removeSong(songId: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.removeSongFromPlaylist(playlistId, songId)
                if (response.isSuccessful) {
                    _state.update { current ->
                        current.copy(
                            songs = current.songs.filter { it.id != songId },
                            playlist = current.playlist?.copy(
                                songCount = (current.playlist.songCount - 1).coerceAtLeast(0)
                            )
                        )
                    }
                }
            } catch (_: Exception) { }
        }
    }

    fun playAll() {
        val songs = _state.value.songs
        if (songs.isEmpty()) return
        musicPlayerManager.playQueue(songs.map { it.toSongInfo() }, 0)
    }

    fun shuffle() {
        val songs = _state.value.songs
        if (songs.isEmpty()) return
        val shuffled = songs.map { it.toSongInfo() }.shuffled()
        musicPlayerManager.playQueue(shuffled, 0)
    }

    private fun Song.toSongInfo() = SongInfo(
        id = id,
        title = title,
        artistName = artistName,
        coverArt = coverArt,
        streamUrl = streamUrl,
        duration = duration
    )

    val isOwner: Boolean
        get() {
            val state = _state.value
            return state.currentUserId != null && state.playlist?.userId == state.currentUserId
        }

    fun retry() {
        loadPlaylistDetails()
    }
}
