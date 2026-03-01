package com.kyilmaz.neurocomet

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.util.Log

/**
 * Robust launcher detection utility.
 * Provides comprehensive detection of the current launcher and its capabilities.
 */
object LauncherDetector {

    private const val TAG = "LauncherDetector"

    /**
     * Enum representing known launcher types with their specific capabilities and package patterns.
     */
    enum class LauncherType(
        val displayName: String,
        val packagePatterns: List<String>,
        val supportsPinnedShortcuts: Boolean,
        val supportsLegacyBroadcast: Boolean,
        val customShortcutAction: String? = null,
        val requiresConfirmation: Boolean = true
    ) {
        PIXEL_LAUNCHER(
            displayName = "Pixel Launcher",
            packagePatterns = listOf("com.google.android.apps.nexuslauncher", "com.android.launcher3"),
            supportsPinnedShortcuts = true,
            supportsLegacyBroadcast = false,
            requiresConfirmation = true
        ),
        SAMSUNG_ONE_UI(
            displayName = "Samsung One UI",
            packagePatterns = listOf("com.sec.android.app.launcher", "com.samsung.android.launcher"),
            supportsPinnedShortcuts = true,
            supportsLegacyBroadcast = true,
            customShortcutAction = "com.android.launcher.action.INSTALL_SHORTCUT",
            requiresConfirmation = true
        ),
        NOVA_LAUNCHER(
            displayName = "Nova Launcher",
            packagePatterns = listOf("com.teslacoilsw.launcher", "com.teslacoilsw.launcher.prime"),
            supportsPinnedShortcuts = true,
            supportsLegacyBroadcast = true,
            customShortcutAction = "com.teslacoilsw.launcher.INSTALL_SHORTCUT",
            requiresConfirmation = false
        ),
        LAWNCHAIR(
            displayName = "Lawnchair",
            packagePatterns = listOf("ch.deletescape.lawnchair", "app.lawnchair"),
            supportsPinnedShortcuts = true,
            supportsLegacyBroadcast = true,
            requiresConfirmation = true
        ),
        MICROSOFT_LAUNCHER(
            displayName = "Microsoft Launcher",
            packagePatterns = listOf("com.microsoft.launcher"),
            supportsPinnedShortcuts = true,
            supportsLegacyBroadcast = true,
            requiresConfirmation = true
        ),
        ACTION_LAUNCHER(
            displayName = "Action Launcher",
            packagePatterns = listOf("com.actionlauncher.playstore", "com.chrislacy.actionlauncher.pro"),
            supportsPinnedShortcuts = true,
            supportsLegacyBroadcast = true,
            requiresConfirmation = false
        ),
        MIUI_LAUNCHER(
            displayName = "MIUI Launcher",
            packagePatterns = listOf("com.miui.home", "com.mi.android.globallauncher"),
            supportsPinnedShortcuts = true,
            supportsLegacyBroadcast = true,
            customShortcutAction = "com.miui.home.launcher.action.INSTALL_SHORTCUT",
            requiresConfirmation = true
        ),
        POCO_LAUNCHER(
            displayName = "POCO Launcher",
            packagePatterns = listOf("com.mi.android.globallauncher", "com.poco.launcher"),
            supportsPinnedShortcuts = true,
            supportsLegacyBroadcast = true,
            customShortcutAction = "com.miui.home.launcher.action.INSTALL_SHORTCUT",
            requiresConfirmation = true
        ),
        OPPO_LAUNCHER(
            displayName = "OPPO/ColorOS Launcher",
            packagePatterns = listOf("com.oppo.launcher", "com.coloros.launcher"),
            supportsPinnedShortcuts = true,
            supportsLegacyBroadcast = true,
            customShortcutAction = "com.oppo.launcher.action.INSTALL_SHORTCUT",
            requiresConfirmation = true
        ),
        REALME_LAUNCHER(
            displayName = "Realme Launcher",
            packagePatterns = listOf("com.realme.launcher"),
            supportsPinnedShortcuts = true,
            supportsLegacyBroadcast = true,
            requiresConfirmation = true
        ),
        HUAWEI_LAUNCHER(
            displayName = "Huawei/EMUI Launcher",
            packagePatterns = listOf("com.huawei.android.launcher", "com.huawei.home"),
            supportsPinnedShortcuts = true,
            supportsLegacyBroadcast = true,
            customShortcutAction = "com.huawei.android.launcher.action.INSTALL_SHORTCUT",
            requiresConfirmation = true
        ),
        HONOR_LAUNCHER(
            displayName = "Honor Launcher",
            packagePatterns = listOf("com.hihonor.android.launcher"),
            supportsPinnedShortcuts = true,
            supportsLegacyBroadcast = true,
            requiresConfirmation = true
        ),
        ONEPLUS_LAUNCHER(
            displayName = "OnePlus Launcher",
            packagePatterns = listOf("net.oneplus.launcher", "com.oneplus.hydrogen.launcher"),
            supportsPinnedShortcuts = true,
            supportsLegacyBroadcast = true,
            requiresConfirmation = true
        ),
        VIVO_LAUNCHER(
            displayName = "Vivo/FuntouchOS Launcher",
            packagePatterns = listOf("com.bbk.launcher2", "com.vivo.launcher"),
            supportsPinnedShortcuts = true,
            supportsLegacyBroadcast = true,
            requiresConfirmation = true
        ),
        ASUS_LAUNCHER(
            displayName = "ASUS ZenUI Launcher",
            packagePatterns = listOf("com.asus.launcher"),
            supportsPinnedShortcuts = true,
            supportsLegacyBroadcast = true,
            requiresConfirmation = true
        ),
        MOTOROLA_LAUNCHER(
            displayName = "Motorola Launcher",
            packagePatterns = listOf("com.motorola.launcher3"),
            supportsPinnedShortcuts = true,
            supportsLegacyBroadcast = true,
            requiresConfirmation = true
        ),
        NOTHING_LAUNCHER(
            displayName = "Nothing Launcher",
            packagePatterns = listOf("com.nothing.launcher"),
            supportsPinnedShortcuts = true,
            supportsLegacyBroadcast = false,
            requiresConfirmation = true
        ),
        NIAGARA_LAUNCHER(
            displayName = "Niagara Launcher",
            packagePatterns = listOf("bitpit.launcher"),
            supportsPinnedShortcuts = true,
            supportsLegacyBroadcast = false,
            requiresConfirmation = true
        ),
        SMART_LAUNCHER(
            displayName = "Smart Launcher",
            packagePatterns = listOf("ginlemon.flowerfree", "ginlemon.flowerpro"),
            supportsPinnedShortcuts = true,
            supportsLegacyBroadcast = true,
            requiresConfirmation = true
        ),
        APEX_LAUNCHER(
            displayName = "Apex Launcher",
            packagePatterns = listOf("com.anddoes.launcher", "com.anddoes.launcher.pro"),
            supportsPinnedShortcuts = true,
            supportsLegacyBroadcast = true,
            requiresConfirmation = false
        ),
        GO_LAUNCHER(
            displayName = "GO Launcher",
            packagePatterns = listOf("com.gau.go.launcherex"),
            supportsPinnedShortcuts = false,
            supportsLegacyBroadcast = true,
            requiresConfirmation = false
        ),
        EVIE_LAUNCHER(
            displayName = "Evie Launcher",
            packagePatterns = listOf("is.shortcut"),
            supportsPinnedShortcuts = true,
            supportsLegacyBroadcast = true,
            requiresConfirmation = true
        ),
        HYPERION_LAUNCHER(
            displayName = "Hyperion Launcher",
            packagePatterns = listOf("projekt.starter"),
            supportsPinnedShortcuts = true,
            supportsLegacyBroadcast = true,
            requiresConfirmation = true
        ),
        UNKNOWN(
            displayName = "Unknown Launcher",
            packagePatterns = emptyList(),
            supportsPinnedShortcuts = true, // Assume modern API support
            supportsLegacyBroadcast = true,
            requiresConfirmation = true
        )
    }

