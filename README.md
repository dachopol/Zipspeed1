# Test speed by AnakinYoo

Android-first internet speed-test MVP focused on measurement integrity.

Current source version (verified from `app/build.gradle.kts`): **44.0.0** (`versionCode 44`). Historical ZIP 1.1.0 (versionCode 2) is not the current source. No installed version or Play rollout is inferred.

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
- Application ID: `com.aistudio.zipspeed.zskt`
- Kotlin/Android namespace: `com.anakinyoo.testspeed` (not the application ID)
- `minSdk = 24`
- `targetSdk = 36`
- `compileSdk = 37`
- Android Gradle Plugin: 9.4.1
- JDK: 17
- AGP 9 built-in Kotlin is used; no separate Kotlin Android plugin is required.

Current platform facts are documented in `PLATFORM_VERIFICATION.md` with official references and verification date.

## Build

> Source identity checked against commit `f793e9323cbbda43e543ca6aa484507bbb8b5104`: app/build.gradle.kts, build.gradle.kts, settings.gradle.kts. The remaining historical feature/provider claims require separate current-source/runtime verification. `gradlew.bat` and `gradle/wrapper/gradle-wrapper.properties` were not found at the checked ref. Do not run an assumed wrapper or install a new one automatically.

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


## UCISE Local — คู่มือเริ่มต้นและ Documentation Gate / Getting started

<!-- UCISE_DOCUMENTATION_GATE_V1 -->

**ขั้นตอนเริ่มต้น (TH):** อ่าน What works, What this app does NOT claim และ Measurement provider → ตรวจ Project configuration และ Build → ก่อนเริ่ม GO ให้ตรวจเครือข่ายและปริมาณข้อมูลที่อาจใช้ตาม Default data usage → ใช้ STOP เพื่อหยุด → ตรวจผลที่วัดได้จริงและข้อจำกัด; ไม่ใช้ค่าเดาหรือสรุปว่า production พร้อมจาก MVP.

**Getting started (EN):** Read the project-specific sections above. Confirm exact version, authorized workspace, platform and prerequisites before following any command. Run only documented, authorized steps; record actual output and errors. Do not infer runtime or release readiness from this documentation.

### Required documentation before closing work

- [ ] Purpose, exact version/source identity, prerequisites and installation/configuration.
- [ ] Step-by-step user workflow, examples, expected outputs, and TH/EN guidance appropriate to the audience.
- [ ] Build/run/test commands from actual source where applicable; record host, exact command, exit status and evidence. Untested commands are NOT_VERIFIED, not PASS.
- [ ] Architecture, permissions, safety constraints, troubleshooting, recovery/rollback and limitations.
- [ ] AI handoff: checkpoint, changed files, remaining tasks, dependency and next authorized action; research assumptions and evidence scope when applicable.
- [ ] Review existing documentation first; update it without duplication; never copy secrets or local-only data to the cloud.

Use README sections or existing linked guides; split into QUICKSTART, USER_GUIDE, AGENTS, TESTING, SECURITY, RESEARCH or RECOVERY files only when useful. Missing required documentation or evidence keeps the relevant DoD item INCOMPLETE/BLOCKED. This checklist is not a test receipt, native approval, release authorization or permission to bypass Owner/Watchdog Hold. Production and existing security gates remain unchanged. Documentation-only candidate; command execution and independent QA are NOT_RUN in this edit.

### Exact next local QA / ขั้นตอนทดสอบต่อ
1. Open the authorized checkout root containing `settings.gradle.kts`, `build.gradle.kts`, and `app/build.gradle.kts`; verify the source version/application ID above.
2. In an existing compatible JDK 17/Gradle environment, check `java -version` and `gradle --version` first. If Gradle or compatible Android SDK is absent, stop and record the missing prerequisite; do not install automatically.
3. Only in approved isolated build scope, run `gradle :app:testDebugUnitTest :app:assembleDebug`; record exit status and build output. This command is a proposed Android Gradle task sequence, **NOT_RUN**, not a verified execution. Never use release signing or upload to validate this guide.
4. On an authorized test device, verify the exact debug candidate and user-confirmed network/data allowance, then run GO/STOP and error/cancellation checks from the existing guide. Missing/stale measurements must not be presented as real. No live test or device install was performed in this documentation update.
5. หากล้มเหลว ให้เก็บข้อผิดพลาดแรกและเวอร์ชันเครื่องมือ แยกสาเหตุ dependency/SDK ออกจาก source error; ห้ามใช้คำสั่งลบ cache หรือเปลี่ยนระบบจริงโดยอัตโนมัติ. Rollback: abandon the isolated candidate and retain the unchanged base; do not revert another user's work.
