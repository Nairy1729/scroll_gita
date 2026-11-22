package com.scrollthegita.feature.journeys

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrollthegita.core.model.Journey
import com.scrollthegita.core.model.JourneyProgress
import com.scrollthegita.domain.repository.JourneyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class JourneyItem(
    val journey: Journey,
    val progress: JourneyProgress?,
    val isComplete: Boolean,
)

sealed interface JourneysUiState {
    data object Loading : JourneysUiState
    data class Content(val items: List<JourneyItem>) : JourneysUiState
}

@HiltViewModel
class JourneysViewModel @Inject constructor(
    private val journeyRepository: JourneyRepository,
) : ViewModel() {

    val state: StateFlow<JourneysUiState> = combine(
        journeyRepository.observeJourneys(),
        journeyRepository.observeAllProgress(),
    ) { journeys, progresses ->
        val progressMap = progresses.associateBy { it.journeyId }
        val items = journeys.map { journey ->
            val progress = progressMap[journey.id]
            val isComplete = progress != null && progress.currentIndex >= journey.shlokas.size - 1
            JourneyItem(
                journey = journey,
                progress = progress,
                isComplete = isComplete,
            )
        }
        JourneysUiState.Content(items)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = JourneysUiState.Loading,
    )
}