    /**
     * Data class containing detailed launcher information.
     */
    data class LauncherInfo(
        val packageName: String,
        val launcherType: LauncherType,
        val appLabel: String?,
        val versionName: String?,
        val versionCode: Long,
        val supportsPinnedShortcuts: Boolean,
        val supportsLegacyBroadcast: Boolean,
        val customShortcutAction: String?,
        val requiresConfirmation: Boolean
    ) {
        /**
         * Get a tip for shortcut creation based on the launcher type.
         */
        fun getShortcutTip(): String {
            return when (launcherType) {
                LauncherType.PIXEL_LAUNCHER -> "Confirm the popup to add to home screen."
                LauncherType.SAMSUNG_ONE_UI -> "Check your home screen or notification."
                LauncherType.NOVA_LAUNCHER -> "Nova Launcher will add the shortcut directly."
                LauncherType.LAWNCHAIR -> "Confirm the popup to add the shortcut."
                LauncherType.MICROSOFT_LAUNCHER -> "Check your home screen."
                LauncherType.ACTION_LAUNCHER -> "Action Launcher will add the shortcut."
                LauncherType.MIUI_LAUNCHER, LauncherType.POCO_LAUNCHER -> "Check your home screen. May require permission in Settings."
                LauncherType.OPPO_LAUNCHER, LauncherType.REALME_LAUNCHER -> "Check your home screen."
                LauncherType.HUAWEI_LAUNCHER, LauncherType.HONOR_LAUNCHER -> "Check your home screen."
                LauncherType.ONEPLUS_LAUNCHER -> "Check your home screen."
                LauncherType.VIVO_LAUNCHER -> "Check your home screen."
                LauncherType.NOTHING_LAUNCHER -> "Confirm to add the shortcut."
                LauncherType.NIAGARA_LAUNCHER -> "Check your Niagara home screen."
                else -> "Check your home screen or notification shade."
            }
        }

        /**
         * Get a tip for icon change based on the launcher type.
         * Icon changes via activity-alias may take time to refresh depending on the launcher.
         */
        fun getIconChangeTip(): String {
            return when (launcherType) {
                LauncherType.PIXEL_LAUNCHER -> "Icon may take 5-10 seconds to update. If not, restart Pixel Launcher."
                LauncherType.SAMSUNG_ONE_UI -> "Icon should update shortly. If not, clear launcher cache in Settings."
                LauncherType.NOVA_LAUNCHER -> "Nova caches icons. Go to Nova Settings → Look & Feel → Rebuild Icon Cache."
                LauncherType.LAWNCHAIR -> "Restart Lawnchair from settings to see the new icon."
                LauncherType.MICROSOFT_LAUNCHER -> "Icon should update automatically within seconds."
                LauncherType.ACTION_LAUNCHER -> "Icon should update. If not, restart Action Launcher."
                LauncherType.MIUI_LAUNCHER, LauncherType.POCO_LAUNCHER -> "Icon may take a moment. Try clearing launcher cache in Settings."
                LauncherType.OPPO_LAUNCHER, LauncherType.REALME_LAUNCHER -> "Clear launcher cache in Settings to refresh the icon."
                LauncherType.HUAWEI_LAUNCHER, LauncherType.HONOR_LAUNCHER -> "Restart the launcher or device to see the new icon."
                LauncherType.ONEPLUS_LAUNCHER -> "Icon should update. If not, restart the launcher."
                LauncherType.VIVO_LAUNCHER -> "Restart your device to ensure the icon updates."
                LauncherType.NOTHING_LAUNCHER -> "Restart Nothing Launcher to refresh icons."
                LauncherType.NIAGARA_LAUNCHER -> "Niagara should update the icon automatically."
                LauncherType.SMART_LAUNCHER -> "Smart Launcher may need a restart to show the new icon."
                LauncherType.APEX_LAUNCHER -> "Apex Launcher should update the icon shortly."
                else -> "Icon should update within a few seconds. If not, restart your launcher."
            }
        }
    }

