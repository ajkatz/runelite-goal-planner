# Mission: MockGameState test framework for tracker coverage
Date: 2026-04-13
Status: active

## Goal
Build a reusable test mocking framework that represents complete player
game state (varbits, varps, skills, quests, items) as a data object,
configures a mock Client from it, and enables copy-and-modify for
before/after test scenarios across all 7 tracker types.

## Sub-goals
- [ ] S1 — MockGameState.java: state snapshot with fluent API + copy()
- [ ] S2 — MockClientFactory.java: state → configured Client mock
- [ ] S3 — TrackerTestHarness.java: full GoalStore+API+tracker wiring
- [ ] S4 — BossKillTrackerTest.java: example test validating framework

## Predictions
- Completion: All sub-goals achievable in one session
- Confidence: High — patterns established by InMemoryConfigManager + ItemTrackerTest
- Risks: Quest.getState() mocking via runScript/getIntStack may need debugging
- Estimated cost: N/A

## Tasks Log
