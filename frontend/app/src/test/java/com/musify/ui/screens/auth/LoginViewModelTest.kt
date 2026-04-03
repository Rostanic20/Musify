package com.musify.ui.screens.auth

import com.musify.domain.entity.AuthResult
import com.musify.domain.entity.User
import com.musify.domain.exception.InvalidCredentialsException
import com.musify.domain.exception.ValidationException
import com.musify.domain.usecase.auth.LoginUseCase
import com.musify.utils.TokenManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel
    private val loginUseCase = mockk<LoginUseCase>()
    private val tokenManager = mockk<TokenManager>(relaxed = true)

    private val testUser = User(
        id = 1,
        email = "test@example.com",
        username = "testuser",
        displayName = "Test User",
        bio = null,
        profilePictureUrl = null,
        isPremium = false,
        isVerified = true,
        isEmailVerified = true,
        has2FAEnabled = false,
        isArtist = false
    )

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = LoginViewModel(loginUseCase, tokenManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loginWithValidCredentialsSetsIsLoginSuccessfulToTrue() = runTest {
        val authResult = AuthResult(
            user = testUser,
            accessToken = "access-token-123",
            refreshToken = "refresh-token-456",
            requires2FA = false
        )
        coEvery { loginUseCase(any(), any(), any()) } returns Result.success(authResult)

        viewModel.login("testuser", "password123")

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.isLoginSuccessful)
        assertNull(state.errorMessage)
        assertFalse(state.requires2FA)
    }

    @Test
    fun loginWithValidCredentialsSavesTokens() = runTest {
        val authResult = AuthResult(
            user = testUser,
            accessToken = "access-token-123",
            refreshToken = "refresh-token-456",
            requires2FA = false
        )
        coEvery { loginUseCase(any(), any(), any()) } returns Result.success(authResult)

        viewModel.login("testuser", "password123")

        verify { tokenManager.saveTokens("access-token-123", "refresh-token-456") }
    }

    @Test
    fun loginWithInvalidCredentialsSetsErrorMessage() = runTest {
        coEvery { loginUseCase(any(), any(), any()) } returns
            Result.failure(InvalidCredentialsException())

        viewModel.login("wrong", "wrong")

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isLoginSuccessful)
        assertEquals("Invalid username or password", state.errorMessage)
    }

    @Test
    fun loginWithEmptyUsernameSetsValidationError() = runTest {
        coEvery { loginUseCase(any(), any(), any()) } returns
            Result.failure(ValidationException("Username cannot be empty"))

        viewModel.login("", "password123")

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isLoginSuccessful)
        assertEquals("Username cannot be empty", state.errorMessage)
    }

    @Test
    fun loginWithEmptyPasswordSetsValidationError() = runTest {
        coEvery { loginUseCase(any(), any(), any()) } returns
            Result.failure(ValidationException("Password cannot be empty"))

        viewModel.login("testuser", "")

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isLoginSuccessful)
        assertEquals("Password cannot be empty", state.errorMessage)
    }

    @Test
    fun loginRequiring2FASetsRequires2FAToTrue() = runTest {
        val authResult = AuthResult(
            user = testUser,
            accessToken = "",
            refreshToken = null,
            requires2FA = true
        )
        coEvery { loginUseCase(any(), any(), any()) } returns Result.success(authResult)

        viewModel.login("testuser", "password123")

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isLoginSuccessful)
        assertTrue(state.requires2FA)
    }

    @Test
    fun loginWith2FASuccessSetsIsLoginSuccessfulToTrue() = runTest {
        val authResult = AuthResult(
            user = testUser,
            accessToken = "access-token-2fa",
            refreshToken = "refresh-token-2fa",
            requires2FA = false
        )
        coEvery { loginUseCase(any(), any(), any()) } returns Result.success(authResult)

        viewModel.loginWith2FA("testuser", "password123", "123456")

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.isLoginSuccessful)
        assertNull(state.errorMessage)
        verify { tokenManager.saveTokens("access-token-2fa", "refresh-token-2fa") }
    }

    @Test
    fun loginWith2FAWithInvalidCodeSetsErrorMessage() = runTest {
        coEvery { loginUseCase(any(), any(), any()) } returns
            Result.failure(Exception("Invalid 2FA code"))

        viewModel.loginWith2FA("testuser", "password123", "000000")

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isLoginSuccessful)
        assertEquals("Invalid 2FA code", state.errorMessage)
    }

    @Test
    fun loginClearsPreviousErrorBeforeMakingRequest() = runTest {
        coEvery { loginUseCase(any(), any(), any()) } returns
            Result.failure(InvalidCredentialsException())

        viewModel.login("wrong", "wrong")
        assertEquals("Invalid username or password", viewModel.state.value.errorMessage)

        val authResult = AuthResult(
            user = testUser,
            accessToken = "token",
            refreshToken = "refresh",
            requires2FA = false
        )
        coEvery { loginUseCase(any(), any(), any()) } returns Result.success(authResult)

        viewModel.login("testuser", "password123")
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun initialStateIsDefaultLoginState() {
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isLoginSuccessful)
        assertNull(state.errorMessage)
        assertFalse(state.requires2FA)
    }
}
