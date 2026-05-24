package com.example.emergencynow.usecase

import com.example.emergencynow.data.error.EmergencyError
import com.example.emergencynow.domain.model.entity.Gender
import com.example.emergencynow.domain.model.entity.Profile
import com.example.emergencynow.domain.repository.ProfileRepository
import com.example.emergencynow.domain.usecase.auth.GetUserOnboardingStateUseCase
import com.example.emergencynow.domain.usecase.auth.OnboardingState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetUserOnboardingStateUseCaseTest {

    private lateinit var repository: ProfileRepository
    private lateinit var useCase: GetUserOnboardingStateUseCase

    private val fakeProfile = Profile(
        id = "p-1",
        height = 180,
        weight = 75,
        gender = Gender.MALE,
        allergies = null,
        bloodType = "A+",
        illnesses = null,
        medicines = null,
        dateOfBirth = null
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetUserOnboardingStateUseCase(repository)
    }

    @Test
    fun `invoke returns ReturningUser when profile exists`() = runTest {
        coEvery { repository.getMyProfile() } returns Result.success(fakeProfile)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(OnboardingState.ReturningUser, result.getOrNull())
    }

    @Test
    fun `invoke returns NewUser when profile returns 404`() = runTest {
        val notFound = EmergencyError.Generic(httpStatusCode = 404)
        coEvery { repository.getMyProfile() } returns Result.failure(notFound)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(OnboardingState.NewUser, result.getOrNull())
    }

    @Test
    fun `invoke returns failure when repository fails with non-404 error`() = runTest {
        val serverError = EmergencyError.Generic(httpStatusCode = 500, messageString = "Server error")
        coEvery { repository.getMyProfile() } returns Result.failure(serverError)

        val result = useCase()

        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke returns failure for generic non-EmergencyError exceptions`() = runTest {
        coEvery { repository.getMyProfile() } returns Result.failure(Exception("Network timeout"))

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals("Network timeout", result.exceptionOrNull()?.message)
    }
}
