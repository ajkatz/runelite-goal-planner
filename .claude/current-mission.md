# Mission: Incremental persistence for 10K+ goal scalability
Date: 2026-04-12
Status: active

## Goal
Replace the monolithic JSON blob persistence with per-goal incremental
storage. Each goal gets its own ConfigManager key so mutations are O(1)
instead of serializing the entire list. Target: seamless performance
at 10,000+ completed goals.

## Sub-goals
- [ ] S1 — Design the per-goal key schema
- [ ] S2 — Implement incremental save (add/update/remove individual goals)
- [ ] S3 — Implement incremental load (reconstruct from per-goal keys)
- [ ] S4 — Migration path from monolithic JSON to per-goal keys
- [ ] S5 — Same treatment for tags and sections
- [ ] S6 — Performance testing with large goal counts

## Predictions
- Completion: S1-S4 confident, S5-S6 depends on time
- Confidence: Medium — ConfigManager API constraints unknown
- Risks: ConfigManager may have key count limits or sync issues
- Estimated cost: N/A

## Tasks Log
