# Mission: F2P quest library + F2P tag
Date: 2026-04-09
Status: complete

## Goal
Add all F2P quests to the quest requirements library with full
skill/quest prerequisite links, and enrich quest goal cards with
F2P tags, XP reward skill icons, lamp icons, and quest point descriptions.

## Sub-goals
- [x] **S1 — Add all F2P quest requirements.** 17 new entries in
  `QuestRequirements.java` (23 total F2P quests including The Ides of Milk).
- [x] **S2 — F2P tag.** Gray "F2P" pill (OTHER category) auto-tagged on
  F2P quest goal creation.
- [x] **S3 — XP reward skill tags.** SKILLING icon tags for 11 F2P quests
  with fixed XP rewards. Choice lamps excluded from skill tags.
- [x] **S4 — Lamp reward tag.** Bundled `lamp.png` icon, "Lamp" tag
  (OTHER category) auto-tagged on quests that reward XP lamps.
- [x] **S5 — Quest point descriptions.** Quest goal description changed
  from "Quest" to "N Quest Point(s)" using QP reward data.
- [x] **S6 — Tests.** F2P identification tests, updated stale test fixtures.

## Outcome
- Build + all tests pass.
- New data tables: `F2P_QUESTS`, `XP_REWARDS`, `LAMP_REWARD_QUESTS`, `QP_REWARDS`
- New queries: `isF2P()`, `xpRewards()`, `rewardsLamp()`, `questPointReward()`
- Bundled asset: `src/main/resources/icons/lamp.png`

## Learnings
- `item:` prefix icon keys require the game client cache — bundled PNGs
  are more reliable for custom icons.
- Swing's HTML renderer doesn't support data URIs (carried from prior mission).
- RuneLite Quest enum uses `VAMPYRE_SLAYER` (with Y) and `ROMEO__JULIET`
  (double underscore).
