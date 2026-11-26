plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.notto.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.notto.exact"
        minSdk = 28
        targetSdk = 34
        versionCode = 10
        versionName = "1.0.9"
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("NOTTO_KEYSTORE") ?: "notto-release.jks")
            storePassword = System.getenv("NOTTO_STORE_PASS") ?: "notto1234"
            keyAlias = System.getenv("NOTTO_KEY_ALIAS") ?: "notto"
            keyPassword = System.getenv("NOTTO_KEY_PASS") ?: "notto1234"
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-ktx:1.9.2")
}
