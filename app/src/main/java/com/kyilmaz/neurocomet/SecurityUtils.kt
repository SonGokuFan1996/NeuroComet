package com.kyilmaz.neurocomet

import android.util.Log

/**
 * Utility for basic string obfuscation to prevent simple string searches in the APK.
 * Note: This is not "unbreakable" encryption, but it's much better than plain text
 * for discouraging casual bad actors.
 */
object SecurityUtils {

    private const val TAG = "SecurityUtils"

    // XOR key loaded from BuildConfig and injected at build time.
    private val xorKey: String
        get() = BuildConfig.OBFUSCATION_KEY

    /**
     * De-obfuscates a string that was obfuscated with XOR and Hex encoded.
     */
    fun decrypt(obfuscated: String?): String {
        if (obfuscated.isNullOrEmpty() || obfuscated == "null" || obfuscated == "\"\"") {
            return ""
        }

        if (xorKey.isBlank()) {
            Log.w(TAG, "Missing obfuscation key; refusing to decrypt build-time secrets")
            return ""
        }

        return try {
            val result = StringBuilder()
            for (i in 0 until obfuscated.length step 2) {
                val hex = obfuscated.substring(i, i + 2)
                val byte = hex.toInt(16)
                val keyChar = xorKey[(i / 2) % xorKey.length].code
                result.append((byte xor keyChar).toChar())
            }
            result.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decrypt string", e)
            ""
        }
    }

    /**
     * Obfuscates a string using XOR and Hex encoding.
     */
    fun encrypt(plain: String): String {
        if (xorKey.isBlank()) {
            Log.w(TAG, "Missing obfuscation key; refusing to encrypt runtime value")
            return ""
        }

        val bytes = plain.toByteArray(Charsets.UTF_8)
        val result = StringBuilder()
        for (i in bytes.indices) {
            val obfuscatedByte = bytes[i].toInt() xor xorKey[i % xorKey.length].code
            result.append(String.format("%02x", obfuscatedByte and 0xFF))
        }
        return result.toString()
    }
}
