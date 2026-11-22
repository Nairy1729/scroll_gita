package com.scrollthegita.data.seed

import com.scrollthegita.core.model.BackgroundStyle
import com.scrollthegita.core.model.Chapter
import com.scrollthegita.core.model.ContentStatus
import com.scrollthegita.core.model.Shloka
import com.scrollthegita.core.model.ShlokaIds
import com.scrollthegita.core.model.ShlokaTheme

/**
 * SAMPLE CONTENT ONLY.
 *
 * Sanskrit, transliteration, and translations in this file are explicit placeholders.
 * They are not scripture and must not ship as a reading of the Bhagavad Gita.
 *
 * Production content must be legally licensed or in the public domain, then reviewed
 * by a qualified Sanskrit and Gita scholar. Import it with the offline JSON pipeline
 * described in content/gita-content.schema.json. Keep [ShlokaIds] stable.
 * This repository inserts the seed only when the tables are empty. A later valid
 * import updates rows with the same id and leaves bookmarks attached.
 *
 * Chapter titles below are conventional navigation labels, not verse text.
 * Traditional shloka counts vary by edition and are not a completion target.
 */
object SampleGitaContent {
    const val SOURCE_TITLE = "Scroll the Gita sample seed"
    const val CONTENT_VERSION = "sample-0"

    private const val ATTRIBUTION =
        "Prototype placeholder. Do not treat this record as scripture or as a translation. " +
            "Replace it with a licensed or public-domain text reviewed by a qualified Sanskrit scholar."

    val shlokas: List<Shloka> = listOf(
        sample(2, 47, BackgroundStyle.KARMA_SUNRISE, listOf(ShlokaTheme.ACTION, ShlokaTheme.DUTY, ShlokaTheme.DISCIPLINE), "action and duty", "What duty is in front of you, and can you meet it without demanding a particular result?"),
        sample(2, 48, BackgroundStyle.DETACHMENT_FLOW, listOf(ShlokaTheme.EQUANIMITY, ShlokaTheme.DETACHMENT, ShlokaTheme.ACCEPTANCE), "equanimity", "Where are you holding success and failure as if they were the same measure of you?"),
        sample(2, 50, BackgroundStyle.WISDOM_LIGHT, listOf(ShlokaTheme.KNOWLEDGE, ShlokaTheme.CLARITY, ShlokaTheme.REALIZATION), "clarity", "What would this choice look like if you were seeking clarity rather than advantage?"),
        sample(3, 19, BackgroundStyle.KARMA_SUNRISE, listOf(ShlokaTheme.DUTY, ShlokaTheme.DISCIPLINE), "discipline", "Which necessary work can you do today without making it a performance?"),
        sample(4, 7, BackgroundStyle.COURAGE_DAWN, listOf(ShlokaTheme.COURAGE, ShlokaTheme.PURPOSE, ShlokaTheme.RESILIENCE), "courage and purpose", "What would steadiness look like in a moment that asks more of you?"),
        sample(6, 26, BackgroundStyle.MEDITATION_MANDALA, listOf(ShlokaTheme.MEDITATION, ShlokaTheme.MIND, ShlokaTheme.SELF_CONTROL), "the mind", "When attention wanders, can you bring it back gently, without scolding yourself?"),
        sample(9, 22, BackgroundStyle.DEVOTION_AURA, listOf(ShlokaTheme.DEVOTION, ShlokaTheme.FAITH), "devotion", "What would it mean to offer the next small act, rather than to perfect it?"),
        sample(12, 13, BackgroundStyle.PEACE_LOTUS, listOf(ShlokaTheme.PEACE, ShlokaTheme.COMPASSION, ShlokaTheme.EQUANIMITY), "peace and compassion", "Who near you could receive a quieter kind of goodwill today?"),
        sample(18, 66, BackgroundStyle.DIVINE_COSMOS, listOf(ShlokaTheme.SURRENDER, ShlokaTheme.DEVOTION, ShlokaTheme.ETERNAL_SELF, ShlokaTheme.UNIVERSALITY), "surrender", "What are you carrying that is not yours to solve alone?"),
    )

