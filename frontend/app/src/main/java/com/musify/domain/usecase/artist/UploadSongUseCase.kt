package com.musify.domain.usecase.artist

import com.musify.domain.entity.Song
import com.musify.domain.repository.MusicRepository
import java.io.File
import javax.inject.Inject

class UploadSongUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(
        audioFile: File,
        artistId: Int,
        genre: String? = null
    ): Result<Song> {
        return musicRepository.uploadSong(audioFile, artistId, genre)
    }
}