# MASTER APP BUILD RULES — applied to Test speed by AnakinYoo

## Permanent engineering rules
- Real Data: displayed speed/latency/jitter values must originate from actual measurement.
- Unknown > Fake: missing information renders `--` / error; never guessed.
- No Random: production source must not generate live-looking network metrics from random values.
- No misleading hardcode: constants may configure real test behavior, but must not be presented as measured facts.
- HTTP truth gate: a phase must receive HTTP 2xx before its result can be accepted.
- Byte truth gate: download/upload results are accepted only when expected payload bytes were actually transferred.
- Raw byte counting: request identity encoding to avoid transparent compression distorting byte accounting.
- Metadata truth: IP/ISP/edge/client-area fields only come from the provider response; unavailable fields remain unknown.
- Label truth: edge/server metadata and client-area metadata are separate concepts.
- No fake outage / packet loss / coordinates / server city.
- Ads/Billing: absent until real AdMob, Play Billing, consent, and product IDs are implemented.
- Prototype != Production: placeholder/simulated behavior may not be described as production capability.

## Anti-random app build rules
- Scope is locked in `APP_SCOPE_LOCK.md`.
- Requirement-to-code acceptance criteria are tracked in `PRD_TRACEABILITY.md`.
- New features require a source, acceptance criteria, and data/privacy impact.
- Requirement conflicts must be flagged; do not silently guess.
- A “production ready” claim is blocked until release signing, privacy/data safety, AAB, real-device testing, and Play testing are actually complete.

## Thesis / research integrity rules
- `RESEARCH_TO_APP_OS.md` distinguishes product requirements from empirical research.
- No fabricated paper, DOI, sample, survey, interview, IOC, reliability, statistics, or findings.
- Missing research evidence must remain `Gap`, `Assumption`, or `To verify`.
- Thesis consistency gate: title ↔ RQ ↔ objectives ↔ method ↔ instrument ↔ data ↔ analysis ↔ result ↔ conclusion ↔ app feature.
