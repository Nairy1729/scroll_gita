package com.scrollthegita.data.repository

import com.scrollthegita.core.database.ReadingHistoryDao
import com.scrollthegita.core.database.ReadingHistoryEntity
import com.scrollthegita.core.model.ReadingHistory
import com.scrollthegita.core.model.Shloka
import com.scrollthegita.domain.repository.ReadingHistoryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ReadingHistoryRepositoryImpl @Inject constructor(
    private val dao: ReadingHistoryDao,
) : ReadingHistoryRepository {
    override fun observeAll(): Flow<List<ReadingHistory>> = dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun recordSettledRead(shloka: Shloka, nowEpochMillis: Long): Boolean {
        val existing = dao.find(shloka.id)
        if (existing == null) {
            dao.insert(
                ReadingHistoryEntity(
                    shlokaId = shloka.id,
                    chapter = shloka.chapter,
                    verse = shloka.verse,
                    firstReadAtEpochMillis = nowEpochMillis,
                    lastReadAtEpochMillis = nowEpochMillis,
                    settledReadCount = 1,
                ),
            )
            return true
        }
        dao.update(
            existing.copy(
                chapter = shloka.chapter,
                verse = shloka.verse,
                lastReadAtEpochMillis = nowEpochMillis,
                settledReadCount = existing.settledReadCount + 1,
            ),
        )
        return false
    }

    private fun ReadingHistoryEntity.toDomain() = ReadingHistory(
        shlokaId = shlokaId,
        chapter = chapter,
        verse = verse,
        firstReadAtEpochMillis = firstReadAtEpochMillis,
        lastReadAtEpochMillis = lastReadAtEpochMillis,
        settledReadCount = settledReadCount,
    )
}
