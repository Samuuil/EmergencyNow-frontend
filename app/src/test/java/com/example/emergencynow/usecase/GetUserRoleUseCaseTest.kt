package com.example.emergencynow.usecase

import com.example.emergencynow.domain.repository.UserRepository
import com.example.emergencynow.domain.usecase.user.GetUserRoleUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetUserRoleUseCaseTest {

    private lateinit var repository: UserRepository
    private lateinit var useCase: GetUserRoleUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetUserRoleUseCase(repository)
    }

    @Test
    fun `invoke returns role string on success`() = runTest {
        coEvery { repository.getUserRole("user-1") } returns Result.success("DISPATCHER")

        val result = useCase("user-1")

        assertTrue(result.isSuccess)
        assertEquals("DISPATCHER", result.getOrNull())
    }

    @Test
    fun `invoke returns driver role correctly`() = runTest {
        coEvery { repository.getUserRole("user-2") } returns Result.success("DRIVER")

        val result = useCase("user-2")

        assertEquals("DRIVER", result.getOrNull())
    }

    @Test
    fun `invoke returns failure when user not found`() = runTest {
        coEvery { repository.getUserRole(any()) } returns Result.failure(Exception("User not found"))

        val result = useCase("unknown-id")

        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke forwards userId to repository unchanged`() = runTest {
        coEvery { repository.getUserRole("u-99") } returns Result.success("USER")

        useCase("u-99")

        coVerify(exactly = 1) { repository.getUserRole("u-99") }
    }
}
