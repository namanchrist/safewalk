package com.example.safewalk.repository

import com.example.safewalk.model.LocationType
import com.example.safewalk.model.SafeLocation
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class LocationRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val locationsCollection = firestore.collection("safe_locations")
    
    fun getNearbyLocations(
        latitude: Double,
        longitude: Double,
        locationType: LocationType? = null
    ): Flow<List<SafeLocation>> = flow {
        try {
            // Try to get data from Firestore first
            val query = if (locationType != null) {
                locationsCollection.whereEqualTo("locationType", locationType.name)
            } else {
                locationsCollection
            }
            
            val snapshot = query.get().await()
            val firestoreLocations = snapshot.toObjects(SafeLocation::class.java)
            
            // If we have no data in Firestore, use mock data
            val locations = if (firestoreLocations.isEmpty()) {
                getMockLocations(latitude, longitude, locationType)
            } else {
                firestoreLocations
            }
            
            // Sort locations by distance
            val sortedLocations = locations.sortedBy { location ->
                calculateDistance(
                    latitude, longitude,
                    location.latitude, location.longitude
                )
            }
            
            emit(sortedLocations)
        } catch (e: Exception) {
            // Use mock data if there's an error accessing Firestore
            val mockLocations = getMockLocations(latitude, longitude, locationType)
            emit(mockLocations)
        }
    }
    
    // Mock data generator for locations around the given coordinates
    private fun getMockLocations(
        centerLat: Double,
        centerLng: Double,
        locationType: LocationType?
    ): List<SafeLocation> {
        val allMockLocations = mutableListOf<SafeLocation>()
        
        // Police stations in Bangalore
        val policeStations = listOf(
            SafeLocation(
                id = "police1",
                name = "Hulimavu Police Station",
                latitude = 12.8879,
                longitude = 77.6174,
                locationType = LocationType.POLICE,
                address = "Bannerghatta Road, Hulimavu, Bangalore",
                phoneNumber = "080-2278-3050"
            ),
            SafeLocation(
                id = "police2",
                name = "Mico Layout Police Station",
                latitude = 12.9001,
                longitude = 77.6033,
                locationType = LocationType.POLICE,
                address = "BTM Layout, Bangalore",
                phoneNumber = "080-2299-4000"
            ),
            SafeLocation(
                id = "police3",
                name = "Bannerghatta Police Station",
                latitude = 12.8653,
                longitude = 77.5951,
                locationType = LocationType.POLICE,
                address = "Bannerghatta Road, Bangalore",
                phoneNumber = "080-2210-0100"
            ),
            SafeLocation(
                id = "police4",
                name = "Electronic City Police Station",
                latitude = 12.8455,
                longitude = 77.6610,
                locationType = LocationType.POLICE,
                address = "Electronic City, Bangalore",
                phoneNumber = "080-2852-2222"
            ),
            SafeLocation(
                id = "police5",
                name = "Koramangala Police Station",
                latitude = 12.9338,
                longitude = 77.6224,
                locationType = LocationType.POLICE,
                address = "Koramangala, Bangalore",
                phoneNumber = "080-2554-0100"
            ),
            SafeLocation(
                id = "police6",
                name = "Madiwala Police Station",
                latitude = 12.9201,
                longitude = 77.6179,
                locationType = LocationType.POLICE,
                address = "Madiwala, Bangalore",
                phoneNumber = "080-2553-4100"
            ),
            SafeLocation(
                id = "police7",
                name = "Christ University Campus Security",
                latitude = 12.9684,
                longitude = 77.5982,
                locationType = LocationType.POLICE,
                address = "Christ University, Bangalore",
                phoneNumber = "080-4012-9100"
            )
        )
        
        // Hospitals in Bangalore
        val hospitals = listOf(
            SafeLocation(
                id = "hospital1",
                name = "Apollo Hospital",
                latitude = 12.9154,
                longitude = 77.5996,
                locationType = LocationType.HOSPITAL,
                address = "Bannerghatta Road, Bangalore",
                phoneNumber = "080-4612-4444"
            ),
            SafeLocation(
                id = "hospital2",
                name = "Fortis Hospital",
                latitude = 12.8919,
                longitude = 77.5958,
                locationType = LocationType.HOSPITAL,
                address = "Bannerghatta Road, Bangalore",
                phoneNumber = "080-6621-4444"
            ),
            SafeLocation(
                id = "hospital3",
                name = "Jayadeva Institute of Cardiology",
                latitude = 12.9133,
                longitude = 77.5996,
                locationType = LocationType.HOSPITAL,
                address = "Bannerghatta Road, Bangalore",
                phoneNumber = "080-2653-5400"
            ),
            SafeLocation(
                id = "hospital4",
                name = "Nimhans Hospital",
                latitude = 12.9437,
                longitude = 77.5947,
                locationType = LocationType.HOSPITAL,
                address = "Hosur Road, Bangalore",
                phoneNumber = "080-2699-5000"
            ),
            SafeLocation(
                id = "hospital5",
                name = "St. John's Medical College Hospital",
                latitude = 12.9294,
                longitude = 77.6247,
                locationType = LocationType.HOSPITAL,
                address = "Koramangala, Bangalore",
                phoneNumber = "080-2206-5000"
            ),
            SafeLocation(
                id = "hospital6",
                name = "Columbia Asia Hospital",
                latitude = 12.8762,
                longitude = 77.6033,
                locationType = LocationType.HOSPITAL,
                address = "Bommanahalli, Bangalore",
                phoneNumber = "080-7177-2727"
            ),
            SafeLocation(
                id = "hospital7",
                name = "Christ University Medical Center",
                latitude = 12.9717,
                longitude = 77.5969,
                locationType = LocationType.HOSPITAL,
                address = "Christ University, Bangalore",
                phoneNumber = "080-4012-9200"
            ),
            SafeLocation(
                id = "hospital8",
                name = "Sagar Hospital",
                latitude = 12.9073,
                longitude = 77.5965,
                locationType = LocationType.HOSPITAL,
                address = "Bannerghatta Road, Bangalore",
                phoneNumber = "080-4363-5000"
            )
        )
        
        // Bus stations in Bangalore
        val busStations = listOf(
            SafeLocation(
                id = "bus1",
                name = "Kempegowda Bus Station (Majestic)",
                latitude = 12.9778,
                longitude = 77.5726,
                locationType = LocationType.BUS_STATION,
                address = "Majestic, Bangalore",
                phoneNumber = "080-2295-8222"
            ),
            SafeLocation(
                id = "bus2",
                name = "Banashankari Bus Station",
                latitude = 12.9248,
                longitude = 77.5476,
                locationType = LocationType.BUS_STATION,
                address = "Banashankari, Bangalore",
                phoneNumber = "080-2677-7666"
            ),
            SafeLocation(
                id = "bus3",
                name = "Shantinagar Bus Station",
                latitude = 12.9572,
                longitude = 77.5952,
                locationType = LocationType.BUS_STATION,
                address = "Shantinagar, Bangalore",
                phoneNumber = "080-2222-5555"
            ),
            SafeLocation(
                id = "bus4",
                name = "Silk Board Bus Stop",
                latitude = 12.9179,
                longitude = 77.6229,
                locationType = LocationType.BUS_STATION,
                address = "BTM Layout, Bangalore",
                phoneNumber = "080-2222-5555"
            ),
            SafeLocation(
                id = "bus5",
                name = "Christ University Bus Stop",
                latitude = 12.9710,
                longitude = 77.5939,
                locationType = LocationType.BUS_STATION,
                address = "Dairy Circle, Bangalore",
                phoneNumber = "080-4012-9000"
            )
        )
        
        allMockLocations.addAll(policeStations)
        allMockLocations.addAll(hospitals)
        allMockLocations.addAll(busStations)
        
        // Filter by location type if specified
        return if (locationType != null) {
            allMockLocations.filter { it.locationType == locationType }
        } else {
            allMockLocations
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
    
    suspend fun addSafeLocation(location: SafeLocation): Result<SafeLocation> {
        return try {
            val documentRef = locationsCollection.document()
            val newLocation = location.copy(id = documentRef.id)
            documentRef.set(newLocation).await()
            Result.success(newLocation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 