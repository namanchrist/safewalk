package com.example.safewalk.repository

import com.example.safewalk.model.User
import com.example.safewalk.model.UserType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    
    suspend fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        
        return try {
            val document = usersCollection.document(firebaseUser.uid).get().await()
            document.toObject(User::class.java)
        } catch (e: Exception) {
            throw Exception("Failed to get user profile: ${e.message}")
        }
    }
    
    suspend fun login(email: String, password: String): User {
        try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("Authentication failed")
            
            val document = usersCollection.document(userId).get().await()
            return document.toObject(User::class.java) ?: throw Exception("User profile not found")
        } catch (e: Exception) {
            throw Exception("Login failed: ${e.message}")
        }
    }
    
    suspend fun register(
        name: String, 
        email: String, 
        password: String, 
        phoneNumber: String = "",
        guardianPhoneNumber: String = "",
        userType: UserType = UserType.USER,
        age: Int = 0
    ): User {
        try {
            // Create the Firebase Auth user
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("Failed to create user account")
            
            // Create the user profile in Firestore
            val user = User(
                id = userId,
                name = name,
                email = email,
                phoneNumber = phoneNumber,
                guardianPhoneNumber = guardianPhoneNumber,
                userType = userType,
                age = age
            )
            
            // Save user data to Firestore
            usersCollection.document(userId).set(user).await()
            
            return user
        } catch (e: Exception) {
            throw Exception("Registration failed: ${e.message}")
        }
    }
    
    fun logout() {
        auth.signOut()
    }
} 