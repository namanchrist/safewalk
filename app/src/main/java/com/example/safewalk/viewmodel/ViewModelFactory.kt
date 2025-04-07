package com.example.safewalk.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.safewalk.repository.EmergencyContactRepository
import com.example.safewalk.repository.SOSAlertRepository
import com.example.safewalk.service.ContactsService
import com.example.safewalk.service.LocationService
import com.example.safewalk.service.SOSService
import com.example.safewalk.ui.safety.SafetyViewModel

class ViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SafetyViewModel::class.java) -> {
                val locationService = LocationService(context)
                val sosService = SOSService(context)
                val emergencyContactRepository = EmergencyContactRepository()
                val sosAlertRepository = SOSAlertRepository()
                
                SafetyViewModel(
                    context,
                    locationService,
                    sosService,
                    emergencyContactRepository,
                    sosAlertRepository
                ) as T
            }
            modelClass.isAssignableFrom(EmergencyContactViewModel::class.java) -> {
                EmergencyContactViewModel(context) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
} 