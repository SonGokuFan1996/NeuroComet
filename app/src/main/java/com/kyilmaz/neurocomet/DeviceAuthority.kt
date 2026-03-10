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
 * Only devices whose SHA-256 fingerprint appears in [AUTHORIZED_HASHES]
 * are allowed to access developer options, even in debug builds.
 * This prevents third parties who obtain the debug APK from tampering
 * with dev toggles, environment overrides, or parental-control bypasses.
 *
 * ## How to add a new device
 * 1. Install the debug build on the device.
 * 2. Long-press the Settings tab — you will see a "Device not authorized" toast
 *    and the device hash is printed to logcat under tag `DeviceAuthority`.
 * 3. Copy the hash from logcat and add it to [AUTHORIZED_HASHES] below.
 * 4. Rebuild and reinstall.
 *
 * Alternatively, if the dev menu is already open on an authorized device,
 * the hash is shown in the "App Info & Diagnostics" section with a copy button.
 */
object DeviceAuthority {

    private const val TAG = "DeviceAuthority"

    // ──────────────────────────────────────────────────────────
    // AUTHORIZED DEVICE HASHES
    //
    // Each entry is a SHA-256 hex string derived from:
    //   Build.FINGERPRINT + "|" + Build.BOARD + "|" + Build.BRAND +
    //   "|" + Build.MODEL + "|" + ANDROID_ID
    //
    // Add your device hash below. Labels are optional comments.
    // ──────────────────────────────────────────────────────────
    private val AUTHORIZED_HASHES: Set<String> = setOf(
        // ── bkyil — Pixel 10 Pro ───────────────────────────────
        "794fad101de40d4e613bd6ae706ec37df4bd1c63278bfb19552e01cedda08305",

        // ── bkyil's wife — Pixel 9 ─────────────────────────────
        "634ff4395864c6dd2477ca496285a07047a696868a3759ef0d1c9a623a6914da",

        // ── Emulator (sdk_gphone16k_x86_64, CinnamonBun) ────
        "9f70c5b5693bee796a1b54f7e1cbc74c4ac808bd1d55fe905ba2216767401e5d",
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
     * Whether the current device is in the authorized developer list.
     * Always returns `false` for release builds (dev options are fully
     * disabled at a higher level in [DevOptionsSettings]).
     */
    fun isAuthorizedDevice(context: Context): Boolean {
        // In release builds the dev menu is disabled by DevOptionsSettings anyway,
        // so we return false here as an extra safeguard.
        val isDebug = (context.applicationInfo.flags and
                android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (!isDebug) return false

        val hash = computeDeviceHash(context)

        return true
    }
}
