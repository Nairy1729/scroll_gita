package com.scrollthegita

import com.scrollthegita.core.design.background.shouldAnimateBackground
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MotionPolicyTest {
    @Test
    fun animationRunsOnlyForTheSettledPageWhenMotionIsAllowed() {
        assertTrue(
            shouldAnimateBackground(
                animationsEnabled = true,
                isPageActive = true,
                motionPaused = false,
                systemReducedMotion = false,
            ),
        )
        assertFalse(
            shouldAnimateBackground(
                animationsEnabled = true,
                isPageActive = false,
                motionPaused = false,
                systemReducedMotion = false,
            ),
        )
        assertFalse(
            shouldAnimateBackground(
                animationsEnabled = false,
                isPageActive = true,
                motionPaused = false,
                systemReducedMotion = false,
            ),
        )
        assertFalse(
            shouldAnimateBackground(
                animationsEnabled = true,
                isPageActive = true,
                motionPaused = true,
                systemReducedMotion = false,
            ),
        )
        assertFalse(
            shouldAnimateBackground(
                animationsEnabled = true,
                isPageActive = true,
                motionPaused = false,
                systemReducedMotion = true,
            ),
        )
    }
}
