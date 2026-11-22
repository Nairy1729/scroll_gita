package com.scrollthegita

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.scrollthegita.app.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeedPagerTest {

    private val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(OnboardingPreferenceRule(completed = true))
        .around(composeRule)

    private val targetContext
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun swipesVerticallyBetweenShlokasAndUpdatesReference() {
        val ref247 = targetContext.getString(R.string.reading_position, 2, 47)
        val ref248 = targetContext.getString(R.string.reading_position, 2, 48)

        // Wait until feed loads
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag("shloka_body").fetchSemanticsNodes().isNotEmpty()
        }

        // Verify initial shloka reference (2.47) is displayed
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText(ref247).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithText(ref247).onFirst().assertIsDisplayed()

        // Swipe up vertically to move to the next shloka
        composeRule.onAllNodesWithTag("shloka_body").onFirst().performTouchInput {
            swipeUp()
        }

        // Assert shloka reference updates to 2.48
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText(ref248).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithText(ref248).onFirst().assertIsDisplayed()
    }
}
