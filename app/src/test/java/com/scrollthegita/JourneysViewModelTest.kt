package com.scrollthegita

import com.scrollthegita.core.model.Journey
import com.scrollthegita.core.model.JourneyProgress
import com.scrollthegita.domain.repository.JourneyRepository
import com.scrollthegita.feature.journeys.JourneysUiState
import com.scrollthegita.feature.journeys.JourneysViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JourneysViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun mapsJourneysAndProgressIntoUiState() = runBlocking {
        val fakeRepo = FakeJourneyRepository()
        fakeRepo.setJourneys(
            listOf(
                Journey("duty", "Gita on Duty", "Duty description", listOf("2.47", "2.48", "3.35")),
                Journey("equanimity", "Gita on Equanimity", "Equanimity description", listOf("2.48", "2.50")),
            ),
        )
        fakeRepo.saveProgress("duty", 1)
        fakeRepo.saveProgress("equanimity", 1)

        val viewModel = JourneysViewModel(fakeRepo)

        val state = viewModel.state.first { it is JourneysUiState.Content } as JourneysUiState.Content
        assertEquals(2, state.items.size)

        val dutyItem = state.items.first { it.journey.id == "duty" }
        assertEquals(1, dutyItem.progress?.currentIndex)
        assertFalse("Duty journey with index 1 of 3 is not yet complete", dutyItem.isComplete)

        val equanimityItem = state.items.first { it.journey.id == "equanimity" }
        assertEquals(1, equanimityItem.progress?.currentIndex)
        assertTrue("Equanimity journey with index 1 of 2 is complete", equanimityItem.isComplete)
    }

    @Test
    fun unstartedJourneyHasNullProgressAndNotComplete() = runBlocking {
        val fakeRepo = FakeJourneyRepository()
        fakeRepo.setJourneys(
            listOf(
                Journey("duty", "Gita on Duty", "Duty description", listOf("2.47", "2.48")),
            ),
        )

        val viewModel = JourneysViewModel(fakeRepo)

        val state = viewModel.state.first { it is JourneysUiState.Content } as JourneysUiState.Content
        val item = state.items.first()
        assertEquals(null, item.progress)
        assertFalse(item.isComplete)
    }
}

private class FakeJourneyRepository : JourneyRepository {
    private val journeysFlow = MutableStateFlow<List<Journey>>(emptyList())
    private val progressMap = MutableStateFlow<Map<String, JourneyProgress>>(emptyMap())

    fun setJourneys(list: List<Journey>) {
        journeysFlow.value = list
    }

    override fun observeJourneys(): Flow<List<Journey>> = journeysFlow

    override fun observeAllProgress(): Flow<List<JourneyProgress>> =
        progressMap.map { it.values.toList() }

    override fun observeProgress(journeyId: String): Flow<JourneyProgress?> =
        progressMap.map { it[journeyId] }

    override suspend fun saveProgress(journeyId: String, currentIndex: Int) {
        val current = progressMap.value.toMutableMap()
        current[journeyId] = JourneyProgress(
            journeyId = journeyId,
            currentIndex = currentIndex,
            lastReadAtEpochMillis = System.currentTimeMillis(),
        )
        progressMap.value = current
    }

    override suspend fun getJourney(journeyId: String): Journey? {
        return journeysFlow.value.firstOrNull { it.id == journeyId }
    }
}
