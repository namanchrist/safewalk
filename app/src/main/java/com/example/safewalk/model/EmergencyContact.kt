package com.example.safewalk.model

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentId

@Keep
data class EmergencyContact(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val relationship: String = "",
    val isGuardian: Boolean = false
) 