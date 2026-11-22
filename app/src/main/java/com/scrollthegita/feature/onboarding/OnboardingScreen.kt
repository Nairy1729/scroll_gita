package com.scrollthegita.feature.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scrollthegita.R
import com.scrollthegita.core.design.GitaRadioRow
import com.scrollthegita.core.design.GitaSpacing
import com.scrollthegita.core.design.GitaSwitchRow
import com.scrollthegita.core.design.LocalGitaPalette
import com.scrollthegita.core.model.PreferredLanguage
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = GitaSpacing.screenHorizontal, vertical = GitaSpacing.section),
    ) {
        Text(
            text = stringResource(R.string.onboarding_step, pagerState.currentPage + 1, 3),
            style = MaterialTheme.typography.labelLarge,
            color = LocalGitaPalette.current.verseLabel,
            modifier = Modifier.testTag("onboarding_step_text"),
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) { page ->
            when (page) {
                0 -> IntroPage()
                1 -> DisplayPage(state, viewModel)
                else -> MotionPage(state, viewModel)
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (pagerState.currentPage > 0) {
                TextButton(
                    onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                    modifier = Modifier
                        .heightIn(min = GitaSpacing.minTouch)
                        .testTag("onboarding_back_button"),
                ) {
                    Text(stringResource(R.string.back))
                }
            }
            TextButton(
                onClick = {
                    if (pagerState.currentPage < 2) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        viewModel.begin(onFinished)
                    }
                },
                modifier = Modifier
                    .heightIn(min = GitaSpacing.minTouch)
                    .testTag("onboarding_next_button"),
            ) {
                Text(
                    if (pagerState.currentPage < 2) {
                        stringResource(R.string.continue_label)
                    } else {
                        stringResource(R.string.begin_reading)
                    },
                )
            }
        }
    }
}

@Composable
private fun IntroPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = GitaSpacing.section),
    ) {
        Text(text = stringResource(R.string.onboarding_title), style = MaterialTheme.typography.labelLarge, color = LocalGitaPalette.current.verseLabel)
        Text(
            text = stringResource(R.string.onboarding_headline),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = GitaSpacing.section),
        )
        Text(
            text = stringResource(R.string.onboarding_body),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = GitaSpacing.section),
        )
    }
}

@Composable
private fun DisplayPage(state: OnboardingUiState, viewModel: OnboardingViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = GitaSpacing.section),
    ) {
        Text(text = stringResource(R.string.onboarding_display_title), style = MaterialTheme.typography.titleLarge)
        Text(
            text = stringResource(R.string.onboarding_display_body),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = GitaSpacing.item, bottom = GitaSpacing.section),
        )
        GitaSwitchRow(
            title = stringResource(R.string.show_sanskrit),
            checked = state.showSanskrit,
            onCheckedChange = viewModel::setShowSanskrit,
            modifier = Modifier.testTag("switch_sanskrit"),
        )
        GitaSwitchRow(
            title = stringResource(R.string.show_transliteration),
            checked = state.showTransliteration,
            onCheckedChange = viewModel::setShowTransliteration,
            modifier = Modifier.testTag("switch_transliteration"),
        )
        Text(
            text = stringResource(R.string.meaning_language),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = GitaSpacing.section, bottom = GitaSpacing.item),
        )
        GitaRadioRow(
            title = stringResource(R.string.language_english),
            selected = state.language == PreferredLanguage.ENGLISH,
            modifier = Modifier.testTag("radio_language_english"),
            onClick = { viewModel.setLanguage(PreferredLanguage.ENGLISH) },
        )
        GitaRadioRow(
            title = stringResource(R.string.language_hindi),
            selected = state.language == PreferredLanguage.HINDI,
            modifier = Modifier.testTag("radio_language_hindi"),
            onClick = { viewModel.setLanguage(PreferredLanguage.HINDI) },
        )
    }
}

@Composable
private fun MotionPage(state: OnboardingUiState, viewModel: OnboardingViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = GitaSpacing.section),
    ) {
        Text(text = stringResource(R.string.onboarding_motion_title), style = MaterialTheme.typography.titleLarge)
        Text(
            text = stringResource(R.string.onboarding_motion_body),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = GitaSpacing.item, bottom = GitaSpacing.section),
        )
        GitaRadioRow(
            title = stringResource(R.string.motion_gentle),
            selected = state.animationsEnabled,
            modifier = Modifier.testTag("radio_motion_gentle"),
            onClick = { viewModel.setAnimationsEnabled(true) },
            support = stringResource(R.string.motion_gentle_support),
        )
        GitaRadioRow(
            title = stringResource(R.string.motion_still),
            selected = !state.animationsEnabled,
            modifier = Modifier.testTag("radio_motion_still"),
            onClick = { viewModel.setAnimationsEnabled(false) },
            support = stringResource(R.string.motion_still_support),
        )
    }
}
