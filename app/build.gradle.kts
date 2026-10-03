plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
}

import java.util.Properties

val localProperties = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        localFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.metron.app"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.codesorcerer.metron"
        minSdk = 26
        targetSdk = 36
        versionCode = 3
        versionName = "1.1.0"
    }

    signingConfigs {
        create("release") {
            val keystoreFile = file("metron-release-key.jks")
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = localProperties.getProperty("metron.keystore.password") ?: "metron2026release"
                keyAlias = localProperties.getProperty("metron.key.alias") ?: "metron-release"
                keyPassword = localProperties.getProperty("metron.key.password") ?: "metron2026release"
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
                enableV4Signing = true
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      compose = true
      aidl = false
      buildConfig = false
      shaders = false
    }

    packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
    }

    testOptions {
      unitTests.isReturnDefaultValues = true
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)
  // Instrumented tests
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Local tests: jUnit, coroutines, Android runner, JSON
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation("org.json:json:20240303")

  // Instrumented tests: jUnit rules and runners
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)

  // Navigation
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)

  // Icons Extended
  implementation(libs.androidx.compose.material.icons.extended)

  // ML Kit Offline Text Recognition for Receipts
  implementation("com.google.mlkit:text-recognition:16.0.1")

  // Jetpack Glance for modern Compose AppWidget
  implementation("androidx.glance:glance-appwidget:1.1.1")
  implementation("androidx.glance:glance-material3:1.1.1")

  // Room Database
  implementation("androidx.room:room-runtime:2.6.1")
  implementation("androidx.room:room-ktx:2.6.1")
}
