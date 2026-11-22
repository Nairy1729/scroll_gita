package com.scrollthegita.feature.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scrollthegita.R
import com.scrollthegita.core.design.CalmMessage
import com.scrollthegita.core.design.GitaSpacing
import com.scrollthegita.core.design.LocalGitaPalette
import com.scrollthegita.core.model.SavedShloka
import com.scrollthegita.feature.common.QuietTopBar
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen(
    onBack: () -> Unit,
    onOpenShloka: (String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val removedMessage = stringResource(R.string.removed_snackbar)
    val undoLabel = stringResource(R.string.undo)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
        ),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            QuietTopBar(title = stringResource(R.string.library_title), onBack = onBack)
            when (val current = state) {
                LibraryUiState.Loading -> CalmMessage(stringResource(R.string.loading), stringResource(R.string.loading_cd))
                is LibraryUiState.Error -> CalmMessage(stringResource(R.string.error_title), stringResource(R.string.error_body))
                is LibraryUiState.Content -> {
                    if (current.saved.isEmpty()) {
                        CalmMessage(
                            title = stringResource(R.string.library_empty_title),
                            body = stringResource(R.string.library_empty_body),
                        )
                    } else {
                        LazyColumn {
                            items(current.saved, key = { it.shloka.id }) { item ->
                                SavedRow(
                                    item = item,
                                    onOpen = { onOpenShloka(item.shloka.id) },
                                    onRemove = {
                                        viewModel.remove(item.shloka.id)
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = removedMessage,
                                                actionLabel = undoLabel,
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                viewModel.restore(item.shloka.id)
                                            }
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedRow(
    item: SavedShloka,
    onOpen: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(horizontal = GitaSpacing.screenHorizontal, vertical = GitaSpacing.item),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.chapter_verse, item.shloka.chapter, item.shloka.verse),
                style = MaterialTheme.typography.labelLarge,
                color = LocalGitaPalette.current.verseLabel,
            )
            Text(
                text = item.shloka.simpleMeaning,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = GitaSpacing.item),
            )
        }
        TextButton(
            onClick = onRemove,
            modifier = Modifier.heightIn(min = GitaSpacing.minTouch),
        ) {
            Text(stringResource(R.string.remove_saved_cd, item.shloka.id))
        }
    }
}
