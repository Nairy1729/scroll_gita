package com.scrollthegita.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ShlokaDao {
    @Query("SELECT * FROM shlokas ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<ShlokaEntity>>

    @Query("SELECT COUNT(*) FROM shlokas")
    suspend fun count(): Int

    @Query("SELECT * FROM shlokas")
    suspend fun listAll(): List<ShlokaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ShlokaEntity>)

    @Query("SELECT EXISTS(SELECT 1 FROM shlokas WHERE id = :id)")
    suspend fun exists(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: ShlokaEntity)

    @Update
    suspend fun update(entity: ShlokaEntity)
}

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters ORDER BY number ASC")
    fun observeAll(): Flow<List<ChapterEntity>>

    @Query("SELECT COUNT(*) FROM chapters")
    suspend fun count(): Int

    @Query("SELECT * FROM chapters")
    suspend fun listAll(): List<ChapterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ChapterEntity>)

    @Query("SELECT EXISTS(SELECT 1 FROM chapters WHERE number = :number)")
    suspend fun exists(number: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: ChapterEntity)

    @Update
    suspend fun update(entity: ChapterEntity)
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE shlokaId = :id)")
    suspend fun exists(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE shlokaId = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM bookmarks")
    suspend fun deleteAll()
}

@Dao
interface ReadingHistoryDao {
    @Query("SELECT * FROM reading_history")
    fun observeAll(): Flow<List<ReadingHistoryEntity>>

    @Query("SELECT * FROM reading_history WHERE shlokaId = :id")
    suspend fun find(id: String): ReadingHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: ReadingHistoryEntity)

    @Update
    suspend fun update(entity: ReadingHistoryEntity)
}

@Dao
interface SearchDao {
    @Query(
        """
        SELECT shlokas.* FROM shlokas
        JOIN shlokas_fts ON shlokas.rowid = shlokas_fts.rowid
        WHERE shlokas_fts MATCH :query
        """,
    )
    suspend fun searchFts(query: String): List<ShlokaEntity>

    @Query(
        """
        SELECT shlokas.id FROM shlokas
        JOIN shlokas_fts ON shlokas.rowid = shlokas_fts.rowid
        WHERE shlokas_fts MATCH :query
        """,
    )
    suspend fun searchFtsIds(query: String): List<String>

    @Query("SELECT * FROM shlokas WHERE searchTransliteration LIKE :like ESCAPE '\\'")
    suspend fun searchTransliteration(like: String): List<ShlokaEntity>

    @Query(
        """
        SELECT shlokas.id FROM shlokas
        INNER JOIN chapters ON chapters.number = shlokas.chapter
        WHERE shlokas.id = :exactId
        OR (:filter = 'ALL' AND (
            shlokas.searchSanskrit LIKE :like ESCAPE '\'
            OR shlokas.searchTransliteration LIKE :like ESCAPE '\'
            OR shlokas.searchEnglish LIKE :like ESCAPE '\'
            OR shlokas.searchHindi LIKE :like ESCAPE '\'
            OR shlokas.searchMeaning LIKE :like ESCAPE '\'
            OR shlokas.searchThemes LIKE :like ESCAPE '\'
            OR chapters.searchEnglish LIKE :like ESCAPE '\'
            OR chapters.searchSanskrit LIKE :like ESCAPE '\'
        ))
        OR (:filter = 'SANSKRIT' AND (
            shlokas.searchSanskrit LIKE :like ESCAPE '\'
            OR shlokas.searchTransliteration LIKE :like ESCAPE '\'
        ))
        OR (:filter = 'MEANING' AND (
            shlokas.searchEnglish LIKE :like ESCAPE '\'
            OR shlokas.searchHindi LIKE :like ESCAPE '\'
            OR shlokas.searchMeaning LIKE :like ESCAPE '\'
        ))
        OR (:filter = 'THEMES' AND shlokas.searchThemes LIKE :like ESCAPE '\')
        """,
    )
    suspend fun searchIds(exactId: String, filter: String, like: String): List<String>

    @Query("SELECT * FROM shlokas WHERE id IN (:ids)")
    suspend fun findShlokas(ids: List<String>): List<ShlokaEntity>

    @Query("SELECT * FROM chapters")
    suspend fun chapters(): List<ChapterEntity>
}

@Dao
interface JourneyProgressDao {
    @Query("SELECT * FROM journey_progress")
    fun observeAll(): Flow<List<JourneyProgressEntity>>

    @Query("SELECT * FROM journey_progress WHERE journeyId = :journeyId")
    fun observe(journeyId: String): Flow<JourneyProgressEntity?>

    @Query("SELECT * FROM journey_progress WHERE journeyId = :journeyId")
    suspend fun find(journeyId: String): JourneyProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: JourneyProgressEntity)

    @Query("DELETE FROM journey_progress WHERE journeyId = :journeyId")
    suspend fun delete(journeyId: String)
}
