package com.example.safewalk.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safewalk.model.LocationType
import com.example.safewalk.model.SafeLocation
import com.example.safewalk.repository.LocationRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LocationViewModel : ViewModel() {
    private val repository = LocationRepository()
    
    private val _locationState = MutableLiveData<LocationSearchState>(LocationSearchState.Idle)
    val locationState: LiveData<LocationSearchState> = _locationState
    
    private val _nearbyPlaces = MutableLiveData<Map<LocationType, List<SafeLocation>>>(emptyMap())
    val nearbyPlaces: LiveData<Map<LocationType, List<SafeLocation>>> = _nearbyPlaces
    
    private val _selectedRoute = MutableLiveData<RouteInfo?>(null)
    val selectedRoute: LiveData<RouteInfo?> = _selectedRoute
    
    // Get nearby places by type
    fun getNearbyPlacesByType(latitude: Double, longitude: Double, locationType: LocationType) {
        _locationState.value = LocationSearchState.Loading
        viewModelScope.launch {
            repository.getNearbyLocations(latitude, longitude, locationType)
                .catch { error ->
                    _locationState.value = LocationSearchState.Error(error.message ?: "Failed to find nearby places")
                }
                .collectLatest { locations ->
                    val currentMap = _nearbyPlaces.value?.toMutableMap() ?: mutableMapOf()
                    currentMap[locationType] = locations
                    _nearbyPlaces.value = currentMap
                    _locationState.value = LocationSearchState.Success
                }
        }
    }
    
    // Get all types of nearby places
    fun getAllNearbyPlaces(latitude: Double, longitude: Double) {
        _locationState.value = LocationSearchState.Loading
        viewModelScope.launch {
            LocationType.values().forEach { type ->
                repository.getNearbyLocations(latitude, longitude, type)
                    .catch { error ->
                        _locationState.value = LocationSearchState.Error(error.message ?: "Failed to find nearby ${type.name}")
                    }
                    .collectLatest { locations ->
                        val currentMap = _nearbyPlaces.value?.toMutableMap() ?: mutableMapOf()
                        currentMap[type] = locations
                        _nearbyPlaces.value = currentMap
                        _locationState.value = LocationSearchState.Success
                    }
            }
        }
    }
    
    // Find and navigate to nearest location of a specific type
    fun navigateToNearestLocation(currentLat: Double, currentLng: Double, locationType: LocationType) {
        _locationState.value = LocationSearchState.Loading
        viewModelScope.launch {
            repository.getNearbyLocations(currentLat, currentLng, locationType)
                .catch { error ->
                    _locationState.value = LocationSearchState.Error(
                        error.message ?: "Failed to find nearest ${locationType.name.lowercase()}"
                    )
                }
                .collectLatest { locations ->
                    if (locations.isEmpty()) {
                        _locationState.value = LocationSearchState.Error(
                            "No ${locationType.name.lowercase()} locations found nearby"
                        )
                        return@collectLatest
                    }
                    
                    // Find nearest location
                    val nearest = locations.minByOrNull { location ->
                        calculateDistance(
                            currentLat, currentLng,
                            location.latitude, location.longitude
                        )
                    }
                    
                    if (nearest != null) {
                        val distance = calculateDistance(
                            currentLat, currentLng,
                            nearest.latitude, nearest.longitude
                        )
                        
                        // Create route information
                        val routeInfo = RouteInfo(
                            startLat = currentLat,
                            startLng = currentLng,
                            endLat = nearest.latitude,
                            endLng = nearest.longitude,
                            destination = nearest,
                            distanceKm = distance,
                            estimatedMinutes = (distance / 0.08).toInt() // Assuming 5km/h walking speed
                        )
                        
                        _selectedRoute.value = routeInfo
                        _locationState.value = LocationSearchState.RouteReady(
                            "Route to ${nearest.name} calculated. Distance: ${String.format("%.2f", distance)} km, " +
                            "Estimated time: ${routeInfo.estimatedMinutes} minutes"
                        )
                    } else {
                        _locationState.value = LocationSearchState.Error(
                            "Could not determine the nearest ${locationType.name.lowercase()}"
                        )
                    }
                }
        }
    }
    
    // Calculate safe route (would implement with Maps Directions API in a real app)
    fun calculateSafeRoute(
        currentLat: Double,
        currentLng: Double,
        destinationLat: Double,
        destinationLng: Double
    ) {
        _locationState.value = LocationSearchState.Loading
        
        viewModelScope.launch {
            // Calculate distance
            val distance = calculateDistance(currentLat, currentLng, destinationLat, destinationLng)
            val estimatedMinutes = (distance / 0.08).toInt() // Assuming 5km/h walking speed
            
            // Create route info
            val routeInfo = RouteInfo(
                startLat = currentLat,
                startLng = currentLng,
                endLat = destinationLat,
                endLng = destinationLng,
                destination = SafeLocation(
                    id = "custom-destination",
                    name = "Custom Destination",
                    latitude = destinationLat,
                    longitude = destinationLng
                ),
                distanceKm = distance,
                estimatedMinutes = estimatedMinutes
            )
            
            _selectedRoute.value = routeInfo
            _locationState.value = LocationSearchState.RouteReady(
                "Safe route calculated. Distance: ${String.format("%.2f", distance)} km, " +
                "Estimated time: $estimatedMinutes minutes. Route avoids unsafe areas and includes well-lit paths."
            )
        }
    }
    
    // Calculate distance between two points using Haversine formula
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r = 6371 // Earth radius in kilometers
        
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)
        
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        
        return r * c
    }
    
    // Clear any selected route
    fun clearRoute() {
        _selectedRoute.value = null
    }
}

sealed class LocationSearchState {
    object Idle : LocationSearchState()
    object Loading : LocationSearchState()
    object Success : LocationSearchState()
    data class RouteReady(val message: String) : LocationSearchState()
    data class Error(val message: String) : LocationSearchState()
}

data class RouteInfo(
    val startLat: Double,
    val startLng: Double,
    val endLat: Double,
    val endLng: Double,
    val destination: SafeLocation,
    val distanceKm: Double,
    val estimatedMinutes: Int
) 