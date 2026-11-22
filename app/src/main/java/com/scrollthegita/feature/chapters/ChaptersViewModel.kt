package com.scrollthegita.feature.chapters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrollthegita.core.common.UserFacingError
import com.scrollthegita.core.model.Chapter
import com.scrollthegita.core.model.Shloka
import com.scrollthegita.domain.usecase.ObserveChaptersUseCase
import com.scrollthegita.domain.usecase.ObserveShlokasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ChapterListItem(
    val chapter: Chapter,
    val availableShlokas: Int,
    val firstShlokaId: String?,
)

sealed interface ChaptersUiState {
    data object Loading : ChaptersUiState
    data class Content(val chapters: List<ChapterListItem>) : ChaptersUiState
    data class Error(val error: UserFacingError) : ChaptersUiState
}

@HiltViewModel
class ChaptersViewModel @Inject constructor(
    observeChapters: ObserveChaptersUseCase,
    observeShlokas: ObserveShlokasUseCase,
) : ViewModel() {
    val state: StateFlow<ChaptersUiState> = combine(
        observeChapters(),
        observeShlokas(),
    ) { chapters, shlokas ->
        chapters.toItems(shlokas)
    }.map<List<ChapterListItem>, ChaptersUiState> { ChaptersUiState.Content(it) }
        .catch { emit(ChaptersUiState.Error(UserFacingError.ContentUnavailable)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChaptersUiState.Loading)

    private fun List<Chapter>.toItems(shlokas: List<Shloka>): List<ChapterListItem> = map { chapter ->
        val available = shlokas.filter { it.chapter == chapter.number }
        ChapterListItem(
            chapter = chapter,
            availableShlokas = available.size,
            firstShlokaId = available.firstOrNull()?.id,
        )
    }
}
