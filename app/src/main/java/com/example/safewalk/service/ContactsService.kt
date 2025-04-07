package com.example.safewalk.service

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import com.example.safewalk.model.PhoneContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsService(private val context: Context) {

    // Get device contacts using coroutines (suspend function)
    suspend fun getDeviceContacts(): List<PhoneContact> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<PhoneContact>()
        
        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.HAS_PHONE_NUMBER
        )
        
        context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            null,
            null,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                    val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                    val hasPhoneIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                    
                    if (idIndex < 0 || nameIndex < 0 || hasPhoneIndex < 0) continue
                    
                    val contactId = cursor.getString(idIndex)
                    val name = cursor.getString(nameIndex)
                    val hasPhone = cursor.getInt(hasPhoneIndex) > 0
                    
                    if (hasPhone) {
                        val phoneNumber = getContactPhoneNumber(contactId)
                        if (phoneNumber.isNotEmpty()) {
                            contacts.add(PhoneContact(contactId, name, phoneNumber))
                        }
                    }
                } while (cursor.moveToNext())
            }
        }
        
        return@withContext contacts
    }
    
    private fun getContactPhoneNumber(contactId: String): String {
        var phoneNumber = ""
        
        val phoneCursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId),
            null
        )
        
        if (phoneCursor != null && phoneCursor.moveToFirst()) {
            val numberIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            if (numberIndex >= 0) {
                phoneNumber = phoneCursor.getString(numberIndex)
            }
            phoneCursor.close()
        }
        
        return phoneNumber
    }

    // Get all contacts from the device (non-suspend synchronous version)
    fun getDeviceContactsSync(): List<PhoneContact> {
        val contacts = mutableListOf<PhoneContact>()
        val contentResolver: ContentResolver = context.contentResolver
        
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        
        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            
            while (it.moveToNext()) {
                if (nameIndex >= 0 && phoneIndex >= 0) {
                    val name = it.getString(nameIndex)
                    val phoneNumber = it.getString(phoneIndex)
                    
                    if (name != null && phoneNumber != null) {
                        // Remove duplicates with the same phone number
                        val existingContact = contacts.find { contact -> 
                            normalizePhoneNumber(contact.phoneNumber) == normalizePhoneNumber(phoneNumber) 
                        }
                        
                        if (existingContact == null) {
                            // Generate a unique ID based on the phone number
                            val contactId = phoneNumber.hashCode().toString()
                            contacts.add(PhoneContact(contactId, name, phoneNumber))
                        }
                    }
                }
            }
        }
        
        return contacts
    }
    
    // Normalize phone number for comparison (remove non-digits)
    private fun normalizePhoneNumber(phoneNumber: String): String {
        return phoneNumber.replace(Regex("[^0-9]"), "")
    }
} 