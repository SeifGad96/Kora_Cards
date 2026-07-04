# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep source file names and line numbers for readable crash traces.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# =============================================
# kotlinx.serialization
# =============================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.example.koracards.**$$serializer { *; }
-keepclassmembers class com.example.koracards.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.koracards.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# =============================================
# Ktor (WebSocket client/server)
# =============================================
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**
-keep class io.netty.** { *; }
-dontwarn io.netty.**

# =============================================
# Coil (image loading)
# =============================================
-keep class coil.** { *; }
-dontwarn coil.**

# =============================================
# Lottie (animations)
# =============================================
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# =============================================
# Hilt / Dagger
# =============================================
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-dontwarn dagger.hilt.**

# =============================================
# OkHttp (used by Ktor client)
# =============================================
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# =============================================
# Google Fonts provider
# =============================================
-keep class androidx.compose.ui.text.googlefonts.** { *; }
-dontwarn androidx.compose.ui.text.googlefonts.**