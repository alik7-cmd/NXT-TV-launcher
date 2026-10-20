package com.techascent.tv_launcher.data.remote

import com.techascent.tv_launcher.data.model.HourlyPrecipitationDto
import com.techascent.tv_launcher.data.model.LocationDto
import com.techascent.tv_launcher.data.model.WeatherDto
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Keyless public services: IP geolocation (GeoJS, falling back to ipwho.is) and the Open-Meteo
 * forecast. Uses the platform HTTP client and JSON parser, so no extra dependencies.
 */
class HttpWeatherRemoteDataSource : WeatherRemoteDataSource {

    override suspend fun fetchLocation(): LocationDto = withContext(Dispatchers.IO) {
        runCatching {
            val json = getJson("https://get.geojs.io/v1/ip/geo.json")
            LocationDto(
                latitude = json.getString("latitude").toDouble(),
                longitude = json.getString("longitude").toDouble(),
                city = json.optString("city").takeIf(String::isNotBlank),
            )
        }.recoverCatching {
            val json = getJson("https://ipwho.is/")
            check(json.optBoolean("success")) { "IP location unavailable" }
            LocationDto(
                latitude = json.getDouble("latitude"),
                longitude = json.getDouble("longitude"),
                city = json.optString("city").takeIf(String::isNotBlank),
            )
        }.getOrThrow()
    }

    override suspend fun fetchWeather(location: LocationDto): WeatherDto = withContext(Dispatchers.IO) {
        val url = String.format(
            Locale.ROOT,
            "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f" +
                "&current=temperature_2m,weather_code,is_day" +
                "&daily=temperature_2m_max,temperature_2m_min" +
                "&hourly=precipitation_probability&forecast_days=2&timezone=auto",
            location.latitude,
            location.longitude,
        )
        val json = getJson(url)
        val current = json.getJSONObject("current")
        val daily = json.getJSONObject("daily")
        val hourly = json.getJSONObject("hourly")
        val hourlyTimes = hourly.getJSONArray("time")
        val hourlyProbabilities = hourly.getJSONArray("precipitation_probability")
        WeatherDto(
            city = location.city,
            time = LocalDateTime.parse(current.getString("time")),
            temperatureC = current.getDouble("temperature_2m"),
            weatherCode = current.getInt("weather_code"),
            isDay = current.getInt("is_day") == 1,
            highC = daily.getJSONArray("temperature_2m_max").getDouble(0),
            lowC = daily.getJSONArray("temperature_2m_min").getDouble(0),
            hourlyPrecipitation = (0 until hourlyTimes.length()).map { index ->
                HourlyPrecipitationDto(
                    time = LocalDateTime.parse(hourlyTimes.getString(index)),
                    probabilityPercent = hourlyProbabilities.optInt(index, 0),
                )
            },
        )
    }

    private fun getJson(url: String): JSONObject {
        val connection = URL(url).openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.setRequestProperty("Accept", "application/json")
            check(connection.responseCode == HttpURLConnection.HTTP_OK) { "HTTP ${connection.responseCode} for $url" }
            JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val TIMEOUT_MS = 10_000
    }
}
