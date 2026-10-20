package com.techascent.tv_launcher.data

import com.techascent.tv_launcher.data.model.LocationDto
import com.techascent.tv_launcher.data.model.WeatherDto
import com.techascent.tv_launcher.data.remote.WeatherRemoteDataSource
import com.techascent.tv_launcher.data.repository.WeatherRepositoryImpl
import com.techascent.tv_launcher.domain.model.Weather
import java.io.IOException
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherRepositoryImplTest {
    private class FakeRemote : WeatherRemoteDataSource {
        var locationCalls = 0
        var weatherCalls = 0
        val failures = ArrayDeque<Boolean>()
        var temperature = 5.0

        override suspend fun fetchLocation(): LocationDto {
            locationCalls++
            return LocationDto(59.9, 10.7, "Oslo")
        }

        override suspend fun fetchWeather(location: LocationDto): WeatherDto {
            weatherCalls++
            if (failures.removeFirstOrNull() == true) throw IOException("offline")
            return WeatherDto(location.city, LocalDateTime.of(2026, 10, 19, 12, 0), temperature, 0, true, 9.0, 3.0, emptyList())
        }
    }

    private fun TestScope.collect(repository: WeatherRepositoryImpl, into: MutableList<Weather>) =
        backgroundScope.launch { repository.observeWeather().toList(into) }

    @Test
    fun `retries with backoff after a failure, then refreshes every 30 minutes`() = runTest {
        val remote = FakeRemote().apply { failures.addAll(listOf(true, true)) }
        val repository = WeatherRepositoryImpl(remote, clock = { testScheduler.currentTime })
        val emitted = mutableListOf<Weather>()
        collect(repository, emitted)

        runCurrent()
        assertEquals(0, emitted.size)
        advanceTimeBy(WeatherRepositoryImpl.FIRST_RETRY_DELAY_MS)
        runCurrent()
        assertEquals("second failure", 0, emitted.size)
        advanceTimeBy(WeatherRepositoryImpl.FIRST_RETRY_DELAY_MS * 2)
        runCurrent()
        assertEquals(1, emitted.size)

        remote.temperature = 8.0
        advanceTimeBy(WeatherRepositoryImpl.REFRESH_INTERVAL_MS)
        runCurrent()
        assertEquals(listOf(5.0, 8.0), emitted.map { it.temperatureC })
        assertEquals("location is looked up once", 1, remote.locationCalls)
    }

    @Test
    fun `a new collector gets the cached weather without refetching`() = runTest {
        val remote = FakeRemote()
        val repository = WeatherRepositoryImpl(remote, clock = { testScheduler.currentTime })
        val first = backgroundScope.launch { repository.observeWeather().toList(mutableListOf()) }
        runCurrent()
        first.cancel()

        advanceTimeBy(10 * 60_000L)
        val emitted = mutableListOf<Weather>()
        collect(repository, emitted)
        runCurrent()

        assertEquals(1, emitted.size)
        assertEquals(1, remote.weatherCalls)
        advanceTimeBy(20 * 60_000L)
        runCurrent()
        assertEquals("refreshes when the cached result is 30 minutes old", 2, remote.weatherCalls)
    }
}
