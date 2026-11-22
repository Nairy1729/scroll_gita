package com.scrollthegita.feature.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrollthegita.core.common.UserFacingError
import com.scrollthegita.core.model.SearchFilter
import com.scrollthegita.core.model.SearchResult
import com.scrollthegita.core.model.VerseReference
import com.scrollthegita.domain.usecase.ObserveUserPreferencesUseCase
import com.scrollthegita.domain.usecase.SearchShlokasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Results(val items: List<SearchResult>) : SearchUiState
    data object NoResults : SearchUiState
    data class Error(val error: UserFacingError) : SearchUiState
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchShlokas: SearchShlokasUseCase,
    observePreferences: ObserveUserPreferencesUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val query: StateFlow<String> = savedStateHandle.getStateFlow(KEY_QUERY, "")
    val filterName: StateFlow<String> = savedStateHandle.getStateFlow(KEY_FILTER, SearchFilter.ALL.name)
    val showSanskrit: StateFlow<Boolean> = observePreferences()
        .map { it.showSanskrit }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    private val retryNonce = MutableStateFlow(0)
    private val _results = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val results: StateFlow<SearchUiState> = _results.asStateFlow()

    init {
        viewModelScope.launch {
            combine(query, filterName, retryNonce) { text, filter, nonce -> Triple(text, filter, nonce) }
                .collect { (text, filter, _) ->
                    val trimmed = text.trim()
                    if (trimmed.isEmpty()) {
                        _results.value = SearchUiState.Idle
                        return@collect
                    }
                    if (VerseReference.parse(trimmed) == null) delay(TEXT_DEBOUNCE_MILLIS)
                    _results.value = SearchUiState.Loading
                    val selected = runCatching { SearchFilter.valueOf(filter) }.getOrDefault(SearchFilter.ALL)
                    _results.value = try {
                        val found = searchShlokas(trimmed, selected)
                        if (found.isEmpty()) SearchUiState.NoResults else SearchUiState.Results(found)
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        SearchUiState.Error(UserFacingError.ContentUnavailable)
                    }
                }
        }
    }

    fun setQuery(value: String) {
        savedStateHandle[KEY_QUERY] = value
    }

    fun setFilter(filter: SearchFilter) {
        savedStateHandle[KEY_FILTER] = filter.name
    }

    fun clearQuery() {
        setQuery("")
    }

    fun retry() {
        retryNonce.value = retryNonce.value + 1
    }

    private companion object {
        const val KEY_QUERY = "search_query"
        const val KEY_FILTER = "search_filter"
        const val TEXT_DEBOUNCE_MILLIS = 300L
    }
}
