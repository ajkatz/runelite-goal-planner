# Mission: Achievement diary requirements system
Date: 2026-04-11
Status: complete

## Goal
Build diary requirements system parallel to quest requirements.

## Tasks Log
- DiaryRequirements.java: Ardougne Easy/Medium/Hard/Elite data
- DiaryRequirementResolver.java: resolves against live player state
- addDiaryGoalWithPrereqs API + right-click menu wiring
- Unlock milestones (Fairy Rings = Fairytale I + Lost City, Dramen staff icon)
- Recursive quest prereq seeding within diary trees
- Higher tiers stripped of lower-tier requirements
- Fixed: diary menu "Open" filter, tag label 30-char crash, ConcurrentModificationException, client thread assertion, LOGGED_IN guard, nested compound UI refresh
- Unified ascending skill sort across all seeders
