package com.techascent.tv_launcher.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.techascent.tv_launcher.data.repository.AppStyleRepositoryImpl
import com.techascent.tv_launcher.data.repository.AppOrganizationRepositoryImpl
import com.techascent.tv_launcher.data.repository.InstalledAppsRepositoryImpl
import com.techascent.tv_launcher.data.repository.RecentAppsRepositoryImpl
import com.techascent.tv_launcher.domain.usecase.ObserveLauncherApps
import com.techascent.tv_launcher.domain.usecase.ObserveRecentApps
import com.techascent.tv_launcher.domain.usecase.RecordAppLaunch
import com.techascent.tv_launcher.presentation.LauncherViewModel

/** Manual dependency graph, created once per process and shared across configuration changes. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val installedAppsRepository by lazy { InstalledAppsRepositoryImpl(appContext) }
    private val appStyleRepository by lazy { AppStyleRepositoryImpl(appContext) }
    private val recentAppsRepository by lazy { RecentAppsRepositoryImpl(appContext) }
    private val appOrganizationRepository by lazy { AppOrganizationRepositoryImpl(appContext) }

    val launcherViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            LauncherViewModel(
                observeLauncherApps = ObserveLauncherApps(installedAppsRepository),
                observeRecentApps = ObserveRecentApps(recentAppsRepository),
                recordAppLaunch = RecordAppLaunch(recentAppsRepository),
                appStyleRepository = appStyleRepository,
                appOrganizationRepository = appOrganizationRepository,
            )
        }
    }
}
