package com.musify.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musify.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForgotPasswordState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    fun requestReset(email: String) {
        viewModelScope.launch {
            _state.value = ForgotPasswordState(isLoading = true)
            authRepository.requestPasswordReset(email)
                .onSuccess {
                    _state.value = ForgotPasswordState(isSuccess = true)
                }
                .onFailure { e ->
                    _state.value = ForgotPasswordState(
                        errorMessage = e.message ?: "Failed to send reset link. Please check your email and try again."
                    )
                }
        }
    }
}
