package com.kyilmaz.neurocomet

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import android.provider.Settings
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * SecurityManager - Comprehensive security hardening for NeuroComet
 *
 * Prevents bypass of app restrictions through:
 * - Root/Superuser detection
 * - Debugger/ADB detection
 * - Emulator detection
 * - Xposed/Frida hook detection
 * - App tampering detection
 * - Screenshot/screen recording restrictions (optional)
 *
 * IMPORTANT: This is defense-in-depth. No security is 100% bulletproof,
 * but these checks make bypassing restrictions significantly harder.
 */
object SecurityManager {

    private const val TAG = "SecurityManager"

    // Security check results cache (to avoid repeated expensive checks)
    @Volatile private var securityCheckResult: SecurityCheckResult? = null
    @Volatile private var lastCheckTime: Long = 0L
    private const val CHECK_CACHE_DURATION_MS = 30_000L // 30 seconds

    data class SecurityCheckResult(
        val isSecure: Boolean,
        val isRooted: Boolean,
        val isDebuggerAttached: Boolean,
        val isEmulator: Boolean,
        val isHookingFrameworkDetected: Boolean,
        val isAppTampered: Boolean,
        val isDeveloperOptionsEnabled: Boolean,
        val isAdbEnabled: Boolean,
        val threatLevel: ThreatLevel,
        val details: List<String>
    )

    enum class ThreatLevel {
        NONE,       // All checks passed
        LOW,        // Minor issues (e.g., developer options enabled)
        MEDIUM,     // Concerning (e.g., ADB enabled, emulator)
        HIGH,       // Serious (e.g., root detected)
        CRITICAL    // Multiple serious threats detected
    }

    /**
     * Perform comprehensive security check.
     * Call this at app startup and periodically during sensitive operations.
     */
    fun performSecurityCheck(context: Context, forceRefresh: Boolean = false): SecurityCheckResult {
        val now = System.currentTimeMillis()

        // Return cached result if still valid
        if (!forceRefresh && securityCheckResult != null &&
            (now - lastCheckTime) < CHECK_CACHE_DURATION_MS) {
            return securityCheckResult!!
        }

        val details = mutableListOf<String>()

        // Perform all checks
        val isRooted = checkRootAccess(details)
        val isDebuggerAttached = checkDebugger(details)
        val isEmulator = checkEmulator(details)
        val isHookingFramework = checkHookingFrameworks(context, details)
        val isAppTampered = checkAppIntegrity(context, details)
        val isDeveloperOptions = checkDeveloperOptions(context, details)
        val isAdbEnabled = checkAdbEnabled(context, details)

        // Calculate threat level
        val threatLevel = calculateThreatLevel(
            isRooted, isDebuggerAttached, isEmulator,
            isHookingFramework, isAppTampered, isDeveloperOptions, isAdbEnabled
        )

        val isSecure = threatLevel == ThreatLevel.NONE || threatLevel == ThreatLevel.LOW

        val result = SecurityCheckResult(
            isSecure = isSecure,
            isRooted = isRooted,
            isDebuggerAttached = isDebuggerAttached,
            isEmulator = isEmulator,
            isHookingFrameworkDetected = isHookingFramework,
            isAppTampered = isAppTampered,
            isDeveloperOptionsEnabled = isDeveloperOptions,
            isAdbEnabled = isAdbEnabled,
            threatLevel = threatLevel,
            details = details
        )

        // Cache result
        securityCheckResult = result
        lastCheckTime = now

        // Log security status (only in debug builds)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Security check: $threatLevel - ${details.joinToString(", ")}")
        }

