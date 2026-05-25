package com.example.emergencynow.ui.feature.doctor

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.example.emergencynow.ui.BaseComposeTest
import com.example.emergencynow.ui.theme.EmergencyNowTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test

class PatientProfileScreenTest : BaseComposeTest() {

    private val uiState = MutableStateFlow(PatientProfileUiState())
    private lateinit var viewModel: PatientProfileViewModel

    @Before
    fun setUp() {
        viewModel = mockk(relaxed = true)
        every { viewModel.uiState } returns uiState
    }

    @Test
    fun screenTitleIsDisplayed() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                PatientProfileScreen(egn = "1234567890", onBack = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Patient Medical Profile").assertIsDisplayed()
    }

    @Test
    fun errorTextShownWhenErrorState() {
        uiState.value = PatientProfileUiState(isLoading = false, error = "Patient not found")
        composeTestRule.setContent {
            EmergencyNowTheme {
                PatientProfileScreen(egn = "1234567890", onBack = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Patient not found").assertIsDisplayed()
    }

    @Test
    fun errorMessageDisplayedVerbatim() {
        uiState.value = PatientProfileUiState(isLoading = false, error = "Network error")
        composeTestRule.setContent {
            EmergencyNowTheme {
                PatientProfileScreen(egn = "1234567890", onBack = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Network error").assertIsDisplayed()
    }
}
