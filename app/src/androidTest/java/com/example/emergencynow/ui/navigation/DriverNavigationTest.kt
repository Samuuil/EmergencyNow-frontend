package com.example.emergencynow.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.emergencynow.ui.constants.HomeRoute
import com.example.emergencynow.ui.feature.home.DriverUiState
import com.example.emergencynow.ui.feature.home.HomeUiState
import org.junit.Test

class DriverNavigationTest : BaseNavigationTest() {

    @Test
    fun driverHome_selectAmbulance_navigatesToAmbulanceSelection() {
        env.homeState.value = HomeUiState(isLoading = false, isDriver = true)
        env.driverState.value = DriverUiState(assignedAmbulanceId = null)

        setContent(HomeRoute)

        composeTestRule.onNodeWithText("Select Ambulance").performClick()

        composeTestRule.onNodeWithText("No ambulances available").assertIsDisplayed()
        assertOnRoute("AmbulanceSelectionRoute")
    }
}
