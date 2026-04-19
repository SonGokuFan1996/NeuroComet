@file:Suppress(
    "UnstableApiUsage",
    "AndroidSdkUpgrade",
    "AndroidSdkUpgradeAssistant"
)

import java.util.Properties
import org.gradle.api.GradleException

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hotswan.compiler)
}

// Load properties from multiple potential secret files
val combinedProperties = Properties()
listOf("local.properties", "secrets.properties").forEach { fileName ->
    val file = rootProject.file(fileName)
    if (file.exists()) {
        file.inputStream().use { combinedProperties.load(it) }
    }
}

val requestedTaskNames = gradle.startParameter.taskNames
val isBundleTaskRequested = requestedTaskNames.any { taskName ->
    taskName.contains("bundle", ignoreCase = true)
}
val isReleaseStyleBuildRequested = requestedTaskNames.any { taskName ->
    taskName.contains("release", ignoreCase = true) ||
        taskName.contains("bundle", ignoreCase = true) ||
        taskName.contains("publish", ignoreCase = true)
}

/**
 * Basic obfuscation to hide keys from simple string searches in the binary.
 * Uses XOR with a key and hex encoding.
 * The key is read from local.properties (OBFUSCATION_KEY) so it never
 * appears in committed source.
 */
val configuredObfuscationKey = combinedProperties.getProperty("OBFUSCATION_KEY")?.trim().orEmpty()
if (isReleaseStyleBuildRequested && configuredObfuscationKey.isBlank()) {
    throw GradleException(
        "OBFUSCATION_KEY is required for release-style builds. Add it to local.properties or secrets.properties before building."
    )
}
val obfuscationKey: String = configuredObfuscationKey.ifBlank {
    "debug-local-obfuscation-key"
}

