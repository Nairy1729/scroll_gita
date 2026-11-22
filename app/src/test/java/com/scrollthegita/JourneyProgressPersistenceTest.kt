package com.scrollthegita

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.scrollthegita.core.database.GitaDatabase
import com.scrollthegita.core.database.JourneyProgressDao
import com.scrollthegita.core.database.JourneyProgressEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class JourneyProgressPersistenceTest {

    private lateinit var database: GitaDatabase
    private lateinit var journeyProgressDao: JourneyProgressDao

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, GitaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        journeyProgressDao = database.journeyProgressDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun saveAndRetrieveProgress() = runBlocking {
        assertNull(journeyProgressDao.find("duty"))

        val entity = JourneyProgressEntity(
            journeyId = "duty",
            currentIndex = 2,
            lastReadAtEpochMillis = 1700000000000L,
        )
        journeyProgressDao.save(entity)

        val retrieved = journeyProgressDao.find("duty")
        assertNotNull(retrieved)
        assertEquals("duty", retrieved?.journeyId)
        assertEquals(2, retrieved?.currentIndex)
        assertEquals(1700000000000L, retrieved?.lastReadAtEpochMillis)
    }

    @Test
    fun updateProgressOverwritesPreviousIndex() = runBlocking {
        journeyProgressDao.save(
            JourneyProgressEntity(
                journeyId = "duty",
                currentIndex = 0,
                lastReadAtEpochMillis = 1000L,
            ),
        )

        journeyProgressDao.save(
            JourneyProgressEntity(
                journeyId = "duty",
                currentIndex = 4,
                lastReadAtEpochMillis = 2000L,
            ),
        )

        val retrieved = journeyProgressDao.find("duty")
        assertEquals(4, retrieved?.currentIndex)
        assertEquals(2000L, retrieved?.lastReadAtEpochMillis)
    }

    @Test
    fun observeEmitsUpdates() = runBlocking {
        val initial = journeyProgressDao.observe("equanimity").first()
        assertNull(initial)

        journeyProgressDao.save(
            JourneyProgressEntity(
                journeyId = "equanimity",
                currentIndex = 1,
                lastReadAtEpochMillis = 5000L,
            ),
        )

        val updated = journeyProgressDao.observe("equanimity").first()
        assertNotNull(updated)
        assertEquals(1, updated?.currentIndex)
    }

    @Test
    fun observeAllReturnsAllPersistedProgress() = runBlocking {
        journeyProgressDao.save(
            JourneyProgressEntity(journeyId = "duty", currentIndex = 1, lastReadAtEpochMillis = 100L),
        )
        journeyProgressDao.save(
            JourneyProgressEntity(journeyId = "equanimity", currentIndex = 3, lastReadAtEpochMillis = 200L),
        )

        val all = journeyProgressDao.observeAll().first()
        assertEquals(2, all.size)
        val map = all.associateBy { it.journeyId }
        assertEquals(1, map["duty"]?.currentIndex)
        assertEquals(3, map["equanimity"]?.currentIndex)
    }
}
