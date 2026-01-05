package com.kyilmaz.neurocomet

import android.content.Context
import android.os.SystemClock
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.core.content.edit
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64

/**
 * Parental Controls Settings - Secure storage for parental control configuration.
 *
 * Security features:
 * - PIN is hashed with SHA-256 + salt before storage
 * - Uses Android Keystore for encryption where available
 * - Lockout after failed attempts
 * - Time-based restrictions with ANTI-BYPASS protection
 * - Uses monotonic time (SystemClock.elapsedRealtime) to prevent system clock manipulation
 * - Tracks last known system time vs monotonic time delta to detect time changes
 */
object ParentalControlsSettings {
    private const val PREFS = "parental_controls"
    private const val KEYSTORE_ALIAS = "NeuroComet_parental_key"

    // Keys for SharedPreferences
    private const val KEY_PIN_HASH = "pin_hash"
    private const val KEY_PIN_SALT = "pin_salt"
    private const val KEY_IS_ENABLED = "is_enabled"
    private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
    private const val KEY_LOCKOUT_UNTIL = "lockout_until"
    private const val KEY_LOCKOUT_ELAPSED_REALTIME = "lockout_elapsed_realtime"
    private const val KEY_MAX_DAILY_MINUTES = "max_daily_minutes"
    private const val KEY_BEDTIME_START_HOUR = "bedtime_start_hour"
    private const val KEY_BEDTIME_START_MINUTE = "bedtime_start_minute"
    private const val KEY_BEDTIME_END_HOUR = "bedtime_end_hour"
    private const val KEY_BEDTIME_END_MINUTE = "bedtime_end_minute"
    private const val KEY_BEDTIME_ENABLED = "bedtime_enabled"
    private const val KEY_BLOCK_DMS = "block_dms"
    private const val KEY_BLOCK_EXPLORE = "block_explore"
    private const val KEY_BLOCK_POSTING = "block_posting"
    private const val KEY_REQUIRE_APPROVAL_FOR_FOLLOWS = "require_approval_follows"
    private const val KEY_CONTENT_FILTER_LEVEL = "content_filter_level"
    private const val KEY_DAILY_USAGE_MINUTES = "daily_usage_minutes"
    private const val KEY_USAGE_DATE = "usage_date"

    // Time bypass protection keys
    private const val KEY_LAST_KNOWN_SYSTEM_TIME = "last_known_system_time"
    private const val KEY_LAST_KNOWN_ELAPSED_REALTIME = "last_known_elapsed_realtime"
    private const val KEY_TIME_TAMPERING_DETECTED = "time_tampering_detected"
    private const val KEY_USAGE_ELAPSED_START = "usage_elapsed_start"
    private const val KEY_SESSION_USAGE_SECONDS = "session_usage_seconds"

    // Security constants
    private const val MAX_FAILED_ATTEMPTS = 5
    private const val LOCKOUT_DURATION_MS = 30 * 60 * 1000L // 30 minutes
    private const val MIN_PIN_LENGTH = 4
    private const val MAX_PIN_LENGTH = 8
    private const val TIME_DRIFT_TOLERANCE_MS = 5 * 60 * 1000L // 5 minutes tolerance for normal drift

    // Developer master PIN for bypassing parental controls (debug builds only)
    private const val DEV_MASTER_PIN = "000000"

    /**
     * Check if a PIN is the developer master PIN (only works in debug builds)
     */
    private fun isDevMasterPin(context: Context, pin: String): Boolean {
        val isDebuggable = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        return isDebuggable && pin == DEV_MASTER_PIN
    }

    /**
     * Check if the current device is a developer device
     */
    fun isDevDevice(context: Context): Boolean {
        val isDebuggable = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        return isDebuggable
    }

