package com.example.emergencynow.ui.feature.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.example.emergencynow.ui.BaseComposeTest
import com.example.emergencynow.ui.theme.EmergencyNowTheme
import org.junit.Test

class ProfileHomeScreenTest : BaseComposeTest() {

    @Test
    fun screenTitleIsDisplayed() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                ProfileHomeScreen(onBack = {}, onPersonalInfo = {}, onEmergencyContacts = {})
            }
        }
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
    }

    @Test
    fun yourProfileHeadingIsDisplayed() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                ProfileHomeScreen(onBack = {}, onPersonalInfo = {}, onEmergencyContacts = {})
            }
        }
        composeTestRule.onNodeWithText("Your Profile").assertIsDisplayed()
    }

    @Test
    fun personalInformationMenuItemIsDisplayed() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                ProfileHomeScreen(onBack = {}, onPersonalInfo = {}, onEmergencyContacts = {})
            }
        }
        composeTestRule.onNodeWithText("Personal Information").assertIsDisplayed()
    }

    @Test
    fun emergencyContactsMenuItemIsDisplayed() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                ProfileHomeScreen(onBack = {}, onPersonalInfo = {}, onEmergencyContacts = {})
            }
        }
        composeTestRule.onNodeWithText("Emergency Contacts").assertIsDisplayed()
    }
}
