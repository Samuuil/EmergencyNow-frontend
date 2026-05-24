package com.example.emergencynow.usecase

import com.example.emergencynow.domain.model.entity.HospitalRoute
import com.example.emergencynow.domain.model.entity.NearbyHospital
import com.example.emergencynow.domain.repository.HospitalRepository
import com.example.emergencynow.domain.usecase.hospital.GetHospitalRouteUseCase
import com.example.emergencynow.domain.usecase.hospital.GetHospitalsForCallUseCase
import com.example.emergencynow.domain.usecase.hospital.SelectHospitalUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HospitalUseCasesTest {

    private lateinit var repository: HospitalRepository

    private val fakeHospital = NearbyHospital(
        id = "h-1", name = "City Hospital",
        latitude = 42.0, longitude = 23.0,
        distanceMeters = 500, availableBeds = 10, address = "Sofia"
    )

    private val fakeRoute = HospitalRoute(
        polyline = "encoded_polyline", distance = 500, duration = 120, steps = emptyList()
    )

    @Before
    fun setUp() {
        repository = mockk()
    }

    // --- GetHospitalsForCallUseCase ---

    @Test
    fun `GetHospitalsForCall returns list of hospitals on success`() = runTest {
        coEvery { repository.getHospitalsForCall("call-1", 42.0, 23.0) } returns
            Result.success(listOf(fakeHospital))

        val result = GetHospitalsForCallUseCase(repository)("call-1", 42.0, 23.0)

        assertTrue(result.isSuccess)
        assertEquals("City Hospital", result.getOrNull()?.first()?.name)
    }

    @Test
    fun `GetHospitalsForCall returns empty list when none nearby`() = runTest {
        coEvery { repository.getHospitalsForCall(any(), any(), any()) } returns
            Result.success(emptyList())

        val result = GetHospitalsForCallUseCase(repository)("call-1", 0.0, 0.0)

        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `GetHospitalsForCall forwards all parameters to repository`() = runTest {
        coEvery { repository.getHospitalsForCall("call-5", 41.5, 22.8) } returns
            Result.success(emptyList())

        GetHospitalsForCallUseCase(repository)("call-5", 41.5, 22.8)

        coVerify(exactly = 1) { repository.getHospitalsForCall("call-5", 41.5, 22.8) }
    }

    @Test
    fun `GetHospitalsForCall returns failure on error`() = runTest {
        coEvery { repository.getHospitalsForCall(any(), any(), any()) } returns
            Result.failure(Exception("Server error"))

        assertTrue(GetHospitalsForCallUseCase(repository)("call-1", 0.0, 0.0).isFailure)
    }

    // --- SelectHospitalUseCase ---

    @Test
    fun `SelectHospital returns Unit on success`() = runTest {
        coEvery { repository.selectHospitalForCall("call-1", "h-1", 42.0, 23.0) } returns
            Result.success(Unit)

        val result = SelectHospitalUseCase(repository)("call-1", "h-1", 42.0, 23.0)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `SelectHospital forwards all parameters to repository`() = runTest {
        coEvery { repository.selectHospitalForCall("c-1", "h-2", 41.0, 22.0) } returns
            Result.success(Unit)

        SelectHospitalUseCase(repository)("c-1", "h-2", 41.0, 22.0)

        coVerify(exactly = 1) { repository.selectHospitalForCall("c-1", "h-2", 41.0, 22.0) }
    }

    @Test
    fun `SelectHospital returns failure on error`() = runTest {
        coEvery { repository.selectHospitalForCall(any(), any(), any(), any()) } returns
            Result.failure(Exception("Hospital unavailable"))

        assertTrue(SelectHospitalUseCase(repository)("c-1", "h-1", 0.0, 0.0).isFailure)
    }

    // --- GetHospitalRouteUseCase ---

    @Test
    fun `GetHospitalRoute returns route on success`() = runTest {
        coEvery { repository.getHospitalRoute("call-1") } returns Result.success(fakeRoute)

        val result = GetHospitalRouteUseCase(repository)("call-1")

        assertTrue(result.isSuccess)
        assertEquals(500, result.getOrNull()?.distance)
        assertEquals(120, result.getOrNull()?.duration)
    }

    @Test
    fun `GetHospitalRoute returns failure when no route found`() = runTest {
        coEvery { repository.getHospitalRoute(any()) } returns
            Result.failure(Exception("Route not available"))

        assertTrue(GetHospitalRouteUseCase(repository)("call-1").isFailure)
    }

    @Test
    fun `GetHospitalRoute forwards callId to repository`() = runTest {
        coEvery { repository.getHospitalRoute("call-99") } returns Result.success(fakeRoute)

        GetHospitalRouteUseCase(repository)("call-99")

        coVerify(exactly = 1) { repository.getHospitalRoute("call-99") }
    }
}
