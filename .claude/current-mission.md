# Mission: Debug lag spike on skill XP gain
Date: 2026-04-14
Status: active

## Goal
Investigate and fix lag spike that occurs when gaining Strength XP
with a 20 Strength goal active. Likely an inefficiency in the tracker
or save path triggered by VarbitChanged/StatChanged events.

## Sub-goals
- [ ] S1 — Add timing logs to identify the bottleneck
- [ ] S2 — Fix the root cause

## Predictions
- Completion: Both sub-goals achievable
- Confidence: High — similar performance issues fixed before (debounced rebuilds, batched saves)
- Risks: Could be a new code path introduced during diary/OR-edge work
- Estimated tokens: ~40K-80K (medium)
- Estimated cost: included in subscription

## Tasks Log
