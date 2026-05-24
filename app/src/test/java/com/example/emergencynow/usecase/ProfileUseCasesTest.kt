package com.example.emergencynow.usecase

import com.example.emergencynow.domain.model.entity.Gender
import com.example.emergencynow.domain.model.entity.Profile
import com.example.emergencynow.domain.repository.ProfileRepository
import com.example.emergencynow.domain.usecase.profile.CreateProfileUseCase
import com.example.emergencynow.domain.usecase.profile.GetProfileByEgnUseCase
import com.example.emergencynow.domain.usecase.profile.GetProfileUseCase
import com.example.emergencynow.domain.usecase.profile.UpdateProfileUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProfileUseCasesTest {

    private lateinit var repository: ProfileRepository

    private val fakeProfile = Profile(
        id = "p-1",
        height = 175,
        weight = 70,
        gender = Gender.FEMALE,
        allergies = listOf("Penicillin"),
        bloodType = "B+",
        illnesses = null,
        medicines = null,
        dateOfBirth = "1990-05-15"
    )

    @Before
    fun setUp() {
        repository = mockk()
    }

    // --- GetProfileUseCase ---

    @Test
    fun `GetProfile returns profile on success`() = runTest {
        coEvery { repository.getMyProfile() } returns Result.success(fakeProfile)

        val result = GetProfileUseCase(repository)()

        assertTrue(result.isSuccess)
        assertEquals("p-1", result.getOrNull()?.id)
    }

    @Test
    fun `GetProfile returns failure when not authenticated`() = runTest {
        coEvery { repository.getMyProfile() } returns Result.failure(Exception("Unauthorized"))

        assertTrue(GetProfileUseCase(repository)().isFailure)
    }

    // --- GetProfileByEgnUseCase ---

    @Test
    fun `GetProfileByEgn returns profile for given egn`() = runTest {
        coEvery { repository.getProfileByEgn("1234567890") } returns Result.success(fakeProfile)

        val result = GetProfileByEgnUseCase(repository)("1234567890")

        assertTrue(result.isSuccess)
        assertEquals(175, result.getOrNull()?.height)
    }

    @Test
    fun `GetProfileByEgn forwards egn to repository unchanged`() = runTest {
        coEvery { repository.getProfileByEgn("9876543210") } returns Result.success(fakeProfile)

        GetProfileByEgnUseCase(repository)("9876543210")

        coVerify(exactly = 1) { repository.getProfileByEgn("9876543210") }
    }

    @Test
    fun `GetProfileByEgn returns failure when egn not found`() = runTest {
        coEvery { repository.getProfileByEgn(any()) } returns Result.failure(Exception("Not found"))

        assertTrue(GetProfileByEgnUseCase(repository)("0000000000").isFailure)
    }

    // --- CreateProfileUseCase ---

    @Test
    fun `CreateProfile returns created profile on success`() = runTest {
        coEvery {
            repository.createProfile(175, 70, "FEMALE", listOf("Penicillin"), "B+", null, null, "1990-05-15")
        } returns Result.success(fakeProfile)

        val result = CreateProfileUseCase(repository)(
            height = 175, weight = 70, gender = "FEMALE",
            allergies = listOf("Penicillin"), bloodType = "B+",
            illnesses = null, medicines = null, dateOfBirth = "1990-05-15"
        )

        assertTrue(result.isSuccess)
        assertEquals("B+", result.getOrNull()?.bloodType)
    }

    @Test
    fun `CreateProfile handles null optional fields`() = runTest {
        coEvery {
            repository.createProfile(180, 80, "MALE", null, null, null, null, null)
        } returns Result.success(fakeProfile.copy(allergies = null, bloodType = null))

        val result = CreateProfileUseCase(repository)(
            height = 180, weight = 80, gender = "MALE",
            allergies = null, bloodType = null,
            illnesses = null, medicines = null, dateOfBirth = null
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun `CreateProfile returns failure when profile already exists`() = runTest {
        coEvery { repository.createProfile(any(), any(), any(), any(), any(), any(), any(), any()) } returns
            Result.failure(Exception("Profile already exists"))

        assertTrue(
            CreateProfileUseCase(repository)(175, 70, "MALE", null, null, null, null, null).isFailure
        )
    }

    // --- UpdateProfileUseCase ---

    @Test
    fun `UpdateProfile returns updated profile on success`() = runTest {
        val updated = fakeProfile.copy(weight = 72)
        coEvery {
            repository.updateProfile(175, 72, "FEMALE", listOf("Penicillin"), "B+", null, null, "1990-05-15")
        } returns Result.success(updated)

        val result = UpdateProfileUseCase(repository)(
            height = 175, weight = 72, gender = "FEMALE",
            allergies = listOf("Penicillin"), bloodType = "B+",
            illnesses = null, medicines = null, dateOfBirth = "1990-05-15"
        )

        assertTrue(result.isSuccess)
        assertEquals(72, result.getOrNull()?.weight)
    }

    @Test
    fun `UpdateProfile returns failure on error`() = runTest {
        coEvery { repository.updateProfile(any(), any(), any(), any(), any(), any(), any(), any()) } returns
            Result.failure(Exception("Server error"))

        assertTrue(
            UpdateProfileUseCase(repository)(175, 70, "MALE", null, null, null, null, null).isFailure
        )
    }
}
