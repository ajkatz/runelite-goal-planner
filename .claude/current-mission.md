# Mission: Achievement diary requirements system
Date: 2026-04-11
Status: active

## Goal
Build diary requirements system (parallel to quest requirements):
DiaryRequirements data, resolver, addDiaryGoalWithPrereqs API,
and right-click "Add Goal with Requirements" menu entry.
Start with Ardougne, then expand to all areas.

## Sub-goals
- [ ] S1 — Create DiaryRequirements.java data file (Ardougne first)
- [ ] S2 — Create DiaryRequirementResolver.java
- [ ] S3 — Add addDiaryGoalWithPrereqs to GoalCreationService
- [ ] S4 — Wire "Add Goal with Requirements" menu entry for diaries
- [ ] S5 — Expand to remaining diary areas

## Predictions
- Completion: S1-S4 confident, S5 depends on time
- Confidence: High — pattern is proven from quest system
- Risks: Diary requirements are per-task, not per-tier — need to decide granularity
- Estimated cost: N/A

## Tasks Log
