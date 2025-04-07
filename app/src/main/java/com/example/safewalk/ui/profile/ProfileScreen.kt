package com.example.safewalk.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.safewalk.model.EmergencyContact
import com.example.safewalk.model.User
import com.example.safewalk.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: AuthViewModel,
    user: User,
    onNavigateBack: () -> Unit,
    onSignOut: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE91E63), // Pink color
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Picture
            item {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFCE4EC)), // Light pink color
                    contentAlignment = Alignment.Center
                ) {
                    if (user.profileImageUrl.isNotEmpty()) {
                        // If user has a profile image, display it
                        // This would require a proper image loading library like Coil
                        // For now, we'll just show the first letter of their name
                        Text(
                            text = user.name.first().toString(),
                            style = MaterialTheme.typography.displaySmall,
                            color = Color(0xFFE91E63) // Pink color
                        )
                    } else {
                        Text(
                            text = user.name.first().toString(),
                            style = MaterialTheme.typography.displaySmall,
                            color = Color(0xFFE91E63) // Pink color
                        )
                    }
                }
            }
            
            // User Name
            item {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE91E63) // Pink color
                )
            }
            
            // User Type
            item {
                Text(
                    text = user.userType.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
            
            // User Information Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFCE4EC) // Light pink color
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Personal Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE91E63), // Pink color
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // Email
                        ProfileInfoItem(
                            icon = Icons.Default.Email,
                            label = "Email",
                            value = user.email
                        )
                        
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color(0xFFE91E63).copy(alpha = 0.3f) // Light pink color
                        )
                        
                        // Phone
                        ProfileInfoItem(
                            icon = Icons.Default.Phone,
                            label = "Phone Number",
                            value = user.phoneNumber.ifEmpty { "Not provided" }
                        )
                        
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color(0xFFE91E63).copy(alpha = 0.3f) // Light pink color
                        )
                        
                        // Age
                        ProfileInfoItem(
                            icon = Icons.Default.Person,
                            label = "Age",
                            value = if (user.age > 0) user.age.toString() else "Not provided"
                        )
                        
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color(0xFFE91E63).copy(alpha = 0.3f) // Light pink color
                        )
                        
                        // Guardian Phone
                        ProfileInfoItem(
                            icon = Icons.Default.Phone,
                            label = "Guardian Phone",
                            value = user.guardianPhoneNumber.ifEmpty { "Not provided" }
                        )
                    }
                }
            }
            
            // Emergency Contacts Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFCE4EC) // Light pink color
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Emergency Contacts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE91E63), // Pink color
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        if (user.emergencyContacts.isEmpty()) {
                            Text(
                                text = "No emergency contacts added yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        } else {
                            user.emergencyContacts.forEach { contact ->
                                EmergencyContactItem(contact = contact)
                                
                                if (contact != user.emergencyContacts.last()) {
                                    Divider(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        color = Color(0xFFE91E63).copy(alpha = 0.3f) // Light pink color
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Sign Out Button
            item {
                Button(
                    onClick = {
                        viewModel.signOut()
                        onSignOut()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE91E63) // Pink color
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Sign Out",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Sign Out")
                }
            }
        }
    }
}

@Composable
fun ProfileInfoItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFFE91E63), // Pink color
            modifier = Modifier.size(24.dp)
        )
        
        Column(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun EmergencyContactItem(contact: EmergencyContact) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ContactPhone,
            contentDescription = "Contact",
            tint = Color(0xFFE91E63), // Pink color
            modifier = Modifier.size(24.dp)
        )
        
        Column(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = contact.phoneNumber,
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (contact.relationship.isNotEmpty()) {
                Text(
                    text = contact.relationship,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            if (contact.isGuardian) {
                Text(
                    text = "Guardian",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE91E63), // Pink color
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
} 