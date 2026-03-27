package com.musify.data.api

import com.musify.data.models.RefreshTokenRequest
import com.musify.utils.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val apiService: MusifyApiService
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header("Authorization-Retry") != null) {
            return null
        }

        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken == null) {
            runBlocking { tokenManager.clearTokens() }
            return null
        }

        return runBlocking {
            try {
                val refreshResponse = apiService.refreshToken(
                    RefreshTokenRequest(refreshToken)
                )

                if (refreshResponse.isSuccessful) {
                    refreshResponse.body()?.let { authResponse ->
                        tokenManager.saveTokens(
                            authResponse.token,
                            authResponse.refreshToken
                        )

                        return@runBlocking response.request.newBuilder()
                            .header("Authorization", "Bearer ${authResponse.token}")
                            .header("Authorization-Retry", "true")
                            .build()
                    }
                }

                tokenManager.clearTokens()
                null
            } catch (e: Exception) {
                tokenManager.clearTokens()
                null
            }
        }
    }
}
