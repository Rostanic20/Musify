package com.musify.domain.repository

import com.musify.domain.entity.AuthResult
import com.musify.domain.entity.User

/**
 * Repository interface for authentication operations
 * This interface belongs to the domain layer and defines the contract
 * that the data layer must implement
 */
interface AuthRepository {
    
    suspend fun login(email: String, password: String, totpCode: String? = null): Result<AuthResult>
    
    suspend fun register(
        email: String,
        phoneNumber: String,
        username: String,
        displayName: String,
        password: String,
        isArtist: Boolean = false,
        verificationType: String = "email"
    ): Result<AuthResult>
    
    suspend fun refreshToken(refreshToken: String): Result<AuthResult>
    
    suspend fun logout(): Result<Unit>
    
    suspend fun getCurrentUser(): Result<User?>
    
    suspend fun verifyEmail(token: String): Result<Unit>

    suspend fun resendVerificationEmail(email: String): Result<Unit>

    suspend fun verifySMS(code: String, phoneNumber: String): Result<Unit>

    suspend fun resendVerificationSMS(phoneNumber: String): Result<Unit>
    
    suspend fun requestPasswordReset(email: String): Result<Unit>
    
    suspend fun resetPassword(token: String, newPassword: String): Result<Unit>
    
    suspend fun enable2FA(password: String): Result<Pair<String, String>>
    
    suspend fun disable2FA(totpCode: String): Result<Unit>
    
    suspend fun verify2FA(totpCode: String): Result<Unit>
}