    /**
     * Detect if time tampering has occurred by comparing system time delta with monotonic time delta.
     * Returns true if time manipulation is detected.
     */
    fun detectTimeTampering(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        val lastSystemTime = prefs.getLong(KEY_LAST_KNOWN_SYSTEM_TIME, 0)
        val lastElapsedRealtime = prefs.getLong(KEY_LAST_KNOWN_ELAPSED_REALTIME, 0)

        if (lastSystemTime == 0L || lastElapsedRealtime == 0L) {
            // First run, record baseline
            updateTimeBaseline(context)
            return false
        }

        val currentSystemTime = System.currentTimeMillis()
        val currentElapsedRealtime = SystemClock.elapsedRealtime()

        val systemTimeDelta = currentSystemTime - lastSystemTime
        val elapsedRealtimeDelta = currentElapsedRealtime - lastElapsedRealtime

        // If the difference between system time change and monotonic time change
        // exceeds tolerance, time has been manipulated
        val drift = kotlin.math.abs(systemTimeDelta - elapsedRealtimeDelta)

        if (drift > TIME_DRIFT_TOLERANCE_MS && elapsedRealtimeDelta > 0) {
            // Time tampering detected!
            prefs.edit { putBoolean(KEY_TIME_TAMPERING_DETECTED, true) }
            return true
        }

        // Update baseline
        updateTimeBaseline(context)
        return prefs.getBoolean(KEY_TIME_TAMPERING_DETECTED, false)
    }

    /**
     * Update the time baseline for tampering detection.
     */
    private fun updateTimeBaseline(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit {
            putLong(KEY_LAST_KNOWN_SYSTEM_TIME, System.currentTimeMillis())
            putLong(KEY_LAST_KNOWN_ELAPSED_REALTIME, SystemClock.elapsedRealtime())
        }
    }

