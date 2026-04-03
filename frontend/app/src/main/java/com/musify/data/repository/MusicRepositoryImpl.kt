package com.musify.data.repository

import com.musify.data.api.MusifyApiService
import com.musify.data.mapper.SongMapper.toDomainModel
import com.musify.domain.entity.Album
import com.musify.domain.entity.Artist
import com.musify.domain.entity.Song
import com.musify.domain.exception.NetworkException
import com.musify.domain.exception.ServerException
import com.musify.domain.exception.SongNotFoundException
import com.musify.domain.repository.AlbumDetail
import com.musify.domain.repository.MusicRepository
import com.musify.domain.repository.RawSearchResults
import com.musify.domain.repository.RawTrendingResults
import com.musify.domain.repository.SearchResults
import com.musify.domain.repository.SkipResult
import com.musify.domain.repository.SongDetail
import com.musify.domain.repository.TrendingResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val apiService: MusifyApiService
) : MusicRepository {
    
    override suspend fun getSongs(limit: Int, offset: Int): Result<List<Song>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSongs(limit = limit, offset = offset)
                if (response.isSuccessful) {
                    val songs = response.body()?.map { it.toDomainModel() } ?: emptyList()
                    Result.success(songs)
                } else {
                    Result.failure(ServerException("Failed to fetch songs"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun getSongById(songId: Int): Result<Song?> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSongDetails(songId)
                if (response.isSuccessful) {
                    Result.success(response.body()?.toDomainModel())
                } else if (response.code() == 404) {
                    Result.failure(SongNotFoundException())
                } else {
                    Result.failure(ServerException("Failed to fetch song details"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun searchSongs(query: String, limit: Int): Result<List<Song>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.search(
                    mapOf(
                        "query" to query,
                        "type" to "songs",
                        "limit" to limit
                    )
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    @Suppress("UNCHECKED_CAST")
                    val songsList = body?.get("songs") as? List<Map<String, Any>>
                    val songs = songsList?.mapNotNull { map ->
                        val id = (map["id"] as? Double)?.toInt() ?: return@mapNotNull null
                        val title = map["title"] as? String ?: return@mapNotNull null
                        val artistName = map["artistName"] as? String ?: "Unknown"
                        val artistId = (map["artistId"] as? Double)?.toInt() ?: 0
                        Song(
                            id = id,
                            title = title,
                            artist = com.musify.domain.entity.Artist(
                                id = artistId,
                                name = artistName,
                                bio = null,
                                profileImageUrl = null,
                                isVerified = false,
                                monthlyListeners = 0,
                                followersCount = 0
                            ),
                            album = null,
                            durationSeconds = (map["duration"] as? Double)?.toInt() ?: 0,
                            coverArtUrl = map["coverArt"] as? String,
                            genre = map["genre"] as? String,
                            playCount = (map["playCount"] as? Double)?.toLong() ?: 0,
                            isFavorite = false
                        )
                    } ?: emptyList()
                    Result.success(songs)
                } else {
                    Result.failure(ServerException("Search failed"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun getRecentlyPlayed(limit: Int): Result<List<Song>> {
        return withContext(Dispatchers.IO) {
            try {
                val userResponse = apiService.getCurrentUser()
                if (userResponse.isSuccessful) {
                    val userId = userResponse.body()?.id
                    if (userId != null) {
                        val response = apiService.getListeningHistory(userId, limit)
                        if (response.isSuccessful) {
                            val songs = response.body()?.map { it.toDomainModel() } ?: emptyList()
                            return@withContext Result.success(songs)
                        }
                    }
                }
                val response = apiService.getSongs(limit = limit)
                if (response.isSuccessful) {
                    val songs = response.body()?.map { it.toDomainModel() } ?: emptyList()
                    Result.success(songs)
                } else {
                    Result.failure(ServerException("Failed to fetch recently played"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun getRecommendations(limit: Int): Result<List<Song>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getRecommendations(limit = limit)
                if (response.isSuccessful) {
                    val songs = response.body()?.map { it.toDomainModel() } ?: emptyList()
                    Result.success(songs)
                } else {
                    Result.failure(ServerException("Failed to fetch recommendations"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun getPopularAlbums(limit: Int): Result<List<Album>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAlbums(sort = "popular", limit = limit)
                if (response.isSuccessful) {
                    val albums = response.body()?.map { it.toDomainModel() } ?: emptyList()
                    Result.success(albums)
                } else {
                    Result.failure(ServerException("Failed to fetch popular albums"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun getNewReleases(limit: Int): Result<List<Album>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAlbums(sort = "newest", limit = limit)
                if (response.isSuccessful) {
                    val albums = response.body()?.map { it.toDomainModel() } ?: emptyList()
                    Result.success(albums)
                } else {
                    Result.failure(ServerException("Failed to fetch new releases"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun getAlbumById(albumId: Int): Result<Album?> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAlbumDetails(albumId)
                if (response.isSuccessful) {
                    Result.success(response.body()?.album?.toDomainModel())
                } else {
                    Result.failure(ServerException("Failed to fetch album"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun getAlbumSongs(albumId: Int): Result<List<Song>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAlbumDetails(albumId)
                if (response.isSuccessful) {
                    val songs = response.body()?.songs?.map { it.toDomainModel() } ?: emptyList()
                    Result.success(songs)
                } else {
                    Result.failure(ServerException("Failed to fetch album songs"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun toggleFavorite(songId: Int): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.toggleFavorite(songId)
                if (response.isSuccessful) {
                    val isFavorite = response.body()?.get("isFavorite") ?: false
                    Result.success(isFavorite)
                } else {
                    Result.failure(ServerException("Failed to toggle favorite"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun getFavoriteSongs(): Flow<List<Song>> {
        return flow {
            try {
                val userResponse = apiService.getCurrentUser()
                if (userResponse.isSuccessful) {
                    val userId = userResponse.body()?.id
                    if (userId != null) {
                        val response = apiService.getLikedSongs(userId)
                        if (response.isSuccessful) {
                            val songs = response.body()?.map { it.toDomainModel() } ?: emptyList()
                            emit(songs)
                            return@flow
                        }
                    }
                }
                emit(emptyList())
            } catch (e: Exception) {
                emit(emptyList())
            }
        }
    }
    
    override suspend fun getStreamUrl(songId: Int, quality: String?): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getStreamUrl(songId, quality)
                if (response.isSuccessful) {
                    val url = response.body()?.get("url")
                    if (url != null) {
                        Result.success(url)
                    } else {
                        Result.failure(ServerException("No stream URL returned"))
                    }
                } else {
                    Result.failure(ServerException("Failed to get stream URL"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun recordPlay(songId: Int, playedDuration: Int): Result<Unit> {
        // Play tracking is handled server-side when the stream URL is requested
        return Result.success(Unit)
    }
    
    override suspend fun recordSkip(songId: Int, playedDuration: Int): Result<SkipResult> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.skipSong(
                    mapOf(
                        "songId" to songId,
                        "playedDuration" to playedDuration
                    )
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    Result.success(
                        SkipResult(
                            allowed = body?.get("allowed") as? Boolean ?: true,
                            skipsRemaining = (body?.get("skipsRemaining") as? Double)?.toInt(),
                            message = body?.get("message") as? String
                        )
                    )
                } else {
                    Result.failure(ServerException("Skip not allowed"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun uploadSong(audioFile: File, artistId: Int, genre: String?): Result<Song> {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = audioFile.asRequestBody("audio/mpeg".toMediaType())
                val multipartBody = MultipartBody.Part.createFormData(
                    "file",
                    audioFile.name,
                    requestBody
                )
                
                val response = apiService.uploadSong(multipartBody, artistId, genre)
                if (response.isSuccessful) {
                    val uploadResponse = response.body()
                    if (uploadResponse != null) {
                        val songResponse = apiService.getSongDetails(uploadResponse.songId)
                        if (songResponse.isSuccessful) {
                            val songBody = songResponse.body()
                                ?: return@withContext Result.failure(ServerException("Failed to parse uploaded song details"))
                            Result.success(songBody.toDomainModel())
                        } else {
                            Result.failure(ServerException("Failed to fetch uploaded song details"))
                        }
                    } else {
                        Result.failure(ServerException("Upload failed: no response"))
                    }
                } else {
                    Result.failure(ServerException("Upload failed: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Upload error"))
            }
        }
    }
    
    override suspend fun uploadCoverArt(imageFile: File, songId: Int): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = imageFile.asRequestBody("image/*".toMediaType())
                val multipartBody = MultipartBody.Part.createFormData(
                    "file",
                    imageFile.name,
                    requestBody
                )
                
                val response = apiService.uploadSongCover(songId, multipartBody)
                if (response.isSuccessful) {
                    val coverUrl = response.body()?.get("url")
                    if (coverUrl != null) {
                        Result.success(coverUrl)
                    } else {
                        Result.failure(ServerException("No cover URL returned"))
                    }
                } else {
                    Result.failure(ServerException("Cover upload failed: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Cover upload error"))
            }
        }
    }
    
    override suspend fun getArtistSongs(artistId: Int): Result<List<Song>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getArtistSongs(artistId)
                if (response.isSuccessful) {
                    val songs = response.body()?.map { it.toDomainModel() } ?: emptyList()
                    Result.success(songs)
                } else {
                    Result.failure(ServerException("Failed to fetch artist songs"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }

    override suspend fun getArtistDetails(artistId: Int): Result<Artist?> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getArtistDetails(artistId)
                if (response.isSuccessful) {
                    Result.success(response.body()?.toDomainModel())
                } else {
                    Result.failure(ServerException("Failed to fetch artist details"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }

    override suspend fun getArtistAlbums(artistId: Int): Result<List<Album>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getArtistAlbums(artistId)
                if (response.isSuccessful) {
                    val albums = response.body()?.map { it.toDomainModel() } ?: emptyList()
                    Result.success(albums)
                } else {
                    Result.failure(ServerException("Failed to fetch artist albums"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }

    override suspend fun getSongDetails(songId: Int): Result<SongDetail?> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSongDetails(songId)
                if (response.isSuccessful) {
                    val details = response.body()
                    if (details != null) {
                        Result.success(
                            SongDetail(
                                song = details.toDomainModel(),
                                isFavorite = details.isFavorite
                            )
                        )
                    } else {
                        Result.failure(ServerException("No song details returned"))
                    }
                } else if (response.code() == 404) {
                    Result.failure(SongNotFoundException())
                } else {
                    Result.failure(ServerException("Failed to fetch song details"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }

    override suspend fun getAlbumDetail(albumId: Int): Result<AlbumDetail?> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAlbumDetails(albumId)
                if (response.isSuccessful) {
                    val details = response.body()
                    if (details != null) {
                        Result.success(
                            AlbumDetail(
                                album = details.album.toDomainModel(),
                                songs = details.songs.map { it.toDomainModel() }
                            )
                        )
                    } else {
                        Result.failure(ServerException("No album details returned"))
                    }
                } else {
                    Result.failure(ServerException("Failed to fetch album details"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }

    override suspend fun searchAll(query: String): Result<SearchResults> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.search(mapOf("query" to query))
                if (response.isSuccessful) {
                    val body = response.body() ?: emptyMap()
                    val gson = com.google.gson.Gson()
                    val songs = parseSearchList<com.musify.data.models.Song>(gson, body["songs"])
                        .map { it.toDomainModel() }
                    val artists = parseSearchList<com.musify.data.models.Artist>(gson, body["artists"])
                        .map { it.toDomainModel() }
                    val albums = parseSearchList<com.musify.data.models.Album>(gson, body["albums"])
                        .map { it.toDomainModel() }
                    Result.success(SearchResults(songs = songs, artists = artists, albums = albums))
                } else {
                    Result.failure(ServerException("Search failed"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }

    override suspend fun getTrending(limit: Int): Result<TrendingResults> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTrending(limit = limit)
                if (response.isSuccessful) {
                    val body = response.body() ?: emptyMap()
                    val gson = com.google.gson.Gson()
                    val songs = parseSearchList<com.musify.data.models.Song>(gson, body["songs"])
                        .map { it.toDomainModel() }
                    val artists = parseSearchList<com.musify.data.models.Artist>(gson, body["artists"])
                        .map { it.toDomainModel() }
                    Result.success(TrendingResults(songs = songs, artists = artists))
                } else {
                    Result.failure(ServerException("Failed to load trending"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }

    override suspend fun getLikedSongs(userId: Int, limit: Int, offset: Int): Result<List<Song>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getLikedSongs(userId, limit, offset)
                if (response.isSuccessful) {
                    val songs = response.body()?.map { it.toDomainModel() } ?: emptyList()
                    Result.success(songs)
                } else {
                    Result.failure(ServerException("Failed to fetch liked songs"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }

    override suspend fun getFollowedArtists(userId: Int): Result<List<Artist>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getFollowedArtists(userId)
                if (response.isSuccessful) {
                    val artists = response.body()?.map { it.toDomainModel() } ?: emptyList()
                    Result.success(artists)
                } else {
                    Result.failure(ServerException("Failed to fetch followed artists"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }

    override suspend fun searchRaw(query: String): Result<RawSearchResults> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.search(mapOf("query" to query))
                if (response.isSuccessful) {
                    val body = response.body() ?: emptyMap()
                    val gson = com.google.gson.Gson()
                    val songs = parseSearchList<com.musify.data.models.Song>(gson, body["songs"])
                    val artists = parseSearchList<com.musify.data.models.Artist>(gson, body["artists"])
                    val albums = parseSearchList<com.musify.data.models.Album>(gson, body["albums"])
                    Result.success(RawSearchResults(songs = songs, artists = artists, albums = albums))
                } else {
                    Result.failure(ServerException("Search failed"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }

    override suspend fun getTrendingRaw(limit: Int): Result<RawTrendingResults> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTrending(limit = limit)
                if (response.isSuccessful) {
                    val body = response.body() ?: emptyMap()
                    val gson = com.google.gson.Gson()
                    val songs = parseSearchList<com.musify.data.models.Song>(gson, body["songs"])
                    val artists = parseSearchList<com.musify.data.models.Artist>(gson, body["artists"])
                    Result.success(RawTrendingResults(songs = songs, artists = artists))
                } else {
                    Result.failure(ServerException("Failed to load trending"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }

    override suspend fun getCurrentUserPlaylistsRaw(): Result<List<com.musify.data.models.Playlist>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCurrentUserPlaylists()
                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    Result.failure(ServerException("Failed to fetch playlists"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }

    override suspend fun getFollowedPlaylistsRaw(): Result<List<com.musify.data.models.Playlist>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getFollowedPlaylists()
                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    Result.failure(ServerException("Failed to fetch followed playlists"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }

    override suspend fun getLikedSongsRaw(userId: Int, limit: Int, offset: Int): Result<List<com.musify.data.models.Song>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getLikedSongs(userId, limit, offset)
                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    Result.failure(ServerException("Failed to fetch liked songs"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }

    override suspend fun getFollowedArtistsRaw(userId: Int): Result<List<com.musify.data.models.Artist>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getFollowedArtists(userId)
                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    Result.failure(ServerException("Failed to fetch followed artists"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }

    override suspend fun createPlaylistRaw(name: String, description: String?, isPublic: Boolean): Result<com.musify.data.models.Playlist> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createPlaylist(
                    com.musify.data.models.CreatePlaylistRequest(
                        name = name,
                        description = description,
                        isPublic = isPublic
                    )
                )
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(ServerException("Empty response"))
                } else {
                    Result.failure(ServerException("Failed to create playlist"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }

    override suspend fun getArtistDetailsRaw(artistId: Int): Result<com.musify.data.models.Artist> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getArtistDetails(artistId)
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(ServerException("Empty response"))
                } else {
                    Result.failure(ServerException("Failed to fetch artist details"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }

    override suspend fun getArtistSongsRaw(artistId: Int, sort: String, limit: Int): Result<List<com.musify.data.models.Song>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getArtistSongs(artistId, sort = sort, limit = limit)
                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    Result.failure(ServerException("Failed to fetch artist songs"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }

    override suspend fun getArtistAlbumsRaw(artistId: Int): Result<List<com.musify.data.models.Album>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getArtistAlbums(artistId)
                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    Result.failure(ServerException("Failed to fetch artist albums"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }

    override suspend fun followArtistRaw(artistId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.followArtist(artistId)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(ServerException("Failed to follow artist"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }

    override suspend fun unfollowArtistRaw(artistId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.unfollowArtist(artistId)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(ServerException("Failed to unfollow artist"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }

    override suspend fun getPlaylistDetailsRaw(playlistId: Int): Result<com.musify.data.models.PlaylistDetails> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getPlaylistDetails(playlistId)
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(ServerException("Empty response"))
                } else {
                    Result.failure(ServerException("Failed to load playlist"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }

    override suspend fun removeSongFromPlaylistRaw(playlistId: Int, songId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.removeSongFromPlaylist(playlistId, songId)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(ServerException("Failed to remove song from playlist"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }

    private inline fun <reified T> parseSearchList(gson: com.google.gson.Gson, data: Any?): List<T> {
        if (data == null) return emptyList()
        val json = gson.toJson(data)
        val type = com.google.gson.reflect.TypeToken.getParameterized(List::class.java, T::class.java).type
        return gson.fromJson(json, type) ?: emptyList()
    }
}