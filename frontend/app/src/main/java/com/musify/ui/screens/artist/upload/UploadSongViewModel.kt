package com.musify.ui.screens.artist.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musify.domain.usecase.artist.UploadSongUseCase
import com.musify.domain.usecase.artist.UploadCoverArtUseCase
import com.musify.domain.usecase.auth.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class UploadSongState(
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean = false,
    val uploadedSongId: Int? = null,
    val errorMessage: String? = null,
    val uploadProgress: Float = 0f
)

@HiltViewModel
class UploadSongViewModel @Inject constructor(
    private val uploadSongUseCase: UploadSongUseCase,
    private val uploadCoverArtUseCase: UploadCoverArtUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(UploadSongState())
    val state: StateFlow<UploadSongState> = _state.asStateFlow()
    
    fun uploadSong(
        genre: String,
        audioFilePath: String,
        coverArtPath: String? = null
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isUploading = true,
                errorMessage = null,
                uploadProgress = 0f
            )
            
            try {
                val userResult = getCurrentUserUseCase()
                if (userResult.isFailure) {
                    _state.value = _state.value.copy(
                        isUploading = false,
                        errorMessage = "Failed to get user information"
                    )
                    return@launch
                }
                
                val user = userResult.getOrNull()!!
                if (!user.isArtist) {
                    _state.value = _state.value.copy(
                        isUploading = false,
                        errorMessage = "Only artists can upload songs"
                    )
                    return@launch
                }
                
                val audioFile = File(audioFilePath)
                if (!audioFile.exists()) {
                    _state.value = _state.value.copy(
                        isUploading = false,
                        errorMessage = "Audio file not found"
                    )
                    return@launch
                }
                
                _state.value = _state.value.copy(uploadProgress = 0.3f)
                
                val uploadResult = uploadSongUseCase(
                    audioFile = audioFile,
                    artistId = user.id,
                    genre = genre.ifBlank { null }
                )
                
                if (uploadResult.isSuccess) {
                    val song = uploadResult.getOrNull()!!
                    _state.value = _state.value.copy(uploadProgress = 0.7f)
                    
                    if (!coverArtPath.isNullOrBlank()) {
                        val coverFile = File(coverArtPath)
                        if (coverFile.exists()) {
                            uploadCoverArtUseCase(coverFile, song.id)
                        }
                    }
                    
                    _state.value = _state.value.copy(
                        isUploading = false,
                        uploadSuccess = true,
                        uploadedSongId = song.id,
                        uploadProgress = 1f
                    )
                } else {
                    _state.value = _state.value.copy(
                        isUploading = false,
                        errorMessage = uploadResult.exceptionOrNull()?.message ?: "Upload failed"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isUploading = false,
                    errorMessage = e.message ?: "An error occurred"
                )
            }
        }
    }
    
    fun resetState() {
        _state.value = UploadSongState()
    }
}