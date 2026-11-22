package com.scrollthegita.core.model

import java.text.Normalizer
import java.util.Locale

/**
 * Search text is stored beside the source columns. Source Sanskrit and transliteration are not rewritten.
 * Latin diacritics fold so “karma” can match “karmā”. Devanagari marks are kept, because stripping them
 * would change the sacred text’s search form.
 */
object SearchText {
    fun normalize(value: String): String {
        val collapsed = value.trim().replace(Regex("\\s+"), " ")
        if (collapsed.any { it in '\u0900'..'\u097F' }) {
            return collapsed.lowercase(Locale.ROOT)
        }
        val stripped = Normalizer.normalize(collapsed, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        return stripped.lowercase(Locale.ROOT)
    }

    /** Bound to a LIKE parameter. `%`, `_`, and `\` in the query are literals, not wildcards. */
    fun likePattern(query: String): String {
        val escaped = normalize(query)
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")
        return "%$escaped%"
    }
}

object VerseReference {
    private val numberPair = listOf(
        Regex("""(?i)^(?:bg\s+)?(\d{1,2})\s*[.:]\s*(\d{1,3})$"""),
        Regex("""(?i)^chapter\s+(\d{1,2})\s+verse\s+(\d{1,3})$"""),
        Regex("""^अध्याय\s+(\d{1,2})\s+श्लोक\s+(\d{1,3})$"""),
    )

    fun parse(raw: String): String? {
        val text = raw.trim().replace(Regex("\\s+"), " ")
        if (text.isEmpty()) return null
        for (pattern in numberPair) {
            val match = pattern.matchEntire(text) ?: continue
            val chapter = match.groupValues[1].toIntOrNull() ?: return null
            val verse = match.groupValues[2].toIntOrNull() ?: return null
            if (chapter !in 1..18 || verse !in 1..200) return null
            return ShlokaIds.of(chapter, verse)
        }
        return null
    }
}
