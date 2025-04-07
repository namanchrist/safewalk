package com.example.safewalk.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safewalk.model.User
import com.example.safewalk.ui.theme.SafePink
import com.example.safewalk.ui.theme.SafePinkLight

@Composable
fun HomeScreen(
    user: User,
    navigateToSOS: () -> Unit,
    navigateToEmergencyContacts: () -> Unit,
    navigateToMap: () -> Unit,
    navigateToProfile: () -> Unit
) {
    // Sample news data
    val newsItems = listOf(
        NewsItem(
            title = "Women's Safety App Launches in Major Cities",
            description = "A new app focused on women's safety has launched in major cities across the country, providing real-time location sharing and emergency alerts.",
            date = "April 5, 2025"
        ),
        NewsItem(
            title = "Government Announces New Women's Safety Initiatives",
            description = "The government has announced new initiatives to improve women's safety, including increased street lighting and emergency response teams.",
            date = "April 3, 2025"
        ),
        NewsItem(
            title = "Community Self-Defense Classes See Record Attendance",
            description = "Self-defense classes for women have seen record attendance as communities come together to promote safety and empowerment.",
            date = "April 1, 2025"
        )
    )
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome message
        item {
            Text(
                text = "Welcome, ${user.name}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = SafePink
            )
        }
        
        // SOS Button
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = navigateToSOS,
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text(
                        text = "SOS",
                        fontSize = 32.sp,
                        color = Color.White
                    )
                }
            }
        }
        
        // Feature cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FeatureCard(
                    icon = Icons.Default.ContactPhone,
                    title = "Emergency Contacts",
                    onClick = navigateToEmergencyContacts,
                    color = SafePink
                )
                
                FeatureCard(
                    icon = Icons.Default.LocationOn,
                    title = "Map",
                    onClick = navigateToMap,
                    color = SafePink
                )
            }
        }
        
        // News section
        item {
            Text(
                text = "Women's Empowerment News",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = SafePink,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
        
        // News items
        items(newsItems) { newsItem ->
            NewsCard(newsItem = newsItem)
        }
        
        // Safety Tips section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SafePinkLight
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Safety Tips",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SafePink,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "• Share your location with trusted contacts when traveling",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    Text(
                        text = "• Avoid poorly lit areas at night",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    Text(
                        text = "• Keep your phone charged when going out",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    color: Color
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(120.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = SafePinkLight
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(36.dp),
                tint = color
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = color
            )
        }
    }
}

@Composable
fun NewsCard(newsItem: NewsItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = SafePinkLight
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = newsItem.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SafePink
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = newsItem.description,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = newsItem.date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

data class NewsItem(
    val title: String,
    val description: String,
    val date: String
) 