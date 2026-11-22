package com.scrollthegita

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.scrollthegita.app.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackgroundPickerTest {

    private val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(OnboardingPreferenceRule(completed = true))
        .around(composeRule)

    @Test
    fun opensSettingsAndSelectsBackgroundStyle() {
        // Wait for feed to appear
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag("shloka_body").fetchSemanticsNodes().isNotEmpty()
        }

        // Open menu
        composeRule.onAllNodesWithTag("btn_menu").onFirst().performClick()
        composeRule.waitForIdle()

        // Navigate to settings
        composeRule.onNodeWithTag("menu_settings").performClick()
        composeRule.waitForIdle()

        // Scroll to background picker
        composeRule.onNodeWithTag("bg_style_picker").performScrollTo()
        composeRule.onNodeWithTag("bg_style_picker").assertIsDisplayed()

        // Verify adaptive and style previews exist
        composeRule.onNodeWithTag("bg_preview_adaptive").assertIsDisplayed()
        composeRule.onNodeWithTag("bg_preview_KARMA_SUNRISE").assertIsDisplayed()
        composeRule.onNodeWithTag("bg_preview_DIVINE_COSMOS").assertIsDisplayed()

        // Select DIVINE_COSMOS
        composeRule.onNodeWithTag("bg_preview_DIVINE_COSMOS").performClick()
        composeRule.waitForIdle()

        // Return to feed and verify feed remains functional
        composeRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }
        composeRule.waitForIdle()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("shloka_body").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithTag("shloka_body").onFirst().assertIsDisplayed()
    }
}
