package com.scrollthegita

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.scrollthegita.app.MainActivity
import com.scrollthegita.core.model.ShlokaIds
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookmarkTest {

    private val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(OnboardingPreferenceRule(completed = true, clearBookmarks = true))
        .around(composeRule)

    private val targetContext
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun savesShlokaOpensLibraryAssertsPresenceRemovesAndRestoresViaUndo() {
        // Wait until feed loads
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag("shloka_chapter_verse").fetchSemanticsNodes().isNotEmpty()
        }

        val saveText = targetContext.getString(R.string.save)
        val savedText = targetContext.getString(R.string.saved)

        // Verify save button initially shows "Save"
        composeRule.onAllNodesWithText(saveText).onFirst().assertIsDisplayed()

        // Click save button
        composeRule.onAllNodesWithTag("btn_save").onFirst().performClick()

        // Wait until saved button text is shown
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText(savedText).fetchSemanticsNodes().isNotEmpty()
        }

        // Open menu
        composeRule.onAllNodesWithTag("btn_menu").onFirst().performClick()

        // Open saved library from menu
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("menu_saved").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("menu_saved").performClick()

        // Assert presence of saved shloka in library
        val shlokaId = ShlokaIds.of(2, 47)
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag("saved_shloka_row_$shlokaId").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("saved_shloka_row_$shlokaId").assertIsDisplayed()

        // Remove the saved shloka
        composeRule.onNodeWithTag("btn_remove_saved_$shlokaId").assertIsDisplayed().performClick()

        // Assert undo snackbar appears
        val removedSnackbarMessage = targetContext.getString(R.string.removed_snackbar)
        val undoLabel = targetContext.getString(R.string.undo)

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText(removedSnackbarMessage).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(removedSnackbarMessage).assertIsDisplayed()
        composeRule.onNodeWithText(undoLabel).assertIsDisplayed()

        // Click undo to restore
        composeRule.onNodeWithText(undoLabel).performClick()

        // Assert presence is restored
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("saved_shloka_row_$shlokaId").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("saved_shloka_row_$shlokaId").assertIsDisplayed()
    }
}
