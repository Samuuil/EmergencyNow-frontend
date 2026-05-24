package com.example.emergencynow.usecase

import com.example.emergencynow.domain.repository.AmbulanceRepository
import com.example.emergencynow.domain.usecase.ambulance.AssignAmbulanceDriverUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import com.example.emergencynow.domain.model.entity.Ambulance

class AssignAmbulanceDriverUseCaseTest {

    private lateinit var repository: AmbulanceRepository
    private lateinit var useCase: AssignAmbulanceDriverUseCase

    private val fakeAmbulance = Ambulance(
        id = "amb-1", licensePlate = "CB001",
        vehicleModel = "Sprinter", latitude = null, longitude = null,
        available = false, driverId = "driver-1",
        lastCallAcceptedAt = null, createdAt = null, updatedAt = null
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase = AssignAmbulanceDriverUseCase(repository)
    }

    @Test
    fun `invoke returns Unit on success`() = runTest {
        coEvery { repository.assignAmbulanceDriver("amb-1", "driver-1") } returns
            Result.success(fakeAmbulance)

        val result = useCase(driverId = "driver-1", ambulanceId = "amb-1")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke returns failure when assignment fails`() = runTest {
        coEvery { repository.assignAmbulanceDriver(any(), any()) } returns
            Result.failure(Exception("Ambulance unavailable"))

        val result = useCase(driverId = "driver-1", ambulanceId = "amb-bad")

        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke passes ambulanceId and driverId to repository in correct order`() = runTest {
        coEvery { repository.assignAmbulanceDriver("amb-2", "driver-5") } returns
            Result.success(fakeAmbulance)

        useCase(driverId = "driver-5", ambulanceId = "amb-2")

        coVerify(exactly = 1) { repository.assignAmbulanceDriver("amb-2", "driver-5") }
    }
}
