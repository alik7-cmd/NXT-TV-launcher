package com.techascent.tv_launcher.data.remote

import com.techascent.tv_launcher.data.model.LocationDto
import com.techascent.tv_launcher.data.model.WeatherDto

interface WeatherRemoteDataSource {
    /** Approximate location of this network's public IP address. */
    suspend fun fetchLocation(): LocationDto

    suspend fun fetchWeather(location: LocationDto): WeatherDto
}
