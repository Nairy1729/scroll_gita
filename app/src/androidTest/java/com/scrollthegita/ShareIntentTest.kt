package com.scrollthegita

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey
import androidx.test.espresso.intent.matcher.IntentMatchers.hasType
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.scrollthegita.app.MainActivity
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.anyOf
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShareIntentTest {

    private val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(OnboardingPreferenceRule(completed = true))
        .around(composeRule)

    @Test
    fun triggersShareAndAssertsActionSendIntentFired() {
        Intents.init()
        try {
            intending(anyIntent()).respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))

            composeRule.waitUntil(10_000) {
                composeRule.onAllNodesWithTag("btn_share").fetchSemanticsNodes().isNotEmpty()
            }

            composeRule.onAllNodesWithTag("btn_share").onFirst().performClick()

            intended(
                anyOf(
                    allOf(
                        hasAction(Intent.ACTION_SEND),
                        hasType("image/png"),
                        hasExtraWithKey(Intent.EXTRA_STREAM),
                        hasExtraWithKey(Intent.EXTRA_TEXT),
                    ),
                    allOf(
                        hasAction(Intent.ACTION_CHOOSER),
                        hasExtra(
                            equalTo(Intent.EXTRA_INTENT),
                            allOf(
                                hasAction(Intent.ACTION_SEND),
                                hasType("image/png"),
                                hasExtraWithKey(Intent.EXTRA_STREAM),
                                hasExtraWithKey(Intent.EXTRA_TEXT),
                            ),
                        ),
                    ),
                ),
            )
        } finally {
            Intents.release()
        }
    }
}
