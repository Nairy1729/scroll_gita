package com.scrollthegita.core.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Reflections live in their own file so backup rules can exclude them
 * without excluding bookmarks or the reading.
 */
@Entity(tableName = "reflections")
data class ReflectionEntity(
    @PrimaryKey val shlokaId: String,
    val body: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

@Dao
interface ReflectionDao {
    @Query("SELECT * FROM reflections ORDER BY updatedAtEpochMillis DESC")
    fun observeAll(): Flow<List<ReflectionEntity>>

    @Query("SELECT * FROM reflections WHERE shlokaId = :id")
    fun observe(id: String): Flow<ReflectionEntity?>

    @Query("SELECT * FROM reflections WHERE shlokaId = :id")
    suspend fun find(id: String): ReflectionEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: ReflectionEntity)

    @Update
    suspend fun update(entity: ReflectionEntity)

    @Query("DELETE FROM reflections WHERE shlokaId = :id")
    suspend fun delete(id: String)
}

@Database(
    entities = [ReflectionEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class GitaPrivateDatabase : RoomDatabase() {
    abstract fun reflectionDao(): ReflectionDao
}