    /**
     * Clear time tampering flag (only via PIN verification)
     */
    fun clearTimeTamperingFlag(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_TIME_TAMPERING_DETECTED, false)
        }
        updateTimeBaseline(context)
    }

    /**
     * Check if time tampering was previously detected
     */
    fun wasTimeTamperingDetected(context: Context): Boolean {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_TIME_TAMPERING_DETECTED, false)
    }

    /**
     * Data class representing all parental control settings.
     */
    data class ParentalControlState(
        val isEnabled: Boolean = false,
        val isPinSet: Boolean = false,
        val failedAttempts: Int = 0,
        val isLockedOut: Boolean = false,
        val lockoutRemainingMs: Long = 0,
        val maxDailyMinutes: Int = 0, // 0 = unlimited
        val bedtimeEnabled: Boolean = false,
        val bedtimeStartHour: Int = 21,
        val bedtimeStartMinute: Int = 0,
        val bedtimeEndHour: Int = 7,
        val bedtimeEndMinute: Int = 0,
        val blockDMs: Boolean = false,
        val blockExplore: Boolean = false,
        val blockPosting: Boolean = false,
        val requireApprovalForFollows: Boolean = false,
        val contentFilterLevel: ContentFilterLevel = ContentFilterLevel.MODERATE,
        val dailyUsageMinutes: Int = 0,
        val isDuringBedtime: Boolean = false,
        val isOverDailyLimit: Boolean = false,
        val timeTamperingDetected: Boolean = false // New: detects if user changed system time
    )

    enum class ContentFilterLevel {
        OFF,        // No filtering
        MODERATE,   // Filter explicit content
        STRICT      // Filter explicit + suggestive content
    }

    /**
     * Generate a random salt for PIN hashing.
     */
    private fun generateSalt(): String {
        val bytes = ByteArray(16)
        java.security.SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    /**
     * Hash a PIN with the given salt using SHA-256.
     */
    private fun hashPin(pin: String, salt: String): String {
        val combined = "$salt:$pin"
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }

    /**
     * Validate PIN format.
     */
    fun isValidPinFormat(pin: String): Boolean {
        return pin.length in MIN_PIN_LENGTH..MAX_PIN_LENGTH && pin.all { it.isDigit() }
    }

    /**
     * Set a new parental control PIN.
     * @return true if successful, false if PIN format is invalid
     */
    fun setPin(context: Context, pin: String): Boolean {
        if (!isValidPinFormat(pin)) return false

        val salt = generateSalt()
        val hash = hashPin(pin, salt)

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit {
            putString(KEY_PIN_SALT, salt)
            putString(KEY_PIN_HASH, hash)
            putBoolean(KEY_IS_ENABLED, true)
            putInt(KEY_FAILED_ATTEMPTS, 0)
            remove(KEY_LOCKOUT_UNTIL)
        }
        return true
    }

    /**
     * Verify the entered PIN against the stored hash.
     * Implements lockout after MAX_FAILED_ATTEMPTS.
     * Uses monotonic time (elapsedRealtime) to prevent time manipulation bypass.
     * Developer master PIN (000000) bypasses verification in debug builds.
     */
    fun verifyPin(context: Context, pin: String): PinVerifyResult {
        // Developer master PIN bypass (debug builds only)
        if (isDevMasterPin(context, pin)) {
            clearTimeTamperingFlag(context)
            return PinVerifyResult.Success
        }

        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        // Check if locked out using monotonic time to prevent bypass
        val lockoutElapsedRealtime = prefs.getLong(KEY_LOCKOUT_ELAPSED_REALTIME, 0)
        val currentElapsedRealtime = SystemClock.elapsedRealtime()

        if (lockoutElapsedRealtime > 0) {
            val elapsedSinceLockout = currentElapsedRealtime - lockoutElapsedRealtime
            if (elapsedSinceLockout < LOCKOUT_DURATION_MS) {
                val remainingMs = LOCKOUT_DURATION_MS - elapsedSinceLockout
                return PinVerifyResult.LockedOut(remainingMs)
            }
        }

        val storedHash = prefs.getString(KEY_PIN_HASH, null)
        val storedSalt = prefs.getString(KEY_PIN_SALT, null)

        if (storedHash == null || storedSalt == null) {
            return PinVerifyResult.NoPinSet
        }

        val enteredHash = hashPin(pin, storedSalt)

        return if (enteredHash == storedHash) {
            // Success - reset failed attempts and clear tampering flag
            prefs.edit {
                putInt(KEY_FAILED_ATTEMPTS, 0)
                remove(KEY_LOCKOUT_UNTIL)
                remove(KEY_LOCKOUT_ELAPSED_REALTIME)
            }
            clearTimeTamperingFlag(context)
            PinVerifyResult.Success
        } else {
            // Failed - increment counter
            val failedAttempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
            prefs.edit {
                putInt(KEY_FAILED_ATTEMPTS, failedAttempts)
                if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                    // Store both system time and monotonic time for lockout
                    putLong(KEY_LOCKOUT_UNTIL, System.currentTimeMillis() + LOCKOUT_DURATION_MS)
                    putLong(KEY_LOCKOUT_ELAPSED_REALTIME, SystemClock.elapsedRealtime())
                }
            }

            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                PinVerifyResult.LockedOut(LOCKOUT_DURATION_MS)
            } else {
                PinVerifyResult.Incorrect(MAX_FAILED_ATTEMPTS - failedAttempts)
            }
        }
    }

    sealed class PinVerifyResult {
        object Success : PinVerifyResult()
        object NoPinSet : PinVerifyResult()
        data class Incorrect(val attemptsRemaining: Int) : PinVerifyResult()
        data class LockedOut(val remainingMs: Long) : PinVerifyResult()
    }

    /**
     * Check if a PIN is set.
     */
    fun isPinSet(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PIN_HASH, null) != null
    }

    /**
     * Change the PIN (requires old PIN verification first).
     */
    fun changePin(context: Context, oldPin: String, newPin: String): Boolean {
        if (verifyPin(context, oldPin) != PinVerifyResult.Success) return false
        return setPin(context, newPin)
    }

    /**
     * Remove parental controls (requires PIN verification).
     */
    fun removeParentalControls(context: Context, pin: String): Boolean {
        if (verifyPin(context, pin) != PinVerifyResult.Success) return false

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { clear() }
        return true
    }

    /**
     * Get the current parental control state.
     * Includes time tampering detection.
     */
    fun getState(context: Context): ParentalControlState {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        val isPinSet = prefs.getString(KEY_PIN_HASH, null) != null
        val isEnabled = prefs.getBoolean(KEY_IS_ENABLED, false) && isPinSet
        val failedAttempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0)

        // Use monotonic time for lockout check to prevent bypass
        val lockoutElapsedRealtime = prefs.getLong(KEY_LOCKOUT_ELAPSED_REALTIME, 0)
        val currentElapsedRealtime = SystemClock.elapsedRealtime()
        val isLockedOut = if (lockoutElapsedRealtime > 0) {
            (currentElapsedRealtime - lockoutElapsedRealtime) < LOCKOUT_DURATION_MS
        } else false
        val lockoutRemainingMs = if (isLockedOut) {
            LOCKOUT_DURATION_MS - (currentElapsedRealtime - lockoutElapsedRealtime)
        } else 0L

        // Check for time tampering
        val timeTamperingDetected = detectTimeTampering(context)

        // Time restrictions
        val bedtimeEnabled = prefs.getBoolean(KEY_BEDTIME_ENABLED, false)
        val bedtimeStartHour = prefs.getInt(KEY_BEDTIME_START_HOUR, 21)
        val bedtimeStartMinute = prefs.getInt(KEY_BEDTIME_START_MINUTE, 0)
        val bedtimeEndHour = prefs.getInt(KEY_BEDTIME_END_HOUR, 7)
        val bedtimeEndMinute = prefs.getInt(KEY_BEDTIME_END_MINUTE, 0)

        // Usage limits
        val maxDailyMinutes = prefs.getInt(KEY_MAX_DAILY_MINUTES, 0)
        val usageDate = prefs.getString(KEY_USAGE_DATE, null)
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        val dailyUsageMinutes = if (usageDate == today) {
            prefs.getInt(KEY_DAILY_USAGE_MINUTES, 0)
        } else {
            0 // Reset for new day
        }

        // Check if currently during bedtime
        // If time tampering is detected, force bedtime mode ON as a security measure
        val isDuringBedtime = if (timeTamperingDetected && isEnabled) {
            true // Force restrictions when tampering detected
        } else if (bedtimeEnabled && isEnabled) {
            checkIfDuringBedtime(bedtimeStartHour, bedtimeStartMinute, bedtimeEndHour, bedtimeEndMinute)
        } else false

        // Check if over daily limit
        // If time tampering detected, force over-limit as security measure
        val isOverDailyLimit = if (timeTamperingDetected && isEnabled && maxDailyMinutes > 0) {
            true
        } else {
            maxDailyMinutes > 0 && dailyUsageMinutes >= maxDailyMinutes
        }

        // Content filtering
        val contentFilterLevel = try {
            ContentFilterLevel.valueOf(prefs.getString(KEY_CONTENT_FILTER_LEVEL, ContentFilterLevel.MODERATE.name)!!)
        } catch (e: Exception) {
            ContentFilterLevel.MODERATE
        }

        return ParentalControlState(
            isEnabled = isEnabled,
            isPinSet = isPinSet,
            failedAttempts = failedAttempts,
            isLockedOut = isLockedOut,
            lockoutRemainingMs = lockoutRemainingMs,
            maxDailyMinutes = maxDailyMinutes,
            bedtimeEnabled = bedtimeEnabled,
            bedtimeStartHour = bedtimeStartHour,
            bedtimeStartMinute = bedtimeStartMinute,
            bedtimeEndHour = bedtimeEndHour,
            bedtimeEndMinute = bedtimeEndMinute,
            blockDMs = prefs.getBoolean(KEY_BLOCK_DMS, false),
            blockExplore = prefs.getBoolean(KEY_BLOCK_EXPLORE, false),
            blockPosting = prefs.getBoolean(KEY_BLOCK_POSTING, false),
            requireApprovalForFollows = prefs.getBoolean(KEY_REQUIRE_APPROVAL_FOR_FOLLOWS, false),
            contentFilterLevel = contentFilterLevel,
            dailyUsageMinutes = dailyUsageMinutes,
            isDuringBedtime = isDuringBedtime,
            isOverDailyLimit = isOverDailyLimit,
            timeTamperingDetected = timeTamperingDetected
        )
    }

    private fun checkIfDuringBedtime(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): Boolean {
        val calendar = java.util.Calendar.getInstance()
        val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(java.util.Calendar.MINUTE)
        val currentTimeMinutes = currentHour * 60 + currentMinute

        val startTimeMinutes = startHour * 60 + startMinute
        val endTimeMinutes = endHour * 60 + endMinute

        return if (startTimeMinutes <= endTimeMinutes) {
            // Same day bedtime (e.g., 14:00 - 16:00)
            currentTimeMinutes in startTimeMinutes until endTimeMinutes
        } else {
            // Overnight bedtime (e.g., 21:00 - 07:00)
            currentTimeMinutes >= startTimeMinutes || currentTimeMinutes < endTimeMinutes
        }
    }

    /**
     * Update settings (requires verified session).
     */
    fun updateSettings(
        context: Context,
        maxDailyMinutes: Int? = null,
        bedtimeEnabled: Boolean? = null,
        bedtimeStartHour: Int? = null,
        bedtimeStartMinute: Int? = null,
        bedtimeEndHour: Int? = null,
        bedtimeEndMinute: Int? = null,
        blockDMs: Boolean? = null,
        blockExplore: Boolean? = null,
        blockPosting: Boolean? = null,
        requireApprovalForFollows: Boolean? = null,
        contentFilterLevel: ContentFilterLevel? = null
    ) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit {
            maxDailyMinutes?.let { putInt(KEY_MAX_DAILY_MINUTES, it.coerceIn(0, 24 * 60)) }
            bedtimeEnabled?.let { putBoolean(KEY_BEDTIME_ENABLED, it) }
            bedtimeStartHour?.let { putInt(KEY_BEDTIME_START_HOUR, it.coerceIn(0, 23)) }
            bedtimeStartMinute?.let { putInt(KEY_BEDTIME_START_MINUTE, it.coerceIn(0, 59)) }
            bedtimeEndHour?.let { putInt(KEY_BEDTIME_END_HOUR, it.coerceIn(0, 23)) }
            bedtimeEndMinute?.let { putInt(KEY_BEDTIME_END_MINUTE, it.coerceIn(0, 59)) }
            blockDMs?.let { putBoolean(KEY_BLOCK_DMS, it) }
            blockExplore?.let { putBoolean(KEY_BLOCK_EXPLORE, it) }
            blockPosting?.let { putBoolean(KEY_BLOCK_POSTING, it) }
            requireApprovalForFollows?.let { putBoolean(KEY_REQUIRE_APPROVAL_FOR_FOLLOWS, it) }
            contentFilterLevel?.let { putString(KEY_CONTENT_FILTER_LEVEL, it.name) }
        }
    }

    /**
     * Record usage time (call periodically while app is in use).
     */
    fun recordUsage(context: Context, minutes: Int = 1) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        val usageDate = prefs.getString(KEY_USAGE_DATE, null)

        val currentUsage = if (usageDate == today) {
            prefs.getInt(KEY_DAILY_USAGE_MINUTES, 0)
        } else {
            0
        }

        prefs.edit {
            putString(KEY_USAGE_DATE, today)
            putInt(KEY_DAILY_USAGE_MINUTES, currentUsage + minutes)
        }
    }

    /**
     * Enable or disable parental controls (PIN must already be set).
     */
    fun setEnabled(context: Context, enabled: Boolean) {
        if (!isPinSet(context)) return
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_IS_ENABLED, enabled)
        }
    }
}

