plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.chaquopy)
}

android {
    namespace = "com.mediasaver.androidapp"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId             = "com.mediasaver.app"
        // 28 (not 26) because the bundled static ffmpeg binary targets Android 9.0+
        minSdk                    = 28
        targetSdk                 = 36
        versionCode               = 1
        versionName               = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Chaquopy's embedded Python + the bundled ffmpeg binary are arm64-v8a only
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled   = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable        = true
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
        // Without this, AGP keeps .so files zipped inside the APK and mmaps them directly
        // rather than extracting real files into nativeLibraryDir — meaning there's no actual
        // file on disk for a subprocess (yt-dlp -> ffmpeg) to execute. This is almost certainly
        // why ffmpeg was reported "not installed" even though it was correctly bundled.
        jniLibs { useLegacyPackaging = true }
    }
}

chaquopy {
    defaultConfig {
        // Matches the Python already installed on this machine (python --version)
        version = "3.14"
        pip {
            // Pure-Python, no mandatory third-party deps — safe under Chaquopy's --only-binary pip
            install("yt-dlp")
        }
    }
}

dependencies {
    // KMP shared module — provides App(), all UI, domain, data logic
    implementation(project(":composeApp"))

    // Android-specific entry point
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(libs.material)

    // Navigation graph (real back stack, system back button, deep-link ready) — this module
    // is plain com.android.application (not composeApp's KMP library), so the classic stable
    // AndroidX artifact is used rather than the still-alpha multiplatform navigation-compose.
    implementation(libs.androidx.navigation.compose)

    // Branded splash screen — same SplashScreen API Android 12+ uses natively, backported to
    // this app's minSdk 28 floor.
    implementation(libs.androidx.core.splashscreen)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
