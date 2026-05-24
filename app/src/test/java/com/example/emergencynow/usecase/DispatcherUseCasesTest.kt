package com.example.emergencynow.usecase

import com.example.emergencynow.domain.model.entity.DispatcherAmbulanceSummary
import com.example.emergencynow.domain.model.entity.DispatcherCallOffer
import com.example.emergencynow.domain.repository.DispatcherRepository
import com.example.emergencynow.domain.usecase.dispatcher.AssignAmbulanceUseCase
import com.example.emergencynow.domain.usecase.dispatcher.GetAvailableAmbulancesForDispatcherUseCase
import com.example.emergencynow.domain.usecase.dispatcher.GetDispatcherCallsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DispatcherUseCasesTest {

    private lateinit var repository: DispatcherRepository

    private val fakeCall = DispatcherCallOffer(
        callId = "call-1", description = "Chest pain",
        latitude = 42.0, longitude = 23.0, createdAt = "2024-01-01", userName = "John"
    )

    private val fakeAmbulance = DispatcherAmbulanceSummary(
        id = "amb-1", licensePlate = "CB001", vehicleModel = "Sprinter",
        latitude = 42.0, longitude = 23.0, driverId = "driver-1",
        driverOnline = true, available = true
    )

    @Before
    fun setUp() {
        repository = mockk()
    }

    // --- GetDispatcherCallsUseCase ---

    @Test
    fun `GetDispatcherCalls returns list of calls on success`() = runTest {
        coEvery { repository.getMyCalls() } returns Result.success(listOf(fakeCall))

        val result = GetDispatcherCallsUseCase(repository)()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun `GetDispatcherCalls returns empty list when no pending calls`() = runTest {
        coEvery { repository.getMyCalls() } returns Result.success(emptyList())

        val result = GetDispatcherCallsUseCase(repository)()

        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `GetDispatcherCalls returns failure on error`() = runTest {
        coEvery { repository.getMyCalls() } returns Result.failure(Exception("Unauthorized"))

        assertTrue(GetDispatcherCallsUseCase(repository)().isFailure)
    }

    // --- GetAvailableAmbulancesForDispatcherUseCase ---

    @Test
    fun `GetAvailableAmbulancesForDispatcher returns ambulances on success`() = runTest {
        coEvery { repository.getAvailableAmbulances() } returns Result.success(listOf(fakeAmbulance))

        val result = GetAvailableAmbulancesForDispatcherUseCase(repository)()

        assertTrue(result.isSuccess)
        assertEquals("amb-1", result.getOrNull()?.first()?.id)
    }

    @Test
    fun `GetAvailableAmbulancesForDispatcher returns empty list when none available`() = runTest {
        coEvery { repository.getAvailableAmbulances() } returns Result.success(emptyList())

        val result = GetAvailableAmbulancesForDispatcherUseCase(repository)()

        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `GetAvailableAmbulancesForDispatcher returns failure on error`() = runTest {
        coEvery { repository.getAvailableAmbulances() } returns Result.failure(Exception("Server error"))

        assertTrue(GetAvailableAmbulancesForDispatcherUseCase(repository)().isFailure)
    }

    // --- AssignAmbulanceUseCase ---

    @Test
    fun `AssignAmbulance returns Unit on success`() = runTest {
        coEvery { repository.assignAmbulance("call-1", "amb-1") } returns Result.success(Unit)

        val result = AssignAmbulanceUseCase(repository)("call-1", "amb-1")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `AssignAmbulance forwards callId and ambulanceId to repository`() = runTest {
        coEvery { repository.assignAmbulance("call-5", "amb-3") } returns Result.success(Unit)

        AssignAmbulanceUseCase(repository)("call-5", "amb-3")

        coVerify(exactly = 1) { repository.assignAmbulance("call-5", "amb-3") }
    }

    @Test
    fun `AssignAmbulance returns failure on error`() = runTest {
        coEvery { repository.assignAmbulance(any(), any()) } returns
            Result.failure(Exception("Ambulance already assigned"))

        assertTrue(AssignAmbulanceUseCase(repository)("call-1", "amb-1").isFailure)
    }
}
