# Scroll the Gita

A peaceful, full-screen, offline Android reading of the Bhagavad Gita. One shloka fills the screen. Swipe up for the next and down for the previous. The background stays quiet so the shloka remains the focus.

This is not a social app. There are no likes, counts, followers, comments, streaks, or recommendations.

**Milestone 1.1** prepares the app for a real device and for a licensed, scholar-reviewed text. The reading is still the Milestone 1 sample. No network, accounts, analytics, audio, or subscriptions.

## Sacred Minimalism

- The shloka is the most important element on the screen.
- Backgrounds create space. They do not compete with the text.
- Motion is slow, low contrast, and optional.
- Sample scripture is labeled as sample content. It is never presented as a verified translation.

## Tech stack

- Kotlin, Jetpack Compose, Material 3
- Gradle Kotlin DSL, Android Gradle Plugin 9.5, Kotlin 2.4
- MVVM, repository, use cases, Hilt
- Room for chapters, shlokas, and bookmarks
- DataStore Preferences for settings and reading progress
- Navigation Compose and a vertical pager
- Canvas backgrounds
- Media3 ExoPlayer is on the classpath for a later audio milestone and is not used
- JUnit, Robolectric, and Compose UI tests

The app is one Gradle module. Packages follow the feature layout (`core`, `data`, `domain`, `feature`) so a later split into modules does not require a rewrite.

## Project structure

```
com.scrollthegita
├── app                 application, navigation, Hilt modules
├── core
│   ├── database        Room
│   ├── design          theme, type, controls, backgrounds
│   ├── model
│   ├── navigation
│   └── common
├── data
│   ├── repository
│   └── seed            sample content
├── domain
│   ├── repository
│   └── usecase
└── feature
    ├── onboarding
    ├── feed
    ├── details
    ├── chapters
    ├── library
    └── settings
```

## Build environment

Verified with:

- Java 17 via Gradle toolchains (auto-provisioned via `foojay-resolver-convention` if JDK 17 is not present locally).
- Android SDK compile SDK 37, min SDK 26, target SDK 36.
- Gradle 9.6.0 and Android Gradle Plugin 9.5.0.
- No network permission in the manifest.

### Local SDK Configuration

`local.properties` contains machine-specific paths and is excluded from version control. Before building via the command line, create `local.properties` at the project root (you can copy [local.properties.example](file:///c:/Users/nairy/gita_scroll/local.properties.example)) and specify your local Android SDK location:

```properties
sdk.dir=C\:\\Users\\<username>\\AppData\\Local\\Android\\Sdk   # Windows
# sdk.dir=/Users/<username>/Library/Android/sdk                # macOS
# sdk.dir=/home/<username>/Android/Sdk                        # Linux
```

Alternatively, opening the project in Android Studio will generate this file automatically.

```
gradlew.bat assembleDebug
gradlew.bat testDebugUnitTest
```

Install `app/build/outputs/apk/debug/app-debug.apk`, or run the `app` configuration from Android Studio. A device checklist is in `docs/QA_CHECKLIST.md`.

The first launch shows a three-step introduction. After “Begin reading”, that introduction stays completed until Settings → Review the introduction.

## Content file

The JSON schema is `content/gita-content.schema.json`. The parser is `GitaContentJson`. It checks chapter numbers 1–18, verse ranges against that file’s chapter counts, unique ids, and the stable sort order. A malformed file returns errors and does not throw.

Review status is `SAMPLE`, `DRAFT`, `REVIEWED`, or `APPROVED`. Sample and draft readings show a label and a note that the text is not a scholarly translation. Reviewed text is labeled as not yet approved. Approved text hides those labels and still shows its source. An approved record that still contains the sample placeholder phrases is rejected. Simple meaning is labeled as an explanation, not the translation.

The current seed stays in `SampleGitaContent`. Every row is `SAMPLE`. Sanskrit, transliteration, and translations are explicit placeholders. There is no `assets/content/gita.json` in this milestone, so a normal install keeps the seed.

## Approval workflow

1. Use a text that is licensed for the app or in the public domain.
2. Have a qualified Sanskrit and Gita scholar review the Sanskrit, transliteration, translation, and explanation.
3. Author a JSON file that matches the schema. Set `reviewStatus` to `APPROVED` only after that review. Include the edition, translator, reviewer, license, and source reference.
4. Validate the file in a unit test or by calling `GitaContentJson.parse`. Do not ship it while the result is `Invalid`.
5. Place the accepted file at `app/src/main/assets/content/gita.json`.
6. On the next launch the app updates rows with the same id and inserts new ones. It does not delete other rows.

Reuse the stable id when a sample verse is replaced. The id format is `{chapter}.{verse}` with no leading zeros. Chapter 2 verse 47 is `2.47`. `sortOrder` must be `chapter * 1000 + verse`. Bookmarks and reading progress store that id. A new id leaves the old bookmark pointing at a row that is no longer updated.

Do not paste a copyrighted translation into the seed or the JSON file.

## Room migrations

The database is `scroll_the_gita.db`, version 2. Version 1 is the Milestone 1 schema. `GitaMigrations.MIGRATION_1_2` adds edition columns and renames `SAMPLE_PLACEHOLDER` to `SAMPLE`. Production code does not use a destructive migration. Bookmarks use a foreign key to the shloka id and are not deleted by that migration.

Reading progress lives in DataStore, not Room, so a content update does not clear it.

## Device testing

Install the debug APK. Turn on airplane mode and walk through `docs/QA_CHECKLIST.md`. Include a small phone, a large screen, TalkBack, a 200% font, Remove animations, dark and parchment themes, and rotation. The shloka page scrolls inside itself when the text is taller than the screen. Swiping at the end of that scroll moves to the next shloka.

## Accessibility and reduced motion

- Text uses `sp`. The system font scale is kept and multiplied by the in-app text size. The app does not shrink type to fit one screen.
- Controls are at least 48 dp. Labels carry meaning, not color alone.
- Safe drawing insets cover the status bar, navigation bar, and display cutout.
- Bookmark confirmation uses a polite live region. Removing a saved shloka uses a snackbar with undo.
- Layout uses start and end padding, and the back icon mirrors in RTL.

Background motion runs only when gentle animation is on, the pager page has settled, a finger is not down, and the system animator duration scale is not 0. Otherwise the same background is drawn still. Only that settled page runs the animation. Paths used by the flow background are reused across frames.

Macrobenchmark and Baseline Profile are the next performance task. They need a connected device and a separate benchmark module, so they are not part of this milestone.

## Known limitations

- Scripture text is still sample placeholder copy.
- Nine shlokas are seeded. The other chapters explain that they are not in this reading yet.
- UI chrome is English. Hindi switches only the translation field, which is also a placeholder.
- There is no account, sync, audio, commentary, or word-by-word meaning.
- “Save reflection” is disabled and labeled as coming later.
- Unit tests run on the JVM with Robolectric. A connected instrumentation run was not part of this environment.

## Milestone 2

Ship one licensed, scholar-approved edition through the JSON file. Add word-by-word meaning only where that license allows it. Then consider optional offline audio. Keep the reading free of social mechanics, recommendations, and network calls. Add Macrobenchmark and a Baseline Profile on a physical device.
