# Mission: Incremental persistence and performance
Date: 2026-04-12
Status: complete

## Goal
Per-entity persistence, performance optimization, diary requirements,
selection fixes, bulk quest actions.

## Tasks Log
- Per-entity persistence (goals + tags as individual ConfigManager keys)
- Schema migration v1 → v2
- Dirty tracking with compound save suspension
- Tracker-only save path (saveDirtyGoals)
- Lightweight selection refresh (no rebuild on click)
- Live selection state fix (stale wasSelected capture)
- Shift-click additive selection + deselection
- Debounced panel rebuild (200ms coalescing, EDT-safe)
- Performance timing logs
- GoalCard layout fix (tags below progress)
- Bulk "Add All Unfinished Quests" with requirements
- Skip completed goals in requirement seeding
- Leaf quest promotion after bulk actions
- Unreleased quest exclusion (The Red Reef)
- ConcurrentModificationException fix (tracker snapshot)
- LOGGED_IN game state guard
