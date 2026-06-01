package com.example.emergencynow.viewmodel

import com.example.emergencynow.domain.model.response.AmbulanceDto
import com.example.emergencynow.domain.usecase.ambulance.AssignAmbulanceDriverUseCase
import com.example.emergencynow.domain.usecase.ambulance.GetAvailableAmbulancesUseCase
import com.example.emergencynow.ui.feature.ambulance.AmbulanceSelectionViewModel
import com.example.emergencynow.ui.util.AuthSession
import com.example.emergencynow.ui.util.NotificationManager
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AmbulanceSelectionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getAmbulancesUseCase: GetAvailableAmbulancesUseCase
    private lateinit var assignUseCase: AssignAmbulanceDriverUseCase
    private lateinit var notificationManager: NotificationManager

    private val fakeAmbulance = AmbulanceDto(
        id = "amb-1", licensePlate = "CB001", type = "Sprinter", status = "AVAILABLE"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getAmbulancesUseCase = mockk()
        assignUseCase = mockk()
        notificationManager = mockk(relaxed = true)
        AuthSession.userId = "driver-1"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        AuthSession.userId = null
    }

    @Test
    fun `init loads available ambulances`() = runTest {
        coEvery { getAmbulancesUseCase() } returns Result.success(listOf(fakeAmbulance))

        val vm = AmbulanceSelectionViewModel(getAmbulancesUseCase, assignUseCase, notificationManager)
        advanceUntilIdle()

        assertEquals(listOf(fakeAmbulance), vm.uiState.value.availableAmbulances)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `init sets error when load fails`() = runTest {
        coEvery { getAmbulancesUseCase() } returns Result.failure(Exception("Server error"))

        val vm = AmbulanceSelectionViewModel(getAmbulancesUseCase, assignUseCase, notificationManager)
        advanceUntilIdle()

        assertEquals("Server error", vm.uiState.value.error)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `selectAmbulance sets selectedAmbulanceId`() = runTest {
        coEvery { getAmbulancesUseCase() } returns Result.success(listOf(fakeAmbulance))

        val vm = AmbulanceSelectionViewModel(getAmbulancesUseCase, assignUseCase, notificationManager)
        advanceUntilIdle()
        vm.selectAmbulance("amb-1")

        assertEquals("amb-1", vm.uiState.value.selectedAmbulanceId)
    }

    @Test
    fun `selectAmbulance same id twice deselects`() = runTest {
        coEvery { getAmbulancesUseCase() } returns Result.success(listOf(fakeAmbulance))

        val vm = AmbulanceSelectionViewModel(getAmbulancesUseCase, assignUseCase, notificationManager)
        advanceUntilIdle()
        vm.selectAmbulance("amb-1")
        vm.selectAmbulance("amb-1")

        assertNull(vm.uiState.value.selectedAmbulanceId)
    }

    @Test
    fun `assignAmbulance does nothing when no selection`() = runTest {
        coEvery { getAmbulancesUseCase() } returns Result.success(listOf(fakeAmbulance))

        val vm = AmbulanceSelectionViewModel(getAmbulancesUseCase, assignUseCase, notificationManager)
        advanceUntilIdle()

        var callbackCalled = false
        vm.assignAmbulance { callbackCalled = true }
        advanceUntilIdle()

        assertFalse(callbackCalled)
    }

    @Test
    fun `assignAmbulance success calls onSuccess callback`() = runTest {
        coEvery { getAmbulancesUseCase() } returns Result.success(listOf(fakeAmbulance))
        coEvery { assignUseCase("driver-1", "amb-1") } returns Result.success(Unit)

        val vm = AmbulanceSelectionViewModel(getAmbulancesUseCase, assignUseCase, notificationManager)
        advanceUntilIdle()
        vm.selectAmbulance("amb-1")

        var callbackCalled = false
        vm.assignAmbulance { callbackCalled = true }
        advanceUntilIdle()

        assertTrue(callbackCalled)
    }

    @Test
    fun `assignAmbulance failure sets error`() = runTest {
        coEvery { getAmbulancesUseCase() } returns Result.success(listOf(fakeAmbulance))
        coEvery { assignUseCase(any(), any()) } returns Result.failure(Exception("Assignment failed"))

        val vm = AmbulanceSelectionViewModel(getAmbulancesUseCase, assignUseCase, notificationManager)
        advanceUntilIdle()
        vm.selectAmbulance("amb-1")
        vm.assignAmbulance {}
        advanceUntilIdle()

        verify { notificationManager.showError("Assignment failed") }
        assertFalse(vm.uiState.value.isAssigning)
    }

    @Test
    fun `retry reloads ambulances`() = runTest {
        coEvery { getAmbulancesUseCase() } returns Result.success(emptyList())

        val vm = AmbulanceSelectionViewModel(getAmbulancesUseCase, assignUseCase, notificationManager)
        advanceUntilIdle()
        vm.retry()
        advanceUntilIdle()

        io.mockk.coVerify(exactly = 2) { getAmbulancesUseCase() }
    }
}
