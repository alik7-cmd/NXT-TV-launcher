package com.techascent.tv_launcher.presentation

import androidx.annotation.StringRes

sealed interface AppsUiState {
    data object Loading : AppsUiState
    data object Empty : AppsUiState

    data class Error(@param:StringRes val messageResId: Int) : AppsUiState

    data class Content(
        val apps: List<LauncherAppUiState>,
        val favorites: List<LauncherAppUiState> = emptyList(),
        val isRefreshing: Boolean = false,
        @param:StringRes val refreshErrorResId: Int? = null,
    ) : AppsUiState
}
