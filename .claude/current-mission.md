# Mission: Quest requirement associations (PoC vertical slice)
Date: 2026-04-08
Status: complete

## Goal
Build the first cut of pre-defined quest→requirement associations so that
adding a quest goal can optionally auto-seed its skill (and quest)
prerequisites. Ship a proof-of-concept handful of quests end-to-end rather
than full coverage — prove the data shape, infra, and UX before bulk data
entry.

## Sub-goals
- [x] **S1 — Data model + PoC data table.** Define how an association is
  represented (quest id → list of skill reqs + quest reqs), decide where
  it lives (static class, resource file, enum map), and populate for
  ~5–10 quests spanning simple (one skill req) to complex (multi-skill +
  quest chain, e.g. Dragon Slayer II, Monkey Madness II).
- [x] **S2 — Atomic multi-create infra.** (mostly pre-existed; `findOrCreateRequirement` + `beginCompound`/`endCompound` already covered this)
- [x] **S3 — Auto-create hook on quest add.**
- [x] **S4 — Tests.**
- [x] **S4.5 — Quest point prereqs stubbed.**
- [x] **S6 — Quest-tag associations.** (added in-mission at user request)

**Superseded (please ignore below):**
- [ ] ~~**S2 — Atomic multi-create infra.**~~ Per the roadmap prerequisite,
  extend `beginCompound`/`endCompound` coverage so a single user action
  can create N goals + N requirement edges and land as one undo entry.
  Verify existing callers still compose cleanly.
- [ ] **S3 — Auto-create hook on quest add.** When a user adds a quest
  goal that has a known association, offer to seed the prerequisite
  goals (skill goals at the required level, quest goals for prereq
  quests) and wire them as `requires` edges. Respect `autoSeeded=true`
  flag per the roadmap's absorption rule (future work).
- [ ] **S4 — Tests.** API-level tests for the new data lookup, compound
  undo test covering the multi-create path, and an integration test
  exercising the full "add DS2, get prereq tree" flow.
- [ ] **S4.5 — Quest point prereqs stubbed.** Some quests require a
  minimum total quest point count (e.g. RFD subquests, Lunar Diplomacy).
  Quest-point goals aren't a supported type yet (roadmap "Account-wide
  goals"). For now, the data table should record QP requirements, but
  the auto-create flow skips them and leaves a TODO — either an inline
  code comment, a log line, or a visible "unsupported requirement"
  marker — so they're easy to find when the QP goal type ships.
- [ ] **S5 — UX:** confirm the surface for the auto-create prompt
  (checkbox in add-quest dialog? follow-up toast? modal?). Likely a
  decision point once S1–S3 are wired.

## Predictions
- **Completion:** S1 and S2 land cleanly this session. S3 gets a working
  prototype. S4 partial (unit tests done, integration may spill). S5
  likely to surface a design decision mid-session that gets logged and
  deferred.
- **Confidence:**
  - S1: High — pure data, clear shape
  - S2: Medium — compound infra exists but multi-create path may expose
    edge cases (ordering, rollback on failure)
  - S3: Medium — auto-create UX needs thought; data → goal translation
    needs to respect existing goal dedupe
  - S4: High — test patterns well-established
  - S5: Low — design call, depends on user input
- **Risks:**
  1. Quest id representation — does the plugin already have a canonical
     quest identifier (RuneLite `Quest` enum?) or does it roll its own?
     If the latter, the data table needs a stable key.
  2. Dedupe collision: if the user already has a "35 Agility" goal,
     auto-seeding must attach to the existing one, not create a
     duplicate. This touches the same goal-identity logic that the
     absorption rule is meant to handle.
  3. Atomic rollback: if goal N of N fails to create, the compound
     needs to undo cleanly. Current `beginCompound`/`endCompound` docs
     don't mention failure semantics — may need to audit.
- **Estimated cost:** N/A (local dev, no paid API calls).

## Tasks Log
(updated by /task and /task-done)
