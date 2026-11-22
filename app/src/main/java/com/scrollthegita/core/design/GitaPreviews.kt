package com.scrollthegita.core.design

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.scrollthegita.core.design.background.SpiritualBackground
import com.scrollthegita.core.model.BackgroundStyle
import com.scrollthegita.core.model.ThemeMode
import com.scrollthegita.core.model.UserPreferences
import com.scrollthegita.data.seed.SampleGitaContent
import com.scrollthegita.feature.feed.ShlokaPage

/** Previews use the sample seed. The text is a placeholder, not scripture. */
@Preview(name = "Dark", widthDp = 360, heightDp = 800)
@Composable
private fun ShlokaDarkPreview() {
    GitaTheme(themeMode = ThemeMode.DARK, textScale = 1f) {
        ShlokaPage(
            shloka = SampleGitaContent.shlokas.first(),
            preferences = UserPreferences(),
            isPageActive = false,
            bookmarked = false,
            chromeVisible = true,
            bookmarkNote = null,
            onToggleChrome = {},
            onUnderstand = {},
            onToggleBookmark = {},
            onOpenMenu = {},
        )
    }
}

@Preview(name = "Parchment", widthDp = 360, heightDp = 800)
@Composable
private fun ShlokaParchmentPreview() {
    GitaTheme(themeMode = ThemeMode.PARCHMENT, textScale = 1f) {
        ShlokaPage(
            shloka = SampleGitaContent.shlokas.first(),
            preferences = UserPreferences(showTransliteration = false),
            isPageActive = false,
            bookmarked = true,
            chromeVisible = true,
            bookmarkNote = null,
            onToggleChrome = {},
            onUnderstand = {},
            onToggleBookmark = {},
            onOpenMenu = {},
        )
    }
}

@Preview(name = "Sunrise still", widthDp = 360, heightDp = 800)
@Composable
private fun SunrisePreview() {
    GitaTheme(themeMode = ThemeMode.DARK, textScale = 1f) {
        SpiritualBackground(
            style = BackgroundStyle.KARMA_SUNRISE,
            animationsEnabled = false,
            isPageActive = false,
            motionPaused = false,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
