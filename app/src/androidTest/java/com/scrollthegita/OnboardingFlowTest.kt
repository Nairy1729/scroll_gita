package com.scrollthegita

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.scrollthegita.app.MainActivity
import com.scrollthegita.core.model.PreferredLanguage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingFlowTest {

    private val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(OnboardingPreferenceRule(completed = false))
        .around(composeRule)

    @Test
    fun completesThreeStepOnboardingAndPersistsPreferencesAcrossRecreate() {
        // Step 1: Intro page
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag("onboarding_step_text").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("onboarding_step_text").assertIsDisplayed()
        composeRule.onNodeWithTag("onboarding_next_button").assertIsDisplayed().performClick()

        // Step 2: Display page (toggles and language)
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("switch_sanskrit").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("switch_sanskrit").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("switch_transliteration").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("radio_language_hindi").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("onboarding_next_button").performClick()

        // Step 3: Motion page (gentle vs still motion)
        val beginReadingText = composeRule.activity.getString(R.string.begin_reading)
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("radio_motion_still").fetchSemanticsNodes().isNotEmpty() &&
                composeRule.onAllNodesWithText(beginReadingText).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("radio_motion_still").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("onboarding_next_button").performClick()

        // After completing onboarding, feed opens
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag("feed_pager").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("feed_pager").assertIsDisplayed()
        composeRule.onAllNodesWithTag("btn_menu").onFirst().assertIsDisplayed()

        // Verify preferences are persisted in repository
        val repository = TestHelper.getUserSettingsRepository()
        val settingsBeforeRecreate = runBlocking { repository.observePreferences().first() }
        assertTrue(settingsBeforeRecreate.onboardingCompleted)
        assertEquals(PreferredLanguage.HINDI, settingsBeforeRecreate.preferredLanguage)
        assertFalse(settingsBeforeRecreate.showSanskrit)
        assertFalse(settingsBeforeRecreate.showTransliteration)
        assertFalse(settingsBeforeRecreate.animationsEnabled)

        // Recreate activity
        composeRule.activityRule.scenario.recreate()

        // Assert feed is still shown directly without returning to onboarding
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag("feed_pager").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("feed_pager").assertIsDisplayed()
        composeRule.onAllNodesWithTag("btn_menu").onFirst().assertIsDisplayed()
        composeRule.onNodeWithTag("onboarding_step_text").assertDoesNotExist()

        // Verify preferences remain persisted after recreate
        val settingsAfterRecreate = runBlocking { repository.observePreferences().first() }
        assertTrue(settingsAfterRecreate.onboardingCompleted)
        assertEquals(PreferredLanguage.HINDI, settingsAfterRecreate.preferredLanguage)
        assertFalse(settingsAfterRecreate.showSanskrit)
        assertFalse(settingsAfterRecreate.showTransliteration)
        assertFalse(settingsAfterRecreate.animationsEnabled)
    }
}
