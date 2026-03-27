package com.musify.domain.usecase.playlist

import com.musify.domain.entity.Playlist
import com.musify.domain.repository.PlaylistRepository
import javax.inject.Inject

/**
 * Use case for getting user playlists
 */
class GetUserPlaylistsUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(userId: Int? = null): Result<List<Playlist>> {
        return playlistRepository.getUserPlaylists(userId)
    }
}