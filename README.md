# OSRS Goal Planner — RuneLite Plugin

[![Discord](https://img.shields.io/discord/1494572077448040588?label=discord&logo=discord&logoColor=white&color=5865F2)](https://discord.gg/CFQsA3fmh7)
[![License](https://img.shields.io/github/license/ajkatz/runelite-goal-planner)](LICENSE)
[![Release](https://img.shields.io/github/v/release/ajkatz/runelite-goal-planner)](https://github.com/ajkatz/runelite-goal-planner/releases)

> ⚠️ **Experimental v0.1.1** — Early public cut. Persistence
> format and the public Java API may change in breaking ways before a
> stable 1.0 tag. If you track goals with this today, expect to
> re-create them if you upgrade across a breaking change. See
> [CHANGELOG.md](CHANGELOG.md) for what's in this release.

A RuneLite sidebar plugin that tracks Old School RuneScape goals and
grinds. Cards auto-update from game state, support sections + colors +
tags + multi-select bulk actions + undo/redo, and expose a public Java
API so other plugins can read and create goals programmatically.

<p align="center">
  <img src="assets/sidebar-overview.png" alt="Goal Planner sidebar showing Incomplete and Completed sections with skill, quest, and league-points goals" width="280">
</p>

## Goal types

| Type                    | Auto-tracked | Notes |
|-------------------------|--------------|-------|
| **Skill**               | Yes (XP)     | Target by Level (1–99) or raw XP up to 200M. Same-skill goals auto-order lower-target above higher-target within their section. |
| **Quest**               | Yes          | Binary: complete when `Quest.getState(client) == FINISHED`. Adding a quest can seed its full prereq chain (skills, prior quests, recommended combat). |
| **Achievement Diary**   | Yes (varbits) | One goal per (area, tier). All 11 areas with verified requirements. Karamja Easy/Med/Hard lack named varbits and stay manual. |
| **Combat Achievement**  | Yes (varplayers) | Bit-packed across 20 `CA_TASK_COMPLETED` varplayers. 640 task slots covered. Wiki data + tier sword icons. |
| **Boss Kill Count**     | Yes (varps)  | 89 bosses/activities incl. GWD, slayer, wilderness, DT2 (+ awakened), raids, Perilous Moons, Fortis Colosseum, Doom of Mokhaiotl (per-level), Brutus, Gauntlet, Barrows, etc. Prereqs auto-seed with transitive quest chaining. |
| **Item / Resource Grind** | Yes        | Counts inventory + bank. Manually markable when you want to call it done. |
| **Account Metric**      | Yes          | Quest Points, Combat Level, Total Level, CA Points, Slayer Points, Museum Kudos, Combined Att+Str, Misc Approval, Tears of Guthix PB, Chompy Kills, Colosseum Glory, DoM Deepest Level, League Points, League Tasks. |
| **Custom**              | Manual       | Free-text. User-set name, description, color, tags. |

## Features

- **Sections are independent buckets** — built-in **Incomplete + Completed** are the *default* home (a goal with no user section auto-sorts between them on completion), plus user-defined sections in the middle band. A user section keeps its own goals. By default, completing one **graduates out to the Completed list** — the global **Auto-archive completed** setting (on by default, in config). Turn it off to keep completed goals **inline as a checklist** (sinking to the bottom of their section as a ✓). Each section can also **override** the global default: right-click the header → **Completed goals** → *Use default / Auto-archive / Keep inline*. Sections can be renamed, recolored, reordered, and right-clicked for bulk operations.
- **Add to Section** — quests, diaries, and CA tasks are only addable from the in-game right-click; use **Add to Section** there to drop one (even an already-completed one) straight into a user section, instead of it auto-routing to the default Incomplete/Completed. Shared sets you import land in their own user section, so you instantly see what you've already done versus what's left as an inline checklist.
- **Move or Duplicate across sections** — right-click a goal (or a multi-selection) → **Move to Section** or **Duplicate to Section**. Identity is **per-section**: the same goal can live once in each section. Duplicating makes an independent copy (relations among a duplicated selection are preserved); you can't move/duplicate a goal into a section that already holds it; and adding a goal in-game that already sits in a user section creates a fresh default-tracked instance rather than reusing it.
- **Dependency nesting ("guide" view)** — turn on **Indent dependencies by default** (Appearance config) to render sections as an outline: each goal indents under the prerequisite it requires, with a thin file-tree guide. A goal with several prerequisites nests under its deepest one and lists the rest on hover. Override it per section via the header right-click → **Dependency nesting** → *Use default / Always nested / Never nested*. In a nested section, completed goals stay inline and **sink to the bottom as cards** (un-completing returns one to its place in the tree) rather than graduating to the Completed list — so a guide keeps its finished steps visible.
- **Colors** — every section, goal, and tag has a default color and an optional user override. Curated 12-swatch palette + JColorChooser escape hatch. Section header backgrounds are darkened to keep light text readable.
- **Multi-select + bulk actions** — click to select, cmd/ctrl-click to multi-select. Right-click a multi-selection for bulk Move to Section, Add Tag, Change Color, Mark Complete (CUSTOM only), Remove. Selection state is ephemeral.
- **Undo / Redo** — Ctrl-Z / Ctrl-Shift-Z (or Cmd on macOS) reverses every user mutation: adds, removes, edits, reorders, bulk actions, section changes, color + tag edits.
- **Right-click menus** — goal/section context menus built lazily on each show. Tags can be recolored, moved, hidden; goals can be marked complete/incomplete, removed, moved to sections; sections restore their default tags.
- **In-game integration** — right-click any skill in the Stats tab → Add Goal → enter Level/XP. Right-click any quest, diary row, CA task, boss/activity entry in the collection log, or inventory/bank/CA item → Add Goal as well. For quests and diary rows the **Add Goal** menu drills into your sections (quest: *Add Goal ▸ Default / &lt;section&gt;*; diary: *Add Goal ▸ &lt;tier&gt; ▸ Default / &lt;section&gt;*) so you can drop the goal straight into a section.
- **Prereq seeding** — adding a quest/boss goal that has its own requirements seeds the whole AND-linked prereq tree (skills, child quests, item requirements, account metrics, boss-kill prereqs, and OR-alternatives where defined). Diary goals add **bare** in-game (no auto-seed) — seed them after the fact. You can seed requirements onto an **existing** quest **or diary** goal at any time: right-click it → **Add requirements to this section** → *Incomplete only* (just what you still need) or *All* (the whole tree, kept inline as cards so met requirements show as ✓ rather than archiving away). Seeded prereqs land in the goal's own section, and a prereq shared across paths is reused rather than duplicated.
- **Public API** — other RuneLite plugins can declare `@PluginDependency(GoalPlannerPlugin.class)` and inject `GoalPlannerApi` to read goals + sections + tags and create new ones. See [API.md](API.md).
- **Local persistence** — every goal, section, color, and tag round-trips through `ConfigManager`. Survives client restarts. Schema migrations for built-in section ordering and boss-goal section reconciliation.
- **Readable fonts** — the panel font is configurable: a family picker (Default / Sans-serif / Serif) and a size scale (Small → Larger) under the *Appearance* config section, applied live across the whole panel for readability on large or high-DPI displays. An **Indent dependencies by default** toggle turns on the nested dependency "guide" view described above (overridable per section).

## Install (development)

```bash
export JAVA_HOME=/path/to/jdk-21
./gradlew run
```

Requires JDK 21 (Zulu recommended on macOS for FlatLaf compatibility).
The `run` task launches RuneLite in developer mode with the plugin
loaded. Public plugin-hub install flow will follow in a future release.

## Architecture

See [ARCHITECTURE.md](ARCHITECTURE.md) for the full module + data flow
walkthrough.

Quick map:

```
src/main/java/com/goalplanner/
├── GoalPlannerPlugin.java        # Plugin lifecycle, event handlers, MenuEntry injection
├── GoalPlannerConfig.java        # RuneLite plugin settings
├── api/                          # Public + internal API impl (GoalPlannerApi, DTOs, services)
├── command/                      # Undo/redo command pattern (Command, CompositeCommand, CommandHistory)
├── data/                         # Quest / diary / CA / boss requirement tables + resolvers
├── model/                        # Persisted entities (Goal, Section, ItemTag, enums)
├── persistence/
│   └── GoalStore.java            # ConfigManager-backed JSON + migrations + reconcile
├── service/
│   └── GoalReorderingService.java  # Skill-chain + section-aware ordering rules
├── tracker/                      # 8 trackers: Skill / Quest / Diary / CA / Item / Boss / Account / base
├── ui/                           # 14 Swing components: panel, cards, dialogs, pickers, icons
└── util/                         # Formatting helpers
```

## Cross-plugin API

Other plugins in the same client can hand Goal Planner a share bundle to import
by posting a RuneLite `PluginMessage` — no dependency on Goal Planner required
(the same convention shortest-path / quest-helper use to interop):

| field | value |
|---|---|
| **namespace** | `goalplanner` |
| **name** | `import-share` |
| **data** | `{ "code": "<a Goal Planner share code, e.g. GPSHARE1:…>" }` |

```java
eventBus.post(new PluginMessage(
    "goalplanner", "import-share",
    java.util.Map.of("code", shareCode)));
```

Goal Planner decodes the code and imports the goals into their own new user
section. Because user sections keep their completed goals inline, you immediately
see the shared set as a checklist against your own account — requirements you
already meet show ticked off. Get a share code from the "Copy share code"
right-click menu, or build one with the `ShareCodec` / `ShareBundle` classes in
`com.goalplanner.share`.

Two wire versions exist. `GPSHARE1:` carries one section (or loose goals) and is
what single-section shares still emit, so any plugin build imports them.
`GPSHARE2:` carries **multiple sections in one code** — each imports as its own
section, in one undo — and a section can be marked `targetDefault`, landing its
goals in your **Default plan** instead: existing equivalent goals are reused
(the same dedup as in-game adds), so re-importing never duplicates. Dependency
links **between** sections travel on a bundle-level cross-edge list (per-goal
relation refs stay section-scoped) and are rewired on import. "Copy share code
(all sections)" in the section right-click menu exports every user section as
one `GPSHARE2:` code; a multi-select spanning sections also exports per-section.

## Testing

```bash
./gradlew test
```

399 tests covering the API impl, persistence + migrations, all 8
trackers, the reordering service, requirement resolvers, OR-group
seeding, and integration flows that cover deep prereq chains. New code
that adds public API methods ships with tests in the same change. See
[TESTING.md](TESTING.md) for the fixture pattern, mock-vs-fake rules,
and the MockClient thread-affinity caveat.

## Documentation

- [CHANGELOG.md](CHANGELOG.md) — release notes
- [API.md](API.md) — public API reference for external plugin consumers
- [ARCHITECTURE.md](ARCHITECTURE.md) — module map, data flow, key invariants
- [ROADMAP.md](ROADMAP.md) — planned future work
- [CONTRIBUTING.md](CONTRIBUTING.md) — commit style, test-first rule, known pitfalls
- [TESTING.md](TESTING.md) — test fixtures, mock-vs-fake rules

## License

BSD 2-Clause. See [LICENSE](LICENSE).
