package com.scrollthegita

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.scrollthegita.app.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JourneysFlowTest {

    private val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(OnboardingPreferenceRule(completed = true))
        .around(composeRule)

    @Test
    fun opensJourneysAndEntersAndExitsJourney() {
        // Wait for feed to appear
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag("shloka_body").fetchSemanticsNodes().isNotEmpty()
        }

        // Open menu
        composeRule.onAllNodesWithTag("btn_menu").onFirst().performClick()
        composeRule.waitForIdle()

        // Navigate to journeys
        composeRule.onNodeWithTag("menu_journeys").performClick()
        composeRule.waitForIdle()

        // Verify journeys list is displayed
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("journeys_list").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("journeys_list").assertIsDisplayed()

        // Verify journey row for gita_on_duty exists
        composeRule.onNodeWithTag("journey_row_gita_on_duty").assertIsDisplayed()

        // Tap Begin/Continue on gita_on_duty
        composeRule.onNodeWithTag("btn_journey_gita_on_duty").performClick()
        composeRule.waitForIdle()

        // Verify journey header appears with exit button
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("journey_header").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("journey_header").assertIsDisplayed()
        composeRule.onNodeWithTag("btn_exit_journey").assertIsDisplayed()

        // Exit back to full Gita
        composeRule.onNodeWithTag("btn_exit_journey").performClick()
        composeRule.waitForIdle()

        // Verify journey header is no longer displayed
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("journey_header").fetchSemanticsNodes().isEmpty()
        }
    }
}
