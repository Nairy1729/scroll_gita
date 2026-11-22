package com.scrollthegita

import androidx.compose.ui.graphics.Color
import com.scrollthegita.core.design.GitaColors
import com.scrollthegita.core.design.background.backgroundColors
import com.scrollthegita.core.design.background.shouldAnimateBackground
import com.scrollthegita.core.model.BackgroundStyle
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class BackgroundContrastAndMotionTest {

    @Test
    fun allBackgroundStylesHaveMotionSafeFallbacks() {
        for (style in BackgroundStyle.entries) {
            // Animated when active, allowed, and not paused
            assertTrue(
                "Style $style should animate under normal conditions",
                shouldAnimateBackground(
                    animationsEnabled = true,
                    isPageActive = true,
                    motionPaused = false,
                    systemReducedMotion = false,
                ),
            )

            // Falls back to still when user selects still
            assertFalse(
                "Style $style must fallback to still when animationsEnabled=false",
                shouldAnimateBackground(
                    animationsEnabled = false,
                    isPageActive = true,
                    motionPaused = false,
                    systemReducedMotion = false,
                ),
            )

            // Falls back to still when system reduced motion is active
            assertFalse(
                "Style $style must fallback to still when systemReducedMotion=true",
                shouldAnimateBackground(
                    animationsEnabled = true,
                    isPageActive = true,
                    motionPaused = false,
                    systemReducedMotion = true,
                ),
            )

            // Pauses when user holds down finger
            assertFalse(
                "Style $style must pause motion when finger is pressed",
                shouldAnimateBackground(
                    animationsEnabled = true,
                    isPageActive = true,
                    motionPaused = true,
                    systemReducedMotion = false,
                ),
            )

            // Background pages do not animate
            assertFalse(
                "Style $style must not animate on non-active pages",
                shouldAnimateBackground(
                    animationsEnabled = true,
                    isPageActive = false,
                    motionPaused = false,
                    systemReducedMotion = false,
                ),
            )
        }
    }

    @Test
    fun textContrastMeetsWcagAaInDarkTheme() {
        val darkColors = backgroundColors(parchment = false)
        val textLuminance = relativeLuminance(GitaColors.SoftCream)
        val labelLuminance = relativeLuminance(GitaColors.AntiqueGold)
        val baseLuminance = relativeLuminance(darkColors.base)
        val deepLuminance = relativeLuminance(darkColors.deep)

        // Base background contrast
        val textToBaseContrast = contrastRatio(textLuminance, baseLuminance)
        assertTrue(
            "Dark theme text to base contrast ($textToBaseContrast) must meet WCAG AA (>= 4.5:1)",
            textToBaseContrast >= 4.5,
        )

        val labelToBaseContrast = contrastRatio(labelLuminance, baseLuminance)
        assertTrue(
            "Dark theme label to base contrast ($labelToBaseContrast) must meet WCAG AA (>= 4.5:1)",
            labelToBaseContrast >= 4.5,
        )

        // Gradient deeper background contrast
        val textToDeepContrast = contrastRatio(textLuminance, deepLuminance)
        assertTrue(
            "Dark theme text to deep gradient contrast ($textToDeepContrast) must meet WCAG AA (>= 4.5:1)",
            textToDeepContrast >= 4.5,
        )
    }

    @Test
    fun textContrastMeetsWcagAaInParchmentTheme() {
        val parchmentColors = backgroundColors(parchment = true)
        val textLuminance = relativeLuminance(GitaColors.ParchmentInk)
        val labelLuminance = relativeLuminance(GitaColors.ParchmentGold)
        val baseLuminance = relativeLuminance(parchmentColors.base)
        val deepLuminance = relativeLuminance(parchmentColors.deep)

        val textToBaseContrast = contrastRatio(textLuminance, baseLuminance)
        assertTrue(
            "Parchment theme text to base contrast ($textToBaseContrast) must meet WCAG AA (>= 4.5:1)",
            textToBaseContrast >= 4.5,
        )

        val labelToBaseContrast = contrastRatio(labelLuminance, baseLuminance)
        assertTrue(
            "Parchment theme label to base contrast ($labelToBaseContrast) must meet WCAG AA (>= 4.5:1)",
            labelToBaseContrast >= 4.5,
        )

        val textToDeepContrast = contrastRatio(textLuminance, deepLuminance)
        assertTrue(
            "Parchment theme text to deep gradient contrast ($textToDeepContrast) must meet WCAG AA (>= 4.5:1)",
            textToDeepContrast >= 4.5,
        )
    }

    private fun relativeLuminance(color: Color): Double {
        fun channel(c: Float): Double {
            val v = c.toDouble()
            return if (v <= 0.04045) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
        }
        val r = channel(color.red)
        val g = channel(color.green)
        val b = channel(color.blue)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    private fun contrastRatio(l1: Double, l2: Double): Double {
        val lighter = max(l1, l2)
        val darker = min(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }
}
