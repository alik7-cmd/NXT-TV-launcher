plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.androidx.baselineprofile)
}

android {
    namespace = "com.techascent.tv_launcher.baselineprofile"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        // Profile capture without root needs API 33+; benchmarks still run from the app's minSdk.
        minSdk = 29
        targetSdk = 37
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Startup benchmarks are indicative only on the TV emulator; real hardware gives real numbers.
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR"
    }

    targetProjectPath = ":app"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

baselineProfile {
    // Gradle Managed Devices reject TV system images ("TV and Auto devices are presently not
    // supported"), so generation runs on a connected Android TV emulator. Always pin it with
    // ANDROID_SERIAL; generate-on-tv-emulator.sh does this. The tests refuse non-TV and physical devices.
    useConnectedDevices = true
}

dependencies {
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}

androidComponents {
    onVariants { variant ->
        val artifactsLoader = variant.artifacts.getBuiltArtifactsLoader()
        variant.instrumentationRunnerArguments.put(
            "targetAppId",
            variant.testedApks.map { artifactsLoader.load(it)?.applicationId },
        )
    }
}

// Device-driving tasks only run when pinned to an emulator, so a connected phone or TV is never used.
val androidSerial = providers.environmentVariable("ANDROID_SERIAL")
tasks.matching { it.name.startsWith("connected") }.configureEach {
    val serial = androidSerial
    doFirst {
        check(serial.orNull?.startsWith("emulator-") == true) {
            "Set ANDROID_SERIAL=emulator-<port> (an Android TV emulator) or use generate-on-tv-emulator.sh"
        }
    }
}
