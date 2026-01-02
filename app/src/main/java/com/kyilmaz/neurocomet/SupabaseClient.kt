@file:Suppress(
    "unused",
    "UNUSED",
    "ObjectPropertyName",
    "unused",
    "UNUSED_VARIABLE",
    "MemberVisibilityCanBePrivate"
)

package com.kyilmaz.neurocomet

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Supabase client wrapper that handles missing API keys gracefully.
 *
 * The app can run without Supabase connectivity - it will use mock data instead.
 * This allows development and testing without requiring API keys in the client code.
 *
 * Configuration (optional):
 * - Set SUPABASE_URL and SUPABASE_KEY via buildConfigField in build.gradle.kts
 * - Or set them in local.properties (not committed to version control)
 *
 * Security notes:
 * - API keys are NOT required for the app to function
 * - Internal endpoints should NOT require auth (handled server-side)
 * - CORS is handled by the server, not the client
 * - Input validation happens both client-side and server-side
 */
@Suppress("unused")
object AppSupabaseClient {

    private const val TAG = "AppSupabaseClient"

    /**
     * Whether Supabase is properly configured and available.
     */
    val isConfigured: Boolean by lazy {
        getSupabaseUrl() != null && getSupabaseKey() != null
    }

    /**
     * Get the Supabase URL if configured, null otherwise.
     */
    private fun getSupabaseUrl(): String? {
        return try {
            val url = BuildConfig.SUPABASE_URL
            if (url.isNotBlank() && url != "\"\"" && url != "null") url else null
        } catch (e: Exception) {
            Log.d(TAG, "SUPABASE_URL not configured")
            null
        }
    }

    /**
     * Get the Supabase key if configured, null otherwise.
     */
    private fun getSupabaseKey(): String? {
        return try {
            val key = BuildConfig.SUPABASE_KEY
            if (key.isNotBlank() && key != "\"\"" && key != "null") key else null
        } catch (e: Exception) {
            Log.d(TAG, "SUPABASE_KEY not configured")
            null
        }
    }

    /**
     * Lazily initialized Supabase client.
     * Returns null if not configured - app will use mock data instead.
     */
    val client: SupabaseClient? by lazy {
        val url = getSupabaseUrl()
        val key = getSupabaseKey()

        if (url == null || key == null) {
            Log.i(TAG, "Supabase not configured - using mock data mode")
            Log.i(TAG, "To enable Supabase, set SUPABASE_URL and SUPABASE_KEY in local.properties")
            null
        } else {
            try {
                Log.i(TAG, "Initializing Supabase client")
                createSupabaseClient(
                    supabaseUrl = url,
                    supabaseKey = key
                ) {
                    install(Postgrest)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Supabase client", e)
                null
            }
        }
    }

    /**
     * Check if we have a working Supabase connection.
     */
    fun isAvailable(): Boolean = client != null
}

/**
 * Helper interface for services that need Supabase.
 * Allows easy mocking and fallback to local data.
 */
interface SupabaseService {
    val isOnline: Boolean
        get() = AppSupabaseClient.isAvailable()
}

typealias SupabaseClientProvider = AppSupabaseClient

