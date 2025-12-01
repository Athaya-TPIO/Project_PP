plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt") // perlu untuk Room compiler
}

android {
    namespace = "com.example.projectpp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.projectpp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    kotlinOptions { jvmTarget = "11" }

    buildFeatures {
        compose = true
        viewBinding = true // biarkan ON kalau masih ingin XML
    }

    // supaya file schema Room ikut terdaftar (menghilangkan warning)
    sourceSets {
        getByName("androidTest").assets.srcDirs(files("$projectDir/schemas"))
    }
}

dependencies {
    // ==== Compose dasar ====
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Ikon Compose (pakai BOM, jadi tanpa versi eksplisit)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    // ==== Room (database lokal) ====
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // ==== WorkManager (jadwal notifikasi) ====
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // ==== Lifecycle / ViewModel untuk Compose ====
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
    // (opsional, kalau ingin observe State langsung di Compose)
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.5")

    // ==== Coroutines Android (Flow, background) ====
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // (opsional) RecyclerView bila pakai layar XML klasik
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // ==== Testing ====
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

// Argumen untuk annotation processor Room (schema export, incremental)
kapt {
    correctErrorTypes = true
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")
        // opsional:
        // arg("room.expandProjection", "true")
    }
}
