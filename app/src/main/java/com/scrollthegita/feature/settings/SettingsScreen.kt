package com.scrollthegita.feature.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scrollthegita.R
import com.scrollthegita.core.design.CalmMessage
import com.scrollthegita.core.design.GitaRadioRow
import com.scrollthegita.core.design.GitaSpacing
import com.scrollthegita.core.design.GitaSwitchRow
import com.scrollthegita.core.design.LocalGitaPalette
import com.scrollthegita.core.design.background.SpiritualBackground
import com.scrollthegita.core.model.BackgroundStyle
import com.scrollthegita.core.model.PreferredLanguage
import com.scrollthegita.core.model.ThemeMode
import com.scrollthegita.core.model.UserPreferences
import com.scrollthegita.feature.common.QuietTopBar

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onRestartOnboarding: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
            ),
    ) {
        QuietTopBar(title = stringResource(R.string.settings_title), onBack = onBack)
        when (val current = state) {
            SettingsUiState.Loading -> CalmMessage(stringResource(R.string.loading), stringResource(R.string.loading_cd))
            is SettingsUiState.Error -> CalmMessage(stringResource(R.string.error_title), stringResource(R.string.error_body))
            is SettingsUiState.Content -> SettingsContent(
                preferences = current.preferences,
                viewModel = viewModel,
                onRestartOnboarding = onRestartOnboarding,
            )
        }
    }
}

@Composable
private fun SettingsContent(
    preferences: UserPreferences,
    viewModel: SettingsViewModel,
    onRestartOnboarding: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = GitaSpacing.screenHorizontal)
            .padding(bottom = GitaSpacing.section),
    ) {
        SectionTitle(stringResource(R.string.theme))
        GitaRadioRow(stringResource(R.string.theme_dark), preferences.themeMode == ThemeMode.DARK) {
            viewModel.setTheme(ThemeMode.DARK)
        }
        GitaRadioRow(stringResource(R.string.theme_parchment), preferences.themeMode == ThemeMode.PARCHMENT) {
            viewModel.setTheme(ThemeMode.PARCHMENT)
        }
        GitaRadioRow(stringResource(R.string.theme_system), preferences.themeMode == ThemeMode.SYSTEM) {
            viewModel.setTheme(ThemeMode.SYSTEM)
        }
        SectionTitle(stringResource(R.string.meaning_language))
        GitaRadioRow(stringResource(R.string.language_english), preferences.preferredLanguage == PreferredLanguage.ENGLISH) {
            viewModel.setLanguage(PreferredLanguage.ENGLISH)
        }
        GitaRadioRow(stringResource(R.string.language_hindi), preferences.preferredLanguage == PreferredLanguage.HINDI) {
            viewModel.setLanguage(PreferredLanguage.HINDI)
        }
        GitaSwitchRow(stringResource(R.string.show_sanskrit), preferences.showSanskrit, viewModel::setShowSanskrit)
        GitaSwitchRow(
            title = stringResource(R.string.show_transliteration),
            checked = preferences.showTransliteration,
            onCheckedChange = viewModel::setShowTransliteration,
        )
        GitaSwitchRow(
            title = stringResource(R.string.gentle_animation),
            checked = preferences.animationsEnabled,
            onCheckedChange = viewModel::setAnimations,
            support = stringResource(R.string.gentle_animation_support),
        )
        Text(
            text = stringResource(R.string.animation_system_note),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = GitaSpacing.item),
        )

        SectionTitle(stringResource(R.string.background_style_title))
        BackgroundStylePicker(
            currentStyle = preferences.preferredBackgroundStyle,
            animationsEnabled = preferences.animationsEnabled,
            onSelectStyle = viewModel::setPreferredBackgroundStyle,
        )

        Text(text = stringResource(R.string.text_size), style = MaterialTheme.typography.titleMedium)
        var textScale by remember { mutableFloatStateOf(preferences.textScale) }
        Text(
            text = stringResource(R.string.text_size_value, (textScale * 100).toInt()),
            style = MaterialTheme.typography.labelLarge,
            color = LocalGitaPalette.current.quiet,
        )
        Slider(
            value = textScale,
            onValueChange = { textScale = it },
            onValueChangeFinished = { viewModel.setTextScale(textScale) },
            valueRange = 0.85f..1.35f,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = GitaSpacing.minTouch),
        )
        TextButton(
            onClick = { viewModel.restartOnboarding(onRestartOnboarding) },
            modifier = Modifier.heightIn(min = GitaSpacing.minTouch),
        ) {
            Text(stringResource(R.string.restart_onboarding))
        }
        Text(
            text = stringResource(R.string.restart_onboarding_support),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SectionTitle(stringResource(R.string.about_title))
        Text(text = stringResource(R.string.about_body), style = MaterialTheme.typography.bodyLarge)
        SectionTitle(stringResource(R.string.sources_title))
        Text(text = stringResource(R.string.sources_body), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun BackgroundStylePicker(
    currentStyle: BackgroundStyle?,
    animationsEnabled: Boolean,
    onSelectStyle: (BackgroundStyle?) -> Unit,
) {
    val styles = listOf(
        null to stringResource(R.string.bg_style_adaptive),
        BackgroundStyle.KARMA_SUNRISE to stringResource(R.string.bg_style_karma_sunrise),
        BackgroundStyle.DIVINE_COSMOS to stringResource(R.string.bg_style_divine_cosmos),
        BackgroundStyle.MEDITATION_MANDALA to stringResource(R.string.bg_style_meditation_mandala),
        BackgroundStyle.PARCHMENT_GRAIN to stringResource(R.string.bg_style_parchment_grain),
        BackgroundStyle.DETACHMENT_FLOW to stringResource(R.string.bg_style_detachment_flow),
        BackgroundStyle.PEACE_LOTUS to stringResource(R.string.bg_style_peace_lotus),
    )
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(GitaSpacing.item),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = GitaSpacing.item)
            .testTag("bg_style_picker"),
    ) {
        items(styles) { (style, label) ->
            BackgroundPreviewCard(
                style = style,
                label = label,
                isSelected = currentStyle == style,
                animationsEnabled = animationsEnabled,
                onClick = { onSelectStyle(style) },
            )
        }
    }
}

@Composable
private fun BackgroundPreviewCard(
    style: BackgroundStyle?,
    label: String,
    isSelected: Boolean,
    animationsEnabled: Boolean,
    onClick: () -> Unit,
) {
    val palette = LocalGitaPalette.current
    val previewStyle = style ?: BackgroundStyle.KARMA_SUNRISE
    val borderColor = if (isSelected) {
        palette.verseLabel
    } else {
        palette.verseLabel.copy(alpha = 0.25f)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(110.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) {
                selected = isSelected
            }
            .testTag(if (style == null) "bg_preview_adaptive" else "bg_preview_${style.name}"),
    ) {
        Box(
            modifier = Modifier
                .size(width = 110.dp, height = 75.dp)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(8.dp),
                )
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            SpiritualBackground(
                style = previewStyle,
                animationsEnabled = animationsEnabled && isSelected,
                isPageActive = true,
                motionPaused = false,
                modifier = Modifier.fillMaxSize(),
            )
            Text(
                text = "ॐ",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) palette.verseLabel else palette.quiet,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.padding(horizontal = 2.dp),
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = LocalGitaPalette.current.verseLabel,
        modifier = Modifier.padding(top = GitaSpacing.section, bottom = GitaSpacing.item),
    )
}
