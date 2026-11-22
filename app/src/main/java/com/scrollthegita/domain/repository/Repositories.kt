package com.scrollthegita.domain.repository

import com.scrollthegita.core.model.Bookmark
import com.scrollthegita.core.model.Chapter
import com.scrollthegita.core.model.Journey
import com.scrollthegita.core.model.JourneyProgress
import com.scrollthegita.core.model.OnboardingDraft
import com.scrollthegita.core.model.PersonalReflection
import com.scrollthegita.core.model.ReadingHistory
import com.scrollthegita.core.model.ReadingProgress
import com.scrollthegita.core.model.SearchFilter
import com.scrollthegita.core.model.SearchResult
import com.scrollthegita.core.model.Shloka
import com.scrollthegita.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface GitaContentRepository {
    suspend fun ensureSeeded()

    /**
     * Updates rows with the same shloka id and inserts new ones.
     * Does not delete rows, so a bookmark survives when its id is still in the import.
     */
    suspend fun importContent(chapters: List<Chapter>, shlokas: List<Shloka>)

    fun observeShlokas(): Flow<List<Shloka>>
    fun observeChapters(): Flow<List<Chapter>>
    fun observeBookmarks(): Flow<List<Bookmark>>

    /** @return true when the shloka is bookmarked after the call. */
    suspend fun setBookmarked(shlokaId: String, bookmarked: Boolean): Boolean
}

interface UserSettingsRepository {
    fun observePreferences(): Flow<UserPreferences>
    suspend fun updatePreferences(transform: (UserPreferences) -> UserPreferences)
    suspend fun completeOnboarding(draft: OnboardingDraft)
    suspend fun resetOnboarding()
    fun observeProgress(): Flow<ReadingProgress?>
    suspend fun saveProgress(progress: ReadingProgress)
}

interface SearchRepository {
    suspend fun search(rawQuery: String, filter: SearchFilter): List<SearchResult>
}

interface ReadingHistoryRepository {
    fun observeAll(): Flow<List<ReadingHistory>>

    /** @return true when this is the first settled read of this shloka. */
    suspend fun recordSettledRead(shloka: Shloka, nowEpochMillis: Long): Boolean
}

interface ReflectionRepository {
    fun observeAll(): Flow<List<PersonalReflection>>
    fun observe(shlokaId: String): Flow<PersonalReflection?>
    suspend fun save(shlokaId: String, body: String, nowEpochMillis: Long)
    suspend fun delete(shlokaId: String)
}

interface JourneyRepository {
    fun observeJourneys(): Flow<List<Journey>>
    fun observeAllProgress(): Flow<List<JourneyProgress>>
    fun observeProgress(journeyId: String): Flow<JourneyProgress?>
    suspend fun saveProgress(journeyId: String, currentIndex: Int)
    suspend fun getJourney(journeyId: String): Journey?
}
