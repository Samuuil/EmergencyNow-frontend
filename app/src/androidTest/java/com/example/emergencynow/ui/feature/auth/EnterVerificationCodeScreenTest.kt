package com.example.emergencynow.ui.feature.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import com.example.emergencynow.ui.BaseComposeTest
import com.example.emergencynow.ui.theme.EmergencyNowTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test

class EnterVerificationCodeScreenTest : BaseComposeTest() {

    private val state = MutableStateFlow(VerifyCodeUIState())
    private lateinit var viewModel: VerifyCodeViewModel

    @Before
    fun setUp() {
        viewModel = mockk(relaxed = true)
        every { viewModel.state } returns state
    }

    @Test
    fun verifyYourNumberHeadingIsDisplayed() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                EnterVerificationCodeScreen(egn = "1234567890", onBack = {}, onVerified = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Verify\nYour Number").assertIsDisplayed()
    }

    @Test
    fun verifyButtonDisabledWhenCodeIsEmpty() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                EnterVerificationCodeScreen(egn = "1234567890", onBack = {}, onVerified = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Verify").assertIsNotEnabled()
    }

    @Test
    fun resendCodeButtonIsDisplayed() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                EnterVerificationCodeScreen(egn = "1234567890", onBack = {}, onVerified = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Resend Code").assertIsDisplayed()
    }

    @Test
    fun verifyButtonDisabledWhenLoading() {
        state.value = VerifyCodeUIState(isLoading = true)
        composeTestRule.setContent {
            EmergencyNowTheme {
                EnterVerificationCodeScreen(egn = "1234567890", onBack = {}, onVerified = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Verify").assertDoesNotExist()
    }
}
