package com.scrollthegita.core.database

import com.scrollthegita.core.model.BackgroundStyle
import com.scrollthegita.core.model.Bookmark
import com.scrollthegita.core.model.Chapter
import com.scrollthegita.core.model.ContentStatus
import com.scrollthegita.core.model.JourneyProgress
import com.scrollthegita.core.model.SearchText
import com.scrollthegita.core.model.Shloka
import com.scrollthegita.core.model.ShlokaTheme

internal fun Shloka.toEntity(): ShlokaEntity = ShlokaEntity(
    id = id,
    chapter = chapter,
    verse = verse,
    sanskrit = sanskrit,
    transliteration = transliteration,
    translationEnglish = translationEnglish,
    translationHindi = translationHindi,
    simpleMeaning = simpleMeaning,
    reflectionPrompt = reflectionPrompt,
    themes = themes.joinToString(separator = ",") { it.name },
    backgroundStyle = backgroundStyle.name,
    sourceTitle = sourceTitle,
    sourceAttribution = sourceAttribution,
    sortOrder = sortOrder,
    editionTitle = editionTitle,
    translator = translator,
    reviewer = reviewer,
    licenseStatement = licenseStatement,
    sourceReference = sourceReference,
    contentVersion = contentVersion,
    contentStatus = contentStatus.name,
    searchSanskrit = SearchText.normalize(sanskrit),
    searchTransliteration = SearchText.normalize(transliteration),
    searchEnglish = SearchText.normalize(translationEnglish),
    searchHindi = SearchText.normalize(translationHindi.orEmpty()),
    searchMeaning = SearchText.normalize(simpleMeaning),
    searchThemes = SearchText.normalize(themes.joinToString(" ") { it.name }),
)

internal fun ShlokaEntity.toDomain(): Shloka = Shloka(
    id = id,
    chapter = chapter,
    verse = verse,
    sanskrit = sanskrit,
    transliteration = transliteration,
    translationEnglish = translationEnglish,
    translationHindi = translationHindi,
    simpleMeaning = simpleMeaning,
    reflectionPrompt = reflectionPrompt,
    themes = themes.split(",").filter { it.isNotBlank() }.map { ShlokaTheme.valueOf(it) },
    backgroundStyle = BackgroundStyle.valueOf(backgroundStyle),
    sourceTitle = sourceTitle,
    sourceAttribution = sourceAttribution,
    sortOrder = sortOrder,
    editionTitle = editionTitle,
    translator = translator,
    reviewer = reviewer,
    licenseStatement = licenseStatement,
    sourceReference = sourceReference,
    contentVersion = contentVersion,
    contentStatus = when (contentStatus) {
        "SAMPLE_PLACEHOLDER" -> ContentStatus.SAMPLE
        else -> ContentStatus.valueOf(contentStatus)
    },
)

internal fun Chapter.toEntity(): ChapterEntity = ChapterEntity(
    number = number,
    nameSanskrit = nameSanskrit,
    nameEnglish = nameEnglish,
    summary = summary,
    totalShlokas = totalShlokas,
    isContentComplete = isContentComplete,
    searchSanskrit = SearchText.normalize(nameSanskrit),
    searchEnglish = SearchText.normalize(nameEnglish),
)

internal fun ChapterEntity.toDomain(): Chapter = Chapter(
    number = number,
    nameSanskrit = nameSanskrit,
    nameEnglish = nameEnglish,
    summary = summary,
    totalShlokas = totalShlokas,
    isContentComplete = isContentComplete,
)

internal fun BookmarkEntity.toDomain(): Bookmark = Bookmark(
    shlokaId = shlokaId,
    createdAtEpochMillis = createdAtEpochMillis,
)

internal fun JourneyProgress.toEntity(): JourneyProgressEntity = JourneyProgressEntity(
    journeyId = journeyId,
    currentIndex = currentIndex,
    lastReadAtEpochMillis = lastReadAtEpochMillis,
)

internal fun JourneyProgressEntity.toDomain(): JourneyProgress = JourneyProgress(
    journeyId = journeyId,
    currentIndex = currentIndex,
    lastReadAtEpochMillis = lastReadAtEpochMillis,
)
