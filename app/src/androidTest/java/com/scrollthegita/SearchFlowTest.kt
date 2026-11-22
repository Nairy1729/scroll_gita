package com.scrollthegita

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.scrollthegita.app.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchFlowTest {

    private val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(OnboardingPreferenceRule(completed = true))
        .around(composeRule)

    @Test
    fun opensSearchFromChaptersAndNavigatesToMatchingShloka() {
        // Wait for feed to appear
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag("shloka_body").fetchSemanticsNodes().isNotEmpty()
        }

        // Open menu
        composeRule.onAllNodesWithTag("btn_menu").onFirst().performClick()
        composeRule.waitForIdle()

        // Navigate to chapters
        composeRule.onNodeWithTag("menu_chapters").performClick()
        composeRule.waitForIdle()

        // Tap search icon in Chapters top bar
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("btn_search").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("btn_search").performClick()
        composeRule.waitForIdle()

        // Verify search input is displayed
        composeRule.onNodeWithTag("input_search_query").assertIsDisplayed()

        // Type query "duty"
        composeRule.onNodeWithTag("input_search_query").performTextInput("duty")

        // Wait for results to appear
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag("search_result_2.47").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("search_result_2.47").assertIsDisplayed()

        // Tap result to navigate to shloka
        composeRule.onNodeWithTag("search_result_2.47").performClick()
        composeRule.waitForIdle()

        // Verify feed displays the selected shloka
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag("shloka_body").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithTag("shloka_body").onFirst().assertIsDisplayed()
    }
}
