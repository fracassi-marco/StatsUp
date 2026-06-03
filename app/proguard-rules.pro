# Keep line numbers for readable crash stack traces
-keepattributes SourceFile,LineNumberTable,Signature
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
-keepclassmembers class com.statsup.domain.** { *; }
-keepclassmembers class com.statsup.infrastructure.** { *; }

# AppAuth
-keep class net.openid.appauth.** { *; }
-dontwarn net.openid.appauth.**

# Google Maps / Play Services
-keep class com.google.android.gms.maps.** { *; }
-dontwarn com.google.android.gms.**

# Lottie
-dontwarn com.airbnb.lottie.**
