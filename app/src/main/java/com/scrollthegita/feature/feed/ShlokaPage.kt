package com.scrollthegita.feature.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.scrollthegita.R
import com.scrollthegita.core.design.GitaSpacing
import com.scrollthegita.core.design.LocalGitaPalette
import com.scrollthegita.core.design.background.SpiritualBackground
import com.scrollthegita.core.model.ContentStatus
import com.scrollthegita.core.model.PreferredLanguage
import com.scrollthegita.core.model.Shloka
import com.scrollthegita.core.model.UserPreferences

@Composable
fun ShlokaPage(
    shloka: Shloka,
    preferences: UserPreferences,
    isPageActive: Boolean,
    bookmarked: Boolean,
    chromeVisible: Boolean,
    bookmarkNote: BookmarkNote?,
    onToggleChrome: () -> Unit,
    onUnderstand: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenMenu: () -> Unit,
    onShare: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var motionPaused by remember { mutableStateOf(false) }
    val reviewLabel = when (shloka.contentStatus) {
        ContentStatus.SAMPLE -> stringResource(R.string.sample_content_label)
        ContentStatus.DRAFT -> stringResource(R.string.draft_content_label)
        ContentStatus.REVIEWED -> stringResource(R.string.reviewed_content_label)
        ContentStatus.APPROVED -> null
    }
    val showSampleNote = shloka.contentStatus == ContentStatus.SAMPLE || shloka.contentStatus == ContentStatus.DRAFT
    val translation = when (preferences.preferredLanguage) {
        PreferredLanguage.HINDI -> shloka.translationHindi ?: shloka.translationEnglish
        PreferredLanguage.ENGLISH -> shloka.translationEnglish
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .pauseMotionWhileTouched { motionPaused = it },
    ) {
        SpiritualBackground(
            style = preferences.preferredBackgroundStyle ?: shloka.backgroundStyle,
            animationsEnabled = preferences.animationsEnabled,
            isPageActive = isPageActive,
            motionPaused = motionPaused,
            modifier = Modifier.fillMaxSize(),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = GitaSpacing.screenHorizontal),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.chapter_verse, shloka.chapter, shloka.verse),
                    style = MaterialTheme.typography.labelLarge,
                    color = LocalGitaPalette.current.verseLabel,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("shloka_chapter_verse"),
                )
                if (chromeVisible) {
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.testTag("btn_share"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = stringResource(R.string.share_action),
                        )
                    }
                    IconButton(
                        onClick = onOpenMenu,
                        modifier = Modifier.testTag("btn_menu"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = stringResource(R.string.open_menu_cd),
                        )
                    }
                }
            }
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = maxHeight)
                        .verticalScroll(rememberScrollState())
                        .testTag("shloka_body"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (reviewLabel != null) {
                        Text(
                            text = reviewLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = LocalGitaPalette.current.quiet,
                        )
                    }
                    if (preferences.showSanskrit) {
                        Spacer(modifier = Modifier.height(GitaSpacing.section))
                        Text(
                            text = stringResource(R.string.sanskrit_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = LocalGitaPalette.current.quiet,
                        )
                        Text(
                            text = shloka.sanskrit,
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(top = GitaSpacing.item)
                                .semantics { heading() },
                        )
                    }
                    if (preferences.showTransliteration) {
                        Spacer(modifier = Modifier.height(GitaSpacing.section))
                        Text(
                            text = stringResource(R.string.transliteration_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = LocalGitaPalette.current.quiet,
                        )
                        Text(
                            text = shloka.transliteration,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = GitaSpacing.item),
                        )
                    }
                    if (showSampleNote) {
                        Spacer(modifier = Modifier.height(GitaSpacing.section))
                        Text(
                            text = stringResource(R.string.sample_orientation_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = LocalGitaPalette.current.quiet,
                        )
                        Text(
                            text = stringResource(R.string.sample_orientation_note),
                            style = MaterialTheme.typography.labelMedium,
                            color = LocalGitaPalette.current.quiet,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .widthIn(max = 360.dp),
                        )
                    }
                    Text(
                        text = translation,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(top = GitaSpacing.item)
                            .widthIn(max = 420.dp),
                    )
                    Text(
                        text = stringResource(R.string.source_line, shloka.sourceTitle),
                        style = MaterialTheme.typography.labelMedium,
                        color = LocalGitaPalette.current.quiet,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(top = GitaSpacing.section)
                            .widthIn(max = 420.dp),
                    )
                }
            }
            if (chromeVisible) {
                TextButton(
                    onClick = onUnderstand,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = GitaSpacing.minTouch)
                        .testTag("btn_understand"),
                ) {
                    Text(stringResource(R.string.understand))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier
                            .heightIn(min = GitaSpacing.minTouch)
                            .testTag("btn_save"),
                    ) {
                        Text(
                            text = if (bookmarked) stringResource(R.string.saved) else stringResource(R.string.save),
                        )
                    }
                    Text(
                        text = stringResource(R.string.reading_position, shloka.chapter, shloka.verse),
                        style = MaterialTheme.typography.labelLarge,
                        color = LocalGitaPalette.current.verseLabel,
                        modifier = Modifier.testTag("shloka_reading_position"),
                    )
                }
                bookmarkNote?.let { note ->
                    val label = when (note) {
                        BookmarkNote.Saved -> stringResource(R.string.saved)
                        BookmarkNote.Removed -> stringResource(R.string.removed)
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = LocalGitaPalette.current.quiet,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    )
                }
                TextButton(
                    onClick = onToggleChrome,
                    modifier = Modifier.heightIn(min = GitaSpacing.minTouch),
                ) {
                    Text(stringResource(R.string.hide_tools))
                }
            } else {
                TextButton(
                    onClick = onToggleChrome,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .heightIn(min = GitaSpacing.minTouch),
                ) {
                    Text(stringResource(R.string.show_tools))
                }
            }
        }
    }
}

/**
 * Observes finger-down without consuming the event, so the pager can still swipe.
 * Motion pauses for as long as a finger is down, including a long press.
 */
@Composable
private fun Modifier.pauseMotionWhileTouched(onPressed: (Boolean) -> Unit): Modifier {
    val current = rememberUpdatedState(onPressed)
    return pointerInput(Unit) {
        awaitPointerEventScope {
            var down = false
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val pressed = event.changes.any { it.pressed }
                if (pressed != down) {
                    down = pressed
                    current.value(pressed)
                }
            }
        }
    }
}
