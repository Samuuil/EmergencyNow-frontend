package com.example.emergencynow.viewmodel

import com.example.emergencynow.domain.model.response.CallDto
import com.example.emergencynow.domain.usecase.call.CreateCallUseCase
import com.example.emergencynow.ui.feature.call.EmergencyCallViewModel
import com.example.emergencynow.ui.feature.call.PatientIdentification
import com.example.emergencynow.ui.util.AuthSession
import com.example.emergencynow.ui.util.AuthStorage
import io.mockk.coEvery
import io.mockk.every
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
class EmergencyCallViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var createCallUseCase: CreateCallUseCase
    private lateinit var authStorage: AuthStorage

    private val fakeDto = CallDto(
        id = "call-1", userId = "user-1",
        latitude = 42.0, longitude = 23.0, status = "PENDING"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        createCallUseCase = mockk()
        authStorage = mockk(relaxed = true)
        AuthSession.userId = "user-1"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        AuthSession.userId = null
    }

    private fun buildVm() = EmergencyCallViewModel(createCallUseCase, authStorage)

    @Test
    fun `createCall sets error when location is null`() = runTest {
        val vm = buildVm()
        vm.createCall()
        advanceUntilIdle()

        assertEquals("Location not available", vm.uiState.value.error)
        assertFalse(vm.uiState.value.isLoading)
        assertFalse(vm.uiState.value.callCreated)
    }

    @Test
    fun `createCall success sets callCreated and callId`() = runTest {
        coEvery { createCallUseCase(any(), any()) } returns Result.success(fakeDto)

        val vm = buildVm()
        vm.updateLocation(42.0, 23.0)
        vm.createCall()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.callCreated)
        assertEquals("call-1", vm.uiState.value.callId)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `createCall failure sets error`() = runTest {
        coEvery { createCallUseCase(any(), any()) } returns Result.failure(Exception("Network error"))

        val vm = buildVm()
        vm.updateLocation(42.0, 23.0)
        vm.createCall()
        advanceUntilIdle()

        assertEquals("Network error", vm.uiState.value.error)
        assertFalse(vm.uiState.value.callCreated)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `createCall with no phone sets SELF identification`() = runTest {
        coEvery { createCallUseCase(any(), any()) } returns Result.success(fakeDto)

        val vm = buildVm()
        vm.updateLocation(42.0, 23.0)
        vm.createCall()
        advanceUntilIdle()

        assertEquals(PatientIdentification.SELF, vm.uiState.value.patientIdentification)
    }

    @Test
    fun `createCall with phone and patientEgn sets IDENTIFIED`() = runTest {
        val dtoWithEgn = fakeDto.copy(patientEgn = "1234567890")
        coEvery { createCallUseCase(any(), any()) } returns Result.success(dtoWithEgn)

        val vm = buildVm()
        vm.updateLocation(42.0, 23.0)
        vm.setSelectedContact("Bob", "+35988000000")
        vm.createCall()
        advanceUntilIdle()

        assertEquals(PatientIdentification.IDENTIFIED, vm.uiState.value.patientIdentification)
    }

    @Test
    fun `createCall with phone but no patientEgn sets NOT_IDENTIFIED`() = runTest {
        coEvery { createCallUseCase(any(), any()) } returns Result.success(fakeDto)

        val vm = buildVm()
        vm.updateLocation(42.0, 23.0)
        vm.setSelectedContact("Bob", "+35988000000")
        vm.createCall()
        advanceUntilIdle()

        assertEquals(PatientIdentification.NOT_IDENTIFIED, vm.uiState.value.patientIdentification)
    }

    @Test
    fun `updateDescription updates state`() {
        val vm = buildVm()
        vm.updateDescription("Chest pain")
        assertEquals("Chest pain", vm.uiState.value.description)
    }

    @Test
    fun `setSelectedContact and clearSelectedContact work correctly`() {
        val vm = buildVm()
        vm.setSelectedContact("Alice", "+359123")
        assertEquals("Alice", vm.uiState.value.selectedContactName)

        vm.clearSelectedContact()
        assertNull(vm.uiState.value.selectedContactName)
        assertNull(vm.uiState.value.selectedContactPhoneNumber)
    }
}
