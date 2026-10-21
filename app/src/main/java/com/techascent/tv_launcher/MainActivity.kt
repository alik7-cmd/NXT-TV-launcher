package com.techascent.tv_launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.techascent.tv_launcher.presentation.WeatherViewModel
import com.techascent.tv_launcher.presentation.navigation.LauncherNavigator
import com.techascent.tv_launcher.presentation.ui.picker.FavoritePickerPanelV2
import com.techascent.tv_launcher.presentation.ui.common.launcherBackground
import com.techascent.tv_launcher.presentation.ui.common.LauncherBackgroundPicker
import com.techascent.tv_launcher.presentation.ui.home.LauncherHomeV2
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
                //val backgroundPalette = rememberSaveable { LauncherBackgroundPicker().pick() }
                val uiState by launcherViewModel.uiState.collectAsStateWithLifecycle()
                val weatherState by weatherViewModel.uiState.collectAsStateWithLifecycle()
                val forecastState by weatherViewModel.forecastState.collectAsStateWithLifecycle()
                Box(modifier = Modifier.fillMaxSize()) {
                    // Separate cached layer: the gradients are rendered once instead of on
                    // every scroll frame, which low-end TV GPUs can't keep up with.
                    Box(
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                            .launcherBackground(),
                    )
                    LauncherHomeV2(
                        uiState = uiState,
                        weatherState = weatherState,
                        forecastState = forecastState,
                        onInputClick = launcherViewModel::onInputSelected,
                        onSettingsClick = launcherViewModel::onSettingsSelected,
                        onAppClick = launcherViewModel::onAppSelected,
                        onAppLongClick = launcherViewModel::onAppLongPressed,
                        onFavoriteLongClick = launcherViewModel::onFavoriteLongPressed,
                        onAddFavoriteClick = launcherViewModel::onAddFavoriteSelected,
                        onRemoveFavorite = launcherViewModel::onToggleFavoriteSelected,
                        onUninstall = launcherViewModel::onUninstallSelected,
                        onOptionsDismiss = launcherViewModel::onAppOptionsDismissed,
                    )
                    uiState.favoritePickerApps?.let { apps ->
                        FavoritePickerPanelV2(
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
