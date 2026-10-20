package com.techascent.tv_launcher.baselineprofile

import android.content.pm.PackageManager
import android.os.Build
import android.view.KeyEvent
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Until
import java.util.regex.Pattern

/**
 * TV-safe user journeys shared by the profile generator and the benchmarks.
 *
 * They only use D-pad movement and Back. OK (DPAD_CENTER) is pressed solely on the launcher's own
 * "add favourite" tile after verifying it holds focus, so no app, settings, input picker, Play
 * Store or uninstall dialog is ever launched.
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

/** Walks the favourites row, the All apps grid and the Play Store panel, then uses Back to return. */
internal fun MacrobenchmarkScope.browseHomeWithDpad() {
    ensureLauncherInForeground()
    repeat(4) { dpad(KeyEvent.KEYCODE_DPAD_RIGHT) }
    repeat(4) { dpad(KeyEvent.KEYCODE_DPAD_LEFT) }
    // Down into the grid and across it so the collapsing header and pivot scrolling run.
    repeat(6) {
        dpad(KeyEvent.KEYCODE_DPAD_DOWN)
        dpad(KeyEvent.KEYCODE_DPAD_RIGHT)
    }
    // Far enough to reach the Play Store panel on any grid size; focus is never activated.
    repeat(6) { dpad(KeyEvent.KEYCODE_DPAD_DOWN) }
    repeat(4) { dpad(KeyEvent.KEYCODE_DPAD_UP) }
    // When scrolled, Back is consumed by the launcher and returns to the top of the home screen.
    back()
    ensureLauncherInForeground()
    // Up into the header actions (inputs/settings) and back down; OK is never pressed there.
    repeat(2) { dpad(KeyEvent.KEYCODE_DPAD_UP) }
    dpad(KeyEvent.KEYCODE_DPAD_RIGHT)
    dpad(KeyEvent.KEYCODE_DPAD_LEFT)
    dpad(KeyEvent.KEYCODE_DPAD_DOWN)
}

/** Opens the in-launcher favourite picker from the add tile, browses it and closes it with Back. */
internal fun MacrobenchmarkScope.openAndDismissFavoritePicker() {
    ensureLauncherInForeground()
    val addLabel = targetString("add_favorite_tile", "Add app to favourite")
    var steps = 0
    while (!isFocused(addLabel) && steps < MAX_FAVORITE_ROW_STEPS) {
        dpad(KeyEvent.KEYCODE_DPAD_RIGHT)
        steps++
    }
    // Never press OK unless the add tile is verifiably focused: on an app tile it would launch the app.
    if (!isFocused(addLabel)) return
    dpad(KeyEvent.KEYCODE_DPAD_CENTER)

    val pickerTitle = targetString("select_favorite_app", "Select app")
    val allAdded = targetString("favorites_all_added", "All apps have been added as favourites.")
    val pickerShown = device.wait(
        Until.hasObject(By.pkg(packageName).text(Pattern.compile("${Pattern.quote(pickerTitle)}|${Pattern.quote(allAdded)}"))),
        UI_TIMEOUT_MS,
    ) == true
    if (pickerShown) {
        repeat(4) { dpad(KeyEvent.KEYCODE_DPAD_DOWN) }
        repeat(2) { dpad(KeyEvent.KEYCODE_DPAD_UP) }
        back()
        device.wait(Until.gone(By.pkg(packageName).text(pickerTitle)), UI_TIMEOUT_MS)
    }
    ensureLauncherInForeground()
}

private fun MacrobenchmarkScope.awaitHomeFocus() {
    device.wait(Until.hasObject(By.pkg(packageName).depth(0)), START_TIMEOUT_MS)
    // The home screen requests focus on the first favourite (or add tile) once apps have loaded.
    device.wait(Until.hasObject(inTarget().focused(true)), START_TIMEOUT_MS)
    device.waitForIdle()
}

/**
 * Back on an unscrolled home screen sends the launcher to the background. Relaunching our own
 * activity keeps the remaining journey inside the launcher instead of the system home.
 */
private fun MacrobenchmarkScope.ensureLauncherInForeground() {
    if (!device.hasObject(By.pkg(packageName).depth(0))) {
        startLauncherAndAwaitFocus()
    }
}

private fun MacrobenchmarkScope.isFocused(label: String): Boolean {
    val text = Pattern.compile(".*${Pattern.quote(label)}.*", Pattern.DOTALL)
    return device.hasObject(inTarget().focused(true).text(text)) ||
        device.hasObject(inTarget().focused(true).hasDescendant(By.text(text)))
}

private fun MacrobenchmarkScope.inTarget(): BySelector = By.pkg(packageName)

private fun MacrobenchmarkScope.dpad(keyCode: Int) {
    device.pressKeyCode(keyCode)
    device.waitForIdle()
}

private fun MacrobenchmarkScope.back() {
    device.pressBack()
    device.waitForIdle()
}

/** Reads a launcher string in the device locale, so selectors also work on Norwegian devices. */
private fun MacrobenchmarkScope.targetString(name: String, fallback: String): String = runCatching {
    val resources = InstrumentationRegistry.getInstrumentation().context.packageManager
        .getResourcesForApplication(packageName)
    @Suppress("DiscouragedApi")
    val id = resources.getIdentifier(name, "string", packageName)
    if (id != 0) resources.getString(id) else fallback
}.getOrDefault(fallback)

private const val START_TIMEOUT_MS = 10_000L
private const val UI_TIMEOUT_MS = 3_000L
private const val MAX_FAVORITE_ROW_STEPS = 30
