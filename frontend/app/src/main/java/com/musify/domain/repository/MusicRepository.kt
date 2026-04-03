package com.musify.domain.repository

import com.musify.domain.entity.Album
import com.musify.domain.entity.Artist
import com.musify.domain.entity.Song
import kotlinx.coroutines.flow.Flow
import com.musify.data.models.Song as DataSong
import com.musify.data.models.Artist as DataArtist
import com.musify.data.models.Album as DataAlbum
import com.musify.data.models.Playlist as DataPlaylist
import com.musify.data.models.PlaylistDetails as DataPlaylistDetails

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

    suspend fun getArtistDetails(artistId: Int): Result<Artist?>

    suspend fun getArtistAlbums(artistId: Int): Result<List<Album>>

    suspend fun getSongDetails(songId: Int): Result<SongDetail?>

    suspend fun getAlbumDetail(albumId: Int): Result<AlbumDetail?>

    suspend fun searchAll(query: String): Result<SearchResults>

    suspend fun getTrending(limit: Int = 10): Result<TrendingResults>

    suspend fun getLikedSongs(userId: Int, limit: Int = 50, offset: Int = 0): Result<List<Song>>

    suspend fun getFollowedArtists(userId: Int): Result<List<Artist>>

    // Raw methods returning data.models types for screens that use them directly
    suspend fun searchRaw(query: String): Result<RawSearchResults>
    suspend fun getTrendingRaw(limit: Int = 10): Result<RawTrendingResults>
    suspend fun getCurrentUserPlaylistsRaw(): Result<List<DataPlaylist>>
    suspend fun getFollowedPlaylistsRaw(): Result<List<DataPlaylist>>
    suspend fun getLikedSongsRaw(userId: Int, limit: Int = 50, offset: Int = 0): Result<List<DataSong>>
    suspend fun getFollowedArtistsRaw(userId: Int): Result<List<DataArtist>>
    suspend fun createPlaylistRaw(name: String, description: String?, isPublic: Boolean): Result<DataPlaylist>
    suspend fun getArtistDetailsRaw(artistId: Int): Result<DataArtist>
    suspend fun getArtistSongsRaw(artistId: Int, sort: String = "popular", limit: Int = 50): Result<List<DataSong>>
    suspend fun getArtistAlbumsRaw(artistId: Int): Result<List<DataAlbum>>
    suspend fun followArtistRaw(artistId: Int): Result<Unit>
    suspend fun unfollowArtistRaw(artistId: Int): Result<Unit>
    suspend fun getPlaylistDetailsRaw(playlistId: Int): Result<DataPlaylistDetails>
    suspend fun removeSongFromPlaylistRaw(playlistId: Int, songId: Int): Result<Unit>
}

data class SkipResult(
    val allowed: Boolean,
    val skipsRemaining: Int?,
    val message: String?
)

data class SongDetail(
    val song: Song,
    val isFavorite: Boolean
)

data class AlbumDetail(
    val album: Album,
    val songs: List<Song>
)

data class SearchResults(
    val songs: List<Song> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val albums: List<Album> = emptyList()
)

data class TrendingResults(
    val songs: List<Song> = emptyList(),
    val artists: List<Artist> = emptyList()
)

data class RawSearchResults(
    val songs: List<DataSong> = emptyList(),
    val artists: List<DataArtist> = emptyList(),
    val albums: List<DataAlbum> = emptyList()
)

data class RawTrendingResults(
    val songs: List<DataSong> = emptyList(),
    val artists: List<DataArtist> = emptyList()
)