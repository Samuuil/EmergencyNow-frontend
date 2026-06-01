package com.example.emergencynow.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.emergencynow.ui.constants.HomeRoute
import com.example.emergencynow.ui.constants.PatientLookupRoute
import com.example.emergencynow.ui.feature.home.HomeUiState
import org.junit.Test

class DoctorNavigationTest : BaseNavigationTest() {

    @Test
    fun doctorHome_patientLookup_navigatesToPatientLookup() {
        env.homeState.value = HomeUiState(isLoading = false, isDoctor = true)

        setContent(HomeRoute)

        composeTestRule.onNodeWithText("Patient Lookup").performClick()

        composeTestRule.onNodeWithText("Enter\nPatient EGN").assertIsDisplayed()
        assertOnRoute("PatientLookupRoute")
    }

    @Test
    fun patientLookup_lookup_navigatesToPatientProfile() {
        setContent(PatientLookupRoute)

        composeTestRule.onNode(hasSetTextAction()).performTextInput("1234567890")
        composeTestRule.onNodeWithText("Lookup Patient").performClick()

        composeTestRule.onNodeWithText("Patient Medical Profile").assertIsDisplayed()
        assertOnRoute("PatientProfileRoute")
    }
}
