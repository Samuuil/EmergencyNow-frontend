package com.example.emergencynow.usecase

import com.example.emergencynow.domain.repository.DeviceTokenRepository
import com.example.emergencynow.domain.usecase.notifications.RegisterDeviceTokenUseCase
import com.example.emergencynow.domain.usecase.notifications.UnregisterDeviceTokenUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotificationsUseCasesTest {

    private lateinit var repository: DeviceTokenRepository

    @Before
    fun setUp() {
        repository = mockk()
    }

    // --- RegisterDeviceTokenUseCase ---

    @Test
    fun `Register returns Unit on success`() = runTest {
        coEvery { repository.register("fcm-token-abc") } returns Result.success(Unit)

        val result = RegisterDeviceTokenUseCase(repository)("fcm-token-abc")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `Register forwards token to repository unchanged`() = runTest {
        coEvery { repository.register("my-token") } returns Result.success(Unit)

        RegisterDeviceTokenUseCase(repository)("my-token")

        coVerify(exactly = 1) { repository.register("my-token") }
    }

    @Test
    fun `Register returns failure when server rejects token`() = runTest {
        coEvery { repository.register(any()) } returns Result.failure(Exception("Invalid token"))

        assertTrue(RegisterDeviceTokenUseCase(repository)("bad-token").isFailure)
    }

    // --- UnregisterDeviceTokenUseCase ---

    @Test
    fun `Unregister returns Unit on success`() = runTest {
        coEvery { repository.unregister("fcm-token-abc") } returns Result.success(Unit)

        val result = UnregisterDeviceTokenUseCase(repository)("fcm-token-abc")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `Unregister forwards token to repository unchanged`() = runTest {
        coEvery { repository.unregister("my-token") } returns Result.success(Unit)

        UnregisterDeviceTokenUseCase(repository)("my-token")

        coVerify(exactly = 1) { repository.unregister("my-token") }
    }

    @Test
    fun `Unregister returns failure when token not found`() = runTest {
        coEvery { repository.unregister(any()) } returns Result.failure(Exception("Token not found"))

        assertTrue(UnregisterDeviceTokenUseCase(repository)("unknown-token").isFailure)
    }
}
