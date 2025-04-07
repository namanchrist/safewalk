package com.example.safewalk.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.safewalk.model.User
import com.example.safewalk.ui.EmergencyContactsScreen
import com.example.safewalk.ui.home.HomeScreen
import com.example.safewalk.ui.map.MapScreen
import com.example.safewalk.ui.profile.ProfileScreen
import com.example.safewalk.ui.SOSScreen
import com.example.safewalk.ui.auth.LoginScreen
import com.example.safewalk.ui.auth.RegisterScreen
import com.example.safewalk.ui.theme.SafePink
import com.example.safewalk.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

// Route constants
const val ROUTE_SPLASH = "splash"
const val ROUTE_LOGIN = "login"
const val ROUTE_REGISTER = "register"
const val ROUTE_MAIN = "main"
const val ROUTE_EMERGENCY_CONTACTS = "emergency_contacts"
const val ROUTE_SOS = "sos"
const val ROUTE_HOME = "home"
const val ROUTE_MAP = "map"
const val ROUTE_PROFILE = "profile"
const val ROUTE_SETTINGS = "settings"
const val ROUTE_HELP = "help"
const val ROUTE_ABOUT = "about"

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle(initialValue = null)
    
    if (currentUser == null) {
        // User is not logged in, show auth screens
        AuthNavigation(authViewModel)
    } else {
        // User is logged in, show main app screens
        MainNavigation(authViewModel, currentUser!!)
    }
}

@Composable
fun AuthNavigation(
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = ROUTE_LOGIN) {
        composable(ROUTE_LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(ROUTE_REGISTER) }
            )
        }
        
        composable(ROUTE_REGISTER) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(
    authViewModel: AuthViewModel,
    currentUser: User
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Define primary navigation items
    val primaryItems = listOf(
        NavigationItem(
            route = ROUTE_HOME,
            icon = Icons.Default.Home,
            label = "Home"
        ),
        NavigationItem(
            route = ROUTE_SOS,
            icon = Icons.Default.Warning,
            label = "SOS"
        ),
        NavigationItem(
            route = ROUTE_EMERGENCY_CONTACTS,
            icon = Icons.Default.ContactPhone,
            label = "Emergency Contacts"
        ),
        NavigationItem(
            route = ROUTE_MAP,
            icon = Icons.Default.LocationOn,
            label = "Safety Map"
        )
    )
    
    // Define secondary navigation items
    val secondaryItems = listOf(
        NavigationItem(
            route = ROUTE_PROFILE,
            icon = Icons.Default.Person,
            label = "Profile"
        ),
        NavigationItem(
            route = ROUTE_SETTINGS,
            icon = Icons.Default.Settings,
            label = "Settings"
        ),
        NavigationItem(
            route = ROUTE_HELP,
            icon = Icons.Default.Help,
            label = "Help & Support"
        ),
        NavigationItem(
            route = ROUTE_ABOUT,
            icon = Icons.Default.Info,
            label = "About"
        )
    )
    
    // State for the navigation drawer
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Navigation drawer content
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(12.dp))
                
                // User profile in drawer header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = SafePink
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser.name.first().toString(),
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = currentUser.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = currentUser.email,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Primary navigation items
                Text(
                    text = "Safety Features",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                primaryItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Secondary navigation items
                Text(
                    text = "Account & Support",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                secondaryItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Sign out button at the bottom
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out") },
                    label = { Text("Sign Out") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            authViewModel.signOut()
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = when(currentDestination?.route) {
                                ROUTE_HOME -> "SafeWalk"
                                ROUTE_SOS -> "Emergency SOS"
                                ROUTE_EMERGENCY_CONTACTS -> "Emergency Contacts"
                                ROUTE_MAP -> "Safety Map"
                                ROUTE_PROFILE -> "My Profile"
                                ROUTE_SETTINGS -> "Settings"
                                ROUTE_HELP -> "Help & Support"
                                ROUTE_ABOUT -> "About SafeWalk"
                                else -> "SafeWalk"
                            }
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SafePink,
                        titleContentColor = Color.White
                    ),
                    navigationIcon = {
                        IconButton(onClick = { 
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = ROUTE_HOME,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(ROUTE_HOME) {
                    HomeScreen(
                        user = currentUser,
                        navigateToSOS = { navController.navigate(ROUTE_SOS) },
                        navigateToEmergencyContacts = { navController.navigate(ROUTE_EMERGENCY_CONTACTS) },
                        navigateToMap = { navController.navigate(ROUTE_MAP) },
                        navigateToProfile = { navController.navigate(ROUTE_PROFILE) }
                    )
                }
                
                composable(ROUTE_SOS) {
                    SOSScreen(
                        onNavigateBack = { navController.navigateUp() }
                    )
                }
                
                composable(ROUTE_EMERGENCY_CONTACTS) {
                    EmergencyContactsScreen(
                        navigateToSOS = { navController.navigate(ROUTE_SOS) }
                    )
                }
                
                composable(ROUTE_MAP) {
                    MapScreen(
                        onNavigateBack = { navController.navigateUp() }
                    )
                }
                
                composable(ROUTE_PROFILE) {
                    ProfileScreen(
                        viewModel = authViewModel,
                        user = currentUser,
                        onNavigateBack = { navController.navigateUp() },
                        onSignOut = {
                            // When user signs out, navigate back to login screen
                            // This will be handled in AppNavigation since currentUser becomes null
                        }
                    )
                }
                
                // TODO: Implement these screens
                composable(ROUTE_SETTINGS) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Settings Screen (Coming Soon)")
                    }
                }
                
                composable(ROUTE_HELP) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Help & Support Screen (Coming Soon)")
                    }
                }
                
                composable(ROUTE_ABOUT) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("About SafeWalk Screen (Coming Soon)")
                    }
                }
            }
        }
    }
}

data class NavigationItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) 