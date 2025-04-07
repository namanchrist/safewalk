package com.example.safewalk.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.safewalk.model.EmergencyContact
import com.example.safewalk.model.LocationData
import com.example.safewalk.service.LocationService
import com.example.safewalk.service.SOSService
import com.example.safewalk.viewmodel.EmergencyContactViewModel
import com.example.safewalk.viewmodel.ViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SOSScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val locationService = remember { LocationService(context) }
    val sosService = remember { SOSService(context) }
    
    val viewModel: EmergencyContactViewModel = viewModel(
        factory = ViewModelFactory(context)
    )
    
    val contacts = viewModel.contacts.collectAsStateWithLifecycle(initialValue = emptyList()).value
    val coroutineScope = rememberCoroutineScope()
    
    var location by remember { mutableStateOf<LocationData?>(null) }
    var isLocationLoading by remember { mutableStateOf(false) }
    var alertSent by remember { mutableStateOf(false) }
    var timeRemaining by remember { mutableStateOf(5) }
    
    LaunchedEffect(Unit) {
        viewModel.loadEmergencyContacts()
    }
    
    // Countdown effect
    LaunchedEffect(alertSent) {
        if (alertSent) {
            while (timeRemaining > 0) {
                delay(1000)
                timeRemaining--
            }
            // Navigate back after countdown
            onNavigateBack()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, "Back")
            }
            
            Text(
                text = "Emergency SOS",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            
            // Empty spacer for alignment
            Spacer(Modifier.size(48.dp))
        }
        
        if (alertSent) {
            AlertSentContent(timeRemaining)
        } else {
            SOSContent(
                isLocationLoading = isLocationLoading,
                onSendSOS = {
                    coroutineScope.launch {
                        isLocationLoading = true
                        try {
                            location = locationService.getCurrentLocation()
                            if (location != null) {
                                sosService.sendSOSAlert(contacts, location!!)
                                alertSent = true
                            } else {
                                Toast.makeText(
                                    context,
                                    "Could not get location. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Error: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        } finally {
                            isLocationLoading = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun SOSContent(
    isLocationLoading: Boolean,
    onSendSOS: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Tap the SOS button to send alert with your location to all your emergency contacts",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        if (isLocationLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(120.dp),
                strokeWidth = 8.dp
            )
        } else {
            Button(
                onClick = onSendSOS,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                Text(
                    text = "SOS",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "This will send an emergency SMS alert with your current location to all your emergency contacts.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AlertSentContent(timeRemaining: Int) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .background(Color.Green.copy(alpha = 0.2f), CircleShape)
                .padding(16.dp),
            tint = Color.Green
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Alert Sent Successfully!",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Your emergency contacts have been notified with your location",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Returning to home screen in $timeRemaining seconds...",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
} 