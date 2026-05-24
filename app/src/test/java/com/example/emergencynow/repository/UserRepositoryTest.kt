package com.example.emergencynow.repository

import com.example.emergencynow.data.datasource.UserDataSource
import com.example.emergencynow.data.repository.UserRepositoryImpl
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserRepositoryTest {

    private lateinit var dataSource: UserDataSource
    private lateinit var repository: UserRepositoryImpl

    @Before
    fun setUp() {
        dataSource = mockk()
        repository = UserRepositoryImpl(dataSource)
    }

    @Test
    fun `getUserRole returns role string from data source`() = runTest {
        coEvery { dataSource.getUserRole("user-1") } returns "DRIVER"

        val result = repository.getUserRole("user-1")

        assertTrue(result.isSuccess)
        assertEquals("DRIVER", result.getOrNull())
    }

    @Test
    fun `getUserRole returns failure when data source throws`() = runTest {
        coEvery { dataSource.getUserRole(any()) } throws Exception("User not found")

        val result = repository.getUserRole("user-x")

        assertTrue(result.isFailure)
    }

    @Test
    fun `getUserEgn returns EGN from data source`() = runTest {
        coEvery { dataSource.getUserEgn("user-1") } returns "1234567890"

        val result = repository.getUserEgn("user-1")

        assertTrue(result.isSuccess)
        assertEquals("1234567890", result.getOrNull())
    }

    @Test
    fun `getUserEgn returns failure when data source throws`() = runTest {
        coEvery { dataSource.getUserEgn(any()) } throws Exception("Not found")

        val result = repository.getUserEgn("user-x")

        assertTrue(result.isFailure)
    }

    @Test
    fun `getMyEgn returns current user EGN`() = runTest {
        coEvery { dataSource.getMyEgn() } returns "9001011234"

        val result = repository.getMyEgn()

        assertTrue(result.isSuccess)
        assertEquals("9001011234", result.getOrNull())
    }

    @Test
    fun `getMyEgn returns failure when data source throws`() = runTest {
        coEvery { dataSource.getMyEgn() } throws Exception("Unauthorized")

        val result = repository.getMyEgn()

        assertTrue(result.isFailure)
        assertEquals("Unauthorized", result.exceptionOrNull()?.message)
    }
}
