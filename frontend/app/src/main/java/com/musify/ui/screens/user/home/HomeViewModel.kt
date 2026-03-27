package com.musify.ui.screens.user.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musify.domain.entity.Album
import com.musify.domain.entity.Playlist
import com.musify.domain.entity.Song
import com.musify.domain.usecase.music.GetRecentlyPlayedUseCase
import com.musify.domain.usecase.music.GetRecommendationsUseCase
import com.musify.domain.usecase.music.GetPopularAlbumsUseCase
import com.musify.domain.usecase.music.GetNewReleasesUseCase
import com.musify.domain.usecase.playlist.GetUserPlaylistsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val isLoading: Boolean = false,
    val recentlyPlayed: List<Song> = emptyList(),
    val recommendations: List<Song> = emptyList(),
    val popularAlbums: List<Album> = emptyList(),
    val newReleases: List<Album> = emptyList(),
    val userPlaylists: List<Playlist> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRecentlyPlayedUseCase: GetRecentlyPlayedUseCase,
    private val getRecommendationsUseCase: GetRecommendationsUseCase,
    private val getPopularAlbumsUseCase: GetPopularAlbumsUseCase,
    private val getNewReleasesUseCase: GetNewReleasesUseCase,
    private val getUserPlaylistsUseCase: GetUserPlaylistsUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()
    
    init {
        loadHomeContent()
    }
    
    private fun loadHomeContent() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            coroutineScope {
                val recentlyPlayedDeferred = async { getRecentlyPlayedUseCase() }
                val recommendationsDeferred = async { getRecommendationsUseCase() }
                val popularAlbumsDeferred = async { getPopularAlbumsUseCase() }
                val newReleasesDeferred = async { getNewReleasesUseCase() }
                val userPlaylistsDeferred = async { getUserPlaylistsUseCase() }

                val recentlyPlayedResult = recentlyPlayedDeferred.await()
                val recommendationsResult = recommendationsDeferred.await()
                val popularAlbumsResult = popularAlbumsDeferred.await()
                val newReleasesResult = newReleasesDeferred.await()
                val userPlaylistsResult = userPlaylistsDeferred.await()

                _state.value = _state.value.copy(
                    isLoading = false,
                    recentlyPlayed = recentlyPlayedResult.getOrElse { emptyList() },
                    recommendations = recommendationsResult.getOrElse { emptyList() },
                    popularAlbums = popularAlbumsResult.getOrElse { emptyList() },
                    newReleases = newReleasesResult.getOrElse { emptyList() },
                    userPlaylists = userPlaylistsResult.getOrElse { emptyList() },
                    errorMessage = if (recentlyPlayedResult.isFailure || recommendationsResult.isFailure) {
                        "Failed to load some content"
                    } else null
                )
            }
        }
    }
    
    fun refresh() {
        loadHomeContent()
    }
}