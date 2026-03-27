package com.musify.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musify.domain.entity.User
import com.musify.domain.usecase.auth.GetCurrentUserUseCase
import com.musify.player.MusicPlayerManager
import com.musify.utils.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val shouldNavigateToLogin: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val tokenManager: TokenManager,
    val musicPlayerManager: MusicPlayerManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()
    
    init {
        loadCurrentUser()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().fold(
                onSuccess = { user ->
                    _state.value = _state.value.copy(
                        user = user,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    if (error.message?.contains("401") == true ||
                        error.message?.contains("Unauthorized") == true ||
                        error.message?.contains("Server error occurred") == true) {
                        tokenManager.clearTokens()
                        _state.value = _state.value.copy(
                            isLoading = false,
                            shouldNavigateToLogin = true
                        )
                    } else {
                        _state.value = _state.value.copy(
                            isLoading = false
                        )
                    }
                }
            )
        }
    }
}