package com.scrollthegita.data.repository

import com.scrollthegita.core.database.ReflectionDao
import com.scrollthegita.core.database.ReflectionEntity
import com.scrollthegita.core.model.PersonalReflection
import com.scrollthegita.core.model.ReflectionPolicy
import com.scrollthegita.domain.repository.ReflectionRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ReflectionRepositoryImpl @Inject constructor(
    private val dao: ReflectionDao,
) : ReflectionRepository {
    override fun observeAll(): Flow<List<PersonalReflection>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observe(shlokaId: String): Flow<PersonalReflection?> =
        dao.observe(shlokaId).map { it?.toDomain() }

    override suspend fun save(shlokaId: String, body: String, nowEpochMillis: Long) {
        val trimmed = body.trim()
        require(trimmed.isNotEmpty()) { "An empty reflection is not saved. Delete it instead." }
        require(trimmed.length <= ReflectionPolicy.MAX_BODY_LENGTH) { "Reflection is too long." }
        val existing = dao.find(shlokaId)
        if (existing == null) {
            dao.insert(
                ReflectionEntity(
                    shlokaId = shlokaId,
                    body = trimmed,
                    createdAtEpochMillis = nowEpochMillis,
                    updatedAtEpochMillis = nowEpochMillis,
                ),
            )
        } else {
            dao.update(existing.copy(body = trimmed, updatedAtEpochMillis = nowEpochMillis))
        }
    }

    override suspend fun delete(shlokaId: String) {
        dao.delete(shlokaId)
    }

    private fun ReflectionEntity.toDomain() = PersonalReflection(
        shlokaId = shlokaId,
        body = body,
        createdAtEpochMillis = createdAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis,
    )
}
