# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/.../Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# Retrofit & Gson rules
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class com.qfinopt.app.data.model.** { *; }
