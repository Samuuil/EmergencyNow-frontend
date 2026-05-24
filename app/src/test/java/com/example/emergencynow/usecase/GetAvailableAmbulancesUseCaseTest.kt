package com.example.emergencynow.usecase

import com.example.emergencynow.domain.model.entity.Ambulance
import com.example.emergencynow.domain.repository.AmbulanceRepository
import com.example.emergencynow.domain.usecase.ambulance.GetAvailableAmbulancesUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetAvailableAmbulancesUseCaseTest {

    private lateinit var repository: AmbulanceRepository
    private lateinit var useCase: GetAvailableAmbulancesUseCase

    private fun makeAmbulance(id: String, available: Boolean, model: String = "Sprinter") = Ambulance(
        id = id,
        licensePlate = "CB$id",
        vehicleModel = model,
        latitude = 42.0,
        longitude = 23.0,
        available = available,
        driverId = null,
        lastCallAcceptedAt = null,
        createdAt = null,
        updatedAt = null
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetAvailableAmbulancesUseCase(repository)
    }

    @Test
    fun `invoke maps available ambulance to AVAILABLE status`() = runTest {
        coEvery { repository.getAvailableAmbulances() } returns
            Result.success(listOf(makeAmbulance("1", available = true)))

        val result = useCase()

        assertEquals("AVAILABLE", result.getOrNull()?.first()?.status)
    }

    @Test
    fun `invoke maps unavailable ambulance to BUSY status`() = runTest {
        coEvery { repository.getAvailableAmbulances() } returns
            Result.success(listOf(makeAmbulance("2", available = false)))

        val result = useCase()

        assertEquals("BUSY", result.getOrNull()?.first()?.status)
    }

    @Test
    fun `invoke returns all ambulances mapped correctly`() = runTest {
        val ambulances = listOf(
            makeAmbulance("1", available = true, model = "Sprinter"),
            makeAmbulance("2", available = false, model = "Transit")
        )
        coEvery { repository.getAvailableAmbulances() } returns Result.success(ambulances)

        val result = useCase()

        assertTrue(result.isSuccess)
        val dtos = result.getOrNull()!!
        assertEquals(2, dtos.size)
        assertEquals("Sprinter", dtos[0].type)
        assertEquals("Transit", dtos[1].type)
        assertEquals("AVAILABLE", dtos[0].status)
        assertEquals("BUSY", dtos[1].status)
    }

    @Test
    fun `invoke returns empty list when no ambulances`() = runTest {
        coEvery { repository.getAvailableAmbulances() } returns Result.success(emptyList())

        val result = useCase()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        coEvery { repository.getAvailableAmbulances() } returns
            Result.failure(Exception("Server error"))

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals("Server error", result.exceptionOrNull()?.message)
    }
}
