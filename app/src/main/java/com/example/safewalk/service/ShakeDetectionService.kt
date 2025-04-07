package com.example.safewalk.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.example.safewalk.MainActivity
import com.example.safewalk.R
import com.example.safewalk.repository.EmergencyContactRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ShakeDetectionService : Service() {
    
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    
    private lateinit var sensorService: SensorService
    private lateinit var locationService: LocationService
    private lateinit var sosService: SOSService
    private lateinit var emergencyContactRepository: EmergencyContactRepository
    
    private val CHANNEL_ID = "ShakeDetectionChannel"
    private val NOTIFICATION_ID = 1001
    
    companion object {
        val isRunning = MutableLiveData<Boolean>(false)
    }
    
    override fun onCreate() {
        super.onCreate()
        
        sensorService = SensorService(this)
        locationService = LocationService(this)
        sosService = SOSService(this)
        emergencyContactRepository = EmergencyContactRepository()
        
        createNotificationChannel()
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SafeWalk is protecting you")
            .setContentText("Shake detection is active")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(getPendingIntent())
            .build()
        
        startForeground(NOTIFICATION_ID, notification)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning.postValue(true)
        
        startShakeDetection()
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        isRunning.postValue(false)
    }
    
    private fun startShakeDetection() {
        sensorService.getShakeDetection()
            .onEach {
                // When shake is detected, trigger emergency response
                handleShakeDetected()
            }
            .catch { e ->
                // Log error in real app
            }
            .launchIn(serviceScope)
    }
    
    private fun handleShakeDetected() {
        serviceScope.launch {
            try {
                // Get current location
                val locationData = locationService.getCurrentLocation()
                
                // Get emergency contacts
                val contacts = emergencyContactRepository.getAllContacts()
                
                // Send SOS alert
                if (contacts.isNotEmpty()) {
                    sosService.sendSOSAlert(contacts, locationData)
                } else {
                    // If no contacts, still try to call emergency number
                    callEmergencyNumber()
                }
            } catch (e: Exception) {
                // If getting contacts fails, still try to call emergency number directly
                callEmergencyNumber()
            }
        }
    }
    
    private fun callEmergencyNumber() {
        try {
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = android.net.Uri.parse("tel:100") // Indian police emergency number
            callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(callIntent)
        } catch (e: Exception) {
            // Handle exception
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Shake Detection Service"
            val descriptionText = "Detects when you shake your device for emergency"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }
} 