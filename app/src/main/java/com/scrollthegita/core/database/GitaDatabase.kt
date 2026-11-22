package com.scrollthegita.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ShlokaEntity::class,
        ChapterEntity::class,
        BookmarkEntity::class,
        ReadingHistoryEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class GitaDatabase : RoomDatabase() {
    abstract fun shlokaDao(): ShlokaDao
    abstract fun chapterDao(): ChapterDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun readingHistoryDao(): ReadingHistoryDao
    abstract fun searchDao(): SearchDao
}
