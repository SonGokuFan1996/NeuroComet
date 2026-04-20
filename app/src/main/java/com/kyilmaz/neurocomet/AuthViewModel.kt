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
import io.github.jan.supabase.auth.providers.Github
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class AccountLifecycleStatus(
    val id: String = "",
    val is_active: Boolean = true,
    val deletion_scheduled_at: String? = null,
    val detox_started_at: String? = null,
    val detox_until: String? = null,
) {
    val hasDeletionScheduled: Boolean
        get() = !deletion_scheduled_at.isNullOrBlank()

    val isDetoxActive: Boolean
        get() = detox_until?.let {
            runCatching { Instant.parse(it).isAfter(Instant.now()) }.getOrDefault(false)
        } ?: false
}

sealed interface PendingAccountAction {
    data class ScheduledDeletion(val scheduledAt: String?) : PendingAccountAction
    data class DetoxActive(val until: String?) : PendingAccountAction
}

/**
 * AuthViewModel - Manages authentication state and integrates with AuthenticationManager
 */
class AuthViewModel : ViewModel() {

    private val TAG = "AuthViewModel"
    private var authManager: AuthenticationManager? = null
    private var applicationContext: Context? = null

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

    private val _accountStatus = MutableStateFlow<AccountLifecycleStatus?>(null)
    val accountStatus: StateFlow<AccountLifecycleStatus?> = _accountStatus.asStateFlow()

    private val _pendingAccountAction = MutableStateFlow<PendingAccountAction?>(null)
    val pendingAccountAction: StateFlow<PendingAccountAction?> = _pendingAccountAction.asStateFlow()

    // 2FA Logic
    private val _is2FAEnabled = MutableStateFlow(false)
    val is2FAEnabled = _is2FAEnabled.asStateFlow()

    private val _is2FARequired = MutableStateFlow(false)
    val is2FARequired = _is2FARequired.asStateFlow()


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

    // Dev-only: transient status text for the in-app Supabase auth gate in Developer Options.
    private val _devAuthStatus = MutableStateFlow<String?>(null)
    val devAuthStatus: StateFlow<String?> = _devAuthStatus.asStateFlow()

    /**
     * Developer-only helper used by the Supabase auth gate in Developer Options.
     * Attempts to sign in with a fixed dev password; if the user does not exist,
     * signs them up. Status updates are surfaced through [devAuthStatus].
     *
     * Only runs in debug builds and only when a real Supabase client is configured.
     */
    fun devSignInForTesting(email: String) {
        if (!BuildConfig.DEBUG) {
            _devAuthStatus.value = "Dev sign-in is only available in debug builds."
            return
        }
        val trimmed = email.trim()
        if (trimmed.isBlank() || !trimmed.contains("@") || !trimmed.contains(".")) {
            _devAuthStatus.value = "Enter a valid email."
            return
        }
        val client = AppSupabaseClient.client
        if (client == null) {
            _devAuthStatus.value = "Supabase client unavailable in this build."
            return
        }
        val devPassword = "DevPassword!123"
        viewModelScope.launch {
            _devAuthStatus.value = "Signing in\u2026"
            try {
                client.auth.signInWith(Email) {
                    this.email = trimmed
                    this.password = devPassword
                }
                val restored = userFromCurrentSession(trimmed.substringBefore("@"))
                if (restored != null) {
                    _user.value = restored
                    refreshCurrentAccountStatus(restored.id)
                    _devAuthStatus.value = "Signed in as ${restored.id.take(8)}\u2026"
                } else {
                    _devAuthStatus.value = "Signed in (no session user resolved)."
                }
            } catch (signInErr: Exception) {
                Log.w(TAG, "Dev sign-in failed, attempting sign-up", signInErr)
                _devAuthStatus.value = "Sign-in failed, trying sign-up\u2026"
                try {
                    val displayName = trimmed.substringBefore("@")
                    client.auth.signUpWith(Email) {
                        this.email = trimmed
                        this.password = devPassword
                        this.data = kotlinx.serialization.json.buildJsonObject {
                            put("display_name", kotlinx.serialization.json.JsonPrimitive(displayName))
                            put("username", kotlinx.serialization.json.JsonPrimitive("dev_${System.currentTimeMillis() % 100000}"))
                        }
                    }
                    // After sign-up, try sign-in to guarantee a session (works if email confirmation is disabled).
                    try {
                        client.auth.signInWith(Email) {
                            this.email = trimmed
                            this.password = devPassword
                        }
                    } catch (_: Throwable) { /* ignored – confirmation may be required */ }

                    val restored = userFromCurrentSession(displayName)
                    if (restored != null) {
                        _user.value = restored
                        refreshCurrentAccountStatus(restored.id)
                        _devAuthStatus.value = "Signed up as ${restored.id.take(8)}\u2026"
                    } else {
                        _devAuthStatus.value = "Signed up. If confirmation is required, check email."
                    }
                } catch (signUpErr: Exception) {
                    Log.e(TAG, "Dev sign-up failed", signUpErr)
                    _devAuthStatus.value = "Auth failed: ${signUpErr.message ?: signInErr.message ?: "unknown error"}"
                }
            }
        }
    }

