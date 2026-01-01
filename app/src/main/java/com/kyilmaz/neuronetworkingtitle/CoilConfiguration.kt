package com.kyilmaz.neuronetworkingtitle

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import java.io.File

/**
 * Optimized ImageLoader configuration for NeuroNet.
 *
 * Features:
 * - Memory caching for fast repeated image loads
 * - Disk caching for offline access and reduced network usage
 * - Hardware bitmap support on Android O+ for reduced memory
 * - Crossfade animations disabled in performance mode
 */
object CoilConfiguration {

    private const val MEMORY_CACHE_PERCENTAGE = 0.25 // 25% of app's memory
    private const val DISK_CACHE_SIZE = 100L * 1024 * 1024 // 100 MB

    /**
     * Create an optimized ImageLoader for the application.
     * Call this in Application.onCreate() or provide via Coil.setImageLoader()
     */
    fun createImageLoader(
        context: Context,
        isDebug: Boolean = BuildConfig.DEBUG
    ): ImageLoader {
        return ImageLoader.Builder(context)
            // Memory cache - stores decoded images
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(MEMORY_CACHE_PERCENTAGE)
                    .build()
            }
            // Disk cache - stores encoded images
            .diskCache {
                DiskCache.Builder()
                    .directory(File(context.cacheDir, "image_cache"))
                    .maxSizeBytes(DISK_CACHE_SIZE)
                    .build()
            }
            // Use hardware bitmaps for reduced memory (always enabled since minSdk >= 26)
            .allowHardware(true)
            // Enable crossfade only in non-performance-critical situations
            .crossfade(true)
            // Respect cache headers from server
            .respectCacheHeaders(true)
            // Memory cache policy
            .memoryCachePolicy(CachePolicy.ENABLED)
            // Disk cache policy
            .diskCachePolicy(CachePolicy.ENABLED)
            // Network cache policy
            .networkCachePolicy(CachePolicy.ENABLED)
            // Debug logging in debug builds only
            .apply {
                if (isDebug) {
                    logger(DebugLogger())
                }
            }
            .build()
    }

    /**
     * Create a performance-optimized ImageLoader for list views.
     * Disables crossfade for smoother scrolling.
     */
    fun createListOptimizedImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(MEMORY_CACHE_PERCENTAGE)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(File(context.cacheDir, "image_cache"))
                    .maxSizeBytes(DISK_CACHE_SIZE)
                    .build()
            }
            .allowHardware(true)
            .crossfade(false) // Disable for smoother scrolling
            .respectCacheHeaders(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }
}

