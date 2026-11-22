package com.scrollthegita.data.repository

import android.content.Context
import com.scrollthegita.core.common.Clock
import com.scrollthegita.core.database.JourneyProgressDao
import com.scrollthegita.core.database.JourneyProgressEntity
import com.scrollthegita.core.database.toDomain
import com.scrollthegita.core.model.Journey
import com.scrollthegita.core.model.JourneyProgress
import com.scrollthegita.data.content.JourneyJson
import com.scrollthegita.domain.repository.JourneyRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

@Singleton
class JourneyRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val journeyProgressDao: JourneyProgressDao,
    private val clock: Clock,
) : JourneyRepository {

    private var cachedJourneys: List<Journey>? = null

    private fun loadJourneys(): List<Journey> {
        cachedJourneys?.let { return it }
        val json = runCatching {
            context.assets.open(JourneyJson.ASSET_PATH).bufferedReader().use { it.readText() }
        }.getOrNull().orEmpty()
        val parsed = JourneyJson.parse(json)
        cachedJourneys = parsed
        return parsed
    }

    override fun observeJourneys(): Flow<List<Journey>> = flow {
        emit(loadJourneys())
    }

    override fun observeAllProgress(): Flow<List<JourneyProgress>> =
        journeyProgressDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeProgress(journeyId: String): Flow<JourneyProgress?> =
        journeyProgressDao.observe(journeyId).map { it?.toDomain() }

    override suspend fun saveProgress(journeyId: String, currentIndex: Int) {
        journeyProgressDao.save(
            JourneyProgressEntity(
                journeyId = journeyId,
                currentIndex = currentIndex,
                lastReadAtEpochMillis = clock.now(),
            ),
        )
    }

    override suspend fun getJourney(journeyId: String): Journey? {
        return loadJourneys().firstOrNull { it.id == journeyId }
    }
}
