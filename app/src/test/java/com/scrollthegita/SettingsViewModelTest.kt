package com.scrollthegita

import com.scrollthegita.core.model.OnboardingDraft
import com.scrollthegita.core.model.PreferredLanguage
import com.scrollthegita.core.model.ReadingProgress
import com.scrollthegita.core.model.UserPreferences
import com.scrollthegita.domain.repository.UserSettingsRepository
import com.scrollthegita.domain.usecase.ObserveUserPreferencesUseCase
import com.scrollthegita.domain.usecase.UpdateUserPreferencesUseCase
import com.scrollthegita.feature.settings.SettingsUiState
import com.scrollthegita.feature.settings.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun languageChangeUpdatesUiState() = runBlocking {
        val repository = FakeUserSettingsRepository()
        val viewModel = SettingsViewModel(
            ObserveUserPreferencesUseCase(repository),
            UpdateUserPreferencesUseCase(repository),
        )
        viewModel.setLanguage(PreferredLanguage.HINDI)
        val content = viewModel.state.first {
            it is SettingsUiState.Content && it.preferences.preferredLanguage == PreferredLanguage.HINDI
        } as SettingsUiState.Content
        assertEquals(PreferredLanguage.HINDI, content.preferences.preferredLanguage)
    }
}

private class FakeUserSettingsRepository : UserSettingsRepository {
    private val preferences = MutableStateFlow(UserPreferences())
    private val progress = MutableStateFlow<ReadingProgress?>(null)

    override fun observePreferences(): Flow<UserPreferences> = preferences

    override suspend fun updatePreferences(transform: (UserPreferences) -> UserPreferences) {
        preferences.value = transform(preferences.value)
    }

    override suspend fun completeOnboarding(draft: OnboardingDraft) {
        preferences.value = preferences.value.copy(
            onboardingCompleted = true,
            showSanskrit = draft.showSanskrit,
            showTransliteration = draft.showTransliteration,
            preferredLanguage = draft.preferredLanguage,
            animationsEnabled = draft.animationsEnabled,
        )
    }

    override suspend fun resetOnboarding() {
        preferences.value = preferences.value.copy(onboardingCompleted = false)
    }

    override fun observeProgress(): Flow<ReadingProgress?> = progress

    override suspend fun saveProgress(progress: ReadingProgress) {
        this.progress.value = progress
    }
}
