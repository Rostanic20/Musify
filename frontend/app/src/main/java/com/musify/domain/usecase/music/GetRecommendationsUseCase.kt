package com.musify.domain.usecase.music

import com.musify.domain.entity.Song
import com.musify.domain.repository.MusicRepository
import javax.inject.Inject

/**
 * Use case for getting personalized recommendations
 */
class GetRecommendationsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(limit: Int = 20): Result<List<Song>> {
        return musicRepository.getRecommendations(limit.coerceIn(1, 100))
    }
}