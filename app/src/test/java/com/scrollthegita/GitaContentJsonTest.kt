package com.scrollthegita

import android.app.Application
import com.scrollthegita.core.model.ContentStatus
import com.scrollthegita.core.model.ShlokaIds
import com.scrollthegita.data.content.ContentImportResult
import com.scrollthegita.data.content.GitaContentJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class GitaContentJsonTest {
    @Test
    fun validSampleDocumentImportsInStableOrder() {
        val parsed = GitaContentJson.parse(document(shloka()))
        val valid = parsed as? ContentImportResult.Valid
            ?: error((parsed as ContentImportResult.Invalid).errors.joinToString())
        val shloka = valid.shlokas.single()
        assertEquals("2.47", shloka.id)
        assertEquals(ShlokaIds.sortOrder(2, 47), shloka.sortOrder)
        assertEquals(ContentStatus.SAMPLE, shloka.contentStatus)
        assertEquals(null, shloka.translationHindi)
        assertEquals("Fixture edition", shloka.editionTitle)
        assertEquals(listOf(2), valid.chapters.map { it.number })
    }

    @Test
    fun malformedJsonIsRejectedWithoutThrowing() {
        val parsed = GitaContentJson.parse("{")
        val invalid = parsed as ContentImportResult.Invalid
        assertTrue(invalid.errors.joinToString(), invalid.errors.any { it.contains("JSON") })
    }

    @Test
    fun duplicateShlokaIdIsRejected() {
        val parsed = GitaContentJson.parse(document("${shloka()},${shloka()}"))
        val invalid = parsed as ContentImportResult.Invalid
        assertTrue(invalid.errors.joinToString(), invalid.errors.any { it.contains("duplicated") })
    }

    @Test
    fun verseOutsideChapterIsRejected() {
        val parsed = GitaContentJson.parse(document(shloka(verse = 80, sort = ShlokaIds.sortOrder(2, 80))))
        val invalid = parsed as ContentImportResult.Invalid
        assertTrue(invalid.errors.joinToString(), invalid.errors.any { it.contains("outside chapter 2") })
    }

    @Test
    fun chapterOutsideTheGitaIsRejected() {
        val json = document(shloka(chapter = 19, id = "19.1", verse = 1, sort = ShlokaIds.sortOrder(19, 1)))
            .replace(""""number": 2""", """"number": 19""")
        val parsed = GitaContentJson.parse(json)
        val invalid = parsed as ContentImportResult.Invalid
        assertTrue(invalid.errors.joinToString(), invalid.errors.any { it.contains("outside chapters 1 through 18") })
    }

    @Test
    fun approvedStatusRejectsSamplePlaceholders() {
        val parsed = GitaContentJson.parse(document(shloka(status = "APPROVED")))
        val invalid = parsed as ContentImportResult.Invalid
        assertTrue(
            invalid.errors.joinToString(),
            invalid.errors.any { it.contains("APPROVED") && it.contains("placeholder") },
        )
    }

    @Test
    fun approvedStatusAcceptsReviewedFixtureText() {
        val parsed = GitaContentJson.parse(document(approvedShloka()))
        val valid = parsed as? ContentImportResult.Valid
            ?: error((parsed as ContentImportResult.Invalid).errors.joinToString())
        assertEquals(ContentStatus.APPROVED, valid.shlokas.single().contentStatus)
        val sample = GitaContentJson.parse(document(shloka()))
        val sampleValid = sample as? ContentImportResult.Valid
            ?: error((sample as ContentImportResult.Invalid).errors.joinToString())
        assertEquals(ContentStatus.SAMPLE, sampleValid.shlokas.single().contentStatus)
    }

    @Test
    fun sortOrderMustFollowTheStableIdPolicy() {
        val parsed = GitaContentJson.parse(document(shloka(sort = 1)))
        val invalid = parsed as ContentImportResult.Invalid
        assertTrue(invalid.errors.joinToString(), invalid.errors.any { it.contains("stable order") })
    }

    private fun document(shlokas: String, chapterTotal: Int = 72) = """
        {
          "contentVersion": "sample-0",
          "editionTitle": "Fixture edition",
          "translator": "None. Fixture.",
          "reviewer": "Not reviewed",
          "licenseStatement": "Fixture. Not scripture.",
          "sourceReference": "No edition",
          "chapters": [{
            "number": 2,
            "nameSanskrit": "Fixture",
            "nameEnglish": "Fixture",
            "summary": "Fixture chapter label.",
            "totalShlokas": $chapterTotal
          }],
          "shlokas": [$shlokas]
        }
    """.trimIndent()

    private fun shloka(
        id: String = "2.47",
        chapter: Int = 2,
        verse: Int = 47,
        status: String = "SAMPLE",
        sort: Int = 2047,
    ) = """
        {
          "id": "$id",
          "chapter": $chapter,
          "verse": $verse,
          "sanskrit": "[Verified Sanskrit text required — $id]",
          "transliteration": "[Verified transliteration required]",
          "translationEnglish": "[Sample — not a licensed translation] Fixture.",
          "simpleMeaning": "Placeholder for a scholar-reviewed explanation. Fixture.",
          "reflectionPrompt": "A question written for the app.",
          "themes": ["ACTION"],
          "backgroundStyle": "KARMA_SUNRISE",
          "sourceTitle": "Fixture source",
          "sourceAttribution": "Fixture. Not scripture.",
          "sortOrder": $sort,
          "reviewStatus": "$status"
        }
    """.trimIndent()

    private fun approvedShloka() = """
        {
          "id": "2.47",
          "chapter": 2,
          "verse": 47,
          "sanskrit": "Fixture Sanskrit supplied by the edition file.",
          "transliteration": "Fixture transliteration supplied by the edition file.",
          "translationEnglish": "Fixture English supplied by the edition file. Not shipped as scripture.",
          "simpleMeaning": "Fixture explanation. Not a translation.",
          "reflectionPrompt": "A question written for the app.",
          "themes": ["ACTION"],
          "backgroundStyle": "KARMA_SUNRISE",
          "sourceTitle": "Fixture edition",
          "sourceAttribution": "Fixture attribution for a file that is not included in the app.",
          "sortOrder": 2047,
          "reviewStatus": "APPROVED"
        }
    """.trimIndent()
}
