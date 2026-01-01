# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ============================================================================
# NEURONET PRODUCTION PROGUARD RULES
# ============================================================================

# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep BuildConfig fields (needed for developer device hash check)
-keep class com.kyilmaz.neuronetworkingtitle.BuildConfig { *; }

# Keep data classes used by Supabase/Ktor serialization
-keep class com.kyilmaz.neuronetworkingtitle.** { *; }
-keepclassmembers class com.kyilmaz.neuronetworkingtitle.** {
    <fields>;
    <methods>;
}

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.kyilmaz.neuronetworkingtitle.**$$serializer { *; }
-keepclassmembers class com.kyilmaz.neuronetworkingtitle.** {
    *** Companion;
}
-keepclasseswithmembers class com.kyilmaz.neuronetworkingtitle.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Ktor
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { volatile <fields>; }
-keep class kotlin.reflect.jvm.internal.** { *; }
-dontwarn io.ktor.**

# Supabase
-keep class io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.**

# Coil
-dontwarn coil.**

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# RevenueCat
-keep class com.revenuecat.purchases.** { *; }
-dontwarn com.revenuecat.purchases.**

# Compose - keep composables from being stripped
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Prevent R8 from stripping interface implementations
-keep interface * { *; }

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Okhttp/OkIO (used by Ktor)
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# SLF4J
-dontwarn org.slf4j.**
