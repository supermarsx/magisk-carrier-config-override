# Add project specific ProGuard rules here.

# Xposed API
-keep class de.robv.android.xposed.** { *; }
-keep class com.supermarsx.cco.xposed.CCOXposedModule { *; }

# Keep hook classes
-keep class com.supermarsx.cco.xposed.hooks.** { *; }

# Keep utility classes
-keep class com.supermarsx.cco.xposed.utils.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class com.supermarsx.cco.xposed.utils.ConfigManager$* { *; }

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# Don't warn about missing classes
-dontwarn de.robv.android.xposed.**
