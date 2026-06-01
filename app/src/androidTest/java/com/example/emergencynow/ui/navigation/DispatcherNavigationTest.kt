package com.example.emergencynow.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.emergencynow.domain.model.entity.DispatcherCallOffer
import com.example.emergencynow.ui.constants.HomeRoute
import com.example.emergencynow.ui.feature.dispatcher.DispatcherUiState
import com.example.emergencynow.ui.feature.home.HomeUiState
import org.junit.Test

class DispatcherNavigationTest : BaseNavigationTest() {

    private fun call(id: String) = DispatcherCallOffer(
        callId = id,
        description = "Test emergency",
        latitude = 42.0,
        longitude = 23.0,
        createdAt = "2024-01-01T00:00:00Z",
        userName = null,
    )

    private fun openDispatcherHome() {
        env.homeState.value = HomeUiState(isLoading = false, isDispatcher = true)
        setContent(HomeRoute)
        composeTestRule.onNodeWithText("Assigned calls (0)").assertIsDisplayed()
    }

    @Test
    fun dispatcherHome_profile_navigatesToProfileHome() {
        openDispatcherHome()

        composeTestRule.onNodeWithText("Profile").performClick()

        composeTestRule.onNodeWithText("Your Profile").assertIsDisplayed()
        assertOnRoute("ProfileHomeRoute")
    }

    @Test
    fun dispatcherHome_contacts_navigatesToEmergencyContacts() {
        openDispatcherHome()

        composeTestRule.onNodeWithText("Contacts").performClick()

        assertOnRoute("EmergencyContactsRoute")
    }

    @Test
    fun dispatcherHome_emergencyCall_navigatesToEmergencyCall() {
        openDispatcherHome()

        composeTestRule.onNodeWithText("Emergency Call").performClick()

        composeTestRule.onNodeWithText("Emergency Assistance").assertIsDisplayed()
        assertOnRoute("EmergencyCallRoute")
    }

    @Test
    fun dispatcherHome_assignCall_navigatesToDispatcherAssign() {
        env.homeState.value = HomeUiState(isLoading = false, isDispatcher = true)
        env.dispatcherState.value = DispatcherUiState(calls = mapOf("call-1" to call("call-1")))

        setContent(HomeRoute)

        composeTestRule.onNodeWithText("Assign ambulance").performClick()

        assertOnRoute("DispatcherAssignRoute")
    }
}
