package com.techascent.tv_launcher

/** Feature switches for the launcher; change the defaults here, or pass another instance in tests. */
data class LauncherConfig(
    /**
     * true: shows the weather header and five-day forecast and fetches weather over the network.
     * false: weather stays hidden and no network requests are made.
     */
    val useWeatherHero: Boolean = true,
    /**
     * true: the weather art moves (drifting cloud, pulsing sun/moon glow, twinkling stars, falling
     * rain and snow). The animation pauses while the art is scrolled out of view.
     * false: the weather art is a still image, which saves GPU on low-end TVs.
     */
    val animateWeatherArt: Boolean = false,
    /** Shows the high/low temperature chart above the daily forecast details. */
    val showWeatherTemperatureChart: Boolean = false,
    /** Shows the bottom forecast section, including its loading placeholder; header weather is unaffected. */
    val showWeatherForecastSection: Boolean = false,
)
