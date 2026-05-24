package com.example.emergencynow.ui.feature.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.example.emergencynow.ui.BaseComposeTest
import com.example.emergencynow.ui.theme.EmergencyNowTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test

class ChooseVerificationMethodScreenTest : BaseComposeTest() {

    private val uiState = MutableStateFlow(ChooseVerificationMethodUiState())
    private lateinit var viewModel: ChooseVerificationMethodViewModel

    @Before
    fun setUp() {
        viewModel = mockk(relaxed = true)
        every { viewModel.uiState } returns uiState
    }

    @Test
    fun phoneCardIsDisplayed() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                ChooseVerificationMethodScreen(
                    onBack = {},
                    onPhone = {},
                    onEmail = {},
                    viewModel = viewModel
                )
            }
        }
        composeTestRule.onNodeWithText("Send to Phone Number").assertIsDisplayed()
    }

    @Test
    fun emailCardIsDisplayed() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                ChooseVerificationMethodScreen(
                    onBack = {},
                    onPhone = {},
                    onEmail = {},
                    viewModel = viewModel
                )
            }
        }
        composeTestRule.onNodeWithText("Send to Email").assertIsDisplayed()
    }

    @Test
    fun bothMethodCardsDisplayedTogether() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                ChooseVerificationMethodScreen(
                    onBack = {},
                    onPhone = {},
                    onEmail = {},
                    viewModel = viewModel
                )
            }
        }
        composeTestRule.onNodeWithText("Send to Phone Number").assertIsDisplayed()
        composeTestRule.onNodeWithText("Send to Email").assertIsDisplayed()
    }

    @Test
    fun screenTitleIsDisplayed() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                ChooseVerificationMethodScreen(
                    onBack = {},
                    onPhone = {},
                    onEmail = {},
                    viewModel = viewModel
                )
            }
        }
        composeTestRule.onNodeWithText("Verify Your Account").assertIsDisplayed()
    }

    @Test
    fun cardsStillInTreeWhenLoading() {
        uiState.value = ChooseVerificationMethodUiState(isLoading = true)
        composeTestRule.setContent {
            EmergencyNowTheme {
                ChooseVerificationMethodScreen(
                    onBack = {},
                    onPhone = {},
                    onEmail = {},
                    viewModel = viewModel
                )
            }
        }
        // Cards are rendered behind the loading overlay — they exist in the semantic tree
        composeTestRule.onNodeWithText("Send to Phone Number").assertIsDisplayed()
    }
}
