package com.scrollthegita

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.scrollthegita.core.database.GitaDatabase
import com.scrollthegita.data.repository.GitaContentRepositoryImpl
import com.scrollthegita.data.seed.SampleGitaContent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class GitaContentRepositoryTest {
    private lateinit var database: GitaDatabase
    private lateinit var repository: GitaContentRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, GitaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = GitaContentRepositoryImpl(
            database,
            ApplicationProvider.getApplicationContext(),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun seedLoadsInSortOrder() = runBlocking {
        repository.ensureSeeded()
        val loaded = repository.observeShlokas().first()
        assertEquals(SampleGitaContent.shlokas.map { it.id }, loaded.map { it.id })
        assertEquals(SampleGitaContent.shlokas.map { it.sortOrder }, loaded.map { it.sortOrder })
    }

    @Test
    fun bookmarkCanBeAddedAndRemoved() = runBlocking {
        repository.ensureSeeded()
        assertTrue(repository.setBookmarked("2.47", true))
        assertEquals(listOf("2.47"), repository.observeBookmarks().first().map { it.shlokaId })
        assertFalse(repository.setBookmarked("2.47", false))
        assertTrue(repository.observeBookmarks().first().isEmpty())
    }
}
