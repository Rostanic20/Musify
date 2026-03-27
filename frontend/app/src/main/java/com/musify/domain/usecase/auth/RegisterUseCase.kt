package com.musify.domain.usecase.auth

import com.musify.domain.entity.AuthResult
import com.musify.domain.exception.ValidationException
import com.musify.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for user registration
 */
class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        phoneNumber: String,
        username: String,
        displayName: String,
        password: String,
        confirmPassword: String,
        isArtist: Boolean = false,
        verificationType: String = "email"
    ): Result<AuthResult> {
        when (verificationType) {
            "email" -> {
                if (email.isBlank()) {
                    return Result.failure(ValidationException("Email cannot be empty"))
                }
                if (!isValidEmail(email)) {
                    return Result.failure(ValidationException("Invalid email format"))
                }
            }
            "sms" -> {
                if (phoneNumber.isBlank()) {
                    return Result.failure(ValidationException("Phone number cannot be empty"))
                }
                if (phoneNumber.length < 10) {
                    return Result.failure(ValidationException("Invalid phone number"))
                }
            }
            else -> {
                return Result.failure(ValidationException("Invalid verification type"))
            }
        }

        if (username.isBlank()) {
            return Result.failure(ValidationException("Username cannot be empty"))
        }

        if (username.length < 3) {
            return Result.failure(ValidationException("Username must be at least 3 characters"))
        }

        if (!isValidUsername(username)) {
            return Result.failure(ValidationException("Username can only contain letters, numbers, and underscores"))
        }

        if (displayName.isBlank()) {
            return Result.failure(ValidationException("Display name cannot be empty"))
        }

        if (password.length < 8) {
            return Result.failure(ValidationException("Password must be at least 8 characters"))
        }

        if (password != confirmPassword) {
            return Result.failure(ValidationException("Passwords do not match"))
        }

        return authRepository.register(
            email = email,
            phoneNumber = phoneNumber,
            username = username,
            displayName = displayName,
            password = password,
            isArtist = isArtist,
            verificationType = verificationType
        )
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    private fun isValidUsername(username: String): Boolean {
        return username.matches(Regex("^[a-zA-Z0-9_]+$"))
    }
}