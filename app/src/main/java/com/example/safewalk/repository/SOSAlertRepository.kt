package com.example.safewalk.repository

import com.example.safewalk.model.SOSAlert
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class SOSAlertRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _alerts = MutableStateFlow<List<SOSAlert>>(emptyList())
    val alerts: StateFlow<List<SOSAlert>> = _alerts
    
    // Get the current user ID
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    // Get alerts collection reference for current user
    private fun getAlertsCollection() = getCurrentUserId()?.let {
        firestore.collection("users").document(it).collection("sos_alerts")
    }
    
    // Create a new SOS alert
    suspend fun createSOSAlert(alert: SOSAlert): SOSAlert {
        val alertsCollection = getAlertsCollection() ?: throw Exception("User not authenticated")
        
        // If alert doesn't have a userId yet, set it to the current user
        val updatedAlert = if (alert.userId == "current_user") {
            alert.copy(userId = getCurrentUserId() ?: "unknown")
        } else {
            alert
        }
        
        // Add to Firestore
        alertsCollection.document(updatedAlert.id).set(updatedAlert).await()
        
        // Update local state
        val updatedAlerts = _alerts.value.toMutableList()
        updatedAlerts.add(updatedAlert)
        _alerts.value = updatedAlerts
        
        return updatedAlert
    }
    
    // Get all alerts for the current user
    suspend fun getAllAlerts(): List<SOSAlert> {
        val alertsCollection = getAlertsCollection() ?: return emptyList()
        
        try {
            val snapshot = alertsCollection.get().await()
            val alerts = snapshot.documents.mapNotNull { doc ->
                doc.toObject(SOSAlert::class.java)
            }
            _alerts.value = alerts
            return alerts
        } catch (e: Exception) {
            throw Exception("Failed to fetch alerts: ${e.message}")
        }
    }
    
    // Update alert status
    suspend fun updateAlertStatus(alertId: String, status: String): SOSAlert? {
        val alertsCollection = getAlertsCollection() ?: throw Exception("User not authenticated")
        
        // Get the alert
        val alertDoc = alertsCollection.document(alertId).get().await()
        val alert = alertDoc.toObject(SOSAlert::class.java) ?: return null
        
        // Update status
        val updatedAlert = alert.copy(status = status)
        
        // Update in Firestore
        alertsCollection.document(alertId).update("status", status).await()
        
        // Update local state
        val updatedAlerts = _alerts.value.toMutableList()
        val index = updatedAlerts.indexOfFirst { it.id == alertId }
        if (index >= 0) {
            updatedAlerts[index] = updatedAlert
            _alerts.value = updatedAlerts
        }
        
        return updatedAlert
    }
    
    // Delete alert
    suspend fun deleteAlert(alertId: String) {
        val alertsCollection = getAlertsCollection() ?: throw Exception("User not authenticated")
        
        // Delete from Firestore
        alertsCollection.document(alertId).delete().await()
        
        // Update local state
        val updatedAlerts = _alerts.value.toMutableList()
        updatedAlerts.removeIf { it.id == alertId }
        _alerts.value = updatedAlerts
    }
} 