Here is the updated build.gradle.kts file modernized for current Android development standards.
I have applied several important updates:
 * Fixed SDK Versions: Replaced the invalid experimental compileSdk { ... } block with the standard compileSdk = 35 and updated targetSdk = 35.
 * Java 17 / Kotlin 2.0+ Compatibility: Upgraded Java compatibility from 11 to 17, and added the modern Kotlin compilerOptions block to ensure your JVM targets align. Java 17 is the current standard for modern Android and Compose.
 * KSP Declaration: Changed the string-based "ksp"(...) calls to the native Kotlin DSL ksp(...) function.
 * BOM Variables: Extracted the Firebase and Compose BOMs into variables so they correctly apply to both implementation and UI testing dependencies.
 * Release Build Optimization: Turned ON isMinifyEnabled and added isShrinkResources = true for the release build. Modern apps should always shrink and obfuscate release builds to reduce APK/Bundle size.
 * Dependency Grouping: Cleaned up and grouped your dependencies by category (Compose, Room, Firebase, Networking) to make the file highly readable.
Modernized build.gradle.kts
import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android) // Ensure Kotlin Android plugin is explicitly declared
    alias(libs.plugins.kotlin.compose) // Modern Compose Compiler (Kotlin 2.0+)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.secrets)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example" // Note: Consider matching this with your applicationId below
    compileSdk = 35 // Standardized modern syntax

    defaultConfig {
        applicationId = "com.ZALDI.deliveryapp.omoprt"
        minSdk = 24
        targetSdk = 35 // Updated to match compileSdk
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPS_API_KEY"] = ""
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_PATH") ?: "$rootDir/my-upload-key.jks"
            storeFile = file(keystorePath)
            storePassword = System.getenv("STORE_PASSWORD")
            keyAlias = "upload"
            keyPassword = System.getenv("KEY_PASSWORD")
        }
        getByName("debug") { // Replaced custom 'debugConfig' with standard 'debug' override
            storeFile = file("$rootDir/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            // Modern standard: Enable minification and resource shrinking for release
            isMinifyEnabled = true
            isShrinkResources = true 
            isCrunchPngs = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), 
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug { 
            signingConfig = signingConfigs.getByName("debug") 
        }
    }

    compileOptions {
        // Upgraded to Java 17 for modern Android compatibility
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions { 
        unitTests { isIncludeAndroidResources = true } 
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = true
    }
}

// Modern Kotlin block to align JVM target with Java 17
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

secrets {
    propertiesFileName = ".env"
    defaultPropertiesFileName = ".env.example"
    ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}

googleServices { 
    missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN 
}

dependencies {
    // --- BOMs (Bill of Materials) ---
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom) // Required for UI tests

    val firebaseBom = platform(libs.firebase.bom)
    implementation(firebaseBom)

    // --- Compose Core ---
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)

    // --- AndroidX & Lifecycle ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // --- Room Database ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler) // Modern KSP syntax

    // --- Networking & JSON (Moshi & Retrofit) ---
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen) // Modern KSP syntax

    // --- Firebase & Auth ---
    implementation(libs.firebase.ai)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.appcheck.recaptcha)
    implementation(libs.firebase.appcheck.debug)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)

    // --- Google Maps & Location ---
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)

    // --- Coroutines ---
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // --- Local Unit Tests ---
    testImplementation(libs.junit)
    testImplementation(libs.androidx.core)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.compose.ui.test.junit4)

    // --- Android UI Tests ---
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // --- Debug Tooling ---
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