    /**
     * Signs in using GitHub OAuth. Ideal for developers who linked their Supabase project
     * with their GitHub accounts.
     */
    fun devSignInWithGithub() {
        viewModelScope.launch {
            val client = AppSupabaseClient.client
            if (client == null) {
                _devAuthStatus.value = "Supabase client not configured"
                return@launch
            }
            
            try {
                _devAuthStatus.value = "Opening GitHub sign in…"
                client.auth.signInWith(Github)
                _devAuthStatus.value = "Returning from GitHub sign in flow…"
            } catch (e: Exception) {
                Log.e(TAG, "Dev GitHub sign in failed", e)
                _devAuthStatus.value = "GitHub sign in failed: ${e.message}"
            }
        }
    }

    /**
     * Initialize the AuthenticationManager with context
     */
    fun initialize(context: Context) {
        applicationContext = context.applicationContext
        authManager = AuthenticationManager.getInstance(context)
        refreshAuthState()
        viewModelScope.launch {
            restoreUserFromCurrentSession()
        }
    }

    private fun userFromCurrentSession(fallbackName: String? = null): User? {
        val session = AppSupabaseClient.client?.auth?.currentSessionOrNull() ?: return null
        val userId = session.user?.id ?: return null
        val meta = session.user?.userMetadata
        val displayName = meta?.get("display_name")?.toString()?.removeSurrounding("\"")
            ?: fallbackName
            ?: session.user?.email?.substringBefore("@")
            ?: "NeuroComet user"
        val avatar = meta?.get("avatar_url")?.toString()?.removeSurrounding("\"") ?: ""
        return User(
            id = userId,
            name = displayName,
            avatarUrl = avatar,
            isVerified = session.user?.emailConfirmedAt != null,
            personality = "NeuroComet user"
        )
    }

    private suspend fun restoreUserFromCurrentSession() {
        val restoredUser = userFromCurrentSession() ?: return
        _user.value = restoredUser
        refreshCurrentAccountStatus(restoredUser.id)
    }

    private fun publishPendingAccountAction(status: AccountLifecycleStatus?) {
        _pendingAccountAction.value = when {
            status?.hasDeletionScheduled == true -> PendingAccountAction.ScheduledDeletion(status.deletion_scheduled_at)
            status?.isDetoxActive == true -> PendingAccountAction.DetoxActive(status.detox_until)
            else -> null
        }
    }

    suspend fun refreshCurrentAccountStatus(userId: String? = null): AccountLifecycleStatus? {
        val resolvedUserId = userId ?: _user.value?.id ?: return null
        val client = AppSupabaseClient.client ?: return null
        return try {
            var status = client.from("users")
                .select(columns = Columns.list("id", "is_active", "deletion_scheduled_at", "detox_started_at", "detox_until")) {
                    filter { eq("id", resolvedUserId) }
                    limit(1)
                }
                .decodeList<AccountLifecycleStatus>()
                .firstOrNull()

            if (status?.detox_until != null && !status.isDetoxActive) {
                client.from("users").update({
                    set("detox_started_at", null as String?)
                    set("detox_until", null as String?)
                    set("updated_at", Instant.now().toString())
                }) {
                    filter { eq("id", resolvedUserId) }
                }
                applicationContext?.also { ctx ->
                    SocialSettingsManager.restoreDetoxDefaults(context = ctx)
                }
                status = status.copy(detox_started_at = null, detox_until = null)
            }

            _accountStatus.value = status
            publishPendingAccountAction(status)
            status
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh account status", e)
            null
        }
    }

