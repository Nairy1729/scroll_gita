package com.scrollthegita.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Fts4
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "shlokas_fts")
@Fts4(contentEntity = ShlokaEntity::class)
data class ShlokaFts(
    val sanskrit: String,
    val transliteration: String,
    @ColumnInfo(name = "translationEnglish") val translation: String,
    val simpleMeaning: String,
)

@Entity(
    tableName = "shlokas",
    indices = [Index(value = ["searchTransliteration"])],
)
data class ShlokaEntity(
    @PrimaryKey val id: String,
    val chapter: Int,
    val verse: Int,
    val sanskrit: String,
    val transliteration: String,
    val translationEnglish: String,
    val translationHindi: String?,
    val simpleMeaning: String,
    val reflectionPrompt: String,
    val themes: String,
    val backgroundStyle: String,
    val sourceTitle: String,
    val sourceAttribution: String,
    val sortOrder: Int,
    val contentStatus: String,
    @ColumnInfo(defaultValue = "''") val editionTitle: String,
    @ColumnInfo(defaultValue = "''") val translator: String,
    @ColumnInfo(defaultValue = "''") val reviewer: String,
    @ColumnInfo(defaultValue = "''") val licenseStatement: String,
    @ColumnInfo(defaultValue = "''") val sourceReference: String,
    @ColumnInfo(defaultValue = "''") val contentVersion: String,
    @ColumnInfo(defaultValue = "''") val searchSanskrit: String,
    @ColumnInfo(defaultValue = "''") val searchTransliteration: String,
    @ColumnInfo(defaultValue = "''") val searchEnglish: String,
    @ColumnInfo(defaultValue = "''") val searchHindi: String,
    @ColumnInfo(defaultValue = "''") val searchMeaning: String,
    @ColumnInfo(defaultValue = "''") val searchThemes: String,
)

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey val number: Int,
    val nameSanskrit: String,
    val nameEnglish: String,
    val summary: String,
    val totalShlokas: Int,
    @ColumnInfo(defaultValue = "0") val isContentComplete: Boolean,
    @ColumnInfo(defaultValue = "''") val searchSanskrit: String,
    @ColumnInfo(defaultValue = "''") val searchEnglish: String,
)

@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = ShlokaEntity::class,
            parentColumns = ["id"],
            childColumns = ["shlokaId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["shlokaId"])],
)
data class BookmarkEntity(
    @PrimaryKey val shlokaId: String,
    val createdAtEpochMillis: Long,
)

/**
 * No foreign key. A content refresh must not erase private reading history when ids stay stable,
 * and a removed sample row must not cascade-delete the fact that it was read.
 */
@Entity(tableName = "reading_history")
data class ReadingHistoryEntity(
    @PrimaryKey val shlokaId: String,
    val chapter: Int,
    val verse: Int,
    val firstReadAtEpochMillis: Long,
    val lastReadAtEpochMillis: Long,
    val settledReadCount: Int,
)

@Entity(tableName = "journey_progress")
data class JourneyProgressEntity(
    @PrimaryKey val journeyId: String,
    val currentIndex: Int,
    val lastReadAtEpochMillis: Long,
)
