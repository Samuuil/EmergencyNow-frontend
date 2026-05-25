package com.example.emergencynow.ui.feature.dispatcher

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.example.emergencynow.domain.model.entity.DispatcherCallOffer
import com.example.emergencynow.ui.BaseComposeTest
import com.example.emergencynow.ui.theme.EmergencyNowTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test

private fun testCall(id: String) = DispatcherCallOffer(
    callId = id,
    description = "Test emergency",
    latitude = 42.0,
    longitude = 23.0,
    createdAt = "2024-01-01T00:00:00Z",
    userName = null,
)

class DispatcherHomeScreenTest : BaseComposeTest() {

    private val uiState = MutableStateFlow(DispatcherUiState())
    private lateinit var viewModel: DispatcherViewModel

    @Before
    fun setUp() {
        viewModel = mockk(relaxed = true)
        every { viewModel.uiState } returns uiState
    }

    @Test
    fun assignedCallsCountShownWithNoCalls() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                DispatcherHomeScreen(
                    onOpenProfile = {},
                    onOpenContacts = {},
                    onAssignCall = {},
                    onMakeEmergencyCall = {},
                    viewModel = viewModel
                )
            }
        }
        composeTestRule.onNodeWithText("Assigned calls (0)").assertIsDisplayed()
    }

    @Test
    fun emptyStateMessageShownWhenNoActiveCalls() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                DispatcherHomeScreen(
                    onOpenProfile = {},
                    onOpenContacts = {},
                    onAssignCall = {},
                    onMakeEmergencyCall = {},
                    viewModel = viewModel
                )
            }
        }
        composeTestRule.onNodeWithText("No calls right now").assertIsDisplayed()
    }

    @Test
    fun assignedCallsCountUpdatesWithActiveCalls() {
        val call = testCall("call-1")
        uiState.value = DispatcherUiState(calls = mapOf("call-1" to call))
        composeTestRule.setContent {
            EmergencyNowTheme {
                DispatcherHomeScreen(
                    onOpenProfile = {},
                    onOpenContacts = {},
                    onAssignCall = {},
                    onMakeEmergencyCall = {},
                    viewModel = viewModel
                )
            }
        }
        composeTestRule.onNodeWithText("Assigned calls (1)").assertIsDisplayed()
    }
}
