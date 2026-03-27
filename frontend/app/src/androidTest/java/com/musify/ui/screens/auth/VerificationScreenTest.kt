package com.musify.ui.screens.auth

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.musify.domain.entity.User
import com.musify.domain.repository.AuthRepository
import com.musify.domain.usecase.auth.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class VerificationScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Mock
    private lateinit var authRepository: AuthRepository

    @Mock
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase

    private lateinit var viewModel: VerificationViewModel
    private var navigatedToLogin = false
    private var navigatedToSuccess = false

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = VerificationViewModel(authRepository, getCurrentUserUseCase)
        navigatedToLogin = false
        navigatedToSuccess = false
    }

    @Test
    fun emailVerificationScreen_displaysCorrectContent() {
        // Given
        val email = "test@example.com"

        // When
        composeTestRule.setContent {
            VerificationScreen(
                verificationData = "email:$email",
                onNavigateToLogin = { navigatedToLogin = true },
                onVerificationSuccess = { navigatedToSuccess = true },
                viewModel = viewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Verify Your Email").assertIsDisplayed()
        composeTestRule.onNodeWithText(email).assertIsDisplayed()
        composeTestRule.onNodeWithText("We've sent a verification link to").assertIsDisplayed()
        composeTestRule.onNodeWithText("Please check your inbox and click the link to verify your account.")
            .assertIsDisplayed()
    }

    @Test
    fun emailVerificationScreen_displaysOpenEmailButton() {
        // When
        composeTestRule.setContent {
            VerificationScreen(
                verificationData = "email:test@example.com",
                onNavigateToLogin = { navigatedToLogin = true },
                onVerificationSuccess = { navigatedToSuccess = true },
                viewModel = viewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Open Email App").assertIsDisplayed()
    }

    @Test
    fun emailVerificationScreen_displaysResendButton() {
        // When
        composeTestRule.setContent {
            VerificationScreen(
                verificationData = "email:test@example.com",
                onNavigateToLogin = { navigatedToLogin = true },
                onVerificationSuccess = { navigatedToSuccess = true },
                viewModel = viewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Resend Verification Email").assertIsDisplayed()
    }

    @Test
    fun emailVerificationScreen_displaysAlreadyVerifiedLink() {
        // When
        composeTestRule.setContent {
            VerificationScreen(
                verificationData = "email:test@example.com",
                onNavigateToLogin = { navigatedToLogin = true },
                onVerificationSuccess = { navigatedToSuccess = true },
                viewModel = viewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Already verified? Go to Login").assertIsDisplayed()
    }

    @Test
    fun emailVerificationScreen_clickingAlreadyVerified_navigatesToLogin() {
        // When
        composeTestRule.setContent {
            VerificationScreen(
                verificationData = "email:test@example.com",
                onNavigateToLogin = { navigatedToLogin = true },
                onVerificationSuccess = { navigatedToSuccess = true },
                viewModel = viewModel
            )
        }

        // When
        composeTestRule.onNodeWithText("Already verified? Go to Login").performClick()

        // Then
        assert(navigatedToLogin)
    }

    @Test
    fun smsVerificationScreen_displaysCorrectContent() {
        // Given
        val phoneNumber = "+1234567890"

        // When
        composeTestRule.setContent {
            VerificationScreen(
                verificationData = "sms:$phoneNumber",
                onNavigateToLogin = { navigatedToLogin = true },
                onVerificationSuccess = { navigatedToSuccess = true },
                viewModel = viewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Verify Your Phone Number").assertIsDisplayed()
        composeTestRule.onNodeWithText(phoneNumber).assertIsDisplayed()
        composeTestRule.onNodeWithText("We've sent a verification code to").assertIsDisplayed()
        composeTestRule.onNodeWithText("Enter the 6-digit code we sent you.").assertIsDisplayed()
    }

    @Test
    fun smsVerificationScreen_displaysCodeInputField() {
        // When
        composeTestRule.setContent {
            VerificationScreen(
                verificationData = "sms:+1234567890",
                onNavigateToLogin = { navigatedToLogin = true },
                onVerificationSuccess = { navigatedToSuccess = true },
                viewModel = viewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Verification Code").assertIsDisplayed()
    }

    @Test
    fun smsVerificationScreen_displaysVerifyButton() {
        // When
        composeTestRule.setContent {
            VerificationScreen(
                verificationData = "sms:+1234567890",
                onNavigateToLogin = { navigatedToLogin = true },
                onVerificationSuccess = { navigatedToSuccess = true },
                viewModel = viewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Verify").assertIsDisplayed()
    }

    @Test
    fun smsVerificationScreen_verifyButtonDisabled_whenCodeLessThan6Digits() {
        // When
        composeTestRule.setContent {
            VerificationScreen(
                verificationData = "sms:+1234567890",
                onNavigateToLogin = { navigatedToLogin = true },
                onVerificationSuccess = { navigatedToSuccess = true },
                viewModel = viewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Verify").assertIsNotEnabled()
    }

    @Test
    fun smsVerificationScreen_verifyButtonEnabled_whenCodeIs6Digits() {
        // When
        composeTestRule.setContent {
            VerificationScreen(
                verificationData = "sms:+1234567890",
                onNavigateToLogin = { navigatedToLogin = true },
                onVerificationSuccess = { navigatedToSuccess = true },
                viewModel = viewModel
            )
        }

        // Enter 6-digit code
        composeTestRule.onNodeWithText("Verification Code").performTextInput("123456")

        // Then
        composeTestRule.onNodeWithText("Verify").assertIsEnabled()
    }

    @Test
    fun smsVerificationScreen_codeInputField_limitsTo6Digits() {
        // When
        composeTestRule.setContent {
            VerificationScreen(
                verificationData = "sms:+1234567890",
                onNavigateToLogin = { navigatedToLogin = true },
                onVerificationSuccess = { navigatedToSuccess = true },
                viewModel = viewModel
            )
        }

        // Enter more than 6 digits
        composeTestRule.onNodeWithText("Verification Code").performTextInput("12345678")

        // Then - only 6 digits should be displayed
        composeTestRule.onNodeWithText("Verification Code").assertTextEquals("Verification Code", "123456")
    }

    @Test
    fun smsVerificationScreen_displaysResendSMSButton() {
        // When
        composeTestRule.setContent {
            VerificationScreen(
                verificationData = "sms:+1234567890",
                onNavigateToLogin = { navigatedToLogin = true },
                onVerificationSuccess = { navigatedToSuccess = true },
                viewModel = viewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Resend SMS Code").assertIsDisplayed()
    }

    @Test
    fun smsVerificationScreen_doesNotDisplayOpenEmailButton() {
        // When
        composeTestRule.setContent {
            VerificationScreen(
                verificationData = "sms:+1234567890",
                onNavigateToLogin = { navigatedToLogin = true },
                onVerificationSuccess = { navigatedToSuccess = true },
                viewModel = viewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Open Email App").assertDoesNotExist()
    }

    @Test
    fun verificationScreen_resendButton_showsCooldown_afterClick() {
        // Given
        `when`(authRepository.resendVerificationEmail(anyString()))
            .thenReturn(Result.success(Unit))

        // When
        composeTestRule.setContent {
            VerificationScreen(
                verificationData = "email:test@example.com",
                onNavigateToLogin = { navigatedToLogin = true },
                onVerificationSuccess = { navigatedToSuccess = true },
                viewModel = viewModel
            )
        }

        // Click resend button
        composeTestRule.onNodeWithText("Resend Verification Email").performClick()
        composeTestRule.waitForIdle()

        // Then - should show cooldown
        composeTestRule.onNodeWithText("Resend in 60s", substring = true).assertIsDisplayed()
    }

    @Test
    fun verificationScreen_displaysSuccessMessage_afterResend() {
        // Given
        `when`(authRepository.resendVerificationEmail(anyString()))
            .thenReturn(Result.success(Unit))

        // When
        composeTestRule.setContent {
            VerificationScreen(
                verificationData = "email:test@example.com",
                onNavigateToLogin = { navigatedToLogin = true },
                onVerificationSuccess = { navigatedToSuccess = true },
                viewModel = viewModel
            )
        }

        // Click resend
        composeTestRule.onNodeWithText("Resend Verification Email").performClick()
        composeTestRule.waitForIdle()

        // Then
        composeTestRule.onNodeWithText("Verification email sent!").assertIsDisplayed()
    }

    @Test
    fun verificationScreen_displaysErrorMessage_onResendFailure() {
        // Given
        val errorMessage = "Network error"
        `when`(authRepository.resendVerificationEmail(anyString()))
            .thenReturn(Result.failure(Exception(errorMessage)))

        // When
        composeTestRule.setContent {
            VerificationScreen(
                verificationData = "email:test@example.com",
                onNavigateToLogin = { navigatedToLogin = true },
                onVerificationSuccess = { navigatedToSuccess = true },
                viewModel = viewModel
            )
        }

        // Click resend
        composeTestRule.onNodeWithText("Resend Verification Email").performClick()
        composeTestRule.waitForIdle()

        // Then
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun verificationScreen_handlesInvalidFormat_defaultsToEmail() {
        // When - invalid format without colon
        composeTestRule.setContent {
            VerificationScreen(
                verificationData = "invalid-format",
                onNavigateToLogin = { navigatedToLogin = true },
                onVerificationSuccess = { navigatedToSuccess = true },
                viewModel = viewModel
            )
        }

        // Then - should default to email verification
        composeTestRule.onNodeWithText("Verify Your Email").assertIsDisplayed()
    }

    @Test
    fun verificationScreen_handlesEmptyVerificationData() {
        // When
        composeTestRule.setContent {
            VerificationScreen(
                verificationData = "",
                onNavigateToLogin = { navigatedToLogin = true },
                onVerificationSuccess = { navigatedToSuccess = true },
                viewModel = viewModel
            )
        }

        // Then - should still display email verification screen
        composeTestRule.onNodeWithText("Verify Your Email").assertIsDisplayed()
    }
}
