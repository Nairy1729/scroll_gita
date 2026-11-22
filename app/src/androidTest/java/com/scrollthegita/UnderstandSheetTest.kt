package com.scrollthegita

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.scrollthegita.app.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UnderstandSheetTest {

    private val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(OnboardingPreferenceRule(completed = true))
        .around(composeRule)

    private val targetContext
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun opensUnderstandSheetAndDisplaysMeaningReflectionAndSourceCitation() {
        // Wait until feed loads
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag("shloka_body").fetchSemanticsNodes().isNotEmpty()
        }

        // Click "Understand this shloka"
        composeRule.onAllNodesWithTag("btn_understand").onFirst().assertIsDisplayed().performClick()

        // Wait until sheet appears
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("details_sheet").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("details_sheet").assertIsDisplayed()

        // Assert simple meaning title and block are displayed
        val simpleMeaningTitle = targetContext.getString(R.string.simple_meaning)
        composeRule.onNodeWithTag("detail_simple_meaning").assertIsDisplayed()
        composeRule.onNodeWithText(simpleMeaningTitle).assertIsDisplayed()

        // Assert reflection prompt title and block are displayed
        val reflectionTitle = targetContext.getString(R.string.reflection)
        composeRule.onNodeWithTag("detail_reflection").assertIsDisplayed()
        composeRule.onNodeWithText(reflectionTitle).assertIsDisplayed()

        // Assert source citation title and block are displayed
        val sourceTitle = targetContext.getString(R.string.source)
        composeRule.onNodeWithTag("detail_source").assertIsDisplayed()
        composeRule.onNodeWithText(sourceTitle).assertIsDisplayed()
    }
}
