package com.example.emergencynow.repository

import com.example.emergencynow.data.datasource.ProfileDataSource
import com.example.emergencynow.data.repository.ProfileRepositoryImpl
import com.example.emergencynow.domain.model.entity.Gender
import com.example.emergencynow.domain.model.response.ProfileResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProfileRepositoryTest {

    private lateinit var dataSource: ProfileDataSource
    private lateinit var repository: ProfileRepositoryImpl

    @Before
    fun setUp() {
        dataSource = mockk()
        repository = ProfileRepositoryImpl(dataSource)
    }

    private fun fakeProfileResponse() = ProfileResponse(
        id = "prof-1",
        height = 180,
        weight = 80,
        gender = "MALE",
        bloodType = "A+",
        allergies = listOf("Penicillin"),
        illnesses = null,
        medicines = null,
        dateOfBirth = "1990-01-01"
    )

    @Test
    fun `getMyProfile maps response to Profile domain object`() = runTest {
        coEvery { dataSource.getMyProfile() } returns fakeProfileResponse()

        val result = repository.getMyProfile()

        assertTrue(result.isSuccess)
        val profile = result.getOrNull()!!
        assertEquals("prof-1", profile.id)
        assertEquals(180, profile.height)
        assertEquals(80, profile.weight)
        assertEquals(Gender.MALE, profile.gender)
        assertEquals("A+", profile.bloodType)
        assertEquals(listOf("Penicillin"), profile.allergies)
    }

    @Test
    fun `getMyProfile returns failure when data source throws`() = runTest {
        coEvery { dataSource.getMyProfile() } throws Exception("Profile not found")

        val result = repository.getMyProfile()

        assertTrue(result.isFailure)
        assertEquals("Profile not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getProfileByEgn maps response and preserves gender correctly`() = runTest {
        coEvery { dataSource.getProfileByEgn("9001011234") } returns
            fakeProfileResponse().copy(gender = "FEMALE")

        val result = repository.getProfileByEgn("9001011234")

        assertTrue(result.isSuccess)
        assertEquals(Gender.FEMALE, result.getOrNull()!!.gender)
    }

    @Test
    fun `getProfileByEgn maps unknown gender to OTHER`() = runTest {
        coEvery { dataSource.getProfileByEgn(any()) } returns
            fakeProfileResponse().copy(gender = null)

        val result = repository.getProfileByEgn("0000000000")

        assertTrue(result.isSuccess)
        assertEquals(Gender.OTHER, result.getOrNull()!!.gender)
    }

    @Test
    fun `createProfile maps response to Profile`() = runTest {
        coEvery { dataSource.createProfile(180, 80, "MALE", null, "A+", null, null, null) } returns
            fakeProfileResponse()

        val result = repository.createProfile(180, 80, "MALE", null, "A+", null, null, null)

        assertTrue(result.isSuccess)
        assertEquals("prof-1", result.getOrNull()!!.id)
    }

    @Test
    fun `updateProfile maps response to Profile`() = runTest {
        coEvery { dataSource.updateProfile(170, 70, "FEMALE", null, null, null, null, null) } returns
            fakeProfileResponse().copy(height = 170, weight = 70, gender = "FEMALE")

        val result = repository.updateProfile(170, 70, "FEMALE", null, null, null, null, null)

        assertTrue(result.isSuccess)
        val profile = result.getOrNull()!!
        assertEquals(170, profile.height)
        assertEquals(Gender.FEMALE, profile.gender)
    }
}
