package com.example.safewalk.model

/**
 * Data class representing location information
 */
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val address: String = ""
) {
    /**
     * Returns a formatted string with latitude and longitude
     */
    fun getCoordinatesString(): String {
        return "Lat: $latitude, Long: $longitude"
    }
    
    /**
     * Returns a Maps URL that can be used to open this location in a map app
     */
    fun getMapsUrl(): String {
        return "https://maps.google.com/?q=$latitude,$longitude"
    }
    
    /**
     * Returns a string that can be used in emergency messages
     */
    fun getEmergencyLocationText(): String {
        return if (address.isNotEmpty()) {
            "I'm at $address (Coordinates: $latitude, $longitude). View on map: ${getMapsUrl()}"
        } else {
            "I'm at location: $latitude, $longitude. View on map: ${getMapsUrl()}"
        }
    }
} 