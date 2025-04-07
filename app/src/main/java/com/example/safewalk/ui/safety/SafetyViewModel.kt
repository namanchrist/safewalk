package com.example.safewalk.ui.safety

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safewalk.model.EmergencyContact
import com.example.safewalk.model.LocationData
import com.example.safewalk.model.SOSAlert
import com.example.safewalk.repository.EmergencyContactRepository
import com.example.safewalk.repository.SOSAlertRepository
import com.example.safewalk.service.LocationService
import com.example.safewalk.service.SOSService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

class SafetyViewModel(
    private val context: Context,
    private val locationService: LocationService = LocationService(context),
    private val sosService: SOSService = SOSService(context),
    private val emergencyContactRepository: EmergencyContactRepository = EmergencyContactRepository(),
    private val sosAlertRepository: SOSAlertRepository = SOSAlertRepository()
) : ViewModel() {
    
    private val _location = MutableStateFlow<LocationData?>(null)
    val location: StateFlow<LocationData?> = _location
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    init {
        startLocationTracking()
    }
    
    private fun startLocationTracking() {
        viewModelScope.launch {
            try {
                locationService.getLocationUpdates().collect { location ->
                    _location.value = location
                }
            } catch (e: Exception) {
                _error.value = "Failed to get location: ${e.message}"
            }
        }
    }
    
    fun triggerSosAlert() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentLocation = location.value
                if (currentLocation == null) {
                    _error.value = "Location not available"
                    return@launch
                }
                
                // Get emergency contacts
                val contacts = try {
                    emergencyContactRepository.getAllContacts()
                } catch (e: Exception) {
                    _error.value = "Failed to get contacts: ${e.message}"
                    emptyList()
                }
                
                if (contacts.isEmpty()) {
                    _error.value = "No emergency contacts found"
                    return@launch
                }
                
                // Create SOS alert
                val alert = SOSAlert(
                    id = System.currentTimeMillis().toString(),
                    userId = "current_user", // Replace with actual user ID
                    timestamp = Date(),
                    location = currentLocation,
                    contacts = contacts,
                    status = "SENT"
                )
                
                // Save alert to repository
                sosAlertRepository.createSOSAlert(alert)
                
                // Send SOS alert
                sosService.sendSOSAlert(contacts, currentLocation)
                
            } catch (e: Exception) {
                _error.value = "Failed to send SOS alert: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun shareLocationWithEmergencyContacts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentLocation = location.value
                if (currentLocation == null) {
                    _error.value = "Location not available"
                    return@launch
                }
                
                // Get emergency contacts
                val contacts = try {
                    emergencyContactRepository.getAllContacts()
                } catch (e: Exception) {
                    _error.value = "Failed to get contacts: ${e.message}"
                    emptyList()
                }
                
                if (contacts.isEmpty()) {
                    _error.value = "No emergency contacts found"
                    return@launch
                }
                
                // Create location sharing message
                val message = "I'm sharing my location with you: " +
                        "https://maps.google.com/?q=${currentLocation.latitude},${currentLocation.longitude}"
                
                // Share with each contact
                contacts.forEach { contact ->
                    try {
                        // Try WhatsApp first
                        val whatsappIntent = Intent(Intent.ACTION_VIEW)
                        val whatsappUrl = "https://api.whatsapp.com/send?phone=${contact.phoneNumber}&text=${Uri.encode(message)}"
                        whatsappIntent.data = Uri.parse(whatsappUrl)
                        whatsappIntent.setPackage("com.whatsapp")
                        whatsappIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        
                        if (whatsappIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(whatsappIntent)
                        } else {
                            // Fall back to SMS
                            val smsIntent = Intent(Intent.ACTION_VIEW)
                            smsIntent.data = Uri.parse("smsto:${contact.phoneNumber}")
                            smsIntent.putExtra("sms_body", message)
                            smsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(smsIntent)
                        }
                    } catch (e: Exception) {
                        // Skip this contact if sharing fails
                    }
                }
                
            } catch (e: Exception) {
                _error.value = "Failed to share location: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun openEmergencySettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
    
    fun openLocationSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
} 