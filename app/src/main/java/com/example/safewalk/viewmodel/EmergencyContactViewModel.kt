package com.example.safewalk.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safewalk.model.EmergencyContact
import com.example.safewalk.repository.EmergencyContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EmergencyContactViewModel(
    private val context: Context
) : ViewModel() {
    private val repository = EmergencyContactRepository()
    
    private val _contacts = MutableStateFlow<List<EmergencyContact>>(emptyList())
    val contacts: StateFlow<List<EmergencyContact>> = _contacts
    
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    fun loadEmergencyContacts() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            try {
                val contacts = repository.getAllContacts()
                _contacts.value = contacts
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load contacts"
            } finally {
                _loading.value = false
            }
        }
    }
    
    fun addContact(name: String, phoneNumber: String, relationship: String = "", isGuardian: Boolean = false) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            try {
                repository.addContact(
                    name = name,
                    phoneNumber = phoneNumber,
                    relationship = relationship,
                    isGuardian = isGuardian
                )
                
                // Reload contacts after adding
                loadEmergencyContacts()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to add contact"
            } finally {
                _loading.value = false
            }
        }
    }
    
    fun updateContact(contact: EmergencyContact) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            try {
                repository.updateContact(contact)
                
                // Reload contacts after updating
                loadEmergencyContacts()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update contact"
            } finally {
                _loading.value = false
            }
        }
    }
    
    fun deleteContact(contactId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            try {
                repository.deleteContact(contactId)
                
                // Reload contacts after deleting
                loadEmergencyContacts()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete contact"
            } finally {
                _loading.value = false
            }
        }
    }
} 