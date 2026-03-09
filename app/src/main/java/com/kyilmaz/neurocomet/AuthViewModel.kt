package com.kyilmaz.neurocomet

import android.content.Context
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyilmaz.neurocomet.auth.AuthResult
import com.kyilmaz.neurocomet.auth.AuthenticationManager
import com.kyilmaz.neurocomet.auth.BiometricStatus
import com.kyilmaz.neurocomet.auth.Fido2Credential
import com.kyilmaz.neurocomet.auth.TotpSecret
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * AuthViewModel - Manages authentication state and integrates with AuthenticationManager
 */
class AuthViewModel : ViewModel() {

    private val TAG = "AuthViewModel"
    private var authManager: AuthenticationManager? = null

    private fun buildMockUser(name: String): User = User(
        id = "mock_user_id",
        name = name,
        avatarUrl = "",
        isVerified = true,
        personality = "A mock user."
    )

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // 2FA Logic
    private val _is2FAEnabled = MutableStateFlow(false)
    val is2FAEnabled = _is2FAEnabled.asStateFlow()

    private val _is2FARequired = MutableStateFlow(false)
    val is2FARequired = _is2FARequired.asStateFlow()

    private val _ageVerifiedAudience = MutableStateFlow<Audience?>(null)
    val ageVerifiedAudience = _ageVerifiedAudience.asStateFlow()

    // Authentication method states
    private val _biometricEnabled = MutableStateFlow(false)
    val biometricEnabled: StateFlow<Boolean> = _biometricEnabled.asStateFlow()

    private val _fido2Enabled = MutableStateFlow(false)
    val fido2Enabled: StateFlow<Boolean> = _fido2Enabled.asStateFlow()

    private val _totpEnabled = MutableStateFlow(false)
    val totpEnabled: StateFlow<Boolean> = _totpEnabled.asStateFlow()

    private val _backupCodesRemaining = MutableStateFlow(0)
    val backupCodesRemaining: StateFlow<Int> = _backupCodesRemaining.asStateFlow()

    private val _pendingTotpSecret = MutableStateFlow<TotpSecret?>(null)
    val pendingTotpSecret: StateFlow<TotpSecret?> = _pendingTotpSecret.asStateFlow()

    private val _fido2Credentials = MutableStateFlow<List<Fido2Credential>>(emptyList())
    val fido2Credentials: StateFlow<List<Fido2Credential>> = _fido2Credentials.asStateFlow()

    /**
     * Initialize the AuthenticationManager with context
     */
    fun initialize(context: Context) {
        authManager = AuthenticationManager.getInstance(context)
        refreshAuthState()
    }

    /**
     * Refresh authentication state from AuthenticationManager
     */
    fun refreshAuthState() {
        authManager?.let { manager ->
            viewModelScope.launch {
                manager.biometricEnabled.collect { _biometricEnabled.value = it }
            }
            viewModelScope.launch {
                manager.fido2Enabled.collect { _fido2Enabled.value = it }
            }
            viewModelScope.launch {
                manager.totpEnabled.collect { _totpEnabled.value = it }
            }
            viewModelScope.launch {
                manager.backupCodesRemaining.collect { _backupCodesRemaining.value = it }
            }
            _fido2Credentials.value = manager.getFido2Credentials()
        }
    }

    // ==================== BIOMETRIC AUTHENTICATION ====================

    /**
     * Check biometric availability
     */
    fun checkBiometricStatus(): BiometricStatus {
        return authManager?.checkBiometricStatus() ?: BiometricStatus.Unsupported
    }

    /**
     * Authenticate with biometrics
     */
    fun authenticateWithBiometric(
        activity: FragmentActivity,
        title: String = "Authenticate",
        subtitle: String = "Use your fingerprint or face to continue",
        onResult: (AuthResult) -> Unit
    ) {
        authManager?.showBiometricPrompt(
            activity = activity,
            title = title,
            subtitle = subtitle,
            onResult = { result ->
                if (result is AuthResult.Success) {
                    _is2FARequired.value = false
                }
                onResult(result)
            }
        ) ?: onResult(AuthResult.NotAvailable)
    }

    /**
     * Enable/disable biometric authentication
     */
    fun setBiometricEnabled(enabled: Boolean) {
        authManager?.setBiometricEnabled(enabled)
        _biometricEnabled.value = enabled
        updateGlobal2FAState()
    }

    // ==================== FIDO2/PASSKEY AUTHENTICATION ====================

    /**
     * Check if FIDO2 is supported
     */
    fun isFido2Supported(): Boolean {
        return authManager?.isFido2Supported() ?: false
    }

    /**
     * Register a new FIDO2 credential
     */
    fun registerFido2Credential(displayName: String, onResult: (AuthResult) -> Unit) {
        authManager?.registerFido2Credential(displayName) { result ->
            if (result is AuthResult.Success) {
                _fido2Credentials.value = authManager?.getFido2Credentials() ?: emptyList()
                updateGlobal2FAState()
            }
            onResult(result)
        } ?: onResult(AuthResult.NotAvailable)
    }

