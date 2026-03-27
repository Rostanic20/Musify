package com.musify.data.repository

import com.musify.data.api.MusifyApiService
import com.musify.data.mapper.UserMapper.toDomainModel
import com.musify.domain.entity.User
import com.musify.domain.entity.UserProfile
import com.musify.domain.exception.*
import com.musify.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: MusifyApiService
) : UserRepository {
    
    override suspend fun getUserProfile(userId: Int): Result<UserProfile?> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUserProfile(userId)
                if (response.isSuccessful) {
                    response.body()?.let { profileData ->
                        Result.success(
                            UserProfile(
                                user = profileData.user.toDomainModel(),
                                followersCount = profileData.followersCount,
                                followingCount = profileData.followingCount,
                                playlistCount = profileData.playlistCount,
                                isFollowing = profileData.isFollowing
                            )
                        )
                    } ?: Result.failure(ServerException("Empty response"))
                } else if (response.code() == 404) {
                    Result.failure(UserNotFoundException())
                } else {
                    Result.failure(ServerException("Failed to fetch user profile"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun updateProfile(
        displayName: String?,
        bio: String?,
        profilePictureUrl: String?
    ): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val updates = mutableMapOf<String, Any>()
                displayName?.let { updates["displayName"] = it }
                bio?.let { updates["bio"] = it }
                profilePictureUrl?.let { updates["profilePicture"] = it }
                
                val response = apiService.updateProfile(updates)
                
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it.toDomainModel())
                    } ?: Result.failure(ServerException("Empty response"))
                } else {
                    Result.failure(ServerException("Failed to update profile"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.changePassword(
                    mapOf(
                        "currentPassword" to currentPassword,
                        "newPassword" to newPassword
                    )
                )
                
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else if (response.code() == 401) {
                    Result.failure(InvalidCredentialsException())
                } else if (response.code() == 400) {
                    Result.failure(ValidationException("Invalid password"))
                } else {
                    Result.failure(ServerException("Failed to change password"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun deleteAccount(password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteAccount(
                    mapOf("password" to password)
                )
                
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else if (response.code() == 401) {
                    Result.failure(InvalidCredentialsException())
                } else {
                    Result.failure(ServerException("Failed to delete account"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun getFollowers(userId: Int): Result<List<User>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUserFollowers(userId)
                if (response.isSuccessful) {
                    val users = response.body()?.map { it.toDomainModel() } ?: emptyList()
                    Result.success(users)
                } else {
                    Result.failure(ServerException("Failed to fetch followers"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun getFollowing(userId: Int): Result<List<User>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUserFollowing(userId)
                if (response.isSuccessful) {
                    val users = response.body()?.map { it.toDomainModel() } ?: emptyList()
                    Result.success(users)
                } else {
                    Result.failure(ServerException("Failed to fetch following"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun followUser(userId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.followUser(userId)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else if (response.code() == 404) {
                    Result.failure(UserNotFoundException())
                } else {
                    Result.failure(ServerException("Failed to follow user"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun unfollowUser(userId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.unfollowUser(userId)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else if (response.code() == 404) {
                    Result.failure(UserNotFoundException())
                } else {
                    Result.failure(ServerException("Failed to unfollow user"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun searchUsers(query: String, limit: Int): Result<List<User>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.search(
                    mapOf(
                        "query" to query,
                        "type" to "users",
                        "limit" to limit
                    )
                )
                
                if (response.isSuccessful) {
                    Result.success(emptyList())
                } else {
                    Result.failure(ServerException("Search failed"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun getSubscriptionStatus(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCurrentUser()
                if (response.isSuccessful) {
                    Result.success(response.body()?.isPremium ?: false)
                } else {
                    Result.failure(ServerException("Failed to get subscription status"))
                }
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun getPrivacySettings(): Result<Map<String, Boolean>> {
        return withContext(Dispatchers.IO) {
            try {
                Result.success(
                    mapOf(
                        "profilePublic" to true,
                        "playlistsPublic" to true,
                        "listeningActivityPublic" to false
                    )
                )
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
    
    override suspend fun updatePrivacySettings(settings: Map<String, Boolean>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(NetworkException(e.message ?: "Network error"))
            }
        }
    }
}