package com.scrollthegita.data.content

import com.scrollthegita.core.model.BackgroundStyle
import com.scrollthegita.core.model.Chapter
import com.scrollthegita.core.model.ContentStatus
import com.scrollthegita.core.model.Shloka
import com.scrollthegita.core.model.ShlokaIds
import com.scrollthegita.core.model.ShlokaTheme
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Offline validator for a Gita content file. It does not fetch or invent text.
 * A failure returns messages and does not throw, so a bad file cannot crash startup.
 */
object GitaContentJson {
    const val ASSET_PATH = "content/gita.json"

    fun parse(json: String): ContentImportResult {
        if (json.isBlank()) {
            return invalid("The file is empty.")
        }
        val root = try {
            JSONObject(json)
        } catch (error: JSONException) {
            return invalid("The file is not a JSON object. ${error.message}")
        }
        val errors = mutableListOf<String>()
        val contentVersion = requiredText(root, "contentVersion", "document", errors)
        val editionTitle = requiredText(root, "editionTitle", "document", errors)
        val translator = requiredText(root, "translator", "document", errors)
        val reviewer = requiredText(root, "reviewer", "document", errors)
        val licenseStatement = requiredText(root, "licenseStatement", "document", errors)
        val sourceReference = requiredText(root, "sourceReference", "document", errors)
        val chapters = parseChapters(root.optJSONArray("chapters"), errors)
        val shlokas = parseShlokas(
            array = root.optJSONArray("shlokas"),
            chapters = chapters,
            document = DocumentFields(
                contentVersion = contentVersion.orEmpty(),
                editionTitle = editionTitle.orEmpty(),
                translator = translator.orEmpty(),
                reviewer = reviewer.orEmpty(),
                licenseStatement = licenseStatement.orEmpty(),
                sourceReference = sourceReference.orEmpty(),
            ),
            errors = errors,
        )
        if (root.optJSONArray("chapters") == null) {
            errors += "Document is missing the chapters array."
        }
        if (root.optJSONArray("shlokas") == null) {
            errors += "Document is missing the shlokas array."
        }
        if (errors.isNotEmpty() || contentVersion == null || editionTitle == null || translator == null ||
            reviewer == null || licenseStatement == null || sourceReference == null
        ) {
            return ContentImportResult.Invalid(errors.distinct())
        }
        return ContentImportResult.Valid(chapters.values.toList().sortedBy { it.number }, shlokas)
    }

    private fun parseChapters(array: JSONArray?, errors: MutableList<String>): Map<Int, Chapter> {
        if (array == null) return emptyMap()
        val chapters = linkedMapOf<Int, Chapter>()
        for (index in 0 until array.length()) {
            val where = "chapters[${index + 1}]"
            val row = array.optJSONObject(index)
            if (row == null) {
                errors += "$where must be an object."
                continue
            }
            val number = intField(row, "number", where, errors) ?: continue
            val nameSanskrit = requiredText(row, "nameSanskrit", where, errors)
            val nameEnglish = requiredText(row, "nameEnglish", where, errors)
            val summary = requiredText(row, "summary", where, errors)
            val total = intField(row, "totalShlokas", where, errors)
            if (number !in 1..18) {
                errors += "$where number $number is outside chapters 1 through 18."
            }
            if (total != null && total !in 1..MAX_VERSES) {
                errors += "$where totalShlokas $total is outside 1 through $MAX_VERSES."
            }
            if (chapters.containsKey(number)) {
                errors += "Chapter $number is listed more than once."
            }
            if (nameSanskrit == null || nameEnglish == null || summary == null || total == null) continue
            if (number !in 1..18 || total !in 1..MAX_VERSES) continue
            val complete = booleanField(row, "isContentComplete", where, errors)
            chapters[number] = Chapter(
                number = number,
                nameSanskrit = nameSanskrit,
                nameEnglish = nameEnglish,
                summary = summary,
                totalShlokas = total,
                isContentComplete = complete,
            )
        }
        return chapters
    }

