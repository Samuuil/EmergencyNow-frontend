package com.example.emergencynow.viewmodel

import com.example.emergencynow.domain.model.entity.Contact
import com.example.emergencynow.domain.usecase.contact.CreateContactUseCase
import com.example.emergencynow.domain.usecase.contact.DeleteContactUseCase
import com.example.emergencynow.domain.usecase.contact.GetContactsUseCase
import com.example.emergencynow.domain.usecase.contact.UpdateContactUseCase
import com.example.emergencynow.ui.feature.contacts.EmergencyContactsViewModel
import com.example.emergencynow.ui.util.AuthSession
import com.example.emergencynow.ui.util.NotificationManager
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EmergencyContactsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getContacts: GetContactsUseCase
    private lateinit var createContact: CreateContactUseCase
    private lateinit var updateContact: UpdateContactUseCase
    private lateinit var deleteContact: DeleteContactUseCase
    private lateinit var notificationManager: NotificationManager

    private val fakeContact = Contact(id = "c-1", name = "Alice", phoneNumber = "+359111", email = null)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getContacts = mockk()
        createContact = mockk()
        updateContact = mockk()
        deleteContact = mockk()
        notificationManager = mockk(relaxed = true)
        AuthSession.userId = "user-1"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        AuthSession.userId = null
    }

    private fun buildVm() = EmergencyContactsViewModel(
        getContacts, createContact, updateContact, deleteContact, notificationManager
    )

    @Test
    fun `init loads contacts from repository`() = runTest {
        coEvery { getContacts() } returns Result.success(listOf(fakeContact))

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(listOf(fakeContact), vm.uiState.value.contacts)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `init shows empty contact placeholder when no contacts returned`() = runTest {
        coEvery { getContacts() } returns Result.success(emptyList())

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.contacts.size)
        assertEquals("", vm.uiState.value.contacts.first().id)
    }

    @Test
    fun `addContact adds a new empty slot`() = runTest {
        coEvery { getContacts() } returns Result.success(listOf(fakeContact))

        val vm = buildVm()
        advanceUntilIdle()
        vm.addContact()

        assertEquals(2, vm.uiState.value.contacts.size)
    }

    @Test
    fun `addContact respects maximum of 5 contacts`() = runTest {
        val contacts = (1..5).map { fakeContact.copy(id = "c-$it") }
        coEvery { getContacts() } returns Result.success(contacts)

        val vm = buildVm()
        advanceUntilIdle()
        vm.addContact()

        assertEquals(5, vm.uiState.value.contacts.size)
    }

    @Test
    fun `updateContact replaces contact at given index`() = runTest {
        coEvery { getContacts() } returns Result.success(listOf(fakeContact))

        val vm = buildVm()
        advanceUntilIdle()

        val updated = fakeContact.copy(name = "Bob")
        vm.updateContact(0, updated)

        assertEquals("Bob", vm.uiState.value.contacts[0].name)
    }

    @Test
    fun `removeContact removes entry with empty id without calling repository`() = runTest {
        coEvery { getContacts() } returns Result.success(emptyList())

        val vm = buildVm()
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.contacts.size)

        vm.removeContact(0)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.contacts.isEmpty())
    }

    @Test
    fun `saveContacts shows error when all contacts are blank`() = runTest {
        coEvery { getContacts() } returns Result.success(emptyList())

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveContacts {}
        advanceUntilIdle()

        io.mockk.verify { notificationManager.showError(any()) }
    }

    @Test
    fun `saveContacts creates new contacts and calls onSuccess`() = runTest {
        coEvery { getContacts() } returns Result.success(emptyList())
        coEvery { createContact(any(), any(), any()) } returns Result.success(fakeContact)

        val vm = buildVm()
        advanceUntilIdle()

        val newContact = Contact(id = "", name = "Alice", phoneNumber = "+359111", email = null)
        vm.updateContact(0, newContact)

        var callbackCalled = false
        vm.saveContacts { callbackCalled = true }
        advanceUntilIdle()

        assertTrue(callbackCalled)
        assertFalse(vm.uiState.value.isSaving)
    }
}
