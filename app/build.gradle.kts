plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.ksp)
  alias(libs.plugins.kotlinx.serialization)
}

android {
  namespace = "com.example.timeroutetracker"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.example.timeroutetracker"
    minSdk = 29
    //noinspection OldTargetApi
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      // 启用代码混淆
      isMinifyEnabled = true
      // 启用资源压缩
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions {
    jvmTarget = "1.8"
  }
  buildFeatures {
    compose = true
  }
}

dependencies {
  implementation(kotlin("reflect"))
  testImplementation(kotlin("test"))
  androidTestImplementation(kotlin("test"))
  testImplementation(libs.kotlin.test.junit)
  implementation(libs.kotlinx.serialization.json)

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.ui.tooling.preview)
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.material3)
  implementation(libs.androidx.material.icons.extended)
  implementation(libs.core.ktx)

  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.ui.test.junit4)

  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.runner)
  androidTestImplementation(libs.androidx.rules)
  debugImplementation(libs.ui.tooling)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)

  // Room
  implementation(libs.room.common)
  implementation(libs.room.runtime)
  annotationProcessor(libs.room.compiler)
//  ksp(libs.room.compiler)
  implementation(libs.room.ktx)
  testImplementation(libs.room.testing)

  implementation(libs.composeSettings.ui)
  implementation(libs.composeSettings.ui.extended)
}