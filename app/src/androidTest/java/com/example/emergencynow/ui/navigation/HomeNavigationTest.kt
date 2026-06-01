package com.example.emergencynow.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.emergencynow.ui.constants.HomeRoute
import com.example.emergencynow.ui.feature.home.CallTrackingUiState
import com.example.emergencynow.ui.feature.home.HomeUiState
import org.junit.Test

class HomeNavigationTest : BaseNavigationTest() {

    @Test
    fun home_emergencyCall_navigatesToEmergencyCall() {
        setContent(HomeRoute)

        composeTestRule.onNodeWithText("Emergency Call").performClick()

        composeTestRule.onNodeWithText("Emergency Assistance").assertIsDisplayed()
        assertOnRoute("EmergencyCallRoute")
    }

    @Test
    fun home_history_navigatesToHistory() {
        setContent(HomeRoute)

        composeTestRule.onNodeWithText("History").performClick()

        composeTestRule.onNodeWithText("Call History").assertIsDisplayed()
        assertOnRoute("HistoryRoute")
    }

    @Test
    fun home_contacts_navigatesToEmergencyContacts() {
        setContent(HomeRoute)

        composeTestRule.onNodeWithText("Contacts").performClick()

        composeTestRule.onNodeWithText("Emergency Contacts").assertIsDisplayed()
        assertOnRoute("EmergencyContactsRoute")
    }

    @Test
    fun home_profile_navigatesToProfileHome() {
        setContent(HomeRoute)

        composeTestRule.onNodeWithText("Profile").performClick()

        composeTestRule.onNodeWithText("Your Profile").assertIsDisplayed()
        assertOnRoute("ProfileHomeRoute")
    }

    @Test
    fun home_withActiveCall_autoNavigatesToCallTracking() {
        env.homeState.value = HomeUiState(isLoading = false, isDriver = false)
        env.callTrackingState.value = CallTrackingUiState(activeCallId = "call-1")

        setContent(HomeRoute)

        composeTestRule.onNodeWithText("Emergency Call Tracking").assertIsDisplayed()
        assertOnRoute("CallTrackingRoute")
    }
}
