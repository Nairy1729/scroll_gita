package com.scrollthegita

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.scrollthegita.core.model.OnboardingDraft
import com.scrollthegita.core.model.PreferredLanguage
import com.scrollthegita.core.model.ReadingProgress
import com.scrollthegita.core.model.ThemeMode
import com.scrollthegita.data.repository.UserSettingsRepositoryImpl
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserSettingsRepositoryTest {
    @Test
    fun readingProgressIsSavedAndRestored() = runBlocking {
        val repository = repository()
        val progress = ReadingProgress("6.26", 6, 26, 99L)
        repository.saveProgress(progress)
        assertEquals(progress, repository.observeProgress().first())
    }

    @Test
    fun onboardingCompletionIsPersisted() = runBlocking {
        val repository = repository()
        assertFalse(repository.observePreferences().first().onboardingCompleted)
        repository.completeOnboarding(
            OnboardingDraft(
                showSanskrit = false,
                showTransliteration = true,
                preferredLanguage = PreferredLanguage.HINDI,
                animationsEnabled = false,
            ),
        )
        val saved = repository.observePreferences().first()
        assertTrue(saved.onboardingCompleted)
        assertFalse(saved.showSanskrit)
        assertEquals(PreferredLanguage.HINDI, saved.preferredLanguage)
        assertFalse(saved.animationsEnabled)
    }

    @Test
    fun preferenceEditsRoundTrip() = runBlocking {
        val repository = repository()
        repository.updatePreferences { it.copy(themeMode = ThemeMode.PARCHMENT, textScale = 1.2f) }
        val saved = repository.observePreferences().first()
        assertEquals(ThemeMode.PARCHMENT, saved.themeMode)
        assertEquals(1.2f, saved.textScale, 0.001f)
    }

    private fun repository(): UserSettingsRepositoryImpl {
        val file = File.createTempFile("gita-settings", ".preferences_pb")
        file.delete()
        val store = PreferenceDataStoreFactory.create(produceFile = { file })
        return UserSettingsRepositoryImpl(store)
    }
}
