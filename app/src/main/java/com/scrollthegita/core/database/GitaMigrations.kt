package com.scrollthegita.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Version 1 is the Milestone 1 schema: shlokas, chapters, and bookmarks.
 * Version 2 adds edition metadata columns and renames the old sample status.
 * Rows are altered in place, so bookmark foreign keys stay attached to the same shloka id.
 */
object GitaMigrations {
    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE shlokas ADD COLUMN editionTitle TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE shlokas ADD COLUMN translator TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE shlokas ADD COLUMN reviewer TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE shlokas ADD COLUMN licenseStatement TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE shlokas ADD COLUMN sourceReference TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE shlokas ADD COLUMN contentVersion TEXT NOT NULL DEFAULT ''")
            db.execSQL(
                "UPDATE shlokas SET contentStatus = 'SAMPLE' WHERE contentStatus = 'SAMPLE_PLACEHOLDER'",
            )
        }
    }

    /**
     * Version 3 adds normalized search columns, chapter completeness, and private reading history.
     * History has no foreign key, so a later content update cannot cascade it away.
     * Search columns start empty and are filled in Kotlin so Devanagari is not damaged by SQL folding.
     */
    val MIGRATION_2_3: Migration = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE shlokas ADD COLUMN searchSanskrit TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE shlokas ADD COLUMN searchTransliteration TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE shlokas ADD COLUMN searchEnglish TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE shlokas ADD COLUMN searchHindi TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE shlokas ADD COLUMN searchMeaning TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE shlokas ADD COLUMN searchThemes TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE chapters ADD COLUMN isContentComplete INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE chapters ADD COLUMN searchSanskrit TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE chapters ADD COLUMN searchEnglish TEXT NOT NULL DEFAULT ''")
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
                "CREATE INDEX IF NOT EXISTS `index_shlokas_searchTransliteration` ON `shlokas` (`searchTransliteration`)",
            )
        }
    }

    /**
     * Version 4 adds the FTS4 virtual table for full-text search across Sanskrit, transliteration,
     * English translation, and simple meaning, backed by the shlokas table via rowid.
     */
    val MIGRATION_3_4: Migration = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
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
                CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_shlokas_fts_BEFORE_UPDATE BEFORE UPDATE ON `shlokas`
                BEGIN DELETE FROM `shlokas_fts` WHERE `docid`=OLD.`rowid`; END
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_shlokas_fts_BEFORE_DELETE BEFORE DELETE ON `shlokas`
                BEGIN DELETE FROM `shlokas_fts` WHERE `docid`=OLD.`rowid`; END
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_shlokas_fts_AFTER_UPDATE AFTER UPDATE ON `shlokas`
                BEGIN INSERT INTO `shlokas_fts`(`docid`, `sanskrit`, `transliteration`, `translationEnglish`, `simpleMeaning`)
                VALUES (NEW.`rowid`, NEW.`sanskrit`, NEW.`transliteration`, NEW.`translationEnglish`, NEW.`simpleMeaning`); END
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_shlokas_fts_AFTER_INSERT AFTER INSERT ON `shlokas`
                BEGIN INSERT INTO `shlokas_fts`(`docid`, `sanskrit`, `transliteration`, `translationEnglish`, `simpleMeaning`)
                VALUES (NEW.`rowid`, NEW.`sanskrit`, NEW.`transliteration`, NEW.`translationEnglish`, NEW.`simpleMeaning`); END
                """.trimIndent(),
            )
            db.execSQL("INSERT INTO `shlokas_fts`(`shlokas_fts`) VALUES('rebuild')")
        }
    }
}
