package com.example.emergencynow.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.emergencynow.ui.constants.HomeRoute
import com.example.emergencynow.ui.feature.call.EmergencyCallUiState
import com.example.emergencynow.ui.feature.home.CallTrackingUiState
import org.junit.Test

class EmergencyCallNavigationTest : BaseNavigationTest() {

    private fun openEmergencyCall() {
        setContent(HomeRoute)
        composeTestRule.onNodeWithText("Emergency Call").performClick()
        composeTestRule.onNodeWithText("Emergency Assistance").assertIsDisplayed()
    }

    @Test
    fun emergencyCall_pickContact_navigatesToContactPicker() {
        openEmergencyCall()

        composeTestRule.onNodeWithText("Calling for myself").performClick()

        composeTestRule.onNodeWithText("Choose a contact").assertIsDisplayed()
        assertOnRoute("ContactPickerRoute")
    }

    @Test
    fun contactPicker_back_returnsToEmergencyCall() {
        openEmergencyCall()
        composeTestRule.onNodeWithText("Calling for myself").performClick()
        composeTestRule.onNodeWithText("Choose a contact").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        composeTestRule.onNodeWithText("Emergency Assistance").assertIsDisplayed()
        assertOnRoute("EmergencyCallRoute")
    }

    @Test
    fun emergencyCall_back_returnsToHome() {
        openEmergencyCall()

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assertOnRoute("HomeRoute")
    }

    @Test
    fun emergencyCall_callCreated_navigatesToCallTracking() {
        openEmergencyCall()

        env.callTrackingState.value = CallTrackingUiState(activeCallId = "call-1")
        env.emergencyCallState.value = EmergencyCallUiState(callCreated = true, callId = "call-1")

        composeTestRule.onNodeWithText("Emergency Call Tracking").assertIsDisplayed()
        assertOnRoute("CallTrackingRoute")
    }
}