    private fun parseShlokas(
        array: JSONArray?,
        chapters: Map<Int, Chapter>,
        document: DocumentFields,
        errors: MutableList<String>,
    ): List<Shloka> {
        if (array == null) return emptyList()
        val seenIds = mutableSetOf<String>()
        val seenOrder = mutableSetOf<Int>()
        val shlokas = mutableListOf<Shloka>()
        for (index in 0 until array.length()) {
            val where = "shlokas[${index + 1}]"
            val row = array.optJSONObject(index)
            if (row == null) {
                errors += "$where must be an object."
                continue
            }
            val chapter = intField(row, "chapter", where, errors)
            val verse = intField(row, "verse", where, errors)
            val id = requiredText(row, "id", where, errors)
            val sanskrit = requiredText(row, "sanskrit", where, errors)
            val transliteration = requiredText(row, "transliteration", where, errors)
            val translationEnglish = requiredText(row, "translationEnglish", where, errors)
            val simpleMeaning = requiredText(row, "simpleMeaning", where, errors)
            val reflectionPrompt = requiredText(row, "reflectionPrompt", where, errors)
            val sourceTitle = requiredText(row, "sourceTitle", where, errors)
            val sourceAttribution = requiredText(row, "sourceAttribution", where, errors)
            val sortOrder = intField(row, "sortOrder", where, errors)
            val status = reviewStatus(row, where, errors)
            val themes = themes(row, where, errors)
            val background = background(row, where, errors)
            if (chapter == null || verse == null || id == null || sortOrder == null || status == null ||
                themes == null || background == null || sanskrit == null || transliteration == null ||
                translationEnglish == null || simpleMeaning == null || reflectionPrompt == null ||
                sourceTitle == null || sourceAttribution == null
            ) {
                continue
            }
            val expectedId = ShlokaIds.of(chapter, verse)
            if (id != expectedId) {
                errors += "$where id '$id' does not match the stable id '$expectedId'."
            }
            if (!seenIds.add(id)) {
                errors += "Shloka id '$id' is duplicated."
            }
            val expectedOrder = ShlokaIds.sortOrder(chapter, verse)
            if (sortOrder != expectedOrder) {
                errors += "$where sortOrder $sortOrder does not match the stable order $expectedOrder."
            }
            if (!seenOrder.add(sortOrder)) {
                errors += "sortOrder $sortOrder is duplicated."
            }
            val chapterRow = chapters[chapter]
            when {
                chapter !in 1..18 -> errors += "$where chapter $chapter is outside 1 through 18."
                chapterRow == null -> errors += "$where chapter $chapter is not in the chapters array."
                verse !in 1..chapterRow.totalShlokas ->
                    errors += "$where verse $verse is outside chapter $chapter, which lists ${chapterRow.totalShlokas} shlokas."
            }
            val editionTitle = optionalText(row, "editionTitle") ?: document.editionTitle
            val translator = optionalText(row, "translator") ?: document.translator
            val reviewer = optionalText(row, "reviewer") ?: document.reviewer
            val licenseStatement = optionalText(row, "licenseStatement") ?: document.licenseStatement
            val sourceReference = optionalText(row, "sourceReference") ?: document.sourceReference
            val contentVersion = optionalText(row, "contentVersion") ?: document.contentVersion
            if (status == ContentStatus.APPROVED) {
                rejectPlaceholders(where, sanskrit, transliteration, translationEnglish, simpleMeaning, errors)
                if (licenseStatement.isBlank()) {
                    errors += "$where is APPROVED and needs a copyright or license statement."
                }
                if (translator.isBlank() || reviewer.isBlank() || editionTitle.isBlank()) {
                    errors += "$where is APPROVED and needs an edition title, translator, and reviewer."
                }
            }
            shlokas += Shloka(
                id = id,
                chapter = chapter,
                verse = verse,
                sanskrit = sanskrit,
                transliteration = transliteration,
                translationEnglish = translationEnglish,
                translationHindi = optionalText(row, "translationHindi"),
                simpleMeaning = simpleMeaning,
                reflectionPrompt = reflectionPrompt,
                themes = themes,
                backgroundStyle = background,
                sourceTitle = sourceTitle,
                sourceAttribution = sourceAttribution,
                sortOrder = sortOrder,
                editionTitle = editionTitle,
                translator = translator,
                reviewer = reviewer,
                licenseStatement = licenseStatement,
                sourceReference = sourceReference,
                contentVersion = contentVersion,
                contentStatus = status,
            )
        }
        return shlokas
    }

