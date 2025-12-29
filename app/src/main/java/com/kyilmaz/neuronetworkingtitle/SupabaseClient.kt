@file:Suppress(
    "unused",
    "UNUSED",
    "ObjectPropertyName",
    "unused",
    "UNUSED_VARIABLE",
    "MemberVisibilityCanBePrivate"
)

package com.kyilmaz.neuronetworkingtitle

import com.kyilmaz.neuronetworkingtitle.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import android.util.Log

// Avoid name collision with io.github.jan.supabase.SupabaseClient which confuses some analyzers.
@Suppress("unused")
object AppSupabaseClient {
    // NOTE: These are expected to be provided via buildConfigField in Gradle.
    // Validate and fail fast with a clear message if they are not configured.
    private fun resolveSupabaseUrl(): String {
        val configuredUrl = BuildConfig.SUPABASE_URL
        if (configuredUrl.isNotBlank()) {
            return configuredUrl
        }

        if (BuildConfig.DEBUG) {
            Log.w(
                "AppSupabaseClient",
                "Supabase URL is not configured for debug build. " +
                "Set SUPABASE_URL via buildConfigField / gradle.properties to enable Supabase."
            )
        }

        throw IllegalStateException(
            "Supabase URL is not configured. Set SUPABASE_URL via buildConfigField / gradle.properties."
        )
    }

    private fun resolveSupabaseKey(): String {
        val configuredKey = BuildConfig.SUPABASE_KEY
        if (configuredKey.isNotBlank()) {
            return configuredKey
        }

        if (BuildConfig.DEBUG) {
            Log.w(
                "AppSupabaseClient",
                "Supabase key is not configured. Using placeholder key for debug build. " +
                "Set SUPABASE_KEY via buildConfigField / gradle.properties to enable Supabase."
            )
            // Placeholder key used only to avoid hard crashes in development.
            return "DEVELOPMENT_PLACEHOLDER_KEY"
        }

        throw IllegalStateException(
            "Supabase key is not configured. Set SUPABASE_KEY via buildConfigField / gradle.properties."
        )
    }

    private val url: String = resolveSupabaseUrl()
    private val key: String = resolveSupabaseKey()
    @Suppress("unused")
    val client: io.github.jan.supabase.SupabaseClient = createSupabaseClient(
        supabaseUrl = url,
        supabaseKey = key
    ) {
        install(Postgrest)
    }
}

typealias SupabaseClientProvider = AppSupabaseClient
