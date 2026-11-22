package com.scrollthegita.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.scrollthegita.core.model.BackgroundStyle
import com.scrollthegita.core.model.OnboardingDraft
import com.scrollthegita.core.model.PreferredLanguage
import com.scrollthegita.core.model.ReadingProgress
import com.scrollthegita.core.model.ThemeMode
import com.scrollthegita.core.model.UserPreferences
import com.scrollthegita.domain.repository.UserSettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class UserSettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : UserSettingsRepository {

    override fun observePreferences(): Flow<UserPreferences> =
        dataStore.data.map { it.readUserPreferences() }

    override suspend fun updatePreferences(transform: (UserPreferences) -> UserPreferences) {
        dataStore.edit { prefs ->
            prefs.writeUserPreferences(transform(prefs.readUserPreferences()))
        }
    }

    override suspend fun completeOnboarding(draft: OnboardingDraft) {
        updatePreferences {
            it.copy(
                onboardingCompleted = true,
                showSanskrit = draft.showSanskrit,
                showTransliteration = draft.showTransliteration,
                preferredLanguage = draft.preferredLanguage,
                animationsEnabled = draft.animationsEnabled,
            )
        }
    }

    override suspend fun resetOnboarding() {
        updatePreferences { it.copy(onboardingCompleted = false) }
    }

    override fun observeProgress(): Flow<ReadingProgress?> =
        dataStore.data.map { it.toProgress() }

    override suspend fun saveProgress(progress: ReadingProgress) {
        dataStore.edit { prefs ->
            prefs[Keys.LAST_ID] = progress.lastShlokaId
            prefs[Keys.LAST_CHAPTER] = progress.lastChapter
            prefs[Keys.LAST_VERSE] = progress.lastVerse
            prefs[Keys.PROGRESS_AT] = progress.updatedAtEpochMillis
        }
    }

    private fun Preferences.readUserPreferences(): UserPreferences = UserPreferences(
        onboardingCompleted = this[Keys.ONBOARDING] ?: false,
        preferredLanguage = this[Keys.LANGUAGE].toEnum(PreferredLanguage.ENGLISH),
        showSanskrit = this[Keys.SANSKRIT] ?: true,
        showTransliteration = this[Keys.TRANSLITERATION] ?: true,
        animationsEnabled = this[Keys.ANIMATIONS] ?: true,
        themeMode = this[Keys.THEME].toEnum(ThemeMode.DARK),
        textScale = (this[Keys.TEXT_SCALE] ?: 1f).coerceIn(0.85f, 1.35f),
        mindfulPauses = this[Keys.MINDFUL_PAUSES] ?: true,
        preferredBackgroundStyle = this[Keys.BACKGROUND_STYLE]?.let { name ->
            runCatching { BackgroundStyle.valueOf(name) }.getOrNull()
        },
    )

    private fun MutablePreferences.writeUserPreferences(value: UserPreferences) {
        this[Keys.ONBOARDING] = value.onboardingCompleted
        this[Keys.LANGUAGE] = value.preferredLanguage.name
        this[Keys.SANSKRIT] = value.showSanskrit
        this[Keys.TRANSLITERATION] = value.showTransliteration
        this[Keys.ANIMATIONS] = value.animationsEnabled
        this[Keys.THEME] = value.themeMode.name
        this[Keys.TEXT_SCALE] = value.textScale
        this[Keys.MINDFUL_PAUSES] = value.mindfulPauses
        if (value.preferredBackgroundStyle != null) {
            this[Keys.BACKGROUND_STYLE] = value.preferredBackgroundStyle.name
        } else {
            this.remove(Keys.BACKGROUND_STYLE)
        }
    }

    private fun Preferences.toProgress(): ReadingProgress? {
        val id = this[Keys.LAST_ID] ?: return null
        return ReadingProgress(
            lastShlokaId = id,
            lastChapter = this[Keys.LAST_CHAPTER] ?: return null,
            lastVerse = this[Keys.LAST_VERSE] ?: return null,
            updatedAtEpochMillis = this[Keys.PROGRESS_AT] ?: 0L,
        )
    }

    private inline fun <reified T : Enum<T>> String?.toEnum(default: T): T {
        if (this == null) return default
        return runCatching { enumValueOf<T>(this) }.getOrDefault(default)
    }

    private object Keys {
        val ONBOARDING = booleanPreferencesKey("onboarding_completed")
        val LANGUAGE = stringPreferencesKey("preferred_language")
        val SANSKRIT = booleanPreferencesKey("show_sanskrit")
        val TRANSLITERATION = booleanPreferencesKey("show_transliteration")
        val ANIMATIONS = booleanPreferencesKey("animations_enabled")
        val THEME = stringPreferencesKey("theme_mode")
        val TEXT_SCALE = floatPreferencesKey("text_scale")
        val MINDFUL_PAUSES = booleanPreferencesKey("mindful_pauses")
        val BACKGROUND_STYLE = stringPreferencesKey("preferred_background_style")
        val LAST_ID = stringPreferencesKey("last_shloka_id")
        val LAST_CHAPTER = intPreferencesKey("last_chapter")
        val LAST_VERSE = intPreferencesKey("last_verse")
        val PROGRESS_AT = longPreferencesKey("progress_updated_at")
    }
}