    /**
     * Authenticate with FIDO2
     */
    fun authenticateWithFido2(activity: FragmentActivity, onResult: (AuthResult) -> Unit) {
        authManager?.authenticateWithFido2(activity) { result ->
            if (result is AuthResult.Success) {
                _is2FARequired.value = false
                _fido2Credentials.value = authManager?.getFido2Credentials() ?: emptyList()
            }
            onResult(result)
        } ?: onResult(AuthResult.NotAvailable)
    }

    /**
     * Remove a FIDO2 credential
     */
    fun removeFido2Credential(credentialId: String) {
        authManager?.removeFido2Credential(credentialId)
        _fido2Credentials.value = authManager?.getFido2Credentials() ?: emptyList()
        updateGlobal2FAState()
    }

    // ==================== TOTP AUTHENTICATION ====================

    /**
     * Start TOTP setup - generates a secret
     */
    fun startTotpSetup(accountName: String) {
        val secret = authManager?.generateTotpSecret(accountName)
        _pendingTotpSecret.value = secret
    }

    /**
     * Complete TOTP setup by verifying the first code
     */
    fun completeTotpSetup(code: String, onResult: (AuthResult) -> Unit) {
        val secret = _pendingTotpSecret.value
        if (secret == null) {
            onResult(AuthResult.Error("No pending TOTP setup"))
            return
        }

        // Temporarily store the secret to verify
        authManager?.storeTotpSecret(secret)

        if (authManager?.verifyTotpCode(code) == true) {
            _pendingTotpSecret.value = null
            updateGlobal2FAState()
            onResult(AuthResult.Success)
        } else {
            authManager?.disableTotp()
            onResult(AuthResult.Error("Invalid code. Please try again."))
        }
    }

    /**
     * Verify TOTP code
     */
    fun verifyTotpCode(code: String): Boolean {
        val result = authManager?.verifyTotpCode(code) ?: false
        if (result) {
            _is2FARequired.value = false
        }
        return result
    }

    /**
     * Get current TOTP code (for display in dev options)
     */
    fun getCurrentTotpCode(): String? {
        return authManager?.generateTotpCode()
    }

    /**
     * Get TOTP secret URI for QR code
     */
    fun getTotpUri(): String? {
        return authManager?.getTotpSecret()?.toUri()
    }

    /**
     * Disable TOTP
     */
    fun disableTotp() {
        authManager?.disableTotp()
        _pendingTotpSecret.value = null
        updateGlobal2FAState()
    }

    // ==================== EMAIL VERIFICATION ====================

    /**
     * Send email verification code when the current build supports it.
     */
    fun sendEmailVerificationCode(email: String): AuthResult {
        val result = authManager?.sendEmailVerificationCode(email) ?: AuthResult.NotAvailable
        when (result) {
            is AuthResult.Error -> _error.value = result.message
            AuthResult.NotAvailable -> _error.value = "Email verification is unavailable in this build"
            else -> Unit
        }
        return result
    }

    /**
     * Verify email code
     */
    fun verifyEmailCode(code: String, onResult: (AuthResult) -> Unit) {
        val result = authManager?.verifyEmailCode(code) ?: AuthResult.NotAvailable
        if (result is AuthResult.Success) {
            _is2FARequired.value = false
        }
        onResult(result)
    }

    // ==================== BACKUP CODES ====================

    /**
     * Generate new backup codes
     */
    fun generateBackupCodes(): List<String> {
        val codes = authManager?.generateBackupCodes() ?: emptyList()
        _backupCodesRemaining.value = codes.size
        return codes
    }

    /**
     * Verify a backup code
     */
    fun verifyBackupCode(code: String, onResult: (AuthResult) -> Unit) {
        val result = authManager?.verifyBackupCode(code) ?: AuthResult.NotAvailable
        if (result is AuthResult.Success) {
            _is2FARequired.value = false
            _backupCodesRemaining.value = authManager?.getBackupCodes()?.size ?: 0
        }
        onResult(result)
    }

    // ==================== PRIMARY AUTH FLOWS ====================

    /**
     * Sign in with email and password
     */
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                // Input validation — prevent login with empty or malformed credentials
                val trimmedEmail = email.trim()
                // Passwords are NOT trimmed — spaces may be intentional
                val trimmedPassword = password

