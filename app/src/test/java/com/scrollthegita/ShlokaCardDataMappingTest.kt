package com.scrollthegita

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.scrollthegita.core.model.BackgroundStyle
import com.scrollthegita.core.model.ContentStatus
import com.scrollthegita.core.model.PreferredLanguage
import com.scrollthegita.core.model.Shloka
import com.scrollthegita.core.model.ThemeMode
import com.scrollthegita.core.model.UserPreferences
import com.scrollthegita.feature.share.CardFormat
import com.scrollthegita.feature.share.ShlokaCardRenderer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ShlokaCardDataMappingTest {

    private lateinit var context: Context

    private val testShloka = Shloka(
        id = "2.47",
        chapter = 2,
        verse = 47,
        sanskrit = "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन",
        transliteration = "karmaṇy-evādhikāras te mā phaleṣu kadācana",
        translationEnglish = "You have a right to your actions, but never to your action's fruits.",
        translationHindi = "तुम्हारा अधिकार केवल कर्म करने में है, उसके फलों में कभी नहीं।",
        simpleMeaning = "Focus on duty rather than outcome.",
        reflectionPrompt = "What action is before you now?",
        themes = emptyList(),
        backgroundStyle = BackgroundStyle.KARMA_SUNRISE,
        sourceTitle = "Scroll the Gita sample seed",
        sourceAttribution = "Prototype placeholder",
        sortOrder = 2047,
        contentStatus = ContentStatus.SAMPLE,
    )

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun mapsAllFieldsWithDefaultPreferences() {
        val prefs = UserPreferences(
            preferredLanguage = PreferredLanguage.ENGLISH,
            showSanskrit = true,
            showTransliteration = true,
            themeMode = ThemeMode.DARK,
        )

        val cardData = ShlokaCardRenderer.mapToCardData(
            shloka = testShloka,
            preferences = prefs,
            referenceText = "Chapter 2 · Verse 47",
            wordmark = "Scroll the Gita",
        )

        assertEquals(2, cardData.chapter)
        assertEquals(47, cardData.verse)
        assertEquals("Chapter 2 · Verse 47", cardData.referenceText)
        assertEquals("कर्मण्येवाधिकारस्ते मा फलेषु कदाचन", cardData.sanskrit)
        assertEquals("karmaṇy-evādhikāras te mā phaleṣu kadācana", cardData.transliteration)
        assertEquals(testShloka.translationEnglish, cardData.translation)
        assertEquals(BackgroundStyle.KARMA_SUNRISE, cardData.backgroundStyle)
        assertEquals(ThemeMode.DARK, cardData.themeMode)
        assertEquals("Scroll the Gita", cardData.wordmark)
    }

    @Test
    fun hidesSanskritWhenDisabledInPreferences() {
        val prefs = UserPreferences(showSanskrit = false)
        val cardData = ShlokaCardRenderer.mapToCardData(
            shloka = testShloka,
            preferences = prefs,
            referenceText = "2.47",
            wordmark = "Scroll the Gita",
        )
        assertNull(cardData.sanskrit)
    }

    @Test
    fun hidesTransliterationWhenDisabledInPreferences() {
        val prefs = UserPreferences(showTransliteration = false)
        val cardData = ShlokaCardRenderer.mapToCardData(
            shloka = testShloka,
            preferences = prefs,
            referenceText = "2.47",
            wordmark = "Scroll the Gita",
        )
        assertNull(cardData.transliteration)
    }

    @Test
    fun usesHindiTranslationWhenHindiSelected() {
        val prefs = UserPreferences(preferredLanguage = PreferredLanguage.HINDI)
        val cardData = ShlokaCardRenderer.mapToCardData(
            shloka = testShloka,
            preferences = prefs,
            referenceText = "2.47",
            wordmark = "Scroll the Gita",
        )
        assertEquals(testShloka.translationHindi, cardData.translation)
    }

    @Test
    fun fallsBackToEnglishWhenHindiTranslationMissing() {
        val shlokaWithoutHindi = testShloka.copy(translationHindi = null)
        val prefs = UserPreferences(preferredLanguage = PreferredLanguage.HINDI)
        val cardData = ShlokaCardRenderer.mapToCardData(
            shloka = shlokaWithoutHindi,
            preferences = prefs,
            referenceText = "2.47",
            wordmark = "Scroll the Gita",
        )
        assertEquals(testShloka.translationEnglish, cardData.translation)
    }

    @Test
    fun preservesParchmentThemeMode() {
        val prefs = UserPreferences(themeMode = ThemeMode.PARCHMENT)
        val cardData = ShlokaCardRenderer.mapToCardData(
            shloka = testShloka,
            preferences = prefs,
            referenceText = "2.47",
            wordmark = "Scroll the Gita",
        )
        assertEquals(ThemeMode.PARCHMENT, cardData.themeMode)
    }

    @Test
    fun cardFormatsHaveExpectedDimensions() {
        assertEquals(1080, CardFormat.PORTRAIT.width)
        assertEquals(1920, CardFormat.PORTRAIT.height)
        assertEquals(1080, CardFormat.SQUARE.width)
        assertEquals(1080, CardFormat.SQUARE.height)
    }

    @Test
    fun buildPlainTextFallbackIncludesAllEnabledComponents() {
        val prefs = UserPreferences(
            preferredLanguage = PreferredLanguage.ENGLISH,
            showSanskrit = true,
            showTransliteration = true,
        )
        val plainText = ShlokaCardRenderer.buildPlainTextFallback(testShloka, prefs, context)

        assertTrue(plainText.contains("Chapter 2 · Verse 47"))
        assertTrue(plainText.contains(testShloka.sanskrit))
        assertTrue(plainText.contains(testShloka.transliteration))
        assertTrue(plainText.contains(testShloka.translationEnglish))
        assertTrue(plainText.contains("Scroll the Gita"))
    }
}
