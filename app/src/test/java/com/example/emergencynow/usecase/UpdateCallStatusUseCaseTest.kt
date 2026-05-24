package com.example.emergencynow.usecase

import com.example.emergencynow.domain.model.entity.Call
import com.example.emergencynow.domain.model.entity.CallStatus
import com.example.emergencynow.domain.repository.CallRepository
import com.example.emergencynow.domain.usecase.call.UpdateCallStatusUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateCallStatusUseCaseTest {

    private lateinit var repository: CallRepository
    private lateinit var useCase: UpdateCallStatusUseCase

    private val fakeCall = Call(
        id = "call-1", description = "", latitude = 0.0, longitude = 0.0,
        status = CallStatus.DISPATCHED, routePolyline = null, estimatedDistance = null,
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
        useCase = UpdateCallStatusUseCase(repository)
    }

    @Test
    fun `invoke returns Unit on success`() = runTest {
        coEvery { repository.updateCallStatus("call-1", CallStatus.DISPATCHED) } returns
            Result.success(fakeCall)

        val result = useCase("call-1", CallStatus.DISPATCHED)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        coEvery { repository.updateCallStatus(any(), any()) } returns
            Result.failure(Exception("Call not found"))

        val result = useCase("bad-id", CallStatus.COMPLETED)

        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke forwards callId and status to repository`() = runTest {
        coEvery { repository.updateCallStatus("call-5", CallStatus.COMPLETED) } returns
            Result.success(fakeCall)

        useCase("call-5", CallStatus.COMPLETED)

        coVerify(exactly = 1) { repository.updateCallStatus("call-5", CallStatus.COMPLETED) }
    }
}
