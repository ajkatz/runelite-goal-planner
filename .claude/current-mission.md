# Mission: Fix goal section assignment not persisting across restarts
Date: 2026-04-14
Status: complete

## Goal
Goals moved to user-defined sections (e.g. "Active Goals") revert to
"Incomplete" on restart. The section assignment isn't persisting or
is being overwritten during load/reconciliation.

## Sub-goals
- [ ] S1 — Identify where sectionId is lost (save, load, or reconcile)
- [ ] S2 — Fix the root cause

## Predictions
- Completion: Both sub-goals achievable
- Confidence: High — likely a reconcile or load ordering issue
- Risks: Could be a subtle interaction between reconcileCompletedSection and section assignment
- Estimated tokens: ~30K-60K (small)
- Estimated cost: included in subscription

## Tasks Log
