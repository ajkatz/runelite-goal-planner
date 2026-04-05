# OSRS Goal Tracker — RuneLite Plugin

Track and manage Old School RuneScape goals with a visual drag-and-drop priority list.

## Goal Types

| Type | Color | Example |
|------|-------|---------|
| Skill | Green | 99 Mining, 50M total XP |
| Quest | Blue | Dragon Slayer II |
| Diary | Orange | Morytania Hard Diary |
| Collection Log | Purple | Unlock pet from Zulrah |
| Item/Resource Grind | Gold | 25,000 coal, 3 zenyte shards |
| Combat Achievement | Red | Kill Jad in under 25 min |
| Custom | Gray | Any free-text goal |

## Features

- **Drag-and-drop priority list** in the sidebar panel
- **Gradient progress cards** — the card IS the progress bar, filling with color as you progress
- **Auto-tracking** — progress updates live from game events
- **Integrations** — links to OSRS Wiki and Inventory Setups plugin
- **Compact overlay** — top goals shown in-game while playing
- **Local persistence** — goals survive client restarts

## Development

Built on the [RuneLite example plugin](https://github.com/runelite/example-plugin) template.

```bash
# Build
./gradlew build

# Run RuneLite with plugin loaded
./gradlew run
```

Requires JDK 11.

## Architecture

```
src/main/java/com/goaltracker/
├── GoalTrackerPlugin.java       # Main plugin lifecycle + events
├── GoalTrackerConfig.java       # Plugin settings
├── model/
│   ├── Goal.java                # Goal data class
│   ├── GoalType.java            # 7 goal types with colors
│   └── GoalStatus.java          # Active, complete, blocked, paused
├── tracker/
│   ├── GoalTracker.java         # Core progress checking logic
│   ├── SkillTracker.java        # Skill XP/level tracking
│   ├── QuestTracker.java        # Quest completion tracking
│   ├── DiaryTracker.java        # Achievement diary tracking
│   └── ItemTracker.java         # Item quantity tracking
├── ui/
│   ├── GoalPanel.java           # Sidebar panel with drag-and-drop
│   ├── GoalCard.java            # Gradient progress card
│   ├── GoalOverlay.java         # In-game compact overlay
│   └── GoalCreationDialog.java  # Goal creation/editing
├── integration/
│   ├── WikiLink.java            # OSRS Wiki URL generation
│   └── InventorySetupLink.java  # Inventory Setups plugin link
└── persistence/
    └── GoalStore.java           # JSON persistence
```
