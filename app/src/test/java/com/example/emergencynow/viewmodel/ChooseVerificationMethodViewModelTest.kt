package com.example.emergencynow.viewmodel

import com.example.emergencynow.domain.usecase.auth.RequestVerificationCodeUseCase
import com.example.emergencynow.ui.feature.auth.ChooseVerificationMethodViewModel
import com.example.emergencynow.ui.util.AuthSession
import com.example.emergencynow.ui.util.NotificationManager
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChooseVerificationMethodViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var requestCodeUseCase: RequestVerificationCodeUseCase
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        requestCodeUseCase = mockk()
        notificationManager = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        AuthSession.egn = null
    }

    private fun buildVm() = ChooseVerificationMethodViewModel(requestCodeUseCase, notificationManager)

    @Test
    fun `requestVerificationCode shows error when egn is missing`() = runTest {
        AuthSession.egn = null

        val vm = buildVm()
        vm.requestVerificationCode("sms") {}
        advanceUntilIdle()

        verify { notificationManager.showError(any()) }
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `requestVerificationCode success calls onSuccess and clears loading`() = runTest {
        AuthSession.egn = "1234567890"
        coEvery { requestCodeUseCase("1234567890", "sms") } returns Result.success("ok")

        val vm = buildVm()
        var callbackCalled = false
        vm.requestVerificationCode("sms") { callbackCalled = true }
        advanceUntilIdle()

        assertTrue(callbackCalled)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `requestVerificationCode failure shows error and clears loading`() = runTest {
        AuthSession.egn = "1234567890"
        coEvery { requestCodeUseCase(any(), any()) } returns Result.failure(Exception("Failed"))

        val vm = buildVm()
        vm.requestVerificationCode("sms") {}
        advanceUntilIdle()

        verify { notificationManager.showError(any()) }
        assertFalse(vm.uiState.value.isLoading)
    }

}
