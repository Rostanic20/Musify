package com.musify.domain.usecase.music

import com.musify.domain.entity.Song
import com.musify.domain.repository.MusicRepository
import javax.inject.Inject

/**
 * Use case for getting recently played songs
 */
class GetRecentlyPlayedUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(limit: Int = 10): Result<List<Song>> {
        return musicRepository.getRecentlyPlayed(limit.coerceIn(1, 50))
    }
}