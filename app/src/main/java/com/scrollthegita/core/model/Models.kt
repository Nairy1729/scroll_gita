package com.scrollthegita.core.model

enum class BackgroundStyle {
    KARMA_SUNRISE,
    PEACE_LOTUS,
    WISDOM_LIGHT,
    DETACHMENT_FLOW,
    DEVOTION_AURA,
    MEDITATION_MANDALA,
    COURAGE_DAWN,
    DIVINE_COSMOS,
    PARCHMENT_GRAIN,
}

enum class ShlokaTheme {
    ACTION,
    DUTY,
    DISCIPLINE,
    PEACE,
    COMPASSION,
    EQUANIMITY,
    KNOWLEDGE,
    CLARITY,
    REALIZATION,
    DETACHMENT,
    ACCEPTANCE,
    IMPERMANENCE,
    DEVOTION,
    SURRENDER,
    FAITH,
    MEDITATION,
    MIND,
    SELF_CONTROL,
    COURAGE,
    PURPOSE,
    RESILIENCE,
    ETERNAL_SELF,
    DIVINE_PRESENCE,
    UNIVERSALITY,
}

enum class PreferredLanguage {
    ENGLISH,
    HINDI,
}

enum class ThemeMode {
    DARK,
    PARCHMENT,
    SYSTEM,
}

/**
 * How far a record is from a scholar-approved reading.
 * SAMPLE and DRAFT stay visibly distinct from APPROVED in the reading UI.
 */
enum class ContentStatus {
    SAMPLE,
    DRAFT,
    REVIEWED,
    APPROVED,
}

data class Shloka(
    val id: String,
    val chapter: Int,
    val verse: Int,
    val sanskrit: String,
    val transliteration: String,
    val translationEnglish: String,
    val translationHindi: String?,
    val simpleMeaning: String,
    val reflectionPrompt: String,
    val themes: List<ShlokaTheme>,
    val backgroundStyle: BackgroundStyle,
    val sourceTitle: String,
    val sourceAttribution: String,
    val sortOrder: Int,
    val editionTitle: String = "",
    val translator: String = "",
    val reviewer: String = "",
    val licenseStatement: String = "",
    val sourceReference: String = "",
    val contentVersion: String = "",
    val contentStatus: ContentStatus = ContentStatus.SAMPLE,
)

data class Chapter(
    val number: Int,
    val nameSanskrit: String,
    val nameEnglish: String,
    val summary: String,
    val totalShlokas: Int,
    val isContentComplete: Boolean = false,
)

data class Bookmark(
    val shlokaId: String,
    val createdAtEpochMillis: Long,
)

data class SavedShloka(
    val bookmark: Bookmark,
    val shloka: Shloka,
)

data class ReadingProgress(
    val lastShlokaId: String,
    val lastChapter: Int,
    val lastVerse: Int,
    val updatedAtEpochMillis: Long,
)

data class UserPreferences(
    val onboardingCompleted: Boolean = false,
    val preferredLanguage: PreferredLanguage = PreferredLanguage.ENGLISH,
    val showSanskrit: Boolean = true,
    val showTransliteration: Boolean = true,
    val animationsEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val textScale: Float = 1f,
    val mindfulPauses: Boolean = true,
    val preferredBackgroundStyle: BackgroundStyle? = null,
)

data class OnboardingDraft(
    val showSanskrit: Boolean,
    val showTransliteration: Boolean,
    val preferredLanguage: PreferredLanguage,
    val animationsEnabled: Boolean,
)

data class Journey(
    val id: String,
    val title: String,
    val description: String,
    val shlokas: List<String>,
)

data class JourneyProgress(
    val journeyId: String,
    val currentIndex: Int,
    val lastReadAtEpochMillis: Long,
)
