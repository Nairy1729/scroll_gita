package com.scrollthegita.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrollthegita.core.common.UserFacingError
import com.scrollthegita.core.model.SavedShloka
import com.scrollthegita.domain.usecase.ObserveBookmarksUseCase
import com.scrollthegita.domain.usecase.ObserveShlokasUseCase
import com.scrollthegita.domain.usecase.ToggleBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface LibraryUiState {
    data object Loading : LibraryUiState
    data class Content(val saved: List<SavedShloka>) : LibraryUiState
    data class Error(val error: UserFacingError) : LibraryUiState
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    observeBookmarks: ObserveBookmarksUseCase,
    observeShlokas: ObserveShlokasUseCase,
    private val toggleBookmark: ToggleBookmarkUseCase,
) : ViewModel() {
    val state: StateFlow<LibraryUiState> = combine(observeBookmarks(), observeShlokas()) { marks, shlokas ->
        val byId = shlokas.associateBy { it.id }
        marks.mapNotNull { mark -> byId[mark.shlokaId]?.let { SavedShloka(mark, it) } }
    }.map<List<SavedShloka>, LibraryUiState> { LibraryUiState.Content(it) }
        .catch { emit(LibraryUiState.Error(UserFacingError.ContentUnavailable)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState.Loading)

    fun remove(shlokaId: String) {
        viewModelScope.launch { toggleBookmark(shlokaId, currentlySaved = true) }
    }

    fun restore(shlokaId: String) {
        viewModelScope.launch { toggleBookmark(shlokaId, currentlySaved = false) }
    }
}
