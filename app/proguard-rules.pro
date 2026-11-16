# Keep Media3 classes
-keep class androidx.media3.** { *; }

# Keep OkHttp & Kotlinx Serialization
-keepattributes *Annotation*,Signature
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class kotlinx.serialization.** { *; }
