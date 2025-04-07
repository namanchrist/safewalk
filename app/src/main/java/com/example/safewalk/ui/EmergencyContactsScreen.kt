package com.example.safewalk.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.safewalk.R
import com.example.safewalk.model.EmergencyContact
import com.example.safewalk.model.PhoneContact
import com.example.safewalk.viewmodel.EmergencyContactViewModel
import com.example.safewalk.viewmodel.ViewModelFactory

@Composable
fun EmergencyContactsScreen(
    navigateToSOS: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: EmergencyContactViewModel = viewModel(
        factory = ViewModelFactory(context)
    )
    
    val contacts = viewModel.contacts.collectAsStateWithLifecycle(initialValue = emptyList()).value
    val isLoading = viewModel.loading.collectAsStateWithLifecycle(initialValue = false).value
    val error = viewModel.error.collectAsStateWithLifecycle(initialValue = null).value
    
    var showAddContactDialog by remember { mutableStateOf(false) }
    var contactToEdit by remember { mutableStateOf<EmergencyContact?>(null) }
    
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Phone contacts access granted, but we don't have device contacts feature now
            showAddContactDialog = true
        } else {
            Toast.makeText(
                context,
                "Permission required to access contacts",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.loadEmergencyContacts()
    }
    
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SOSHeader(navigateToSOS)
            
            EmergencyContactsList(
                contacts = contacts,
                onEditContact = { contactToEdit = it; showAddContactDialog = true },
                onDeleteContact = { viewModel.deleteContact(it.id) },
                onCallContact = { contact ->
                    val callIntent = Intent(Intent.ACTION_CALL).apply {
                        data = Uri.parse("tel:${contact.phoneNumber}")
                    }
                    
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CALL_PHONE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        context.startActivity(callIntent)
                    } else {
                        Toast.makeText(
                            context,
                            "Call permission required",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }
        
        // FAB to add new contact
        FloatingActionButton(
            onClick = {
                showAddContactDialog = true
            },
            containerColor = Color(0xFFE91E63), // Pink color
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Contact"
            )
        }
        
        // Add/Edit Contact Dialog
        if (showAddContactDialog) {
            AddEditContactDialog(
                contact = contactToEdit,
                onDismiss = {
                    showAddContactDialog = false
                    contactToEdit = null
                },
                onSave = { name, phoneNumber, relationship, isGuardian ->
                    if (contactToEdit != null) {
                        // Update existing contact
                        val updatedContact = contactToEdit!!.copy(
                            name = name,
                            phoneNumber = phoneNumber,
                            relationship = relationship,
                            isGuardian = isGuardian
                        )
                        viewModel.updateContact(updatedContact)
                    } else {
                        // Add new contact
                        viewModel.addContact(
                            name = name,
                            phoneNumber = phoneNumber,
                            relationship = relationship,
                            isGuardian = isGuardian
                        )
                    }
                    showAddContactDialog = false
                    contactToEdit = null
                }
            )
        }
        
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFFE91E63) // Pink color
            )
        }
    }
}

@Composable
fun SOSHeader(onSOSClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Button(
            onClick = onSOSClick,
            modifier = Modifier
                .align(Alignment.Center)
                .size(120.dp)
                .clip(CircleShape),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red
            )
        ) {
            Text(
                text = "SOS",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
        }
    }
}

@Composable
fun EmergencyContactsList(
    contacts: List<EmergencyContact>,
    onEditContact: (EmergencyContact) -> Unit,
    onDeleteContact: (EmergencyContact) -> Unit,
    onCallContact: (EmergencyContact) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Emergency Contacts",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE91E63), // Pink color
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        val guardian = contacts.find { it.isGuardian }
        
        guardian?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(
                        width = 2.dp,
                        color = Color(0xFFE91E63), // Pink color
                        shape = RoundedCornerShape(8.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFCE4EC) // Light pink color
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                ContactItem(
                    contact = it,
                    isGuardian = true,
                    onEditContact = onEditContact,
                    onDeleteContact = onDeleteContact,
                    onCallContact = onCallContact
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Other Contacts",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE91E63), // Pink color
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        LazyColumn {
            val otherContacts = contacts.filter { !it.isGuardian }
            items(otherContacts) { contact ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFCE4EC) // Light pink color
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    ContactItem(
                        contact = contact,
                        isGuardian = false,
                        onEditContact = onEditContact,
                        onDeleteContact = onDeleteContact,
                        onCallContact = onCallContact
                    )
                }
            }
        }
    }
}

@Composable
fun ContactItem(
    contact: EmergencyContact,
    isGuardian: Boolean,
    onEditContact: (EmergencyContact) -> Unit,
    onDeleteContact: (EmergencyContact) -> Unit,
    onCallContact: (EmergencyContact) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.titleMedium,
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
            
            if (isGuardian) {
                Text(
                    text = "Guardian",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE91E63), // Pink color
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Row {
            IconButton(onClick = { onCallContact(contact) }) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    tint = Color(0xFFE91E63) // Pink color
                )
            }
            
            IconButton(onClick = { onEditContact(contact) }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Color(0xFFE91E63) // Pink color
                )
            }
            
            IconButton(onClick = { onDeleteContact(contact) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red
                )
            }
        }
    }
}

@Composable
fun AddEditContactDialog(
    contact: EmergencyContact?,
    onDismiss: () -> Unit,
    onSave: (name: String, phoneNumber: String, relationship: String, isGuardian: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(contact?.name ?: "") }
    var phoneNumber by remember { mutableStateOf(contact?.phoneNumber ?: "") }
    var relationship by remember { mutableStateOf(contact?.relationship ?: "") }
    var isGuardian by remember { mutableStateOf(contact?.isGuardian ?: false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = if (contact == null) "Add Emergency Contact" else "Edit Emergency Contact",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE91E63) // Pink color
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE91E63), // Pink color
                        focusedLabelColor = Color(0xFFE91E63) // Pink color
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE91E63), // Pink color
                        focusedLabelColor = Color(0xFFE91E63) // Pink color
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = relationship,
                    onValueChange = { relationship = it },
                    label = { Text("Relationship (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE91E63), // Pink color
                        focusedLabelColor = Color(0xFFE91E63) // Pink color
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isGuardian,
                        onCheckedChange = { isGuardian = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFFE91E63) // Pink color
                        )
                    )
                    
                    Text("Set as Guardian Contact")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (name.isNotBlank() && phoneNumber.isNotBlank()) {
                                onSave(name, phoneNumber, relationship, isGuardian)
                            }
                        },
                        enabled = name.isNotBlank() && phoneNumber.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE91E63) // Pink color
                        )
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
} 
