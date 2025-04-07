package com.example.safewalk.repository

import com.example.safewalk.model.AlertType
import com.example.safewalk.model.EmergencyContact
import com.example.safewalk.model.SafetyAlert
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class SafetyRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val alertsCollection = firestore.collection("safety_alerts")
    private val contactsCollection = firestore.collection("emergency_contacts")
    
    suspend fun createAlert(
        userId: String,
        alertType: AlertType,
        latitude: Double,
        longitude: Double,
        message: String = ""
    ): Result<SafetyAlert> {
        return try {
            val documentRef = alertsCollection.document()
            val alert = SafetyAlert(
                id = documentRef.id,
                userId = userId,
                alertType = alertType,
                latitude = latitude,
                longitude = longitude,
                timestamp = Timestamp.now(),
                message = message,
                resolved = false
            )
            documentRef.set(alert).await()
            Result.success(alert)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun resolveAlert(alertId: String): Result<Unit> {
        return try {
            alertsCollection.document(alertId).update("resolved", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getUserAlerts(userId: String): Flow<List<SafetyAlert>> = flow {
        try {
            val snapshot = alertsCollection
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            val alerts = snapshot.toObjects(SafetyAlert::class.java)
            emit(alerts)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    suspend fun getUserEmergencyContacts(userId: String): Result<List<EmergencyContact>> {
        return try {
            val snapshot = contactsCollection.whereEqualTo("userId", userId).get().await()
            val contacts = snapshot.toObjects(EmergencyContact::class.java)
            Result.success(contacts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 