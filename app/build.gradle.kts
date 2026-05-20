plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.pbrockt.tagebuch"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pbrockt.tagebuch"
        minSdk = 35
        targetSdk = 35
        versionCode = 2
        versionName = "0.1a"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val keystoreFile = file("tagebuch.keystore")
    signingConfigs {
        create("consistent") {
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = "tagebuch2024"
                keyAlias = "tagebuchkey"
                keyPassword = "tagebuch2024"
            }
        }
    }

    buildTypes {
        debug {
            if (keystoreFile.exists()) signingConfig = signingConfigs.getByName("consistent")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (keystoreFile.exists()) signingConfig = signingConfigs.getByName("consistent")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // APK filename includes version
    applicationVariants.all {
        val variant = this
        variant.outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                output.outputFileName = "Tagebuch-App-v${variant.versionName}.apk"
            }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Room + SQLCipher
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.android.database.sqlcipher)
    implementation(libs.androidx.sqlite.ktx)

    // Security
    implementation(libs.androidx.security.crypto)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // OkHttp (WebDAV)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Coil (Bilder)
    implementation(libs.coil.compose)

    // Serialization
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    debugImplementation(libs.androidx.ui.tooling)
}
