package com.example.emergencynow.usecase

import com.example.emergencynow.domain.model.entity.Token
import com.example.emergencynow.domain.repository.AuthRepository
import com.example.emergencynow.domain.usecase.auth.RefreshTokenUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RefreshTokenUseCaseTest {

    private lateinit var repository: AuthRepository
    private lateinit var useCase: RefreshTokenUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = RefreshTokenUseCase(repository)
    }

    @Test
    fun `invoke returns new token on success`() = runTest {
        val newToken = Token(accessToken = "newAccess", refreshToken = "newRefresh")
        coEvery { repository.refreshToken("oldRefresh") } returns Result.success(newToken)

        val result = useCase("oldRefresh")

        assertTrue(result.isSuccess)
        assertEquals(newToken, result.getOrNull())
    }

    @Test
    fun `invoke returns failure when refresh token is expired`() = runTest {
        coEvery { repository.refreshToken(any()) } returns Result.failure(Exception("Token expired"))

        val result = useCase("expiredToken")

        assertTrue(result.isFailure)
        assertEquals("Token expired", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke forwards refresh token to repository unchanged`() = runTest {
        coEvery { repository.refreshToken("myToken") } returns Result.success(Token("a", "b"))

        useCase("myToken")

        coVerify(exactly = 1) { repository.refreshToken("myToken") }
    }
}
