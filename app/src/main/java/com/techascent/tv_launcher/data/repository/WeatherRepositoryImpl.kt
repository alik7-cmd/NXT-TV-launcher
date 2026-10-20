package com.techascent.tv_launcher.data.repository

import com.techascent.tv_launcher.data.model.LocationDto
import com.techascent.tv_launcher.data.model.WeatherDto
import com.techascent.tv_launcher.data.remote.WeatherRemoteDataSource
import com.techascent.tv_launcher.domain.model.Weather
import com.techascent.tv_launcher.domain.repository.WeatherRepository
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.milliseconds

/**
 * Refreshes every [REFRESH_INTERVAL_MS] while collected. The latest result is kept in memory, so
 * returning to the launcher shows it immediately instead of refetching.
 */
class WeatherRepositoryImpl(
    private val remote: WeatherRemoteDataSource,
    private val clock: () -> Long = System::currentTimeMillis,
) : WeatherRepository {
    private var location: LocationDto? = null
    private var latest: Pair<Long, WeatherDto>? = null

    override fun observeWeather(): Flow<Weather> = flow {
        latest?.let { (fetchedAt, weather) ->
            emit(weather.toDomain())
            delay((fetchedAt + REFRESH_INTERVAL_MS - clock()).coerceAtLeast(0L).milliseconds)
        }
        var retryDelay = FIRST_RETRY_DELAY_MS
        while (true) {
            val weather = try {
                val place = location ?: remote.fetchLocation().also { location = it }
                remote.fetchWeather(place)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                null
            }
            if (weather != null) {
                latest = clock() to weather
                emit(weather.toDomain())
                retryDelay = FIRST_RETRY_DELAY_MS
                delay(REFRESH_INTERVAL_MS.milliseconds)
            } else {
                // Offline or a service hiccup: keep showing the last result and retry with backoff.
                delay(retryDelay.milliseconds)
                retryDelay = (retryDelay * 2).coerceAtMost(MAX_RETRY_DELAY_MS)
            }
        }
    }

    internal companion object {
        const val REFRESH_INTERVAL_MS = 30 * 60_000L
        const val FIRST_RETRY_DELAY_MS = 60_000L
        const val MAX_RETRY_DELAY_MS = 15 * 60_000L
    }
}
