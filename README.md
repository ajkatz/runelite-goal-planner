# OSRS Goal Tracker — RuneLite Plugin

A RuneLite sidebar plugin that tracks Old School RuneScape goals and grinds.
Cards auto-update from game state, support sections + colors + tags + multi-
select bulk actions, and expose a public Java API so other plugins can read
and create goals programmatically.

## Goal types

| Type                | Auto-tracked | Notes |
|---------------------|--------------|-------|
| **Skill**           | Yes (XP)     | Target by Level (1-99) or raw XP up to 200M. Same-skill goals auto-order lower-target above higher-target within their section. |
| **Quest**           | Yes          | Binary: complete when `Quest.getState(client) == FINISHED`. |
| **Achievement Diary** | Yes (varbits) | One goal per (area, tier). Karamja Easy/Med/Hard lack named varbits and stay manual. |
| **Combat Achievement** | Yes (varplayers) | Bit-packed across 20 `CA_TASK_COMPLETED` varplayers. 640 task slots covered. Wiki data + tier sword icons. |
| **Item / Resource Grind** | Yes | Counts inventory + bank. Manually markable when you want to call it done. |
| **Custom**          | Manual       | Free-text. User-set name, description, color, tags. |

## Features

- **Sections** — built-in Incomplete + Completed plus user-defined sections in the middle band. Each section can be renamed, recolored, reordered, and right-clicked for bulk operations.
- **Colors** — every section, goal, and tag has a default color and an optional user override. Curated 12-swatch palette + JColorChooser escape hatch. Section header backgrounds are darkened by 0.55× to keep light text readable.
- **Multi-select + bulk actions** — click to select, cmd/ctrl-click for multi-select. Right-click a multi-selection for bulk Move to Section, Add Tag, Change Color, Mark as Complete (CUSTOM only), Remove. Selection state is ephemeral (lost on restart).
- **Right-click goal/section operations** — Add Goal, Change Color, Recolor Tag, Move to Section, Mark Complete/Incomplete, Remove, Restore Default Tags, etc. The right-click menu is built lazily on each show, so it always reflects current state.
- **In-game integration** — right-click any skill in the Stats tab → Add Goal → enter Level/XP. Right-click any quest, diary row, CA task, or inventory/bank/collection-log item → Add Goal as well.
- **Public API** — other RuneLite plugins can declare `@PluginDependency(GoalTrackerPlugin.class)` and inject `GoalTrackerApi` to read goals + sections and create new ones. See [API.md](API.md).
- **Local persistence** — every goal, section, color, and tag round-trips through `ConfigManager`. Survives client restarts. Built-in section order auto-migrates if the constants change.

## Install (development)

```bash
export JAVA_HOME=/path/to/jdk-21
./gradlew run
```

Requires JDK 21 (Zulu recommended on macOS for FlatLaf compatibility). The
`run` task launches RuneLite in developer mode with the plugin loaded.

## Architecture

See [ARCHITECTURE.md](ARCHITECTURE.md) for the full module + data flow walkthrough.

Quick map:

```
src/main/java/com/goaltracker/
├── GoalTrackerPlugin.java       # Plugin lifecycle, event handlers, MenuEntry injection
├── GoalTrackerConfig.java       # RuneLite plugin settings
├── api/
│   ├── GoalTrackerApi.java          # Public API (bound via Plugin.configure)
│   ├── GoalTrackerInternalApi.java  # Plugin-private API (NOT bound)
│   ├── GoalTrackerApiImpl.java      # Single impl class for both interfaces
│   ├── GoalView.java                # Public DTO for goals
│   ├── SectionView.java             # Public DTO for sections
│   └── TagView.java                 # Public DTO for tags
├── model/                       # Persisted entities (Goal, Section, ItemTag, GoalType, GoalStatus, TagCategory)
├── persistence/
│   └── GoalStore.java           # ConfigManager-backed JSON persistence + section CRUD + reconcile
├── service/
│   └── GoalReorderingService.java   # Skill-chain ordering rules (section-aware)
├── tracker/                     # 5 trackers: Skill / Quest / Diary / CombatAchievement / Item
├── data/                        # Wiki CA repo, tag options, source attributes
└── ui/                          # GoalPanel, GoalCard, SectionHeaderRow, ColorPickerField, SkillTargetForm, ShapeIcons, ...
```

## Testing

```bash
./gradlew test
```

92 tests covering the API impl, persistence layer, ItemTracker, the
reordering service, and the model. New code that adds API methods is
expected to ship with tests in the same change. See [TESTING.md](TESTING.md)
(WIP) for the fixture pattern and mock-vs-fake rules.

## Documentation

- [API.md](API.md) — public API reference for external plugin consumers
- [ARCHITECTURE.md](ARCHITECTURE.md) — module map, data flow, key invariants
- [TESTING.md](TESTING.md) — test fixtures, mock-vs-fake rules (WIP)
- [CONTRIBUTING.md](CONTRIBUTING.md) — mission process, commit style, test-first rule (WIP)

## License

See LICENSE.
