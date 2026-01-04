# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ============================================================================
# NEUROCOMET PRODUCTION PROGUARD RULES
# ============================================================================

# ============================================================================
# GENERAL OPTIMIZATION SETTINGS
# ============================================================================
# Optimize aggressively
-optimizationpasses 5
-allowaccessmodification
-dontpreverify
-repackageclasses ''

# Keep line numbers for crash reports (production debugging)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Remove logging in release builds for performance
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# ============================================================================
# APP SPECIFIC RULES
# ============================================================================
# Keep BuildConfig fields (needed for developer device hash check)
-keep class com.kyilmaz.neurocomet.BuildConfig { *; }

# Keep data classes used by Supabase/Ktor serialization (only serializable classes)
-keepclassmembers class com.kyilmaz.neurocomet.** {
    @kotlinx.serialization.Serializable <fields>;
}

# ============================================================================
# KOTLIN SERIALIZATION
# ============================================================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.kyilmaz.neurocomet.**$$serializer { *; }
-keepclassmembers class com.kyilmaz.neurocomet.** {
    *** Companion;
}
-keepclasseswithmembers class com.kyilmaz.neurocomet.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ============================================================================
# KTOR / NETWORKING
# ============================================================================
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { volatile <fields>; }
-dontwarn io.ktor.**

# Okhttp/OkIO (used by Ktor)
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# ============================================================================
# SUPABASE
# ============================================================================
-keep class io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.**

# ============================================================================
# FIREBASE
# ============================================================================
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ============================================================================
# REVENUECAT
# ============================================================================
-keep class com.revenuecat.purchases.** { *; }
-dontwarn com.revenuecat.purchases.**

# ============================================================================
# COMPOSE (Minimal rules - Compose handles most itself)
# ============================================================================
-dontwarn androidx.compose.**

# ============================================================================
# COIL (Image Loading)
# ============================================================================
-dontwarn coil.**

# ============================================================================
# WEBRTC
# ============================================================================
-keep class org.webrtc.** { *; }
-dontwarn org.webrtc.**

# ============================================================================
# KOTLIN REFLECTION (Keep minimal)
# ============================================================================
-keep class kotlin.reflect.jvm.internal.** { *; }
-dontwarn kotlin.reflect.jvm.internal.**

# ============================================================================
# ENUMS
# ============================================================================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ============================================================================
# PARCELABLE
# ============================================================================
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# ============================================================================
# MISC WARNINGS SUPPRESSION
# ============================================================================
-dontwarn org.slf4j.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ============================================================================
# SECURITY HARDENING
# ============================================================================
# Obfuscate aggressively - make reverse engineering harder
-obfuscationdictionary proguard-dictionary.txt
-classobfuscationdictionary proguard-dictionary.txt
-packageobfuscationdictionary proguard-dictionary.txt

# Keep security-critical classes but obfuscate their internals
-keep class com.kyilmaz.neurocomet.SecurityManager {
    public static *** performSecurityCheck(...);
    public static *** enforceSecurity(...);
    public static *** isParentalControlsSafe(...);
}

# Keep SubscriptionManager public API but obfuscate internals
-keep class com.kyilmaz.neurocomet.SubscriptionManager {
    public static *** verifyPremiumStatus();
    public static *** checkPremiumStatus(...);
}

# Encrypt string constants (R8 full mode)
# Note: R8 doesn't have built-in string encryption, but aggressive optimization helps

# Remove debug/verbose logging completely
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
}

# Prevent class name reflection attacks
-adaptclassstrings
-adaptresourcefilenames
-adaptresourcefilecontents

# Hide security-related method names
-keepclassmembernames class com.kyilmaz.neurocomet.SecurityManager {
    private *** check*(...);
}

# Anti-tampering: Keep integrity check methods
-keepclassmembers class com.kyilmaz.neurocomet.SecurityManager$SecurityCheckResult {
    <fields>;
}

# Prevent serialization vulnerabilities
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}


