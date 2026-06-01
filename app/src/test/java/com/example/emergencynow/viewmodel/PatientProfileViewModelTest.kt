package com.example.emergencynow.viewmodel

import com.example.emergencynow.domain.model.entity.Gender
import com.example.emergencynow.domain.model.entity.Profile
import com.example.emergencynow.domain.usecase.profile.GetProfileByEgnUseCase
import com.example.emergencynow.ui.feature.doctor.PatientProfileViewModel
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
class PatientProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var useCase: GetProfileByEgnUseCase
    private lateinit var notificationManager: NotificationManager

    private val fakeProfile = Profile(
        id = "p-1", height = 175, weight = 70, gender = Gender.MALE,
        allergies = listOf("Penicillin"), bloodType = "A+",
        illnesses = null, medicines = null, dateOfBirth = "1990-01-01"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        useCase = mockk()
        notificationManager = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadPatientProfile success populates profile and clears loading`() = runTest {
        coEvery { useCase("1234567890") } returns Result.success(fakeProfile)

        val vm = PatientProfileViewModel(useCase, notificationManager)
        vm.loadPatientProfile("1234567890")
        advanceUntilIdle()

        assertEquals(fakeProfile, vm.uiState.value.profile)
        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `loadPatientProfile failure sets error message`() = runTest {
        coEvery { useCase(any()) } returns Result.failure(Exception("Patient not found"))

        val vm = PatientProfileViewModel(useCase, notificationManager)
        vm.loadPatientProfile("0000000000")
        advanceUntilIdle()

        assertEquals("Patient not found", vm.uiState.value.error)
        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.profile)
    }

    @Test
    fun `loadPatientProfile sets isLoading true while running`() = runTest {
        coEvery { useCase(any()) } returns Result.success(fakeProfile)

        val vm = PatientProfileViewModel(useCase, notificationManager)
        vm.loadPatientProfile("1234567890")

        assertTrue(vm.uiState.value.isLoading)

        advanceUntilIdle()
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `loadPatientProfile can be called multiple times`() = runTest {
        coEvery { useCase("egn-1") } returns Result.success(fakeProfile)
        val updated = fakeProfile.copy(height = 180)
        coEvery { useCase("egn-2") } returns Result.success(updated)

        val vm = PatientProfileViewModel(useCase, notificationManager)
        vm.loadPatientProfile("egn-1")
        advanceUntilIdle()
        assertEquals(175, vm.uiState.value.profile?.height)

        vm.loadPatientProfile("egn-2")
        advanceUntilIdle()
        assertEquals(180, vm.uiState.value.profile?.height)
    }
}
