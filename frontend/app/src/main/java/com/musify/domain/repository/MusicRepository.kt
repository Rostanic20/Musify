package com.musify.domain.repository

import com.musify.domain.entity.Album
import com.musify.domain.entity.Song
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for music-related operations
 */
interface MusicRepository {
    
    suspend fun getSongs(limit: Int = 50, offset: Int = 0): Result<List<Song>>
    
    suspend fun getSongById(songId: Int): Result<Song?>
    
    suspend fun searchSongs(query: String, limit: Int = 20): Result<List<Song>>
    
    suspend fun getRecentlyPlayed(limit: Int = 10): Result<List<Song>>
    
    suspend fun getRecommendations(limit: Int = 20): Result<List<Song>>
    
    suspend fun getPopularAlbums(limit: Int = 10): Result<List<Album>>
    
    suspend fun getNewReleases(limit: Int = 10): Result<List<Album>>
    
    suspend fun getAlbumById(albumId: Int): Result<Album?>
    
    suspend fun getAlbumSongs(albumId: Int): Result<List<Song>>
    
    suspend fun toggleFavorite(songId: Int): Result<Boolean>
    
    suspend fun getFavoriteSongs(): Flow<List<Song>>
    
    suspend fun getStreamUrl(songId: Int, quality: String? = null): Result<String>
    
    suspend fun recordPlay(songId: Int, playedDuration: Int): Result<Unit>
    
    suspend fun recordSkip(songId: Int, playedDuration: Int): Result<SkipResult>
    
    suspend fun uploadSong(audioFile: java.io.File, artistId: Int, genre: String?): Result<Song>
    
    suspend fun uploadCoverArt(imageFile: java.io.File, songId: Int): Result<String>
    
    suspend fun getArtistSongs(artistId: Int): Result<List<Song>>
}

data class SkipResult(
    val allowed: Boolean,
    val skipsRemaining: Int?,
    val message: String?
)