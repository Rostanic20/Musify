package com.musify.domain.usecase.music

import com.musify.domain.exception.SongNotFoundException
import com.musify.domain.repository.MusicRepository
import javax.inject.Inject

/**
 * Use case for toggling favorite status of a song
 */
class ToggleFavoriteSongUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(songId: Int): Result<Boolean> {
        if (songId <= 0) {
            return Result.failure(SongNotFoundException())
        }
        
        return musicRepository.toggleFavorite(songId)
    }
}