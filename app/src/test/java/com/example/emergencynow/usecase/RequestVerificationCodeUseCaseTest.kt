package com.example.emergencynow.usecase

import com.example.emergencynow.domain.repository.AuthRepository
import com.example.emergencynow.domain.usecase.auth.RequestVerificationCodeUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RequestVerificationCodeUseCaseTest {

    private lateinit var repository: AuthRepository
    private lateinit var useCase: RequestVerificationCodeUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = RequestVerificationCodeUseCase(repository)
    }

    @Test
    fun `invoke returns success message when code sent`() = runTest {
        coEvery { repository.requestVerificationCode("1234567890", "sms") } returns
            Result.success("Code sent successfully")

        val result = useCase("1234567890", "sms")

        assertTrue(result.isSuccess)
        assertEquals("Code sent successfully", result.getOrNull())
    }

    @Test
    fun `invoke returns failure when egn not found`() = runTest {
        val error = Exception("EGN not registered")
        coEvery { repository.requestVerificationCode("0000000000", any()) } returns
            Result.failure(error)

        val result = useCase("0000000000", "sms")

        assertTrue(result.isFailure)
        assertEquals("EGN not registered", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke passes method type to repository unchanged`() = runTest {
        coEvery { repository.requestVerificationCode(any(), "email") } returns
            Result.success("ok")

        useCase("1234567890", "email")

        coVerify(exactly = 1) { repository.requestVerificationCode("1234567890", "email") }
    }
}
