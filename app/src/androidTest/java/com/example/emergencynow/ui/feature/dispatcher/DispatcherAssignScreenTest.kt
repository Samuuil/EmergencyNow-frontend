package com.example.emergencynow.ui.feature.dispatcher

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.example.emergencynow.ui.BaseComposeTest
import com.example.emergencynow.ui.theme.EmergencyNowTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test

class DispatcherAssignScreenTest : BaseComposeTest() {

    private val uiState = MutableStateFlow(DispatcherUiState())
    private lateinit var viewModel: DispatcherViewModel

    @Before
    fun setUp() {
        viewModel = mockk(relaxed = true)
        every { viewModel.uiState } returns uiState
    }

    @Test
    fun defaultTitleShownWhenCallNotFound() {
        composeTestRule.setContent {
            EmergencyNowTheme {
                DispatcherAssignScreen(callId = "unknown-id", onBack = {}, onAssigned = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Assign ambulance").assertIsDisplayed()
    }
}
