package com.musify.domain.usecase.auth

import com.musify.domain.repository.AuthRepository
import com.musify.utils.TokenManager
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(): Result<Unit> {
        val result = authRepository.logout()
        tokenManager.clearTokens()
        return result
    }
}