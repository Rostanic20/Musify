package com.musify.domain.usecase.artist

import com.musify.domain.entity.Song
import com.musify.domain.repository.MusicRepository
import javax.inject.Inject

class GetArtistSongsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(artistId: Int): Result<List<Song>> {
        return musicRepository.getArtistSongs(artistId)
    }
}