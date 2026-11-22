package com.scrollthegita.core.model

enum class SearchFilter {
    ALL,
    SANSKRIT,
    MEANING,
    THEMES,
}

data class SearchResult(
    val shloka: Shloka,
    val chapterTitle: String,
    val rank: Int,
)

/**
 * How text results are ordered, after an exact chapter.verse id:
 * 0 exact id, 1 chapter title, 2 theme tag, 3 other text.
 * Ties break by [Shloka.sortOrder]. This is not personalized.
 */
object SearchRanking {
    const val EXACT_VERSE = 0
    const val CHAPTER_TITLE = 1
    const val THEME = 2
    const val TEXT = 3

    fun rank(shloka: Shloka, chapter: Chapter?, exactId: String?, normalizedQuery: String): Int {
        if (exactId != null && shloka.id == exactId) return EXACT_VERSE
        if (chapter != null && normalizedQuery.isNotEmpty()) {
            val title = SearchText.normalize("${chapter.nameEnglish} ${chapter.nameSanskrit}")
            if (title.contains(normalizedQuery)) return CHAPTER_TITLE
        }
        val themes = SearchText.normalize(shloka.themes.joinToString(" ") { it.name })
        if (normalizedQuery.isNotEmpty() && themes.contains(normalizedQuery)) return THEME
        return TEXT
    }
}

data class ReadingHistory(
    val shlokaId: String,
    val chapter: Int,
    val verse: Int,
    val firstReadAtEpochMillis: Long,
    val lastReadAtEpochMillis: Long,
    val settledReadCount: Int,
)

data class PersonalReflection(
    val shlokaId: String,
    val body: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

enum class ChapterProgressKind {
    NOT_STARTED,
    IN_PROGRESS,
    AVAILABLE_READ,
    APPROVED_CHAPTER_READ,
}

data class ChapterProgress(
    val available: Int,
    val read: Int,
    val kind: ChapterProgressKind,
)

/**
 * Sample or incomplete chapters are never described as a finished traditional chapter.
 * [ChapterProgressKind.APPROVED_CHAPTER_READ] requires the package to mark the chapter complete,
 * every traditional verse to be present and approved, and each of those verses to have been read.
 */
fun chapterProgress(
    available: List<Shloka>,
    readIds: Set<String>,
    contentComplete: Boolean,
    traditionalTotal: Int,
): ChapterProgress {
    val read = available.count { it.id in readIds }
    val approved = available.count { it.contentStatus == ContentStatus.APPROVED }
    val kind = when {
        available.isEmpty() || read == 0 -> ChapterProgressKind.NOT_STARTED
        contentComplete &&
            traditionalTotal > 0 &&
            available.size >= traditionalTotal &&
            approved >= traditionalTotal &&
            read >= traditionalTotal -> ChapterProgressKind.APPROVED_CHAPTER_READ
        read >= available.size -> ChapterProgressKind.AVAILABLE_READ
        else -> ChapterProgressKind.IN_PROGRESS
    }
    return ChapterProgress(available = available.size, read = read, kind = kind)
}
