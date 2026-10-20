package com.techascent.tv_launcher.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates the launcher's Baseline Profile and startup profile.
 *
 * Run on an Android TV emulator (Gradle Managed Devices do not support TV images):
 * `baselineprofile/generate-on-tv-emulator.sh`
 *
 * The result is written to `app/src/release/generated/baselineProfiles/`.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Before
    fun checkDevice() = requireTvTestDevice()

    /** Startup only, so the startup profile (used for dex layout) stays focused on cold start. */
    @Test
    fun startup() = rule.collect(
        packageName = targetAppId(),
        includeInStartupProfile = true,
    ) {
        startLauncherAndAwaitFocus()
    }

    /** D-pad browsing, Back-to-top and the favourite picker; merged into the Baseline Profile only. */
    @Test
    fun dpadNavigation() = rule.collect(
        packageName = targetAppId(),
    ) {
        startLauncherAndAwaitFocus()
        browseHomeWithDpad()
        openAndDismissFavoritePicker()
    }
}
