package com.techascent.tv_launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.techascent.tv_launcher.presentation.LauncherViewModel
import com.techascent.tv_launcher.presentation.navigation.LauncherNavigator
import com.techascent.tv_launcher.presentation.ui.home.LauncherHome
import com.techascent.tv_launcher.ui.theme.TVLauncherTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val launcherViewModel: LauncherViewModel by viewModels {
        (application as LauncherApplication).container.launcherViewModelFactory
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
                LauncherHome(
                    uiState = uiState,
                    onAppClick = launcherViewModel::onAppSelected,
                    onAppLongClick = launcherViewModel::onAppLongPressed,
                    onUninstall = launcherViewModel::onUninstallSelected,
                    onOptionsDismiss = launcherViewModel::onAppOptionsDismissed,
                    onInputClick = launcherViewModel::onInputSelected,
                    onSettingsClick = launcherViewModel::onSettingsSelected,
                    onCustomizeClick = launcherViewModel::onCustomizeSelected,
                    onAppStyleSelected = launcherViewModel::onAppStyleSelected,
                    onCustomizeDismiss = launcherViewModel::onCustomizeDismissed,
                    onShowAppNamesSelected = launcherViewModel::onShowAppNamesSelected,
                    onWallpaperApplied = launcherViewModel::onWallpaperApplied,
                    onPin = launcherViewModel::onPinSelected,
                    onHide = launcherViewModel::onHideSelected,
                    onRestoreApp = launcherViewModel::onRestoreAppSelected,
                    onSortOrderSelected = launcherViewModel::onSortOrderSelected,
                )
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launcherViewModel.effect.collect(navigator::handle)
            }
        }
    }
}
