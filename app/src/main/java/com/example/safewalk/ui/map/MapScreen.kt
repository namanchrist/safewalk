package com.example.safewalk.ui.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.safewalk.service.LocationService
import com.example.safewalk.ui.theme.SafePink
import com.example.safewalk.ui.theme.SafePinkLight
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import kotlin.math.*
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.TravelMode
import com.google.maps.model.DirectionsResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val locationService = remember { LocationService(context) }
    val scope = rememberCoroutineScope()
    
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var routeInfo by remember { mutableStateOf<RouteInfo?>(null) }
    
    // State for places of interest
    var showPoliceStations by remember { mutableStateOf(false) }
    var showHospitals by remember { mutableStateOf(false) }
    var showBusStations by remember { mutableStateOf(false) }
    var showPharmacies by remember { mutableStateOf(false) }
    var showShelters by remember { mutableStateOf(false) }
    
    // State for route display
    var selectedPlace by remember { mutableStateOf<PlaceOfInterest?>(null) }
    var showRouteDialog by remember { mutableStateOf(false) }
    var showingRoute by remember { mutableStateOf(false) }
    
    // Initialize GeoApiContext for Directions API
    val geoApiContext = remember {
        GeoApiContext.Builder()
            .apiKey("YOUR_API_KEY") // Replace with your actual API key
            .build()
    }
    
    // Function to fetch route using Directions API
    suspend fun fetchRoute(origin: LatLng, destination: LatLng): RouteInfo? {
        return withContext(Dispatchers.IO) {
            try {
                val result = DirectionsApi.newRequest(geoApiContext)
                    .mode(TravelMode.WALKING)
                    .origin(com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                    .destination(com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                    .await()
                
                if (result.routes.isNotEmpty() && result.routes[0].legs.isNotEmpty()) {
                    val route = result.routes[0].legs[0]
                    val points = result.routes[0].overviewPolyline.decodePath()
                    
                    RouteInfo(
                        points = points.map { LatLng(it.lat, it.lng) },
                        distance = route.distance.humanReadable,
                        duration = route.duration.humanReadable
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    // Simulated places data (in a real app, these would come from Places API)
    // Real locations for police stations
    val nearbyPoliceStations = remember(currentLocation) {
        if (currentLocation != null) {
            listOf(
                PlaceOfInterest(
                    name = "Bangalore City Police Station", 
                    location = LatLng(12.9767, 77.5713),
                    type = PlaceType.POLICE,
                    distance = calculateDistance(currentLocation!!, LatLng(12.9767, 77.5713))
                ),
                PlaceOfInterest(
                    name = "Cubbon Park Police Station", 
                    location = LatLng(12.9778, 77.5951),
                    type = PlaceType.POLICE,
                    distance = calculateDistance(currentLocation!!, LatLng(12.9778, 77.5951))
                ),
                PlaceOfInterest(
                    name = "Indiranagar Police Station", 
                    location = LatLng(12.9719, 77.6412),
                    type = PlaceType.POLICE,
                    distance = calculateDistance(currentLocation!!, LatLng(12.9719, 77.6412))
                )
            )
        } else {
            emptyList()
        }
    }
    
    // Real locations for hospitals
    val nearbyHospitals = remember(currentLocation) {
        if (currentLocation != null) {
            listOf(
                PlaceOfInterest(
                    name = "Manipal Hospital", 
                    location = LatLng(12.9582, 77.6488),
                    type = PlaceType.HOSPITAL,
                    distance = calculateDistance(currentLocation!!, LatLng(12.9582, 77.6488))
                ),
                PlaceOfInterest(
                    name = "Apollo Hospital", 
                    location = LatLng(12.9489, 77.6302),
                    type = PlaceType.HOSPITAL,
                    distance = calculateDistance(currentLocation!!, LatLng(12.9489, 77.6302))
                ),
                PlaceOfInterest(
                    name = "Fortis Hospital", 
                    location = LatLng(12.9112, 77.6258),
                    type = PlaceType.HOSPITAL,
                    distance = calculateDistance(currentLocation!!, LatLng(12.9112, 77.6258))
                )
            )
        } else {
            emptyList()
        }
    }
    
    // Real locations for bus stations
    val nearbyBusStations = remember(currentLocation) {
        if (currentLocation != null) {
            listOf(
                PlaceOfInterest(
                    name = "Majestic Bus Terminal", 
                    location = LatLng(12.9774, 77.5724),
                    type = PlaceType.BUS,
                    distance = calculateDistance(currentLocation!!, LatLng(12.9774, 77.5724))
                ),
                PlaceOfInterest(
                    name = "Shivajinagar Bus Station", 
                    location = LatLng(12.9854, 77.6036),
                    type = PlaceType.BUS,
                    distance = calculateDistance(currentLocation!!, LatLng(12.9854, 77.6036))
                ),
                PlaceOfInterest(
                    name = "Kempegowda Bus Station", 
                    location = LatLng(12.9784, 77.5697),
                    type = PlaceType.BUS,
                    distance = calculateDistance(currentLocation!!, LatLng(12.9784, 77.5697))
                )
            )
        } else {
            emptyList()
        }
    }
    
    // Simulated data for pharmacies
    val nearbyPharmacies = remember(currentLocation) {
        if (currentLocation != null) {
            listOf(
                PlaceOfInterest(
                    name = "Apollo Pharmacy", 
                    location = LatLng(12.9500, 77.6400),
                    type = PlaceType.PHARMACY,
                    distance = calculateDistance(currentLocation!!, LatLng(12.9500, 77.6400))
                ),
                PlaceOfInterest(
                    name = "MedPlus Pharmacy", 
                    location = LatLng(12.9650, 77.5890),
                    type = PlaceType.PHARMACY,
                    distance = calculateDistance(currentLocation!!, LatLng(12.9650, 77.5890))
                ),
                PlaceOfInterest(
                    name = "Wellness Forever", 
                    location = LatLng(12.9820, 77.6150),
                    type = PlaceType.PHARMACY,
                    distance = calculateDistance(currentLocation!!, LatLng(12.9820, 77.6150))
                )
            )
        } else {
            emptyList()
        }
    }
    
    // Simulated data for shelters
    val nearbyShelters = remember(currentLocation) {
        if (currentLocation != null) {
            listOf(
                PlaceOfInterest(
                    name = "Bangalore Urban Shelter", 
                    location = LatLng(12.9690, 77.5900),
                    type = PlaceType.SHELTER,
                    distance = calculateDistance(currentLocation!!, LatLng(12.9690, 77.5900))
                ),
                PlaceOfInterest(
                    name = "Home of Hope", 
                    location = LatLng(12.9550, 77.6050),
                    type = PlaceType.SHELTER,
                    distance = calculateDistance(currentLocation!!, LatLng(12.9550, 77.6050))
                ),
                PlaceOfInterest(
                    name = "Women's Shelter Bangalore", 
                    location = LatLng(12.9800, 77.6300),
                    type = PlaceType.SHELTER,
                    distance = calculateDistance(currentLocation!!, LatLng(12.9800, 77.6300))
                )
            )
        } else {
            emptyList()
        }
    }
    
    // Initialize camera position
    val defaultLocation = LatLng(12.9716, 77.5946) // Default to Bangalore coordinates
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            currentLocation ?: defaultLocation, 15f
        )
    }
    
    // Map UI settings
    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = false,
            indoorLevelPickerEnabled = true,
            mapToolbarEnabled = true,
            compassEnabled = true
        )
    }
    
    // Map properties
    val mapProperties = remember {
        MapProperties(
            mapType = MapType.NORMAL,
            isMyLocationEnabled = true
        )
    }
    
    // Get current location on screen load
    LaunchedEffect(Unit) {
        try {
            val locationData = locationService.getCurrentLocation()
            locationData?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                currentLocation = latLng
                
                // Move camera to current location
                cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
            }
        } catch (e: Exception) {
            // Handle location error
        } finally {
            isLoading = false
        }
    }
    
    // Update route calculation
    LaunchedEffect(currentLocation, selectedPlace) {
        if (currentLocation != null && selectedPlace != null && showingRoute) {
            routeInfo = fetchRoute(currentLocation!!, selectedPlace!!.location)
        } else {
            routeInfo = null
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = uiSettings,
            onMapLoaded = {
                isLoading = false
            },
            onMapClick = {
                // Clear route if map is clicked
                if (showingRoute) {
                    selectedPlace = null
                    showingRoute = false
                }
            }
        ) {
            // Add marker for current location
            currentLocation?.let { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = "My Location",
                    snippet = "Current location",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }
            
            // Show police stations if selected
            if (showPoliceStations) {
                nearbyPoliceStations.forEach { station ->
                    Marker(
                        state = MarkerState(position = station.location),
                        title = station.name,
                        snippet = "Police Station (${formatDistance(station.distance)})",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE),
                        onClick = {
                            selectedPlace = station
                            showRouteDialog = true
                            true
                        }
                    )
                }
            }
            
            // Show hospitals if selected
            if (showHospitals) {
                nearbyHospitals.forEach { hospital ->
                    Marker(
                        state = MarkerState(position = hospital.location),
                        title = hospital.name,
                        snippet = "Hospital (${formatDistance(hospital.distance)})",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                        onClick = {
                            selectedPlace = hospital
                            showRouteDialog = true
                            true
                        }
                    )
                }
            }
            
            // Show bus stations if selected
            if (showBusStations) {
                nearbyBusStations.forEach { busStation ->
                    Marker(
                        state = MarkerState(position = busStation.location),
                        title = busStation.name,
                        snippet = "Bus Station (${formatDistance(busStation.distance)})",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                        onClick = {
                            selectedPlace = busStation
                            showRouteDialog = true
                            true
                        }
                    )
                }
            }
            
            // Show pharmacies if selected
            if (showPharmacies) {
                nearbyPharmacies.forEach { pharmacy ->
                    Marker(
                        state = MarkerState(position = pharmacy.location),
                        title = pharmacy.name,
                        snippet = "Pharmacy (${formatDistance(pharmacy.distance)})",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE),
                        onClick = {
                            selectedPlace = pharmacy
                            showRouteDialog = true
                            true
                        }
                    )
                }
            }
            
            // Show shelters if selected
            if (showShelters) {
                nearbyShelters.forEach { shelter ->
                    Marker(
                        state = MarkerState(position = shelter.location),
                        title = shelter.name,
                        snippet = "Shelter (${formatDistance(shelter.distance)})",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET),
                        onClick = {
                            selectedPlace = shelter
                            showRouteDialog = true
                            true
                        }
                    )
                }
            }
            
            // Show route if selected
            if (routeInfo != null && showingRoute) {
                Polyline(
                    points = routeInfo!!.points,
                    color = SafePink,
                    width = 8f
                )
            }
        }
        
        // Places filter buttons
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PlaceFilterChip(
                icon = Icons.Default.LocalPolice,
                label = "Police Stations",
                isSelected = showPoliceStations,
                onClick = { 
                    showPoliceStations = !showPoliceStations
                    if (showPoliceStations) {
                        // When enabling, zoom out to see all stations
                        if (currentLocation != null) {
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation!!, 12f)
                        }
                    }
                }
            )
            
            PlaceFilterChip(
                icon = Icons.Default.LocalHospital,
                label = "Hospitals",
                isSelected = showHospitals,
                onClick = { 
                    showHospitals = !showHospitals
                    if (showHospitals) {
                        // When enabling, zoom out to see all hospitals
                        if (currentLocation != null) {
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation!!, 12f)
                        }
                    }
                }
            )
            
            PlaceFilterChip(
                icon = Icons.Default.DirectionsBus,
                label = "Bus Stations",
                isSelected = showBusStations,
                onClick = { 
                    showBusStations = !showBusStations
                    if (showBusStations) {
                        // When enabling, zoom out to see all bus stations
                        if (currentLocation != null) {
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation!!, 12f)
                        }
                    }
                }
            )
            
            PlaceFilterChip(
                icon = Icons.Default.LocalPharmacy,
                label = "Pharmacies",
                isSelected = showPharmacies,
                onClick = { 
                    showPharmacies = !showPharmacies
                    if (showPharmacies) {
                        // When enabling, zoom out to see all pharmacies
                        if (currentLocation != null) {
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation!!, 12f)
                        }
                    }
                }
            )
            
            PlaceFilterChip(
                icon = Icons.Default.NightShelter,
                label = "Shelters",
                isSelected = showShelters,
                onClick = { 
                    showShelters = !showShelters
                    if (showShelters) {
                        // When enabling, zoom out to see all shelters
                        if (currentLocation != null) {
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation!!, 12f)
                        }
                    }
                }
            )
        }
        
        // My Location button
        FloatingActionButton(
            onClick = {
                currentLocation?.let {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = SafePink
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "My Location",
                tint = Color.White
            )
        }
        
        // Loading indicator
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = SafePink
            )
        }
        
        // Route dialog
        if (showRouteDialog && selectedPlace != null) {
            RouteInfoDialog(
                place = selectedPlace!!,
                onDismiss = { showRouteDialog = false },
                onShowRoute = {
                    showRouteDialog = false
                    showingRoute = true
                    // Adjust camera to show both points
                    if (currentLocation != null) {
                        val boundsBuilder = LatLngBounds.builder()
                        boundsBuilder.include(currentLocation!!)
                        boundsBuilder.include(selectedPlace!!.location)
                        
                        // Animate camera to show the route
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(
                            LatLng(
                                (currentLocation!!.latitude + selectedPlace!!.location.latitude) / 2,
                                (currentLocation!!.longitude + selectedPlace!!.location.longitude) / 2
                            ),
                            13f
                        )
                    }
                }
            )
        }
        
        // Display route info when showing a route
        if (showingRoute && selectedPlace != null && routeInfo != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = selectedPlace!!.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "Distance: ${routeInfo!!.distance}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            text = "Est. Time: ${routeInfo!!.duration}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    IconButton(
                        onClick = {
                            showingRoute = false
                            selectedPlace = null
                            routeInfo = null
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Route"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RouteInfoDialog(
    place: PlaceOfInterest,
    onDismiss: () -> Unit,
    onShowRoute: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon based on place type
                val icon = when (place.type) {
                    PlaceType.POLICE -> Icons.Default.LocalPolice
                    PlaceType.HOSPITAL -> Icons.Default.LocalHospital
                    PlaceType.BUS -> Icons.Default.DirectionsBus
                    PlaceType.PHARMACY -> Icons.Default.LocalPharmacy
                    PlaceType.SHELTER -> Icons.Default.NightShelter
                }
                
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = 8.dp),
                    tint = SafePink
                )
                
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = when (place.type) {
                        PlaceType.POLICE -> "Police Station"
                        PlaceType.HOSPITAL -> "Hospital"
                        PlaceType.BUS -> "Bus Station"
                        PlaceType.PHARMACY -> "Pharmacy"
                        PlaceType.SHELTER -> "Shelter"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Distance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatDistance(place.distance),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Column {
                        Text(
                            text = "Est. Time",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatTime(place.distance),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Show route button
                Button(
                    onClick = onShowRoute,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SafePink
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Directions,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Show Route",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                
                // Cancel button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun PlaceFilterChip(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.height(36.dp),
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) SafePink else MaterialTheme.colorScheme.surface,
        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, if (isSelected) SafePink else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        tonalElevation = if (isSelected) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp
            )
        }
    }
}

