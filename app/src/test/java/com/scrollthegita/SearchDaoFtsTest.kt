package com.scrollthegita

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.scrollthegita.core.database.GitaDatabase
import com.scrollthegita.core.database.SearchDao
import com.scrollthegita.core.database.ShlokaDao
import com.scrollthegita.core.database.ShlokaEntity
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
class SearchDaoFtsTest {

    private lateinit var database: GitaDatabase
    private lateinit var searchDao: SearchDao
    private lateinit var shlokaDao: ShlokaDao

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, GitaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        searchDao = database.searchDao()
        shlokaDao = database.shlokaDao()

        runBlocking {
            shlokaDao.insertAll(
                listOf(
                    createShlokaEntity(
                        id = "2.47",
                        chapter = 2,
                        verse = 47,
                        sanskrit = "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन",
                        transliteration = "karmaṇy-evādhikāras te mā phaleṣu kadācana",
                        translationEnglish = "You have a right to perform your prescribed duty",
                        simpleMeaning = "Focus on action without clinging to the fruit of work.",
                    ),
                    createShlokaEntity(
                        id = "2.48",
                        chapter = 2,
                        verse = 48,
                        sanskrit = "योगस्थः कुरु कर्माणि सङ्गं त्यक्त्वा धनञ्जय",
                        transliteration = "yoga-sthaḥ kuru karmāṇi saṅgaṁ tyaktvā dhanañjaya",
                        translationEnglish = "Perform your duty with an even mind, abandoning attachment",
                        simpleMeaning = "Equanimity of mind is true yoga.",
                    ),
                ),
            )
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun ftsSearchMatchesSanskrit() = runBlocking {
        val results = searchDao.searchFts("कर्मण्येवाधिकारस्ते*")
        assertEquals(1, results.size)
        assertEquals("2.47", results.first().id)
    }

    @Test
    fun ftsSearchMatchesTranslation() = runBlocking {
        val results = searchDao.searchFts("prescribed*")
        assertEquals(1, results.size)
        assertEquals("2.47", results.first().id)
    }

    @Test
    fun ftsSearchMatchesSimpleMeaning() = runBlocking {
        val results = searchDao.searchFts("equanimity*")
        assertEquals(1, results.size)
        assertEquals("2.48", results.first().id)
    }

    @Test
    fun ftsSearchMatchesTransliteration() = runBlocking {
        val results = searchDao.searchFts("dhanañjaya*")
        assertEquals(1, results.size)
        assertEquals("2.48", results.first().id)
    }

    @Test
    fun ftsSearchMultiWordAnd() = runBlocking {
        val results = searchDao.searchFts("duty* even*")
        assertEquals(1, results.size)
        assertEquals("2.48", results.first().id)
    }

    @Test
    fun ftsSearchIdsReturnsMatchingIds() = runBlocking {
        val ids = searchDao.searchFtsIds("duty*")
        assertEquals(2, ids.size)
        assertTrue(ids.contains("2.47"))
        assertTrue(ids.contains("2.48"))
    }

    @Test
    fun ftsSearchNoMatchReturnsEmpty() = runBlocking {
        val results = searchDao.searchFts("nonexistentwordxyz*")
        assertTrue(results.isEmpty())
    }

    private fun createShlokaEntity(
        id: String,
        chapter: Int,
        verse: Int,
        sanskrit: String,
        transliteration: String,
        translationEnglish: String,
        simpleMeaning: String,
    ) = ShlokaEntity(
        id = id,
        chapter = chapter,
        verse = verse,
        sanskrit = sanskrit,
        transliteration = transliteration,
        translationEnglish = translationEnglish,
        translationHindi = null,
        simpleMeaning = simpleMeaning,
        reflectionPrompt = "Reflection for $id",
        themes = "ACTION",
        backgroundStyle = "DEFAULT",
        sourceTitle = "Title",
        sourceAttribution = "Attribution",
        sortOrder = chapter * 1000 + verse,
        contentStatus = "APPROVED",
        editionTitle = "",
        translator = "",
        reviewer = "",
        licenseStatement = "",
        sourceReference = "",
        contentVersion = "1",
        searchSanskrit = sanskrit,
        searchTransliteration = transliteration,
        searchEnglish = translationEnglish,
        searchHindi = "",
        searchMeaning = simpleMeaning,
        searchThemes = "ACTION",
    )
}
