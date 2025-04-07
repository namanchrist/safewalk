package com.example.safewalk.repository

import com.example.safewalk.model.EmergencyContact
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class EmergencyContactRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _contacts = MutableStateFlow<List<EmergencyContact>>(emptyList())
    val contacts: StateFlow<List<EmergencyContact>> = _contacts
    
    // Get the current user ID
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    // Get contacts collection reference for current user
    private fun getContactsCollection() = getCurrentUserId()?.let {
        firestore.collection("users").document(it).collection("contacts")
    }
    
    // Get all contacts for the current user
    suspend fun getAllContacts(): List<EmergencyContact> {
        val contactsCollection = getContactsCollection() ?: return emptyList()
        
        try {
            val snapshot = contactsCollection.get().await()
            val contacts = snapshot.documents.mapNotNull { doc ->
                doc.toObject(EmergencyContact::class.java)
            }
            _contacts.value = contacts
            return contacts
        } catch (e: Exception) {
            throw Exception("Failed to fetch contacts: ${e.message}")
        }
    }
    
    // Add a new contact
    suspend fun addContact(
        name: String,
        phoneNumber: String,
        relationship: String = "",
        isGuardian: Boolean = false
    ): EmergencyContact {
        val contactsCollection = getContactsCollection() ?: throw Exception("User not authenticated")
        
        // Handle guardian logic - only one contact can be guardian
        if (isGuardian) {
            // Remove guardian status from any existing guardians
            val existingContacts = getAllContacts()
            existingContacts.filter { it.isGuardian }.forEach { oldGuardian ->
                contactsCollection.document(oldGuardian.id)
                    .update("isGuardian", false)
                    .await()
            }
        }
        
        // Create new contact
        val newContact = EmergencyContact(
            id = UUID.randomUUID().toString(),
            name = name,
            phoneNumber = phoneNumber,
            relationship = relationship,
            isGuardian = isGuardian
        )
        
        // Add to Firestore
        contactsCollection.document(newContact.id).set(newContact).await()
        
        // Update local state
        val updatedContacts = _contacts.value.toMutableList()
        updatedContacts.add(newContact)
        _contacts.value = updatedContacts
        
        return newContact
    }
    
    // Update existing contact
    suspend fun updateContact(contact: EmergencyContact): EmergencyContact {
        val contactsCollection = getContactsCollection() ?: throw Exception("User not authenticated")
        
        // Handle guardian logic
        if (contact.isGuardian) {
            // Remove guardian status from any existing guardians
            val existingContacts = getAllContacts()
            existingContacts
                .filter { it.isGuardian && it.id != contact.id }
                .forEach { oldGuardian ->
                    contactsCollection.document(oldGuardian.id)
                        .update("isGuardian", false)
                        .await()
                }
        }
        
        // Update in Firestore
        contactsCollection.document(contact.id).set(contact).await()
        
        // Update local state
        val updatedContacts = _contacts.value.toMutableList()
        val index = updatedContacts.indexOfFirst { it.id == contact.id }
        if (index >= 0) {
            updatedContacts[index] = contact
        } else {
            updatedContacts.add(contact)
        }
        _contacts.value = updatedContacts
        
        return contact
    }
    
    // Delete contact
    suspend fun deleteContact(contactId: String) {
        val contactsCollection = getContactsCollection() ?: throw Exception("User not authenticated")
        
        // Delete from Firestore
        contactsCollection.document(contactId).delete().await()
        
        // Update local state
        val updatedContacts = _contacts.value.toMutableList()
        updatedContacts.removeIf { it.id == contactId }
        _contacts.value = updatedContacts
    }
} 