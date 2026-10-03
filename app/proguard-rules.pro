# Metron R8 & ProGuard Optimization Rules

# Keep Data Models for reflection and JSON serialization/deserialization
-keep class com.metron.app.model.** { *; }

# Keep AppWidget Providers and Glance Widget
-keep class com.metron.app.widget.** { *; }
-keep class androidx.glance.** { *; }
-dontwarn androidx.glance.**

# Keep Notification and Broadcast Receivers
-keep class com.metron.app.notification.** { *; }

# Keep Auto-Backup and OCR components
-keep class com.metron.app.backup.** { *; }
-keep class com.metron.app.ocr.** { *; }

# Keep ML Kit and Google Play Services
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.**

# Keep Room Database and Entities
-keep class androidx.room.** { *; }
-keep class com.metron.app.data.room.** { *; }
-dontwarn androidx.room.**

# Kotlin Serialization (if used)
-keepattributes *Annotation*, InnerClasses, SourceFile, LineNumberTable
-dontnote kotlinx.serialization.**
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Coroutines & StateFlow
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
