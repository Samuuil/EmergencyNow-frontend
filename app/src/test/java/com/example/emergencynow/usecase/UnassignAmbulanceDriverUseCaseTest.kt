package com.example.emergencynow.usecase

import com.example.emergencynow.domain.model.entity.Ambulance
import com.example.emergencynow.domain.repository.AmbulanceRepository
import com.example.emergencynow.domain.usecase.ambulance.UnassignAmbulanceDriverUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UnassignAmbulanceDriverUseCaseTest {

    private lateinit var repository: AmbulanceRepository
    private lateinit var useCase: UnassignAmbulanceDriverUseCase

    private val fakeAmbulance = Ambulance(
        id = "amb-1", licensePlate = "CB001", vehicleModel = null,
        latitude = null, longitude = null, available = true, driverId = null,
        lastCallAcceptedAt = null, createdAt = null, updatedAt = null
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase = UnassignAmbulanceDriverUseCase(repository)
    }

    @Test
    fun `invoke returns Unit on success`() = runTest {
        coEvery { repository.assignAmbulanceDriver("amb-1", null) } returns
            Result.success(fakeAmbulance)

        val result = useCase("amb-1")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke calls assignAmbulanceDriver with null driverId to clear assignment`() = runTest {
        coEvery { repository.assignAmbulanceDriver("amb-1", null) } returns
            Result.success(fakeAmbulance)

        useCase("amb-1")

        coVerify(exactly = 1) { repository.assignAmbulanceDriver("amb-1", null) }
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        coEvery { repository.assignAmbulanceDriver(any(), null) } returns
            Result.failure(Exception("Ambulance not found"))

        val result = useCase("bad-id")

        assertTrue(result.isFailure)
    }
}
