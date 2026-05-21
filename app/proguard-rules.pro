# SQLCipher
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }

# Room
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# ViewModels
-keep class * extends androidx.lifecycle.ViewModel { *; }

# --- kotlinx.serialization ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Serialization runtime
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep generated $$serializer classes for ALL app model classes
-keep,includedescriptorclasses class com.pbrockt.tagebuch.**$$serializer { *; }

# Keep Companion objects and serializer() methods on all app data classes
-keepclassmembers @kotlinx.serialization.Serializable class com.pbrockt.tagebuch.** {
    *** Companion;
    static ** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep all @Serializable annotated classes and their members
-keepclasseswithmembers @kotlinx.serialization.Serializable class * {
    *;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Coil
-dontwarn coil.**

# Google Error Prone / Tink (used by EncryptedSharedPreferences)
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
-dontwarn org.checkerframework.**
-dontwarn com.google.crypto.tink.**

# WorkManager
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keepclassmembers class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Compose
-keep class androidx.compose.** { *; }

# BuildConfig
-keep class com.pbrockt.tagebuch.BuildConfig { *; }

# General
-keepattributes SourceFile, LineNumberTable
-keep public class * extends java.lang.Exception
