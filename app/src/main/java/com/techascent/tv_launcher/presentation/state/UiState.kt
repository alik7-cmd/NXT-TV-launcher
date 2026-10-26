package com.techascent.tv_launcher.presentation.state

import androidx.annotation.StringRes

/** Loading, success or error state of one screen section. */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(@param:StringRes val messageResId: Int) : UiState<Nothing>
}
