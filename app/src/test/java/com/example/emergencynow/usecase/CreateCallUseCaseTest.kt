package com.example.emergencynow.usecase

import com.example.emergencynow.domain.model.entity.Call
import com.example.emergencynow.domain.model.entity.CallStatus
import com.example.emergencynow.domain.model.request.CreateCallRequest
import com.example.emergencynow.domain.repository.CallRepository
import com.example.emergencynow.domain.usecase.call.CreateCallUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class CreateCallUseCaseTest {

    private lateinit var repository: CallRepository
    private lateinit var useCase: CreateCallUseCase

    private val fakeCall = Call(
        id = "call-1",
        description = "Chest pain",
        latitude = 42.6977,
        longitude = 23.3219,
        status = CallStatus.PENDING,
        routePolyline = null,
        estimatedDistance = null,
        estimatedDuration = null,
        routeSteps = null,
        ambulanceCurrentLatitude = null,
        ambulanceCurrentLongitude = null,
        dispatchedAt = null,
        arrivedAt = null,
        completedAt = null,
        createdAt = Instant.parse("2024-01-01T12:00:00Z"),
        selectedHospitalId = null,
        selectedHospitalName = null,
        hospitalRoutePolyline = null,
        hospitalRouteDistance = null,
        hospitalRouteDuration = null,
        hospitalRouteSteps = null,
        patientEgn = null,
        patientPhoneNumber = "+35988000000"
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase = CreateCallUseCase(repository)
    }

    @Test
    fun `invoke maps Call entity to CallDto with correct fields`() = runTest {
        val request = CreateCallRequest(
            description = "Chest pain",
            latitude = 42.6977,
            longitude = 23.3219,
            patientPhoneNumber = "+35988000000"
        )
        coEvery {
            repository.createCall(
                description = request.description,
                latitude = request.latitude,
                longitude = request.longitude,
                patientPhoneNumber = request.patientPhoneNumber
            )
        } returns Result.success(fakeCall)

        val result = useCase(request, userId = "user-42")

        assertTrue(result.isSuccess)
        val dto = result.getOrNull()!!
        assertEquals("call-1", dto.id)
        assertEquals("user-42", dto.userId)
        assertEquals(42.6977, dto.latitude, 0.0001)
        assertEquals(23.3219, dto.longitude, 0.0001)
        assertEquals("PENDING", dto.status)
        assertEquals("+35988000000", dto.patientPhoneNumber)
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        val request = CreateCallRequest("Accident", 0.0, 0.0)
        coEvery {
            repository.createCall(any(), any(), any(), any())
        } returns Result.failure(Exception("Network error"))

        val result = useCase(request, userId = "user-1")

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke injects userId into dto even though repository does not return it`() = runTest {
        val request = CreateCallRequest("Fall", 1.0, 2.0)
        coEvery { repository.createCall(any(), any(), any(), any()) } returns
            Result.success(fakeCall.copy(id = "call-99"))

        val result = useCase(request, userId = "dispatcher-7")

        assertEquals("dispatcher-7", result.getOrNull()?.userId)
    }
}
