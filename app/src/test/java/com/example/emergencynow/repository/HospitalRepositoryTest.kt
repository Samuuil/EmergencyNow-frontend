package com.example.emergencynow.repository

import com.example.emergencynow.data.datasource.HospitalDataSource
import com.example.emergencynow.data.repository.HospitalRepositoryImpl
import com.example.emergencynow.domain.model.response.HospitalDto
import com.example.emergencynow.domain.model.response.HospitalRouteResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HospitalRepositoryTest {

    private lateinit var dataSource: HospitalDataSource
    private lateinit var repository: HospitalRepositoryImpl

    @Before
    fun setUp() {
        dataSource = mockk()
        repository = HospitalRepositoryImpl(dataSource)
    }

    @Test
    fun `getHospitalsForCall maps DTOs to NearbyHospital list`() = runTest {
        val dtos = listOf(
            HospitalDto(id = "h-1", name = "City Hospital", latitude = 42.1, longitude = 23.1, distance = 500, availableBeds = 10),
            HospitalDto(id = "h-2", name = "Emergency Center", latitude = 42.2, longitude = 23.2, distance = 1200)
        )
        coEvery { dataSource.getHospitalsForCall("call-1", 42.0, 23.0) } returns dtos

        val result = repository.getHospitalsForCall("call-1", 42.0, 23.0)

        assertTrue(result.isSuccess)
        val hospitals = result.getOrNull()!!
        assertEquals(2, hospitals.size)
        assertEquals("h-1", hospitals[0].id)
        assertEquals("City Hospital", hospitals[0].name)
        assertEquals(500, hospitals[0].distanceMeters)
        assertEquals(10, hospitals[0].availableBeds)
        assertEquals(42.2, hospitals[1].latitude, 0.001)
    }

    @Test
    fun `getHospitalsForCall returns failure when data source throws`() = runTest {
        coEvery { dataSource.getHospitalsForCall(any(), any(), any()) } throws Exception("Network error")

        val result = repository.getHospitalsForCall("call-1", 0.0, 0.0)

        assertTrue(result.isFailure)
    }

    @Test
    fun `selectHospitalForCall returns success`() = runTest {
        coEvery { dataSource.selectHospitalForCall("call-1", "h-1", 42.0, 23.0) } returns Unit

        val result = repository.selectHospitalForCall("call-1", "h-1", 42.0, 23.0)

        assertTrue(result.isSuccess)
        coVerify { dataSource.selectHospitalForCall("call-1", "h-1", 42.0, 23.0) }
    }

    @Test
    fun `selectHospitalForCall returns failure when data source throws`() = runTest {
        coEvery { dataSource.selectHospitalForCall(any(), any(), any(), any()) } throws Exception("Hospital unavailable")

        val result = repository.selectHospitalForCall("call-1", "h-x", 0.0, 0.0)

        assertTrue(result.isFailure)
    }

    @Test
    fun `getHospitalRoute maps response to HospitalRoute`() = runTest {
        val response = HospitalRouteResponse(
            polyline = "encoded_polyline_string",
            distance = 3500,
            duration = 420,
            steps = listOf("Turn left", "Continue 2km")
        )
        coEvery { dataSource.getHospitalRoute("call-1") } returns response

        val result = repository.getHospitalRoute("call-1")

        assertTrue(result.isSuccess)
        val route = result.getOrNull()!!
        assertEquals("encoded_polyline_string", route.polyline)
        assertEquals(3500, route.distance)
        assertEquals(420, route.duration)
        assertEquals(2, route.steps.size)
        assertEquals("Turn left", route.steps[0])
    }

    @Test
    fun `getHospitalRoute preserves null polyline`() = runTest {
        coEvery { dataSource.getHospitalRoute(any()) } returns
            HospitalRouteResponse(polyline = null, distance = 0, duration = 0)

        val result = repository.getHospitalRoute("call-1")

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull()!!.polyline)
    }
}
