package com.musify.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musify.data.api.MusifyApiService
import com.musify.data.models.Album
import com.musify.data.models.Artist
import com.musify.data.models.Song
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
    private val apiService: MusifyApiService,
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
                    val artistDeferred = async { apiService.getArtistDetails(artistId) }
                    val songsDeferred = async { apiService.getArtistSongs(artistId, sort = "popular", limit = 10) }
                    val albumsDeferred = async { apiService.getArtistAlbums(artistId) }

                    val artistResponse = artistDeferred.await()
                    val songsResponse = songsDeferred.await()
                    val albumsResponse = albumsDeferred.await()

                    if (artistResponse.isSuccessful) {
                        _state.update {
                            it.copy(
                                artist = artistResponse.body(),
                                songs = songsResponse.body() ?: emptyList(),
                                albums = albumsResponse.body() ?: emptyList(),
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
                val response = if (_state.value.isFollowing) {
                    apiService.unfollowArtist(artistId)
                } else {
                    apiService.followArtist(artistId)
                }
                if (response.isSuccessful) {
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