    val chapters: List<Chapter> = listOf(
        chapter(1, "अर्जुनविषादयोगः", "Arjuna Vishada Yoga", "Arjuna’s dilemma.", 47),
        chapter(2, "सांख्ययोगः", "Sankhya Yoga", "Knowledge, and the discipline of action.", 72),
        chapter(3, "कर्मयोगः", "Karma Yoga", "The yoga of action.", 43),
        chapter(4, "ज्ञानकर्मसंन्यासयोगः", "Jnana Karma Sannyasa Yoga", "Knowledge and renunciation of action.", 42),
        chapter(5, "कर्मसंन्यासयोगः", "Karma Sannyasa Yoga", "Renunciation of action.", 29),
        chapter(6, "ध्यानयोगः", "Dhyana Yoga", "Meditation. Some editions title this chapter differently.", 47),
        chapter(7, "ज्ञानविज्ञानयोगः", "Jnana Vijnana Yoga", "Knowledge and realization.", 30),
        chapter(8, "अक्षरब्रह्मयोगः", "Akshara Brahma Yoga", "The imperishable.", 28),
        chapter(9, "राजविद्याराजगुह्ययोगः", "Raja Vidya Raja Guhya Yoga", "Royal knowledge and the royal secret.", 34),
        chapter(10, "विभूतियोगः", "Vibhuti Yoga", "Divine glories.", 42),
        chapter(11, "विश्वरूपदर्शनयोगः", "Vishvarupa Darshana Yoga", "The vision of the universal form.", 55),
        chapter(12, "भक्तियोगः", "Bhakti Yoga", "The yoga of devotion.", 20),
        chapter(13, "क्षेत्रक्षेत्रज्ञविभागयोगः", "Kshetra Kshetrajna Vibhaga Yoga", "The field and the knower of the field.", 35),
        chapter(14, "गुणत्रयविभागयोगः", "Guna Traya Vibhaga Yoga", "The three qualities.", 27),
        chapter(15, "पुरुषोत्तमयोगः", "Purushottama Yoga", "The supreme person.", 20),
        chapter(16, "दैवासुरसम्पद्विभागयोगः", "Daivasura Sampad Vibhaga Yoga", "Divine and demonic qualities.", 24),
        chapter(17, "श्रद्धात्रयविभागयोगः", "Shraddha Traya Vibhaga Yoga", "The three kinds of faith.", 28),
        chapter(18, "मोक्षसंन्यासयोगः", "Moksha Sannyasa Yoga", "Liberation and renunciation.", 78),
    )

    private fun sample(
        chapter: Int,
        verse: Int,
        style: BackgroundStyle,
        themes: List<ShlokaTheme>,
        themeLabel: String,
        reflection: String,
    ) = Shloka(
        id = ShlokaIds.of(chapter, verse),
        chapter = chapter,
        verse = verse,
        sanskrit = "[Verified Sanskrit text required — $chapter.$verse]",
        transliteration = "[Verified transliteration required]",
        translationEnglish = "[Sample — not a licensed translation] A verified English translation of $chapter.$verse is not included. Layout theme: $themeLabel.",
        translationHindi = "[Verified Hindi translation required]",
        simpleMeaning = "Placeholder for a scholar-reviewed explanation. This prototype only names a theme for layout: $themeLabel.",
        reflectionPrompt = reflection,
        themes = themes,
        backgroundStyle = style,
        sourceTitle = SOURCE_TITLE,
        sourceAttribution = ATTRIBUTION,
        sortOrder = ShlokaIds.sortOrder(chapter, verse),
        editionTitle = SOURCE_TITLE,
        translator = "None. This record is not a translation.",
        reviewer = "Not reviewed",
        licenseStatement = "Sample placeholder. Not a licensed edition.",
        sourceReference = "No source edition",
        contentVersion = CONTENT_VERSION,
        contentStatus = ContentStatus.SAMPLE,
    )

    private fun chapter(
        number: Int,
        sanskrit: String,
        english: String,
        summary: String,
        total: Int,
    ) = Chapter(
        number = number,
        nameSanskrit = sanskrit,
        nameEnglish = english,
        summary = "Conventional title for navigation, pending edition review. $summary Source: $SOURCE_TITLE.",
        totalShlokas = total,
    )
}
