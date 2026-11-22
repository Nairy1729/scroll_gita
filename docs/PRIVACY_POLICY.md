# Privacy Policy for Scroll the Gita

*Last updated: November 22, 2026*

**Scroll the Gita** is built with a commitment to sacred simplicity, contemplation, and absolute user privacy. This policy explains how the application operates in plain, clear language.

---

## 1. No Data Collection

We do not collect, transmit, sell, or share any personal information, usage metrics, or reading habits. 

- **No account required**: You do not need to register, log in, or provide an email address, phone number, or name.
- **No analytics or tracking**: We do not track which shlokas you read, how long you reflect, what you search, or which verses you bookmark.
- **No advertising**: The application contains zero advertisements, trackers, or behavioral profiling tools.

---

## 2. No Network Access

**Scroll the Gita** does not request the `android.permission.INTERNET` permission in its manifest and has no ability to connect to the internet. 

- All 700 shlokas, translations, word-by-word meanings, and audio recitations are bundled directly inside the application on your device.
- Search queries are executed 100% offline using a local database on your device.
- Image card sharing renders locally onto your device cache and delegates directly to Android's native share sheet. No cloud servers are involved.

---

## 3. No Third-Party SDKs

The application does not include any third-party analytics, advertising, tracking, or telemetry SDKs (such as Google Analytics, Firebase Analytics, Facebook SDK, or crash trackers). The app is built entirely using open-source Android Jetpack and Kotlin libraries.

---

## 4. Local On-Device Storage

All application data is stored locally on your device in private app storage:

- **Reading Position**: The last shloka you viewed is saved locally so you can resume where you left off.
- **Bookmarks & Notes**: Verses you save or reflect upon remain on your device in a local Room database.
- **Preferences**: Display language, theme mode (Dark or Parchment), motion settings, and text size are saved locally via Android Jetpack DataStore.

This data never leaves your device. If you uninstall the application or clear app data via Android Settings, all stored preferences, bookmarks, and progress are permanently deleted.

---

## 5. Optional Local Notifications

The application includes an optional daily shloka reminder:

- **Disabled by default**: Daily reminders are turned off unless you explicitly enable them in Settings.
- **100% Local**: Reminders are scheduled directly on your device using Android's WorkManager. No push notification servers or cloud messaging services (such as FCM) are used.
- **Permission**: On Android 13 (API level 33) and higher, the app requests the standard `POST_NOTIFICATIONS` permission only when you toggle reminders on.
- **No Streaks or Gamification**: Reminders never count missed days, send guilt messages, or escalate frequency. You can turn them off at any time.

---

## 6. Permissions Summary

| Permission | Purpose | Optional / Required |
| :--- | :--- | :--- |
| `android.permission.POST_NOTIFICATIONS` | Allows delivering the optional daily shloka reminder. | **Optional** (Requested only if daily reminder is enabled on Android 13+) |

No camera, microphone, location, contact, phone state, or storage permissions are requested.

---

## 7. Children's Privacy

Because **Scroll the Gita** collects no data whatsoever and contains no advertisements or external links, it is safe for users of all ages, including children.

---

## 8. Open Source & Verifiability

The complete source code of **Scroll the Gita** is open and auditable at [https://github.com/Nairy1729/scroll_gita](https://github.com/Nairy1729/scroll_gita). Anyone can inspect the codebase to verify these privacy commitments.

---

## 9. Contact

If you have questions or feedback regarding this privacy policy or the application, please reach out via GitHub:
- **Repository**: [https://github.com/Nairy1729/scroll_gita/issues](https://github.com/Nairy1729/scroll_gita/issues)
