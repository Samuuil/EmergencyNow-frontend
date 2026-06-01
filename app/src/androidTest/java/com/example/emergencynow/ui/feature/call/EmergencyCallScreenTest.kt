package com.example.emergencynow.ui.feature.call

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.test.rule.GrantPermissionRule
import com.example.emergencynow.ui.BaseComposeTest
import com.example.emergencynow.ui.theme.EmergencyNowTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EmergencyCallScreenTest : BaseComposeTest() {

    @get:Rule
    val grantPermissions: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val uiState = MutableStateFlow(EmergencyCallUiState())
    private lateinit var viewModel: EmergencyCallViewModel

    @Before
    fun setUp() {
        viewModel = mockk(relaxed = true)
        every { viewModel.uiState } returns uiState
    }

    @Test
    fun screenTitleIsDisplayed() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                EmergencyCallScreen(onBack = {}, onCallCreated = {}, onPickContact = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Emergency Call").assertIsDisplayed()
    }

    @Test
    fun emergencyAssistanceHeadingIsDisplayed() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                EmergencyCallScreen(onBack = {}, onCallCreated = {}, onPickContact = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Emergency Assistance").assertIsDisplayed()
    }

    @Test
    fun callButtonDisabledWhenNoLocation() {
        uiState.value = EmergencyCallUiState(latitude = null, longitude = null)
        composeTestRule.setContent {
            EmergencyNowTheme {
                EmergencyCallScreen(onBack = {}, onCallCreated = {}, onPickContact = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Call Emergency Services").assertIsNotEnabled()
    }

    @Test
    fun callingForMyselfShownWhenNoContactSelected() {
        uiState.value = EmergencyCallUiState(selectedContactName = null)
        composeTestRule.setContent {
            EmergencyNowTheme {
                EmergencyCallScreen(onBack = {}, onCallCreated = {}, onPickContact = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Calling for myself").assertIsDisplayed()
    }

    @Test
    fun callingForNameShownWhenContactSelected() {
        uiState.value = EmergencyCallUiState(
            selectedContactName = "Alice",
            selectedContactPhoneNumber = "0888123456"
        )
        composeTestRule.setContent {
            EmergencyNowTheme {
                EmergencyCallScreen(onBack = {}, onCallCreated = {}, onPickContact = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Calling for: Alice").assertIsDisplayed()
    }
}
