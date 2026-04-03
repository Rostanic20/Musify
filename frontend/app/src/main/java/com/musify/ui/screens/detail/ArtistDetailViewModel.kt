package com.musify.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musify.data.models.Album
import com.musify.data.models.Artist
import com.musify.data.models.Song
import com.musify.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArtistDetailState(
    val isLoading: Boolean = false,
    val artist: Artist? = null,
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val isFollowing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val artistId: Int = savedStateHandle.get<Int>("artistId") ?: -1

    private val _state = MutableStateFlow(ArtistDetailState())
    val state: StateFlow<ArtistDetailState> = _state.asStateFlow()

    init {
        loadArtistDetails()
    }

    private fun loadArtistDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                coroutineScope {
                    val artistDeferred = async { musicRepository.getArtistDetailsRaw(artistId) }
                    val songsDeferred = async { musicRepository.getArtistSongsRaw(artistId, sort = "popular", limit = 10) }
                    val albumsDeferred = async { musicRepository.getArtistAlbumsRaw(artistId) }

                    val artistResult = artistDeferred.await()
                    val songsResult = songsDeferred.await()
                    val albumsResult = albumsDeferred.await()

                    if (artistResult.isSuccess) {
                        _state.update {
                            it.copy(
                                artist = artistResult.getOrNull(),
                                songs = songsResult.getOrDefault(emptyList()),
                                albums = albumsResult.getOrDefault(emptyList()),
                                isLoading = false
                            )
                        }
                    } else {
                        _state.update { it.copy(error = "Failed to load artist details", isLoading = false) }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Unknown error", isLoading = false) }
            }
        }
    }

    fun toggleFollow() {
        viewModelScope.launch {
            try {
                val result = if (_state.value.isFollowing) {
                    musicRepository.unfollowArtistRaw(artistId)
                } else {
                    musicRepository.followArtistRaw(artistId)
                }
                if (result.isSuccess) {
                    _state.update { current ->
                        val delta = if (current.isFollowing) -1 else 1
                        current.copy(
                            isFollowing = !current.isFollowing,
                            artist = current.artist?.copy(
                                followersCount = current.artist.followersCount + delta
                            )
                        )
                    }
                }
            } catch (_: Exception) { }
        }
    }

    fun retry() {
        loadArtistDetails()
    }
}
