package com.musify.ui.screens.user.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musify.data.models.Artist
import com.musify.data.models.Playlist
import com.musify.data.models.Song
import com.musify.domain.repository.AuthRepository
import com.musify.domain.repository.MusicRepository
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
    private val authRepository: AuthRepository,
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val userResult = authRepository.getCurrentUser()
                val user = userResult.getOrNull()
                if (user != null) {
                    _state.value = _state.value.copy(userId = user.id)
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
                    val playlistsDeferred = async { musicRepository.getCurrentUserPlaylistsRaw() }
                    val followedPlaylistsDeferred = async { musicRepository.getFollowedPlaylistsRaw() }
                    val likedSongsDeferred = async {
                        _state.value.userId?.let { musicRepository.getLikedSongsRaw(it) }
                    }
                    val artistsDeferred = async {
                        _state.value.userId?.let { musicRepository.getFollowedArtistsRaw(it) }
                    }

                    val playlistsResult = playlistsDeferred.await()
                    val followedPlaylistsResult = followedPlaylistsDeferred.await()
                    val likedSongsResult = likedSongsDeferred.await()
                    val artistsResult = artistsDeferred.await()

                    _state.value = _state.value.copy(
                        isLoading = false,
                        playlists = playlistsResult.getOrDefault(emptyList()),
                        followedPlaylists = followedPlaylistsResult.getOrDefault(emptyList()),
                        likedSongs = likedSongsResult?.getOrDefault(emptyList()) ?: emptyList(),
                        followedArtists = artistsResult?.getOrDefault(emptyList()) ?: emptyList()
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
            musicRepository.createPlaylistRaw(name, description, isPublic)
                .onSuccess { newPlaylist ->
                    _state.value = _state.value.copy(
                        playlists = listOf(newPlaylist) + _state.value.playlists,
                        isCreatingPlaylist = false
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(
                        isCreatingPlaylist = false,
                        errorMessage = "Failed to create playlist"
                    )
                }
        }
    }

    fun refresh() {
        loadLibraryContent()
    }
}
