package com.kyilmaz.neurocomet

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.sqrt

/**
 * Location and Sensor Service for NeuroComet
 *
 * Features:
 * - Fused location provider for optimal accuracy
 * - Sensor fusion for improved location precision
 * - Battery-efficient location updates
 * - Configurable update intervals
 * - Privacy-focused (only collects when needed)
 *
 * Uses:
 * - GPS, Network, and Wi-Fi for location
 * - Accelerometer for motion detection
 * - Magnetometer for heading/orientation
 * - Barometer for altitude correction
 */

object LocationService {

    private const val TAG = "LocationService"

    // Location request configurations
    private const val DEFAULT_UPDATE_INTERVAL_MS = 10_000L
    private const val FASTEST_UPDATE_INTERVAL_MS = 5_000L
    private const val HIGH_ACCURACY_INTERVAL_MS = 1_000L

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var sensorManager: SensorManager? = null
    private var currentLocationCallback: LocationCallback? = null

    // Current sensor data
    private val _sensorData = MutableStateFlow(SensorData())
    val sensorData: StateFlow<SensorData> = _sensorData.asStateFlow()

    // Current location
    private val _currentLocation = MutableStateFlow<LocationData?>(null)
    val currentLocation: StateFlow<LocationData?> = _currentLocation.asStateFlow()

    // Location status
    private val _locationStatus = MutableStateFlow(LocationStatus.IDLE)
    val locationStatus: StateFlow<LocationStatus> = _locationStatus.asStateFlow()

    /**
     * Initialize location services
     */
    fun initialize(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        Log.d(TAG, "Location service initialized")
    }

    // ═══════════════════════════════════════════════════════════════
    // PERMISSION CHECKING
    // ═══════════════════════════════════════════════════════════════

