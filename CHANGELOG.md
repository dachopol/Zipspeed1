# Changelog

## 1.1.0
- Strengthened start-state guard with atomic compare-and-set.
- Added raw-byte integrity header (`Accept-Encoding: identity`).
- Disabled request caching for measurement requests.
- Rejects incomplete latency/download/upload payloads instead of accepting partial data as a valid result.
- Clears previous metrics and metadata before a new run.
- Splits provider `EDGE` from provider-reported `CLIENT AREA` to avoid server-location ambiguity.
- Shows HTTP latency value in the gauge center during latency phase.
- Cancels an active test when the activity stops.
- Expanded anti-random / measurement-integrity tests.
- Added App Scope Lock, PRD traceability, Research to App OS, and platform verification documents.

## 1.0.0
- Initial real-data Android MVP.
