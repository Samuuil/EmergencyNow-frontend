package com.example.emergencynow.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.emergencynow.ui.constants.ProfileHomeRoute
import org.junit.Test

class ProfileNavigationTest : BaseNavigationTest() {

    @Test
    fun profile_personalInformation_navigatesToPersonalInfo() {
        setContent(ProfileHomeRoute)

        composeTestRule.onNodeWithText("Personal Information").performClick()

        composeTestRule.onNodeWithText("Your Health Profile").assertIsDisplayed()
        assertOnRoute("PersonalInfoRoute")
    }

    @Test
    fun profile_emergencyContacts_navigatesToEmergencyContacts() {
        setContent(ProfileHomeRoute)

        composeTestRule.onNodeWithText("Emergency Contacts").performClick()

        assertOnRoute("EmergencyContactsRoute")
    }
}
