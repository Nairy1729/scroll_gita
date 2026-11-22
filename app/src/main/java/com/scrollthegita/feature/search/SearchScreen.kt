package com.scrollthegita.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scrollthegita.R
import com.scrollthegita.core.design.CalmMessage
import com.scrollthegita.core.design.GitaSpacing
import com.scrollthegita.core.design.LocalGitaPalette
import com.scrollthegita.core.model.SearchResult

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenShloka: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val resultsState by viewModel.results.collectAsStateWithLifecycle()
    val showSanskrit by viewModel.showSanskrit.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
            ),
    ) {
        SearchTopBar(
            query = query,
            onQueryChange = viewModel::setQuery,
            onClear = viewModel::clearQuery,
            onBack = onBack,
        )

        when (val current = resultsState) {
            SearchUiState.Idle -> {
                CalmMessage(
                    title = stringResource(R.string.search_idle_title),
                    body = stringResource(R.string.search_idle_body),
                )
            }
            SearchUiState.Loading -> {
                CalmMessage(
                    title = stringResource(R.string.loading),
                    body = stringResource(R.string.loading_cd),
                )
            }
            SearchUiState.NoResults -> {
                CalmMessage(
                    title = stringResource(R.string.search_no_results_title),
                    body = stringResource(R.string.search_no_results_body),
                )
            }
            is SearchUiState.Error -> {
                CalmMessage(
                    title = stringResource(R.string.error_title),
                    body = stringResource(R.string.error_body),
                    actionLabel = stringResource(R.string.retry),
                    onAction = viewModel::retry,
                )
            }
            is SearchUiState.Results -> {
                val grouped = current.items.groupBy { it.shloka.chapter }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("search_results_list"),
                ) {
                    grouped.forEach { (chapterNum, shlokasInChapter) ->
                        val chapterTitle = shlokasInChapter.first().chapterTitle
                        item(key = "chapter_header_$chapterNum") {
                            ChapterHeaderRow(chapterNum = chapterNum, chapterTitle = chapterTitle)
                        }
                        items(shlokasInChapter, key = { it.shloka.id }) { result ->
                            SearchResultRow(
                                result = result,
                                showSanskrit = showSanskrit,
                                onClick = { onOpenShloka(result.shloka.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(horizontal = GitaSpacing.item, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back")) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
            )
        }
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    text = stringResource(R.string.search_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClear, modifier = Modifier.testTag("btn_clear_query")) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.search_clear),
                        )
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
            modifier = Modifier
                .weight(1f)
                .testTag("input_search_query"),
        )
    }
}

@Composable
private fun ChapterHeaderRow(chapterNum: Int, chapterTitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = GitaSpacing.screenHorizontal,
                end = GitaSpacing.screenHorizontal,
                top = GitaSpacing.section,
                bottom = 8.dp,
            ),
    ) {
        Text(
            text = if (chapterTitle.isNotEmpty()) {
                "${stringResource(R.string.chapter_number, chapterNum)}: $chapterTitle"
            } else {
                stringResource(R.string.chapter_number, chapterNum)
            },
            style = MaterialTheme.typography.titleMedium,
            color = LocalGitaPalette.current.verseLabel,
        )
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun SearchResultRow(
    result: SearchResult,
    showSanskrit: Boolean,
    onClick: () -> Unit,
) {
    val shloka = result.shloka
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = GitaSpacing.screenHorizontal, vertical = GitaSpacing.item)
            .testTag("search_result_${shloka.id}"),
    ) {
        Text(
            text = stringResource(R.string.chapter_verse, shloka.chapter, shloka.verse),
            style = MaterialTheme.typography.labelLarge,
            color = LocalGitaPalette.current.verseLabel,
        )
        if (showSanskrit && shloka.sanskrit.isNotEmpty()) {
            Text(
                text = shloka.sanskrit,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        if (shloka.transliteration.isNotEmpty()) {
            Text(
                text = shloka.transliteration,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        if (shloka.translationEnglish.isNotEmpty()) {
            Text(
                text = shloka.translationEnglish,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
