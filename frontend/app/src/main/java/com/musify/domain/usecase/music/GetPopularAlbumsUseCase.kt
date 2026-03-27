package com.musify.domain.usecase.music

import com.musify.domain.entity.Album
import com.musify.domain.repository.MusicRepository
import javax.inject.Inject

/**
 * Use case for getting popular albums
 */
class GetPopularAlbumsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(limit: Int = 20): Result<List<Album>> {
        return musicRepository.getPopularAlbums(limit)
    }
}