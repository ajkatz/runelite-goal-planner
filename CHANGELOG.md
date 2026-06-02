# Changelog

All notable changes to the OSRS Goal Planner plugin will be documented
here. Format based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/);
versioning is [semver](https://semver.org/) with the caveat that the
0.x series is experimental and may include breaking changes on minor
bumps.

## [0.2.0] — Unreleased

### Added
- **Configurable panel font (family + size).** An Appearance config
  section adds a font-family picker (Default / Sans-serif / Serif) and a
  size scale (Small / Normal / Large / Larger) for the side panel, routed
  through a central `PanelFonts` helper and applied live. Addresses a
  plugin-hub user's "font is a little difficult to read" report. The
  controls are marked experimental in-config with a Discord link for
  usability feedback.

### Fixed
- **Stale skill levels when seeding goal prerequisites.** Adding a goal
  that seeds skill prereqs (e.g. Song of the Elves seeding Herblore /
  Farming) right after login could read the not-yet-synced skill cache,
  which reports every skill at its default level (1). Already-met skills
  then failed the skip check and seeded as spurious level-1 cards that
  only self-corrected once the game pushed stat updates. Such adds are
  now deferred through a new `SkillSyncGate` until the post-login stat
  burst has arrived and settled (with a wall-clock fallback so an add is
  never stranded), so prerequisites are filtered against accurate levels.
  Covers quest, achievement-diary, and boss seeding.

## [0.1.1] — 2026-05-28

First post-submission release. Right-click menus restructured into a
column-view drill-down, bulk-action surface expanded to mirror the
single-item shape, several persistence and refresh bugs fixed.

### Added
- **ColumnMenu** — column-view popup menu for bulk and single right-click,
  single-column drill-down with a back row. Keep-open flag for repeat-
  friendly actions. Clamped inside the RuneLite client window.
- **Bulk Customize > Relations** — bulk Requires… / Required by… /
  Remove Requirement / Remove Dependent against the multi-select.
- **Bulk Move** — Up / Down / Top / Bottom preserving relative order
  within the selection.
- **Bulk Mark Optional / Required** under Customize.
- **Move-to… pick mode** — click a section header or goal card as a
  drop target after a Move-to gesture; cancel with ESC.
- **Auto-select on add** — every `addXxxGoal` at the API layer leaves
  the newly created goal selected.
- **Auto-deselect rules** at the API layer covering pick-mode entry,
  arrow actions, and single-item right-click activations.
- **"Open in Quest Helper"** context menu item on achievement diary
  goals.
- **Goal customizations menu** grouped under a Customize submenu,
  Move actions promoted to a top-level sibling.
- **Tag submenu** that collapses Add Tag + Remove Tag together when
  both apply.
- **Discord community server.** README + issue-template config link
  to the public invite.
- README badges (Discord member count, license, latest release).
- `.github/` issue templates (bug report, feature request, data
  correction) and PR template.
- Screenshots under `assets/` with the sidebar overview inlined in
  the README as a hero image.
- Karamja diary varbits — Easy / Medium / Hard now auto-track instead
  of remaining manual-complete.
- `.githooks/pre-commit` blocks `java.lang.reflect` usage with a
  `@SuppressReflection` escape hatch for tests that genuinely need it.

### Changed
- Repository renamed from `runelite-goal-tracker` to
  `runelite-goal-planner` on GitHub. Old URL still redirects; the
  plugin-hub manifest points at the canonical URL.
- Empty-state hints rewritten to describe the right-click flow
  (Incomplete header, in-game skill / quest / diary / item / collection
  log entries, custom-section creation). The `+` button is gone.
- Goal-card right-click menu trimmed and consolidated; bulk menu
  restructured to mirror the single-item shape.
- Completed section now sorts by completion date, newest first.
- Move-to-Top / Move-to-Bottom baked into the arrow buttons via
  right-click; FlatLaf base font + 13pt for ColumnMenu rows; ASCII
  back glyph for cross-platform consistency.
- Remove Tag / Remove Requirement / Remove Dependent migrated to the
  multi-select checkbox dialog instead of nested submenus.
- QuestHelper plugin integration dropped — there's no published
  cross-plugin compile-time interop API.

### Fixed
- Panel didn't refresh when logging directly into Leagues.
- Move-to no-op when the source goal sat above the target.
- Completion state was lost across profile switches and login
  windows; canonical tags are now re-seeded on profile switch.
- Placeholder Discord invite URL in the sidebar Options menu.
- Plugin-hub deprecated-API check: replaced `WidgetID` and the shared
  Gson reference; added `build=standard` to plugin properties.
- "Shadows of Custodia" fix and assorted completion-UI cleanup landed
  during plugin-hub prep.

### Internal
- Rebranded source tree from `goaltracker` → `goalplanner` package +
  class names; renamed plugin to "Goal Planner".
- Reflection usage in tests replaced with direct package-private
  access.
- `addXxxGoal` APIs merged at the API layer; dev-only bulk helpers
  gated behind a flag.

## [0.1.0] — 2026-04-16 (Experimental first release)

> ⚠️ Experimental. Persistence format and public API are not stable;
> expect to re-create goals across upgrades until 1.0. See
> [ROADMAP.md](ROADMAP.md) for what's planned before 1.0.

### Added — Goal types

- **Skill goals** — target by Level (1–99) or raw XP up to 200M. Auto-
  track via `onStatChanged`. Same-skill goals auto-order lower-target
  above higher-target within their section.
- **Quest goals** — binary complete-when-finished tracking via
  `Quest.getState(client)`. Adding a quest offers "Add Goal with
  Requirements" that seeds the full transitive prereq chain.
- **Achievement diary goals** — all 11 areas verified across
  Easy/Medium/Hard/Elite tiers. Requirement data covers skills,
  quests, unlocks, boss kills, item reqs, and account metrics with
  OR-group alternatives (e.g. Warriors Guild entry). Karamja
  Easy/Medium/Hard lack named varbits and remain manual-complete.
- **Combat achievement goals** — 640 task slots covered via
  bit-packed `CA_TASK_COMPLETED_0..19` varplayers. Wiki data cache
  supplies task names, tiers, and monster tags; tier sword icons
  for Easy → Grandmaster.
- **Boss kill goals** — 89 bosses and activities covered, including
  GWD (+ Nex Frozen Door chain), slayer bosses, wilderness bosses,
  DT2 base + awakened variants, raids (CoX/CM, ToB/HM/Story, ToA
  tiers), waves (Jad, Zuk, Sol Heredit), skilling bosses, Varlamore
  content (Amoxliatl, Hueycoatl, Yama, Perilous Moons, Doom of
  Mokhaiotl), Zemouregal's Fort (Brutus + Demonic), The Gauntlet
  pair, Shellbane Gryphon, Fortis Colosseum waves, Barrows. 62 of
  89 have declared prereqs that auto-seed on goal creation.
- **Item / resource grind goals** — counts inventory + bank +
  equipment + seed vault + group storage + Kingdom of Miscellania.
  Bank-null pre-visit guard prevents wiping persisted totals to
  inventory-only on login. Relative-target mode ("Reach X" vs
  "Gain X more") on the Add Goal dialog.
- **Account metric goals** — 14 metrics: Quest Points, Combat Level,
  Total Level, CA Points, Slayer Points, Museum Kudos, Combined
  Att+Str, Misc Approval, Tears of Guthix PB, Chompy Kills,
  Colosseum Glory, DoM Deepest Level, League Points, League Tasks.
- **Custom goals** — free-text name + description + manual
  complete/incomplete.

### Added — Prereq seeding

- Widened `BossPrereqs` schema to match `DiaryRequirements.Reqs`
  (items, account metrics, boss-kill prereqs, OR-alternatives).
- `seedPrereqsInto` priority-queue BFS engine powers prereq seeding
  for diaries, quests, and bosses — a single shared implementation
  with consistent card-ordering (optional → skill/item/account →
  quest) and transitive quest chaining.
- Transitive quest chaining on boss goal creation: adding e.g.
  Vorkath now seeds Dragon Slayer II AND DS2's own prereq chain
  (Bone Voyage, Legends' Quest, etc.).
- OR-group seeding via `addOrRequirement` — alternative paths
  (e.g. 99 Attack OR 99 Strength OR 130 Att+Str combined for
  Warriors Guild) are stored as OR-edges, parent auto-completes
  when ANY one OR-child is complete AND all AND-children are
  complete.
- Pre-filtering in resolvers for recommended combat level and
  recommended skills — already-met recommendations don't seed as
  pre-completed cards.

### Added — UI

- Sections: built-in Incomplete + Completed plus user-defined
  sections, each renamable, recolorable, reorderable.
- Colors: 12-swatch curated palette + JColorChooser escape hatch.
  Per-section, per-goal, per-tag overrides. Category-level tag
  colors for BOSS / RAID / CLUE / MINIGAME.
- Tags: first-class entities with labels, categories, optional
  colors, optional icons (skill icons or bundled PNG). System vs
  user tag distinction with appropriate edit guards.
- Multi-select + bulk actions: Move to Section, Add Tag, Change
  Color, Mark Complete (CUSTOM), Remove.
- Undo / Redo: Ctrl-Z / Ctrl-Shift-Z for every user mutation.
  50-entry history cap. Tracker-driven mutations bypass the stack
  (skill XP, quest tick, item count change).
- Right-click menus on skills, quests, diary rows, CA tasks, boss
  entries in collection log, inventory/bank/CA items — all offer
  Add Goal (with prereq seeding where applicable).
- Search + filter on the goal list.
- Plugin sidebar icon.
- Relative goal targets (Reach X / Gain X more) for SKILL and
  ITEM_GRIND goals.

### Added — Public API

See [API.md](API.md) for the full method catalog.

- Read: `queryAllGoals`, `queryAllSections`.
- Create: `addSkillGoal`, `addSkillGoalForLevel`, `addItemGoal`,
  `addQuestGoal`, `addDiaryGoal`, `addCombatAchievementGoal`,
  `addBossGoal`, `addAccountGoal`, `addCustomGoal`.
- Prereq-chain create: `addQuestGoalWithPrereqs`,
  `addDiaryGoalWithPrereqs`.
- Mutate: `changeTarget`, `editCustomGoal`, `markGoalComplete`,
  `markGoalIncomplete`, `addTag`, `removeTag`, `restoreDefaultTags`,
  `removeGoal`.
- DTOs: `GoalView`, `SectionView`, `TagView` with type-specific
  attribute maps for BOSS and ACCOUNT types.

### Added — Infrastructure

- Command-pattern undo/redo with in-memory stacks, bulk-action
  single-step semantics, and ID-stability on add/remove cycles.
- `GoalStore` persistence via `ConfigManager`: three JSON blobs
  (`goalplanner.goals` / `.sections` / `.tags`) with load-time
  migrations for built-in section ordering and orphaned sectionIds.
- Eight trackers sharing an `AbstractTracker` template.
- `MockClient` + `MockGameState` + `TrackerTestHarness` test
  fixtures. Synthetic-data test hook (`swapPrereqsForTest`) for
  features that land without production data exercising them.

### Fixed

- **Goal section assignment persistence across restarts** —
  user-defined section assignments were being reverted to
  Incomplete on load.
- **EDT-swallowing bug in collection-log right-click** — the
  "Add KC Goal" path wrapped `addBossGoal` in `SwingUtilities.invokeLater`,
  causing `Quest.getState(client)` and `getRealSkillLevel` (called
  via the resolver) to throw on the EDT. Exceptions were silently
  swallowed by the AWT uncaught handler — chain never seeded. Fixed
  by hopping back to `clientThread.invokeLater` before the API
  call.
- **Recommended combat level seeded as already-completed card** —
  the resolver emitted a recommended-combat template without
  checking the player's live combat level. Downstream, the ACCOUNT
  goal was created with `currentValue=0` (builder default) so the
  post-creation `isComplete` check missed; the next AccountTracker
  tick flipped it to complete. Fixed by pre-filtering in the
  resolver.
- **Lag spike when adding goals** — compound gesture processing
  was firing the panel rebuild callback per goal; batched to once
  per compound.
- **Diary requirement corrections** — 11 area verification pass
  corrected task requirements; added 3 new account metrics
  (CHOMPY_KILLS, TOG_MAX_TEARS, MISC_APPROVAL), Zulrah / Thermy
  prereqs, OR-linked wildy boss pairs.
- **Data corrections on user review** — Amoxliatl quest gate
  (Heart of Darkness), Yama gate (A Kingdom Divided), Nex Frozen
  Door (1 KC per GWD room, not 40), TzKal-Zuk prereq (Fight Caves
  kill, not Fire cape item), DKs (no hard prereq, multiple access
  paths), Abyssal Sire (Enter the Abyss miniquest), Nightmare
  (Priest in Peril, not Sins of the Father), Grotesque Guardians
  (Brittle key). Brutus/Demonic Brutus (Defender of Varrock +
  BossReq), DoM (The Final Dawn).

### Internal

- `seedPrereqsInto` refactor: boss prereq seeding now routes through
  the same priority-queue BFS as quest + diary paths, producing
  consistent card-ordering.
- Additive constructor pattern for `BossPrereqs` — existing entries
  compile unchanged when new fields are added.
- `IntSupplier combatLevelLookup` lookup added to
  `QuestRequirementResolver` core overload (with backwards-compat
  3-arg overload) enabling live-state pre-filtering.
- EDT / client-thread discipline documented; existing
  `SwingUtilities.invokeLater` call sites audited.

### Known limitations

- Persistence has no schema version field — breaking changes will
  not migrate cleanly. Users upgrading across a breaking change
  should expect to re-create goals.
- Public API does not yet expose `queryAllTags`, requirement
  resolvers, or relation helpers (`addRequirement` / `addOrRequirement`).
  These may be promoted to public in 1.0.
- Some pets have no ItemID constant in runelite-api 1.12.23 (Doom
  of Mokhaiotl's "Dom", moon boss pets) — those entries ship
  without an icon. Will be filled in on api bump.
- Corporeal Beast's Summer's End quest is not in
  `net.runelite.api.Quest`; Corp ships without a quest prereq.
- Alternative OR-groups are seeded via edges but the UI doesn't
  yet visually group OR-children separately from AND-children in
  the card list.

## Not released

See [ROADMAP.md](ROADMAP.md).
