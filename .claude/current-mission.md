# Mission: Codebase-wide tech debt cleanup and architecture improvements
Date: 2026-04-09
Status: incomplete

## Goal
Deep architectural cleanup: delete dead code, extract abstractions to
eliminate duplication, break up god classes, add index maps for
performance, clean up code quality issues. Make this well-engineered.

## Sub-goals
- [ ] S1 — Delete dead stubs (GoalCreationDialog, GoalOverlay, WikiLink, InventorySetupLink)
- [ ] S2 — Extract tracker boilerplate into abstract base class
- [ ] S3 — Break up GoalTrackerApiImpl (3,301 lines) into focused services
- [ ] S4 — Break up GoalPanel (2,242 lines) into focused components
- [ ] S5 — Add index map to replace linear scans in GoalPanel
- [ ] S6 — Clean up mission comments, wildcard imports, magic numbers

## Predictions
- Completion: S1+S2+S5+S6 confident. S3+S4 ambitious — may carry over.
- Confidence: Medium overall (large refactors touch many call sites)
- Risks: Swing wiring in GoalPanel, call site updates for ApiImpl split
- Estimated cost: N/A (local dev)

## Tasks Log
- Phase 1: Deleted 5 dead stubs (GoalCreationDialog, GoalOverlay, WikiLink, InventorySetupLink, GoalTracker)
- Phase 2: Created AbstractTracker base class, refactored 6 trackers to extend it
- Phase 3: Replaced 15s polling loop with event-driven VarbitChanged/StatChanged handlers
- Phase 7: Added O(1) index maps (goalIndex, sectionIndex, tagIndex) to GoalStore
- Phase 8: Stripping mission comments (137 occurrences), expanded wildcard imports
- Phases 4-6 (ApiImpl/GoalPanel/GoalStore split): carry-over for focused session