    /**
     * Check if location permissions are granted
     */
    fun hasLocationPermission(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    /**
     * Check if background location permission is granted (Android 10+)
     */
    fun hasBackgroundLocationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not needed on older versions
        }
    }

    /**
     * Get required permissions based on use case
     */
    fun getRequiredPermissions(includeBackground: Boolean = false): Array<String> {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (includeBackground && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        return permissions.toTypedArray()
    }

    // ═══════════════════════════════════════════════════════════════
    // LOCATION RETRIEVAL
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get the last known location (fast, but may be stale)
     */
    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocation(context: Context): LocationData? {
        if (!hasLocationPermission(context)) {
            Log.w(TAG, "Location permission not granted")
            return null
        }

        val client = fusedLocationClient ?: run {
            initialize(context)
            fusedLocationClient
        }

        return suspendCancellableCoroutine { continuation ->
            client?.lastLocation
                ?.addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(location.toLocationData())
                    } else {
                        continuation.resume(null)
                    }
                }
                ?.addOnFailureListener { e ->
                    Log.e(TAG, "Failed to get last location", e)
                    continuation.resume(null)
                }
        }
    }

    /**
     * Get current location with high accuracy (may take a few seconds)
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(
        context: Context,
        priority: LocationPriority = LocationPriority.BALANCED
    ): LocationData? {
        if (!hasLocationPermission(context)) {
            Log.w(TAG, "Location permission not granted")
            return null
        }

        val client = fusedLocationClient ?: run {
            initialize(context)
            fusedLocationClient
        }

        _locationStatus.value = LocationStatus.ACQUIRING

        val gmsePriority = when (priority) {
            LocationPriority.HIGH_ACCURACY -> Priority.PRIORITY_HIGH_ACCURACY
            LocationPriority.BALANCED -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
            LocationPriority.LOW_POWER -> Priority.PRIORITY_LOW_POWER
            LocationPriority.PASSIVE -> Priority.PRIORITY_PASSIVE
        }

        return suspendCancellableCoroutine { continuation ->
            val request = CurrentLocationRequest.Builder()
                .setPriority(gmsePriority)
                .setMaxUpdateAgeMillis(30_000L)
                .build()

            client?.getCurrentLocation(request, null)
                ?.addOnSuccessListener { location ->
                    _locationStatus.value = LocationStatus.ACQUIRED
                    if (location != null) {
                        val locationData = location.toLocationData()
                        _currentLocation.value = locationData
                        continuation.resume(locationData)
                    } else {
                        continuation.resume(null)
                    }
                }
                ?.addOnFailureListener { e ->
                    _locationStatus.value = LocationStatus.ERROR
                    Log.e(TAG, "Failed to get current location", e)
                    continuation.resume(null)
                }
        }
    }

    /**
     * Start continuous location updates
     */
    @SuppressLint("MissingPermission")
    fun startLocationUpdates(
        context: Context,
        priority: LocationPriority = LocationPriority.BALANCED,
        intervalMs: Long = DEFAULT_UPDATE_INTERVAL_MS,
        onLocationUpdate: (LocationData) -> Unit
    ) {
        if (!hasLocationPermission(context)) {
            Log.w(TAG, "Location permission not granted")
            return
        }

        stopLocationUpdates()

        val client = fusedLocationClient ?: run {
            initialize(context)
            fusedLocationClient
        }

        val gmsPriority = when (priority) {
            LocationPriority.HIGH_ACCURACY -> Priority.PRIORITY_HIGH_ACCURACY
            LocationPriority.BALANCED -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
            LocationPriority.LOW_POWER -> Priority.PRIORITY_LOW_POWER
            LocationPriority.PASSIVE -> Priority.PRIORITY_PASSIVE
        }

        val request = LocationRequest.Builder(gmsPriority, intervalMs)
            .setMinUpdateIntervalMillis(FASTEST_UPDATE_INTERVAL_MS)
            .setWaitForAccurateLocation(priority == LocationPriority.HIGH_ACCURACY)
            .build()

        currentLocationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val locationData = location.toLocationData()
                    _currentLocation.value = locationData
                    _locationStatus.value = LocationStatus.TRACKING
                    onLocationUpdate(locationData)
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                if (!availability.isLocationAvailable) {
                    _locationStatus.value = LocationStatus.UNAVAILABLE
                }
            }
        }

        client?.requestLocationUpdates(
            request,
            currentLocationCallback!!,
            Looper.getMainLooper()
        )

        _locationStatus.value = LocationStatus.TRACKING
        Log.d(TAG, "Started location updates with interval: ${intervalMs}ms")
    }

    /**
     * Stop location updates
     */
    fun stopLocationUpdates() {
        currentLocationCallback?.let { callback ->
            fusedLocationClient?.removeLocationUpdates(callback)
            currentLocationCallback = null
            _locationStatus.value = LocationStatus.IDLE
            Log.d(TAG, "Stopped location updates")
        }
    }

    /**
     * Get location updates as a Flow
     */
    @SuppressLint("MissingPermission")
    fun locationUpdatesFlow(
        context: Context,
        priority: LocationPriority = LocationPriority.BALANCED,
        intervalMs: Long = DEFAULT_UPDATE_INTERVAL_MS
    ): Flow<LocationData> = callbackFlow {
        if (!hasLocationPermission(context)) {
            close(SecurityException("Location permission not granted"))
            return@callbackFlow
        }

        val client = fusedLocationClient ?: LocationServices.getFusedLocationProviderClient(context)

        val gmsPriority = when (priority) {
            LocationPriority.HIGH_ACCURACY -> Priority.PRIORITY_HIGH_ACCURACY
            LocationPriority.BALANCED -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
            LocationPriority.LOW_POWER -> Priority.PRIORITY_LOW_POWER
            LocationPriority.PASSIVE -> Priority.PRIORITY_PASSIVE
        }

        val request = LocationRequest.Builder(gmsPriority, intervalMs)
            .setMinUpdateIntervalMillis(FASTEST_UPDATE_INTERVAL_MS)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(location.toLocationData())
                }
            }
        }

        client.requestLocationUpdates(request, callback, Looper.getMainLooper())

        awaitClose {
            client.removeLocationUpdates(callback)
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // SENSOR ACCESS FOR LOCATION ENHANCEMENT
    // ═══════════════════════════════════════════════════════════════

    private var accelerometerListener: SensorEventListener? = null
    private var magnetometerListener: SensorEventListener? = null
    private var barometerListener: SensorEventListener? = null

    /**
     * Start sensor monitoring for enhanced location accuracy
     */
    fun startSensorMonitoring(context: Context) {
        val sm = sensorManager ?: run {
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            sensorManager
        }

        // Accelerometer for motion detection
        sm?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let { accelerometer ->
            accelerometerListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    val magnitude = sqrt(x * x + y * y + z * z)

                    _sensorData.value = _sensorData.value.copy(
                        accelerometerX = x,
                        accelerometerY = y,
                        accelerometerZ = z,
                        isMoving = magnitude > 12f // Threshold for movement detection
                    )
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sm.registerListener(
                accelerometerListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        // Magnetometer for heading
        sm?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.let { magnetometer ->
            magnetometerListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    _sensorData.value = _sensorData.value.copy(
                        magneticX = event.values[0],
                        magneticY = event.values[1],
                        magneticZ = event.values[2]
                    )
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sm.registerListener(
                magnetometerListener,
                magnetometer,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        // Barometer for altitude
        sm?.getDefaultSensor(Sensor.TYPE_PRESSURE)?.let { barometer ->
            barometerListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val pressure = event.values[0]
                    val altitude = SensorManager.getAltitude(
                        SensorManager.PRESSURE_STANDARD_ATMOSPHERE,
                        pressure
                    )

                    _sensorData.value = _sensorData.value.copy(
                        pressure = pressure,
                        barometerAltitude = altitude
                    )
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sm.registerListener(
                barometerListener,
                barometer,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        Log.d(TAG, "Started sensor monitoring")
    }

    /**
     * Stop sensor monitoring
     */
    fun stopSensorMonitoring() {
        accelerometerListener?.let { sensorManager?.unregisterListener(it) }
        magnetometerListener?.let { sensorManager?.unregisterListener(it) }
        barometerListener?.let { sensorManager?.unregisterListener(it) }

        accelerometerListener = null
        magnetometerListener = null
        barometerListener = null

        Log.d(TAG, "Stopped sensor monitoring")
    }

    /**
     * Calculate heading from accelerometer and magnetometer data
     */
    fun getHeading(): Float? {
        val sensor = _sensorData.value

        if (sensor.accelerometerX == 0f && sensor.magneticX == 0f) {
            return null
        }

        val rotationMatrix = FloatArray(9)
        val inclinationMatrix = FloatArray(9)
        val accelerometerReading = floatArrayOf(
            sensor.accelerometerX,
            sensor.accelerometerY,
            sensor.accelerometerZ
        )
        val magnetometerReading = floatArrayOf(
            sensor.magneticX,
            sensor.magneticY,
            sensor.magneticZ
        )

        val success = SensorManager.getRotationMatrix(
            rotationMatrix,
            inclinationMatrix,
            accelerometerReading,
            magnetometerReading
        )

        return if (success) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            Math.toDegrees(orientation[0].toDouble()).toFloat()
        } else {
            null
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // CLEANUP
    // ═══════════════════════════════════════════════════════════════

    /**
     * Clean up all location and sensor resources
     */
    fun cleanup() {
        stopLocationUpdates()
        stopSensorMonitoring()
        fusedLocationClient = null
        sensorManager = null
        _currentLocation.value = null
        _locationStatus.value = LocationStatus.IDLE
        Log.d(TAG, "Location service cleaned up")
    }
}

// ═══════════════════════════════════════════════════════════════
// DATA CLASSES
// ═══════════════════════════════════════════════════════════════

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val accuracy: Float? = null,
    val bearing: Float? = null,
    val speed: Float? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val provider: String? = null
)

data class SensorData(
    val accelerometerX: Float = 0f,
    val accelerometerY: Float = 0f,
    val accelerometerZ: Float = 0f,
    val magneticX: Float = 0f,
    val magneticY: Float = 0f,
    val magneticZ: Float = 0f,
    val pressure: Float = 0f,
    val barometerAltitude: Float = 0f,
    val isMoving: Boolean = false
)

enum class LocationPriority {
    HIGH_ACCURACY,  // Best accuracy, highest battery
    BALANCED,       // Good accuracy, moderate battery
    LOW_POWER,      // Lower accuracy, better battery
    PASSIVE         // Only receive updates from other apps
}

enum class LocationStatus {
    IDLE,
    ACQUIRING,
    ACQUIRED,
    TRACKING,
    UNAVAILABLE,
    ERROR
}

// Extension function to convert Android Location to LocationData
fun Location.toLocationData(): LocationData {
    return LocationData(
        latitude = latitude,
        longitude = longitude,
        altitude = if (hasAltitude()) altitude else null,
        accuracy = if (hasAccuracy()) accuracy else null,
        bearing = if (hasBearing()) bearing else null,
        speed = if (hasSpeed()) speed else null,
        timestamp = time,
        provider = provider
    )
}

// ═══════════════════════════════════════════════════════════════
// DISTANCE CALCULATIONS
// ═══════════════════════════════════════════════════════════════

/**
 * Calculate distance between two locations in meters
 */
fun LocationData.distanceTo(other: LocationData): Float {
    val results = FloatArray(1)
    Location.distanceBetween(
        latitude, longitude,
        other.latitude, other.longitude,
        results
    )
    return results[0]
}

/**
 * Calculate bearing from this location to another
 */
fun LocationData.bearingTo(other: LocationData): Float {
    val results = FloatArray(2)
    Location.distanceBetween(
        latitude, longitude,
        other.latitude, other.longitude,
        results
    )
    return results[1]
}

/**
 * Format location for display
 */
fun LocationData.formatForDisplay(): String {
    val lat = "%.6f".format(latitude)
    val lon = "%.6f".format(longitude)
    val acc = accuracy?.let { " (±${it.toInt()}m)" } ?: ""
    return "$lat, $lon$acc"
}

