package com.musify.ui.screens.auth

import com.musify.domain.entity.User
import com.musify.domain.repository.AuthRepository
import com.musify.domain.usecase.auth.GetCurrentUserUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class VerificationViewModelTest {

    @Mock
    private lateinit var authRepository: AuthRepository

    @Mock
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase

    private lateinit var viewModel: VerificationViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = VerificationViewModel(authRepository, getCurrentUserUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `checkVerificationStatus should update state when user is verified`() = runTest {
        // Given
        val verifiedUser = createMockUser(isEmailVerified = true)
        `when`(getCurrentUserUseCase()).thenReturn(Result.success(verifiedUser))

        // When
        viewModel.checkVerificationStatus()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertTrue(state.isVerified)
        assertEquals("Email verified successfully!", state.message)
        assertFalse(state.isError)
    }

    @Test
    fun `checkVerificationStatus should not update state when user is not verified`() = runTest {
        // Given
        val unverifiedUser = createMockUser(isEmailVerified = false)
        `when`(getCurrentUserUseCase()).thenReturn(Result.success(unverifiedUser))

        // When
        viewModel.checkVerificationStatus()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isVerified)
        assertNull(state.message)
    }

    @Test
    fun `checkVerificationStatus should handle null user silently`() = runTest {
        // Given
        `when`(getCurrentUserUseCase()).thenReturn(Result.success(null))

        // When
        viewModel.checkVerificationStatus()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isVerified)
        assertNull(state.message)
    }

    @Test
    fun `checkVerificationStatus should handle failure silently`() = runTest {
        // Given
        `when`(getCurrentUserUseCase()).thenReturn(Result.failure(Exception("Network error")))

        // When
        viewModel.checkVerificationStatus()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isVerified)
        assertNull(state.message)
    }

    @Test
    fun `verifySMS should update state to verified on success`() = runTest {
        // Given
        val code = "123456"
        val phoneNumber = "+1234567890"
        `when`(authRepository.verifySMS(code, phoneNumber)).thenReturn(Result.success(Unit))

        // When
        viewModel.verifySMS(code, phoneNumber)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.isVerified)
        assertEquals("Phone number verified successfully!", state.message)
        assertFalse(state.isError)
    }

    @Test
    fun `verifySMS should show error message on failure`() = runTest {
        // Given
        val code = "123456"
        val phoneNumber = "+1234567890"
        val errorMessage = "Invalid verification code"
        `when`(authRepository.verifySMS(code, phoneNumber))
            .thenReturn(Result.failure(Exception(errorMessage)))

        // When
        viewModel.verifySMS(code, phoneNumber)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isVerified)
        assertEquals(errorMessage, state.message)
        assertTrue(state.isError)
    }

    @Test
    fun `verifySMS should set loading state while verifying`() = runTest {
        // Given
        val code = "123456"
        val phoneNumber = "+1234567890"
        `when`(authRepository.verifySMS(code, phoneNumber)).thenReturn(Result.success(Unit))

        // When
        viewModel.verifySMS(code, phoneNumber)

        // Then (before completion)
        val initialState = viewModel.state.value
        assertTrue(initialState.isLoading)
        assertNull(initialState.message)
        assertFalse(initialState.isError)
    }

    @Test
    fun `resendVerificationEmail should send email and start cooldown on success`() = runTest {
        // Given
        val email = "test@example.com"
        `when`(authRepository.resendVerificationEmail(email)).thenReturn(Result.success(Unit))

        // When
        viewModel.resendVerificationEmail(email)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isResending)
        assertEquals("Verification email sent!", state.message)
        assertFalse(state.isError)
        assertEquals(60, state.resendCooldown)
    }

    @Test
    fun `resendVerificationEmail should show error on failure`() = runTest {
        // Given
        val email = "test@example.com"
        val errorMessage = "Email service unavailable"
        `when`(authRepository.resendVerificationEmail(email))
            .thenReturn(Result.failure(Exception(errorMessage)))

        // When
        viewModel.resendVerificationEmail(email)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isResending)
        assertEquals(errorMessage, state.message)
        assertTrue(state.isError)
    }

    @Test
    fun `resendVerificationEmail should decrement cooldown timer`() = runTest {
        // Given
        val email = "test@example.com"
        `when`(authRepository.resendVerificationEmail(email)).thenReturn(Result.success(Unit))

        // When
        viewModel.resendVerificationEmail(email)
        testDispatcher.scheduler.advanceUntilIdle()

        // Initial cooldown
        assertEquals(60, viewModel.state.value.resendCooldown)

        // Advance time by 1 second
        testDispatcher.scheduler.advanceTimeBy(1000)
        testDispatcher.scheduler.runCurrent()

        // Then
        assertEquals(59, viewModel.state.value.resendCooldown)
    }

    @Test
    fun `resendVerificationSMS should send SMS and start cooldown on success`() = runTest {
        // Given
        val phoneNumber = "+1234567890"
        `when`(authRepository.resendVerificationSMS(phoneNumber)).thenReturn(Result.success(Unit))

        // When
        viewModel.resendVerificationSMS(phoneNumber)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isResending)
        assertEquals("Verification code sent!", state.message)
        assertFalse(state.isError)
        assertEquals(60, state.resendCooldown)
    }

    @Test
    fun `resendVerificationSMS should show error on failure`() = runTest {
        // Given
        val phoneNumber = "+1234567890"
        val errorMessage = "SMS service unavailable"
        `when`(authRepository.resendVerificationSMS(phoneNumber))
            .thenReturn(Result.failure(Exception(errorMessage)))

        // When
        viewModel.resendVerificationSMS(phoneNumber)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isResending)
        assertEquals(errorMessage, state.message)
        assertTrue(state.isError)
    }

    @Test
    fun `cooldown timer should reach zero after 60 seconds`() = runTest {
        // Given
        val email = "test@example.com"
        `when`(authRepository.resendVerificationEmail(email)).thenReturn(Result.success(Unit))

        // When
        viewModel.resendVerificationEmail(email)
        testDispatcher.scheduler.advanceUntilIdle()

        // Advance time by 60 seconds
        testDispatcher.scheduler.advanceTimeBy(60000)
        testDispatcher.scheduler.runCurrent()

        // Then
        assertEquals(0, viewModel.state.value.resendCooldown)
    }

    @Test
    fun `initial state should be default`() {
        // Then
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isResending)
        assertFalse(state.isVerified)
        assertFalse(state.isError)
        assertNull(state.message)
        assertEquals(0, state.resendCooldown)
    }

    private fun createMockUser(
        id: Int = 1,
        email: String = "test@example.com",
        username: String = "testuser",
        displayName: String = "Test User",
        isEmailVerified: Boolean = false,
        isPremium: Boolean = false
    ) = User(
        id = id,
        email = email,
        username = username,
        displayName = displayName,
        bio = null,
        profilePictureUrl = null,
        isPremium = isPremium,
        isVerified = false,
        isEmailVerified = isEmailVerified,
        has2FAEnabled = false,
        isArtist = false
    )
}
