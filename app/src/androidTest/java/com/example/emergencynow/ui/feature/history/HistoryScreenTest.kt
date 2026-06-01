package com.example.emergencynow.ui.feature.history

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.example.emergencynow.ui.BaseComposeTest
import com.example.emergencynow.ui.theme.EmergencyNowTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test

class HistoryScreenTest : BaseComposeTest() {

    private val uiState = MutableStateFlow(HistoryUiState())
    private lateinit var viewModel: HistoryViewModel

    @Before
    fun setUp() {
        viewModel = mockk(relaxed = true)
        every { viewModel.uiState } returns uiState
    }

    @Test
    fun screenTitleIsAlwaysDisplayed() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                HistoryScreen(onBack = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Call History").assertIsDisplayed()
    }

    @Test
    fun emptyStateMessageShownWhenNoCallsAndNotLoading() {
        uiState.value = HistoryUiState(isLoading = false, calls = emptyList())
        composeTestRule.setContent {
            EmergencyNowTheme {
                HistoryScreen(onBack = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("No Call History").assertIsDisplayed()
    }

    @Test
    fun emptyStateSubtitleShown() {
        uiState.value = HistoryUiState(isLoading = false, calls = emptyList())
        composeTestRule.setContent {
            EmergencyNowTheme {
                HistoryScreen(onBack = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Your emergency call history will appear here").assertIsDisplayed()
    }

    @Test
    fun errorStateShowsErrorHeading() {
        uiState.value = HistoryUiState(error = "Network error", isLoading = false)
        composeTestRule.setContent {
            EmergencyNowTheme {
                HistoryScreen(onBack = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Error loading call history").assertIsDisplayed()
    }

    @Test
    fun errorStateShowsRetryButton() {
        uiState.value = HistoryUiState(error = "Network error", isLoading = false)
        composeTestRule.setContent {
            EmergencyNowTheme {
                HistoryScreen(onBack = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun errorStateShowsErrorMessage() {
        uiState.value = HistoryUiState(error = "Network error", isLoading = false)
        composeTestRule.setContent {
            EmergencyNowTheme {
                HistoryScreen(onBack = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Network error").assertIsDisplayed()
    }
}
