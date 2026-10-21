package com.techascent.tv_launcher.baselineprofile

import android.content.pm.PackageManager
import android.os.Build
import android.view.KeyEvent
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until

/**
 * TV-safe user journeys shared by the profile generator and the benchmarks.
 *
 * They only use D-pad movement and Back, so no app, settings, input picker, or uninstall dialog is
 * ever launched.
 */

/**
 * Fails fast unless the test device is an Android TV emulator, so a stray phone or a real TV is never
 * driven. Physical TVs can be opted in explicitly with the instrumentation argument
 * `-Pandroid.testInstrumentationRunnerArguments.tvlauncher.allowPhysicalDevice=true`.
 */
internal fun requireTvTestDevice() {
    val packageManager = InstrumentationRegistry.getInstrumentation().context.packageManager
    check(packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)) {
        "Not an Android TV device (${Build.MODEL}); run on the Television AVD via generate-on-tv-emulator.sh"
    }
    val allowPhysical = InstrumentationRegistry.getArguments()
        .getString("tvlauncher.allowPhysicalDevice").toBoolean()
    check(allowPhysical || isEmulator()) {
        "Refusing to drive physical device ${Build.MODEL}; use a TV emulator or opt in with tvlauncher.allowPhysicalDevice=true"
    }
}

private fun isEmulator(): Boolean =
    Build.HARDWARE in setOf("ranchu", "goldfish") ||
        Build.FINGERPRINT.startsWith("generic") ||
        Build.PRODUCT.contains("sdk")

internal fun targetAppId(): String =
    InstrumentationRegistry.getArguments().getString("targetAppId")
        ?: error("targetAppId instrumentation argument missing; run through the :baselineprofile module")

/** Cold-starts the launcher and waits until the initial D-pad focus has landed on the home screen. */
internal fun MacrobenchmarkScope.startLauncherAndAwaitFocus() {
    startActivityAndWait()
    awaitHomeFocus()
}

/** Browses the recent-apps row and installed-app grid, then returns to the top of the home screen. */
internal fun MacrobenchmarkScope.browseHomeWithDpad() {
    repeat(4) { dpad(KeyEvent.KEYCODE_DPAD_RIGHT) }
    repeat(4) { dpad(KeyEvent.KEYCODE_DPAD_LEFT) }
    // Down into the grid and across it so the collapsing header and pivot scrolling run.
    repeat(6) {
        dpad(KeyEvent.KEYCODE_DPAD_DOWN)
        dpad(KeyEvent.KEYCODE_DPAD_RIGHT)
    }
    repeat(3) { dpad(KeyEvent.KEYCODE_DPAD_DOWN) }
    repeat(4) { dpad(KeyEvent.KEYCODE_DPAD_UP) }
    // When scrolled, Back is consumed by the launcher and returns to the top of the home screen.
    back()
    // Up into the header actions (inputs/settings) and back down; OK is never pressed there.
    repeat(2) { dpad(KeyEvent.KEYCODE_DPAD_UP) }
    dpad(KeyEvent.KEYCODE_DPAD_RIGHT)
    dpad(KeyEvent.KEYCODE_DPAD_LEFT)
    dpad(KeyEvent.KEYCODE_DPAD_DOWN)
}

private fun MacrobenchmarkScope.awaitHomeFocus() {
    device.wait(Until.hasObject(By.pkg(packageName).depth(0)), START_TIMEOUT_MS)
    // Wait until initial focus lands somewhere on the home screen.
    device.wait(Until.hasObject(inTarget().focused(true)), START_TIMEOUT_MS)
    device.waitForIdle()
}
private fun MacrobenchmarkScope.inTarget() = By.pkg(packageName)

private fun MacrobenchmarkScope.dpad(keyCode: Int) {
    device.pressKeyCode(keyCode)
    device.waitForIdle()
}

private fun MacrobenchmarkScope.back() {
    device.pressBack()
    device.waitForIdle()
}

private const val START_TIMEOUT_MS = 10_000L
