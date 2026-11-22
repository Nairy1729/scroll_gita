package com.scrollthegita

import android.app.Application
import com.scrollthegita.data.content.JourneyJson
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class JourneyJsonTest {

    @Test
    fun parseValidJsonReturnsJourneys() {
        val json = """
            [
              {
                "id": "test-journey",
                "title": "Test Journey",
                "description": "A journey for testing.",
                "shlokas": ["2.47", "2.48", "3.35"]
              }
            ]
        """.trimIndent()

        val result = JourneyJson.parse(json)
        assertEquals(1, result.size)
        val journey = result.first()
        assertEquals("test-journey", journey.id)
        assertEquals("Test Journey", journey.title)
        assertEquals("A journey for testing.", journey.description)
        assertEquals(listOf("2.47", "2.48", "3.35"), journey.shlokas)
    }

    @Test
    fun parseEmptyOrBlankReturnsEmptyList() {
        assertEquals(emptyList<Any>(), JourneyJson.parse(""))
        assertEquals(emptyList<Any>(), JourneyJson.parse("   "))
    }

    @Test
    fun parseMalformedJsonReturnsEmptyList() {
        assertEquals(emptyList<Any>(), JourneyJson.parse("{ not valid json }"))
        assertEquals(emptyList<Any>(), JourneyJson.parse("[{ incomplete"))
    }

    @Test
    fun parseSkipsEntriesWithMissingRequiredFields() {
        val json = """
            [
              {
                "id": "",
                "title": "No ID",
                "shlokas": ["2.47"]
              },
              {
                "id": "no-shlokas",
                "title": "No Shlokas",
                "shlokas": []
              },
              {
                "id": "valid",
                "title": "Valid",
                "shlokas": ["1.1"]
              }
            ]
        """.trimIndent()

        val result = JourneyJson.parse(json)
        assertEquals(1, result.size)
        assertEquals("valid", result.first().id)
    }

    @Test
    fun shippedJourneysAssetContainsAllRequiredJourneys() {
        val assetFile = File("src/main/assets/journeys.json")
        assertTrue("journeys.json should exist in assets", assetFile.exists())
        val json = assetFile.readText()
        val journeys = JourneyJson.parse(json)

        val requiredIds = setOf(
            "gita_on_duty",
            "gita_on_fear_and_anxiety",
            "gita_on_equanimity",
            "chapter_2_in_7_days",
            "essence_in_18_shlokas",
        )

        assertEquals("Must contain all 5 required journeys", 5, journeys.size)
        val parsedIds = journeys.map { it.id }.toSet()
        assertEquals(requiredIds, parsedIds)

        for (journey in journeys) {
            assertTrue("Title should not be blank for ${journey.id}", journey.title.isNotBlank())
            assertTrue("Description should not be blank for ${journey.id}", journey.description.isNotBlank())
            assertTrue("Shlokas should not be empty for ${journey.id}", journey.shlokas.isNotEmpty())
            for (ref in journey.shlokas) {
                assertTrue("Shloka ref $ref should match chapter.verse", ref.matches(Regex("""\d+\.\d+""")))
            }
        }

        val essenceJourney = journeys.firstOrNull { it.id == "essence_in_18_shlokas" }
        assertNotNull(essenceJourney)
        assertEquals("Essence in 18 shlokas should have exactly 18 shlokas", 18, essenceJourney?.shlokas?.size)

        val chapter2Journey = journeys.firstOrNull { it.id == "chapter_2_in_7_days" }
        assertNotNull(chapter2Journey)
        assertEquals("Chapter 2 in 7 days should have 7 shlokas", 7, chapter2Journey?.shlokas?.size)
    }
}