    fun scheduleAccountDeletion(onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val client = AppSupabaseClient.client
            val userId = _user.value?.id
            if (client == null || userId == null) {
                onResult(false, "No user logged in")
                return@launch
            }

            try {
                client.from("users").update({
                    set("deletion_scheduled_at", Instant.now().plusSeconds(14 * 24 * 60 * 60L).toString())
                    set("detox_started_at", null as String?)
                    set("detox_until", null as String?)
                    set("is_active", false)
                    set("updated_at", Instant.now().toString())
                }) {
                    filter { eq("id", userId) }
                }
                applicationContext?.also { ctx ->
                    SocialSettingsManager.restoreDetoxDefaults(context = ctx)
                }
                refreshCurrentAccountStatus(userId)
                signOut()
                onResult(true, "Account scheduled for deletion in 14 days. Sign in again to cancel.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to schedule account deletion", e)
                onResult(false, e.message ?: "Failed to schedule account deletion")
            }
        }
    }

    fun cancelScheduledDeletion(onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val client = AppSupabaseClient.client
            val userId = _user.value?.id
            if (client == null || userId == null) {
                onResult(false, "No user logged in")
                return@launch
            }

            try {
                client.from("users").update({
                    set("deletion_scheduled_at", null as String?)
                    set("is_active", true)
                    set("updated_at", Instant.now().toString())
                }) {
                    filter { eq("id", userId) }
                }
                refreshCurrentAccountStatus(userId)
                onResult(true, "Account deletion cancelled.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to cancel account deletion", e)
                onResult(false, e.message ?: "Failed to cancel account deletion")
            }
        }
    }

    fun startDetoxMode(days: Int, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val client = AppSupabaseClient.client
            val userId = _user.value?.id
            if (client == null || userId == null) {
                onResult(false, "No user logged in")
                return@launch
            }

            try {
                val now = Instant.now()
                val until = now.plusSeconds(days.coerceAtLeast(1).toLong() * 24 * 60 * 60)
                client.from("users").update({
                    set("detox_started_at", now.toString())
                    set("detox_until", until.toString())
                    set("updated_at", now.toString())
                }) {
                    filter { eq("id", userId) }
                }
                applicationContext?.also { ctx ->
                    SocialSettingsManager.applyDetoxDefaults(context = ctx)
                }
                refreshCurrentAccountStatus(userId)
                signOut()
                onResult(true, "Detox mode started until $until.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start detox mode", e)
                onResult(false, e.message ?: "Failed to start detox mode")
            }
        }
    }

    fun endDetoxMode(onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val client = AppSupabaseClient.client
            val userId = _user.value?.id
            if (client == null || userId == null) {
                onResult(false, "No user logged in")
                return@launch
            }

            try {
                client.from("users").update({
                    set("detox_started_at", null as String?)
                    set("detox_until", null as String?)
                    set("updated_at", Instant.now().toString())
                }) {
                    filter { eq("id", userId) }
                }
                applicationContext?.also { ctx ->
                    SocialSettingsManager.restoreDetoxDefaults(context = ctx)
                }
                refreshCurrentAccountStatus(userId)
                onResult(true, "Detox mode ended. Welcome back.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to end detox mode", e)
                onResult(false, e.message ?: "Failed to end detox mode")
            }
        }
    }

    fun keepScheduledDeletion() {
        _error.value = "Your account is still scheduled for deletion. Sign in again to cancel it."
        signOut()
    }

    fun continueDetoxBreak() {
        _error.value = "Detox mode is still active. Sign in again when you're ready to come back."
        signOut()
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
                    val restoredUser = userFromCurrentSession(trimmedEmail.substringBefore("@"))
                    if (restoredUser != null) {
                        _user.value = restoredUser
                        refreshCurrentAccountStatus(restoredUser.id)
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
                    val restoredUser = userFromCurrentSession(displayName)
                    if (restoredUser != null) {
                        _user.value = restoredUser
                        refreshCurrentAccountStatus(restoredUser.id)
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
        // Clear persisted audience so next sign-in triggers age verification
        applicationContext?.let { AudiencePrefs.clear(it) }
        _user.value = null
        _accountStatus.value = null
        _pendingAccountAction.value = null
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
