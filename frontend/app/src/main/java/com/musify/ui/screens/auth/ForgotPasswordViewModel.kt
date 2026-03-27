package com.musify.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musify.data.api.MusifyApiService
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
    private val apiService: MusifyApiService
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    fun requestReset(email: String) {
        viewModelScope.launch {
            _state.value = ForgotPasswordState(isLoading = true)
            try {
                val request = mapOf("email" to email)
                val response = apiService.forgotPassword(request)
                if (response.isSuccessful) {
                    _state.value = ForgotPasswordState(isSuccess = true)
                } else {
                    _state.value = ForgotPasswordState(
                        errorMessage = "Failed to send reset link. Please check your email and try again."
                    )
                }
            } catch (e: Exception) {
                _state.value = ForgotPasswordState(
                    errorMessage = "Network error. Please check your connection and try again."
                )
            }
        }
    }
}
