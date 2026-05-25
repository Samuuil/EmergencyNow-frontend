package com.example.emergencynow.ui.feature.doctor

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import com.example.emergencynow.ui.BaseComposeTest
import com.example.emergencynow.ui.theme.EmergencyNowTheme
import org.junit.Test

class PatientLookupScreenTest : BaseComposeTest() {

    @Test
    fun screenTitleIsDisplayed() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                PatientLookupScreen(onBack = {}, onLookup = {})
            }
        }
        composeTestRule.onNodeWithText("Patient Lookup").assertIsDisplayed()
    }

    @Test
    fun enterPatientEgnHeadingIsDisplayed() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                PatientLookupScreen(onBack = {}, onLookup = {})
            }
        }
        composeTestRule.onNodeWithText("Enter\nPatient EGN").assertIsDisplayed()
    }

    @Test
    fun lookupButtonDisabledWhenEgnIsEmpty() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                PatientLookupScreen(onBack = {}, onLookup = {})
            }
        }
        composeTestRule.onNodeWithText("Lookup Patient").assertIsNotEnabled()
    }
}
