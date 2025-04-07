package com.example.safewalk.model

import androidx.annotation.Keep

@Keep
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val guardianPhoneNumber: String = "",
    val profileImageUrl: String = "",
    val userType: UserType = UserType.USER,
    val age: Int = 0,
    val emergencyContacts: List<EmergencyContact> = emptyList()
)

enum class UserType {
    USER,
    GUARDIAN
} 