package com.musify.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musify.domain.usecase.auth.LoginUseCase
import com.musify.utils.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginState(
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val errorMessage: String? = null,
    val requires2FA: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            loginUseCase(email, password).fold(
                onSuccess = { authResult ->
                    if (authResult.requires2FA) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            requires2FA = true
                        )
                    } else {
                        tokenManager.saveTokens(
                            authResult.accessToken,
                            authResult.refreshToken
                        )
                        
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isLoginSuccessful = true
                        )
                    }
                },
                onFailure = { exception ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Login failed"
                    )
                }
            )
        }
    }
    
    fun loginWith2FA(email: String, password: String, totpCode: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            loginUseCase(email, password, totpCode).fold(
                onSuccess = { authResult ->
                    tokenManager.saveTokens(
                        authResult.accessToken,
                        authResult.refreshToken
                    )
                    
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isLoginSuccessful = true
                    )
                },
                onFailure = { exception ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Invalid 2FA code"
                    )
                }
            )
        }
    }
}