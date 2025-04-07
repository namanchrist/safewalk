package com.example.safewalk.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.telephony.SmsManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.safewalk.MainActivity
import com.example.safewalk.R
import com.example.safewalk.model.EmergencyContact
import com.example.safewalk.model.LocationData
import java.lang.Exception

class SOSService(private val context: Context) {
    
    private val channelId = "sos_channel"
    private val notificationId = 101
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "SOS Alerts"
            val descriptionText = "Channel for SOS emergency alerts"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun sendSOSAlert(contacts: List<EmergencyContact>, location: LocationData) {
        val message = constructEmergencyMessage(location)
        var successCount = 0
        
        // Send messages to all contacts
        contacts.forEach { contact ->
            try {
                // Try to send WhatsApp message first
                if (sendWhatsApp(contact.phoneNumber, message)) {
                    successCount++
                } else {
                    // Fall back to SMS if WhatsApp fails
                    sendSMS(contact.phoneNumber, message)
                    successCount++
                }
            } catch (e: Exception) {
                // Try SMS if WhatsApp throws an exception
                try {
                    sendSMS(contact.phoneNumber, message)
                    successCount++
                } catch (e: Exception) {
                    // Both methods failed for this contact
                }
            }
        }
        
        // Find a contact to call (priority to contacts marked as guardian)
        val guardianContact = contacts.find { it.isGuardian } ?: contacts.firstOrNull()
        if (guardianContact != null) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                callContact(guardianContact.phoneNumber)
            }
        } else {
            // If no contacts available, call emergency number directly
            callEmergencyNumber()
        }
        
        // Show notification
        showSOSNotification(successCount, contacts.size)
    }
    
    private fun constructEmergencyMessage(location: LocationData): String {
        val googleMapsLink = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
        return "EMERGENCY ALERT! I'm in an emergency situation. Here's my current location: $googleMapsLink"
    }
    
    private fun sendWhatsApp(phoneNumber: String, message: String): Boolean {
        try {
            // Format the phone number
            val formattedNumber = formatPhoneNumber(phoneNumber)
            val numericPhone = formattedNumber.replace(Regex("[^0-9]"), "")
            
            // Create WhatsApp intent
            val intent = Intent(Intent.ACTION_VIEW)
            val url = "https://api.whatsapp.com/send?phone=$numericPhone&text=${Uri.encode(message)}"
            intent.data = Uri.parse(url)
            intent.setPackage("com.whatsapp")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            
            // Check if WhatsApp is installed
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                return true
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }
    
    private fun sendSMS(phoneNumber: String, message: String) {
        try {
            val formattedNumber = formatPhoneNumber(phoneNumber)
            
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.SEND_SMS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val smsManager = context.getSystemService(SmsManager::class.java)
                    smsManager.sendTextMessage(formattedNumber, null, message, null, null)
                } else {
                    @Suppress("DEPRECATION")
                    val smsManager = SmsManager.getDefault()
                    smsManager.sendTextMessage(formattedNumber, null, message, null, null)
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }
    
    private fun callContact(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:${formatPhoneNumber(phoneNumber)}")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            // If calling contact fails, try emergency number
            callEmergencyNumber()
        }
    }
    
    // New function to call Indian police emergency number
    private fun callEmergencyNumber() {
        try {
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:100") // India's police emergency number
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            // Handle exception
        }
    }
    
    private fun formatPhoneNumber(phoneNumber: String): String {
        // Remove any non-numeric characters
        var formattedNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
        
        // Add India country code if needed (assuming India as default)
        if (!formattedNumber.startsWith("+")) {
            if (formattedNumber.startsWith("0")) {
                formattedNumber = "+91" + formattedNumber.substring(1)
            } else if (!formattedNumber.startsWith("91")) {
                formattedNumber = "+91" + formattedNumber
            } else {
                formattedNumber = "+" + formattedNumber
            }
        }
        
        return formattedNumber
    }
    
    private fun showSOSNotification(successCount: Int, totalContacts: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("SOS Alert Sent")
            .setContentText("Alert sent to $successCount out of $totalContacts contacts")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        // Show notification if permission granted
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        }
    }
} 