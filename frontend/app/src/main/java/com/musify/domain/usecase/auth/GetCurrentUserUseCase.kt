package com.musify.domain.usecase.auth

import com.musify.domain.entity.User
import com.musify.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for getting the current logged-in user
 */
class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<User?> {
        return authRepository.getCurrentUser()
    }
}