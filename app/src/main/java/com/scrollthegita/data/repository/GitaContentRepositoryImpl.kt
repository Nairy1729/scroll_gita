package com.scrollthegita.data.repository

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.scrollthegita.core.database.BookmarkDao
import com.scrollthegita.core.database.BookmarkEntity
import com.scrollthegita.core.database.ChapterDao
import com.scrollthegita.core.database.GitaDatabase
import com.scrollthegita.core.database.ShlokaDao
import com.scrollthegita.core.database.toDomain
import com.scrollthegita.core.database.toEntity
import com.scrollthegita.core.model.Bookmark
import com.scrollthegita.core.model.Chapter
import com.scrollthegita.core.model.Shloka
import com.scrollthegita.data.content.ContentImportResult
import com.scrollthegita.data.content.GitaContentJson
import com.scrollthegita.data.seed.SampleGitaContent
import com.scrollthegita.domain.repository.GitaContentRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class GitaContentRepositoryImpl @Inject constructor(
    private val database: GitaDatabase,
    @ApplicationContext private val context: Context,
) : GitaContentRepository {
    private val shlokaDao: ShlokaDao = database.shlokaDao()
    private val chapterDao: ChapterDao = database.chapterDao()
    private val bookmarkDao: BookmarkDao = database.bookmarkDao()
    private val seedMutex = Mutex()

    override suspend fun ensureSeeded() {
        seedMutex.withLock {
            // Insert only when empty. REPLACE would delete shloka rows and cascade bookmarks.
            if (shlokaDao.count() == 0) {
                shlokaDao.insertAll(SampleGitaContent.shlokas.map { it.toEntity() })
            }
            if (chapterDao.count() == 0) {
                chapterDao.insertAll(SampleGitaContent.chapters.map { it.toEntity() })
            }
            importBundledFile()
            refreshSearchFields()
        }
    }

    override suspend fun importContent(chapters: List<Chapter>, shlokas: List<Shloka>) {
        database.withTransaction {
            for (chapter in chapters) {
                val entity = chapter.toEntity()
                if (chapterDao.exists(chapter.number)) chapterDao.update(entity) else chapterDao.insert(entity)
            }
            for (shloka in shlokas) {
                val entity = shloka.toEntity()
                if (shlokaDao.exists(shloka.id)) shlokaDao.update(entity) else shlokaDao.insert(entity)
            }
        }
    }

    override fun observeShlokas(): Flow<List<Shloka>> =
        shlokaDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeChapters(): Flow<List<Chapter>> =
        chapterDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeBookmarks(): Flow<List<Bookmark>> =
        bookmarkDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun setBookmarked(shlokaId: String, bookmarked: Boolean): Boolean {
        if (bookmarked) {
            bookmarkDao.insert(
                BookmarkEntity(
                    shlokaId = shlokaId,
                    createdAtEpochMillis = System.currentTimeMillis(),
                ),
            )
        } else {
            bookmarkDao.delete(shlokaId)
        }
        return bookmarked
    }

    private suspend fun refreshSearchFields() {
        database.withTransaction {
            for (row in shlokaDao.listAll()) {
                val refreshed = row.toDomain().toEntity()
                if (refreshed.searchTransliteration != row.searchTransliteration ||
                    refreshed.searchSanskrit != row.searchSanskrit ||
                    refreshed.searchEnglish != row.searchEnglish
                ) {
                    shlokaDao.update(refreshed)
                }
            }
            for (row in chapterDao.listAll()) {
                val refreshed = row.toDomain().toEntity()
                if (refreshed.searchEnglish != row.searchEnglish || refreshed.searchSanskrit != row.searchSanskrit) {
                    chapterDao.update(refreshed)
                }
            }
        }
    }

    private suspend fun importBundledFile() {
        val json = runCatching {
            context.assets.open(GitaContentJson.ASSET_PATH).bufferedReader().use { it.readText() }
        }.getOrNull() ?: return
        when (val parsed = GitaContentJson.parse(json)) {
            is ContentImportResult.Valid -> importContent(parsed.chapters, parsed.shlokas)
            is ContentImportResult.Invalid -> Log.w(TAG, parsed.errors.joinToString(separator = "\n"))
        }
    }

    private companion object {
        const val TAG = "GitaContent"
    }
}
