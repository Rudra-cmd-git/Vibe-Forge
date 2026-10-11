plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.vibecoder.runtime"
    compileSdk = 34

    defaultConfig {
        minSdk = 28
        targetSdk = 28  // Target SDK 28 for direct PRoot execution
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    // Include native libraries
    sourceSets {
        main {
            jniLibs.srcDirs += "src/main/jniLibs"
        }
    }

    packagingOptions {
        jniLibs {
            useLegacyPackaging = true
        }
        doNotStrip "**/libproot*.so"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.okio)
}