package com.scrollthegita.feature.journeys

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.scrollthegita.feature.common.QuietTopBar

@Composable
fun JourneysScreen(
    onBack: () -> Unit,
    onOpenJourney: (String) -> Unit,
    viewModel: JourneysViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
            ),
    ) {
        QuietTopBar(
            title = stringResource(R.string.journeys_title),
            onBack = onBack,
        )

        when (val current = state) {
            JourneysUiState.Loading -> CalmMessage(
                title = stringResource(R.string.loading),
                body = stringResource(R.string.loading_cd),
            )
            is JourneysUiState.Content -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("journeys_list"),
                ) {
                    items(current.items, key = { it.journey.id }) { item ->
                        JourneyRow(
                            item = item,
                            onClick = { onOpenJourney(item.journey.id) },
                        )
                        HorizontalDivider(
                            color = LocalGitaPalette.current.verseLabel.copy(alpha = 0.2f),
                            thickness = 1.dp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JourneyRow(
    item: JourneyItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalGitaPalette.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = GitaSpacing.screenHorizontal, vertical = GitaSpacing.item)
            .testTag("journey_row_${item.journey.id}"),
    ) {
        Text(
            text = item.journey.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.journey.description,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.verseLabel,
        )
        if (item.isComplete) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.journey_completed_note),
                style = MaterialTheme.typography.bodySmall,
                color = palette.verseLabel,
                modifier = Modifier.testTag("journey_complete_${item.journey.id}"),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val label = when {
                item.isComplete -> stringResource(R.string.journey_read_again)
                item.progress != null && item.progress.currentIndex > 0 -> stringResource(R.string.journey_continue)
                else -> stringResource(R.string.journey_begin)
            }
            TextButton(
                onClick = onClick,
                modifier = Modifier
                    .heightIn(min = GitaSpacing.minTouch)
                    .testTag("btn_journey_${item.journey.id}"),
            ) {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
