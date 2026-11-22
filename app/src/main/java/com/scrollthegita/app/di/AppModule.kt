package com.scrollthegita.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import com.scrollthegita.core.common.AppClock
import com.scrollthegita.core.common.Clock
import com.scrollthegita.core.database.BookmarkDao
import com.scrollthegita.core.database.ChapterDao
import com.scrollthegita.core.database.GitaDatabase
import com.scrollthegita.core.database.GitaMigrations
import com.scrollthegita.core.database.GitaPrivateDatabase
import com.scrollthegita.core.database.ReadingHistoryDao
import com.scrollthegita.core.database.ReflectionDao
import com.scrollthegita.core.database.SearchDao
import com.scrollthegita.core.database.ShlokaDao
import com.scrollthegita.data.repository.GitaContentRepositoryImpl
import com.scrollthegita.data.repository.ReadingHistoryRepositoryImpl
import com.scrollthegita.data.repository.ReflectionRepositoryImpl
import com.scrollthegita.data.repository.SearchRepositoryImpl
import com.scrollthegita.data.repository.UserSettingsRepositoryImpl
import com.scrollthegita.domain.repository.GitaContentRepository
import com.scrollthegita.domain.repository.ReadingHistoryRepository
import com.scrollthegita.domain.repository.ReflectionRepository
import com.scrollthegita.domain.repository.SearchRepository
import com.scrollthegita.domain.repository.UserSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GitaDatabase =
        Room.databaseBuilder(context, GitaDatabase::class.java, "scroll_the_gita.db")
            .addMigrations(GitaMigrations.MIGRATION_1_2, GitaMigrations.MIGRATION_2_3)
            .build()

    @Provides
    @Singleton
    fun providePrivateDatabase(@ApplicationContext context: Context): GitaPrivateDatabase =
        Room.databaseBuilder(context, GitaPrivateDatabase::class.java, "gita_private.db")
            .build()

    @Provides
    fun provideShlokaDao(database: GitaDatabase): ShlokaDao = database.shlokaDao()

    @Provides
    fun provideChapterDao(database: GitaDatabase): ChapterDao = database.chapterDao()

    @Provides
    fun provideBookmarkDao(database: GitaDatabase): BookmarkDao = database.bookmarkDao()

    @Provides
    fun provideReadingHistoryDao(database: GitaDatabase): ReadingHistoryDao = database.readingHistoryDao()

    @Provides
    fun provideSearchDao(database: GitaDatabase): SearchDao = database.searchDao()

    @Provides
    fun provideReflectionDao(database: GitaPrivateDatabase): ReflectionDao = database.reflectionDao()
}

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun provideUserSettings(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("user_settings") },
        )
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindContent(impl: GitaContentRepositoryImpl): GitaContentRepository

    @Binds
    @Singleton
    abstract fun bindSettings(impl: UserSettingsRepositoryImpl): UserSettingsRepository

    @Binds
    @Singleton
    abstract fun bindSearch(impl: SearchRepositoryImpl): SearchRepository

    @Binds
    @Singleton
    abstract fun bindHistory(impl: ReadingHistoryRepositoryImpl): ReadingHistoryRepository

    @Binds
    @Singleton
    abstract fun bindReflections(impl: ReflectionRepositoryImpl): ReflectionRepository

    @Binds
    abstract fun bindClock(impl: AppClock): Clock
}
