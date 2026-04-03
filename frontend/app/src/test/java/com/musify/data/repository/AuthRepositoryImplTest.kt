package com.musify.data.repository

import com.musify.data.api.MusifyApiService
import com.musify.data.models.AuthResponse
import com.musify.data.models.MessageResponse
import com.musify.data.models.User
import com.musify.domain.exception.EmailAlreadyExistsException
import com.musify.domain.exception.InvalidCredentialsException
import com.musify.domain.exception.NetworkException
import com.musify.domain.exception.ServerException
import com.musify.domain.exception.SessionExpiredException
import com.musify.domain.exception.TimeoutException
import com.musify.domain.exception.UnauthorizedException
import com.musify.domain.exception.UsernameAlreadyExistsException
import com.musify.domain.exception.ValidationException
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryImplTest {

    private lateinit var repository: AuthRepositoryImpl
    private val apiService = mockk<MusifyApiService>()

    private val dataUser = User(
        id = 1,
        email = "test@example.com",
        username = "testuser",
        displayName = "Test User",
        isPremium = false,
        isVerified = true,
        emailVerified = true,
        twoFactorEnabled = false,
        isArtist = false,
        createdAt = "2024-01-01T00:00:00",
        updatedAt = "2024-01-01T00:00:00"
    )

    private val authResponse = AuthResponse(
        token = "access-token-123",
        refreshToken = "refresh-token-456",
        user = dataUser,
        expiresIn = 3600,
        requires2FA = false,
        message = null
    )

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = AuthRepositoryImpl(apiService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loginSuccessReturnsAuthResultWithTokens() = runTest {
        coEvery { apiService.login(any()) } returns Response.success(authResponse)

        val result = repository.login("testuser", "password123")

        assertTrue(result.isSuccess)
        val authResult = result.getOrNull()!!
        assertEquals("access-token-123", authResult.accessToken)
        assertEquals("refresh-token-456", authResult.refreshToken)
        assertEquals("testuser", authResult.user.username)
        assertFalse(authResult.requires2FA)
    }

    @Test
    fun loginWith2FARequiredReturnsRequires2FATrue() = runTest {
        val twoFaResponse = authResponse.copy(requires2FA = true)
        coEvery { apiService.login(any()) } returns Response.success(twoFaResponse)

        val result = repository.login("testuser", "password123")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.requires2FA)
    }

    @Test
    fun loginWithInvalidCredentialsReturnsInvalidCredentialsException() = runTest {
        coEvery { apiService.login(any()) } returns
            Response.error(401, """{"error":"Invalid credentials"}""".toResponseBody())

        val result = repository.login("wrong", "wrong")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvalidCredentialsException)
    }

    @Test
    fun loginWithNetworkErrorReturnsNetworkException() = runTest {
        coEvery { apiService.login(any()) } throws IOException("Connection failed")

        val result = repository.login("testuser", "password123")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NetworkException)
    }

    @Test
    fun loginWithNoInternetReturnsNetworkExceptionWithMessage() = runTest {
        coEvery { apiService.login(any()) } throws UnknownHostException("Unable to resolve host")

        val result = repository.login("testuser", "password123")

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as NetworkException
        assertEquals("No internet connection", exception.message)
    }

    @Test
    fun loginWithTimeoutReturnsTimeoutException() = runTest {
        coEvery { apiService.login(any()) } throws SocketTimeoutException("Connection timed out")

        val result = repository.login("testuser", "password123")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is TimeoutException)
    }

    @Test
    fun registerSuccessReturnsAuthResult() = runTest {
        coEvery { apiService.register(any()) } returns Response.success(authResponse)

        val result = repository.register(
            email = "new@example.com",
            phoneNumber = "",
            username = "newuser",
            displayName = "New User",
            password = "password123"
        )

        assertTrue(result.isSuccess)
        val authResult = result.getOrNull()!!
        assertEquals("access-token-123", authResult.accessToken)
    }

    @Test
    fun registerWithExistingEmailReturnsEmailAlreadyExistsException() = runTest {
        coEvery { apiService.register(any()) } returns Response.error(
            409,
            """{"error":"email already registered"}""".toResponseBody()
        )

        val result = repository.register(
            email = "existing@example.com",
            phoneNumber = "",
            username = "newuser",
            displayName = "New User",
            password = "password123"
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is EmailAlreadyExistsException)
    }

    @Test
    fun registerWithExistingUsernameReturnsUsernameAlreadyExistsException() = runTest {
        coEvery { apiService.register(any()) } returns Response.error(
            409,
            """{"error":"username already taken"}""".toResponseBody()
        )

        val result = repository.register(
            email = "new@example.com",
            phoneNumber = "",
            username = "existinguser",
            displayName = "New User",
            password = "password123"
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is UsernameAlreadyExistsException)
    }

    @Test
    fun registerWithValidationErrorReturnsValidationException() = runTest {
        coEvery { apiService.register(any()) } returns Response.error(
            400,
            """{"error":"Password too short"}""".toResponseBody()
        )

        val result = repository.register(
            email = "new@example.com",
            phoneNumber = "",
            username = "newuser",
            displayName = "New User",
            password = "short"
        )

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as ValidationException
        assertEquals("Password too short", exception.message)
    }

    @Test
    fun logoutAlwaysSucceedsEvenOnAPIError() = runTest {
        coEvery { apiService.logout() } returns
            Response.error(500, "Error".toResponseBody())

        val result = repository.logout()

        assertTrue(result.isSuccess)
    }

    @Test
    fun logoutSucceedsOnNetworkFailure() = runTest {
        coEvery { apiService.logout() } throws IOException("No connection")

        val result = repository.logout()

        assertTrue(result.isSuccess)
    }

    @Test
    fun getCurrentUserReturnsMappedUserOnSuccess() = runTest {
        coEvery { apiService.getCurrentUser() } returns Response.success(dataUser)

        val result = repository.getCurrentUser()

        assertTrue(result.isSuccess)
        val user = result.getOrNull()!!
        assertEquals(1, user.id)
        assertEquals("testuser", user.username)
        assertEquals("Test User", user.displayName)
        assertTrue(user.isEmailVerified)
    }

    @Test
    fun getCurrentUserReturnsUnauthorizedExceptionOn401() = runTest {
        coEvery { apiService.getCurrentUser() } returns
            Response.error(401, "Unauthorized".toResponseBody())

        val result = repository.getCurrentUser()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is UnauthorizedException)
    }

    @Test
    fun getCurrentUserReturnsServerExceptionOn500() = runTest {
        coEvery { apiService.getCurrentUser() } returns
            Response.error(500, "Server error".toResponseBody())

        val result = repository.getCurrentUser()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ServerException)
    }

    @Test
    fun refreshTokenSuccessReturnsNewAuthResult() = runTest {
        val newAuthResponse = authResponse.copy(
            token = "new-access-token",
            refreshToken = "new-refresh-token"
        )
        coEvery { apiService.refreshToken(any()) } returns Response.success(newAuthResponse)

        val result = repository.refreshToken("old-refresh-token")

        assertTrue(result.isSuccess)
        val authResult = result.getOrNull()!!
        assertEquals("new-access-token", authResult.accessToken)
        assertEquals("new-refresh-token", authResult.refreshToken)
    }

    @Test
    fun refreshTokenFailureReturnsSessionExpiredException() = runTest {
        coEvery { apiService.refreshToken(any()) } returns
            Response.error(401, "Expired".toResponseBody())

        val result = repository.refreshToken("expired-token")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SessionExpiredException)
    }

    @Test
    fun loginWithEmptyResponseBodyReturnsServerException() = runTest {
        coEvery { apiService.login(any()) } returns Response.success(null)

        val result = repository.login("testuser", "password123")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ServerException)
    }

    @Test
    fun rateLimitErrorReturnsValidationException() = runTest {
        coEvery { apiService.login(any()) } returns Response.error(
            429,
            """{"error":"Rate limited"}""".toResponseBody()
        )

        val result = repository.login("testuser", "password123")

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as ValidationException
        assertEquals("Too many attempts. Please try again later.", exception.message)
    }
}
