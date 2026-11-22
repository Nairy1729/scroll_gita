package com.scrollthegita.feature.feed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scrollthegita.R
import com.scrollthegita.core.design.CalmMessage
import com.scrollthegita.core.design.GitaSpacing
import com.scrollthegita.core.model.Shloka
import com.scrollthegita.feature.details.ShlokaDetailsSheet
import com.scrollthegita.feature.share.ShlokaCardRenderer
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

@Composable
fun FeedRoute(
    requestedShlokaId: String,
    onOpenChapters: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: FeedViewModel = hiltViewModel(),
    navigator: ReadingNavigator = activityReadingNavigator(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val request by navigator.request.collectAsStateWithLifecycle()
    val pauseVisible by viewModel.pause.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(requestedShlokaId) {
        if (requestedShlokaId.isNotBlank()) navigator.open(requestedShlokaId)
    }
    FeedScreen(
        state = state,
        scrollToId = request,
        onScrollConsumed = navigator::consume,
        onPageSettled = viewModel::onPageSettled,
        pauseVisible = pauseVisible,
        onDismissPause = viewModel::dismissPause,
        onToggleBookmark = viewModel::toggleSave,
        onOpenDetails = viewModel::openDetails,
        onDismissDetails = viewModel::dismissDetails,
        onOpenChapters = onOpenChapters,
        onOpenLibrary = onOpenLibrary,
        onOpenSettings = onOpenSettings,
        onShareShloka = { shloka ->
            (state as? FeedUiState.Content)?.let { content ->
                ShlokaCardRenderer.shareShloka(
                    context = context,
                    shloka = shloka,
                    preferences = content.preferences,
                )
            }
        },
    )
}

@Composable
fun FeedScreen(
    state: FeedUiState,
    scrollToId: String?,
    onScrollConsumed: () -> Unit,
    onPageSettled: (Int, Boolean) -> Unit,
    pauseVisible: Boolean,
    onDismissPause: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenDetails: () -> Unit,
    onDismissDetails: () -> Unit,
    onOpenChapters: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenSettings: () -> Unit,
    onShareShloka: (Shloka) -> Unit = {},
) {
    when (val current = state) {
        FeedUiState.Loading -> InsetMessage(
            title = stringResource(R.string.loading),
            body = stringResource(R.string.loading_cd),
        )
        FeedUiState.Empty -> InsetMessage(
            title = stringResource(R.string.empty_feed_title),
            body = stringResource(R.string.empty_feed_body),
        )
        is FeedUiState.Error -> InsetMessage(
            title = stringResource(R.string.error_title),
            body = stringResource(R.string.error_body),
        )
        is FeedUiState.Content -> FeedPager(
            content = current,
            scrollToId = scrollToId,
            onScrollConsumed = onScrollConsumed,
            onPageSettled = onPageSettled,
            pauseVisible = pauseVisible,
            onDismissPause = onDismissPause,
            onToggleBookmark = onToggleBookmark,
            onOpenDetails = onOpenDetails,
            onDismissDetails = onDismissDetails,
            onOpenChapters = onOpenChapters,
            onOpenLibrary = onOpenLibrary,
            onOpenSettings = onOpenSettings,
            onShareShloka = onShareShloka,
        )
    }
}

@Composable
private fun InsetMessage(title: String, body: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center,
    ) {
        CalmMessage(title = title, body = body)
    }
}

