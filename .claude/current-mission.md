# Mission: A-D members quest library
Date: 2026-04-09
Status: complete

## Goal
Add all A-D members quests (and their transitive dependencies) to the
quest requirements library with full skill/quest links, XP reward tags,
lamp tags, and QP descriptions.

## Outcome
- ~40 new quest entries added (A-D members + all transitive deps)
- Backfilled QP/XP/lamp data for all existing members quests in table
- DT2 dependency stubs partially filled (Secrets of the North, etc. TBD)
- addQuestGoal/addQuestGoalWithPrereqs now deselect all before running
- Build + all tests pass

## Tasks Log
- S1: Added A-D quest requirements + Myreque/Kourend/Dorgeshuun/DT2 dep chains
- S2: Backfilled QP rewards for 39 existing quests, XP rewards for 30, lamp tags for 5
- S3: Added QP/XP/lamp data for all new A-D entries
- Deselect-all added to addQuestGoal and addQuestGoalWithPrereqs
