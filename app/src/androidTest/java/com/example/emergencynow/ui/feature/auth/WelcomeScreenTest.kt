package com.example.emergencynow.ui.feature.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.emergencynow.ui.BaseComposeTest
import com.example.emergencynow.ui.theme.EmergencyNowTheme
import org.junit.Assert.assertTrue
import org.junit.Test

class WelcomeScreenTest : BaseComposeTest() {

    @Test
    fun appTitleIsDisplayed() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                WelcomeScreen(onRegisterEgn = {}, onLogin = {})
            }
        }
        composeTestRule.onNodeWithText("Emergency", substring = true).assertIsDisplayed()
    }

    @Test
    fun registerEgnButtonIsDisplayed() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                WelcomeScreen(onRegisterEgn = {}, onLogin = {})
            }
        }
        composeTestRule.onNodeWithText("Register EGN").assertIsDisplayed()
    }

    @Test
    fun registerEgnButtonTriggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            EmergencyNowTheme {
                WelcomeScreen(onRegisterEgn = { clicked = true }, onLogin = {})
            }
        }
        composeTestRule.onNodeWithText("Register EGN").performClick()
        assertTrue(clicked)
    }

    @Test
    fun taglineIsDisplayed() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                WelcomeScreen(onRegisterEgn = {}, onLogin = {})
            }
        }
        composeTestRule.onNodeWithText("Fast emergency response", substring = true).assertIsDisplayed()
    }
}
