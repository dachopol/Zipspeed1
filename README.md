# Test speed by AnakinYoo

Android-first internet speed-test MVP focused on measurement integrity.

Version in this ZIP: **1.1.0** (`versionCode 2`)

## What works
- GO / STOP flow.
- Provider metadata lookup for IP / ISP / edge / client-area fields when available.
- Measured HTTP latency samples and jitter calculated from those samples.
- Single-stream HTTP download throughput from actual bytes read + monotonic elapsed time.
- Single-stream HTTP upload throughput from actual bytes written + monotonic elapsed time.
- HTTP 2xx validation before accepting a phase.
- Expected payload-byte validation before accepting download/upload results.
- `Accept-Encoding: identity` and no-cache request headers for measurement byte integrity.
- Unknown metadata displays `--` rather than guessed data.
- Re-test clears stale results/metadata first.
- Leaving the activity cancels an active test to avoid hidden background transfer.
- Dark/light appearance follows the Android system configuration.

## What this app does NOT claim
- It is not yet a multi-server or multi-stream benchmark.
- HTTP latency is not labeled as ICMP ping.
- Client-area metadata is not GPS and is not labeled as server location.
- No packet-loss result is shown because this MVP does not implement a supported real packet-loss method.
- No outage status, AdMob, paid entitlement, billing, or subscription is simulated.
- This ZIP has not passed a real Play Console release pipeline yet.

## Measurement provider used by this MVP
- Metadata: `https://speed.cloudflare.com/meta`
- Latency/download: `https://speed.cloudflare.com/__down`
- Upload: `https://speed.cloudflare.com/__up`

Provider use must be re-checked before production distribution. For a production-scale product, consider authorized/owned test infrastructure and explicit bandwidth controls.

## Default data usage per complete run
- Download payload requested: 20 MiB.
- Upload payload requested: 5 MiB.
- Plus small metadata/latency requests and protocol overhead.

The test can consume mobile data. The current UI states the default payload size.

## Project configuration
- Package: `com.anakinyoo.testspeed`
- `minSdk = 24`
- `targetSdk = 36`
- `compileSdk = 37`
- Android Gradle Plugin: 9.4.1
- JDK: 17
- AGP 9 built-in Kotlin is used; no separate Kotlin Android plugin is required.

Current platform facts are documented in `PLATFORM_VERIFICATION.md` with official references and verification date.

## Build
This generated ZIP does not bundle an unverified Gradle Wrapper JAR. Open the project in a compatible current Android Studio and sync with Gradle, or generate a trusted wrapper from an official Gradle installation compatible with AGP 9.4 (Gradle 9.6.0+).

Suggested local validation sequence:

```text
Gradle sync
→ unit tests
→ assembleDebug
→ install on real Android device
→ Wi-Fi test
→ cellular test
→ STOP/cancel test
→ offline/error test
→ rotate/responsive test
→ background/onStop cancellation test
```

## Source-of-truth documents
- `MASTER_RULES_APPLIED.md` — permanent build/research integrity rules.
- `APP_SCOPE_LOCK.md` — Must/Should/Could/Won't and acceptance scope.
- `PRD_TRACEABILITY.md` — requirement → code → acceptance criteria.
- `RESEARCH_TO_APP_OS.md` — thesis/research pipeline without fabricated evidence.
- `PLATFORM_VERIFICATION.md` — externally verified Android/Play build facts.
- `BUILD_CHECK.md` — what was and was not actually tested in the generation environment.

## Production gate
Do not call this app production-ready until all of these are real and passed:

```text
Real Android device matrix
→ privacy/data-safety review
→ provider terms/infrastructure review
→ release signing
→ release AAB
→ Play internal/closed testing
→ crash/performance review
→ production release
```

If AdMob or paid ad-free access is later added, insert real consent + AdMob + Play Billing steps before release; do not use simulations as production behavior.
