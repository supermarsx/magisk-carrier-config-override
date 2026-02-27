# Proguard rules for CarrierConfig Override app

# Keep application class
-keep class com.supermarsx.carrierconfig.CarrierConfigApplication { *; }

# Keep data classes for JSON serialization
-keep class com.supermarsx.carrierconfig.data.model.** { *; }
-keepclassmembers class com.supermarsx.carrierconfig.data.model.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# LibSU
-keep class com.topjohnwu.superuser.** { *; }
-keep class androidx.databinding.ViewDataBinding { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel { *; }

# Compose
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.animation.AndroidFlingSpline

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Parcelables
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# Keep view constructors
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

# Debugging rules (remove/comment out for release)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
