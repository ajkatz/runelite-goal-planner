# Mission: Boss kill count goals
Date: 2026-04-12
Status: active

## Goal
Add boss kill count tracking as a new goal type. Players set a target
kill count for any boss, and progress updates automatically from
RuneLite's VarPlayerID kill count values.

## Sub-goals
- [ ] S1 — BossKillData: boss name → VarPlayerID mapping
- [ ] S2 — GoalType.BOSS + Goal.bossName field
- [ ] S3 — BossKillTracker extends AbstractTracker
- [ ] S4 — addBossGoal API method
- [ ] S5 — Add Boss type to Add Goal dialog with boss selector + target

## Predictions
- Completion: All sub-goals achievable
- Confidence: High — pattern established by skill/quest/CA goals
- Risks: VarPlayerID coverage (some bosses may not have kill count vars)
- Estimated cost: N/A

## Tasks Log
