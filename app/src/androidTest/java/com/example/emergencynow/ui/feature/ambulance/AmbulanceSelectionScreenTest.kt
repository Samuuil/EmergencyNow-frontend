package com.example.emergencynow.ui.feature.ambulance

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import com.example.emergencynow.ui.BaseComposeTest
import com.example.emergencynow.ui.theme.EmergencyNowTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test

class AmbulanceSelectionScreenTest : BaseComposeTest() {

    private val uiState = MutableStateFlow(AmbulanceSelectionUiState())
    private lateinit var viewModel: AmbulanceSelectionViewModel

    @Before
    fun setUp() {
        viewModel = mockk(relaxed = true)
        every { viewModel.uiState } returns uiState
    }

    @Test
    fun screenTitleIsDisplayed() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                AmbulanceSelectionScreen(
                    onBack = {},
                    onAmbulanceSelected = {},
                    viewModel = viewModel
                )
            }
        }
        composeTestRule.onNodeWithText("Select Ambulance").assertIsDisplayed()
    }

    @Test
    fun confirmButtonDisabledWhenNoAmbulanceSelected() {
        uiState.value = AmbulanceSelectionUiState(selectedAmbulanceId = null, isLoading = false)
        composeTestRule.setContent {
            EmergencyNowTheme {
                AmbulanceSelectionScreen(
                    onBack = {},
                    onAmbulanceSelected = {},
                    viewModel = viewModel
                )
            }
        }
        composeTestRule.onNodeWithText("Confirm Selection").assertIsNotEnabled()
    }

    @Test
    fun confirmButtonEnabledWhenAmbulanceSelected() {
        uiState.value = AmbulanceSelectionUiState(
            selectedAmbulanceId = "amb-1",
            isLoading = false,
            isAssigning = false
        )
        composeTestRule.setContent {
            EmergencyNowTheme {
                AmbulanceSelectionScreen(
                    onBack = {},
                    onAmbulanceSelected = {},
                    viewModel = viewModel
                )
            }
        }
        composeTestRule.onNodeWithText("Confirm Selection").assertIsEnabled()
    }

    @Test
    fun emptyStateShownWhenNoAmbulancesAvailable() {
        uiState.value = AmbulanceSelectionUiState(availableAmbulances = emptyList(), isLoading = false)
        composeTestRule.setContent {
            EmergencyNowTheme {
                AmbulanceSelectionScreen(
                    onBack = {},
                    onAmbulanceSelected = {},
                    viewModel = viewModel
                )
            }
        }
        composeTestRule.onNodeWithText("No ambulances available").assertIsDisplayed()
    }

    @Test
    fun refreshButtonShownInEmptyState() {
        uiState.value = AmbulanceSelectionUiState(availableAmbulances = emptyList(), isLoading = false)
        composeTestRule.setContent {
            EmergencyNowTheme {
                AmbulanceSelectionScreen(
                    onBack = {},
                    onAmbulanceSelected = {},
                    viewModel = viewModel
                )
            }
        }
        composeTestRule.onNodeWithText("Refresh").assertIsDisplayed()
    }

    @Test
    fun errorStateShowsRetryButton() {
        uiState.value = AmbulanceSelectionUiState(error = "Failed to load ambulances", isLoading = false)
        composeTestRule.setContent {
            EmergencyNowTheme {
                AmbulanceSelectionScreen(
                    onBack = {},
                    onAmbulanceSelected = {},
                    viewModel = viewModel
                )
            }
        }
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun errorStateShowsErrorMessage() {
        uiState.value = AmbulanceSelectionUiState(error = "Failed to load ambulances", isLoading = false)
        composeTestRule.setContent {
            EmergencyNowTheme {
                AmbulanceSelectionScreen(
                    onBack = {},
                    onAmbulanceSelected = {},
                    viewModel = viewModel
                )
            }
        }
        composeTestRule.onNodeWithText("Failed to load ambulances").assertIsDisplayed()
    }
}
