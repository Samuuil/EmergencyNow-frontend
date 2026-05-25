package com.example.emergencynow.ui.feature.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.example.emergencynow.ui.BaseComposeTest
import com.example.emergencynow.ui.theme.EmergencyNowTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test

class CallTrackingScreenTest : BaseComposeTest() {

    private val homeUiState = MutableStateFlow(HomeUiState(isLoading = false))
    private val trackingUiState = MutableStateFlow(CallTrackingUiState(activeCallId = "test-call-id"))

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var callTrackingViewModel: CallTrackingViewModel

    @Before
    fun setUp() {
        homeViewModel = mockk(relaxed = true)
        callTrackingViewModel = mockk(relaxed = true)
        every { homeViewModel.uiState } returns homeUiState
        every { callTrackingViewModel.uiState } returns trackingUiState
    }

    @Test
    fun screenTitleIsDisplayed() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                CallTrackingScreen(
                    onBackToHome = {},
                    homeViewModel = homeViewModel,
                    callTrackingViewModel = callTrackingViewModel
                )
            }
        }
        composeTestRule.onNodeWithText("Emergency Call Tracking").assertIsDisplayed()
    }
}
