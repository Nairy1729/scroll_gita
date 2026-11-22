package com.scrollthegita

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.scrollthegita.core.database.GitaDatabase
import com.scrollthegita.core.database.GitaMigrations
import com.scrollthegita.core.model.ContentStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class GitaMigrationTest {
    private val name = "gita-migration-test"
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(name)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(name)
    }

    @Test
    fun migrationKeepsBookmarkAndMapsSampleStatus() = runBlocking {
        context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { db ->
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `shlokas` (
                  `id` TEXT NOT NULL,
                  `chapter` INTEGER NOT NULL,
                  `verse` INTEGER NOT NULL,
                  `sanskrit` TEXT NOT NULL,
                  `transliteration` TEXT NOT NULL,
                  `translationEnglish` TEXT NOT NULL,
                  `translationHindi` TEXT,
                  `simpleMeaning` TEXT NOT NULL,
                  `reflectionPrompt` TEXT NOT NULL,
                  `themes` TEXT NOT NULL,
                  `backgroundStyle` TEXT NOT NULL,
                  `sourceTitle` TEXT NOT NULL,
                  `sourceAttribution` TEXT NOT NULL,
                  `sortOrder` INTEGER NOT NULL,
                  `contentStatus` TEXT NOT NULL,
                  PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `chapters` (
                  `number` INTEGER NOT NULL,
                  `nameSanskrit` TEXT NOT NULL,
                  `nameEnglish` TEXT NOT NULL,
                  `summary` TEXT NOT NULL,
                  `totalShlokas` INTEGER NOT NULL,
                  PRIMARY KEY(`number`)
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `bookmarks` (
                  `shlokaId` TEXT NOT NULL,
                  `createdAtEpochMillis` INTEGER NOT NULL,
                  PRIMARY KEY(`shlokaId`),
                  FOREIGN KEY(`shlokaId`) REFERENCES `shlokas`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_bookmarks_shlokaId` ON `bookmarks` (`shlokaId`)")
            db.execSQL(
                """
                INSERT INTO shlokas (
                  id, chapter, verse, sanskrit, transliteration, translationEnglish, translationHindi,
                  simpleMeaning, reflectionPrompt, themes, backgroundStyle, sourceTitle, sourceAttribution,
                  sortOrder, contentStatus
                ) VALUES (
                  '2.47', 2, 47, '[Verified Sanskrit text required — 2.47]',
                  '[Verified transliteration required]', '[Sample — not a licensed translation]', NULL,
                  'Placeholder for a scholar-reviewed explanation.', 'A question written for the app.',
                  'ACTION', 'KARMA_SUNRISE', 'Scroll the Gita sample seed', 'Prototype placeholder.',
                  2047, 'SAMPLE_PLACEHOLDER'
                )
                """.trimIndent(),
            )
            db.execSQL("INSERT INTO bookmarks (shlokaId, createdAtEpochMillis) VALUES ('2.47', 5)")
            db.version = 1
        }

        val version2 = Room.databaseBuilder(context, GitaDatabase::class.java, name)
            .addMigrations(
                GitaMigrations.MIGRATION_1_2,
                GitaMigrations.MIGRATION_2_3,
                GitaMigrations.MIGRATION_3_4,
                GitaMigrations.MIGRATION_4_5,
            )
            .allowMainThreadQueries()
            .build()
        val shloka = version2.shlokaDao().observeAll().first().single()
        val bookmark = version2.bookmarkDao().observeAll().first().single()
        assertEquals("2.47", bookmark.shlokaId)
        assertEquals(5L, bookmark.createdAtEpochMillis)
        assertEquals(ContentStatus.SAMPLE.name, shloka.contentStatus)
        assertEquals("", shloka.editionTitle)
        val ftsResults = version2.searchDao().searchFts("Verified*")
        assertEquals(1, ftsResults.size)
        assertEquals("2.47", ftsResults.first().id)
        version2.close()
    }

    @Test
    fun migration3To4BuildsFtsAndPreservesBookmarksAndReadingPosition() = runBlocking {
        context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { db ->
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `shlokas` (
                  `id` TEXT NOT NULL,
                  `chapter` INTEGER NOT NULL,
                  `verse` INTEGER NOT NULL,
                  `sanskrit` TEXT NOT NULL,
                  `transliteration` TEXT NOT NULL,
                  `translationEnglish` TEXT NOT NULL,
                  `translationHindi` TEXT,
                  `simpleMeaning` TEXT NOT NULL,
                  `reflectionPrompt` TEXT NOT NULL,
                  `themes` TEXT NOT NULL,
                  `backgroundStyle` TEXT NOT NULL,
                  `sourceTitle` TEXT NOT NULL,
                  `sourceAttribution` TEXT NOT NULL,
                  `sortOrder` INTEGER NOT NULL,
                  `contentStatus` TEXT NOT NULL,
                  `editionTitle` TEXT NOT NULL DEFAULT '',
                  `translator` TEXT NOT NULL DEFAULT '',
                  `reviewer` TEXT NOT NULL DEFAULT '',
                  `licenseStatement` TEXT NOT NULL DEFAULT '',
                  `sourceReference` TEXT NOT NULL DEFAULT '',
                  `contentVersion` TEXT NOT NULL DEFAULT '',
                  `searchSanskrit` TEXT NOT NULL DEFAULT '',
                  `searchTransliteration` TEXT NOT NULL DEFAULT '',
                  `searchEnglish` TEXT NOT NULL DEFAULT '',
                  `searchHindi` TEXT NOT NULL DEFAULT '',
                  `searchMeaning` TEXT NOT NULL DEFAULT '',
                  `searchThemes` TEXT NOT NULL DEFAULT '',
                  PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `chapters` (
                  `number` INTEGER NOT NULL,
                  `nameSanskrit` TEXT NOT NULL,
                  `nameEnglish` TEXT NOT NULL,
                  `summary` TEXT NOT NULL,
                  `totalShlokas` INTEGER NOT NULL,
                  `isContentComplete` INTEGER NOT NULL DEFAULT 0,
                  `searchSanskrit` TEXT NOT NULL DEFAULT '',
                  `searchEnglish` TEXT NOT NULL DEFAULT '',
                  PRIMARY KEY(`number`)
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `bookmarks` (
                  `shlokaId` TEXT NOT NULL,
                  `createdAtEpochMillis` INTEGER NOT NULL,
                  PRIMARY KEY(`shlokaId`),
                  FOREIGN KEY(`shlokaId`) REFERENCES `shlokas`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_bookmarks_shlokaId` ON `bookmarks` (`shlokaId`)")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `reading_history` (
                  `shlokaId` TEXT NOT NULL,
                  `chapter` INTEGER NOT NULL,
                  `verse` INTEGER NOT NULL,
                  `firstReadAtEpochMillis` INTEGER NOT NULL,
                  `lastReadAtEpochMillis` INTEGER NOT NULL,
                  `settledReadCount` INTEGER NOT NULL,
                  PRIMARY KEY(`shlokaId`)
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                INSERT INTO shlokas (
                  id, chapter, verse, sanskrit, transliteration, translationEnglish, translationHindi,
                  simpleMeaning, reflectionPrompt, themes, backgroundStyle, sourceTitle, sourceAttribution,
                  sortOrder, contentStatus, searchTransliteration
                ) VALUES (
                  '2.47', 2, 47, 'कर्मण्येवाधिकारस्ते मा फलेषु कदाचन',
                  'karmaṇy-evādhikāras te mā phaleṣu kadācana', 'You have a right to perform your prescribed duty', NULL,
                  'Focus on action without clinging to the fruit of work.', 'What is the action in front of you today?',
                  'ACTION', 'KARMA_SUNRISE', 'Scroll the Gita sample seed', 'Sample source',
                  2047, 'SAMPLE', 'karmany-evadhikaras te ma phalesu kadacana'
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_shlokas_searchTransliteration` ON `shlokas` (`searchTransliteration`)")
            db.execSQL("INSERT INTO bookmarks (shlokaId, createdAtEpochMillis) VALUES ('2.47', 12345)")
            db.execSQL(
                """
                INSERT INTO reading_history (
                  shlokaId, chapter, verse, firstReadAtEpochMillis, lastReadAtEpochMillis, settledReadCount
                ) VALUES ('2.47', 2, 47, 1000, 2000, 3)
                """.trimIndent(),
            )
            db.version = 3
        }

        val migratedDb = Room.databaseBuilder(context, GitaDatabase::class.java, name)
            .addMigrations(GitaMigrations.MIGRATION_3_4, GitaMigrations.MIGRATION_4_5)
            .allowMainThreadQueries()
            .build()

        val bookmark = migratedDb.bookmarkDao().observeAll().first().single()
        assertEquals("2.47", bookmark.shlokaId)
        assertEquals(12345L, bookmark.createdAtEpochMillis)

        val history = migratedDb.readingHistoryDao().observeAll().first().single()
        assertEquals("2.47", history.shlokaId)
        assertEquals(2, history.chapter)
        assertEquals(47, history.verse)
        assertEquals(3, history.settledReadCount)

        // Verify FTS table was populated via 'rebuild' in MIGRATION_3_4
        val sanskritResults = migratedDb.searchDao().searchFts("कर्मण्येवाधिकारस्ते*")
        assertEquals(1, sanskritResults.size)
        assertEquals("2.47", sanskritResults.first().id)

        val translationResults = migratedDb.searchDao().searchFts("duty*")
        assertEquals(1, translationResults.size)
        assertEquals("2.47", translationResults.first().id)

        val meaningResults = migratedDb.searchDao().searchFts("action*")
        assertEquals(1, meaningResults.size)
        assertEquals("2.47", meaningResults.first().id)

        migratedDb.close()
    }

    @Test
    fun migration4To5CreatesJourneyProgressAndPreservesData() = runBlocking {
        context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { db ->
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `shlokas` (
                  `id` TEXT NOT NULL,
                  `chapter` INTEGER NOT NULL,
                  `verse` INTEGER NOT NULL,
                  `sanskrit` TEXT NOT NULL,
                  `transliteration` TEXT NOT NULL,
                  `translationEnglish` TEXT NOT NULL,
                  `translationHindi` TEXT,
                  `simpleMeaning` TEXT NOT NULL,
                  `reflectionPrompt` TEXT NOT NULL,
                  `themes` TEXT NOT NULL,
                  `backgroundStyle` TEXT NOT NULL,
                  `sourceTitle` TEXT NOT NULL,
                  `sourceAttribution` TEXT NOT NULL,
                  `sortOrder` INTEGER NOT NULL,
                  `contentStatus` TEXT NOT NULL,
                  `editionTitle` TEXT NOT NULL DEFAULT '',
                  `translator` TEXT NOT NULL DEFAULT '',
                  `reviewer` TEXT NOT NULL DEFAULT '',
                  `licenseStatement` TEXT NOT NULL DEFAULT '',
                  `sourceReference` TEXT NOT NULL DEFAULT '',
                  `contentVersion` TEXT NOT NULL DEFAULT '',
                  `searchSanskrit` TEXT NOT NULL DEFAULT '',
                  `searchTransliteration` TEXT NOT NULL DEFAULT '',
                  `searchEnglish` TEXT NOT NULL DEFAULT '',
                  `searchHindi` TEXT NOT NULL DEFAULT '',
                  `searchMeaning` TEXT NOT NULL DEFAULT '',
                  `searchThemes` TEXT NOT NULL DEFAULT '',
                  PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `chapters` (
                  `number` INTEGER NOT NULL,
                  `nameSanskrit` TEXT NOT NULL,
                  `nameEnglish` TEXT NOT NULL,
                  `summary` TEXT NOT NULL,
                  `totalShlokas` INTEGER NOT NULL,
                  `isContentComplete` INTEGER NOT NULL DEFAULT 0,
                  `searchSanskrit` TEXT NOT NULL DEFAULT '',
                  `searchEnglish` TEXT NOT NULL DEFAULT '',
                  PRIMARY KEY(`number`)
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `bookmarks` (
                  `shlokaId` TEXT NOT NULL,
                  `createdAtEpochMillis` INTEGER NOT NULL,
                  PRIMARY KEY(`shlokaId`),
                  FOREIGN KEY(`shlokaId`) REFERENCES `shlokas`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_bookmarks_shlokaId` ON `bookmarks` (`shlokaId`)")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `reading_history` (
                  `shlokaId` TEXT NOT NULL,
                  `chapter` INTEGER NOT NULL,
                  `verse` INTEGER NOT NULL,
                  `firstReadAtEpochMillis` INTEGER NOT NULL,
                  `lastReadAtEpochMillis` INTEGER NOT NULL,
                  `settledReadCount` INTEGER NOT NULL,
                  PRIMARY KEY(`shlokaId`)
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE VIRTUAL TABLE IF NOT EXISTS `shlokas_fts` USING FTS4(
                  `sanskrit` TEXT NOT NULL,
                  `transliteration` TEXT NOT NULL,
                  `translationEnglish` TEXT NOT NULL,
                  `simpleMeaning` TEXT NOT NULL,
                  content=`shlokas`
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                INSERT INTO shlokas (
                  id, chapter, verse, sanskrit, transliteration, translationEnglish, translationHindi,
                  simpleMeaning, reflectionPrompt, themes, backgroundStyle, sourceTitle, sourceAttribution,
                  sortOrder, contentStatus, searchTransliteration
                ) VALUES (
                  '2.47', 2, 47, 'कर्मण्येवाधिकारस्ते मा फलेषु कदाचन',
                  'karmaṇy-evādhikāras te mā phaleṣu kadācana', 'You have a right to perform your prescribed duty', NULL,
                  'Focus on action without clinging to the fruit of work.', 'What is the action in front of you today?',
                  'ACTION', 'KARMA_SUNRISE', 'Scroll the Gita sample seed', 'Sample source',
                  2047, 'SAMPLE', 'karmany-evadhikaras te ma phalesu kadacana'
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_shlokas_searchTransliteration` ON `shlokas` (`searchTransliteration`)")
            db.execSQL("INSERT INTO bookmarks (shlokaId, createdAtEpochMillis) VALUES ('2.47', 999)")
            db.execSQL(
                """
                INSERT INTO reading_history (
                  shlokaId, chapter, verse, firstReadAtEpochMillis, lastReadAtEpochMillis, settledReadCount
                ) VALUES ('2.47', 2, 47, 500, 800, 1)
                """.trimIndent(),
            )
            db.version = 4
        }

        val migratedDb = Room.databaseBuilder(context, GitaDatabase::class.java, name)
            .addMigrations(GitaMigrations.MIGRATION_4_5)
            .allowMainThreadQueries()
            .build()

        // Verify existing tables survive
        val bookmark = migratedDb.bookmarkDao().observeAll().first().single()
        assertEquals("2.47", bookmark.shlokaId)
        assertEquals(999L, bookmark.createdAtEpochMillis)

        val history = migratedDb.readingHistoryDao().observeAll().first().single()
        assertEquals("2.47", history.shlokaId)

        // Verify journey_progress table exists and works
        migratedDb.journeyProgressDao().save(
            com.scrollthegita.core.database.JourneyProgressEntity(
                journeyId = "gita_on_duty",
                currentIndex = 3,
                lastReadAtEpochMillis = 8888L,
            ),
        )

        val progress = migratedDb.journeyProgressDao().find("gita_on_duty")
        assertEquals("gita_on_duty", progress?.journeyId)
        assertEquals(3, progress?.currentIndex)
        assertEquals(8888L, progress?.lastReadAtEpochMillis)

        migratedDb.close()
    }
}
