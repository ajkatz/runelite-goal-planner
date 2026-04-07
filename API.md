# Goal Tracker Plugin API

The Goal Tracker plugin exposes a small public API so other RuneLite plugins
can create goals programmatically. This document is for developers of consumer
plugins.

## Quick start

```java
import com.goaltracker.GoalTrackerPlugin;
import com.goaltracker.api.GoalTrackerApi;
import javax.inject.Inject;
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(name = "My Goal Producer")
@PluginDependency(GoalTrackerPlugin.class)
public class MyConsumerPlugin extends Plugin
{
    @Inject
    private GoalTrackerApi goalTracker;

    public void onSomeEvent()
    {
        // Add a level-99 Slayer goal
        String id = goalTracker.addSkillGoalForLevel(Skill.SLAYER, 99);

        // Add a quest goal
        goalTracker.addQuestGoal(Quest.DRAGON_SLAYER_II);

        // Add an item goal (1 Twisted bow)
        goalTracker.addItemGoal(20997, 1);

        // Add an Elite achievement diary goal
        goalTracker.addDiaryGoal("Falador", GoalTrackerApi.DiaryTier.ELITE);

        // Add a combat achievement goal by wiki task id
        goalTracker.addCombatAchievementGoal(0); // Noxious Foe
    }
}
```

## Setup

### 1. Declare the dependency

Add `@PluginDependency(GoalTrackerPlugin.class)` to your plugin class. RuneLite's
`PluginManager` will:

- Topologically sort plugins so Goal Tracker initializes first
- Create a child Guice injector for your plugin that can resolve Goal Tracker's
  bindings

### 2. Compile-time access to the API interface

Your plugin code needs access to the `com.goaltracker.api.GoalTrackerApi`
interface at compile time. Two practical paths:

1. **Maven dependency on the Goal Tracker plugin** — once Goal Tracker is on the
   plugin hub, declare it as a dependency in your `build.gradle`.
2. **Copy the interface** — paste `GoalTrackerApi.java` into your project under
   the same package name. The interface only references types from
   `runelite-api` (`Skill`, `Quest`) and primitives, so there's no transitive
   dependency. The class identity is preserved at runtime via the shared
   classloader, so Guice will resolve the binding correctly.

### 3. Inject and call

```java
@Inject
private GoalTrackerApi goalTracker;
```

The injection happens after `startUp()` is called, so use the API from event
handlers or game-tick subscribers — not from the constructor.

## API methods

All `add*` methods are **idempotent**: calling them with the same arguments
multiple times is safe. Duplicate goals (same skill/item/quest/etc.) are not
created — the existing goal's id is returned instead.

All `add*` methods return:

- The created goal's id (a UUID `String`) on success
- The existing goal's id if a duplicate already exists
- `null` if validation failed (invalid input, unknown reference, etc.)

### `addSkillGoal(Skill, int)`

```java
String addSkillGoal(Skill skill, int targetXp);
```

Add a skill goal targeting a raw XP amount. Canonical form — all skill goals are
stored internally as XP targets.

- `skill` — the skill to track (must not be null)
- `targetXp` — target XP, 1 to 200,000,000

Goals are auto-positioned in the panel so lower targets appear above higher
targets for the same skill.

### `addSkillGoalForLevel(Skill, int)`

```java
String addSkillGoalForLevel(Skill skill, int level);
```

Convenience: takes a level and converts it to the corresponding XP threshold via
`Experience.getXpForLevel(level)`, then delegates to `addSkillGoal`.

- `level` — 1 to 126 (1-99 normal, 100-126 virtual)

### `addItemGoal(int, int)`

```java
String addItemGoal(int itemId, int targetQuantity);
```

Add an item goal by item id and target quantity.

- `itemId` — OSRS item id, e.g. from `net.runelite.api.gameval.ItemID`
- `targetQuantity` — target count, must be > 0

Auto-tags are derived from the plugin's `ItemSourceData` if available (e.g.
boss/raid/clue source labels).

### `addQuestGoal(Quest)`

```java
String addQuestGoal(Quest quest);
```

Add a quest goal. Auto-tracks completion via `Quest.getState(client)` polled on
the game tick (~15-second intervals).

### `addDiaryGoal(String, DiaryTier)`

```java
String addDiaryGoal(String areaDisplayName, GoalTrackerApi.DiaryTier tier);
```

Add an achievement diary goal by area display name and tier.

- `areaDisplayName` — area name as displayed in-game (e.g. "Ardougne",
  "Falador", "Karamja")
- `tier` — `EASY`, `MEDIUM`, `HARD`, or `ELITE`

Auto-tracks completion via the per-area-per-tier completion varbits where
exposed in `runelite-api` (Karamja Easy/Medium/Hard are not exposed and stay
manual-completion).

### `addCombatAchievementGoal(int)`

```java
String addCombatAchievementGoal(int caTaskId);
```

Add a combat achievement goal by wiki/in-game task id.

- `caTaskId` — the task id, 0 to 639

The task name, description, monster, tier, and tooltip are looked up from the
plugin's wiki cache. The goal auto-tracks via the bit-packed
`CA_TASK_COMPLETED_0..19` varplayers.

If the wiki cache hasn't loaded yet (first plugin run, no network), the call
returns null. Try again after a short delay.

To find wiki task ids, query the OSRS Wiki bucket API:
```
https://oldschool.runescape.wiki/api.php?action=bucket&format=json&query=bucket('combat_achievement').select('id','name').where('name','Noxious Foe').limit(1).run()
```

## Versioning and stability

The API is currently in v1. The interface is intentionally minimal and will
remain backward-compatible across plugin updates wherever possible. Methods may
be added; existing method signatures and return semantics will not change.

## Alternatives considered

The plugin could also expose the API via the RuneLite event bus (`@Subscribe`
on a public event class). That path was not implemented in v1 because:

1. The typed-service approach (`@Inject GoalTrackerApi`) is the canonical
   RuneLite plugin-to-plugin API pattern (used by `ClueScrollService`,
   `BankTagsPlugin`'s `TagManager`, etc.).
2. The event bus is one-way and doesn't return a goal id, which makes
   confirmation/correlation harder.
3. Both paths still require the consumer to have a class reference at compile
   time (the event class or the interface), so the decoupling benefit is
   marginal.

If a use case for an event-bus alternative arises, it can be added alongside
the typed API without breaking changes.
