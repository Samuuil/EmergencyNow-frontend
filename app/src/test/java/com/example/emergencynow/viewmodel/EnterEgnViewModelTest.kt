package com.example.emergencynow.viewmodel

import com.example.emergencynow.ui.feature.auth.EnterEgnAction
import com.example.emergencynow.ui.feature.auth.EnterEgnViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EnterEgnViewModelTest {

    private lateinit var viewModel: EnterEgnViewModel

    @Before
    fun setUp() {
        viewModel = EnterEgnViewModel()
    }

    @Test
    fun `OnEgnChanged updates egn and clears error`() {
        viewModel.onAction(EnterEgnAction.OnEgnChanged("123"))
        assertEquals("123", viewModel.state.value.egn)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `OnContinueClicked with valid 10-digit egn sets shouldNavigateToVerification`() {
        viewModel.onAction(EnterEgnAction.OnEgnChanged("1234567890"))
        viewModel.onAction(EnterEgnAction.OnContinueClicked)
        assertTrue(viewModel.state.value.shouldNavigateToVerification)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `OnContinueClicked with 9-digit egn sets error`() {
        viewModel.onAction(EnterEgnAction.OnEgnChanged("123456789"))
        viewModel.onAction(EnterEgnAction.OnContinueClicked)
        assertFalse(viewModel.state.value.shouldNavigateToVerification)
        assertEquals("EGN must be exactly 10 digits", viewModel.state.value.error)
    }

    @Test
    fun `OnContinueClicked with 11-digit egn sets error`() {
        viewModel.onAction(EnterEgnAction.OnEgnChanged("12345678901"))
        viewModel.onAction(EnterEgnAction.OnContinueClicked)
        assertFalse(viewModel.state.value.shouldNavigateToVerification)
        assertNull(viewModel.state.value.error, null)
    }

    @Test
    fun `OnContinueClicked with letters in egn sets error`() {
        viewModel.onAction(EnterEgnAction.OnEgnChanged("123456789a"))
        viewModel.onAction(EnterEgnAction.OnContinueClicked)
        assertFalse(viewModel.state.value.shouldNavigateToVerification)
        assertEquals("EGN must be exactly 10 digits", viewModel.state.value.error)
    }

    @Test
    fun `OnNavigationHandled clears shouldNavigateToVerification`() {
        viewModel.onAction(EnterEgnAction.OnEgnChanged("1234567890"))
        viewModel.onAction(EnterEgnAction.OnContinueClicked)
        assertTrue(viewModel.state.value.shouldNavigateToVerification)

        viewModel.onAction(EnterEgnAction.OnNavigationHandled)
        assertFalse(viewModel.state.value.shouldNavigateToVerification)
    }
}
