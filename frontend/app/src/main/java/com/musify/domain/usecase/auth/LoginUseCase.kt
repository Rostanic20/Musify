package com.musify.domain.usecase.auth

import com.musify.domain.entity.AuthResult
import com.musify.domain.exception.ValidationException
import com.musify.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for user login
 * Encapsulates the business logic for authentication
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        usernameOrEmail: String,
        password: String,
        totpCode: String? = null
    ): Result<AuthResult> {
        if (usernameOrEmail.isBlank()) {
            return Result.failure(ValidationException("Username cannot be empty"))
        }

        if (password.isBlank()) {
            return Result.failure(ValidationException("Password cannot be empty"))
        }

        return authRepository.login(usernameOrEmail, password, totpCode)
    }
}