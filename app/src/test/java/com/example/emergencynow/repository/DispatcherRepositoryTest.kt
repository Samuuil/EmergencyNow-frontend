package com.example.emergencynow.repository

import com.example.emergencynow.data.datasource.DispatcherDataSource
import com.example.emergencynow.data.repository.DispatcherRepositoryImpl
import com.example.emergencynow.domain.model.response.DispatcherAmbulanceSummaryDto
import com.example.emergencynow.domain.model.response.DispatcherCallDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DispatcherRepositoryTest {

    private lateinit var dataSource: DispatcherDataSource
    private lateinit var repository: DispatcherRepositoryImpl

    @Before
    fun setUp() {
        dataSource = mockk()
        repository = DispatcherRepositoryImpl(dataSource)
    }

    @Test
    fun `getMyCalls maps DTOs to DispatcherCallOffer list`() = runTest {
        coEvery { dataSource.getMyCalls() } returns listOf(
            DispatcherCallDto(callId = "c-1", latitude = 42.0, longitude = 23.0, createdAt = "2025-01-01T10:00:00Z", userName = "Ivan"),
            DispatcherCallDto(callId = "c-2", latitude = 42.1, longitude = 23.1, createdAt = "2025-01-01T11:00:00Z")
        )

        val result = repository.getMyCalls()

        assertTrue(result.isSuccess)
        val calls = result.getOrNull()!!
        assertEquals(2, calls.size)
        assertEquals("c-1", calls[0].callId)
        assertEquals("Ivan", calls[0].userName)
        assertEquals(42.0, calls[0].latitude, 0.001)
    }

    @Test
    fun `getMyCalls returns failure when data source throws`() = runTest {
        coEvery { dataSource.getMyCalls() } throws Exception("Unauthorized")

        val result = repository.getMyCalls()

        assertTrue(result.isFailure)
    }

    @Test
    fun `getAvailableAmbulances maps DTOs to DispatcherAmbulanceSummary list`() = runTest {
        coEvery { dataSource.getAvailableAmbulances() } returns listOf(
            DispatcherAmbulanceSummaryDto(id = "amb-1", licensePlate = "CA1234AB", available = true, driverOnline = true),
            DispatcherAmbulanceSummaryDto(id = "amb-2", licensePlate = "CB5678CD", available = false, driverOnline = false)
        )

        val result = repository.getAvailableAmbulances()

        assertTrue(result.isSuccess)
        val ambulances = result.getOrNull()!!
        assertEquals(2, ambulances.size)
        assertEquals("amb-1", ambulances[0].id)
        assertTrue(ambulances[0].available)
        assertTrue(ambulances[0].driverOnline)
        assertEquals("CB5678CD", ambulances[1].licensePlate)
    }

    @Test
    fun `getAvailableAmbulances returns failure when data source throws`() = runTest {
        coEvery { dataSource.getAvailableAmbulances() } throws Exception("Server error")

        val result = repository.getAvailableAmbulances()

        assertTrue(result.isFailure)
    }

    @Test
    fun `assignAmbulance returns success and calls through to data source`() = runTest {
        coEvery { dataSource.assignAmbulance("call-1", "amb-1") } returns Unit

        val result = repository.assignAmbulance("call-1", "amb-1")

        assertTrue(result.isSuccess)
        coVerify { dataSource.assignAmbulance("call-1", "amb-1") }
    }

    @Test
    fun `assignAmbulance returns failure when data source throws`() = runTest {
        coEvery { dataSource.assignAmbulance(any(), any()) } throws Exception("Ambulance already assigned")

        val result = repository.assignAmbulance("call-1", "amb-busy")

        assertTrue(result.isFailure)
        assertEquals("Ambulance already assigned", result.exceptionOrNull()?.message)
    }
}
