package com.example.emergencynow.repository

import com.example.emergencynow.data.datasource.CallDataSource
import com.example.emergencynow.data.repository.CallRepositoryImpl
import com.example.emergencynow.domain.model.entity.CallStatus
import com.example.emergencynow.domain.model.response.CallResponse
import com.example.emergencynow.domain.model.response.PaginatedResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CallRepositoryTest {

    private lateinit var dataSource: CallDataSource
    private lateinit var repository: CallRepositoryImpl

    @Before
    fun setUp() {
        dataSource = mockk()
        repository = CallRepositoryImpl(dataSource)
    }

    private fun fakeCallResponse(
        id: String = "call-1",
        status: String = "pending",
        description: String = "Chest pain",
        latitude: Double = 42.0,
        longitude: Double = 23.0
    ) = CallResponse(id = id, status = status, description = description, latitude = latitude, longitude = longitude)

    @Test
    fun `createCall maps response to Call domain object`() = runTest {
        coEvery { dataSource.createCall("Chest pain", 42.0, 23.0, null) } returns fakeCallResponse()

        val result = repository.createCall("Chest pain", 42.0, 23.0, null)

        assertTrue(result.isSuccess)
        val call = result.getOrNull()!!
        assertEquals("call-1", call.id)
        assertEquals(CallStatus.PENDING, call.status)
        assertEquals("Chest pain", call.description)
        assertEquals(42.0, call.latitude, 0.001)
    }

    @Test
    fun `createCall returns failure when data source throws`() = runTest {
        coEvery { dataSource.createCall(any(), any(), any(), any()) } throws Exception("Network error")

        val result = repository.createCall("desc", 0.0, 0.0, null)

        assertTrue(result.isFailure)
    }

    @Test
    fun `updateCallStatus maps response and passes wire value`() = runTest {
        coEvery { dataSource.updateCallStatus("call-1", "en_route") } returns
            fakeCallResponse(status = "en_route")

        val result = repository.updateCallStatus("call-1", CallStatus.EN_ROUTE)

        assertTrue(result.isSuccess)
        assertEquals(CallStatus.EN_ROUTE, result.getOrNull()!!.status)
    }

    @Test
    fun `getMyCalls maps paginated response to list of Call`() = runTest {
        val paginated = PaginatedResponse(
            data = listOf(fakeCallResponse("c1", "pending"), fakeCallResponse("c2", "completed")),
            meta = null,
            links = null
        )
        coEvery { dataSource.getMyCalls(null, null) } returns paginated

        val result = repository.getMyCalls(null, null)

        assertTrue(result.isSuccess)
        val calls = result.getOrNull()!!
        assertEquals(2, calls.size)
        assertEquals("c1", calls[0].id)
        assertEquals("c2", calls[1].id)
        assertEquals(CallStatus.COMPLETED, calls[1].status)
    }

    @Test
    fun `getCallById maps response to CallDetail`() = runTest {
        val response = fakeCallResponse(id = "call-99", status = "dispatched").copy(
            userEgn = "1234567890",
            patientEgn = "0987654321",
            selectedHospitalId = "hosp-1"
        )
        coEvery { dataSource.getCallById("call-99") } returns response

        val result = repository.getCallById("call-99")

        assertTrue(result.isSuccess)
        val detail = result.getOrNull()!!
        assertEquals("call-99", detail.id)
        assertEquals(CallStatus.DISPATCHED, detail.status)
        assertEquals("1234567890", detail.userEgn)
        assertEquals("0987654321", detail.patientEgn)
        assertEquals("hosp-1", detail.selectedHospitalId)
    }

    @Test
    fun `getCallById returns failure when data source throws`() = runTest {
        coEvery { dataSource.getCallById(any()) } throws Exception("Not found")

        val result = repository.getCallById("missing")

        assertTrue(result.isFailure)
    }
}
