package com.scrollthegita.feature.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.scrollthegita.R
import com.scrollthegita.core.design.GitaSpacing
import com.scrollthegita.core.design.LocalGitaPalette
import com.scrollthegita.core.model.ContentStatus
import com.scrollthegita.core.model.Shloka

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShlokaDetailsSheet(
    shloka: Shloka,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        ShlokaDetailsContent(shloka = shloka)
    }
}

@Composable
fun ShlokaDetailsContent(
    shloka: Shloka,
    modifier: Modifier = Modifier,
) {
    val sheetMaxHeight = with(LocalDensity.current) {
        val heightPx = LocalWindowInfo.current.containerSize.height
        if (heightPx <= 0) 640.dp else heightPx.toDp() * 0.92f
    }
    val reviewLabel = when (shloka.contentStatus) {
        ContentStatus.SAMPLE -> stringResource(R.string.sample_content_label)
        ContentStatus.DRAFT -> stringResource(R.string.draft_content_label)
        ContentStatus.REVIEWED -> stringResource(R.string.reviewed_content_label)
        ContentStatus.APPROVED -> null
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = sheetMaxHeight)
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = GitaSpacing.screenHorizontal)
            .padding(bottom = GitaSpacing.section)
            .testTag("details_sheet"),
    ) {
        if (reviewLabel != null) {
            Text(
                text = reviewLabel,
                style = MaterialTheme.typography.labelMedium,
                color = LocalGitaPalette.current.verseLabel,
            )
        }
        Text(
            text = stringResource(R.string.chapter_verse, shloka.chapter, shloka.verse),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = GitaSpacing.item),
        )
        DetailBlock(
            title = stringResource(R.string.simple_meaning),
            body = shloka.simpleMeaning,
            note = stringResource(R.string.simple_meaning_note),
            testTag = "detail_simple_meaning",
        )
        DetailBlock(
            title = stringResource(R.string.reflection),
            body = shloka.reflectionPrompt,
            testTag = "detail_reflection",
        )
        DetailBlock(
            title = stringResource(R.string.source),
            body = shloka.sourceTitle + "\n" + shloka.sourceAttribution,
            testTag = "detail_source",
        )
        AttributionLine(shloka.editionTitle) { stringResource(R.string.edition_line, it) }
        AttributionLine(shloka.translator) { stringResource(R.string.translator_line, it) }
        AttributionLine(shloka.reviewer) { stringResource(R.string.reviewer_line, it) }
        AttributionLine(shloka.licenseStatement) { stringResource(R.string.license_line, it) }
        AttributionLine(shloka.sourceReference) { stringResource(R.string.source_reference_line, it) }
        AttributionLine(shloka.contentVersion) { stringResource(R.string.content_version_line, it) }
        DetailBlock(
            title = stringResource(R.string.word_by_word),
            body = stringResource(R.string.word_by_word_placeholder),
        )
        DetailBlock(
            title = stringResource(R.string.commentary),
            body = stringResource(R.string.commentary_placeholder),
        )
        Spacer(modifier = Modifier.height(GitaSpacing.section))
        Button(
            onClick = {},
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = GitaSpacing.minTouch),
        ) {
            Text(stringResource(R.string.save_reflection))
        }
        Text(
            text = stringResource(R.string.save_reflection_future),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = GitaSpacing.item),
        )
    }
}

@Composable
private fun DetailBlock(
    title: String,
    body: String,
    note: String? = null,
    testTag: String? = null,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = LocalGitaPalette.current.verseLabel,
        modifier = Modifier
            .padding(top = GitaSpacing.section)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
    )
    if (note != null) {
        Text(
            text = note,
            style = MaterialTheme.typography.labelMedium,
            color = LocalGitaPalette.current.quiet,
            modifier = Modifier.padding(top = GitaSpacing.item),
        )
    }
    Text(
        text = body,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(top = GitaSpacing.item),
    )
}

@Composable
private fun AttributionLine(value: String, label: @Composable (String) -> String) {
    if (value.isBlank()) return
    Text(
        text = label(value),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = GitaSpacing.item),
    )
}
