package com.techascent.tv_launcher.domain.repository

import com.techascent.tv_launcher.domain.model.Weather
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    /** Emits the local weather when it's available and on every periodic refresh. Never fails. */
    fun observeWeather(): Flow<Weather>
}
