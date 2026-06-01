package com.example.emergencynow.usecase

import com.example.emergencynow.domain.model.entity.Contact
import com.example.emergencynow.domain.repository.ContactRepository
import com.example.emergencynow.domain.usecase.contact.CreateContactUseCase
import com.example.emergencynow.domain.usecase.contact.DeleteContactUseCase
import com.example.emergencynow.domain.usecase.contact.GetContactsUseCase
import com.example.emergencynow.domain.usecase.contact.UpdateContactUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ContactUseCasesTest {

    private lateinit var repository: ContactRepository

    private val fakeContact = Contact(id = "c-1", name = "John", phoneNumber = "+359123", email = null)

    @Before
    fun setUp() {
        repository = mockk()
    }

    // --- GetContactsUseCase ---

    @Test
    fun `GetContacts returns list on success`() = runTest {
        coEvery { repository.getMyContacts() } returns Result.success(listOf(fakeContact))

        val result = GetContactsUseCase(repository)()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun `GetContacts returns empty list when user has no contacts`() = runTest {
        coEvery { repository.getMyContacts() } returns Result.success(emptyList())

        val result = GetContactsUseCase(repository)()

        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `GetContacts returns failure on error`() = runTest {
        coEvery { repository.getMyContacts() } returns Result.failure(Exception("Unauthorized"))

        assertTrue(GetContactsUseCase(repository)().isFailure)
    }

    // --- CreateContactUseCase ---

    @Test
    fun `CreateContact returns created contact on success`() = runTest {
        coEvery { repository.createContact("John", "+359123", null) } returns
            Result.success(fakeContact)

        val result = CreateContactUseCase(repository)("John", "+359123", null)

        assertTrue(result.isSuccess)
        assertEquals("John", result.getOrNull()?.name)
    }

    @Test
    fun `CreateContact passes email when provided`() = runTest {
        val withEmail = fakeContact.copy(email = "john@example.com")
        coEvery { repository.createContact("John", "+359123", "john@example.com") } returns
            Result.success(withEmail)

        val result = CreateContactUseCase(repository)("John", "+359123", "john@example.com")

        assertEquals("john@example.com", result.getOrNull()?.email)
    }

    @Test
    fun `CreateContact returns failure on error`() = runTest {
        coEvery { repository.createContact(any(), any(), any()) } returns
            Result.failure(Exception("Validation error"))

        assertTrue(CreateContactUseCase(repository)("", "", null).isFailure)
    }

    // --- UpdateContactUseCase ---

    @Test
    fun `UpdateContact returns updated contact on success`() = runTest {
        val updated = fakeContact.copy(name = "Jane")
        coEvery { repository.updateContact("c-1", "Jane", "+359123", null) } returns
            Result.success(updated)

        val result = UpdateContactUseCase(repository)("c-1", "Jane", "+359123", null)

        assertTrue(result.isSuccess)
        assertEquals("Jane", result.getOrNull()?.name)
    }

    @Test
    fun `UpdateContact forwards all fields to repository`() = runTest {
        coEvery { repository.updateContact("c-1", "Jane", "+1", "j@j.com") } returns
            Result.success(fakeContact)

        UpdateContactUseCase(repository)("c-1", "Jane", "+1", "j@j.com")

        coVerify(exactly = 1) { repository.updateContact("c-1", "Jane", "+1", "j@j.com") }
    }

    @Test
    fun `UpdateContact returns failure on error`() = runTest {
        coEvery { repository.updateContact(any(), any(), any(), any()) } returns
            Result.failure(Exception("Not found"))

        assertTrue(UpdateContactUseCase(repository)("bad", "n", "p", null).isFailure)
    }

    // --- DeleteContactUseCase ---

    @Test
    fun `DeleteContact returns Unit on success`() = runTest {
        coEvery { repository.deleteContact("c-1") } returns Result.success(Unit)

        val result = DeleteContactUseCase(repository)("c-1")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `DeleteContact returns failure when contact not found`() = runTest {
        coEvery { repository.deleteContact(any()) } returns Result.failure(Exception("Not found"))

        assertTrue(DeleteContactUseCase(repository)("missing").isFailure)
    }

    @Test
    fun `DeleteContact forwards contactId to repository`() = runTest {
        coEvery { repository.deleteContact("c-99") } returns Result.success(Unit)

        DeleteContactUseCase(repository)("c-99")

        coVerify(exactly = 1) { repository.deleteContact("c-99") }
    }
}
