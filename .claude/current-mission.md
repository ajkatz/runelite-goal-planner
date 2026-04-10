# Mission: Break up GoalPanel god class
Date: 2026-04-09
Status: active

## Goal
Extract GoalPanel (2,242 lines) into focused UI components:
GoalContextMenuBuilder (~700 lines), GoalDialogFactory (~800 lines),
GoalReorderController (~170 lines). Then remove direct GoalStore access.

## Sub-goals
- [ ] S1 — Extract GoalReorderController (smallest, validates the pattern)
- [ ] S2 — Extract GoalDialogFactory (biggest chunk)
- [ ] S3 — Extract GoalContextMenuBuilder (most dependencies on panel)
- [ ] S4 — Remove GoalPanel's direct GoalStore access

## Predictions
- Completion: S1-S3 confident, S4 depends on how clean the extractions are
- Confidence: Medium — Swing wiring and callback threading
- Risks: Context menus need panel reference for popup positioning
- Estimated cost: N/A (local dev)

## Tasks Log
