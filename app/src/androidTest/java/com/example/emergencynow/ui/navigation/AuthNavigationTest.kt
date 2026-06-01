package com.example.emergencynow.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.emergencynow.ui.constants.ChooseVerificationRoute
import com.example.emergencynow.ui.constants.EnterEgnRoute
import com.example.emergencynow.ui.feature.auth.VerifyCodeUIState
import com.example.emergencynow.ui.constants.WelcomeRoute
import org.junit.Test

class AuthNavigationTest : BaseNavigationTest() {

    @Test
    fun welcome_registerEgn_navigatesToEnterEgn() {
        setContent(WelcomeRoute)

        composeTestRule.onNodeWithText("Register EGN").performClick()

        composeTestRule.onNodeWithText("Log In or Register").assertIsDisplayed()
        assertOnRoute("EnterEgnRoute")
    }

    @Test
    fun enterEgn_continue_navigatesToChooseVerification() {
        setContent(EnterEgnRoute)

        composeTestRule.onNode(hasSetTextAction()).performTextInput("1234567890")
        composeTestRule.onNodeWithText("Continue").performClick()

        composeTestRule.onNodeWithText("Verify Your Account").assertIsDisplayed()
        assertOnRoute("ChooseVerificationRoute")
    }

    @Test
    fun chooseVerification_phone_navigatesToCodeEntry() {
        setContent(ChooseVerificationRoute(egn = "1234567890"))

        composeTestRule.onNodeWithText("Send to Phone Number").performClick()

        composeTestRule.onNodeWithText("Verify\nYour Number").assertIsDisplayed()
        assertOnRoute("EnterVerificationCodeRoute")
    }

    @Test
    fun fullFlow_welcomeToCodeEntry_viaEmail() {
        setContent(WelcomeRoute)

        composeTestRule.onNodeWithText("Register EGN").performClick()
        composeTestRule.onNode(hasSetTextAction()).performTextInput("1234567890")
        composeTestRule.onNodeWithText("Continue").performClick()
        composeTestRule.onNodeWithText("Send to Email").performClick()

        composeTestRule.onNodeWithText("Verify\nYour Number").assertIsDisplayed()
        assertOnRoute("EnterVerificationCodeRoute")
    }

    @Test
    fun back_fromChooseVerification_returnsToEnterEgn() {
        setContent(EnterEgnRoute)
        composeTestRule.onNode(hasSetTextAction()).performTextInput("1234567890")
        composeTestRule.onNodeWithText("Continue").performClick()
        composeTestRule.onNodeWithText("Verify Your Account").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        composeTestRule.onNodeWithText("Log In or Register").assertIsDisplayed()
        assertOnRoute("EnterEgnRoute")
    }

    @Test
    fun verifiedReturningUser_navigatesToHome() {
        setContent(ChooseVerificationRoute(egn = "1234567890"))
        composeTestRule.onNodeWithText("Send to Phone Number").performClick()
        composeTestRule.onNodeWithText("Verify\nYour Number").assertIsDisplayed()

        env.verifyCodeState.value = VerifyCodeUIState(isVerified = true, isReturningUser = true)

        composeTestRule.waitForIdle()
        assertOnRoute("HomeRoute")
    }

    @Test
    fun verifiedNewUser_navigatesToPersonalInfoOnboarding() {
        setContent(ChooseVerificationRoute(egn = "1234567890"))
        composeTestRule.onNodeWithText("Send to Phone Number").performClick()
        composeTestRule.onNodeWithText("Verify\nYour Number").assertIsDisplayed()

        env.verifyCodeState.value = VerifyCodeUIState(isVerified = true, isReturningUser = false)

        composeTestRule.onNodeWithText("Your Health Profile").assertIsDisplayed()
        assertOnRoute("PersonalInfoRoute")
    }
}