    /**
     * Detect the current default launcher and return detailed information.
     */
    fun detectLauncher(context: Context): LauncherInfo {
        val packageName = getDefaultLauncherPackage(context) ?: "unknown"
        val launcherType = identifyLauncherType(packageName)

        // Get additional package info
        val (appLabel, versionName, versionCode) = getPackageDetails(context, packageName)

        // Check actual pinned shortcut support on this device
        val actualPinnedSupport = checkPinnedShortcutSupport(context)

        Log.d(TAG, "Detected launcher: $packageName")
        Log.d(TAG, "Launcher type: ${launcherType.displayName}")
        Log.d(TAG, "App label: $appLabel, Version: $versionName ($versionCode)")
        Log.d(TAG, "Pinned shortcuts supported: $actualPinnedSupport")

        return LauncherInfo(
            packageName = packageName,
            launcherType = launcherType,
            appLabel = appLabel,
            versionName = versionName,
            versionCode = versionCode,
            supportsPinnedShortcuts = actualPinnedSupport && launcherType.supportsPinnedShortcuts,
            supportsLegacyBroadcast = launcherType.supportsLegacyBroadcast,
            customShortcutAction = launcherType.customShortcutAction,
            requiresConfirmation = launcherType.requiresConfirmation
        )
    }

    /**
     * Get the package name of the default launcher.
     */
    fun getDefaultLauncherPackage(context: Context): String? {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }

