package com.scrollthegita.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrollthegita.core.common.UserFacingError
import com.scrollthegita.core.model.PreferredLanguage
import com.scrollthegita.core.model.ThemeMode
import com.scrollthegita.core.model.UserPreferences
import com.scrollthegita.domain.usecase.ObserveUserPreferencesUseCase
import com.scrollthegita.domain.usecase.UpdateUserPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Content(val preferences: UserPreferences) : SettingsUiState
    data class Error(val error: UserFacingError) : SettingsUiState
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observePreferences: ObserveUserPreferencesUseCase,
    private val updatePreferences: UpdateUserPreferencesUseCase,
) : ViewModel() {
    val state: StateFlow<SettingsUiState> = observePreferences()
        .map<UserPreferences, SettingsUiState> { SettingsUiState.Content(it) }
        .catch { emit(SettingsUiState.Error(UserFacingError.ContentUnavailable)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState.Loading)

    fun setLanguage(language: PreferredLanguage) = launch { updatePreferences.setLanguage(language) }
    fun setShowSanskrit(show: Boolean) = launch { updatePreferences.setShowSanskrit(show) }
    fun setShowTransliteration(show: Boolean) = launch { updatePreferences.setShowTransliteration(show) }
    fun setAnimations(enabled: Boolean) = launch { updatePreferences.setAnimationsEnabled(enabled) }
    fun setTheme(mode: ThemeMode) = launch { updatePreferences.setThemeMode(mode) }
    fun setTextScale(scale: Float) = launch { updatePreferences.setTextScale(scale) }

    fun restartOnboarding(onRestarted: () -> Unit) {
        viewModelScope.launch {
            updatePreferences.resetOnboarding()
            onRestarted()
        }
    }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
