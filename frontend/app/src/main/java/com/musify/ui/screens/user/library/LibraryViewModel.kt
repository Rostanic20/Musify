package com.musify.ui.screens.user.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musify.data.api.MusifyApiService
import com.musify.data.models.Artist
import com.musify.data.models.CreatePlaylistRequest
import com.musify.data.models.Playlist
import com.musify.data.models.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryState(
    val isLoading: Boolean = false,
    val playlists: List<Playlist> = emptyList(),
    val followedPlaylists: List<Playlist> = emptyList(),
    val likedSongs: List<Song> = emptyList(),
    val followedArtists: List<Artist> = emptyList(),
    val selectedTab: LibraryTab = LibraryTab.PLAYLISTS,
    val userId: Int? = null,
    val isCreatingPlaylist: Boolean = false,
    val errorMessage: String? = null
)

enum class LibraryTab {
    PLAYLISTS, LIKED_SONGS, ARTISTS
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val apiService: MusifyApiService
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val response = apiService.getCurrentUser()
                if (response.isSuccessful) {
                    val user = response.body()
                    _state.value = _state.value.copy(userId = user?.id)
                    loadLibraryContent()
                } else {
                    _state.value = _state.value.copy(
                        errorMessage = "Failed to load user"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, errorMessage = "Network error. Please try again.")
            }
        }
    }

    private fun loadLibraryContent() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            try {
                coroutineScope {
                    val playlistsDeferred = async { apiService.getCurrentUserPlaylists() }
                    val followedPlaylistsDeferred = async { apiService.getFollowedPlaylists() }
                    val likedSongsDeferred = async {
                        _state.value.userId?.let { apiService.getLikedSongs(it) }
                    }
                    val artistsDeferred = async {
                        _state.value.userId?.let { apiService.getFollowedArtists(it) }
                    }

                    val playlistsResult = playlistsDeferred.await()
                    val followedPlaylistsResult = followedPlaylistsDeferred.await()
                    val likedSongsResult = likedSongsDeferred.await()
                    val artistsResult = artistsDeferred.await()

                    _state.value = _state.value.copy(
                        isLoading = false,
                        playlists = if (playlistsResult.isSuccessful)
                            playlistsResult.body() ?: emptyList() else emptyList(),
                        followedPlaylists = if (followedPlaylistsResult.isSuccessful)
                            followedPlaylistsResult.body() ?: emptyList() else emptyList(),
                        likedSongs = if (likedSongsResult?.isSuccessful == true)
                            likedSongsResult.body() ?: emptyList() else emptyList(),
                        followedArtists = if (artistsResult?.isSuccessful == true)
                            artistsResult.body() ?: emptyList() else emptyList()
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, errorMessage = "Network error. Please try again.")
            }
        }
    }

    fun selectTab(tab: LibraryTab) {
        _state.value = _state.value.copy(selectedTab = tab)
    }

    fun createPlaylist(name: String, description: String?, isPublic: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isCreatingPlaylist = true)
            try {
                val request = CreatePlaylistRequest(
                    name = name,
                    description = description,
                    isPublic = isPublic
                )
                val response = apiService.createPlaylist(request)
                if (response.isSuccessful) {
                    val newPlaylist = response.body()
                    if (newPlaylist != null) {
                        _state.value = _state.value.copy(
                            playlists = listOf(newPlaylist) + _state.value.playlists,
                            isCreatingPlaylist = false
                        )
                    }
                } else {
                    _state.value = _state.value.copy(
                        isCreatingPlaylist = false,
                        errorMessage = "Failed to create playlist"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isCreatingPlaylist = false,
                    errorMessage = "Network error. Please try again."
                )
            }
        }
    }

    fun refresh() {
        loadLibraryContent()
    }
}
