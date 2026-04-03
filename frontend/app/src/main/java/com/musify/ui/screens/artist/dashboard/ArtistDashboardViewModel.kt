package com.musify.ui.screens.artist.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musify.domain.entity.Song
import com.musify.domain.repository.AuthRepository
import com.musify.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArtistDashboardState(
    val isLoading: Boolean = false,
    val totalPlays: Int = 0,
    val followers: Int = 0,
    val songCount: Int = 0,
    val albumCount: Int = 0,
    val recentSongs: List<Song> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class ArtistDashboardViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ArtistDashboardState())
    val state: StateFlow<ArtistDashboardState> = _state.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val userResult = authRepository.getCurrentUser()
            val user = userResult.getOrNull()
            if (user == null) {
                _state.value = _state.value.copy(isLoading = false, errorMessage = "Failed to load user")
                return@launch
            }

            val songsResult = musicRepository.getArtistSongs(user.id)
            val albumsResult = musicRepository.getArtistAlbums(user.id)

            _state.value = _state.value.copy(
                isLoading = false,
                songCount = songsResult.getOrNull()?.size ?: 0,
                albumCount = albumsResult.getOrNull()?.size ?: 0,
                recentSongs = songsResult.getOrNull()?.take(5) ?: emptyList()
            )
        }
    }

    fun refresh() {
        loadDashboardData()
    }
}
