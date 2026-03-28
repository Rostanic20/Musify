package com.musify.ui.screens.profile

import com.musify.domain.entity.User
import com.musify.domain.exception.NetworkException
import com.musify.domain.exception.UnauthorizedException
import com.musify.domain.usecase.auth.GetCurrentUserUseCase
import com.musify.domain.usecase.auth.LogoutUseCase
import com.musify.utils.TokenManager
import io.mockk.coEvery
import io.mockk.mockk
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
class ProfileViewModelTest {

    private val getCurrentUserUseCase = mockk<GetCurrentUserUseCase>()
    private val logoutUseCase = mockk<LogoutUseCase>()
    private val tokenManager = mockk<TokenManager>(relaxed = true)

    private val testUser = User(
        id = 1,
        email = "test@example.com",
        username = "testuser",
        displayName = "Test User",
        bio = "Test bio",
        profilePictureUrl = "https://example.com/pic.jpg",
        isPremium = true,
        isVerified = true,
        isEmailVerified = true,
        has2FAEnabled = false,
        isArtist = false
    )

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ProfileViewModel {
        return ProfileViewModel(getCurrentUserUseCase, logoutUseCase, tokenManager)
    }

    @Test
    fun `init loads current user successfully`() = runTest {
        coEvery { getCurrentUserUseCase() } returns Result.success(testUser)

        val viewModel = createViewModel()
        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertEquals(testUser, state.user)
        assertEquals("Test User", state.user?.displayName)
        assertEquals("test@example.com", state.user?.email)
    }

    @Test
    fun `init handles user load failure`() = runTest {
        coEvery { getCurrentUserUseCase() } returns Result.failure(NetworkException("No connection"))

        val viewModel = createViewModel()
        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertNull(state.user)
    }

    @Test
    fun `init handles unauthorized error`() = runTest {
        coEvery { getCurrentUserUseCase() } returns Result.failure(UnauthorizedException())

        val viewModel = createViewModel()
        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertNull(state.user)
    }

    @Test
    fun `logout clears tokens and sets isLoggedOut`() = runTest {
        coEvery { getCurrentUserUseCase() } returns Result.success(testUser)
        coEvery { logoutUseCase() } returns Result.success(Unit)

        val viewModel = createViewModel()
        viewModel.logout()

        val state = viewModel.state.value
        assertTrue(state.isLoggedOut)
        verify { tokenManager.clearTokens() }
    }

    @Test
    fun `logout clears tokens even on failure`() = runTest {
        coEvery { getCurrentUserUseCase() } returns Result.success(testUser)
        coEvery { logoutUseCase() } returns Result.failure(NetworkException("error"))

        val viewModel = createViewModel()
        viewModel.logout()

        val state = viewModel.state.value
        assertTrue(state.isLoggedOut)
        verify { tokenManager.clearTokens() }
    }

    @Test
    fun `loading state is false after init`() = runTest {
        coEvery { getCurrentUserUseCase() } returns Result.success(testUser)

        val viewModel = createViewModel()

        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `initial state before init loads has no user`() = runTest {
        coEvery { getCurrentUserUseCase() } returns Result.success(null)

        val viewModel = createViewModel()
        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertNull(state.user)
        assertFalse(state.isLoggedOut)
    }
}
