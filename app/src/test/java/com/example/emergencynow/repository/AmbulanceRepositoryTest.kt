package com.example.emergencynow.repository

import com.example.emergencynow.data.datasource.AmbulanceDataSource
import com.example.emergencynow.data.repository.AmbulanceRepositoryImpl
import com.example.emergencynow.domain.model.response.AmbulanceDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AmbulanceRepositoryTest {

    private lateinit var dataSource: AmbulanceDataSource
    private lateinit var repository: AmbulanceRepositoryImpl

    @Before
    fun setUp() {
        dataSource = mockk()
        repository = AmbulanceRepositoryImpl(dataSource)
    }

    private fun fakeDto(id: String = "amb-1") = AmbulanceDto(
        id = id,
        licensePlate = "CA1234AB",
        vehicleModel = "Mercedes Sprinter",
        available = true,
        latitude = 42.5,
        longitude = 23.5,
        driverId = "driver-1"
    )

    @Test
    fun `getAvailableAmbulances maps DTO list to Ambulance list`() = runTest {
        coEvery { dataSource.getAvailableAmbulances() } returns listOf(fakeDto("amb-1"), fakeDto("amb-2"))

        val result = repository.getAvailableAmbulances()

        assertTrue(result.isSuccess)
        val ambulances = result.getOrNull()!!
        assertEquals(2, ambulances.size)
        assertEquals("amb-1", ambulances[0].id)
        assertEquals("CA1234AB", ambulances[0].licensePlate)
        assertTrue(ambulances[0].available)
        assertEquals(42.5, ambulances[0].latitude!!, 0.001)
    }

    @Test
    fun `getAvailableAmbulances returns failure when data source throws`() = runTest {
        coEvery { dataSource.getAvailableAmbulances() } throws Exception("Server error")

        val result = repository.getAvailableAmbulances()

        assertTrue(result.isFailure)
    }

    @Test
    fun `getAmbulanceByDriver maps DTO to Ambulance when found`() = runTest {
        coEvery { dataSource.getAmbulanceByDriver("driver-1") } returns fakeDto()

        val result = repository.getAmbulanceByDriver("driver-1")

        assertTrue(result.isSuccess)
        val ambulance = result.getOrNull()!!
        assertEquals("amb-1", ambulance!!.id)
        assertEquals("driver-1", ambulance.driverId)
        assertEquals("Mercedes Sprinter", ambulance.vehicleModel)
    }

    @Test
    fun `getAmbulanceByDriver returns null when driver has no ambulance`() = runTest {
        coEvery { dataSource.getAmbulanceByDriver("driver-99") } returns null

        val result = repository.getAmbulanceByDriver("driver-99")

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun `getAmbulanceByDriver returns failure when data source throws`() = runTest {
        coEvery { dataSource.getAmbulanceByDriver(any()) } throws Exception("Not found")

        val result = repository.getAmbulanceByDriver("driver-x")

        assertTrue(result.isFailure)
    }

    @Test
    fun `assignAmbulanceDriver maps response to Ambulance`() = runTest {
        coEvery { dataSource.assignAmbulanceDriver("amb-1", "driver-2") } returns
            fakeDto().copy(driverId = "driver-2", available = false)

        val result = repository.assignAmbulanceDriver("amb-1", "driver-2")

        assertTrue(result.isSuccess)
        val ambulance = result.getOrNull()!!
        assertEquals("driver-2", ambulance.driverId)
    }

    @Test
    fun `assignAmbulanceDriver with null driverId unassigns driver`() = runTest {
        coEvery { dataSource.assignAmbulanceDriver("amb-1", null) } returns
            fakeDto().copy(driverId = null, available = true)

        val result = repository.assignAmbulanceDriver("amb-1", null)

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull()!!.driverId)
        assertTrue(result.getOrNull()!!.available)
    }
}
