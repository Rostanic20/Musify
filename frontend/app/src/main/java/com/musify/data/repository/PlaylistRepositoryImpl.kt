package com.musify.data.repository

import com.musify.data.api.MusifyApiService
import com.musify.data.mapper.SongMapper.toDomainModel
import com.musify.data.models.CreatePlaylistRequest
import com.musify.domain.entity.Playlist
import com.musify.domain.entity.Song
import com.musify.domain.exception.*
import com.musify.domain.repository.PlaylistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val apiService: MusifyApiService
) : PlaylistRepository {
    
    override suspend fun getUserPlaylists(userId: Int?): Result<List<Playlist>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = if (userId != null) {
                    apiService.getUserPlaylists(userId)
                } else {
                    apiService.getCurrentUserPlaylists()
                }
                
                if (response.isSuccessful) {
                    val playlists = response.body()?.map { it.toDomainModel() } ?: emptyList()
                    Result.success(playlists)
                } else {
                    Result.failure(ServerException("Failed to fetch playlists"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun getPlaylistById(playlistId: Int): Result<Playlist?> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getPlaylistDetails(playlistId)
                if (response.isSuccessful) {
                    Result.success(response.body()?.playlist?.toDomainModel())
                } else if (response.code() == 404) {
                    Result.failure(PlaylistNotFoundException())
                } else {
                    Result.failure(ServerException("Failed to fetch playlist"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun getPlaylistSongs(playlistId: Int): Result<List<Song>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getPlaylistDetails(playlistId)
                if (response.isSuccessful) {
                    val songs = response.body()?.songs?.map { it.toDomainModel() } ?: emptyList()
                    Result.success(songs)
                } else if (response.code() == 404) {
                    Result.failure(PlaylistNotFoundException())
                } else {
                    Result.failure(ServerException("Failed to fetch playlist songs"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun createPlaylist(
        name: String,
        description: String?,
        isPublic: Boolean
    ): Result<Playlist> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createPlaylist(
                    CreatePlaylistRequest(
                        name = name,
                        description = description,
                        isPublic = isPublic
                    )
                )
                
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it.toDomainModel())
                    } ?: Result.failure(ServerException("Empty response"))
                } else {
                    Result.failure(ServerException("Failed to create playlist"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun updatePlaylist(
        playlistId: Int,
        name: String?,
        description: String?,
        isPublic: Boolean?
    ): Result<Playlist> {
        return withContext(Dispatchers.IO) {
            try {
                val updates = mutableMapOf<String, Any>()
                name?.let { updates["name"] = it }
                description?.let { updates["description"] = it }
                isPublic?.let { updates["isPublic"] = it }
                
                val response = apiService.updatePlaylist(playlistId, updates)
                
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it.toDomainModel())
                    } ?: Result.failure(ServerException("Empty response"))
                } else if (response.code() == 404) {
                    Result.failure(PlaylistNotFoundException())
                } else {
                    Result.failure(ServerException("Failed to update playlist"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun deletePlaylist(playlistId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deletePlaylist(playlistId)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else if (response.code() == 404) {
                    Result.failure(PlaylistNotFoundException())
                } else {
                    Result.failure(ServerException("Failed to delete playlist"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun addSongToPlaylist(playlistId: Int, songId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.addSongToPlaylist(
                    playlistId,
                    mapOf("songId" to songId)
                )
                
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else if (response.code() == 404) {
                    Result.failure(PlaylistNotFoundException())
                } else if (response.code() == 400) {
                    Result.failure(SongAlreadyInPlaylistException())
                } else {
                    Result.failure(ServerException("Failed to add song to playlist"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun removeSongFromPlaylist(playlistId: Int, songId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.removeSongFromPlaylist(playlistId, songId)
                
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else if (response.code() == 404) {
                    Result.failure(PlaylistNotFoundException())
                } else {
                    Result.failure(ServerException("Failed to remove song from playlist"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun followPlaylist(playlistId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.followPlaylist(playlistId)
                
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else if (response.code() == 404) {
                    Result.failure(PlaylistNotFoundException())
                } else {
                    Result.failure(ServerException("Failed to follow playlist"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun unfollowPlaylist(playlistId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.unfollowPlaylist(playlistId)
                
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else if (response.code() == 404) {
                    Result.failure(PlaylistNotFoundException())
                } else {
                    Result.failure(ServerException("Failed to unfollow playlist"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun reorderPlaylistSongs(
        playlistId: Int,
        fromPosition: Int,
        toPosition: Int
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Result.failure(NotImplementedError("Playlist reordering not yet available"))
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override fun getFollowedPlaylists(): Flow<List<Playlist>> {
        return flow {
            try {
                val response = apiService.getFollowedPlaylists()
                if (response.isSuccessful) {
                    val playlists = response.body()?.map { it.toDomainModel() } ?: emptyList()
                    emit(playlists)
                } else {
                    emit(emptyList())
                }
            } catch (e: Exception) {
                emit(emptyList())
            }
        }
    }
}