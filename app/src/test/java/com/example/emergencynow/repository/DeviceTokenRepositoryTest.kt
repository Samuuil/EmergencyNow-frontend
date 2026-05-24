package com.example.emergencynow.repository

import com.example.emergencynow.data.datasource.DeviceTokenDataSource
import com.example.emergencynow.data.repository.DeviceTokenRepositoryImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeviceTokenRepositoryTest {

    private lateinit var dataSource: DeviceTokenDataSource
    private lateinit var repository: DeviceTokenRepositoryImpl

    @Before
    fun setUp() {
        dataSource = mockk()
        repository = DeviceTokenRepositoryImpl(dataSource)
    }

    @Test
    fun `register returns success and calls data source with token`() = runTest {
        coEvery { dataSource.register("fcm-token-abc") } returns Unit

        val result = repository.register("fcm-token-abc")

        assertTrue(result.isSuccess)
        coVerify { dataSource.register("fcm-token-abc") }
    }

    @Test
    fun `register returns failure when data source throws`() = runTest {
        coEvery { dataSource.register(any()) } throws Exception("Invalid token")

        val result = repository.register("bad-token")

        assertTrue(result.isFailure)
    }

    @Test
    fun `unregister returns success and calls data source with token`() = runTest {
        coEvery { dataSource.unregister("fcm-token-abc") } returns Unit

        val result = repository.unregister("fcm-token-abc")

        assertTrue(result.isSuccess)
        coVerify { dataSource.unregister("fcm-token-abc") }
    }

    @Test
    fun `unregister returns failure when data source throws`() = runTest {
        coEvery { dataSource.unregister(any()) } throws Exception("Token not found")

        val result = repository.unregister("missing-token")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message == "Token not found")
    }
}