            val resolveInfo: ResolveInfo? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.resolveActivity(
                    intent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            }

            resolveInfo?.activityInfo?.packageName
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting launcher", e)
            null
        }
    }

    /**
     * Identify the launcher type from package name.
     */
    private fun identifyLauncherType(packageName: String): LauncherType {
        val lowerPackage = packageName.lowercase()

        return LauncherType.entries.find { type ->
            type.packagePatterns.any { pattern ->
                lowerPackage.contains(pattern.lowercase()) || pattern.lowercase().contains(lowerPackage)
            }
        } ?: LauncherType.UNKNOWN
    }

    /**
     * Get package details (label, version) for the launcher.
     */
    private fun getPackageDetails(context: Context, packageName: String): Triple<String?, String?, Long> {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(packageName, 0)
            }

            val appLabel = packageInfo.applicationInfo?.let {
                context.packageManager.getApplicationLabel(it).toString()
            }
            val versionName = packageInfo.versionName
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }

            Triple(appLabel, versionName, versionCode)
        } catch (e: Exception) {
            Log.w(TAG, "Could not get package details for $packageName", e)
            Triple(null, null, 0L)
        }
    }

    /**
     * Check if the system actually supports pinned shortcuts.
     */
    private fun checkPinnedShortcutSupport(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return false
        }

        return try {
            val shortcutManager = context.getSystemService(android.content.pm.ShortcutManager::class.java)
            shortcutManager?.isRequestPinShortcutSupported ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking pinned shortcut support", e)
            false
        }
    }

    /**
     * Get all available launchers on the device.
     */
    fun getAvailableLaunchers(context: Context): List<LauncherInfo> {
        val launchers = mutableListOf<LauncherInfo>()

        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }

            val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.queryIntentActivities(
                    intent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            }

            for (info in resolveInfos) {
                val packageName = info.activityInfo.packageName
                val launcherType = identifyLauncherType(packageName)
                val (appLabel, versionName, versionCode) = getPackageDetails(context, packageName)

                launchers.add(
                    LauncherInfo(
                        packageName = packageName,
                        launcherType = launcherType,
                        appLabel = appLabel,
                        versionName = versionName,
                        versionCode = versionCode,
                        supportsPinnedShortcuts = launcherType.supportsPinnedShortcuts,
                        supportsLegacyBroadcast = launcherType.supportsLegacyBroadcast,
                        customShortcutAction = launcherType.customShortcutAction,
                        requiresConfirmation = launcherType.requiresConfirmation
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting available launchers", e)
        }

        return launchers
    }

    /**
     * Log detailed launcher diagnostics for debugging.
     */
    fun logDiagnostics(context: Context) {
        Log.d(TAG, "=== Launcher Diagnostics ===")
        Log.d(TAG, "Android SDK: ${Build.VERSION.SDK_INT}")
        Log.d(TAG, "Device: ${Build.MANUFACTURER} ${Build.MODEL}")

        val currentLauncher = detectLauncher(context)
        Log.d(TAG, "Current launcher: ${currentLauncher.packageName}")
        Log.d(TAG, "Type: ${currentLauncher.launcherType.displayName}")
        Log.d(TAG, "Supports pinned shortcuts: ${currentLauncher.supportsPinnedShortcuts}")
        Log.d(TAG, "Supports legacy broadcast: ${currentLauncher.supportsLegacyBroadcast}")
        Log.d(TAG, "Custom action: ${currentLauncher.customShortcutAction ?: "none"}")

        val allLaunchers = getAvailableLaunchers(context)
        Log.d(TAG, "Available launchers (${allLaunchers.size}):")
        allLaunchers.forEach { launcher ->
            Log.d(TAG, "  - ${launcher.appLabel ?: launcher.packageName} (${launcher.launcherType.displayName})")
        }
        Log.d(TAG, "=== End Diagnostics ===")
    }
}

