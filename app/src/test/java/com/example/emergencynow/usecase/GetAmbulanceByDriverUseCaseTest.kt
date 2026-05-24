package com.example.emergencynow.usecase

import com.example.emergencynow.domain.model.entity.Ambulance
import com.example.emergencynow.domain.repository.AmbulanceRepository
import com.example.emergencynow.domain.usecase.ambulance.GetAmbulanceByDriverUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetAmbulanceByDriverUseCaseTest {

    private lateinit var repository: AmbulanceRepository
    private lateinit var useCase: GetAmbulanceByDriverUseCase

    private fun fakeAmbulance(available: Boolean, driverId: String? = "driver-1") = Ambulance(
        id = "amb-1", licensePlate = "CB001",
        vehicleModel = "Transit", latitude = 42.0, longitude = 23.0,
        available = available, driverId = driverId,
        lastCallAcceptedAt = null, createdAt = null, updatedAt = null
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetAmbulanceByDriverUseCase(repository)
    }

    @Test
    fun `invoke maps available ambulance to AVAILABLE status dto`() = runTest {
        coEvery { repository.getAmbulanceByDriver("driver-1") } returns
            Result.success(fakeAmbulance(available = true))

        val result = useCase("driver-1")

        assertEquals("AVAILABLE", result.getOrNull()?.status)
    }

    @Test
    fun `invoke maps unavailable ambulance to BUSY status dto`() = runTest {
        coEvery { repository.getAmbulanceByDriver("driver-1") } returns
            Result.success(fakeAmbulance(available = false))

        val result = useCase("driver-1")

        assertEquals("BUSY", result.getOrNull()?.status)
    }

    @Test
    fun `invoke uses driverId as fallback when ambulance has no driverId`() = runTest {
        coEvery { repository.getAmbulanceByDriver("driver-X") } returns
            Result.success(fakeAmbulance(available = true, driverId = null))

        val result = useCase("driver-X")

        assertEquals("driver-X", result.getOrNull()?.driverId)
    }

    @Test
    fun `invoke returns null dto when driver has no ambulance`() = runTest {
        coEvery { repository.getAmbulanceByDriver(any()) } returns Result.success(null)

        val result = useCase("driver-1")

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        coEvery { repository.getAmbulanceByDriver(any()) } returns
            Result.failure(Exception("Server error"))

        val result = useCase("driver-1")

        assertTrue(result.isFailure)
    }
}
