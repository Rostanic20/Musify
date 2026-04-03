package com.musify.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musify.domain.entity.Album
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

data class AlbumDetailState(
    val isLoading: Boolean = false,
    val album: Album? = null,
    val songs: List<Song> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
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
            musicRepository.getAlbumDetail(albumId)
                .onSuccess { detail ->
                    if (detail != null) {
                        _state.update { it.copy(album = detail.album, songs = detail.songs, isLoading = false) }
                    } else {
                        _state.update { it.copy(error = "Album not found", isLoading = false) }
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message ?: "Unknown error", isLoading = false) }
                }
        }
    }

    fun playAll() {
        val songs = _state.value.songs
        if (songs.isEmpty()) return
        musicPlayerManager.playQueue(songs.map { it.toSongInfo() }, 0)
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
        loadAlbumDetails()
    }
}
