package com.techascent.tv_launcher.data.remote

import com.techascent.tv_launcher.data.model.HourlyPrecipitationDto
import com.techascent.tv_launcher.data.model.DailyForecastDto
import com.techascent.tv_launcher.data.model.LocationDto
import com.techascent.tv_launcher.data.model.WeatherDto
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.json.JSONArray

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
                "&daily=temperature_2m_max,temperature_2m_min,weather_code,precipitation_probability_max" +
                "&hourly=precipitation_probability&forecast_days=6&timezone=auto",
            location.latitude,
            location.longitude,
        )
        val json = getJson(url)
        val current = json.getJSONObject("current")
        val daily = json.getJSONObject("daily")
        val hourly = json.getJSONObject("hourly")
        val hourlyTimes = hourly.getJSONArray("time")
        val hourlyProbabilities = hourly.getJSONArray("precipitation_probability")
        val dailyTimes = daily.getJSONArray("time")
        WeatherDto(
            city = location.city,
            time = LocalDateTime.parse(current.getString("time")),
            temperatureC = current.getDouble("temperature_2m"),
            weatherCode = current.getInt("weather_code"),
            isDay = current.getInt("is_day") == 1,
            highC = daily.getJSONArray("temperature_2m_max").getDouble(0),
            lowC = daily.getJSONArray("temperature_2m_min").getDouble(0),
            timezone = ZoneId.of(json.getString("timezone")),
            dailyForecast = (0 until dailyTimes.length()).map { index ->
                DailyForecastDto(
                    date = LocalDate.parse(dailyTimes.getString(index)),
                    weatherCode = daily.optJSONArray("weather_code")?.numberAt(index)?.toInt(),
                    highC = daily.optJSONArray("temperature_2m_max")?.numberAt(index)?.toDouble(),
                    lowC = daily.optJSONArray("temperature_2m_min")?.numberAt(index)?.toDouble(),
                    maxPrecipitationProbability = daily.optJSONArray("precipitation_probability_max")?.numberAt(index)?.toInt(),
                )
            },
            hourlyPrecipitation = (0 until hourlyTimes.length()).map { index ->
                HourlyPrecipitationDto(
                    time = LocalDateTime.parse(hourlyTimes.getString(index)),
                    probabilityPercent = hourlyProbabilities.optInt(index, 0),
                )
            },
        )
    }

    private fun JSONArray.numberAt(index: Int): Number? = opt(index) as? Number

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
