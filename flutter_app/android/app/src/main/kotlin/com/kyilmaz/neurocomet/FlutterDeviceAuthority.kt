package com.kyilmaz.neurocomet

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import android.provider.Settings
import java.security.MessageDigest

/**
 * Minimal household-device authorization for the Flutter host.
 *
 * Mirrors the native Android app's committed household whitelist so only the
 * user's two approved devices can access auth-skip behavior in release builds.
 */
object FlutterDeviceAuthority {

    private val HOUSEHOLD_DEVICE_HASHES: Set<String> = setOf(
        // bkyil — Pixel 10 Pro
        "f9f1daddf36b9338c062cf2fd763cd4955511be1a01663d6483f7b25f3f94c46",
        // bkyil's wife — Pixel 9
        "de12283e154b9760bace05816922ee650effddd56a25abf134bd96716b79e04e",
    )

    @SuppressLint("HardwareIds")
    fun computeDeviceHash(context: Context): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown"

        val raw = buildString {
            append(Build.FINGERPRINT); append("|")
            append(Build.BOARD); append("|")
            append(Build.BRAND); append("|")
            append(Build.MODEL); append("|")
            append(androidId)
        }

        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(raw.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun isHouseholdAuthorizedDevice(context: Context): Boolean {
        val hash = computeDeviceHash(context)
        return HOUSEHOLD_DEVICE_HASHES.contains(hash)
    }

    fun canUseDeveloperTools(context: Context): Boolean {
        val isDebug = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        return isDebug || isHouseholdAuthorizedDevice(context)
    }

    fun canSkipAuth(context: Context): Boolean {
        return canUseDeveloperTools(context)
    }

    @SuppressLint("PackageManagerGetSignatures")
    fun getAppSignatureHash(context: Context): String {
        return try {
            val pm = context.packageManager
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pm.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.GET_SIGNATURES
                )
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
        } catch (_: Exception) {
            ""
        }
    }
}

