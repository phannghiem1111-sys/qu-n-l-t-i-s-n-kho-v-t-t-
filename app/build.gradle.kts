plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val runNumber = System.getenv("RUN_NUMBER") ?: "1"

android {
    namespace = "com.xepca.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.xepca.app"
        minSdk = 24
        targetSdk = 34
        versionCode = runNumber.toInt()
        versionName = "1.0.$runNumber"
    }

    signingConfigs {
        create("release") {
            // Đọc từ GitHub Secrets khi build trên Actions
            System.getenv("KEYSTORE_FILE")?.let { storeFile = file(it) }
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.webkit:webkit:1.12.1")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
}
