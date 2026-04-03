package com.musify.ui.screens.artist.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musify.domain.entity.Song
import com.musify.domain.usecase.artist.GetArtistSongsUseCase
import com.musify.domain.usecase.auth.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArtistSongsState(
    val isLoading: Boolean = false,
    val songs: List<Song> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class ArtistSongsViewModel @Inject constructor(
    private val getArtistSongsUseCase: GetArtistSongsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(ArtistSongsState())
    val state: StateFlow<ArtistSongsState> = _state.asStateFlow()
    
    init {
        loadArtistSongs()
    }
    
    private fun loadArtistSongs() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            val userResult = getCurrentUserUseCase()
            if (userResult.isSuccess) {
                val user = userResult.getOrNull() ?: run {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to get user information"
                    )
                    return@launch
                }
                if (user.isArtist) {
                    val songsResult = getArtistSongsUseCase(user.id)
                    _state.value = _state.value.copy(
                        isLoading = false,
                        songs = songsResult.getOrElse { emptyList() },
                        errorMessage = if (songsResult.isFailure) {
                            songsResult.exceptionOrNull()?.message ?: "Failed to load songs"
                        } else null
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "User is not an artist"
                    )
                }
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to get user information"
                )
            }
        }
    }
    
    fun refresh() {
        loadArtistSongs()
    }
}