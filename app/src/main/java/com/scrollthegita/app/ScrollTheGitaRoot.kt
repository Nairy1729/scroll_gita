package com.scrollthegita.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import com.scrollthegita.core.design.GitaSpacing
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.scrollthegita.R
import com.scrollthegita.core.design.CalmMessage
import com.scrollthegita.core.design.GitaTheme
import com.scrollthegita.core.design.LocalGitaPalette
import com.scrollthegita.core.model.ThemeMode
import com.scrollthegita.core.navigation.Routes
import com.scrollthegita.domain.usecase.EnsureContentSeededUseCase
import com.scrollthegita.domain.usecase.ObserveUserPreferencesUseCase
import com.scrollthegita.feature.chapters.ChaptersScreen
import com.scrollthegita.feature.feed.FeedRoute
import com.scrollthegita.feature.feed.activityReadingNavigator
import com.scrollthegita.feature.library.LibraryScreen
import com.scrollthegita.feature.onboarding.OnboardingScreen
import com.scrollthegita.feature.search.SearchScreen
import com.scrollthegita.feature.settings.SettingsScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AppStartupViewModel @Inject constructor(
    private val ensureSeeded: EnsureContentSeededUseCase,
    private val observePreferences: ObserveUserPreferencesUseCase,
) : ViewModel() {
    val preferences = observePreferences()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _initialRoute = MutableStateFlow<String?>(null)
    val initialRoute: StateFlow<String?> = _initialRoute.asStateFlow()

    private val _failed = MutableStateFlow(false)
    val failed: StateFlow<Boolean> = _failed.asStateFlow()

    init {
        load()
    }

    fun retry() {
        _failed.value = false
        load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                ensureSeeded()
                val prefs = observePreferences().first()
                if (_initialRoute.value == null) {
                    _initialRoute.value = if (prefs.onboardingCompleted) Routes.feed() else Routes.ONBOARDING
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _failed.value = true
            }
        }
    }
}

@Composable
fun ScrollTheGitaRoot(viewModel: AppStartupViewModel = hiltViewModel()) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val route by viewModel.initialRoute.collectAsStateWithLifecycle()
    val failed by viewModel.failed.collectAsStateWithLifecycle()
    val prefs = preferences
    when {
        failed -> GitaTheme(themeMode = ThemeMode.DARK, textScale = 1f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center,
            ) {
                CalmMessage(
                    title = stringResource(R.string.error_title),
                    body = stringResource(R.string.error_body),
                    actionLabel = stringResource(R.string.retry),
                    onAction = viewModel::retry,
                )
            }
        }
        prefs == null || route == null -> GitaTheme(themeMode = ThemeMode.DARK, textScale = 1f) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .semantics(mergeDescendants = true) {},
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(color = LocalGitaPalette.current.verseLabel)
                Text(
                    text = stringResource(R.string.loading),
                    modifier = Modifier.padding(top = GitaSpacing.item),
                    color = LocalGitaPalette.current.verseLabel,
                )
            }
        }
        else -> {
            val startRoute = checkNotNull(route)
            val currentPrefs = checkNotNull(prefs)
            val start = remember { startRoute }
            GitaTheme(themeMode = currentPrefs.themeMode, textScale = currentPrefs.textScale) {
                GitaNavHost(startDestination = start)
            }
        }
    }
}

@Composable
fun GitaNavHost(startDestination: String) {
    val navController = rememberNavController()
    val navigator = activityReadingNavigator()
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Routes.feed()) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }
        composable(
            route = Routes.FEED,
            arguments = listOf(
                navArgument(Routes.SHLOKA_ARG) {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { entry ->
            FeedRoute(
                requestedShlokaId = entry.arguments?.getString(Routes.SHLOKA_ARG).orEmpty(),
                onOpenChapters = { navController.navigate(Routes.CHAPTERS) },
                onOpenLibrary = { navController.navigate(Routes.LIBRARY) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.CHAPTERS) {
            ChaptersScreen(
                onBack = { navController.popBackStack() },
                onOpenSearch = { navController.navigate(Routes.SEARCH) },
                onOpenShloka = { id ->
                    navigator.open(id)
                    navController.popBackStack()
                },
            )
        }
        composable(Routes.SEARCH) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onOpenShloka = { id ->
                    navigator.open(id)
                    navController.popBackStack(Routes.FEED, inclusive = false)
                },
            )
        }
        composable(Routes.LIBRARY) {
            LibraryScreen(
                onBack = { navController.popBackStack() },
                onOpenShloka = { id ->
                    navigator.open(id)
                    navController.popBackStack()
                },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onRestartOnboarding = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
    }
}
