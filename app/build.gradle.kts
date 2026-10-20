plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.androidx.baselineprofile)
}

android {
    namespace = "com.techascent.tv_launcher"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.techascent.tv_launcher"
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

    }

    buildTypes {
        release {
            // Full-app R8 optimisation. The library-only `packageScope` mode produced an
            // IllegalAccessError at startup on Android 10.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            // Personal sideloaded build: reuse the debug key so it updates the existing install.
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

baselineProfile {
    // Profiles are generated on demand (see :baselineprofile), so assembleRelease never needs a device.
    automaticGenerationDuringBuild = false
    saveInSrc = true
    // Uses the startup profile captured by the generator to put startup classes in the primary dex.
    dexLayoutOptimization = true
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.tv.material)
    implementation(libs.androidx.profileinstaller)
    baselineProfile(project(":baselineprofile"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    debugImplementation(libs.androidx.compose.ui.tooling)
}