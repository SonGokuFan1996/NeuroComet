package com.kyilmaz.neurocomet

import android.util.Log

/**
 * Utility for basic string obfuscation to prevent simple string searches in the APK.
 * Note: This is not "unbreakable" encryption, but it's much better than plain text
 * for discouraging casual bad actors.
 */
object SecurityUtils {

    private const val TAG = "SecurityUtils"

    // XOR key loaded from BuildConfig — never hardcoded in source.
    // The value is injected at build time from local.properties / env.
    private val XOR_KEY: String by lazy {
        try {
            val field = BuildConfig::class.java.getField("OBFUSCATION_KEY")
            field.get(null) as? String ?: ""
        } catch (_: Exception) {
            // Fallback: BuildConfig field not yet generated (pre-sync)
            ""
        }
    }

    /**
     * De-obfuscates a string that was obfuscated with XOR and Hex encoded.
     */
    fun decrypt(obfuscated: String?): String {
        if (obfuscated.isNullOrEmpty() || obfuscated == "null" || obfuscated == "\"\"") {
            return ""
        }

        return try {
            val result = StringBuilder()
            for (i in 0 until obfuscated.length step 2) {
                val hex = obfuscated.substring(i, i + 2)
                val byte = hex.toInt(16)
                val keyChar = XOR_KEY[(i / 2) % XOR_KEY.length].code
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
        val bytes = plain.toByteArray(Charsets.UTF_8)
        val result = StringBuilder()
        for (i in bytes.indices) {
            val obfuscatedByte = bytes[i].toInt() xor XOR_KEY[i % XOR_KEY.length].code
            result.append(String.format("%02x", obfuscatedByte and 0xFF))
        }
        return result.toString()
    }
}