    private fun rejectPlaceholders(
        where: String,
        sanskrit: String,
        transliteration: String,
        translationEnglish: String,
        simpleMeaning: String,
        errors: MutableList<String>,
    ) {
        val combined = listOf(sanskrit, transliteration, translationEnglish, simpleMeaning)
        val marked = PLACEHOLDER_MARKERS.any { marker -> combined.any { field -> field.contains(marker) } }
        if (marked) {
            errors += "$where is APPROVED but still contains sample placeholder text. Mark it SAMPLE or DRAFT until a reviewed text replaces the placeholder."
        }
    }

    private fun reviewStatus(row: JSONObject, where: String, errors: MutableList<String>): ContentStatus? {
        val raw = requiredText(row, "reviewStatus", where, errors) ?: return null
        return runCatching { ContentStatus.valueOf(raw) }.getOrElse {
            errors += "$where reviewStatus '$raw' must be SAMPLE, DRAFT, REVIEWED, or APPROVED."
            null
        }
    }

    private fun themes(row: JSONObject, where: String, errors: MutableList<String>): List<ShlokaTheme>? {
        val array = row.optJSONArray("themes")
        if (array == null || array.length() == 0) {
            errors += "$where needs at least one theme."
            return null
        }
        val themes = mutableListOf<ShlokaTheme>()
        for (index in 0 until array.length()) {
            val name = array.optString(index)
            val theme = runCatching { ShlokaTheme.valueOf(name) }.getOrNull()
            if (theme == null) {
                errors += "$where theme '$name' is not a known theme."
            } else {
                themes += theme
            }
        }
        return themes.takeIf { it.size == array.length() }
    }

    private fun background(row: JSONObject, where: String, errors: MutableList<String>): BackgroundStyle? {
        val raw = requiredText(row, "backgroundStyle", where, errors) ?: return null
        return runCatching { BackgroundStyle.valueOf(raw) }.getOrElse {
            errors += "$where backgroundStyle '$raw' is not a known background."
            null
        }
    }

    private fun requiredText(row: JSONObject, key: String, where: String, errors: MutableList<String>): String? {
        val value = optionalText(row, key)
        if (value == null) {
            errors += "$where is missing $key."
            return null
        }
        return value
    }

    private fun optionalText(row: JSONObject, key: String): String? {
        if (!row.has(key) || row.isNull(key)) return null
        val value = row.optString(key).trim()
        return value.ifBlank { null }
    }

    private fun booleanField(row: JSONObject, key: String, where: String, errors: MutableList<String>): Boolean {
        if (!row.has(key) || row.isNull(key)) return false
        val raw = row.opt(key)
        if (raw is Boolean) return raw
        errors += "$where field $key must be true or false."
        return false
    }

    private fun intField(row: JSONObject, key: String, where: String, errors: MutableList<String>): Int? {
        if (!row.has(key) || row.isNull(key)) {
            errors += "$where is missing $key."
            return null
        }
        return try {
            row.getInt(key)
        } catch (_: JSONException) {
            errors += "$where field $key must be an integer."
            null
        }
    }

    private fun invalid(message: String) = ContentImportResult.Invalid(listOf(message))

    private data class DocumentFields(
        val contentVersion: String,
        val editionTitle: String,
        val translator: String,
        val reviewer: String,
        val licenseStatement: String,
        val sourceReference: String,
    )

    private val PLACEHOLDER_MARKERS = listOf(
        "Verified Sanskrit text required",
        "Verified transliteration required",
        "Verified Hindi translation required",
        "[Sample — not a licensed translation]",
        "Placeholder for a scholar-reviewed explanation",
    )

    private const val MAX_VERSES = 200
}

sealed interface ContentImportResult {
    data class Valid(val chapters: List<Chapter>, val shlokas: List<Shloka>) : ContentImportResult
    data class Invalid(val errors: List<String>) : ContentImportResult
}
