# Add project-specific ProGuard rules here.
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line number information for debugging stack traces (optional)
-keepattributes SourceFile,LineNumberTable

# Keep Retrofit and Gson classes
-keep class com.example.localweatherstation.** { *; }
-keep class retrofit2.** { *; }
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep fields annotated with @SerializedName
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Compose-related classes
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep Coil classes
-keep class io.coil.** { *; }
-dontwarn io.coil.**

# Keep Coroutines classes
-keep class org.jetbrains.kotlinx.** { *; }
-dontwarn org.jetbrains.kotlinx.**

# Keep Google Play Services Location classes
-keep class com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.location.**

# Keep AppCompat classes
-keep class androidx.appcompat.** { *; }
-dontwarn androidx.appcompat.**

# Preserve annotated classes and methods
-keepattributes *Annotation*

# Prevent obfuscation of native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Preserve enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep ThreeTen classes (for org.threeten.bp)
-keep class org.threeten.bp.** { *; }
-dontwarn org.threeten.bp.**

# Keep Accompanist SwipeRefresh classes
-keep class com.google.accompanist.swiperefresh.** { *; }
-dontwarn com.google.accompanist.swiperefresh.**

# Keep Navigation Compose classes
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**