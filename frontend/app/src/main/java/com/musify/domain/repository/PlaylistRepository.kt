package com.musify.domain.repository

import com.musify.domain.entity.Playlist
import com.musify.domain.entity.Song
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for playlist operations
 */
interface PlaylistRepository {
    
    suspend fun getUserPlaylists(userId: Int? = null): Result<List<Playlist>>
    
    suspend fun getPlaylistById(playlistId: Int): Result<Playlist?>
    
    suspend fun getPlaylistSongs(playlistId: Int): Result<List<Song>>
    
    suspend fun createPlaylist(
        name: String,
        description: String?,
        isPublic: Boolean
    ): Result<Playlist>
    
    suspend fun updatePlaylist(
        playlistId: Int,
        name: String?,
        description: String?,
        isPublic: Boolean?
    ): Result<Playlist>
    
    suspend fun deletePlaylist(playlistId: Int): Result<Unit>
    
    suspend fun addSongToPlaylist(playlistId: Int, songId: Int): Result<Unit>
    
    suspend fun removeSongFromPlaylist(playlistId: Int, songId: Int): Result<Unit>
    
    suspend fun reorderPlaylistSongs(
        playlistId: Int,
        fromPosition: Int,
        toPosition: Int
    ): Result<Unit>
    
    suspend fun followPlaylist(playlistId: Int): Result<Unit>
    
    suspend fun unfollowPlaylist(playlistId: Int): Result<Unit>
    
    fun getFollowedPlaylists(): Flow<List<Playlist>>
}