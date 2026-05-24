package com.example.emergencynow.ui.feature.contacts

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import com.example.emergencynow.domain.model.entity.Contact
import com.example.emergencynow.ui.BaseComposeTest
import com.example.emergencynow.ui.theme.EmergencyNowTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test

class EmergencyContactsScreenTest : BaseComposeTest() {

    private val uiState = MutableStateFlow(EmergencyContactsUiState())
    private lateinit var viewModel: EmergencyContactsViewModel

    @Before
    fun setUp() {
        viewModel = mockk(relaxed = true)
        every { viewModel.uiState } returns uiState
    }

    @Test
    fun screenTitleIsDisplayed() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                EmergencyContactsScreen(onBack = {}, onFinish = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Emergency Contacts").assertIsDisplayed()
    }

    @Test
    fun finishSetupButtonDisabledWhenAllContactsAreEmpty() {
        uiState.value = EmergencyContactsUiState(
            contacts = listOf(Contact(id = "", name = "", phoneNumber = "", email = null))
        )
        composeTestRule.setContent {
            EmergencyNowTheme {
                EmergencyContactsScreen(onBack = {}, onFinish = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Finish Setup").assertIsNotEnabled()
    }

    @Test
    fun finishSetupButtonEnabledWhenContactHasNameAndPhone() {
        uiState.value = EmergencyContactsUiState(
            contacts = listOf(
                Contact(id = "1", name = "Alice", phoneNumber = "0888123456", email = null)
            )
        )
        composeTestRule.setContent {
            EmergencyNowTheme {
                EmergencyContactsScreen(onBack = {}, onFinish = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Finish Setup").assertIsEnabled()
    }

    @Test
    fun addAnotherContactButtonIsDisplayed() {
        uiState.value = EmergencyContactsUiState(
            contacts = listOf(
                Contact(id = "1", name = "Alice", phoneNumber = "0888123456", email = null)
            )
        )
        composeTestRule.setContent {
            EmergencyNowTheme {
                EmergencyContactsScreen(onBack = {}, onFinish = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Add Another Contact").assertIsDisplayed()
    }

    @Test
    fun addAnotherContactDisabledWhenLastContactIsEmpty() {
        // Last contact is blank — canAddMore is false
        uiState.value = EmergencyContactsUiState(
            contacts = listOf(Contact(id = "", name = "", phoneNumber = "", email = null))
        )
        composeTestRule.setContent {
            EmergencyNowTheme {
                EmergencyContactsScreen(onBack = {}, onFinish = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Add Another Contact").assertIsNotEnabled()
    }

    @Test
    fun contactsListHiddenWhenLoading() {
        uiState.value = EmergencyContactsUiState(isLoading = true)
        composeTestRule.setContent {
            EmergencyNowTheme {
                EmergencyContactsScreen(onBack = {}, onFinish = {}, viewModel = viewModel)
            }
        }
        // Buttons are not part of the tree when isLoading = true
        composeTestRule.onNodeWithText("Finish Setup").assertDoesNotExist()
    }
}
