package com.techascent.tv_launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.techascent.tv_launcher.presentation.LauncherViewModel
import com.techascent.tv_launcher.presentation.WeatherUiState
import com.techascent.tv_launcher.presentation.WeatherViewModel
import com.techascent.tv_launcher.presentation.navigation.LauncherNavigator
import com.techascent.tv_launcher.presentation.ui.FavoritePickerPanel
import com.techascent.tv_launcher.presentation.ui.LauncherHome
import com.techascent.tv_launcher.presentation.ui.USE_WEATHER_HERO
import com.techascent.tv_launcher.presentation.ui.launcherBackground
import com.techascent.tv_launcher.ui.theme.TVLauncherTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val launcherViewModel: LauncherViewModel by viewModels {
        (application as LauncherApplication).container.launcherViewModelFactory
    }

    private val weatherViewModel: WeatherViewModel by viewModels {
        (application as LauncherApplication).container.weatherViewModelFactory
    }

    private val navigator by lazy { LauncherNavigator(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        setContent {
            TVLauncherTheme {
                val uiState by launcherViewModel.uiState.collectAsStateWithLifecycle()
                val headerState by launcherViewModel.headerState.collectAsStateWithLifecycle()
                // Not collecting keeps the weather flow cold, so nothing is fetched when it's switched off.
                val weatherState = if (USE_WEATHER_HERO) {
                    weatherViewModel.uiState.collectAsStateWithLifecycle().value
                } else WeatherUiState.Hidden
                Box(modifier = Modifier.fillMaxSize()) {
                    // Separate cached layer: the gradients are rendered once instead of on
                    // every scroll frame, which low-end TV GPUs can't keep up with.
                    Box(
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                            .launcherBackground(),
                    )
                    LauncherHome(
                        uiState = uiState,
                        headerState = headerState,
                        weatherState = weatherState,
                        onAppClick = launcherViewModel::onAppSelected,
                        onAppLongClick = launcherViewModel::onAppLongPressed,
                        onInputClick = launcherViewModel::onInputSelected,
                        onSettingsClick = launcherViewModel::onSettingsSelected,
                        onPlayStoreClick = launcherViewModel::onPlayStoreSelected,
                        onAddFavoriteClick = launcherViewModel::onAddFavoriteSelected,
                        onFavoriteLongClick = launcherViewModel::onFavoriteLongPressed,
                        onRemoveFavorite = launcherViewModel::onToggleFavoriteSelected,
                        onUninstall = launcherViewModel::onUninstallSelected,
                        onOptionsDismiss = launcherViewModel::onAppOptionsDismissed,
                    )
                    uiState.favoritePickerApps?.let { apps ->
                        FavoritePickerPanel(
                            apps = apps,
                            onSelect = launcherViewModel::onFavoriteAppPicked,
                            onDismiss = launcherViewModel::onFavoritePickerDismissed,
                        )
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launcherViewModel.effect.collect(navigator::handle)
            }
        }
    }
}
