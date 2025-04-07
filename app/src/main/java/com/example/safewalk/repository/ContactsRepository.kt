package com.example.safewalk.repository

import com.example.safewalk.model.EmergencyContact
import com.example.safewalk.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

class ContactsRepository {
    private val contacts = mutableListOf<EmergencyContact>()
    
    init {
        // Add some sample contacts for testing
        contacts.add(
            EmergencyContact(
                id = "1",
                name = "Emergency Contact 1",
                phoneNumber = "+919876543210",
                relationship = "Family",
                isGuardian = true
            )
        )
        contacts.add(
            EmergencyContact(
                id = "2",
                name = "Emergency Contact 2",
                phoneNumber = "+919876543211",
                relationship = "Friend"
            )
        )
    }
    
    fun addContact(contact: EmergencyContact): Result<EmergencyContact> {
        return try {
            val newId = UUID.randomUUID().toString()
            val newContact = contact.copy(id = newId)
            contacts.add(newContact)
            Result.success(newContact)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun updateContact(contact: EmergencyContact): Result<EmergencyContact> {
        return try {
            val index = contacts.indexOfFirst { it.id == contact.id }
            if (index != -1) {
                contacts[index] = contact
                Result.success(contact)
            } else {
                Result.failure(Exception("Contact not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun deleteContact(contactId: String): Result<Unit> {
        return try {
            val removed = contacts.removeIf { it.id == contactId }
            if (removed) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Contact not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getUserContacts(): Flow<List<EmergencyContact>> = flow {
        emit(contacts)
    }
} 