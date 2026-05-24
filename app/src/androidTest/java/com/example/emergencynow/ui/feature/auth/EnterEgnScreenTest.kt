package com.example.emergencynow.ui.feature.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import com.example.emergencynow.ui.BaseComposeTest
import com.example.emergencynow.ui.theme.EmergencyNowTheme
import org.junit.Test

class EnterEgnScreenTest : BaseComposeTest() {

    @Test
    fun continueButtonDisabledWhenEgnIsEmpty() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                EnterEgnScreen(
                    onBack = {},
                    onContinue = {},
                    viewModel = EnterEgnViewModel()
                )
            }
        }
        composeTestRule.onNodeWithText("Continue").assertIsNotEnabled()
    }

    @Test
    fun continueButtonDisabledWhenEgnIsTooShort() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                EnterEgnScreen(
                    onBack = {},
                    onContinue = {},
                    viewModel = EnterEgnViewModel()
                )
            }
        }
        composeTestRule.onNode(hasSetTextAction()).performTextInput("123456789")
        composeTestRule.onNodeWithText("Continue").assertIsNotEnabled()
    }

    @Test
    fun continueButtonEnabledWhenEgnIsExactlyTenDigits() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                EnterEgnScreen(
                    onBack = {},
                    onContinue = {},
                    viewModel = EnterEgnViewModel()
                )
            }
        }
        composeTestRule.onNode(hasSetTextAction()).performTextInput("1234567890")
        composeTestRule.onNodeWithText("Continue").assertIsEnabled()
    }

    @Test
    fun screenTitleIsDisplayed() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                EnterEgnScreen(
                    onBack = {},
                    onContinue = {},
                    viewModel = EnterEgnViewModel()
                )
            }
        }
        composeTestRule.onNodeWithText("Log In or Register").assertIsDisplayed()
    }
}
