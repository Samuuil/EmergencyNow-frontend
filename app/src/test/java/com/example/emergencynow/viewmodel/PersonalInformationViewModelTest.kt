package com.example.emergencynow.viewmodel

import com.example.emergencynow.domain.model.entity.Gender
import com.example.emergencynow.domain.model.entity.Profile
import com.example.emergencynow.domain.usecase.profile.CreateProfileUseCase
import com.example.emergencynow.domain.usecase.profile.GetProfileUseCase
import com.example.emergencynow.domain.usecase.profile.UpdateProfileUseCase
import com.example.emergencynow.ui.feature.profile.PersonalInformationViewModel
import com.example.emergencynow.ui.util.NotificationManager
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PersonalInformationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getProfileUseCase: GetProfileUseCase
    private lateinit var createProfileUseCase: CreateProfileUseCase
    private lateinit var updateProfileUseCase: UpdateProfileUseCase
    private lateinit var notificationManager: NotificationManager

    private val fakeProfile = Profile(
        id = "p-1", height = 175, weight = 70, gender = Gender.FEMALE,
        allergies = listOf("Penicillin"), bloodType = "B+",
        illnesses = listOf("Asthma"), medicines = null, dateOfBirth = "1990-05-15"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getProfileUseCase = mockk()
        createProfileUseCase = mockk()
        updateProfileUseCase = mockk()
        notificationManager = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildVm() = PersonalInformationViewModel(
        getProfileUseCase, createProfileUseCase, updateProfileUseCase, notificationManager
    )

    @Test
    fun `init loads profile and populates fields`() = runTest {
        coEvery { getProfileUseCase() } returns Result.success(fakeProfile)

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals("175", vm.uiState.value.height)
        assertEquals("70", vm.uiState.value.weight)
        assertEquals("female", vm.uiState.value.gender)
        assertEquals("Penicillin", vm.uiState.value.allergies)
        assertEquals("B+", vm.uiState.value.bloodType)
        assertEquals("Asthma", vm.uiState.value.illnesses)
        assertTrue(vm.uiState.value.isEditMode)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `init sets isEditMode false when profile load fails`() = runTest {
        coEvery { getProfileUseCase() } returns Result.failure(Exception("Not found"))

        val vm = buildVm()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isEditMode)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `updateHeight accepts digits and rejects letters`() = runTest {
        coEvery { getProfileUseCase() } returns Result.failure(Exception())

        val vm = buildVm()
        advanceUntilIdle()

        vm.updateHeight("175")
        assertEquals("175", vm.uiState.value.height)

        vm.updateHeight("17x")
        assertEquals("175", vm.uiState.value.height)
    }

    @Test
    fun `updateWeight accepts digits and rejects letters`() = runTest {
        coEvery { getProfileUseCase() } returns Result.failure(Exception())

        val vm = buildVm()
        advanceUntilIdle()

        vm.updateWeight("70")
        assertEquals("70", vm.uiState.value.weight)

        vm.updateWeight("7a")
        assertEquals("70", vm.uiState.value.weight)
    }

    @Test
    fun `saveProfile sets error when height is blank`() = runTest {
        coEvery { getProfileUseCase() } returns Result.failure(Exception())

        val vm = buildVm()
        advanceUntilIdle()
        vm.updateWeight("70")
        vm.saveProfile {}
        advanceUntilIdle()

        verify { notificationManager.showError("Please enter valid height and weight") }
    }

    @Test
    fun `saveProfile in create mode calls createProfileUseCase`() = runTest {
        coEvery { getProfileUseCase() } returns Result.failure(Exception())
        coEvery { createProfileUseCase(any(), any(), any(), any(), any(), any(), any(), any()) } returns
            Result.success(fakeProfile)

        val vm = buildVm()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isEditMode)

        vm.updateHeight("175")
        vm.updateWeight("70")
        vm.saveProfile {}
        advanceUntilIdle()

        coVerify(exactly = 1) { createProfileUseCase(175, 70, any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `saveProfile in edit mode calls updateProfileUseCase`() = runTest {
        coEvery { getProfileUseCase() } returns Result.success(fakeProfile)
        coEvery { updateProfileUseCase(any(), any(), any(), any(), any(), any(), any(), any()) } returns
            Result.success(fakeProfile)

        val vm = buildVm()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isEditMode)

        vm.saveProfile {}
        advanceUntilIdle()

        coVerify(exactly = 1) { updateProfileUseCase(175, 70, any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `saveProfile failure sets error`() = runTest {
        coEvery { getProfileUseCase() } returns Result.failure(Exception())
        coEvery { createProfileUseCase(any(), any(), any(), any(), any(), any(), any(), any()) } returns
            Result.failure(Exception("Server error"))

        val vm = buildVm()
        advanceUntilIdle()
        vm.updateHeight("175")
        vm.updateWeight("70")
        vm.saveProfile {}
        advanceUntilIdle()

        verify { notificationManager.showError("Server error") }
        assertFalse(vm.uiState.value.isSaving)
    }

    @Test
    fun `saveProfile success sets isSaving false and calls onSuccess`() = runTest {
        coEvery { getProfileUseCase() } returns Result.failure(Exception())
        coEvery { createProfileUseCase(any(), any(), any(), any(), any(), any(), any(), any()) } returns
            Result.success(fakeProfile)

        val vm = buildVm()
        advanceUntilIdle()
        vm.updateHeight("175")
        vm.updateWeight("70")

        var callbackCalled = false
        vm.saveProfile { callbackCalled = true }
        advanceUntilIdle()

        assertTrue(callbackCalled)
        assertFalse(vm.uiState.value.isSaving)
        assertTrue(vm.uiState.value.isEditMode)
    }
}
