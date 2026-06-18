# Keep line numbers for readable crash stack traces
-keepattributes SourceFile,LineNumberTable,Signature,*Annotation*
-renamesourcefileattribute SourceFile

# Room - keep entity/DAO classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keepclassmembers @androidx.room.Entity class * { *; }

# Jackson - keep all model classes used for serialization/deserialization
-keep class com.fasterxml.jackson.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.**
-keep @com.fasterxml.jackson.annotation.JsonIgnoreProperties class * { *; }
-keepclassmembers class * {
    @com.fasterxml.jackson.annotation.JsonProperty *;
    @com.fasterxml.jackson.annotation.JsonCreator *;
}

# Kotlin data classes used with Jackson / Room
-keep class com.statsup.domain.** { *; }
-keep class com.statsup.infrastructure.** { *; }

# Keep all ViewModel subclasses (used by Compose viewModel() – class identity must be stable)
-keep class * extends androidx.lifecycle.ViewModel { *; }

# AppAuth
-keep class net.openid.appauth.** { *; }
-dontwarn net.openid.appauth.**

# Google Maps / Play Services — com.google.android.gms namespace
-keep class com.google.android.gms.maps.** { *; }
-dontwarn com.google.android.gms.**

# Google Maps Android Utils and maps-compose (com.google.maps.android namespace — distinct from gms)
-keep class com.google.maps.android.** { *; }
-dontwarn com.google.maps.android.**

# Lottie
-dontwarn com.airbnb.lottie.**

# Topinambur HTTP client (no consumer proguard rules)
-keep class topinambur.** { *; }
-dontwarn topinambur.**

# JetChart (no consumer proguard rules)
-keep class com.github.fracassi.** { *; }
-dontwarn com.github.fracassi.**
