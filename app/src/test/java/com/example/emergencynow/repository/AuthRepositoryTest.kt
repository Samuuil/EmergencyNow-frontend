package com.example.emergencynow.repository

import com.example.emergencynow.data.datasource.AuthDataSource
import com.example.emergencynow.data.repository.AuthRepositoryImpl
import com.example.emergencynow.domain.model.response.TokenResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthRepositoryTest {

    private lateinit var dataSource: AuthDataSource
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setUp() {
        dataSource = mockk()
        repository = AuthRepositoryImpl(dataSource)
    }

    @Test
    fun `requestVerificationCode passes through data source result`() = runTest {
        coEvery { dataSource.initiateLogin("1234567890", "SMS") } returns Result.success("OTP sent")

        val result = repository.requestVerificationCode("1234567890", "SMS")

        assertTrue(result.isSuccess)
        assertEquals("OTP sent", result.getOrNull())
    }

    @Test
    fun `requestVerificationCode propagates failure`() = runTest {
        coEvery { dataSource.initiateLogin(any(), any()) } returns Result.failure(Exception("User not found"))

        val result = repository.requestVerificationCode("0000000000", "SMS")

        assertTrue(result.isFailure)
        assertEquals("User not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `verifyCode maps TokenResponse to Token`() = runTest {
        coEvery { dataSource.verifyCode("1234567890", "123456") } returns
            Result.success(TokenResponse(accessToken = "acc-token", refreshToken = "ref-token"))

        val result = repository.verifyCode("1234567890", "123456")

        assertTrue(result.isSuccess)
        val token = result.getOrNull()!!
        assertEquals("acc-token", token.accessToken)
        assertEquals("ref-token", token.refreshToken)
    }

    @Test
    fun `verifyCode propagates failure`() = runTest {
        coEvery { dataSource.verifyCode(any(), any()) } returns Result.failure(Exception("Invalid code"))

        val result = repository.verifyCode("1234567890", "wrong")

        assertTrue(result.isFailure)
    }

    @Test
    fun `refreshToken maps TokenResponse to Token`() = runTest {
        coEvery { dataSource.refresh("old-refresh") } returns
            Result.success(TokenResponse(accessToken = "new-acc", refreshToken = "new-ref"))

        val result = repository.refreshToken("old-refresh")

        assertTrue(result.isSuccess)
        assertEquals("new-acc", result.getOrNull()!!.accessToken)
        assertEquals("new-ref", result.getOrNull()!!.refreshToken)
    }

    @Test
    fun `refreshToken propagates failure`() = runTest {
        coEvery { dataSource.refresh(any()) } returns Result.failure(Exception("Token expired"))

        val result = repository.refreshToken("bad-token")

        assertTrue(result.isFailure)
    }
}
