package com.musify.data.mapper

import com.musify.data.models.AuthResponse as DataAuthResponse
import com.musify.data.models.User as DataUser
import com.musify.domain.entity.AuthResult
import com.musify.domain.entity.User

/**
 * Mapper to convert between data layer and domain layer user models
 */
object UserMapper {
    
    fun DataUser.toDomainModel(): User {
        return User(
            id = id,
            email = email,
            username = username,
            displayName = displayName,
            bio = bio,
            profilePictureUrl = profilePicture,
            isPremium = isPremium,
            isVerified = isVerified,
            isEmailVerified = emailVerified,
            has2FAEnabled = twoFactorEnabled,
            isArtist = isArtist
        )
    }
    
    fun DataAuthResponse.toDomainModel(): AuthResult {
        return AuthResult(
            user = user.toDomainModel(),
            accessToken = token,
            refreshToken = refreshToken,
            requires2FA = requires2FA
        )
    }
}