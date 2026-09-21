# Improvement report — v1.1.0

## Fixed
1. Prevented overlapping test starts with an atomic running guard.
2. Added `Accept-Encoding: identity` to keep byte-based throughput accounting honest.
3. Added no-cache headers and disabled connection caching for measurement requests.
4. Added exact payload-length validation for latency/download/upload acceptance.
5. Cleared all previous network metadata and metrics before each new run.
6. Replaced ambiguous `SERVER = edge + client city + country` presentation with separate:
   - TEST PROVIDER
   - EDGE
   - CLIENT AREA
7. Clarified that client area comes from provider metadata, not GPS.
8. Cancelled active transfer when the activity leaves the foreground.
9. Strengthened source guard tests so they fail if the source root is missing instead of silently passing.
10. Added anti-random, scope-lock, PRD traceability, thesis/research integrity, and platform verification documents.

## Verified in generation environment
- Core measurement Kotlin compiles successfully with `kotlinc`.
- No random metric API tokens detected in production source.
- Measurement URLs are HTTPS.
- ZIP packaging is CRC-tested after creation.

## Still requires real Android validation
- Full Android Gradle build / APK / AAB.
- Real device GO/STOP behavior.
- Wi-Fi and cellular accuracy.
- Offline/DNS/timeout behavior on Android runtime.
- Layout across screen sizes/font scales.
- Play Console release pipeline.

No production-ready claim should be made until those checks pass.
