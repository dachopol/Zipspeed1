# APP SCOPE LOCK — Test speed by AnakinYoo

Status: LOCKED for v1.1.0 unless the owner explicitly changes the requirement.

## Problem
Users need a simple Android screen that reports current HTTP network performance without fabricated metrics.

## Target user
Android users who want a quick, understandable measurement of their current internet connection.

## Core use case
Open app → tap GO → measure metadata, HTTP latency, download, upload → show measured result or an explicit error/unknown state → STOP cancels the active test.

## Success criteria
- No random/fake live metrics.
- HTTP phases require 2xx responses.
- Throughput is derived from actual bytes and monotonic elapsed time.
- Expected payload length is verified before accepting download/upload results.
- Unknown metadata is shown as `--`.
- Re-running a test clears stale metadata and previous measurements first.
- Leaving the app cancels an active test to avoid hidden background data use.

## MoSCoW

### Must have
- GO / STOP.
- HTTP latency and jitter from measured samples.
- Single-stream HTTP download throughput.
- Single-stream HTTP upload throughput.
- IP / ISP when returned by the provider.
- Provider edge code and provider-reported client area kept as separate labels.
- Error and cancel states.
- Responsive scrollable Android UI.
- Dark/light appearance following system configuration.

### Should have — not yet production-validated
- Real-device tests on Wi-Fi and cellular.
- Device/network matrix validation.
- Accessibility review.
- Privacy/Data Safety final review based on real data flows.

### Could have — future requirement only
- Authorized multi-provider/server directory.
- History/export/share.
- Video suitability test.
- Endpoint status monitoring.
- AdMob and Play Billing after real product IDs, consent, and policy work exist.

### Won't have in v1.1.0
- Random demo speed values.
- Fake ping, packet loss, outage, server city, coordinates, or health status.
- Simulated ads or simulated paid entitlement presented as real.
- Claims that this single-stream HTTP test equals every ISP/lab benchmark methodology.

## Change control
Any new feature must identify its requirement source, acceptance criteria, data source, privacy impact, and whether it is prototype-only or production-capable.
