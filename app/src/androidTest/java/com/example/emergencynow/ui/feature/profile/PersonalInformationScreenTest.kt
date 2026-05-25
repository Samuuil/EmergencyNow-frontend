package com.example.emergencynow.ui.feature.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.example.emergencynow.ui.BaseComposeTest
import com.example.emergencynow.ui.theme.EmergencyNowTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test

class PersonalInformationScreenTest : BaseComposeTest() {

    private val uiState = MutableStateFlow(PersonalInfoUiState())
    private lateinit var viewModel: PersonalInformationViewModel

    @Before
    fun setUp() {
        viewModel = mockk(relaxed = true)
        every { viewModel.uiState } returns uiState
    }

    @Test
    fun screenTitleIsDisplayed() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                PersonalInformationScreen(onBack = {}, onContinue = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Your Health Profile").assertIsDisplayed()
    }

    @Test
    fun continueButtonShownWhenNotLoading() {
        uiState.value = PersonalInfoUiState(isLoading = false, isEditMode = false)
        composeTestRule.setContent {
            EmergencyNowTheme {
                PersonalInformationScreen(onBack = {}, onContinue = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Continue").assertIsDisplayed()
    }

    @Test
    fun saveChangesButtonShownInEditMode() {
        uiState.value = PersonalInfoUiState(isLoading = false, isEditMode = true)
        composeTestRule.setContent {
            EmergencyNowTheme {
                PersonalInformationScreen(onBack = {}, onContinue = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Save Changes").assertIsDisplayed()
    }

    @Test
    fun continueButtonNotShownWhenLoading() {
        uiState.value = PersonalInfoUiState(isLoading = true)
        composeTestRule.setContent {
            EmergencyNowTheme {
                PersonalInformationScreen(onBack = {}, onContinue = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Continue").assertDoesNotExist()
    }
}
