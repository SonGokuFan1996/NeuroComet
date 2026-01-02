@file:Suppress(
    "UnstableApiUsage",
    "AndroidSdkUpgrade",
    "AndroidSdkUpgradeAssistant"
)

import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Load local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
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
        versionCode = 113
        versionName = "1.0.0-beta13"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Load Supabase credentials from local.properties
        val supabaseUrl = localProperties.getProperty("SUPABASE_URL") ?: ""
        val supabaseKey = localProperties.getProperty("SUPABASE_KEY") ?: ""
        val devHash = localProperties.getProperty("DEVELOPER_DEVICE_HASH") ?: ""
        val geminiApiKey = localProperties.getProperty("GEMINI_API_KEY") ?: ""
        val revenueCatKey = localProperties.getProperty("REVENUECAT_API_KEY") ?: ""

        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_KEY", "\"$supabaseKey\"")
        // Developer device hash for allowing dev options on your personal device
        // Set this in local.properties: DEVELOPER_DEVICE_HASH=your_sha256_hash_here
        // To get your hash: run the app in debug, try dev options, check logcat for "DEV_ACCESS"
        buildConfigField("String", "DEVELOPER_DEVICE_HASH", "\"$devHash\"")
        // Gemini API key for AI-powered testing features
        // Set this in local.properties: GEMINI_API_KEY=your_api_key_here
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
        // RevenueCat API key for in-app purchases
        // Set this in local.properties: REVENUECAT_API_KEY=your_api_key_here
        buildConfigField("String", "REVENUECAT_API_KEY", "\"$revenueCatKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Additional optimizations for release
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
            // Disable minification in debug for faster builds
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    // Enable build optimizations
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE*"
            excludes += "/META-INF/NOTICE*"
            excludes += "/*.properties"
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

    // Add Lint baseline configuration
    lint {
        baseline = file("lint-baseline.xml")
        abortOnError = false // Keep the build from failing on lint errors temporarily
    }
}

dependencies {
    // Keep version-catalog libs that resolve, but use stable coordinates for ones that don't.
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("com.google.android.material:material:1.12.0")


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

    implementation(libs.revenuecat.purchases)

    // WebRTC for voice/video calls
    implementation(libs.webrtc)

    // OkHttp is used directly for Gemini API calls (avoids Ktor version conflicts)
    // The generative-ai SDK has Ktor version conflicts with Supabase

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}