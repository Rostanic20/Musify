package com.musify.ui.screens.artist.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musify.domain.entity.Song
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
) : ViewModel() {

    private val _state = MutableStateFlow(ArtistDashboardState())
    val state: StateFlow<ArtistDashboardState> = _state.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            _state.value = _state.value.copy(
                isLoading = false,
                totalPlays = 0,
                followers = 0,
                songCount = 0,
                albumCount = 0,
                recentSongs = emptyList()
            )
        }
    }

    fun refresh() {
        loadDashboardData()
    }
}
