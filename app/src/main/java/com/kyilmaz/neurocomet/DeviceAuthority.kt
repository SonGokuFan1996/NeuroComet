package com.kyilmaz.neurocomet

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import java.security.MessageDigest

/**
 * Device-level authorization gate for debug/dev functionality.
 *
 * The committed household whitelist is intentionally limited to two devices.
 * Additional local-only authorization for a debug/internal build can still be
 * injected through `local.properties` via `DEVELOPER_DEVICE_HASH`.
 * This prevents third parties who obtain the debug APK from tampering
 * with dev toggles, environment overrides, or parental-control bypasses.
 *
 * ## How to add a new device
 * 1. Install the debug build on the device.
 * 2. Long-press the Settings tab — you will see a "Device not authorized" toast
 *    and the device hash is printed to logcat under tag `DeviceAuthority`.
 * 3. Copy the hash from logcat and add it to `DEVELOPER_DEVICE_HASH` in
 *    `local.properties` for a local-only build, or add it to
 *    [HOUSEHOLD_DEVICE_HASHES] if it should become a committed household device.
 * 4. Rebuild and reinstall.
 *
 * Alternatively, if the dev menu is already open on an authorized device,
 * the hash is shown in the "App Info & Diagnostics" section with a copy button.
 */
object DeviceAuthority {

    private const val TAG = "DeviceAuthority"

    // ──────────────────────────────────────────────────────────
    // HOUSEHOLD DEVICE HASHES
    //
    // Each entry is a SHA-256 hex string derived from:
    //   Build.FINGERPRINT + "|" + Build.BOARD + "|" + Build.BRAND +
    //   "|" + Build.MODEL + "|" + ANDROID_ID
    //
    // Only the user's device and the wife's device belong here.
    // ──────────────────────────────────────────────────────────
    private val HOUSEHOLD_DEVICE_HASHES: Set<String> = setOf(
        // ── bkyil — Pixel 10 Pro (Android 17 / CP21.260306.017) ─
        "f9f1daddf36b9338c062cf2fd763cd4955511be1a01663d6483f7b25f3f94c46",

        // ── bkyil's wife — Pixel 9 (Android 17 / CP21.260306.017)
        "4d18ac796abdb71814159e41a7e5fdd5b63b4ba659d3a5be66cea9ee8dcef1b3",
    )

    // ──────────────────────────────────────────────────────────

    /**
     * Compute the deterministic SHA-256 device fingerprint for this device.
     * This value is stable across app reinstalls (as long as the OS is not reset).
     */
    @SuppressLint("HardwareIds")
    fun computeDeviceHash(context: Context): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown"

        val raw = buildString {
            append(Build.FINGERPRINT); append("|")
            append(Build.BOARD);       append("|")
            append(Build.BRAND);       append("|")
            append(Build.MODEL);       append("|")
            append(androidId)
        }

        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(raw.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Whether the current device is one of the two committed household devices.
     * These devices are allowed to use dev/test affordances in any build.
     */
    fun isHouseholdAuthorizedDevice(context: Context): Boolean {
        val hash = computeDeviceHash(context)
        return HOUSEHOLD_DEVICE_HASHES.contains(hash)
    }

    /**
     * Whether the current device is in the authorized developer list.
     */
    fun isAuthorizedDevice(context: Context): Boolean {
        val isDebug = (context.applicationInfo.flags and
                android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0

        // Detect Play Console Internal version via signature
        val internalSignature = BuildConfig.INTERNAL_SIGNATURE_HASH
        val currentSignature = getAppSignatureHash(context)
        val isInternalBuild = internalSignature.isNotBlank() && currentSignature == internalSignature
        val hash = computeDeviceHash(context)
        val isHouseholdDevice = HOUSEHOLD_DEVICE_HASHES.contains(hash)
        // DEVELOPER_DEVICE_HASH may be a single hash or a comma-separated list of hashes
        // (e.g. a physical dev device plus one or more emulator AVDs).
        val localDeveloperHashes = BuildConfig.DEVELOPER_DEVICE_HASH
            .split(',')
            .map { it.trim() }
            .filter { it.isNotBlank() }
        val authorizedHashes = HOUSEHOLD_DEVICE_HASHES + localDeveloperHashes
        val isAuthorizedHash = authorizedHashes.contains(hash)

        val result = isHouseholdDevice || ((isDebug || isInternalBuild) && isAuthorizedHash)

        if (!result && (isDebug || isInternalBuild)) {
            // Build type is OK, but hash is missing. Log once to help developer.
            Log.w(TAG, "DEVICE NOT AUTHORIZED: Hash '$hash' is not in the allowed list.")
            Log.w(TAG, "Diagnostic Info: Debug=$isDebug, Internal=${currentSignature == internalSignature}")
        }

        return result
    }

    fun canUseDeveloperTools(context: Context): Boolean = isAuthorizedDevice(context)

    fun requireDeveloperToolsAccess(context: Context, operation: String) {
        if (canUseDeveloperTools(context)) return

        logDevAccessInfo(context)
        Log.wtf(TAG, "Unauthorized developer-tools access attempt: $operation")
        throw SecurityException(
            "Developer tools are restricted. Unauthorized attempt to $operation."
        )
    }

    /**
     * Explicitly log diagnostic info for developer authorization.
     */
    fun logDevAccessInfo(context: Context) {
        try {
            val hash = computeDeviceHash(context)
            val currentSignature = getAppSignatureHash(context)
            
            Log.i(TAG, "--- DEV ACCESS DIAGNOSTIC INFO ---")
            Log.i(TAG, "DEVICE HASH: $hash")
            Log.i(TAG, "APP SIGNATURE: $currentSignature")
            Log.i(TAG, "To authorize, add to local.properties and rebuild:")
            Log.i(TAG, "DEVELOPER_DEVICE_HASH=$hash")
            Log.i(TAG, "INTERNAL_SIGNATURE_HASH=$currentSignature")
            Log.i(TAG, "----------------------------------")
        } catch (e: Exception) {
            Log.e(TAG, "Error logging dev access info", e)
        }
    }

    /**
     * Get the SHA-256 hash of the app's signing certificate.
     */
    @SuppressLint("PackageManagerGetSignatures")
    fun getAppSignatureHash(context: Context): String {
        return try {
            val pm = context.packageManager
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pm.getPackageInfo(context.packageName, android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(context.packageName, android.content.pm.PackageManager.GET_SIGNATURES)
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            if (signatures.isNullOrEmpty()) return ""

            val digest = MessageDigest.getInstance("SHA-256")
            val firstSignature = signatures[0] ?: return ""
            val signatureBytes = firstSignature.toByteArray()
            val hashBytes = digest.digest(signatureBytes)
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to compute app signature hash", e)
            ""
        }
    }
}
