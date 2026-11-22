package com.scrollthegita.core.model

/**
 * Stable shloka identity. Bookmarks and reading progress store this id.
 *
 * Format: `{chapter}.{verse}` with no leading zeros. Chapter 2 verse 47 is `2.47`.
 * [sortOrder] is `chapter * 1000 + verse`, so chapter order and verse order agree.
 * An approved import must reuse the same id for the same chapter and verse.
 * Changing an id orphans any bookmark or progress that still points at the old one.
 */
object ShlokaIds {
    fun of(chapter: Int, verse: Int): String = "$chapter.$verse"

    fun sortOrder(chapter: Int, verse: Int): Int = chapter * 1000 + verse
}
