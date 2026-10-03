# Metron R8 & ProGuard Optimization Rules

# Keep Data Models for reflection and JSON serialization/deserialization
-keep class com.metron.app.model.** { *; }

# Keep AppWidget Provider
-keep class com.metron.app.widget.MetronWidgetProvider { *; }

# Keep Notification and Broadcast Receivers
-keep class com.metron.app.notification.** { *; }

# Kotlin Serialization (if used)
-keepattributes *Annotation*, InnerClasses
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
