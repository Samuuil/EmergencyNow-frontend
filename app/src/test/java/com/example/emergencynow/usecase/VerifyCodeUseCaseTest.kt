package com.example.emergencynow.usecase

import com.example.emergencynow.domain.model.entity.Token
import com.example.emergencynow.domain.repository.AuthRepository
import com.example.emergencynow.domain.usecase.auth.VerifyCodeUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VerifyCodeUseCaseTest {

    private lateinit var repository: AuthRepository
    private lateinit var useCase: VerifyCodeUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = VerifyCodeUseCase(repository)
    }

    @Test
    fun `invoke returns token on success`() = runTest {
        val expectedToken = Token(accessToken = "access123", refreshToken = "refresh456")
        coEvery { repository.verifyCode("1234567890", "123456") } returns Result.success(expectedToken)

        val result = useCase("1234567890", "123456")

        assertTrue(result.isSuccess)
        assertEquals(expectedToken, result.getOrNull())
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        val error = Exception("Invalid code")
        coEvery { repository.verifyCode(any(), any()) } returns Result.failure(error)

        val result = useCase("1234567890", "wrong")

        assertTrue(result.isFailure)
        assertEquals("Invalid code", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke delegates egn and code to repository exactly`() = runTest {
        val token = Token("a", "b")
        coEvery { repository.verifyCode("9999999999", "000000") } returns Result.success(token)

        useCase("9999999999", "000000")

        coVerify(exactly = 1) { repository.verifyCode("9999999999", "000000") }
    }
}
