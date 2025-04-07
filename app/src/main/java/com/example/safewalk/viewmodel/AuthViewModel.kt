package com.example.safewalk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safewalk.model.User
import com.example.safewalk.model.UserType
import com.example.safewalk.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    init {
        // Check if user is already logged in
        checkCurrentUser()
    }
    
    private fun checkCurrentUser() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = userRepository.getCurrentUser()
                _currentUser.value = user
            } catch (e: Exception) {
                _error.value = "Failed to get current user: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val user = userRepository.login(email, password)
                _currentUser.value = user
            } catch (e: Exception) {
                _error.value = "Login failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun register(
        name: String, 
        email: String, 
        password: String,
        phoneNumber: String = "",
        guardianPhoneNumber: String = "",
        userType: UserType = UserType.USER,
        age: Int = 0
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val user = userRepository.register(
                    name = name,
                    email = email,
                    password = password,
                    phoneNumber = phoneNumber,
                    guardianPhoneNumber = guardianPhoneNumber,
                    userType = userType,
                    age = age
                )
                _currentUser.value = user
            } catch (e: Exception) {
                _error.value = "Registration failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                userRepository.logout()
                _currentUser.value = null
            } catch (e: Exception) {
                _error.value = "Logout failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Alias for logout to match the name used in ProfileScreen
    fun signOut() {
        logout()
    }
} 