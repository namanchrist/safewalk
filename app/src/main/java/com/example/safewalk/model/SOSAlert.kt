package com.example.safewalk.model

import java.util.Date

data class SOSAlert(
    val id: String = "",
    val userId: String = "",
    val timestamp: Date = Date(),
    val location: LocationData = LocationData(0.0, 0.0),
    val contacts: List<EmergencyContact> = emptyList(),
    val status: String = "PENDING" // PENDING, SENT, RECEIVED, RESPONDED
) {
    // No-args constructor for Firebase
    constructor() : this(
        id = "",
        userId = "",
        timestamp = Date(),
        location = LocationData(0.0, 0.0),
        contacts = emptyList(),
        status = "PENDING"
    )
} 