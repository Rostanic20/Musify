package com.musify.domain.usecase.playlist

import com.musify.domain.entity.Playlist
import com.musify.domain.exception.ValidationException
import com.musify.domain.repository.PlaylistRepository
import javax.inject.Inject

/**
 * Use case for creating a new playlist
 */
class CreatePlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(
        name: String,
        description: String? = null,
        isPublic: Boolean = true
    ): Result<Playlist> {
        if (name.isBlank()) {
            return Result.failure(ValidationException("Playlist name cannot be empty"))
        }
        
        if (name.length > 100) {
            return Result.failure(ValidationException("Playlist name is too long (max 100 characters)"))
        }
        
        if (description != null && description.length > 300) {
            return Result.failure(ValidationException("Description is too long (max 300 characters)"))
        }
        
        return playlistRepository.createPlaylist(name.trim(), description?.trim(), isPublic)
    }
}