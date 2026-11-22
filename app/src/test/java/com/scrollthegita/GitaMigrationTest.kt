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
            .addMigrations(GitaMigrations.MIGRATION_1_2, GitaMigrations.MIGRATION_2_3)
            .allowMainThreadQueries()
            .build()
        val shloka = version2.shlokaDao().observeAll().first().single()
        val bookmark = version2.bookmarkDao().observeAll().first().single()
        assertEquals("2.47", bookmark.shlokaId)
        assertEquals(5L, bookmark.createdAtEpochMillis)
        assertEquals(ContentStatus.SAMPLE.name, shloka.contentStatus)
        assertEquals("", shloka.editionTitle)
        version2.close()
    }
}
