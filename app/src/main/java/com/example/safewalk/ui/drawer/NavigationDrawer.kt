package com.example.safewalk.ui.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.safewalk.model.User
import com.example.safewalk.ui.theme.SafePink
import kotlinx.coroutines.launch

enum class DrawerScreen {
    HOME,
    MAP,
    CONTACTS,
    PROFILE
}

@Composable
fun SafeWalkNavigationDrawer(
    drawerState: DrawerState,
    currentScreen: DrawerScreen,
    user: User,
    onScreenSelected: (DrawerScreen) -> Unit,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // Drawer Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SafePink)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.name.first().toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Navigation Items
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = currentScreen == DrawerScreen.HOME,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onScreenSelected(DrawerScreen.HOME)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Map, contentDescription = "Safe Routes") },
                    label = { Text("Safe Routes") },
                    selected = currentScreen == DrawerScreen.MAP,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onScreenSelected(DrawerScreen.MAP)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Contacts, contentDescription = "Emergency Contacts") },
                    label = { Text("Emergency Contacts") },
                    selected = currentScreen == DrawerScreen.CONTACTS,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onScreenSelected(DrawerScreen.CONTACTS)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                    label = { Text("My Profile") },
                    selected = currentScreen == DrawerScreen.PROFILE,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onScreenSelected(DrawerScreen.PROFILE)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                
                Divider(
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                
                // Nearby Services
                Text(
                    text = "Nearby Services",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 24.dp, bottom = 8.dp, top = 8.dp)
                )
                
                NearbyServiceItem(
                    icon = Icons.Default.LocalHospital,
                    title = "Hospitals",
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onScreenSelected(DrawerScreen.MAP)
                    }
                )
                
                NearbyServiceItem(
                    icon = Icons.Default.LocationOn,
                    title = "Police Stations",
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onScreenSelected(DrawerScreen.MAP)
                    }
                )
                
                NearbyServiceItem(
                    icon = Icons.Default.MoreVert,
                    title = "Bus Stations",
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onScreenSelected(DrawerScreen.MAP)
                    }
                )
            }
        },
        content = content
    )
}

@Composable
fun NearbyServiceItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = title) },
        label = { Text(title) },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
} 