// Utility functions
fun calculateDistance(source: LatLng, destination: LatLng): Double {
    val earthRadius = 6371.0 // Radius of the earth in km
    
    val latDistance = Math.toRadians(destination.latitude - source.latitude)
    val lngDistance = Math.toRadians(destination.longitude - source.longitude)
    
    val a = sin(latDistance / 2) * sin(latDistance / 2) +
            cos(Math.toRadians(source.latitude)) * cos(Math.toRadians(destination.latitude)) *
            sin(lngDistance / 2) * sin(lngDistance / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    
    return earthRadius * c
}

fun formatDistance(distance: Double): String {
    return if (distance < 1.0) {
        "${(distance * 1000).toInt()} m"
    } else {
        "${String.format("%.1f", distance)} km"
    }
}

fun formatTime(distance: Double): String {
    // Assuming average walking speed of 5 km/h
    val walkingTimeHours = distance / 5.0
    val walkingTimeMinutes = walkingTimeHours * 60
    
    return when {
        walkingTimeMinutes < 1 -> "< 1 min"
        walkingTimeMinutes < 60 -> "${walkingTimeMinutes.toInt()} min"
        else -> "${String.format("%.1f", walkingTimeHours)} hr"
    }
}

enum class PlaceType {
    POLICE,
    HOSPITAL,
    BUS,
    PHARMACY,
    SHELTER
}

data class PlaceOfInterest(
    val name: String,
    val location: LatLng,
    val type: PlaceType,
    val distance: Double // Distance from current location in km
)

data class RouteInfo(
    val points: List<LatLng>,
    val distance: String,
    val duration: String
) 