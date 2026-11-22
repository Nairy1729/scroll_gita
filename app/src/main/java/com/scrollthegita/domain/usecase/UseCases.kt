package com.scrollthegita.domain.usecase

import com.scrollthegita.core.model.Bookmark
import com.scrollthegita.core.model.Chapter
import com.scrollthegita.core.model.OnboardingDraft
import com.scrollthegita.core.model.PreferredLanguage
import com.scrollthegita.core.model.ReadingProgress
import com.scrollthegita.core.model.Shloka
import com.scrollthegita.core.model.ThemeMode
import com.scrollthegita.core.model.UserPreferences
import com.scrollthegita.domain.repository.GitaContentRepository
import com.scrollthegita.domain.repository.UserSettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class EnsureContentSeededUseCase @Inject constructor(
    private val repository: GitaContentRepository,
) {
    suspend operator fun invoke() = repository.ensureSeeded()
}

class ObserveShlokasUseCase @Inject constructor(
    private val repository: GitaContentRepository,
) {
    operator fun invoke(): Flow<List<Shloka>> = repository.observeShlokas()
}

class ObserveChaptersUseCase @Inject constructor(
    private val repository: GitaContentRepository,
) {
    operator fun invoke(): Flow<List<Chapter>> = repository.observeChapters()
}

class ObserveBookmarksUseCase @Inject constructor(
    private val repository: GitaContentRepository,
) {
    operator fun invoke(): Flow<List<Bookmark>> = repository.observeBookmarks()
}

class ToggleBookmarkUseCase @Inject constructor(
    private val repository: GitaContentRepository,
) {
    suspend operator fun invoke(shlokaId: String, currentlySaved: Boolean): Boolean =
        repository.setBookmarked(shlokaId, bookmarked = !currentlySaved)
}

class ObserveUserPreferencesUseCase @Inject constructor(
    private val repository: UserSettingsRepository,
) {
    operator fun invoke(): Flow<UserPreferences> = repository.observePreferences()
}

class UpdateUserPreferencesUseCase @Inject constructor(
    private val repository: UserSettingsRepository,
) {
    suspend fun setLanguage(language: PreferredLanguage) {
        repository.updatePreferences { it.copy(preferredLanguage = language) }
    }

    suspend fun setShowSanskrit(show: Boolean) {
        repository.updatePreferences { it.copy(showSanskrit = show) }
    }

    suspend fun setShowTransliteration(show: Boolean) {
        repository.updatePreferences { it.copy(showTransliteration = show) }
    }

    suspend fun setAnimationsEnabled(enabled: Boolean) {
        repository.updatePreferences { it.copy(animationsEnabled = enabled) }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        repository.updatePreferences { it.copy(themeMode = mode) }
    }

    suspend fun setTextScale(scale: Float) {
        repository.updatePreferences { it.copy(textScale = scale.coerceIn(0.85f, 1.35f)) }
    }

    suspend fun setMindfulPauses(enabled: Boolean) {
        repository.updatePreferences { it.copy(mindfulPauses = enabled) }
    }

    suspend fun resetOnboarding() = repository.resetOnboarding()
}

class CompleteOnboardingUseCase @Inject constructor(
    private val repository: UserSettingsRepository,
) {
    suspend operator fun invoke(draft: OnboardingDraft) = repository.completeOnboarding(draft)
}

class ObserveReadingProgressUseCase @Inject constructor(
    private val repository: UserSettingsRepository,
) {
    operator fun invoke(): Flow<ReadingProgress?> = repository.observeProgress()
}

class SaveReadingProgressUseCase @Inject constructor(
    private val repository: UserSettingsRepository,
) {
    suspend operator fun invoke(progress: ReadingProgress) = repository.saveProgress(progress)
}
