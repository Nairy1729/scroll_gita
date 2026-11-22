package com.scrollthegita

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.scrollthegita.core.database.GitaDatabase
import com.scrollthegita.core.model.ReadingProgress
import com.scrollthegita.data.repository.GitaContentRepositoryImpl
import com.scrollthegita.data.repository.UserSettingsRepositoryImpl
import com.scrollthegita.data.seed.SampleGitaContent
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class ContentPreservationTest {
    private lateinit var database: GitaDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, GitaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun bookmarkAndReadingProgressSurviveAContentUpdate() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val content = GitaContentRepositoryImpl(database, context)
        content.ensureSeeded()
        assertTrue(content.setBookmarked("2.47", true))
        val settingsFile = File.createTempFile("gita-progress", ".preferences_pb")
        settingsFile.delete()
        val settings = UserSettingsRepositoryImpl(
            PreferenceDataStoreFactory.create(produceFile = { settingsFile }),
        )
        settings.saveProgress(ReadingProgress("2.47", 2, 47, 10L))

        val updated = SampleGitaContent.shlokas.first().copy(
            simpleMeaning = "Updated fixture explanation. Not a translation.",
        )
        content.importContent(SampleGitaContent.chapters, listOf(updated))

        assertEquals(listOf("2.47"), content.observeBookmarks().first().map { it.shlokaId })
        assertEquals("2.47", settings.observeProgress().first()?.lastShlokaId)
        assertEquals(updated.simpleMeaning, content.observeShlokas().first().first().simpleMeaning)
    }
}
