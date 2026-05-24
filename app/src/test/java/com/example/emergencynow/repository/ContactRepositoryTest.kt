package com.example.emergencynow.repository

import com.example.emergencynow.data.datasource.ContactDataSource
import com.example.emergencynow.data.repository.ContactRepositoryImpl
import com.example.emergencynow.domain.model.response.ContactResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ContactRepositoryTest {

    private lateinit var dataSource: ContactDataSource
    private lateinit var repository: ContactRepositoryImpl

    @Before
    fun setUp() {
        dataSource = mockk()
        repository = ContactRepositoryImpl(dataSource)
    }

    private fun fakeContact(id: String = "c-1") =
        ContactResponse(id = id, name = "Maria Ivanova", phoneNumber = "+359888000111", email = null)

    @Test
    fun `getMyContacts maps list to domain contacts`() = runTest {
        coEvery { dataSource.getMyContacts() } returns listOf(fakeContact("c-1"), fakeContact("c-2"))

        val result = repository.getMyContacts()

        assertTrue(result.isSuccess)
        val contacts = result.getOrNull()!!
        assertEquals(2, contacts.size)
        assertEquals("c-1", contacts[0].id)
        assertEquals("Maria Ivanova", contacts[0].name)
        assertNull(contacts[0].email)
    }

    @Test
    fun `getMyContacts returns failure when data source throws`() = runTest {
        coEvery { dataSource.getMyContacts() } throws Exception("Unauthorized")

        val result = repository.getMyContacts()

        assertTrue(result.isFailure)
    }

    @Test
    fun `createContact maps response to domain Contact`() = runTest {
        coEvery { dataSource.createContact("Ivan", "+359888000222", "ivan@test.com") } returns
            ContactResponse(id = "c-3", name = "Ivan", phoneNumber = "+359888000222", email = "ivan@test.com")

        val result = repository.createContact("Ivan", "+359888000222", "ivan@test.com")

        assertTrue(result.isSuccess)
        val contact = result.getOrNull()!!
        assertEquals("c-3", contact.id)
        assertEquals("ivan@test.com", contact.email)
    }

    @Test
    fun `createContact returns failure when data source throws`() = runTest {
        coEvery { dataSource.createContact(any(), any(), any()) } throws Exception("Duplicate")

        val result = repository.createContact("Ivan", "+359000", null)

        assertTrue(result.isFailure)
    }

    @Test
    fun `updateContact maps response to domain Contact`() = runTest {
        coEvery { dataSource.updateContact("c-1", "Updated Name", "+359999", null) } returns
            ContactResponse(id = "c-1", name = "Updated Name", phoneNumber = "+359999", email = null)

        val result = repository.updateContact("c-1", "Updated Name", "+359999", null)

        assertTrue(result.isSuccess)
        assertEquals("Updated Name", result.getOrNull()!!.name)
    }

    @Test
    fun `deleteContact returns success on completion`() = runTest {
        coEvery { dataSource.deleteContact("c-1") } returns Unit

        val result = repository.deleteContact("c-1")

        assertTrue(result.isSuccess)
        coVerify { dataSource.deleteContact("c-1") }
    }

    @Test
    fun `deleteContact returns failure when data source throws`() = runTest {
        coEvery { dataSource.deleteContact(any()) } throws Exception("Not found")

        val result = repository.deleteContact("c-missing")

        assertTrue(result.isFailure)
    }
}
