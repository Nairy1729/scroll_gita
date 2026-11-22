# Device QA checklist

Use a debug build installed from Android Studio or `app/build/outputs/apk/debug/app-debug.apk`. Stay offline. The reading is still sample text. Do not treat it as scripture.

Mark each item pass or fail. A release-blocking failure is defined at the end.

## Installation

- [ ] The app installs on a phone running Android 8 or newer.
- [ ] The launcher name is Scroll the Gita.
- [ ] The first screen is the three-step introduction, not a network error.
- [ ] Airplane mode is on for the rest of this checklist. Nothing asks for a connection.

## Onboarding

- [x] Step text, Sanskrit, transliteration, language, and motion choices are readable. *(Automated: `OnboardingFlowTest`)*
- [x] Back and Continue are easy to tap. *(Automated: `OnboardingFlowTest`)*
- [ ] At a large system font, each step scrolls instead of clipping the buttons.
- [x] Begin reading opens the shloka screen. *(Automated: `OnboardingFlowTest`)*
- [x] Force-closing during onboarding and reopening restores the choices you already changed. *(Automated: `OnboardingFlowTest`)*

## Feed

- [ ] One shloka fills the screen. The sample label and source line are visible.
- [ ] Sanskrit, transliteration, and the sample translation are separate blocks.
- [x] Swipe up moves to the next shloka. Swipe down moves to the previous one. *(Automated: `FeedPagerTest`)*
- [x] The position line matches the chapter and verse. *(Automated: `FeedPagerTest`)*
- [ ] A long shloka scrolls inside the page. At the end of that scroll, the next swipe changes the page.
- [x] Understand this shloka opens the meaning sheet. The sheet scrolls when the text is long. *(Automated: `UnderstandSheetTest`)*
- [ ] Simple meaning is described as an explanation, not the translation.
- [x] The sheet names the source, edition, translator, reviewer, and license for the sample seed. *(Automated: `UnderstandSheetTest`)*
- [x] Close the sheet by dragging it down or tapping outside it. *(Automated: `UnderstandSheetTest`)*

## Bookmarks

- [x] Save changes the control to Saved. *(Automated: `BookmarkTest`)*
- [x] Saved shlokas lists that shloka. *(Automated: `BookmarkTest`)*
- [x] Remove asks for undo, and undo puts the shloka back. *(Automated: `BookmarkTest`)*
- [x] Opening a saved shloka returns to that verse. *(Automated: `BookmarkTest`)*
- [ ] After a later content update that keeps the same id, the bookmark still opens that verse.

## Process death

- [ ] Scroll to a verse other than the first. Remove the app from recents. Open it again. The same verse is showing, or the pager restores it.
- [ ] Open Understand this shloka, then kill the process. The sheet is open again on that shloka.
- [ ] Change a setting, kill the process, and confirm the setting is still applied.

## Chapters

- [ ] All 18 chapters are listed.
- [ ] A chapter with a seeded shloka opens that shloka.
- [ ] A chapter with no shloka explains that it is not in this reading yet.
- [ ] Back returns to the feed without losing the verse you were on, unless you chose another shloka.

## Settings

- [ ] Dark, Parchment, and System themes apply and survive leaving the screen.
- [ ] English and Hindi switch the translation field. Hindi is still a placeholder in this build.
- [ ] Hiding Sanskrit or transliteration removes that block and can be turned back on.
- [ ] The text-size slider moves freely and saves when you lift your finger. The whole screen does not flicker on every tick.
- [ ] Gentle animation off leaves the background still.
- [ ] Review the introduction returns to the three steps.

## Themes

- [ ] Dark: cream text on the midnight background is readable, including the quiet source line.
- [ ] Parchment: ink and gold labels stay readable on the light background.
- [ ] System follows the phone’s light or dark setting.

## TalkBack

- [ ] The chapter and verse are announced before the shloka.
- [ ] Sanskrit is a heading. Transliteration, translation, and source are separate.
- [ ] Sample or draft status is announced. It is not the only way to tell those states apart.
- [ ] Save, menu, and back have spoken names.
- [ ] Saving or removing announces the change.
- [ ] The meaning sheet is read in order: status, verse, simple meaning, reflection, source.

## Large fonts

- [ ] Set the system font to 200%, or the largest size the phone offers if it stops earlier.
- [ ] The shloka is not shrunk to fit. It scrolls.
- [ ] Buttons remain at least about 48 dp and are not covered by the navigation bar.
- [ ] Onboarding, chapters, saved shlokas, and settings scroll.

## Reduced motion

- [ ] Settings → Accessibility → Remove animations, or set animator duration scale to off.
- [ ] The background does not drift, including on the visible shloka.
- [ ] Turn system animation back on and leave gentle motion enabled. Only the settled page moves. During a swipe, the pages stay still.

## Small and large screens

- [ ] On a small phone, the menu, save, and understand controls stay on screen or scroll into reach. They are not under the gesture bar or a cutout.
- [ ] In landscape, text and controls clear the cutout and the system bars.
- [ ] On a large phone or tablet, the shloka column stays a comfortable width and remains centered.

## Offline

- [ ] With airplane mode on, kill and reopen the app. The reading, bookmarks, and settings are still there.
- [ ] No screen asks you to sign in or retry a download.

## Rotation

- [ ] Rotate the feed, the meaning sheet, chapters, and settings.
- [ ] The current shloka, open sheet, and settings values remain.

## Battery and scrolling

- [ ] Leave the app open on one shloka for a few minutes with gentle motion on. The phone should not get hot from this screen. Motion is slow and only on that page.
- [ ] Turn gentle motion off and confirm the background is still.
- [ ] Scroll through every seeded shloka. There is no stutter that makes the text unreadable, and no page animates after you leave it.

## Sacred-content attribution

- [ ] Every sample shloka shows Sample content and the sample-orientation note.
- [ ] The source line is visible without opening the sheet.
- [ ] The sheet shows the sample edition, that there is no translator, that it is not reviewed, and the license statement.
- [ ] Placeholder Sanskrit is visibly marked as needing a verified text.
- [ ] Nothing in the interface looks like a like count, a streak, or a recommendation.

## Release-blocking issues

Stop a release when any of these are true:

- The app crashes, loses bookmarks, or loses the current verse after process death or rotation.
- Sample or draft text can be mistaken for an approved translation.
- Source, license, or the simple-meaning note is missing.
- A control cannot be reached or activated at 200% font, with TalkBack, or on a small phone.
- Motion continues on a page that is not the settled page, or it ignores Remove animations.
- The app requests a network connection or shows social actions.
- A Room upgrade deletes bookmarks or reading progress while shloka ids stay the same.
