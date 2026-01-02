package com.kyilmaz.neurocomet

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import coil.Coil

private const val TAG = "NeuroCometApp"

/**
 * Application class for NeuroComet.
 *
 * Handles:
 * - Global Coil ImageLoader configuration
 * - Application-wide initialization
 *
 * Note: Heavy initialization is deferred to prevent ANR and binder issues.
 */
class NeuroCometApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        try {
            // Store application reference for global access (lightweight)
            ApplicationProvider.init(this)

            // Initialize Coil ImageLoader (lightweight)
            initializeImageLoader()

            // Defer heavier initialization to avoid blocking the main thread
            Handler(Looper.getMainLooper()).post {
                try {
                    // Initialize network configuration (no API keys required)
                    NetworkConfig.initialize(this)

                    // Initialize PrivacyManager with persisted data
                    PrivacyManager.initialize(this)

                    // Create notification channels
                    NotificationChannels.createNotificationChannels(this)

                    Log.d(TAG, "Application initialized successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Error during deferred initialization", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during application initialization", e)
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

