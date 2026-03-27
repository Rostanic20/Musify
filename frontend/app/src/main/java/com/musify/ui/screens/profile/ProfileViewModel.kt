package com.musify.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musify.domain.entity.User
import com.musify.domain.usecase.auth.GetCurrentUserUseCase
import com.musify.domain.usecase.auth.LogoutUseCase
import com.musify.utils.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isLoggedOut: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()
    
    init {
        loadUserProfile()
    }
    
    private fun loadUserProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            getCurrentUserUseCase().fold(
                onSuccess = { user ->
                    _state.value = _state.value.copy(
                        user = user,
                        isLoading = false
                    )
                },
                onFailure = {
                    _state.value = _state.value.copy(isLoading = false)
                }
            )
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            logoutUseCase().fold(
                onSuccess = {
                    tokenManager.clearTokens()
                    _state.value = _state.value.copy(isLoggedOut = true)
                },
                onFailure = {
                    tokenManager.clearTokens()
                    _state.value = _state.value.copy(isLoggedOut = true)
                }
            )
        }
    }
}