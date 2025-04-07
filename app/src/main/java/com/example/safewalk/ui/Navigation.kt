package com.example.safewalk.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.safewalk.ui.EmergencyContactsScreen
import com.example.safewalk.ui.SOSScreen
import com.example.safewalk.ui.navigation.ROUTE_MAIN

const val ROUTE_EMERGENCY_CONTACTS = "emergency_contacts"
const val ROUTE_SOS = "sos"

@Composable
fun SafeWalkNavHost(
    navController: NavHostController,
    // ... existing parameters ...
) {
    NavHost(
        navController = navController,
        startDestination = ROUTE_MAIN
    ) {
        // ... existing composable routes ...
        
        composable(ROUTE_EMERGENCY_CONTACTS) {
            EmergencyContactsScreen(
                navigateToSOS = { navController.navigate(ROUTE_SOS) }
            )
        }
        
        composable(ROUTE_SOS) {
            SOSScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
} 