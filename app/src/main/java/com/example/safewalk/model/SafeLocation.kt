package com.example.safewalk.model

data class SafeLocation(
    val id: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val locationType: LocationType = LocationType.POLICE,
    val address: String = "",
    val phoneNumber: String = ""
)

enum class LocationType {
    POLICE,
    HOSPITAL,
    BUS_STATION,
    SAFE_ZONE
} 