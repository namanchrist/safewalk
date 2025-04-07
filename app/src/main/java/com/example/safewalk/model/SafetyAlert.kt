package com.example.safewalk.model

import com.google.firebase.Timestamp

data class SafetyAlert(
    val id: String = "",
    val userId: String = "",
    val alertType: AlertType = AlertType.SOS,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Timestamp = Timestamp.now(),
    val message: String = "",
    val resolved: Boolean = false
)

enum class AlertType {
    SOS,
    POLICE,
    MEDICAL,
    LOCATION_SHARE
} 