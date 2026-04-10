# Mission: Fill in remaining quest requirements
Date: 2026-04-09
Status: active

## Goal
Populate requirements (skills, prereq quests, QP, combat level, kudos) for
all quests in the RuneLite Quest enum. Wiki-source all data.

## Sub-goals
- [x] S1 — Fill in 3 DT2 stubs (Secrets of the North, Garden of Death, His Faithful Servants)
- [x] S2 — Add all 91 missing quests from the RuneLite Quest enum
- [x] S3 — Add QP/XP/lamp reward data for all new entries
- [x] S4 — Update tests and build

## Predictions
- Completion: All 209 quests covered
- Confidence: High
- Risks: Sailing skill not in RuneLite API (omitted from requirements)
- Estimated cost: N/A (local dev)

## Tasks Log
- Filled 3 DT2 stubs + 9 transitive deps (SotN chain, Making Friends, Swan Song, etc.)
- Added 91 new quest entries covering E-Z + Varlamore + Sailing + RFD chain
- All 209 RuneLite Quest enum values now have TABLE entries
- Added QP_REWARDS, XP_REWARDS, LAMP_REWARD entries for all new quests
- Fixed null-for-missing-quest tests (RATCATCHERS now in table)
- Sailing skill requirements omitted (not in RuneLite API yet)
