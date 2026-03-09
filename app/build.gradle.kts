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
}

// Load properties from multiple potential secret files
val combinedProperties = Properties()
listOf("local.properties", "secrets.properties").forEach { fileName ->
    val file = rootProject.file(fileName)
    if (file.exists()) {
        file.inputStream().use { combinedProperties.load(it) }
    }
}

/**
 * Basic obfuscation to hide keys from simple string searches in the binary.
 * Uses XOR with a key and hex encoding.
 * The key is read from local.properties (OBFUSCATION_KEY) so it never
 * appears in committed source.
 */
val obfuscationKey: String = combinedProperties.getProperty("OBFUSCATION_KEY")
    ?: "neurocomet_internal_security_key_2025"   // fallback so builds don't break

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
    compileSdkPreview = "CinnamonBun"

    defaultConfig {
        applicationId = "com.kyilmaz.neurocomet"
        minSdk = 26
        targetSdk = 36
        versionCode = 144
        versionName = "2.0.0-beta02"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Load credentials from combined properties
        val supabaseUrl = combinedProperties.getProperty("SUPABASE_URL") ?: ""
        val supabaseKey = combinedProperties.getProperty("SUPABASE_KEY") ?: ""
        val devHash = combinedProperties.getProperty("DEVELOPER_DEVICE_HASH") ?: ""
        val geminiApiKey = combinedProperties.getProperty("GEMINI_API_KEY") ?: ""
        val revenueCatKey = combinedProperties.getProperty("REVENUECAT_API_KEY") ?: ""
        val admobAppId = combinedProperties.getProperty("ADMOB_APP_ID") ?: "ca-app-pub-3940256099942544~3347511713"
        
        // AdMob Unit IDs
        val bannerAdId = combinedProperties.getProperty("ADMOB_BANNER_ID") ?: "ca-app-pub-3940256099942544/6300978111"
        val interstitialAdId = combinedProperties.getProperty("ADMOB_INTERSTITIAL_ID") ?: "ca-app-pub-3940256099942544/1033173712"
        val rewardedAdId = combinedProperties.getProperty("ADMOB_REWARDED_ID") ?: "ca-app-pub-3940256099942544/5224354917"

        // Inject obfuscated keys into BuildConfig
        buildConfigField("String", "SUPABASE_URL", "\"${obfuscate(supabaseUrl)}\"")
        buildConfigField("String", "SUPABASE_KEY", "\"${obfuscate(supabaseKey)}\"")
        buildConfigField("String", "GEMINI_API_KEY", "\"${obfuscate(geminiApiKey)}\"")
        buildConfigField("String", "REVENUECAT_API_KEY", "\"${obfuscate(revenueCatKey)}\"")
        buildConfigField("String", "ADMOB_BANNER_ID", "\"${obfuscate(bannerAdId)}\"")
        buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"${obfuscate(interstitialAdId)}\"")
        buildConfigField("String", "ADMOB_REWARDED_ID", "\"${obfuscate(rewardedAdId)}\"")
        
        // Non-secret but device-specific config
        buildConfigField("String", "DEVELOPER_DEVICE_HASH", "\"$devHash\"")
        buildConfigField("String", "ADMOB_APP_ID", "\"${obfuscate(admobAppId)}\"")

        // Obfuscation key — injected so SecurityUtils can decrypt at runtime
        buildConfigField("String", "OBFUSCATION_KEY", "\"$obfuscationKey\"")

        // Add AdMob App ID as a manifest placeholder
        manifestPlaceholders["admobAppId"] = admobAppId
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
            isDebuggable = false
            isCrunchPngs = true
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            isCrunchPngs = false
        }
    }

    splits {
        abi {
            isEnable = true
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

    lint {
        baseline = file("lint-baseline.xml")
        abortOnError = false
    }
}

composeCompiler {
    featureFlags = setOf(
        org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag.OptimizeNonSkippingGroups
    )
    stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("compose_stability.conf"))
}

val googleServicesConfigFiles = listOf(
    file("google-services.json"),
    file("src/main/google-services.json"),
    file("src/debug/google-services.json"),
    file("src/release/google-services.json")
)
val hasGoogleServicesConfig = googleServicesConfigFiles.any { it.exists() }
val requestedTasks = gradle.startParameter.taskNames.map { it.lowercase() }
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
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlin.reflect) // Required by Supabase SDK v3 typeOf() at runtime

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