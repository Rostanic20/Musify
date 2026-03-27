package com.musify.domain.usecase.artist

import com.musify.domain.repository.MusicRepository
import java.io.File
import javax.inject.Inject

class UploadCoverArtUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(
        imageFile: File,
        songId: Int
    ): Result<String> {
        return musicRepository.uploadCoverArt(imageFile, songId)
    }
}