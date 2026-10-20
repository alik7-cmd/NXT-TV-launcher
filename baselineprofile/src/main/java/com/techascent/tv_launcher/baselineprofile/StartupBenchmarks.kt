package com.techascent.tv_launcher.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compares cold start without precompilation against start with the Baseline Profile applied.
 *
 * `ANDROID_SERIAL=<tv-emulator> ./gradlew :baselineprofile:connectedBenchmarkReleaseAndroidTest`
 * Emulator numbers are indicative only; run on TV hardware for real measurements.
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmarks {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Before
    fun checkDevice() = requireTvTestDevice()

    @Test
    fun startupCompilationNone() = benchmark(CompilationMode.None())

    @Test
    fun startupCompilationBaselineProfile() =
        benchmark(CompilationMode.Partial(BaselineProfileMode.Require))

    private fun benchmark(compilationMode: CompilationMode) = rule.measureRepeated(
        packageName = targetAppId(),
        metrics = listOf(StartupTimingMetric()),
        compilationMode = compilationMode,
        startupMode = StartupMode.COLD,
        iterations = 10,
    ) {
        startLauncherAndAwaitFocus()
    }
}
