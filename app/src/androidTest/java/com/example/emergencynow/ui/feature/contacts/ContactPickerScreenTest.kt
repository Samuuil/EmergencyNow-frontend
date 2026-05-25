package com.example.emergencynow.ui.feature.contacts

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.test.rule.GrantPermissionRule
import com.example.emergencynow.ui.BaseComposeTest
import com.example.emergencynow.ui.theme.EmergencyNowTheme
import com.example.emergencynow.ui.util.DeviceContact
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ContactPickerScreenTest : BaseComposeTest() {

    @get:Rule
    val grantPermissions: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.READ_CONTACTS
    )

    private val uiState = MutableStateFlow(ContactPickerUiState())
    private lateinit var viewModel: ContactPickerViewModel

    @Before
    fun setUp() {
        viewModel = mockk(relaxed = true)
        every { viewModel.uiState } returns uiState
        every { viewModel.filteredContacts() } returns emptyList()
    }

    @Test
    fun screenTitleIsDisplayed() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                ContactPickerScreen(onBack = {}, onContactSelected = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Choose a contact").assertIsDisplayed()
    }

    @Test
    fun permissionMessageShownWhenNoPermission() {
        uiState.value = ContactPickerUiState(hasPermission = false, isLoading = false)
        composeTestRule.setContent {
            EmergencyNowTheme {
                ContactPickerScreen(onBack = {}, onContactSelected = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Contacts access needed").assertIsDisplayed()
    }

    @Test
    fun noContactsMessageShownWhenPermissionGrantedAndListEmpty() {
        uiState.value = ContactPickerUiState(hasPermission = true, isLoading = false, contacts = emptyList())
        composeTestRule.setContent {
            EmergencyNowTheme {
                ContactPickerScreen(onBack = {}, onContactSelected = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("No contacts found on this device.").assertIsDisplayed()
    }

    @Test
    fun contactRowShownWhenPermissionGrantedAndContactsLoaded() {
        val contacts = listOf(DeviceContact(id = "1", displayName = "Alice", phoneNumber = "0888123456"))
        uiState.value = ContactPickerUiState(hasPermission = true, isLoading = false, contacts = contacts)
        every { viewModel.filteredContacts() } returns contacts
        composeTestRule.setContent {
            EmergencyNowTheme {
                ContactPickerScreen(onBack = {}, onContactSelected = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Alice").assertIsDisplayed()
    }
}
