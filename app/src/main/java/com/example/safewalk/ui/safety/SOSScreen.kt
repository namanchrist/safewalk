package com.example.safewalk.ui.safety

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.safewalk.model.LocationData
import com.example.safewalk.ui.components.LoadingIndicator
import kotlinx.coroutines.delay

@Composable
fun SOSScreen(
    viewModel: SafetyViewModel = viewModel()
) {
    var showCountdown by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(3) }
    
    val location by viewModel.location.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    LaunchedEffect(showCountdown) {
        if (showCountdown) {
            for (i in 3 downTo 1) {
                countdown = i
                delay(1000)
            }
            viewModel.triggerSosAlert()
            showCountdown = false
            countdown = 3
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            LoadingIndicator()
        } else if (showCountdown) {
            AlertSentContent(countdown)
        } else {
            SOSContent(
                onSOSClick = { showCountdown = true },
                location = location,
                error = error
            )
        }
    }
}

@Composable
private fun SOSContent(
    onSOSClick: () -> Unit,
    location: LocationData?,
    error: String?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        if (location == null) {
            Text(
                text = "Getting your location...",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        Button(
            onClick = onSOSClick,
            modifier = Modifier
                .size(200.dp)
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(
                text = "SOS",
                style = MaterialTheme.typography.headlineLarge
            )
        }
        
        Text(
            text = "Press the button above to send an emergency alert to your contacts",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun AlertSentContent(countdown: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Sending alert in",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = countdown.toString(),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        Text(
            text = "seconds",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
    }
} 