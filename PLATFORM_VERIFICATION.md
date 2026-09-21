# Platform verification

Verified on 2026-09-21 against official Android / Google Play documentation.

- Android Gradle Plugin configured: 9.4.1.
- AGP 9.4 requires Gradle 9.6.0 or newer.
- AGP 9.4 supports compile API level up to 37.
- JDK requirement for AGP 9.4: 17.
- AGP 9.x includes built-in Kotlin support; a separate `kotlin-android` plugin is not required for this project.
- Google Play mobile app submissions from 2026-08-31 require target Android 16 / API 36 or higher; this project targets API 36.

Official references:
- https://developer.android.com/build/releases/agp-9-4-0-release-notes
- https://developer.android.com/reference/tools/gradle-api
- https://developer.android.com/build/migrate-to-built-in-kotlin
- https://developer.android.com/google/play/requirements/target-sdk

This verification does not mean the app has passed Play Console review or release testing.
