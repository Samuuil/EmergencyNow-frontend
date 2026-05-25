package com.example.emergencynow.ui.feature.home

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.test.rule.GrantPermissionRule
import com.example.emergencynow.ui.BaseComposeTest
import com.example.emergencynow.ui.theme.EmergencyNowTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomeScreenTest : BaseComposeTest() {

    @get:Rule
    val grantPermissions: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val homeUiState = MutableStateFlow(HomeUiState(isLoading = false))
    private val driverUiState = MutableStateFlow(DriverUiState())
    private val trackingUiState = MutableStateFlow(CallTrackingUiState())

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var driverViewModel: DriverViewModel
    private lateinit var callTrackingViewModel: CallTrackingViewModel

    @Before
    fun setUp() {
        homeViewModel = mockk(relaxed = true)
        driverViewModel = mockk(relaxed = true)
        callTrackingViewModel = mockk(relaxed = true)
        every { homeViewModel.uiState } returns homeUiState
        every { driverViewModel.uiState } returns driverUiState
        every { callTrackingViewModel.uiState } returns trackingUiState
    }

    @Test
    fun emergencyCallButtonShownForRegularUser() {
        homeUiState.value = HomeUiState(isLoading = false, isDriver = false)
        composeTestRule.setContent {
            EmergencyNowTheme {
                HomeScreen(
                    onMakeEmergencyCall = {},
                    onOpenProfile = {},
                    onSelectAmbulance = {},
                    onNavigateToHistory = {},
                    onNavigateToContacts = {},
                    viewModel = homeViewModel,
                    driverViewModel = driverViewModel,
                    callTrackingViewModel = callTrackingViewModel
                )
            }
        }
        composeTestRule.onNodeWithText("Emergency Call").assertIsDisplayed()
    }

    @Test
    fun selectAmbulanceButtonShownForDriverWithoutAmbulance() {
        homeUiState.value = HomeUiState(isLoading = false, isDriver = true)
        driverUiState.value = DriverUiState(assignedAmbulanceId = null)
        composeTestRule.setContent {
            EmergencyNowTheme {
                HomeScreen(
                    onMakeEmergencyCall = {},
                    onOpenProfile = {},
                    onSelectAmbulance = {},
                    onNavigateToHistory = {},
                    onNavigateToContacts = {},
                    viewModel = homeViewModel,
                    driverViewModel = driverViewModel,
                    callTrackingViewModel = callTrackingViewModel
                )
            }
        }
        composeTestRule.onNodeWithText("Select Ambulance").assertIsDisplayed()
    }
}
