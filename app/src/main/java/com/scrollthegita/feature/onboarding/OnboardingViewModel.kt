package com.scrollthegita.feature.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrollthegita.core.model.OnboardingDraft
import com.scrollthegita.core.model.PreferredLanguage
import com.scrollthegita.domain.usecase.CompleteOnboardingUseCase
import com.scrollthegita.domain.usecase.ObserveUserPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val ready: Boolean = false,
    val showSanskrit: Boolean = true,
    val showTransliteration: Boolean = true,
    val language: PreferredLanguage = PreferredLanguage.ENGLISH,
    val animationsEnabled: Boolean = true,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    observePreferences: ObserveUserPreferencesUseCase,
    private val completeOnboarding: CompleteOnboardingUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(readDraft())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    init {
        if (savedStateHandle.get<Boolean>(KEY_EDITED) != true) {
            viewModelScope.launch {
                val prefs = observePreferences().first()
                if (savedStateHandle.get<Boolean>(KEY_EDITED) == true) return@launch
                val next = OnboardingUiState(
                    ready = true,
                    showSanskrit = prefs.showSanskrit,
                    showTransliteration = prefs.showTransliteration,
                    language = prefs.preferredLanguage,
                    animationsEnabled = prefs.animationsEnabled,
                )
                _state.value = next
            }
        }
    }

    fun setShowSanskrit(value: Boolean) = edit { it.copy(showSanskrit = value) }
    fun setShowTransliteration(value: Boolean) = edit { it.copy(showTransliteration = value) }
    fun setLanguage(value: PreferredLanguage) = edit { it.copy(language = value) }
    fun setAnimationsEnabled(value: Boolean) = edit { it.copy(animationsEnabled = value) }

    fun begin(onFinished: () -> Unit) {
        val draft = _state.value
        viewModelScope.launch {
            completeOnboarding(
                OnboardingDraft(
                    showSanskrit = draft.showSanskrit,
                    showTransliteration = draft.showTransliteration,
                    preferredLanguage = draft.language,
                    animationsEnabled = draft.animationsEnabled,
                ),
            )
            onFinished()
        }
    }

    private fun edit(transform: (OnboardingUiState) -> OnboardingUiState) {
        savedStateHandle[KEY_EDITED] = true
        val next = transform(_state.value).copy(ready = true)
        _state.value = next
        savedStateHandle[KEY_SANSKRIT] = next.showSanskrit
        savedStateHandle[KEY_TRANSLITERATION] = next.showTransliteration
        savedStateHandle[KEY_LANGUAGE] = next.language.name
        savedStateHandle[KEY_MOTION] = next.animationsEnabled
    }

    private fun readDraft(): OnboardingUiState {
        if (savedStateHandle.get<Boolean>(KEY_EDITED) != true) return OnboardingUiState()
        val language = savedStateHandle.get<String>(KEY_LANGUAGE)
            ?.let { runCatching { PreferredLanguage.valueOf(it) }.getOrNull() }
            ?: PreferredLanguage.ENGLISH
        return OnboardingUiState(
            ready = true,
            showSanskrit = savedStateHandle[KEY_SANSKRIT] ?: true,
            showTransliteration = savedStateHandle[KEY_TRANSLITERATION] ?: true,
            language = language,
            animationsEnabled = savedStateHandle[KEY_MOTION] ?: true,
        )
    }

    private companion object {
        const val KEY_EDITED = "onboarding_edited"
        const val KEY_SANSKRIT = "onboarding_sanskrit"
        const val KEY_TRANSLITERATION = "onboarding_transliteration"
        const val KEY_LANGUAGE = "onboarding_language"
        const val KEY_MOTION = "onboarding_motion"
    }
}
