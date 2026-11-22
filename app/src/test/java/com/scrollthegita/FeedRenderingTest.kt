package com.scrollthegita

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import com.scrollthegita.core.design.GitaTheme
import com.scrollthegita.core.model.ContentStatus
import com.scrollthegita.core.model.ThemeMode
import com.scrollthegita.core.model.UserPreferences
import com.scrollthegita.data.seed.SampleGitaContent
import com.scrollthegita.feature.details.ShlokaDetailsContent
import com.scrollthegita.feature.details.ShlokaDetailsSheet
import com.scrollthegita.feature.feed.ShlokaPage
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class FeedRenderingTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun feedRendersShlokaAndOpensDetails() {
        val shloka = SampleGitaContent.shlokas.first()
        composeRule.setContent {
            GitaTheme(themeMode = ThemeMode.DARK, textScale = 1f) {
                var open by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxSize()) {
                    ShlokaPage(
                        shloka = shloka,
                        preferences = UserPreferences(animationsEnabled = false),
                        isPageActive = false,
                        bookmarked = false,
                        chromeVisible = true,
                        bookmarkNote = null,
                        onToggleChrome = {},
                        onUnderstand = { open = true },
                        onToggleBookmark = {},
                        onOpenMenu = {},
                    )
                    if (open) {
                        ShlokaDetailsSheet(shloka = shloka, onDismiss = { open = false })
                    }
                }
            }
        }
        composeRule.onNodeWithText("Chapter 2 · Verse 47").assertIsDisplayed()
        composeRule.onNodeWithText(shloka.sanskrit).assertIsDisplayed()
        composeRule.onNodeWithText("Understand this shloka").performClick()
        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Reflection").assertIsDisplayed()
        composeRule.onNodeWithText(shloka.reflectionPrompt).assertIsDisplayed()
    }

    @Test
    fun largeFontKeepsTheShlokaInAScrollablePage() {
        val shloka = SampleGitaContent.shlokas.first()
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 2.5f, fontScale = 2f)) {
                GitaTheme(themeMode = ThemeMode.DARK, textScale = 1f) {
                    ShlokaPage(
                        shloka = shloka,
                        preferences = UserPreferences(animationsEnabled = false),
                        isPageActive = false,
                        bookmarked = false,
                        chromeVisible = true,
                        bookmarkNote = null,
                        onToggleChrome = {},
                        onUnderstand = {},
                        onToggleBookmark = {},
                        onOpenMenu = {},
                    )
                }
            }
        }
        assertTrue(composeRule.onAllNodesWithText(shloka.sanskrit).fetchSemanticsNodes().isNotEmpty())
        assertTrue(
            composeRule.onNodeWithTag("shloka_body").fetchSemanticsNode().config.contains(SemanticsActions.ScrollBy),
        )
    }

    @Test
    fun approvedContentDoesNotUseTheSampleLabel() {
        val shloka = SampleGitaContent.shlokas.first().copy(contentStatus = ContentStatus.APPROVED)
        composeRule.setContent {
            GitaTheme(themeMode = ThemeMode.DARK, textScale = 1f) {
                ShlokaPage(
                    shloka = shloka,
                    preferences = UserPreferences(animationsEnabled = false),
                    isPageActive = false,
                    bookmarked = false,
                    chromeVisible = true,
                    bookmarkNote = null,
                    onToggleChrome = {},
                    onUnderstand = {},
                    onToggleBookmark = {},
                    onOpenMenu = {},
                )
            }
        }
        assertTrue(composeRule.onAllNodesWithText("Sample content").fetchSemanticsNodes().isEmpty())
        assertTrue(
            composeRule.onAllNodesWithText("Source: ${shloka.sourceTitle}").fetchSemanticsNodes().isNotEmpty(),
        )
    }

    @Test
    fun longMeaningSheetScrolls() {
        val shloka = SampleGitaContent.shlokas.first().copy(
            simpleMeaning = "Orientation only. ".repeat(80),
        )
        composeRule.setContent {
            GitaTheme(themeMode = ThemeMode.DARK, textScale = 1f) {
                ShlokaDetailsContent(shloka = shloka)
            }
        }
        assertTrue(composeRule.onAllNodesWithText(shloka.simpleMeaning).fetchSemanticsNodes().isNotEmpty())
        composeRule.onNodeWithText("An explanation for orientation. This is not the translation.").assertIsDisplayed()
        assertTrue(
            composeRule.onNodeWithTag("details_sheet").fetchSemanticsNode().config.contains(SemanticsActions.ScrollBy),
        )
    }
}
