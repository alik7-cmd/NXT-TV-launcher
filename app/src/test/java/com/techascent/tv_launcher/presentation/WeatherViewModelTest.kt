package com.techascent.tv_launcher.presentation

import com.techascent.tv_launcher.R
import com.techascent.tv_launcher.LauncherConfig
import com.techascent.tv_launcher.domain.model.Weather
import com.techascent.tv_launcher.domain.model.WeatherCondition
import com.techascent.tv_launcher.domain.repository.WeatherRepository
import com.techascent.tv_launcher.domain.usecase.ObserveWeather
import com.techascent.tv_launcher.presentation.state.WeatherUiState
import java.time.LocalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {
    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private class FakeWeatherRepository(private val source: Flow<Weather>) : WeatherRepository {
        override fun observeWeather() = source
    }

    private fun weather(condition: WeatherCondition, isDay: Boolean) = Weather(
        city = "Oslo",
        temperatureC = -2.6,
        condition = condition,
        isDay = isDay,
        highC = 1.5,
        lowC = -4.4,
        rainExpectedAt = LocalTime.of(3, 0),
    )

    @Test
    fun `loading until weather arrives, then rounded content`() = runTest {
        val source = MutableSharedFlow<Weather>()
        val viewModel = WeatherViewModel(ObserveWeather(FakeWeatherRepository(source)))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        assertEquals(WeatherUiState.Loading, viewModel.uiState.value)

        source.emit(weather(WeatherCondition.Clear, isDay = false))
        val content = viewModel.uiState.value as WeatherUiState.Content
        assertEquals(-3, content.temperature)
        assertEquals(2, content.high)
        assertEquals(-4, content.low)
        assertEquals(true, content.isNight)
        assertEquals(R.string.weather_clear, content.conditionResId)
        assertEquals(false, content.showTemperatureChart)
    }

    @Test
    fun `temperature chart can be enabled through config`() = runTest {
        val source = MutableSharedFlow<Weather>()
        val viewModel = WeatherViewModel(
            ObserveWeather(FakeWeatherRepository(source)),
            LauncherConfig(showWeatherTemperatureChart = true),
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        source.emit(weather(WeatherCondition.Clear, isDay = true))

        val content = viewModel.uiState.value as WeatherUiState.Content
        assertEquals(true, content.showTemperatureChart)
        assertEquals(2, content.high)
        assertEquals(-4, content.low)
    }

    @Test
    fun `clear sky reads as sunny during the day`() {
        assertEquals(R.string.weather_sunny, WeatherUiState.from(weather(WeatherCondition.Clear, isDay = true)).conditionResId)
    }

    @Test
    fun `disabled forecast stays hidden while header weather loads`() = runTest {
        val source = MutableSharedFlow<Weather>()
        val viewModel = WeatherViewModel(
            ObserveWeather(FakeWeatherRepository(source)),
            LauncherConfig(showWeatherForecastSection = false),
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        assertEquals(WeatherUiState.Hidden, viewModel.forecastState.value)
        source.emit(weather(WeatherCondition.Clear, isDay = true))
        assertEquals(WeatherUiState.Hidden, viewModel.forecastState.value)
        assertEquals(2, (viewModel.uiState.value as WeatherUiState.Content).high)
    }

    @Test
    fun `enabled forecast shares loading content and error state with weather`() = runTest {
        val source = MutableSharedFlow<Weather>()
        val viewModel = WeatherViewModel(ObserveWeather(FakeWeatherRepository(source)))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.forecastState.collect {} }
        assertEquals(WeatherUiState.Loading, viewModel.forecastState.value)

        source.emit(weather(WeatherCondition.Clear, isDay = true))
        assertEquals(viewModel.uiState.value, viewModel.forecastState.value)
    }

    @Test
    fun `errors keep the weather hidden`() = runTest {
        val viewModel = WeatherViewModel(ObserveWeather(FakeWeatherRepository(flow { throw IllegalStateException() })))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        assertEquals(WeatherUiState.Hidden, viewModel.uiState.value)
        assertEquals(WeatherUiState.Hidden, viewModel.forecastState.value)
    }
}
