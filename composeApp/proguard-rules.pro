# Add project specific ProGuard rules here.
-keepclassmembers class com.mediasaver.app.domain.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class retrofit2.** { *; }
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
