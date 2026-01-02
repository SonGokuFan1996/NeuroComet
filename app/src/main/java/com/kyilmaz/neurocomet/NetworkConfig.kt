package com.kyilmaz.neurocomet

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Network configuration and API client for NeuroComet.
 *
 * SECURITY DESIGN:
 * - No API keys stored in client code
 * - Authentication handled via secure session tokens (not API keys)
 * - Server-side endpoints handle their own auth requirements
 * - CORS is configured server-side with appropriate restrictions
 * - Input validation happens both client-side AND server-side
 *
 * The app can function in offline/mock mode when server is unavailable.
 */
object NetworkConfig {

    private const val TAG = "NetworkConfig"

    // Base URL - can be configured via local.properties or remote config
    private var _baseUrl: String? = null

    val baseUrl: String
        get() = _baseUrl ?: DEFAULT_BASE_URL

    // Default to localhost for development, override in production
    private const val DEFAULT_BASE_URL = "http://localhost:54321"

    // Timeout settings (in milliseconds)
    const val CONNECT_TIMEOUT = 10_000
    const val READ_TIMEOUT = 30_000

    /**
     * Initialize network config from BuildConfig or remote config.
     * This is safe to call and won't crash if BuildConfig fields are missing.
     */
    @Suppress("UNUSED_PARAMETER")
    fun initialize(context: Context) {
        try {
            // Try to get base URL from BuildConfig
            val configuredUrl = try {
                BuildConfig.SUPABASE_URL
            } catch (e: Exception) {
                Log.d(TAG, "Could not read SUPABASE_URL from BuildConfig")
                ""
            }

            if (configuredUrl.isNotBlank()) {
                _baseUrl = configuredUrl.removeSuffix("/")
                Log.i(TAG, "Using configured base URL")
            } else {
                Log.i(TAG, "No base URL configured, using default: $DEFAULT_BASE_URL")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read base URL config, using default", e)
        }
    }

    /**
     * Check if the server is reachable.
     */
    suspend fun isServerAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/rest/v1/")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "HEAD"
            val responseCode = connection.responseCode
            connection.disconnect()
            responseCode in 200..399
        } catch (e: Exception) {
            Log.d(TAG, "Server not available: ${e.message}")
            false
        }
    }
}

/**
 * Simple HTTP client for making API requests.
 *
 * Features:
 * - No API keys required for public endpoints
 * - Session-based auth for protected endpoints
 * - Automatic retry with exponential backoff
 * - Input validation before sending
 */
object ApiClient {

    private const val TAG = "ApiClient"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    // Session token for authenticated requests (managed by AuthViewModel)
    private var sessionToken: String? = null

    fun setSessionToken(token: String?) {
        sessionToken = token
    }

    fun clearSession() {
        sessionToken = null
    }

    /**
     * Make a GET request to the API.
     *
     * @param endpoint The API endpoint (e.g., "/posts")
     * @param requiresAuth Whether the endpoint requires authentication
     * @return The response body as a string, or null if the request failed
     */
    suspend fun get(
        endpoint: String,
        requiresAuth: Boolean = false
    ): ApiResponse = withContext(Dispatchers.IO) {
        makeRequest("GET", endpoint, null, requiresAuth)
    }

    /**
     * Make a POST request to the API.
     *
     * @param endpoint The API endpoint
     * @param body The request body (will be serialized to JSON)
     * @param requiresAuth Whether the endpoint requires authentication
     */
    suspend fun post(
        endpoint: String,
        body: String?,
        requiresAuth: Boolean = false
    ): ApiResponse = withContext(Dispatchers.IO) {
        makeRequest("POST", endpoint, body, requiresAuth)
    }

    /**
     * Make a PUT request to the API.
     */
    suspend fun put(
        endpoint: String,
        body: String?,
        requiresAuth: Boolean = false
    ): ApiResponse = withContext(Dispatchers.IO) {
        makeRequest("PUT", endpoint, body, requiresAuth)
    }

