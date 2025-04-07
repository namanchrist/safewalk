package com.example.safewalk.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safewalk.model.EmergencyContact
import com.example.safewalk.repository.ContactsRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ContactsViewModel : ViewModel() {
    private val repository = ContactsRepository()
    
    private val _contactsState = MutableLiveData<ContactsState>(ContactsState.Idle)
    val contactsState: LiveData<ContactsState> = _contactsState
    
    private val _contacts = MutableLiveData<List<EmergencyContact>>(emptyList())
    val contacts: LiveData<List<EmergencyContact>> = _contacts
    
    fun loadUserContacts() {
        _contactsState.value = ContactsState.Loading
        viewModelScope.launch {
            repository.getUserContacts().collectLatest { contactsList ->
                _contacts.value = contactsList
                _contactsState.value = ContactsState.Success
            }
        }
    }
    
    fun addContact(name: String, phoneNumber: String, relationship: String) {
        viewModelScope.launch {
            _contactsState.value = ContactsState.Loading
            val contact = EmergencyContact(
                id = "",  // ID will be set by the repository
                name = name,
                phoneNumber = phoneNumber,
                relationship = relationship
            )
            
            val result = repository.addContact(contact)
            result.onSuccess {
                loadUserContacts()
            }.onFailure { error ->
                _contactsState.value = ContactsState.Error(error.message ?: "Failed to add contact")
            }
        }
    }
    
    fun updateContact(contact: EmergencyContact) {
        viewModelScope.launch {
            _contactsState.value = ContactsState.Loading
            val result = repository.updateContact(contact)
            result.onSuccess {
                loadUserContacts()
            }.onFailure { error ->
                _contactsState.value = ContactsState.Error(error.message ?: "Failed to update contact")
            }
        }
    }
    
    fun deleteContact(contactId: String) {
        viewModelScope.launch {
            _contactsState.value = ContactsState.Loading
            val result = repository.deleteContact(contactId)
            result.onSuccess {
                loadUserContacts()
            }.onFailure { error ->
                _contactsState.value = ContactsState.Error(error.message ?: "Failed to delete contact")
            }
        }
    }
}

sealed class ContactsState {
    object Idle : ContactsState()
    object Loading : ContactsState()
    object Success : ContactsState()
    data class Error(val message: String) : ContactsState()
} 