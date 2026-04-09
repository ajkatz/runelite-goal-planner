# Mission: Account-wide goal types
Date: 2026-04-09
Status: complete

## Goal
Implement account-wide goal types (quest points, combat level, total level,
CA points, slayer points, museum kudos) with live tracking, UI creation,
and quest prereq seeder integration.

## Outcome
- New GoalType.ACCOUNT with AccountMetric subtypes
- AccountTracker reads live client state per metric
- addAccountGoal API with validation caps per metric
- Add Goal dialog: Account type with metric dropdown, CA tier shortcuts, Max button
- Right-click menu: character summary CA → per-tier entries, QP → prompt
- Quest prereq seeder produces QP/combat/kudos account goals
- CA goals show tier-appropriate sword sprites + tier label in name
- Skill levels always show >= 1
- Music tracks deferred (no reliable bulk API)

## Learnings
- MUSICMULTI varps are for playlists, not unlock tracking
- Music unlock state requires per-track script callbacks
- CA_POINTS varbit (14815) gives total points directly
- Character summary pane is group 712, child 3
- Total level box doesn't generate right-click menus
- Bone Voyage requires 100 kudos (added kudos field to Reqs)
