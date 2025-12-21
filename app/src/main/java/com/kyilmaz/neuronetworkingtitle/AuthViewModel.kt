package com.kyilmaz.neuronetworkingtitle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Replaced Firebase with Mock Auth to prevent crashes due to missing google-services.json
class AuthViewModel : ViewModel() {

    // private val auth: FirebaseAuth = Firebase.auth // CAUSES CRASH without google-services.json

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // 2FA Logic
    private val _is2FAEnabled = MutableStateFlow(false)
    val is2FAEnabled = _is2FAEnabled.asStateFlow()

    private val _is2FARequired = MutableStateFlow(false)
    val is2FARequired = _is2FARequired.asStateFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                delay(1000) // Simulate network
                if (_is2FAEnabled.value) {
                    _is2FARequired.value = true
                } else {
                    _user.value = User(id = "mock_user_id", name = "Mock User", avatarUrl = "", isVerified = true)
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun verify2FA(code: String) {
        viewModelScope.launch {
            delay(500)
            if (code == "123456") { // Mock Verification
                _is2FARequired.value = false
                _user.value = User(id = "mock_user_id", name = "Mock User", avatarUrl = "", isVerified = true)
            } else {
                _error.value = "Invalid 2FA Code"
            }
        }
    }

    fun toggle2FA(enabled: Boolean) {
        _is2FAEnabled.value = enabled
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            delay(1000)
            _user.value = User(id = "mock_user_id", name = "Mock User", avatarUrl = "", isVerified = true)
        }
    }

    fun signOut() {
        _user.value = null
        _is2FARequired.value = false
    }

    fun clearError() {
        _error.value = null
    }

    // Developer Option: Reset 2FA State
    fun reset2FAState() {
        _is2FARequired.value = false
        _is2FAEnabled.value = false
    }
}
