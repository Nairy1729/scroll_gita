package com.scrollthegita.core.design

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily

/**
 * Noto Serif Devanagari (SIL Open Font License) lives at
 * app/src/main/assets/fonts/NotoSerifDevanagari.ttf.
 * Assets are used so the variable font is not rewritten by the resource compiler.
 * If the file cannot be read, Devanagari falls back to the system sans, which on
 * Android includes a Devanagari-capable face.
 */
@Composable
fun rememberDevanagariFamily(): FontFamily {
    val context = LocalContext.current
    return remember(context) { loadDevanagariFamily(context) }
}

internal fun loadDevanagariFamily(context: Context): FontFamily =
    runCatching {
        FontFamily(Font("fonts/NotoSerifDevanagari.ttf", context.assets))
    }.getOrElse {
        FontFamily.Default
    }
