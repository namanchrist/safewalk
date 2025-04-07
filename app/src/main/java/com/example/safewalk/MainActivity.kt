package com.example.safewalk

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.safewalk.service.ShakeDetectionService
import com.example.safewalk.ui.navigation.AppNavigation
import com.example.safewalk.ui.theme.SafewalkTheme
import com.example.safewalk.viewmodel.AuthViewModel
import com.example.safewalk.ui.safety.SafetyViewModel
import com.example.safewalk.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    private lateinit var authViewModel: AuthViewModel
    private lateinit var safetyViewModel: SafetyViewModel
    
    private val requiredPermissions = listOf(
        Manifest.permission.INTERNET,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.SEND_SMS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.POST_NOTIFICATIONS
    )
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            startShakeDetectionService()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize view models
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        safetyViewModel = ViewModelProvider(this, ViewModelFactory(this))[SafetyViewModel::class.java]
        
        // Check and request permissions
        checkAndRequestPermissions()
        
        setContent {
            MainContent()
        }
    }
    
    @Composable
    private fun MainContent() {
        SafewalkTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                AppNavigation()
            }
        }
    }
    
    private fun checkAndRequestPermissions() {
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        
        if (permissionsToRequest.isEmpty()) {
            startShakeDetectionService()
        } else {
            permissionLauncher.launch(permissionsToRequest)
        }
    }
    
    private fun startShakeDetectionService() {
        val serviceIntent = Intent(this, ShakeDetectionService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Note: We intentionally don't stop the shake detection service here
        // because we want it to continue running in the background
    }
}