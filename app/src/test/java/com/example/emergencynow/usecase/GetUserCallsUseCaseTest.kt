package com.example.emergencynow.usecase

import com.example.emergencynow.domain.model.entity.Call
import com.example.emergencynow.domain.model.entity.CallStatus
import com.example.emergencynow.domain.repository.CallRepository
import com.example.emergencynow.domain.usecase.call.GetUserCallsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetUserCallsUseCaseTest {

    private lateinit var repository: CallRepository
    private lateinit var useCase: GetUserCallsUseCase

    private fun fakeCall(id: String) = Call(
        id = id, description = "desc", latitude = 0.0, longitude = 0.0,
        status = CallStatus.PENDING, routePolyline = null, estimatedDistance = null,
        estimatedDuration = null, routeSteps = null, ambulanceCurrentLatitude = null,
        ambulanceCurrentLongitude = null, dispatchedAt = null, arrivedAt = null,
        completedAt = null, createdAt = null, selectedHospitalId = null,
        selectedHospitalName = null, hospitalRoutePolyline = null,
        hospitalRouteDistance = null, hospitalRouteDuration = null,
        hospitalRouteSteps = null
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetUserCallsUseCase(repository)
    }

    @Test
    fun `invoke returns list of calls on success`() = runTest {
        val calls = listOf(fakeCall("c1"), fakeCall("c2"))
        coEvery { repository.getMyCalls(any(), any()) } returns Result.success(calls)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `invoke returns empty list when user has no calls`() = runTest {
        coEvery { repository.getMyCalls(any(), any()) } returns Result.success(emptyList())

        val result = useCase()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `invoke forwards page and limit parameters to repository`() = runTest {
        coEvery { repository.getMyCalls(2, 10) } returns Result.success(emptyList())

        useCase(page = 2, limit = 10)

        coVerify(exactly = 1) { repository.getMyCalls(2, 10) }
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        coEvery { repository.getMyCalls(any(), any()) } returns Result.failure(Exception("Unauthorized"))

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals("Unauthorized", result.exceptionOrNull()?.message)
    }
}
