package com.scrollthegita.feature.share

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.createBitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.scrollthegita.R
import com.scrollthegita.core.design.GitaTheme
import com.scrollthegita.core.design.LocalGitaPalette
import com.scrollthegita.core.design.background.SpiritualBackground
import com.scrollthegita.core.model.BackgroundStyle
import com.scrollthegita.core.model.PreferredLanguage
import com.scrollthegita.core.model.Shloka
import com.scrollthegita.core.model.ThemeMode
import com.scrollthegita.core.model.UserPreferences
import java.io.File
import java.io.FileOutputStream

enum class CardFormat(val width: Int, val height: Int) {
    PORTRAIT(1080, 1920),
    SQUARE(1080, 1080),
}

data class ShlokaCardData(
    val chapter: Int,
    val verse: Int,
    val referenceText: String,
    val sanskrit: String?,
    val transliteration: String?,
    val translation: String,
    val backgroundStyle: BackgroundStyle,
    val themeMode: ThemeMode,
    val wordmark: String,
)

object ShlokaCardRenderer {

    fun mapToCardData(
        shloka: Shloka,
        preferences: UserPreferences,
        referenceText: String,
        wordmark: String,
    ): ShlokaCardData {
        val translation = when (preferences.preferredLanguage) {
            PreferredLanguage.HINDI -> shloka.translationHindi ?: shloka.translationEnglish
            PreferredLanguage.ENGLISH -> shloka.translationEnglish
        }
        return ShlokaCardData(
            chapter = shloka.chapter,
            verse = shloka.verse,
            referenceText = referenceText,
            sanskrit = if (preferences.showSanskrit) shloka.sanskrit else null,
            transliteration = if (preferences.showTransliteration) shloka.transliteration else null,
            translation = translation,
            backgroundStyle = shloka.backgroundStyle,
            themeMode = preferences.themeMode,
            wordmark = wordmark,
        )
    }

    fun buildPlainTextFallback(
        shloka: Shloka,
        preferences: UserPreferences,
        context: Context,
    ): String {
        val builder = StringBuilder()
        builder.appendLine(context.getString(R.string.chapter_verse, shloka.chapter, shloka.verse))
        if (preferences.showSanskrit) {
            builder.appendLine()
            builder.appendLine(shloka.sanskrit)
        }
        if (preferences.showTransliteration) {
            builder.appendLine()
            builder.appendLine(shloka.transliteration)
        }
        builder.appendLine()
        val translation = when (preferences.preferredLanguage) {
            PreferredLanguage.HINDI -> shloka.translationHindi ?: shloka.translationEnglish
            PreferredLanguage.ENGLISH -> shloka.translationEnglish
        }
        builder.appendLine(translation)
        builder.appendLine()
        builder.appendLine(context.getString(R.string.app_name))
        return builder.toString().trim()
    }

