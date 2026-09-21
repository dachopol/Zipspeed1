# PRD TRACEABILITY — v1.1.0

This file is the source-of-truth bridge between requirements and implementation.

| ID | Requirement | Implementation | Acceptance criteria | Status |
|---|---|---|---|---|
| R1 | No fake/random live metrics | `SpeedTestEngine.kt`, `FakeDataGuardTest.kt` | No random-number API in production source | Implemented |
| R2 | Measure real throughput | `measureDownload`, `measureUpload`, `SpeedMath.mbps` | Mbps derives from actual bytes + monotonic elapsed time | Implemented |
| R3 | Reject incomplete measurement | `SpeedTestEngine.kt` | Download/upload byte count must equal requested payload | Implemented |
| R4 | Require valid HTTP response | `requireSuccessful` | Each measurement phase accepts only HTTP 2xx | Implemented |
| R5 | Unknown > fake | `SpeedTestView.kt` | Missing network metadata renders `--` | Implemented |
| R6 | Correct metadata labels | `setMetadata`, network panel | Edge code is not mixed with client city/country; client area is identified as provider metadata | Implemented |
| R7 | Prevent stale result display | `resetForStart` | Previous metrics/IP/ISP/edge/client area clear at new run | Implemented |
| R8 | GO/STOP and no overlapping test | `AtomicBoolean running`, `cancel` | Second start is rejected while active; STOP disconnects active request | Implemented |
| R9 | Avoid hidden background transfer | `MainActivity.onStop` | Active test cancels when activity stops | Implemented |
| R10 | Byte integrity over HTTP | `Accept-Encoding: identity`, no-cache headers | Download byte accounting is not based on transparent content compression | Implemented |
| R11 | Play target baseline | `targetSdk = 36` | Meets current mobile app target requirement verified 2026-09-21 | Configured, not submitted |
| R12 | Production release | signing/privacy/AAB/testing | Must pass real release pipeline before any “production ready” claim | Not complete |

## Rule
If code and this table disagree, the mismatch is a defect. Do not silently reinterpret the requirement.
