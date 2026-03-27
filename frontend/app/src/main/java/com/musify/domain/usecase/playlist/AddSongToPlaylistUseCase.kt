package com.musify.domain.usecase.playlist

import com.musify.domain.exception.PlaylistNotFoundException
import com.musify.domain.exception.SongNotFoundException
import com.musify.domain.repository.PlaylistRepository
import javax.inject.Inject

/**
 * Use case for adding a song to a playlist
 */
class AddSongToPlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: Int, songId: Int): Result<Unit> {
        if (playlistId <= 0) {
            return Result.failure(PlaylistNotFoundException())
        }
        
        if (songId <= 0) {
            return Result.failure(SongNotFoundException())
        }
        
        return playlistRepository.addSongToPlaylist(playlistId, songId)
    }
}