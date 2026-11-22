package com.scrollthegita.feature.feed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.scrollthegita.core.common.Clock
import com.scrollthegita.core.common.UserFacingError
import com.scrollthegita.core.model.Bookmark
import com.scrollthegita.core.model.ReadingPolicy
import com.scrollthegita.core.model.ReadingProgress
import com.scrollthegita.core.model.Shloka
import com.scrollthegita.core.model.UserPreferences
import com.scrollthegita.domain.usecase.ObserveBookmarksUseCase
import com.scrollthegita.domain.usecase.ObserveReadingProgressUseCase
import com.scrollthegita.domain.usecase.ObserveShlokasUseCase
import com.scrollthegita.domain.usecase.ObserveUserPreferencesUseCase
import com.scrollthegita.domain.usecase.RecordSettledReadUseCase
import com.scrollthegita.domain.usecase.SaveReadingProgressUseCase
import com.scrollthegita.domain.usecase.ToggleBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

enum class BookmarkNote { Saved, Removed }

sealed interface FeedUiState {
    data object Loading : FeedUiState
    data class Content(
        val shlokas: List<Shloka>,
        val resumeIndex: Int,
        val bookmarkedIds: Set<String>,
        val preferences: UserPreferences,
        val details: Shloka?,
        val bookmarkNote: BookmarkNote?,
    ) : FeedUiState
    data object Empty : FeedUiState
    data class Error(val error: UserFacingError) : FeedUiState
}

@HiltViewModel
class FeedViewModel @Inject constructor(
    observeShlokas: ObserveShlokasUseCase,
    observeBookmarks: ObserveBookmarksUseCase,
    observePreferences: ObserveUserPreferencesUseCase,
    private val observeProgress: ObserveReadingProgressUseCase,
    private val saveProgress: SaveReadingProgressUseCase,
    private val toggleBookmark: ToggleBookmarkUseCase,
    private val recordSettledRead: RecordSettledReadUseCase,
    private val clock: Clock,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val detailsOpen = savedStateHandle.getStateFlow(KEY_DETAILS_OPEN, false)
    private val bookmarkNote = MutableStateFlow<BookmarkNote?>(null)
    private val currentIndex = MutableStateFlow(0)
    private var resumeIndex = -1
    private var settleJob: Job? = null
    private var settleId: String? = null
    private var sessionSettledReads = 0
    private val _pause = MutableStateFlow(false)
    val pause: StateFlow<Boolean> = _pause.asStateFlow()
    private var shlokas: List<Shloka> = emptyList()

    private val _state = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val state: StateFlow<FeedUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                observeShlokas(),
                observeBookmarks(),
                observePreferences(),
                detailsOpen,
                bookmarkNote,
            ) { pages, marks, prefs, details, note ->
                Snapshot(pages, marks, prefs, details, note)
            }.catch {
                _state.value = FeedUiState.Error(UserFacingError.ContentUnavailable)
            }.collect { snapshot ->
                shlokas = snapshot.shlokas
                if (snapshot.shlokas.isEmpty()) {
                    _state.value = FeedUiState.Empty
                    return@collect
                }
                if (resumeIndex < 0) {
                    val progress = observeProgress().first()
                    resumeIndex = indexOf(snapshot.shlokas, progress?.lastShlokaId)
                    currentIndex.value = resumeIndex
                }
                val page = currentIndex.value.coerceIn(0, snapshot.shlokas.lastIndex)
                _state.value = FeedUiState.Content(
                    shlokas = snapshot.shlokas,
                    resumeIndex = resumeIndex.coerceIn(0, snapshot.shlokas.lastIndex),
                    bookmarkedIds = snapshot.bookmarks.map { it.shlokaId }.toSet(),
                    preferences = snapshot.preferences,
                    details = if (snapshot.detailsOpen) snapshot.shlokas.getOrNull(page) else null,
                    bookmarkNote = snapshot.note,
                )
            }
        }
    }

    fun onPageSettled(index: Int, fromJump: Boolean = false) {
        if (index !in shlokas.indices) return
        currentIndex.value = index
        // Keep this off the emitted state. Publishing it would recompose every page on each swipe.
        // The pager saves its own page, and reading progress is what cold start restores.
        resumeIndex = index
        val shloka = shlokas[index]
        viewModelScope.launch {
            saveProgress(
                ReadingProgress(
                    lastShlokaId = shloka.id,
                    lastChapter = shloka.chapter,
                    lastVerse = shloka.verse,
                    updatedAtEpochMillis = clock.now(),
                ),
            )
        }
        if (settleId == shloka.id) return
        settleJob?.cancel()
        settleId = shloka.id
        settleJob = viewModelScope.launch {
            delay(ReadingPolicy.SETTLED_READ_MILLIS)
            if (settleId != shloka.id) return@launch
            recordSettledRead(shloka, clock.now())
            if (fromJump) return@launch
            val pauses = (_state.value as? FeedUiState.Content)?.preferences?.mindfulPauses != false
            sessionSettledReads += 1
            if (pauses && sessionSettledReads % ReadingPolicy.MINDFUL_PAUSE_EVERY == 0) {
                _pause.value = true
            }
        }
    }

    fun dismissPause() {
        _pause.value = false
    }

    fun toggleSave() {
        val content = _state.value as? FeedUiState.Content ?: return
        val shloka = content.shlokas.getOrNull(currentIndex.value) ?: return
        val saved = shloka.id in content.bookmarkedIds
        viewModelScope.launch {
            val nowSaved = toggleBookmark(shloka.id, currentlySaved = saved)
            val note = if (nowSaved) BookmarkNote.Saved else BookmarkNote.Removed
            bookmarkNote.value = note
            delay(1_600)
            // A later toggle must keep its own confirmation.
            bookmarkNote.update { current -> if (current == note) null else current }
        }
    }

    fun openDetails() {
        savedStateHandle[KEY_DETAILS_OPEN] = true
    }

    fun dismissDetails() {
        savedStateHandle[KEY_DETAILS_OPEN] = false
    }

    private fun indexOf(pages: List<Shloka>, id: String?): Int {
        val found = pages.indexOfFirst { it.id == id }
        return if (found < 0) 0 else found
    }

    private data class Snapshot(
        val shlokas: List<Shloka>,
        val bookmarks: List<Bookmark>,
        val preferences: UserPreferences,
        val detailsOpen: Boolean,
        val note: BookmarkNote?,
    )

    private companion object {
        const val KEY_DETAILS_OPEN = "details_open"
    }
}
