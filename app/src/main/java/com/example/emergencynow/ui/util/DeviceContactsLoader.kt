package com.example.emergencynow.ui.util

import android.content.Context
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DeviceContact(
    val id: String,
    val displayName: String,
    val phoneNumber: String,
)

object DeviceContactsLoader {

    suspend fun load(context: Context): List<DeviceContact> = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
        )
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC",
        ) ?: return@withContext emptyList()

        val seen = HashSet<String>()
        val results = ArrayList<DeviceContact>()
        cursor.use {
            val idIndex = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val id = it.getString(idIndex) ?: continue
                val name = it.getString(nameIndex) ?: continue
                val rawNumber = it.getString(numberIndex) ?: continue
                val normalized = rawNumber.filter { ch -> ch.isDigit() || ch == '+' }
                if (normalized.isEmpty()) continue
                val key = "$id|$normalized"
                if (!seen.add(key)) continue
                results += DeviceContact(id = id, displayName = name, phoneNumber = rawNumber)
            }
        }
        results
    }
}