fun obfuscate(value: String?): String {
    if (value.isNullOrEmpty()) return ""
    val bytes = value.toByteArray(Charsets.UTF_8)
    val result = StringBuilder()
    for (i in bytes.indices) {
        val obfuscatedByte = bytes[i].toInt() xor obfuscationKey[i % obfuscationKey.length].code
        result.append(String.format("%02x", obfuscatedByte and 0xFF))
    }
    return result.toString()
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "com.kyilmaz.neurocomet"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.kyilmaz.neurocomet"
        minSdk = 26
        targetSdk = 36
        versionCode = 192
        versionName = "2.0.0-rc36"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Load credentials from combined properties
        val supabaseUrl = combinedProperties.getProperty("SUPABASE_URL") ?: ""
        val supabaseKey = combinedProperties.getProperty("SUPABASE_KEY") ?: ""
        val devHash = combinedProperties.getProperty("DEVELOPER_DEVICE_HASH") ?: "4d18ac796abdb71814159e41a7e5fdd5b63b4ba659d3a5be66cea9ee8dcef1b3"
        val geminiApiKey = combinedProperties.getProperty("GEMINI_API_KEY") ?: ""
        val revenueCatKey = combinedProperties.getProperty("REVENUECAT_API_KEY") ?: ""
        val admobAppId = combinedProperties.getProperty("ADMOB_APP_ID") ?: "ca-app-pub-3940256099942544~3347511713"
        val turnUrl = combinedProperties.getProperty("TURN_URL") ?: ""
        val turnUsername = combinedProperties.getProperty("TURN_USERNAME") ?: ""
        val turnPassword = combinedProperties.getProperty("TURN_PASSWORD") ?: ""

        // AdMob Unit IDs
        val bannerAdId = combinedProperties.getProperty("ADMOB_BANNER_ID") ?: "ca-app-pub-3940256099942544/6300978111"
        val interstitialAdId = combinedProperties.getProperty("ADMOB_INTERSTITIAL_ID") ?: "ca-app-pub-3940256099942544/1033173712"
        val rewardedAdId = combinedProperties.getProperty("ADMOB_REWARDED_ID") ?: "ca-app-pub-3940256099942544/5224354917"

        val internalSignature = combinedProperties.getProperty("INTERNAL_SIGNATURE_HASH") ?: ""

        // Inject obfuscated keys into BuildConfig
        buildConfigField("String", "SUPABASE_URL", "\"${obfuscate(supabaseUrl)}\"")
        buildConfigField("String", "SUPABASE_KEY", "\"${obfuscate(supabaseKey)}\"")
        buildConfigField("String", "DEVELOPER_DEVICE_HASH", "\"$devHash\"")
        buildConfigField("String", "GEMINI_API_KEY", "\"${obfuscate(geminiApiKey)}\"")
        buildConfigField("String", "REVENUECAT_API_KEY", "\"${obfuscate(revenueCatKey)}\"")
        buildConfigField("String", "ADMOB_BANNER_ID", "\"${obfuscate(bannerAdId)}\"")
        buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"${obfuscate(interstitialAdId)}\"")
        buildConfigField("String", "ADMOB_REWARDED_ID", "\"${obfuscate(rewardedAdId)}\"")
        buildConfigField("String", "TURN_URL", "\"${obfuscate(turnUrl)}\"")
        buildConfigField("String", "TURN_USERNAME", "\"${obfuscate(turnUsername)}\"")
        buildConfigField("String", "TURN_PASSWORD", "\"${obfuscate(turnPassword)}\"")

        // Non-secret but device-specific config
        buildConfigField("String", "DEVELOPER_DEVICE_HASH", "\"$devHash\"")
        buildConfigField("String", "INTERNAL_SIGNATURE_HASH", "\"$internalSignature\"")
        buildConfigField("Boolean", "ALLOW_GUEST_ACCESS", "false")
        buildConfigField("String", "ADMOB_APP_ID", "\"${obfuscate(admobAppId)}\"")

        // Obfuscation key — injected so SecurityUtils can decrypt at runtime
        buildConfigField("String", "OBFUSCATION_KEY", "\"$obfuscationKey\"")

        // Add AdMob App ID as a manifest placeholder
        manifestPlaceholders["admobAppId"] = admobAppId

        ndk {
            debugSymbolLevel = "FULL"
        }
    }

    signingConfigs {
        create("release") {
            val storeFilePath = combinedProperties.getProperty("RELEASE_STORE_FILE") ?: ""
            if (storeFilePath.isNotBlank()) {
                storeFile = file(storeFilePath)
                storePassword = combinedProperties.getProperty("RELEASE_STORE_PASSWORD") ?: ""
                keyAlias = combinedProperties.getProperty("RELEASE_KEY_ALIAS") ?: ""
                keyPassword = combinedProperties.getProperty("RELEASE_KEY_PASSWORD") ?: ""
            }
        }
    }

    flavorDimensions.add("version")
    productFlavors {
        create("prod") {
            dimension = "version"
        }
        create("bypass") {
            dimension = "version"
            buildConfigField("Boolean", "ALLOW_GUEST_ACCESS", "true")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
            isDebuggable = false
            isCrunchPngs = true
        }
        debug {
            // Removed .debug suffix to match RevenueCat dashboard package name
            versionNameSuffix = "-debug"
            isDebuggable = true
            isMinifyEnabled = false
            isCrunchPngs = false
        }
    }

    splits {
        abi {
            // AAB handles ABI splitting natively — only enable for APK builds
            isEnable = !isBundleTaskRequested
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true
        }
    }


    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE*"
            excludes += "/META-INF/NOTICE*"
            excludes += "/*.properties"
            excludes += "/META-INF/versions/**"
            excludes += "/META-INF/proguard/**"
            excludes += "/META-INF/*.kotlin_module"
            excludes += "/kotlin/**"
            excludes += "/DebugProbesKt.bin"
        }
        // 16KB page size alignment for native libraries
        // Required for devices with 16KB memory pages (e.g., Pixel 9 and future devices)
        jniLibs {
            useLegacyPackaging = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    configurations.configureEach {
        resolutionStrategy {
            // androidx.test.ext:junit 1.3.0 needs concurrent-futures 1.2.0
            // but the Compose BOM pins it at 1.1.0 – pick the newer version.
            force("androidx.concurrent:concurrent-futures:1.2.0")
        }
    }

    lint {
        baseline = file("lint-baseline.xml")
        abortOnError = false
    }
}

composeCompiler {
    stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("compose_stability.conf"))
}

val googleServicesConfigFiles = listOf(
    file("google-services.json"),
    file("src/main/google-services.json"),
    file("src/debug/google-services.json"),
    file("src/release/google-services.json")
)
val hasGoogleServicesConfig = googleServicesConfigFiles.any { it.exists() }
val requestedTasks = requestedTaskNames.map { it.lowercase() }
val requiresGoogleServices = requestedTasks.any {
    it.contains("release") || it.contains("bundle")
}

if (hasGoogleServicesConfig) {
    apply(plugin = "com.google.gms.google-services")
} else if (requiresGoogleServices) {
    throw GradleException(
        "google-services.json is required for release-style builds. Add app/google-services.json or a variant-specific file under app/src/<variant>/google-services.json."
    )
} else {
    logger.lifecycle(
        "google-services.json not found; skipping Google Services plugin for this local/debug build. Firebase-backed features will be unavailable."
    )
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("io.github.kyant0:backdrop:2.0.0-alpha03")
    implementation(libs.google.material)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.biometric)

    implementation(libs.play.services.location)
    implementation(libs.play.services.ads)

    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)

    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.auth)
    implementation(libs.supabase.realtime)
    implementation(libs.supabase.storage)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.client.websockets)
    implementation(libs.kotlinx.serialization.json) // Required by Supabase SDK v3 typeOf() at runtime

    implementation(libs.revenuecat.purchases)

    implementation(libs.webrtc)

    // Baseline Profile support - improves startup and rendering speed via AOT compilation
    implementation(libs.androidx.profileinstaller)

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}