package com.scrollthegita.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scrollthegita.R
import com.scrollthegita.core.design.CalmMessage
import com.scrollthegita.core.design.GitaRadioRow
import com.scrollthegita.core.design.GitaSpacing
import com.scrollthegita.core.design.GitaSwitchRow
import com.scrollthegita.core.design.LocalGitaPalette
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
            modifier = Modifier.padding(bottom = GitaSpacing.section),
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
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = LocalGitaPalette.current.verseLabel,
        modifier = Modifier.padding(top = GitaSpacing.section, bottom = GitaSpacing.item),
    )
}
