package com.example.emergencynow.viewmodel

import com.example.emergencynow.domain.usecase.dispatcher.AssignAmbulanceUseCase
import com.example.emergencynow.domain.usecase.dispatcher.GetAvailableAmbulancesForDispatcherUseCase
import com.example.emergencynow.domain.usecase.dispatcher.GetDispatcherCallsUseCase
import com.example.emergencynow.ui.feature.dispatcher.DispatcherViewModel
import com.example.emergencynow.ui.util.AuthStorage
import com.example.emergencynow.ui.util.DispatcherNotificationHelper
import com.example.emergencynow.ui.util.NotificationManager
import io.mockk.coEvery
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
class DispatcherViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var assignUseCase: AssignAmbulanceUseCase
    private lateinit var getCallsUseCase: GetDispatcherCallsUseCase
    private lateinit var getAmbulancesUseCase: GetAvailableAmbulancesForDispatcherUseCase
    private lateinit var authStorage: AuthStorage
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationHelper: DispatcherNotificationHelper

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        assignUseCase = mockk()
        getCallsUseCase = mockk()
        getAmbulancesUseCase = mockk()
        authStorage = mockk(relaxed = true)
        notificationManager = mockk(relaxed = true)
        notificationHelper = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildVm() = DispatcherViewModel(
        authStorage, getCallsUseCase, getAmbulancesUseCase,
        assignUseCase, notificationHelper, notificationManager
    )

    @Test
    fun `assignAmbulance success adds callId to pendingAssignments`() = runTest {
        coEvery { assignUseCase("call-1", "amb-1") } returns Result.success(Unit)

        val vm = buildVm()
        var callbackCalled = false
        vm.assignAmbulance("call-1", "amb-1") { callbackCalled = true }
        advanceUntilIdle()

        assertTrue(callbackCalled)
        assertTrue("call-1" in vm.uiState.value.pendingAssignments)
        assertFalse(vm.uiState.value.isAssigning)
        assertNull(vm.uiState.value.lastAssignError)
    }

    @Test
    fun `assignAmbulance failure sets lastAssignError`() = runTest {
        coEvery { assignUseCase(any(), any()) } returns Result.failure(Exception("Ambulance unavailable"))

        val vm = buildVm()
        vm.assignAmbulance("call-1", "amb-1") {}
        advanceUntilIdle()

        assertEquals("Ambulance unavailable", vm.uiState.value.lastAssignError)
        assertFalse(vm.uiState.value.isAssigning)
        assertTrue(vm.uiState.value.pendingAssignments.isEmpty())
    }

    @Test
    fun `assignAmbulance failure shows notification`() = runTest {
        coEvery { assignUseCase(any(), any()) } returns Result.failure(Exception("Error"))

        val vm = buildVm()
        vm.assignAmbulance("call-1", "amb-1") {}
        advanceUntilIdle()

        io.mockk.verify { notificationManager.showError(any()) }
    }

    @Test
    fun `disconnectSocket sets isSocketConnected false`() = runTest {
        val vm = buildVm()
        vm.disconnectSocket()

        assertFalse(vm.uiState.value.isSocketConnected)
    }
}
