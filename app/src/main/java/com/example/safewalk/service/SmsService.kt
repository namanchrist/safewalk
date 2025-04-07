package com.example.safewalk.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.widget.Toast
import com.example.safewalk.model.EmergencyContact
import com.example.safewalk.model.LocationData
import com.example.safewalk.model.User

class SmsService(private val context: Context) {
    private val smsManager: SmsManager = context.getSystemService(SmsManager::class.java)
    
    fun sendSosMessage(user: User, contacts: List<EmergencyContact>, location: LocationData) {
        val message = constructEmergencyMessage(user.name, location)
        
        contacts.forEach { contact ->
            try {
                sendWhatsAppMessage(contact.phoneNumber, message)
                
                sendSMS(contact.phoneNumber, message)
            } catch (e: Exception) {
                try {
                    sendSMS(contact.phoneNumber, message)
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Failed to send message to ${contact.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        
        if (contacts.isEmpty()) {
            callEmergencyNumber()
        }
        
        Toast.makeText(
            context,
            "SOS alert sent to ${contacts.size} contacts",
            Toast.LENGTH_SHORT
        ).show()
    }
    
    fun sendLocationUpdate(user: User, contact: EmergencyContact, location: LocationData) {
        val message = constructLocationUpdateMessage(user.name, location)
        
        try {
            sendWhatsAppMessage(contact.phoneNumber, message)
            
            sendSMS(contact.phoneNumber, message)
            
            Toast.makeText(
                context,
                "Location shared with ${contact.name}",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Failed to share location with ${contact.name}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun constructEmergencyMessage(userName: String, location: LocationData): String {
        val googleMapsLink = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
        return "EMERGENCY ALERT! I'm in an emergency situation. Here's my current location: $googleMapsLink"
    }
    
    private fun constructLocationUpdateMessage(userName: String, location: LocationData): String {
        val googleMapsLink = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
        return "Hi, I'm sharing my current location with you: $googleMapsLink"
    }
    
    private fun sendSMS(phoneNumber: String, message: String) {
        try {
            val formattedPhoneNumber = formatPhoneNumber(phoneNumber)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                smsManager.sendTextMessage(formattedPhoneNumber, null, message, null, null)
            } else {
                @Suppress("DEPRECATION")
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(formattedPhoneNumber, null, message, null, null)
            }
        } catch (e: Exception) {
            throw e
        }
    }
    
    private fun sendWhatsAppMessage(phoneNumber: String, message: String) {
        try {
            val formattedPhoneNumber = formatPhoneNumber(phoneNumber)
            
            val numericPhone = formattedPhoneNumber.replace(Regex("[^0-9]"), "")
            
            val intent = Intent(Intent.ACTION_VIEW)
            val url = "https://api.whatsapp.com/send?phone=$numericPhone&text=${Uri.encode(message)}"
            intent.data = Uri.parse(url)
            intent.setPackage("com.whatsapp")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                throw Exception("WhatsApp not installed")
            }
        } catch (e: Exception) {
            throw e
        }
    }
    
    private fun formatPhoneNumber(phoneNumber: String): String {
        var formattedNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
        
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
    
    private fun callEmergencyNumber() {
        try {
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:100")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Failed to call emergency services",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
} 