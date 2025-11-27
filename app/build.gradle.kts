plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("kapt")
}

android {
    namespace = "com.example.personaltaskapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.personaltaskapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures { compose = true }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    kotlinOptions { jvmTarget = "17" }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))

    // Compose UI
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Material Icons (fix FitnessCenter)
    implementation(libs.androidx.icons.extended)

    // Lifecycle + ViewModel Compose (fix viewModel())
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Activity Compose
    implementation(libs.androidx.activity.compose)

    // Room + KAPT
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)
}
