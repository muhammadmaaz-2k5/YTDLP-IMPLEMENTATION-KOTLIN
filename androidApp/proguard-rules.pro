# ProGuard rules for androidApp
-keepclassmembers class com.mediasaver.app.domain.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
