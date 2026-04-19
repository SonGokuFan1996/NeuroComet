package com.kyilmaz.neurocomet

import android.annotation.SuppressLint
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

object LocationHelper {
    fun hasForegroundLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    suspend fun getLocationTag(context: Context): String? {
        if (!hasForegroundLocationPermission(context)) return null

        val location = getCurrentLocation(context) ?: return null
        return reverseGeocode(context, location)
    }

    @SuppressLint("MissingPermission")
    @Suppress("MissingPermission")
    private suspend fun getCurrentLocation(context: Context): Location? =
        suspendCancellableCoroutine { continuation ->
            if (!hasForegroundLocationPermission(context)) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }
            val client = LocationServices.getFusedLocationProviderClient(context)
            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (continuation.isActive) continuation.resume(location)
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume(null)
                }
        }

    private suspend fun reverseGeocode(context: Context, location: Location): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                val geocoder = Geocoder(context)
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val address = addresses?.firstOrNull() ?: return@withContext null

                val locality = address.locality?.takeIf { it.isNotBlank() }
                    ?: address.subAdminArea?.takeIf { it.isNotBlank() }
                val region = address.adminArea?.takeIf { it.isNotBlank() }
                    ?: address.countryName?.takeIf { it.isNotBlank() }

                when {
                    locality != null && region != null -> "$locality, $region"
                    locality != null -> locality
                    region != null -> region
                    else -> null
                }
            }.getOrNull()
        }
}

