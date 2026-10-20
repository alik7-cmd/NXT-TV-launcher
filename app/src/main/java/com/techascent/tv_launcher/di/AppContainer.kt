package com.techascent.tv_launcher.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.techascent.tv_launcher.data.repository.FavoriteAppsRepositoryImpl
import com.techascent.tv_launcher.data.remote.HttpWeatherRemoteDataSource
import com.techascent.tv_launcher.data.repository.InstalledAppsRepositoryImpl
import com.techascent.tv_launcher.data.repository.WeatherRepositoryImpl
import com.techascent.tv_launcher.domain.usecase.ObserveFavoriteApps
import com.techascent.tv_launcher.domain.usecase.ObserveInstalledApps
import com.techascent.tv_launcher.domain.usecase.ObserveWeather
import com.techascent.tv_launcher.domain.usecase.ToggleFavoriteApp
import com.techascent.tv_launcher.presentation.LauncherViewModel
import com.techascent.tv_launcher.presentation.WeatherViewModel

/** Manual dependency graph, created once per process and shared across configuration changes. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val installedAppsRepository by lazy { InstalledAppsRepositoryImpl(appContext) }
    private val favoriteAppsRepository by lazy { FavoriteAppsRepositoryImpl(appContext) }
    private val weatherRepository by lazy { WeatherRepositoryImpl(HttpWeatherRemoteDataSource()) }

    val launcherViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            LauncherViewModel(
                observeInstalledApps = ObserveInstalledApps(installedAppsRepository),
                observeFavoriteApps = ObserveFavoriteApps(favoriteAppsRepository),
                toggleFavoriteApp = ToggleFavoriteApp(favoriteAppsRepository),
            )
        }
    }

    val weatherViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { WeatherViewModel(ObserveWeather(weatherRepository)) }
    }
}