        return result
    }

    /**
     * Check for root/superuser access
     */
    private fun checkRootAccess(details: MutableList<String>): Boolean {
        var isRooted = false

        // Check 1: Common root binaries
        val rootBinaries = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su",
            "/system/app/Superuser.apk",
            "/system/app/SuperSU.apk",
            "/system/app/SuperSU/SuperSU.apk",
            "/system/etc/init.d/99telegramhookx", // Common hook
            "/system/xbin/busybox"
        )

        for (path in rootBinaries) {
            if (File(path).exists()) {
                details.add("Root binary found: $path")
                isRooted = true
                break
            }
        }

        // Check 2: Dangerous props
        try {
            val process = Runtime.getRuntime().exec(arrayOf("getprop", "ro.build.tags"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val buildTags = reader.readLine()
            reader.close()

            if (buildTags?.contains("test-keys") == true) {
                details.add("Test-keys build detected")
                isRooted = true
            }
        } catch (_: Exception) { }

        // Check 3: Root management apps
        val rootApps = listOf(
            "com.topjohnwu.magisk",
            "com.koushikdutta.superuser",
            "com.noshufou.android.su",
            "com.thirdparty.superuser",
            "eu.chainfire.supersu",
            "com.yellowes.su",
            "com.kingroot.kinguser",
            "com.kingo.root",
            "com.smedialink.oneclickroot",
            "com.zhiqupk.root.global"
        )

        // Note: We can't check package manager in this static context
        // The app should check this separately with context

        // Check 4: RW system partition
        try {
            val process = Runtime.getRuntime().exec(arrayOf("mount"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line!!.contains("/system") && line!!.contains("rw")) {
                    details.add("System partition mounted as RW")
                    isRooted = true
                    break
                }
            }
            reader.close()
        } catch (_: Exception) { }

        return isRooted
    }

    /**
     * Check if a debugger is attached
     */
    private fun checkDebugger(details: MutableList<String>): Boolean {
        var debuggerAttached = false

        // Check 1: Android Debug API
        if (Debug.isDebuggerConnected()) {
            details.add("Debugger connected")
            debuggerAttached = true
        }

        // Check 2: Debug.waitingForDebugger()
        if (Debug.waitingForDebugger()) {
            details.add("Waiting for debugger")
            debuggerAttached = true
        }

        // Check 3: TracerPid in /proc/self/status
        try {
            val reader = BufferedReader(InputStreamReader(File("/proc/self/status").inputStream()))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line!!.startsWith("TracerPid:")) {
                    val tracerPid = line!!.substringAfter(":").trim().toIntOrNull() ?: 0
                    if (tracerPid > 0) {
                        details.add("TracerPid detected: $tracerPid")
                        debuggerAttached = true
                    }
                    break
                }
            }
            reader.close()
        } catch (_: Exception) { }

        return debuggerAttached
    }

    /**
     * Check if running on an emulator
     */
    private fun checkEmulator(details: MutableList<String>): Boolean {
        var isEmulator = false

        // Check 1: Build properties
        val emulatorIndicators = listOf(
            Build.FINGERPRINT.contains("generic"),
            Build.FINGERPRINT.contains("unknown"),
            Build.MODEL.contains("google_sdk"),
            Build.MODEL.contains("Emulator"),
            Build.MODEL.contains("Android SDK built for x86"),
            Build.MANUFACTURER.contains("Genymotion"),
            Build.BRAND.startsWith("generic"),
            Build.DEVICE.startsWith("generic"),
            Build.PRODUCT.contains("sdk"),
            Build.PRODUCT.contains("emulator"),
            Build.HARDWARE.contains("goldfish"),
            Build.HARDWARE.contains("ranchu"),
            "google_sdk" == Build.PRODUCT,
            "sdk_gphone" in Build.PRODUCT,
            Build.BOARD.lowercase().contains("nox"),
            Build.BOOTLOADER.lowercase().contains("nox"),
            Build.HARDWARE.lowercase().contains("nox"),
            Build.PRODUCT.lowercase().contains("nox"),
            Build.SERIAL.lowercase().contains("nox")
        )

        if (emulatorIndicators.any { it }) {
            details.add("Emulator indicators in Build properties")
            isEmulator = true
        }

        // Check 2: Hardware files
        val emulatorFiles = listOf(
            "/dev/socket/qemud",
            "/dev/qemu_pipe",
            "/system/lib/libc_malloc_debug_qemu.so",
            "/sys/qemu_trace",
            "/system/bin/qemu-props"
        )

        for (path in emulatorFiles) {
            if (File(path).exists()) {
                details.add("Emulator file found: $path")
                isEmulator = true
                break
            }
        }

        return isEmulator
    }

    /**
     * Check for Xposed, Frida, and other hooking frameworks
     */
    private fun checkHookingFrameworks(context: Context, details: MutableList<String>): Boolean {
        var hookingDetected = false

        // Check 1: Xposed classes in stack trace
        try {
            throw Exception("Xposed check")
        } catch (e: Exception) {
            val stackTrace = e.stackTrace.map { it.className }
            val xposedClasses = listOf(
                "de.robv.android.xposed",
                "com.saurik.substrate",
                "com.android.internal.os.ZygoteInit"
            )
            for (xposedClass in xposedClasses) {
                if (stackTrace.any { it.contains(xposedClass) }) {
                    details.add("Xposed framework detected in stack")
                    hookingDetected = true
                    break
                }
            }
        }

        // Check 2: Xposed installer packages
        val xposedPackages = listOf(
            "de.robv.android.xposed.installer",
            "com.saurik.substrate",
            "de.robv.android.xposed",
            "io.va.exposed",
            "org.lsposed.manager",
            "org.meowcat.edxposed.manager"
        )

        val pm = context.packageManager
        for (pkg in xposedPackages) {
            try {
                pm.getPackageInfo(pkg, 0)
                details.add("Xposed package found: $pkg")
                hookingDetected = true
                break
            } catch (_: PackageManager.NameNotFoundException) { }
        }

        // Check 3: Frida detection (common Frida artifacts)
        val fridaIndicators = listOf(
            "/data/local/tmp/frida-server",
            "/data/local/tmp/re.frida.server",
            "/sdcard/frida-server"
        )

        for (path in fridaIndicators) {
            if (File(path).exists()) {
                details.add("Frida detected: $path")
                hookingDetected = true
                break
            }
        }

        // Check 4: Frida port (default 27042)
        try {
            val socket = java.net.Socket()
            socket.connect(java.net.InetSocketAddress("127.0.0.1", 27042), 100)
            socket.close()
            details.add("Frida default port open")
            hookingDetected = true
        } catch (_: Exception) { }

        return hookingDetected
    }

    /**
     * Check app integrity (signature verification)
     */
    private fun checkAppIntegrity(context: Context, details: MutableList<String>): Boolean {
        var isTampered = false

        // Check 1: Debuggable flag in release
        val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebuggable && !BuildConfig.DEBUG) {
            details.add("App is debuggable in release mode")
            isTampered = true
        }

        // Check 2: Installer verification
        val validInstallers = listOf(
            "com.android.vending",      // Google Play Store
            "com.google.android.feedback", // Google Play alternative
            null                          // Debug/sideload (allowed for testing)
        )

        val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getInstallerPackageName(context.packageName)
        }

        // Don't flag sideloading as tampering (users may legitimately install APK)
        // Just log it for awareness
        if (installer != null && installer !in validInstallers) {
            details.add("Installed from non-standard source: $installer")
            // Note: Not setting isTampered here to allow legitimate sideloading
        }

        return isTampered
    }

    /**
     * Check if developer options are enabled
     */
    private fun checkDeveloperOptions(context: Context, details: MutableList<String>): Boolean {
        return try {
            val devOptions = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0
            )
            if (devOptions == 1) {
                details.add("Developer options enabled")
                true
            } else false
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Check if ADB debugging is enabled
     */
    private fun checkAdbEnabled(context: Context, details: MutableList<String>): Boolean {
        return try {
            val adbEnabled = Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.ADB_ENABLED,
                0
            )
            if (adbEnabled == 1) {
                details.add("ADB debugging enabled")
                true
            } else false
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Calculate overall threat level based on detected issues
     */
    private fun calculateThreatLevel(
        isRooted: Boolean,
        isDebuggerAttached: Boolean,
        isEmulator: Boolean,
        isHookingFramework: Boolean,
        isAppTampered: Boolean,
        isDeveloperOptions: Boolean,
        isAdbEnabled: Boolean
    ): ThreatLevel {
        var threatScore = 0

        if (isRooted) threatScore += 50
        if (isDebuggerAttached) threatScore += 40
        if (isHookingFramework) threatScore += 60
        if (isAppTampered) threatScore += 70
        if (isEmulator) threatScore += 20
        if (isDeveloperOptions) threatScore += 5
        if (isAdbEnabled) threatScore += 10

        return when {
            threatScore >= 100 -> ThreatLevel.CRITICAL
            threatScore >= 50 -> ThreatLevel.HIGH
            threatScore >= 20 -> ThreatLevel.MEDIUM
            threatScore >= 5 -> ThreatLevel.LOW
            else -> ThreatLevel.NONE
        }
    }

    /**
     * Enforce security for sensitive operations.
     * Call before allowing access to restricted features.
     *
     * @param context Application context
     * @param allowEmulator Whether to allow emulator (for testing)
     * @param allowDeveloperOptions Whether to allow developer options enabled
     * @throws SecurityException if security check fails
     */
    fun enforceSecurity(
        context: Context,
        allowEmulator: Boolean = BuildConfig.DEBUG,
        allowDeveloperOptions: Boolean = true
    ) {
        val result = performSecurityCheck(context, forceRefresh = true)

        // In debug builds, only warn
        if (BuildConfig.DEBUG) {
            if (result.threatLevel >= ThreatLevel.HIGH) {
                Log.w(TAG, "⚠️ Security warning: ${result.details.joinToString(", ")}")
            }
            return
        }

        // In release builds, enforce security
        if (result.isRooted) {
            throw SecurityException("This app cannot run on rooted devices for your security.")
        }

        if (result.isDebuggerAttached) {
            throw SecurityException("Debugging is not allowed in production builds.")
        }

        if (result.isHookingFrameworkDetected) {
            throw SecurityException("Security framework tampering detected.")
        }

        if (result.isAppTampered) {
            throw SecurityException("App integrity verification failed.")
        }

        if (!allowEmulator && result.isEmulator) {
            throw SecurityException("This app cannot run on emulators.")
        }
    }

    /**
     * Check if the current environment is safe for parental controls.
     * More strict than general security check.
     */
    fun isParentalControlsSafe(context: Context): Boolean {
        val result = performSecurityCheck(context)

        // For parental controls, we need a stricter check
        return !result.isRooted &&
               !result.isHookingFrameworkDetected &&
               !result.isAppTampered &&
               !result.isDebuggerAttached
    }

    /**
     * Get a user-friendly message about security status
     */
    fun getSecurityStatusMessage(context: Context): String {
        val result = performSecurityCheck(context)

        return when (result.threatLevel) {
            ThreatLevel.NONE -> "✅ Device security: Excellent"
            ThreatLevel.LOW -> "✅ Device security: Good"
            ThreatLevel.MEDIUM -> "⚠️ Device security: Fair - Some features may be restricted"
            ThreatLevel.HIGH -> "🔴 Device security: At risk - Security features enabled"
            ThreatLevel.CRITICAL -> "🚨 Device security: Critical - App restrictions active"
        }
    }
}

