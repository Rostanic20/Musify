package com.musify.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musify.domain.repository.AuthRepository
import com.musify.domain.usecase.auth.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VerificationState(
    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val isVerified: Boolean = false,
    val isError: Boolean = false,
    val message: String? = null,
    val resendCooldown: Int = 0
)

@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(VerificationState())
    val state: StateFlow<VerificationState> = _state.asStateFlow()

    fun checkVerificationStatus() {
        viewModelScope.launch {
            getCurrentUserUseCase().fold(
                onSuccess = { user ->
                    if (user != null && user.isEmailVerified) {
                        _state.value = _state.value.copy(
                            isVerified = true,
                            message = "Email verified successfully!",
                            isError = false
                        )
                    }
                },
                onFailure = { }
            )
        }
    }

    fun verifySMS(code: String, phoneNumber: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                message = null,
                isError = false
            )

            authRepository.verifySMS(code, phoneNumber).fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isVerified = true,
                        message = "Phone number verified successfully!",
                        isError = false
                    )
                },
                onFailure = { exception ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        message = exception.message ?: "Verification failed",
                        isError = true
                    )
                }
            )
        }
    }

    fun resendVerificationEmail(email: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isResending = true,
                message = null,
                isError = false
            )

            authRepository.resendVerificationEmail(email).fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        isResending = false,
                        message = "Verification email sent!",
                        isError = false,
                        resendCooldown = 60
                    )
                    startCooldown()
                },
                onFailure = { exception ->
                    _state.value = _state.value.copy(
                        isResending = false,
                        message = exception.message ?: "Failed to resend email",
                        isError = true
                    )
                }
            )
        }
    }

    fun resendVerificationSMS(phoneNumber: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isResending = true,
                message = null,
                isError = false
            )

            authRepository.resendVerificationSMS(phoneNumber).fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        isResending = false,
                        message = "Verification code sent!",
                        isError = false,
                        resendCooldown = 60
                    )
                    startCooldown()
                },
                onFailure = { exception ->
                    _state.value = _state.value.copy(
                        isResending = false,
                        message = exception.message ?: "Failed to resend SMS",
                        isError = true
                    )
                }
            )
        }
    }

    private fun startCooldown() {
        viewModelScope.launch {
            while (_state.value.resendCooldown > 0) {
                delay(1000)
                _state.value = _state.value.copy(
                    resendCooldown = _state.value.resendCooldown - 1
                )
            }
        }
    }
}
