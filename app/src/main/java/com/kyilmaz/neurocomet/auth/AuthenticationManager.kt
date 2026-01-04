package com.kyilmaz.neurocomet.auth

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.floor

/**
 * Authentication Method types supported by the app
 */
enum class AuthMethod {
    BIOMETRIC,      // Fingerprint, Face, etc.
    FIDO2,          // Hardware security keys / Passkeys
    TOTP,           // Time-based One-Time Password (Authenticator apps)
    EMAIL,          // Email verification codes
    SMS,            // SMS verification codes (future)
    BACKUP_CODES    // One-time backup codes
}

/**
 * Result of an authentication attempt
 */
sealed class AuthResult {
    data object Success : AuthResult()
    data class Error(val message: String, val code: Int = 0) : AuthResult()
    data object Cancelled : AuthResult()
    data object NotAvailable : AuthResult()
    data object RequiresSetup : AuthResult()
}

/**
 * Biometric availability status
 */
sealed class BiometricStatus {
    data object Available : BiometricStatus()
    data object NotEnrolled : BiometricStatus()
    data object NoHardware : BiometricStatus()
    data object SecurityUpdateRequired : BiometricStatus()
    data object Unsupported : BiometricStatus()
}

/**
 * FIDO2 Credential data class
 */
data class Fido2Credential(
    val credentialId: String,
    val publicKey: String,
    val userId: String,
    val displayName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsed: Long = System.currentTimeMillis()
)

/**
 * TOTP Secret data class
 */
data class TotpSecret(
    val secret: String,
    val issuer: String = "NeuroComet",
    val accountName: String,
    val algorithm: String = "SHA1",
    val digits: Int = 6,
    val period: Int = 30
) {
    /**
     * Generate TOTP URI for QR code
     */
    fun toUri(): String {
        return "otpauth://totp/$issuer:$accountName?secret=$secret&issuer=$issuer&algorithm=$algorithm&digits=$digits&period=$period"
    }
}

/**
 * AuthenticationManager - Handles all authentication methods
 */
class AuthenticationManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    // State flows for UI observation
    private val _biometricEnabled = MutableStateFlow(prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false))
    val biometricEnabled: StateFlow<Boolean> = _biometricEnabled.asStateFlow()

    private val _fido2Enabled = MutableStateFlow(prefs.getBoolean(KEY_FIDO2_ENABLED, false))
    val fido2Enabled: StateFlow<Boolean> = _fido2Enabled.asStateFlow()

    private val _totpEnabled = MutableStateFlow(prefs.getBoolean(KEY_TOTP_ENABLED, false))
    val totpEnabled: StateFlow<Boolean> = _totpEnabled.asStateFlow()

    private val _emailVerificationEnabled = MutableStateFlow(prefs.getBoolean(KEY_EMAIL_ENABLED, false))
    val emailVerificationEnabled: StateFlow<Boolean> = _emailVerificationEnabled.asStateFlow()

    private val _backupCodesRemaining = MutableStateFlow(getBackupCodes().size)
    val backupCodesRemaining: StateFlow<Int> = _backupCodesRemaining.asStateFlow()

    // ==================== BIOMETRIC AUTHENTICATION ====================

    /**
     * Check if biometric authentication is available on this device
     */
    fun checkBiometricStatus(): BiometricStatus {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.Available
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NoHardware
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.NoHardware
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NotEnrolled
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricStatus.SecurityUpdateRequired
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricStatus.Unsupported
            else -> BiometricStatus.Unsupported
        }
    }

    /**
     * Show biometric prompt for authentication
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "Authenticate",
        subtitle: String = "Use your fingerprint or face to continue",
        negativeButtonText: String = "Cancel",
        onResult: (AuthResult) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(context)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onResult(AuthResult.Success)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                when (errorCode) {
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_CANCELED -> onResult(AuthResult.Cancelled)
                    BiometricPrompt.ERROR_NO_BIOMETRICS -> onResult(AuthResult.RequiresSetup)
                    else -> onResult(AuthResult.Error(errString.toString(), errorCode))
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Don't call onResult here - the system will keep trying
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Enable/disable biometric authentication
     */
    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
        _biometricEnabled.value = enabled
    }

    // ==================== FIDO2/PASSKEY AUTHENTICATION ====================

    /**
     * Check if FIDO2/Passkeys are supported
     */
    fun isFido2Supported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    }

    /**
     * Get stored FIDO2 credentials
     */
    fun getFido2Credentials(): List<Fido2Credential> {
        val credentialsJson = prefs.getString(KEY_FIDO2_CREDENTIALS, null) ?: return emptyList()
        return try {
            // Parse stored credentials (simplified - in production use proper JSON parsing)
            credentialsJson.split("|").filter { it.isNotEmpty() }.map { entry ->
                val parts = entry.split(",")
                Fido2Credential(
                    credentialId = parts.getOrElse(0) { "" },
                    publicKey = parts.getOrElse(1) { "" },
                    userId = parts.getOrElse(2) { "" },
                    displayName = parts.getOrElse(3) { "Security Key" },
                    createdAt = parts.getOrElse(4) { "0" }.toLongOrNull() ?: 0L,
                    lastUsed = parts.getOrElse(5) { "0" }.toLongOrNull() ?: 0L
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Register a new FIDO2 credential (simulated for demo)
     * In production, this would use the Credentials API
     */
    fun registerFido2Credential(displayName: String, onResult: (AuthResult) -> Unit) {
        // Generate a mock credential ID and public key
        val random = SecureRandom()
        val credentialIdBytes = ByteArray(32)
        random.nextBytes(credentialIdBytes)
        val credentialId = Base64.encodeToString(credentialIdBytes, Base64.NO_WRAP)

        val publicKeyBytes = ByteArray(65) // Simulated P-256 public key
        random.nextBytes(publicKeyBytes)
        val publicKey = Base64.encodeToString(publicKeyBytes, Base64.NO_WRAP)

        val userId = prefs.getString(KEY_USER_ID, "user_${System.currentTimeMillis()}") ?: "user"

        val credential = Fido2Credential(
            credentialId = credentialId,
            publicKey = publicKey,
            userId = userId,
            displayName = displayName
        )

        // Store the credential
        val existingCredentials = getFido2Credentials().toMutableList()
        existingCredentials.add(credential)

        val credentialsString = existingCredentials.joinToString("|") { cred ->
            "${cred.credentialId},${cred.publicKey},${cred.userId},${cred.displayName},${cred.createdAt},${cred.lastUsed}"
        }

        prefs.edit()
            .putString(KEY_FIDO2_CREDENTIALS, credentialsString)
            .putBoolean(KEY_FIDO2_ENABLED, true)
            .apply()

        _fido2Enabled.value = true
        onResult(AuthResult.Success)
    }

    /**
     * Authenticate with FIDO2 (simulated for demo)
     */
    fun authenticateWithFido2(
        activity: FragmentActivity,
        onResult: (AuthResult) -> Unit
    ) {
        val credentials = getFido2Credentials()
        if (credentials.isEmpty()) {
            onResult(AuthResult.RequiresSetup)
            return
        }

        // In production, this would use the Credentials API to authenticate
        // For demo, we'll use biometric as a proxy for hardware key
        showBiometricPrompt(
            activity = activity,
            title = "Security Key",
            subtitle = "Touch your security key or use your device passkey",
            negativeButtonText = "Cancel"
        ) { result ->
            if (result is AuthResult.Success) {
                // Update last used timestamp
                val updatedCredentials = credentials.map { cred ->
                    cred.copy(lastUsed = System.currentTimeMillis())
                }
                val credentialsString = updatedCredentials.joinToString("|") { cred ->
                    "${cred.credentialId},${cred.publicKey},${cred.userId},${cred.displayName},${cred.createdAt},${cred.lastUsed}"
                }
                prefs.edit().putString(KEY_FIDO2_CREDENTIALS, credentialsString).apply()
            }
            onResult(result)
        }
    }

    /**
     * Remove a FIDO2 credential
     */
    fun removeFido2Credential(credentialId: String) {
        val credentials = getFido2Credentials().filter { it.credentialId != credentialId }
        val credentialsString = credentials.joinToString("|") { cred ->
            "${cred.credentialId},${cred.publicKey},${cred.userId},${cred.displayName},${cred.createdAt},${cred.lastUsed}"
        }
        prefs.edit()
            .putString(KEY_FIDO2_CREDENTIALS, credentialsString)
            .putBoolean(KEY_FIDO2_ENABLED, credentials.isNotEmpty())
            .apply()
        _fido2Enabled.value = credentials.isNotEmpty()
    }

    // ==================== TOTP AUTHENTICATION ====================

    /**
     * Generate a new TOTP secret
     */
    fun generateTotpSecret(accountName: String): TotpSecret {
        val random = SecureRandom()
        val secretBytes = ByteArray(20)
        random.nextBytes(secretBytes)
        val secret = base32Encode(secretBytes)

        return TotpSecret(
            secret = secret,
            accountName = accountName
        )
    }

    /**
     * Store TOTP secret
     */
    fun storeTotpSecret(secret: TotpSecret) {
        prefs.edit()
            .putString(KEY_TOTP_SECRET, secret.secret)
            .putString(KEY_TOTP_ACCOUNT, secret.accountName)
            .putBoolean(KEY_TOTP_ENABLED, true)
            .apply()
        _totpEnabled.value = true
    }

    /**
     * Get stored TOTP secret
     */
    fun getTotpSecret(): TotpSecret? {
        val secret = prefs.getString(KEY_TOTP_SECRET, null) ?: return null
        val account = prefs.getString(KEY_TOTP_ACCOUNT, "user") ?: "user"
        return TotpSecret(secret = secret, accountName = account)
    }

    /**
     * Generate current TOTP code
     */
    fun generateTotpCode(): String? {
        val secret = getTotpSecret() ?: return null
        return generateTotp(secret.secret, secret.period, secret.digits)
    }

    /**
     * Verify TOTP code
     */
    fun verifyTotpCode(code: String, window: Int = 1): Boolean {
        val secret = getTotpSecret() ?: return false
        val currentTime = System.currentTimeMillis() / 1000

        // Check current period and adjacent periods (to handle clock drift)
        for (i in -window..window) {
            val period = (currentTime / secret.period) + i
            val expectedCode = generateTotpForPeriod(secret.secret, period, secret.digits)
            if (code == expectedCode) {
                return true
            }
        }
        return false
    }

    /**
     * Disable TOTP
     */
    fun disableTotp() {
        prefs.edit()
            .remove(KEY_TOTP_SECRET)
            .remove(KEY_TOTP_ACCOUNT)
            .putBoolean(KEY_TOTP_ENABLED, false)
            .apply()
        _totpEnabled.value = false
    }

    // ==================== EMAIL VERIFICATION ====================

    private var pendingEmailCode: String? = null
    private var pendingEmailExpiry: Long = 0

    /**
     * Send email verification code (simulated)
     */
    fun sendEmailVerificationCode(email: String): String {
        val random = SecureRandom()
        val code = String.format("%06d", random.nextInt(1000000))
        pendingEmailCode = code
        pendingEmailExpiry = System.currentTimeMillis() + (5 * 60 * 1000) // 5 minutes

        // In production, this would actually send an email
        return code
    }

    /**
     * Verify email code
     */
    fun verifyEmailCode(code: String): AuthResult {
        if (pendingEmailCode == null) {
            return AuthResult.Error("No verification code pending")
        }
        if (System.currentTimeMillis() > pendingEmailExpiry) {
            pendingEmailCode = null
            return AuthResult.Error("Verification code expired")
        }
        if (code != pendingEmailCode) {
            return AuthResult.Error("Invalid verification code")
        }

        pendingEmailCode = null
        prefs.edit().putBoolean(KEY_EMAIL_ENABLED, true).apply()
        _emailVerificationEnabled.value = true
        return AuthResult.Success
    }

    // ==================== BACKUP CODES ====================

    /**
     * Generate backup codes
     */
    fun generateBackupCodes(count: Int = 10): List<String> {
        val random = SecureRandom()
        val codes = (1..count).map {
            val bytes = ByteArray(4)
            random.nextBytes(bytes)
            bytes.joinToString("") { byte ->
                String.format("%02x", byte.toInt() and 0xff)
            }.uppercase()
        }

        // Hash and store the codes
        val hashedCodes = codes.map { hashCode(it) }
        prefs.edit()
            .putStringSet(KEY_BACKUP_CODES, hashedCodes.toSet())
            .apply()
        _backupCodesRemaining.value = codes.size

        return codes
    }

    /**
     * Get backup codes count
     */
    fun getBackupCodes(): Set<String> {
        return prefs.getStringSet(KEY_BACKUP_CODES, emptySet()) ?: emptySet()
    }

    /**
     * Verify and consume a backup code
     */
    fun verifyBackupCode(code: String): AuthResult {
        val storedCodes = getBackupCodes().toMutableSet()
        val hashedInput = hashCode(code.uppercase().replace("-", "").replace(" ", ""))

        if (storedCodes.contains(hashedInput)) {
            storedCodes.remove(hashedInput)
            prefs.edit().putStringSet(KEY_BACKUP_CODES, storedCodes).apply()
            _backupCodesRemaining.value = storedCodes.size
            return AuthResult.Success
        }
        return AuthResult.Error("Invalid backup code")
    }

    // ==================== HELPER FUNCTIONS ====================

    private fun generateTotp(secret: String, period: Int, digits: Int): String {
        val time = System.currentTimeMillis() / 1000
        val counter = time / period
        return generateTotpForPeriod(secret, counter, digits)
    }

    private fun generateTotpForPeriod(secret: String, counter: Long, digits: Int): String {
        try {
            val secretBytes = base32Decode(secret)
            val timeBytes = ByteArray(8)
            var temp = counter
            for (i in 7 downTo 0) {
                timeBytes[i] = (temp and 0xff).toByte()
                temp = temp shr 8
            }

            val mac = Mac.getInstance("HmacSHA1")
            mac.init(SecretKeySpec(secretBytes, "HmacSHA1"))
            val hash = mac.doFinal(timeBytes)

            val offset = hash[hash.size - 1].toInt() and 0x0f
            val truncatedHash = ((hash[offset].toInt() and 0x7f) shl 24) or
                    ((hash[offset + 1].toInt() and 0xff) shl 16) or
                    ((hash[offset + 2].toInt() and 0xff) shl 8) or
                    (hash[offset + 3].toInt() and 0xff)

            val otp = truncatedHash % Math.pow(10.0, digits.toDouble()).toInt()
            return String.format("%0${digits}d", otp)
        } catch (e: Exception) {
            return "000000"
        }
    }

    private fun base32Encode(data: ByteArray): String {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val result = StringBuilder()
        var buffer = 0
        var bitsLeft = 0

        for (byte in data) {
            buffer = (buffer shl 8) or (byte.toInt() and 0xff)
            bitsLeft += 8
            while (bitsLeft >= 5) {
                result.append(alphabet[(buffer shr (bitsLeft - 5)) and 0x1f])
                bitsLeft -= 5
            }
        }

        if (bitsLeft > 0) {
            result.append(alphabet[(buffer shl (5 - bitsLeft)) and 0x1f])
        }

        return result.toString()
    }

    private fun base32Decode(encoded: String): ByteArray {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val result = mutableListOf<Byte>()
        var buffer = 0
        var bitsLeft = 0

        for (char in encoded.uppercase()) {
            if (char == '=') continue
            val value = alphabet.indexOf(char)
            if (value < 0) continue

            buffer = (buffer shl 5) or value
            bitsLeft += 5

            if (bitsLeft >= 8) {
                result.add(((buffer shr (bitsLeft - 8)) and 0xff).toByte())
                bitsLeft -= 8
            }
        }

        return result.toByteArray()
    }

    private fun hashCode(code: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(code.toByteArray())
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    companion object {
        private const val PREFS_NAME = "neurocomet_auth"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_FIDO2_ENABLED = "fido2_enabled"
        private const val KEY_FIDO2_CREDENTIALS = "fido2_credentials"
        private const val KEY_TOTP_ENABLED = "totp_enabled"
        private const val KEY_TOTP_SECRET = "totp_secret"
        private const val KEY_TOTP_ACCOUNT = "totp_account"
        private const val KEY_EMAIL_ENABLED = "email_enabled"
        private const val KEY_BACKUP_CODES = "backup_codes"
        private const val KEY_USER_ID = "user_id"

        @Volatile
        private var instance: AuthenticationManager? = null

        fun getInstance(context: Context): AuthenticationManager {
            return instance ?: synchronized(this) {
                instance ?: AuthenticationManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

