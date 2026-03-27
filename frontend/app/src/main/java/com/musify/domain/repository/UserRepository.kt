package com.musify.domain.repository

import com.musify.domain.entity.User
import com.musify.domain.entity.UserProfile

/**
 * Repository interface for user-related operations
 */
interface UserRepository {
    
    suspend fun getUserProfile(userId: Int): Result<UserProfile?>
    
    suspend fun updateProfile(
        displayName: String?,
        bio: String?,
        profilePictureUrl: String?
    ): Result<User>
    
    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Result<Unit>
    
    suspend fun deleteAccount(password: String): Result<Unit>
    
    suspend fun getFollowers(userId: Int): Result<List<User>>
    
    suspend fun getFollowing(userId: Int): Result<List<User>>
    
    suspend fun followUser(userId: Int): Result<Unit>
    
    suspend fun unfollowUser(userId: Int): Result<Unit>
    
    suspend fun searchUsers(query: String, limit: Int = 20): Result<List<User>>
    
    suspend fun getSubscriptionStatus(): Result<Boolean>
    
    suspend fun getPrivacySettings(): Result<Map<String, Boolean>>
    
    suspend fun updatePrivacySettings(settings: Map<String, Boolean>): Result<Unit>
}