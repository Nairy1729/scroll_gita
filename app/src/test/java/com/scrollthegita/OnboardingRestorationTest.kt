package com.scrollthegita

import androidx.lifecycle.SavedStateHandle
import com.scrollthegita.core.model.OnboardingDraft
import com.scrollthegita.core.model.PreferredLanguage
import com.scrollthegita.core.model.ReadingProgress
import com.scrollthegita.core.model.UserPreferences
import com.scrollthegita.domain.repository.UserSettingsRepository
import com.scrollthegita.domain.usecase.CompleteOnboardingUseCase
import com.scrollthegita.domain.usecase.ObserveUserPreferencesUseCase
import com.scrollthegita.feature.onboarding.OnboardingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingRestorationTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun editedDraftSurvivesProcessDeath() {
        val handle = SavedStateHandle()
        val repository = MemorySettings()
        val first = viewModel(handle, repository)
        first.setLanguage(PreferredLanguage.HINDI)
        first.setShowSanskrit(false)

        val restored = viewModel(handle, repository)
        assertEquals(PreferredLanguage.HINDI, restored.state.value.language)
        assertEquals(false, restored.state.value.showSanskrit)
        assertEquals(true, restored.state.value.ready)
    }

    private fun viewModel(handle: SavedStateHandle, repository: UserSettingsRepository) = OnboardingViewModel(
        savedStateHandle = handle,
        observePreferences = ObserveUserPreferencesUseCase(repository),
        completeOnboarding = CompleteOnboardingUseCase(repository),
    )

    private class MemorySettings : UserSettingsRepository {
        private val preferences = MutableStateFlow(UserPreferences())
        override fun observePreferences(): Flow<UserPreferences> = preferences
        override suspend fun updatePreferences(transform: (UserPreferences) -> UserPreferences) = Unit
        override suspend fun completeOnboarding(draft: OnboardingDraft) = Unit
        override suspend fun resetOnboarding() = Unit
        override fun observeProgress(): Flow<ReadingProgress?> = flowOf(null)
        override suspend fun saveProgress(progress: ReadingProgress) = Unit
    }
}
