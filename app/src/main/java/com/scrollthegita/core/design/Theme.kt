package com.scrollthegita.core.design

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.scrollthegita.core.model.ThemeMode

@Immutable
data class GitaPalette(
    val parchment: Boolean,
    val verseLabel: Color,
    val quiet: Color,
)

val LocalGitaPalette = staticCompositionLocalOf {
    GitaPalette(
        parchment = false,
        verseLabel = GitaColors.AntiqueGold,
        quiet = GitaColors.WarmGray,
    )
}

private val DarkScheme = darkColorScheme(
    primary = GitaColors.AntiqueGold,
    onPrimary = GitaColors.Midnight,
    secondary = GitaColors.MutedSaffron,
    onSecondary = GitaColors.Midnight,
    background = GitaColors.Midnight,
    onBackground = GitaColors.SoftCream,
    surface = GitaColors.SheetDark,
    onSurface = GitaColors.SoftCream,
    surfaceVariant = GitaColors.DeepBlue,
    onSurfaceVariant = GitaColors.WarmGray,
    outline = GitaColors.CalmBlueGray,
    primaryContainer = GitaColors.SpiritualIndigo,
    onPrimaryContainer = GitaColors.SoftCream,
)

private val ParchmentScheme = lightColorScheme(
    primary = GitaColors.ParchmentGold,
    onPrimary = GitaColors.SoftCream,
    secondary = Color(0xFF8A5A2A),
    onSecondary = GitaColors.SoftCream,
    background = GitaColors.Parchment,
    onBackground = GitaColors.ParchmentInk,
    surface = GitaColors.SoftCream,
    onSurface = GitaColors.ParchmentInk,
    surfaceVariant = Color(0xFFE7DCCB),
    onSurfaceVariant = Color(0xFF5C5348),
    outline = GitaColors.WarmGray,
    primaryContainer = Color(0xFFF3E2C4),
    onPrimaryContainer = GitaColors.ParchmentInk,
)

fun gitaTypography(devanagari: FontFamily) = androidx.compose.material3.Typography(
    displaySmall = TextStyle(
        fontFamily = devanagari,
        fontWeight = FontWeight.Normal,
        fontSize = 30.sp,
        lineHeight = 48.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = devanagari,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 34.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 30.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
)

@Composable
fun GitaTheme(
    themeMode: ThemeMode,
    textScale: Float,
    content: @Composable () -> Unit,
) {
    val parchment = when (themeMode) {
        ThemeMode.PARCHMENT -> true
        ThemeMode.DARK -> false
        ThemeMode.SYSTEM -> !isSystemInDarkTheme()
    }
    val palette = if (parchment) {
        GitaPalette(parchment = true, verseLabel = GitaColors.ParchmentGold, quiet = Color(0xFF5C5348))
    } else {
        GitaPalette(parchment = false, verseLabel = GitaColors.AntiqueGold, quiet = GitaColors.WarmGray)
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = parchment
            controller.isAppearanceLightNavigationBars = parchment
        }
    }
    val density = LocalDensity.current
    val scaled = Density(
        density = density.density,
        fontScale = density.fontScale * textScale.coerceIn(0.85f, 1.35f),
    )
    val devanagari = rememberDevanagariFamily()
    CompositionLocalProvider(
        LocalGitaPalette provides palette,
        LocalDensity provides scaled,
    ) {
        MaterialTheme(
            colorScheme = if (parchment) ParchmentScheme else DarkScheme,
            typography = gitaTypography(devanagari),
            content = content,
        )
    }
}
