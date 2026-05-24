package com.example.emergencynow.usecase

import com.example.emergencynow.domain.model.entity.CallDetail
import com.example.emergencynow.domain.model.entity.CallStatus
import com.example.emergencynow.domain.repository.CallRepository
import com.example.emergencynow.domain.usecase.call.GetCallByIdUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetCallByIdUseCaseTest {

    private lateinit var repository: CallRepository
    private lateinit var useCase: GetCallByIdUseCase

    private val fakeDetail = CallDetail(
        id = "call-1",
        status = CallStatus.PENDING,
        userEgn = "1234567890",
        patientEgn = null,
        patientPhoneNumber = "+35988000000",
        selectedHospitalId = null
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetCallByIdUseCase(repository)
    }

    @Test
    fun `invoke returns call detail on success`() = runTest {
        coEvery { repository.getCallById("call-1") } returns Result.success(fakeDetail)

        val result = useCase("call-1")

        assertTrue(result.isSuccess)
        assertEquals(fakeDetail, result.getOrNull())
    }

    @Test
    fun `invoke returns failure when call not found`() = runTest {
        coEvery { repository.getCallById(any()) } returns Result.failure(Exception("Not found"))

        val result = useCase("missing-id")

        assertTrue(result.isFailure)
        assertEquals("Not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke forwards callId to repository unchanged`() = runTest {
        coEvery { repository.getCallById("call-99") } returns Result.success(fakeDetail)

        useCase("call-99")

        coVerify(exactly = 1) { repository.getCallById("call-99") }
    }
}
