import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose) // Keeps Jetpack Compose compiler active
}

android {
    namespace = "com.example.calibre_zenith"
    compileSdk = 36 // Correct compileSdk target structure

    defaultConfig {
        applicationId = "com.example.calibre_zenith"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // =================================================================
        // SECURE API INJECTION: Reads GEMINI_API_KEY from local.properties
        // =================================================================
        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { stream ->
                properties.load(stream)
            }
        }

        // Read the key. If it doesn't exist, fallback to an empty string wrapper
        val geminiApiKey = properties.getProperty("GEMINI_API_KEY") ?: "\"\""

        // Inject it as a dynamic field into the auto-generated BuildConfig class
        buildConfigField("String", "GEMINI_API_KEY", geminiApiKey)
        // =================================================================
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Standardized DSL property for minification/obfuscation
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        // Enforces Android Studio to auto-generate the BuildConfig class file
        buildConfig = true
    }
}

// =================================================================
// MODERN KOTLIN COMPILER OPTIONS:
// Migrated from deprecated kotlinOptions to type-safe compilerOptions.
// Bypasses the Kotlin DSL warning in modern Kotlin Gradle plugins.
// =================================================================
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    // Platform & Core Compose Architecture
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.compose.material:material-icons-extended")

    // THE GEMINI AI ENGINE INTEGRATION
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // Local Test Environment Suites
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}