    @Composable
    fun ShlokaCard(
        data: ShlokaCardData,
        format: CardFormat,
        modifier: Modifier = Modifier,
    ) {
        val isSquare = format == CardFormat.SQUARE
        Box(
            modifier = modifier.fillMaxSize(),
        ) {
            SpiritualBackground(
                style = data.backgroundStyle,
                animationsEnabled = false,
                isPageActive = false,
                motionPaused = true,
                modifier = Modifier.fillMaxSize(),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 32.dp,
                        vertical = if (isSquare) 28.dp else 48.dp,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = data.referenceText,
                    style = MaterialTheme.typography.labelLarge,
                    color = LocalGitaPalette.current.verseLabel,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = if (isSquare) 8.dp else 16.dp),
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (data.sanskrit != null) {
                        Text(
                            text = data.sanskrit,
                            style = if (isSquare) MaterialTheme.typography.titleLarge else MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                        )
                    }

                    if (data.transliteration != null) {
                        Spacer(modifier = Modifier.height(if (isSquare) 10.dp else 16.dp))
                        Text(
                            text = data.transliteration,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }

                    Spacer(modifier = Modifier.height(if (isSquare) 12.dp else 20.dp))
                    Text(
                        text = data.translation,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 360.dp),
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = if (isSquare) 8.dp else 16.dp),
                ) {
                    Text(
                        text = data.wordmark,
                        style = MaterialTheme.typography.labelMedium,
                        color = LocalGitaPalette.current.quiet,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }

    fun renderCardToBitmap(
        context: Context,
        data: ShlokaCardData,
        format: CardFormat = CardFormat.PORTRAIT,
    ): Bitmap {
        val width = format.width
        val height = format.height
        val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)

        val composeView = ComposeView(context).apply {
            layoutParams = ViewGroup.LayoutParams(width, height)
            setContent {
                val cardDensity = Density(density = width / 400f, fontScale = 1f)
                CompositionLocalProvider(LocalDensity provides cardDensity) {
                    GitaTheme(themeMode = data.themeMode, textScale = 1f) {
                        ShlokaCard(data = data, format = format)
                    }
                }
            }
        }

        val decorView = (context as? Activity)?.window?.decorView as? ViewGroup
        if (decorView != null) {
            composeView.translationX = 50_000f
            decorView.addView(composeView, ViewGroup.LayoutParams(width, height))
            try {
                val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
                val heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
                composeView.measure(widthSpec, heightSpec)
                composeView.layout(0, 0, width, height)
                composeView.draw(canvas)
            } finally {
                decorView.removeView(composeView)
            }
        } else {
            val lifecycleOwner = (context as? LifecycleOwner) ?: object : LifecycleOwner {
                override val lifecycle: Lifecycle = LifecycleRegistry(this).apply {
                    currentState = Lifecycle.State.RESUMED
                }
            }
            composeView.setViewTreeLifecycleOwner(lifecycleOwner)
            val savedStateOwner = (context as? SavedStateRegistryOwner) ?: object : SavedStateRegistryOwner {
                private val controller = SavedStateRegistryController.create(this).apply {
                    performRestore(null)
                }
                override val lifecycle: Lifecycle = lifecycleOwner.lifecycle
                override val savedStateRegistry: SavedStateRegistry = controller.savedStateRegistry
            }
            composeView.setViewTreeSavedStateRegistryOwner(savedStateOwner)
            if (context is ViewModelStoreOwner) {
                composeView.setViewTreeViewModelStoreOwner(context)
            }
            val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
            composeView.measure(widthSpec, heightSpec)
            composeView.layout(0, 0, width, height)
            composeView.draw(canvas)
        }

        return bitmap
    }

    fun saveCardBitmap(
        context: Context,
        bitmap: Bitmap,
        shlokaId: String,
    ): Uri {
        val cacheDir = File(context.cacheDir, "shared_images").apply { mkdirs() }
        val safeId = shlokaId.replace('.', '_')
        val file = File(cacheDir, "shloka_$safeId.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    }

    fun createShareIntent(
        context: Context,
        imageUri: Uri,
        plainText: String,
    ): Intent {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, plainText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(sendIntent, context.getString(R.string.share_chooser_title))
    }

    @SuppressLint("ThreadConstraint")
    fun shareShloka(
        context: Context,
        shloka: Shloka,
        preferences: UserPreferences,
        format: CardFormat = CardFormat.PORTRAIT,
    ) {
        val cardData = mapToCardData(
            shloka = shloka,
            preferences = preferences,
            referenceText = context.getString(R.string.chapter_verse, shloka.chapter, shloka.verse),
            wordmark = context.getString(R.string.app_name),
        )
        val bitmap = renderCardToBitmap(context, cardData, format)
        val imageUri = saveCardBitmap(context, bitmap, shloka.id)
        val plainText = buildPlainTextFallback(shloka, preferences, context)
        val chooserIntent = createShareIntent(context, imageUri, plainText)
        context.startActivity(chooserIntent)
    }
}
