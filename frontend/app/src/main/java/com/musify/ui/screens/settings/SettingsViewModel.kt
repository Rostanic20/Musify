package com.musify.ui.screens.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musify.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class SettingsState(
    val audioQuality: AudioQuality = AudioQuality.HIGH,
    val crossfadeEnabled: Boolean = false,
    val cacheSizeBytes: Long = 0,
    val isChangingPassword: Boolean = false,
    val passwordChangeSuccess: Boolean = false,
    val passwordChangeError: String? = null,
    val isClearingCache: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val deleteAccountSuccess: Boolean = false,
    val deleteAccountError: String? = null
)

enum class AudioQuality(val label: String) {
    LOW("Low (96 kbps)"),
    MEDIUM("Medium (160 kbps)"),
    HIGH("High (320 kbps)"),
    LOSSLESS("Lossless (FLAC)")
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val application: Application
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        calculateCacheSize()
    }

    fun setAudioQuality(quality: AudioQuality) {
        _state.value = _state.value.copy(audioQuality = quality)
    }

    fun toggleCrossfade() {
        _state.value = _state.value.copy(crossfadeEnabled = !_state.value.crossfadeEnabled)
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isChangingPassword = true,
                passwordChangeError = null,
                passwordChangeSuccess = false
            )

            userRepository.changePassword(currentPassword, newPassword)
                .onSuccess {
                    _state.value = _state.value.copy(
                        isChangingPassword = false,
                        passwordChangeSuccess = true
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(
                        isChangingPassword = false,
                        passwordChangeError = it.message ?: "Failed to change password"
                    )
                }
        }
    }

    fun clearPasswordState() {
        _state.value = _state.value.copy(
            passwordChangeSuccess = false,
            passwordChangeError = null
        )
    }

    fun deleteAccount(password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isDeletingAccount = true,
                deleteAccountError = null
            )

            userRepository.deleteAccount(password)
                .onSuccess {
                    _state.value = _state.value.copy(
                        isDeletingAccount = false,
                        deleteAccountSuccess = true
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(
                        isDeletingAccount = false,
                        deleteAccountError = it.message ?: "Failed to delete account"
                    )
                }
        }
    }

    private fun calculateCacheSize() {
        viewModelScope.launch {
            val size = withContext(Dispatchers.IO) {
                getDirSize(application.cacheDir)
            }
            _state.value = _state.value.copy(cacheSizeBytes = size)
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isClearingCache = true)
            withContext(Dispatchers.IO) {
                deleteDir(application.cacheDir)
            }
            _state.value = _state.value.copy(
                isClearingCache = false,
                cacheSizeBytes = 0
            )
        }
    }

    private fun getDirSize(dir: File): Long {
        if (!dir.exists()) return 0
        var size = 0L
        val files = dir.listFiles() ?: return 0
        for (file in files) {
            size += if (file.isDirectory) getDirSize(file) else file.length()
        }
        return size
    }

    private fun deleteDir(dir: File) {
        if (!dir.exists()) return
        val files = dir.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) deleteDir(file) else file.delete()
        }
    }
}
