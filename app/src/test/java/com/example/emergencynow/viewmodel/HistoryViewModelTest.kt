package com.example.emergencynow.viewmodel

import com.example.emergencynow.domain.model.entity.Call
import com.example.emergencynow.domain.model.entity.CallStatus
import com.example.emergencynow.domain.usecase.call.GetUserCallsUseCase
import com.example.emergencynow.ui.feature.history.HistoryViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var useCase: GetUserCallsUseCase

    private fun fakeCall(id: String) = Call(
        id = id, description = "desc", latitude = 0.0, longitude = 0.0,
        status = CallStatus.COMPLETED, routePolyline = null, estimatedDistance = null,
        estimatedDuration = null, routeSteps = null, ambulanceCurrentLatitude = null,
        ambulanceCurrentLongitude = null, dispatchedAt = null, arrivedAt = null,
        completedAt = null, createdAt = null, selectedHospitalId = null,
        selectedHospitalName = null, hospitalRoutePolyline = null,
        hospitalRouteDistance = null, hospitalRouteDuration = null,
        hospitalRouteSteps = null
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        useCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads calls on success`() = runTest {
        val calls = listOf(fakeCall("c1"), fakeCall("c2"))
        coEvery { useCase(1, 50) } returns Result.success(calls)

        val vm = HistoryViewModel(useCase)
        advanceUntilIdle()

        assertEquals(calls, vm.uiState.value.calls)
        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `init sets error on failure`() = runTest {
        coEvery { useCase(1, 50) } returns Result.failure(Exception("Network error"))

        val vm = HistoryViewModel(useCase)
        advanceUntilIdle()

        assertEquals("Network error", vm.uiState.value.error)
        assertFalse(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.calls.isEmpty())
    }

    @Test
    fun `retry calls loadUserCalls again`() = runTest {
        coEvery { useCase(1, 50) } returns Result.success(emptyList())

        val vm = HistoryViewModel(useCase)
        advanceUntilIdle()
        vm.retry()
        advanceUntilIdle()

        coVerify(exactly = 2) { useCase(1, 50) }
    }

    @Test
    fun `empty call list is reflected in state`() = runTest {
        coEvery { useCase(1, 50) } returns Result.success(emptyList())

        val vm = HistoryViewModel(useCase)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.calls.isEmpty())
    }
}
