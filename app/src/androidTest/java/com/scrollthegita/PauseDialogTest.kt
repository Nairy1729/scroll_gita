package com.scrollthegita

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.scrollthegita.core.design.GitaTheme
import com.scrollthegita.core.model.ThemeMode
import com.scrollthegita.core.model.UserPreferences
import com.scrollthegita.data.seed.SampleGitaContent
import com.scrollthegita.feature.feed.FeedScreen
import com.scrollthegita.feature.feed.FeedUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PauseDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val targetContext
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun triggersReflectionPause_showsTitleAndBothActionsWork() {
        var dismissedCount = 0
        var pauseVisible by mutableStateOf(false)

        val feedContent = FeedUiState.Content(
            shlokas = SampleGitaContent.shlokas,
            resumeIndex = 0,
            bookmarkedIds = emptySet(),
            preferences = UserPreferences(),
            details = null,
            bookmarkNote = null,
        )

        composeTestRule.setContent {
            GitaTheme(themeMode = ThemeMode.DARK, textScale = 1f) {
                FeedScreen(
                    state = feedContent,
                    scrollToId = null,
                    onScrollConsumed = {},
                    onPageSettled = { _, _ -> },
                    pauseVisible = pauseVisible,
                    onDismissPause = {
                        dismissedCount++
                        pauseVisible = false
                    },
                    onToggleBookmark = {},
                    onOpenDetails = {},
                    onDismissDetails = {},
                    onOpenChapters = {},
                    onOpenLibrary = {},
                    onOpenSettings = {},
                )
            }
        }

        // Before triggering pause, dialog is not displayed
        composeTestRule.onNodeWithTag("pause_dialog").assertDoesNotExist()

        // Trigger reflection pause
        composeTestRule.runOnIdle {
            pauseVisible = true
        }

        // Assert pause_dialog and pause_title text are displayed
        val pauseTitle = targetContext.getString(R.string.pause_title)
        val pauseContinue = targetContext.getString(R.string.pause_continue)
        val pauseHere = targetContext.getString(R.string.pause_here)

        composeTestRule.onNodeWithTag("pause_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pause_title").assertIsDisplayed()
        composeTestRule.onNodeWithText(pauseTitle).assertIsDisplayed()

        // Assert both actions are shown
        composeTestRule.onNodeWithTag("btn_pause_continue").assertIsDisplayed()
        composeTestRule.onNodeWithText(pauseContinue).assertIsDisplayed()

        composeTestRule.onNodeWithTag("btn_pause_here").assertIsDisplayed()
        composeTestRule.onNodeWithText(pauseHere).assertIsDisplayed()

        // Click continue action and assert it works
        composeTestRule.onNodeWithTag("btn_pause_continue").performClick()
        assertEquals(1, dismissedCount)
        assertFalse(pauseVisible)
        composeTestRule.onNodeWithTag("pause_dialog").assertDoesNotExist()

        // Trigger again to verify pause_here action
        composeTestRule.runOnIdle {
            pauseVisible = true
        }
        composeTestRule.onNodeWithTag("pause_dialog").assertIsDisplayed()

        // Click pause_here action and assert it works
        composeTestRule.onNodeWithTag("btn_pause_here").performClick()
        assertEquals(2, dismissedCount)
        assertFalse(pauseVisible)
        composeTestRule.onNodeWithTag("pause_dialog").assertDoesNotExist()
    }
}