@Composable
private fun FeedPager(
    content: FeedUiState.Content,
    scrollToId: String?,
    onScrollConsumed: () -> Unit,
    onPageSettled: (Int, Boolean) -> Unit,
    pauseVisible: Boolean,
    onDismissPause: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenDetails: () -> Unit,
    onDismissDetails: () -> Unit,
    onOpenChapters: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenSettings: () -> Unit,
    onShareShloka: (Shloka) -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = content.resumeIndex.coerceIn(0, content.shlokas.lastIndex),
        pageCount = { content.shlokas.size },
    )
    var chromeVisible by remember { mutableStateOf(true) }
    var menuOpen by remember { mutableStateOf(false) }
    val pendingScroll = rememberUpdatedState(scrollToId)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage to pagerState.isScrollInProgress }
            .filter { (_, scrolling) -> !scrolling }
            .map { (page, _) -> page }
            .distinctUntilChanged()
            .collect { page ->
                // Skip the page underneath a pending jump, or progress would be saved too early.
                if (pendingScroll.value == null) onPageSettled(page, false)
            }
    }
    LaunchedEffect(scrollToId, content.shlokas) {
        val id = scrollToId ?: return@LaunchedEffect
        val index = content.shlokas.indexOfFirst { it.id == id }
        if (index >= 0) {
            pagerState.scrollToPage(index)
            onScrollConsumed()
            onPageSettled(index, true)
        }
    }
    VerticalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .testTag("feed_pager"),
        beyondViewportPageCount = 1,
        key = { page -> content.shlokas[page].id },
    ) { page ->
        val shloka = content.shlokas[page]
        ShlokaPage(
            shloka = shloka,
            preferences = content.preferences,
            isPageActive = pagerState.currentPage == page && !pagerState.isScrollInProgress,
            bookmarked = shloka.id in content.bookmarkedIds,
            chromeVisible = chromeVisible,
            bookmarkNote = if (pagerState.currentPage == page) content.bookmarkNote else null,
            onToggleChrome = { chromeVisible = !chromeVisible },
            onUnderstand = onOpenDetails,
            onToggleBookmark = onToggleBookmark,
            onShare = { onShareShloka(shloka) },
            onOpenMenu = {
                onDismissDetails()
                menuOpen = true
            },
        )
    }
    content.details?.let { shloka ->
        ShlokaDetailsSheet(
            shloka = shloka,
            onDismiss = onDismissDetails,
        )
    }
    if (pauseVisible) {
        AlertDialog(
            onDismissRequest = onDismissPause,
            modifier = Modifier.testTag("pause_dialog"),
            title = { Text(stringResource(R.string.pause_title), modifier = Modifier.testTag("pause_title")) },
            text = { Text(stringResource(R.string.pause_body), modifier = Modifier.testTag("pause_body")) },
            confirmButton = {
                TextButton(
                    onClick = onDismissPause,
                    modifier = Modifier
                        .heightIn(min = GitaSpacing.minTouch)
                        .testTag("btn_pause_continue"),
                ) {
                    Text(stringResource(R.string.pause_continue))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismissPause,
                    modifier = Modifier
                        .heightIn(min = GitaSpacing.minTouch)
                        .testTag("btn_pause_here"),
                ) {
                    Text(stringResource(R.string.pause_here))
                }
            },
        )
    }
    if (menuOpen) {
        ReadingMenuSheet(
            onDismiss = { menuOpen = false },
            onChapters = {
                menuOpen = false
                onOpenChapters()
            },
            onSaved = {
                menuOpen = false
                onOpenLibrary()
            },
            onSettings = {
                menuOpen = false
                onOpenSettings()
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReadingMenuSheet(
    onDismiss: () -> Unit,
    onChapters: () -> Unit,
    onSaved: () -> Unit,
    onSettings: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GitaSpacing.screenHorizontal)
                .padding(bottom = GitaSpacing.section),
        ) {
            Text(
                text = stringResource(R.string.menu_title),
                style = MaterialTheme.typography.titleMedium,
            )
            MenuAction(stringResource(R.string.menu_chapters), onChapters, modifier = Modifier.testTag("menu_chapters"))
            MenuAction(stringResource(R.string.menu_saved), onSaved, modifier = Modifier.testTag("menu_saved"))
            MenuAction(stringResource(R.string.menu_settings), onSettings, modifier = Modifier.testTag("menu_settings"))
        }
    }
}

@Composable
private fun MenuAction(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = GitaSpacing.minTouch),
    ) {
        Text(label, modifier = Modifier.fillMaxWidth())
    }
}
