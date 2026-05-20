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
-keep class * extends dagger.hilt.android.internal.managers.ActivityComponentManager { *; }

# ViewModels
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class com.pbrockt.tagebuch.** {
    kotlinx.serialization.KSerializer serializer(...);
    *** Companion;
}
-keep,includedescriptorclasses class com.pbrockt.tagebuch.**$$serializer { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Coil
-dontwarn coil.**

# Google Error Prone / Tink (referenced by EncryptedSharedPreferences)
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

# General Android
-keepattributes SourceFile, LineNumberTable
-keep public class * extends java.lang.Exception
