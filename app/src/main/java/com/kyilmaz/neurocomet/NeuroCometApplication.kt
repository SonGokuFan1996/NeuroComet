package com.kyilmaz.neurocomet

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import coil.Coil
import java.util.concurrent.Executors

private const val TAG = "NeuroCometApp"

/**
 * Application class for NeuroComet.
 *
 * Handles:
 * - Global Coil ImageLoader configuration
 * - Application-wide initialization
 * - Per-App Language restoration
 *
 * Note: Heavy initialization is deferred to prevent ANR and binder issues.
 */
class NeuroCometApplication : Application() {

    override fun onCreate() {
        // IMPORTANT: Restore locale BEFORE super.onCreate() to ensure
        // the correct locale is used when Resources are first accessed
        restoreSavedLocale()

        super.onCreate()

        try {
            // Store application reference for global access (lightweight)
            ApplicationProvider.init(this)

            // Initialize Coil ImageLoader (lightweight)
            initializeImageLoader()

            // Run non-UI initialization on a background thread to avoid blocking startup
            Executors.newSingleThreadExecutor().execute {
                try {
                    // Initialize network configuration (no API keys required)
                    NetworkConfig.initialize(this)

                    // Initialize PrivacyManager with persisted data
                    PrivacyManager.initialize(this)

                    // Create notification channels
                    NotificationChannels.createNotificationChannels(this)

                    // Initialize A/B testing state (deterministic, no network needed)
                    ABTestManager.refreshState(this)
                } catch (e: Exception) {
                    Log.e(TAG, "Error during background initialization", e)
                }
            }

            Log.d(TAG, "Application initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during application initialization", e)
        }
    }

    /**
     * Restore the saved locale preference.
     *
     * We use SharedPreferences as our primary storage for locale preference
     * to ensure consistency and reliability across all Android versions.
     *
     * This ensures the app starts with the user's preferred language.
     */
    private fun restoreSavedLocale() {
        try {
            // Check our SharedPreferences for saved locale
            val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
            val savedLocale = prefs.getString("selected_locale", null)

            if (!savedLocale.isNullOrEmpty()) {
                Log.d(TAG, "Restoring saved locale: $savedLocale")

                // Create LocaleListCompat from the saved tag
                val localeList = LocaleListCompat.forLanguageTags(savedLocale)

                if (!localeList.isEmpty) {
                    AppCompatDelegate.setApplicationLocales(localeList)
                    Log.d(TAG, "Locale set successfully: ${localeList.toLanguageTags()}")
                } else {
                    Log.w(TAG, "Failed to parse locale tag: $savedLocale")
                }
            } else {
                Log.d(TAG, "No saved locale, using system default")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring saved locale", e)
        }
    }

    private fun initializeImageLoader() {
        try {
            // Set the global ImageLoader for Coil
            Coil.setImageLoader {
                CoilConfiguration.createImageLoader(
                    context = this,
                    isDebug = BuildConfig.DEBUG
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ImageLoader", e)
        }
    }
}

