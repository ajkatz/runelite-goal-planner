# Mission: Refine linked goal display and prereq ordering
Date: 2026-04-09
Status: complete

## Goal
Improve linked goal presentation: hide implicit skill-to-skill chain links
from the UI, make quest→skill requirement tooltips more compact, and fix BFS
ordering so skills are prioritized before quests when seeding prerequisites.

## Sub-goals
- [x] **S1 — Hide implicit skill-to-skill links from UI.** Added
  `isSkillChainEdge()` heuristic (same SKILL type + same skillName) to
  filter these edges from `toGoalView()` tooltip data.
- [x] **S2 — Compact quest→skill tooltip format.** Changed from
  "Crafting - Level 10" to "Crafting 10". Base64 data URI icons did not
  work in Swing's HTML renderer — fell back to compact text.
- [x] **S3 — BFS ordering: skills before quests.** Replaced level-order
  BFS with a two-queue priority system (`highPriority` for skills,
  `lowPriority` for quests). Skills always dequeue first regardless of
  discovery depth. Zero-dep quest promotion retained for correct ordering.

## Outcome
- All sub-goals landed. Build + tests pass.
- `GoalView.RelationView` inner class added to carry skill metadata
  through the view layer.
- `requiresNames`/`requiredByNames` changed from `List<String>` to
  `List<RelationView>`.

## Learnings
- Swing's HTML tooltip renderer does NOT support `data:` URI images.
  Future icon-in-tooltip work would need a custom `JToolTip` component.
- Zero-dep quest promotion is still needed even with skill-first BFS
  because leaf quests get pushed to the back of `lowPriority`.

## Tasks Log
- S1: filtered skill-chain edges in `toGoalView()` via `isSkillChainEdge()`
- S2: added `RelationView`, updated `buildTooltipHtml()` + `formatRelations()`
- S3: rewrote `seedPrereqsInto()` with `SeedEntry` + two-queue priority
- Attempted removing zero-dep quest promotion — reverted, still needed
