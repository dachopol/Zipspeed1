# Build check — v1.1.0

## Completed in this generation environment
- `SpeedMath.kt` + `SpeedTestEngine.kt` compiled successfully with local `kotlinc` after the v1.1.0 changes.
- Core compile completed without warnings after changing URL construction to `URI(...).toURL()`.
- Source guard was strengthened so it cannot silently skip if the production source root is not found.
- Static project checks confirm:
  - no random metric API tokens in production source;
  - measurement endpoints use HTTPS;
  - raw-byte identity encoding is requested;
  - incomplete download/upload payload rejection exists;
  - stale network metadata is cleared at a new run;
  - provider edge and client-area labels are separated.
- ZIP structure and CRC are checked after packaging.

## Not completed here
This environment does not provide a configured Android SDK / emulator, so the following are **not claimed as passed**:
- Gradle Android `assembleDebug` / APK build.
- Instrumented Android tests.
- Real-device networking behavior.
- UI touch behavior on physical screen sizes.
- Wi-Fi/cellular accuracy comparison.
- Play Console upload/review.

## Required next validation
1. Open with compatible Android Studio / JDK 17.
2. Gradle sync with Android SDK API 37 installed.
3. Run unit tests.
4. Build debug APK.
5. Test GO/STOP repeatedly on a real phone.
6. Test offline, DNS failure, slow connection, Wi-Fi, and cellular.
7. Confirm actual transferred byte counts and error handling.
8. Verify responsive rendering on small/large screens and font scaling.
9. Only after release signing/privacy/AAB/Play testing may production readiness be claimed.
