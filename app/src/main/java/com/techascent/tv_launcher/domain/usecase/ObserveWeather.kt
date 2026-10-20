package com.techascent.tv_launcher.domain.usecase

import com.techascent.tv_launcher.domain.model.Weather
import com.techascent.tv_launcher.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow

class ObserveWeather(
    private val repository: WeatherRepository,
) {
    operator fun invoke(): Flow<Weather> = repository.observeWeather()
}
