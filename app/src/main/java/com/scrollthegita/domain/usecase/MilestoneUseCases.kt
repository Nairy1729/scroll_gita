package com.scrollthegita.domain.usecase

import com.scrollthegita.core.model.PersonalReflection
import com.scrollthegita.core.model.ReadingHistory
import com.scrollthegita.core.model.SearchFilter
import com.scrollthegita.core.model.SearchResult
import com.scrollthegita.core.model.Shloka
import com.scrollthegita.domain.repository.ReadingHistoryRepository
import com.scrollthegita.domain.repository.ReflectionRepository
import com.scrollthegita.domain.repository.SearchRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class SearchShlokasUseCase @Inject constructor(
    private val repository: SearchRepository,
) {
    suspend operator fun invoke(rawQuery: String, filter: SearchFilter): List<SearchResult> =
        repository.search(rawQuery, filter)
}

class RecordSettledReadUseCase @Inject constructor(
    private val repository: ReadingHistoryRepository,
) {
    suspend operator fun invoke(shloka: Shloka, nowEpochMillis: Long): Boolean =
        repository.recordSettledRead(shloka, nowEpochMillis)
}

class ObserveReadingHistoryUseCase @Inject constructor(
    private val repository: ReadingHistoryRepository,
) {
    operator fun invoke(): Flow<List<ReadingHistory>> = repository.observeAll()
}

class ObserveReflectionsUseCase @Inject constructor(
    private val repository: ReflectionRepository,
) {
    operator fun invoke(): Flow<List<PersonalReflection>> = repository.observeAll()
    fun one(shlokaId: String): Flow<PersonalReflection?> = repository.observe(shlokaId)
}

class SaveReflectionUseCase @Inject constructor(
    private val repository: ReflectionRepository,
) {
    suspend operator fun invoke(shlokaId: String, body: String, nowEpochMillis: Long) =
        repository.save(shlokaId, body, nowEpochMillis)
}

class DeleteReflectionUseCase @Inject constructor(
    private val repository: ReflectionRepository,
) {
    suspend operator fun invoke(shlokaId: String) = repository.delete(shlokaId)
}
