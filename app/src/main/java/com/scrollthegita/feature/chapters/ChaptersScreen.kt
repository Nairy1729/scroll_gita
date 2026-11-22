package com.scrollthegita.feature.chapters

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scrollthegita.R
import com.scrollthegita.core.design.CalmMessage
import com.scrollthegita.core.design.GitaSpacing
import com.scrollthegita.core.design.LocalGitaPalette
import com.scrollthegita.feature.common.QuietTopBar

@Composable
fun ChaptersScreen(
    onBack: () -> Unit,
    onOpenShloka: (String) -> Unit,
    viewModel: ChaptersViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var unavailableNumber by rememberSaveable { mutableStateOf<Int?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
            ),
    ) {
        QuietTopBar(title = stringResource(R.string.chapters_title), onBack = onBack)
        when (val current = state) {
            ChaptersUiState.Loading -> CalmMessage(stringResource(R.string.loading), stringResource(R.string.loading_cd))
            is ChaptersUiState.Error -> CalmMessage(stringResource(R.string.error_title), stringResource(R.string.error_body))
            is ChaptersUiState.Content -> {
                val unavailable = current.chapters.firstOrNull { it.chapter.number == unavailableNumber }
                if (unavailable != null) {
                    CalmMessage(
                        title = stringResource(R.string.chapter_empty_title),
                        body = stringResource(R.string.chapter_empty_body),
                        actionLabel = stringResource(R.string.back_to_chapters),
                        onAction = { unavailableNumber = null },
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(current.chapters, key = { it.chapter.number }) { item ->
                            ChapterRow(
                                item = item,
                                onClick = {
                                    val id = item.firstShlokaId
                                    if (id == null) unavailableNumber = item.chapter.number else onOpenShloka(id)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterRow(item: ChapterListItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = GitaSpacing.screenHorizontal, vertical = GitaSpacing.section),
    ) {
        Text(
            text = stringResource(R.string.chapter_number, item.chapter.number),
            style = MaterialTheme.typography.labelLarge,
            color = LocalGitaPalette.current.verseLabel,
        )
        Text(text = item.chapter.nameSanskrit, style = MaterialTheme.typography.titleLarge)
        Text(text = item.chapter.nameEnglish, style = MaterialTheme.typography.titleMedium)
        Text(
            text = item.chapter.summary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = GitaSpacing.item),
        )
        val progress = if (item.availableShlokas == 0) {
            stringResource(R.string.none_available)
        } else {
            stringResource(R.string.available_count, item.availableShlokas)
        }
        Text(
            text = progress,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = GitaSpacing.item),
        )
        item.firstShlokaId?.let { id ->
            Text(
                text = stringResource(R.string.opens_shloka, id),
                style = MaterialTheme.typography.labelMedium,
                color = LocalGitaPalette.current.quiet,
            )
        }
        Text(
            text = stringResource(R.string.traditional_count, item.chapter.totalShlokas),
            style = MaterialTheme.typography.labelMedium,
            color = LocalGitaPalette.current.quiet,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
}