                if (trimmedEmail.isBlank()) {
                    _error.value = "Email is required"
                    return@launch
                }
                if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
                    _error.value = "Please enter a valid email address"
                    return@launch
                }
                if (trimmedPassword.isBlank()) {
                    _error.value = "Password is required"
                    return@launch
                }
                if (trimmedPassword.length < 6) {
                    _error.value = "Password must be at least 6 characters"
                    return@launch
                }

                val client = AppSupabaseClient.client
                if (client != null) {
                    // Real Supabase Auth
                    client.auth.signInWith(Email) {
                        this.email = trimmedEmail
                        this.password = trimmedPassword
                    }
                    val session = client.auth.currentSessionOrNull()
                    val userId = session?.user?.id
                    if (userId != null) {
                        val meta = session.user?.userMetadata
                        _user.value = User(
                            id = userId,
                            name = meta?.get("display_name")?.toString()?.removeSurrounding("\"")
                                ?: trimmedEmail.substringBefore("@"),
                            avatarUrl = meta?.get("avatar_url")?.toString()?.removeSurrounding("\"") ?: "",
                            isVerified = true,
                            personality = "NeuroComet user"
                        )
                    } else {
                        _error.value = "Sign in failed. Please check your credentials."
                    }
                } else {
                    if (!BuildConfig.DEBUG) {
                        _error.value = "Authentication is temporarily unavailable in this build"
                        return@launch
                    }
                    // Mock mode fallback
                    delay(1000)
                    if (_is2FAEnabled.value) {
                        _is2FARequired.value = true
                    } else {
                        _user.value = buildMockUser(trimmedEmail.substringBefore("@"))
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Sign in failed"
            }
        }
    }

    /**
     * Verify 2FA code
     */
    fun verify2FA(code: String) {
        viewModelScope.launch {
            delay(500)
            // Try TOTP first
            if (verifyTotpCode(code)) {
                _is2FARequired.value = false
                if (_user.value == null) {
                    if (!BuildConfig.DEBUG) {
                        _error.value = "Two-factor verification requires an active sign-in session"
                        return@launch
                    }
                    _user.value = buildMockUser("Mock User")
                }
            } else if (BuildConfig.DEBUG && code == "123456") { // Debug-only fallback mock verification
                _is2FARequired.value = false
                _user.value = buildMockUser("Mock User")
            } else {
                _error.value = "Invalid 2FA Code"
            }
        }
    }

    /**
     * Toggle 2FA enabled state
     */
    fun toggle2FA(enabled: Boolean) {
        _is2FAEnabled.value = enabled
    }

    private fun updateGlobal2FAState() {
        val anyEnabled = _biometricEnabled.value ||
                         _fido2Enabled.value ||
                         _totpEnabled.value ||
                         (_backupCodesRemaining.value > 0)
        _is2FAEnabled.value = anyEnabled
    }

    /**
     * Set the audience for age verification
     */
    fun setAgeVerifiedAudience(audience: Audience) {
        _ageVerifiedAudience.value = audience
    }

    /**
     * Sign up with email and password
     */
    fun signUp(email: String, password: String, audience: Audience?) {
        viewModelScope.launch {
            try {
                // Input validation — prevent sign-up with empty or malformed credentials
                val trimmedEmail = email.trim()
                // Passwords are NOT trimmed — spaces may be intentional
                val trimmedPassword = password

                if (trimmedEmail.isBlank()) {
                    _error.value = "Email is required"
                    return@launch
                }
                if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
                    _error.value = "Please enter a valid email address"
                    return@launch
                }
                if (trimmedPassword.isBlank()) {
                    _error.value = "Password is required"
                    return@launch
                }
                if (trimmedPassword.length < 6) {
                    _error.value = "Password must be at least 6 characters"
                    return@launch
                }

                val client = AppSupabaseClient.client
                if (client != null) {
                    // Real Supabase Auth
                    val displayName = trimmedEmail.substringBefore("@")
                    client.auth.signUpWith(Email) {
                        this.email = trimmedEmail
                        this.password = trimmedPassword
                        this.data = kotlinx.serialization.json.buildJsonObject {
                            put("display_name", kotlinx.serialization.json.JsonPrimitive(displayName))
                            put("username", kotlinx.serialization.json.JsonPrimitive("user_${System.currentTimeMillis() % 100000}"))
                        }
                    }
                    val session = client.auth.currentSessionOrNull()
                    val userId = session?.user?.id
                    if (userId != null) {
                        _user.value = User(
                            id = userId,
                            name = displayName,
                            avatarUrl = "",
                            isVerified = false,
                            personality = "NeuroComet user"
                        )
                    } else {
                        // Supabase may require email confirmation
                        _error.value = "Check your email to confirm your account, then sign in."
                    }
                } else {
                    if (!BuildConfig.DEBUG) {
                        _error.value = "Sign up is temporarily unavailable in this build"
                        return@launch
                    }
                    // Mock mode fallback
                    delay(1000)
                    _user.value = buildMockUser(trimmedEmail.substringBefore("@"))
                }
                audience?.let { _ageVerifiedAudience.value = it }
            } catch (e: Exception) {
                _error.value = e.message ?: "Sign up failed"
            }
        }
    }

    /**
     * Sign out the current user
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                AppSupabaseClient.client?.auth?.signOut()
            } catch (e: Exception) {
                Log.w(TAG, "Error during sign out (non-fatal)", e)
            }
        }
        _user.value = null
        _is2FARequired.value = false
    }

    /**
     * Skip authentication (guest access)
     */
    fun skipAuth() {
        // Only allow guest access in debug builds to prevent unauthorized feed access
        if (!BuildConfig.DEBUG) {
            _error.value = "Authentication is required"
            return
        }
        _user.value = User(
            id = "guest_user",
            name = "Guest",
            avatarUrl = "",
            isVerified = false,
            personality = "A guest user."
        )
    }

    /**
     * Clear the current error message
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Reset the 2FA state
     */
    fun reset2FAState() {
        _is2FARequired.value = false
        _is2FAEnabled.value = false
    }
}