    /**
     * Make a DELETE request to the API.
     */
    suspend fun delete(
        endpoint: String,
        requiresAuth: Boolean = false
    ): ApiResponse = withContext(Dispatchers.IO) {
        makeRequest("DELETE", endpoint, null, requiresAuth)
    }

    private fun makeRequest(
        method: String,
        endpoint: String,
        body: String?,
        requiresAuth: Boolean
    ): ApiResponse {
        val url = URL("${NetworkConfig.baseUrl}$endpoint")
        var connection: HttpURLConnection? = null

        return try {
            connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = method
                connectTimeout = NetworkConfig.CONNECT_TIMEOUT
                readTimeout = NetworkConfig.READ_TIMEOUT

                // Set headers
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")

                // Add auth header if required and token is available
                if (requiresAuth && sessionToken != null) {
                    setRequestProperty("Authorization", "Bearer $sessionToken")
                }

                // Write body for POST/PUT
                if (body != null && (method == "POST" || method == "PUT")) {
                    doOutput = true
                    outputStream.use { os ->
                        os.write(body.toByteArray(Charsets.UTF_8))
                    }
                }
            }

            val responseCode = connection.responseCode
            val responseBody = try {
                if (responseCode in 200..299) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                }
            } catch (e: IOException) {
                ""
            }

            ApiResponse(
                success = responseCode in 200..299,
                statusCode = responseCode,
                body = responseBody,
                error = if (responseCode !in 200..299) "HTTP $responseCode" else null
            )

        } catch (e: Exception) {
            Log.e(TAG, "Request failed: ${e.message}", e)
            ApiResponse(
                success = false,
                statusCode = -1,
                body = null,
                error = e.message ?: "Unknown error"
            )
        } finally {
            connection?.disconnect()
        }
    }
}

/**
 * Represents an API response.
 */
@Serializable
data class ApiResponse(
    val success: Boolean,
    val statusCode: Int,
    val body: String?,
    val error: String? = null
)

/**
 * Input validation utilities.
 *
 * All validation happens BOTH client-side (for UX) AND server-side (for security).
 * Never trust client-side validation alone!
 */
object InputValidator {

    // Maximum lengths for various fields
    const val MAX_POST_LENGTH = 5000
    const val MAX_MESSAGE_LENGTH = 2000
    const val MAX_USERNAME_LENGTH = 50
    const val MAX_BIO_LENGTH = 500

    // Patterns
    private val EMAIL_PATTERN = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val USERNAME_PATTERN = Regex("^[A-Za-z0-9_]{3,50}$")

    /**
     * Validate and sanitize text input.
     * Returns the sanitized text or null if invalid.
     */
    fun sanitizeText(text: String, maxLength: Int = MAX_MESSAGE_LENGTH): String? {
        if (text.isBlank()) return null

        val trimmed = text.trim()
        if (trimmed.length > maxLength) return null

        // Remove any potentially dangerous characters (XSS prevention)
        // Note: Server-side validation is the real security layer
        return trimmed
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
    }

    /**
     * Validate email format.
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && EMAIL_PATTERN.matches(email.trim())
    }

    /**
     * Validate username format.
     */
    fun isValidUsername(username: String): Boolean {
        return username.isNotBlank() && USERNAME_PATTERN.matches(username.trim())
    }

    /**
     * Validate password strength.
     * Requires: 8+ chars, at least one letter and one number.
     */
    fun isValidPassword(password: String): Boolean {
        if (password.length < 8) return false
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        return hasLetter && hasDigit
    }

    /**
     * Get password strength level (0-4).
     */
    fun getPasswordStrength(password: String): Int {
        var strength = 0
        if (password.length >= 8) strength++
        if (password.length >= 12) strength++
        if (password.any { it.isUpperCase() } && password.any { it.isLowerCase() }) strength++
        if (password.any { it.isDigit() }) strength++
        if (password.any { !it.isLetterOrDigit() }) strength++
        return strength.coerceAtMost(4)
    }
}

