# Architecture

This document describes the runelite-goal-tracker plugin's internal layering,
the public/internal API split, the data flow on each event type, and the
invariants that keep things consistent.

If you're looking for the public API method reference, see [API.md](API.md).
If you're looking for an overview of features, see [README.md](README.md).

## Layers (top to bottom)

```
                 ┌──────────────────────────────────────┐
                 │           GoalTrackerPlugin          │  RuneLite event handlers,
                 │  (event subscribe + MenuEntryAdded)  │  startUp/shutDown lifecycle
                 └────────────────┬─────────────────────┘
                                  │
              ┌───────────────────┼───────────────────────┐
              ▼                   ▼                       ▼
       ┌────────────┐   ┌──────────────────┐   ┌──────────────────┐
       │   Panel    │   │     Trackers     │   │  Right-click     │
       │ (Swing UI) │   │  (5 subclasses)  │   │  MenuEntry inject │
       └─────┬──────┘   └────────┬─────────┘   └────────┬─────────┘
             │                   │                      │
             │                   ▼                      │
             │           ┌──────────────────┐           │
             └──────────▶│  GoalTrackerApi  │◀──────────┘
                         │  Impl (canonical │
                         │  mutation API)   │
                         └────────┬─────────┘
                                  │
            ┌─────────────────────┼─────────────────────┐
            ▼                     ▼                     ▼
     ┌────────────┐      ┌──────────────┐      ┌────────────────┐
     │ GoalStore  │      │ GoalReorder- │      │ Wiki/Source/   │
     │ (persist)  │      │ ingService   │      │ Tag data       │
     └─────┬──────┘      └──────────────┘      └────────────────┘
           │
           ▼
     ┌────────────┐
     │ ConfigMgr  │  RuneLite-provided
     │  (JSON)    │  per-profile storage
     └────────────┘
```

**The single most important rule:** every mutation goes through
`GoalTrackerApiImpl`. The panel calls it, the trackers call it, the right-
click handlers call it. As of Mission 19 there are zero direct
`goalStore.X()` mutations from the UI layer. Two bridge methods on the
internal API exist specifically to support panel patterns that needed
more than the public API offered:

- `addTagWithCategory(goalId, label, categoryName)` — the public `addTag`
  forces every external-API tag to `OTHER`; the panel's Add Tag dialog
  preserves user-picked categories via this internal variant
- `changeTarget` regenerates the display string (`SKILL` name from XP
  level, `ITEM_GRIND` description from quantity) so callers don't need to
  re-mutate the goal after a target change

## Public vs internal API

`GoalTrackerApi` (the public interface) is bound via
`Plugin.configure(Binder)` so external consumer plugins can `@Inject` it
after declaring `@PluginDependency(GoalTrackerPlugin.class)`. It's deliberately
limited to read methods (`queryAllGoals`, `queryAllSections`) and the create
methods (`addSkillGoal`, `addCustomGoal`, `addItemGoal`, `addQuestGoal`,
`addDiaryGoal`, `addCombatAchievementGoal`).

`GoalTrackerInternalApi` is a separate interface implemented by the SAME
concrete class (`GoalTrackerApiImpl`). It is **not bound** in the Guice
injector; the plugin and its UI inject the concrete impl class directly.
Internal-only methods include: `removeGoal`, `markGoalComplete`,
`markGoalIncomplete`, section CRUD, color overrides, selection, bulk move,
and `recordGoalProgress` (the silent tracker write path).

This split lets external consumers exercise the safe surface while keeping
destructive / layout-coupled / batch-internal operations off-limits.

## The model layer

`Goal` is the persisted entity. Lombok `@Data @Builder`. Important fields:

- `id` — UUID, stable across renames/moves
- `type` — SKILL / QUEST / DIARY / COMBAT_ACHIEVEMENT / ITEM_GRIND / CUSTOM
- `status` — ACTIVE / COMPLETE / BLOCKED / PAUSED. **Decorative** since
  Mission 11. The canonical completion check is `completedAt > 0`, not
  `status == COMPLETE`. New code should never read or write `status` for
  completion logic.
- `currentValue` / `targetValue` — progress
- `completedAt` — long timestamp (millis). 0 means not complete.
- `sectionId` — UUID of the owning section. `null` is migrated to Incomplete on load.
- `priority` — list-position int, reindexed by GoalStore on every reorder
- `customColorRgb` — packed 0xRRGGBB user override; -1 means "use type default"
- `tags` / `defaultTags` — embedded `ItemTag` lists. defaultTags is the auto-
  generated snapshot from creation; tags is the live list (may include user
  additions). Restore Defaults copies defaultTags back to tags.

`Section` mirrors the same shape: id, name, order int, collapsed bool,
builtInKind enum (INCOMPLETE / COMPLETED / null for user), colorRgb override.
Built-in section orders are pinned to constants (`ORDER_INCOMPLETE` =
`Integer.MAX_VALUE - 1`, `ORDER_COMPLETED` = `Integer.MAX_VALUE`); user
sections fill the 1..N band above them.

`ItemTag` carries label, category enum, and optional colorRgb override.

## GoalStore (the persistence layer)

`GoalStore` is the only thing that touches `ConfigManager`. Two configuration
keys: `goaltracker.goals` and `goaltracker.sections`. Both are JSON arrays.

Key methods worth knowing:

- `load()` — reads both blobs, runs `ensureBuiltInSections()` (creates
  Incomplete + Completed if missing, force-updates their order to current
  constants on every load), `migrateOrphanedGoals()` (assigns null sectionIds
  to Incomplete or Completed based on completion state), and `normalizeOrder()`
  (sorts the goals list by `(section.order, priority)` so the panel can iterate
  contiguous slices per section).
- `save()` — serializes both blobs back via ConfigManager.
- `addGoal(g)` — assigns sectionId to Incomplete by default if null, sets
  priority to end-of-list, saves.
- `reorder(from, to)` — pure list move + reindex priorities. Does NOT
  normalize. Section grouping is preserved only if the caller stays
  within-section. The reordering service relies on this.
- `reconcileCompletedSection()` — moves COMPLETE goals into Completed and
  un-completed goals out of it. Returns true if anything changed. Called
  by every event handler that runs trackers, AND by the API's
  markGoalComplete/Incomplete.
- `normalizeOrder()` — re-sorts the flat goals list by `(section.order,
  priority)` and reindexes. Called after section operations and goal moves
  that could break grouping.
- Section CRUD: `createUserSection`, `renameUserSection`, `deleteUserSection`
  (reassigns goals to Incomplete), `reorderUserSection`, `moveGoalToSection`,
  `removeAllUserSections`. All renumber the user section band 1..N after.

**Section grouping invariant:** the panel renderer (`GoalPanel.rebuild`)
iterates each section in order and finds the contiguous slice of goals with
that sectionId. If a goal's position in the goals list is NOT contiguous
with its section, it will be invisible. Every reorder/move/normalize must
preserve grouping.

## Trackers (the read-from-game path)

Five trackers, each `@Singleton @Inject`. Common shape:

```java
public boolean checkGoals(List<Goal> goals) {
  for (Goal g : goals) {
    if (g.getType() != THIS_TYPE) continue;
    int newValue = readFromGame(g);
    if (api.recordGoalProgress(g.getId(), newValue)) anyUpdated = true;
  }
  return anyUpdated;
}
```

Each tracker is wired to a different RuneLite event:

- **SkillTracker** → `onStatChanged` (also on GameTick as a fallback)
- **QuestTracker, DiaryTracker, CombatAchievementTracker** → `onGameTick`
- **ItemTracker** → `onItemContainerChanged` (filtered to inventory + bank)

The tracker batch contract (Mission 14):

> `recordGoalProgress` is **silent** — it does NOT save, reconcile, or fire
> the panel-rebuild callback. Trackers iterate over their goals, and the
> event handler that called the tracker is responsible for a single
> `goalStore.save() + goalStore.reconcileCompletedSection() +
> SwingUtilities.invokeLater(panel::rebuild)` once at the end if anything
> updated. Firing per-goal would create N rebuilds per tick.

The handlers in `GoalTrackerPlugin` enforce this contract uniformly. Adding
a new event-driven tracker means following the same pattern.

**ItemTracker has special pre-bank-visit logic:** before the user has opened
their bank once this session, the tracker can only see inventory contents.
A naive read would wipe a persisted bank+inventory total down to inventory-
only on every login. The `bankSeenThisSession` flag + a growth-only guard
prevents this: pre-bank-visit, only updates that strictly increase the
persisted value are applied. After the first bank visit, the flag flips
permanently and full counts apply.

**ItemTracker also does NOT filter on `status != ACTIVE`** unlike the other
4 trackers. Item quantities can decrease (drop, sell, use as material) so a
previously-COMPLETE item goal must still be re-checked every event —
`recordGoalProgress`'s symmetric un-complete branch reverts it if the value
drops below target. Other trackers can skip COMPLETE goals because their
underlying state never decreases (once 99 attack, always 99 attack; once
quest done, always quest done).

## GoalReorderingService (skill chain rules)

Same-skill goals must be ordered with lower target above higher target
within their section. e.g. Prayer 96 must appear above Prayer 99 in any
section that contains both.

`findInsertionIndex(skillName, targetXp, sectionId)` — returns the global
index of the first same-skill goal in the same section with a higher target,
or -1. Used by `addSkillGoal` to bubble new goals to the right slot.

`enforceSkillOrderingInSection(sectionId)` — bubble-sort within a section
until the rule holds. Used after `moveGoalToSection` for SKILL goals.

`enforceSkillOrdering()` — convenience that calls the per-section variant
for every distinct sectionId. Used by `moveGoalTo` (Move to Top/Bottom).

`moveGoal(from, to)` and `makeRoom(...)` — recursive single-step move
helper for arrow buttons. Currently still section-blind in the recursive
descent; works in practice because callers only invoke them with within-
section indices, but flagged as a follow-up if cross-section calls ever
appear.

## Panel rendering

`GoalPanel.rebuild()` is the central render path. Every state change ends
with `panel.rebuild()` (either directly from a handler or via the
`onGoalsChanged` callback that the API impl fires).

The render flow:

1. `api.queryAllGoals()` → list of `GoalView` DTOs sorted by section.order then priority
2. `api.queryAllSections()` → list of `SectionView` DTOs
3. For each section, find the contiguous slice of goalViews with matching
   sectionId, render the section header (color, name, count), then each
   GoalCard in that slice
4. Build the right-click menu **lazily on each show** (not at construction
   time) so contents reflect current selection / completion / tag state

`GoalCard.paintComponent` draws everything custom: rounded rect, type-
colored tint, optional 2px white/grey selection outline. Uses paintComponent
not setBorder to avoid FlatLaf UI delegate interference.

The header has 4 buttons: Clear All Goals (gray −), Add Goal (gray +),
Clear All Sections (blue −), Add Section (blue +). The visual gray-vs-blue
split groups them by domain.

## Selection (ephemeral)

Selection is a `LinkedHashSet<String>` on `GoalTrackerApiImpl`. Not
persisted. The DTO carries a `selected` boolean populated from the set in
`toGoalView` so the render path is uniform.

Click semantics:
- Plain click on unselected card → replace selection with this card
- Plain click on selected card → clear selection
- Cmd/Ctrl-click on unselected → add to selection
- Cmd/Ctrl-click on selected → remove from selection
- Right-click on unselected card → menu shows + selection clears (rule 1)
- Right-click on selected card with sel.size() == 1 → normal single-item menu
- Right-click on selected card with sel.size() >= 2 → bulk menu

The "Rule 1" auto-clear on action against an unselected card lives at
each action site (popup-show, arrow-button move) rather than at click time,
because actions can come from both clicks and menus.

## Tags (first-class entity model)

Mission 19 promoted tags from per-goal embedded values to first-class
entities. The store has three top-level collections now: `goals`,
`sections`, `tags`.

`Tag` model: `id` (UUID), `label`, `category` (TagCategory enum), `colorRgb`
(packed override, -1 = use category default), `system` (boolean).

`Goal.tagIds` is a `List<String>` of tag entity ids. `Goal.defaultTagIds`
is the snapshot from creation, used by `restoreDefaultTags`. There is no
embedded ItemTag on goals — `ItemTag` survives only as a lightweight
`(label, category)` spec class used by the data layer (`ItemSourceData`,
`SourceAttributes`) which is auto-generated and too large to refactor.
The data layer's specs are converted to tag entities via
`goalStore.findOrCreateSystemTag(label, category)` at goal creation time.

**System vs user tags.** The `system` flag distinguishes plugin-generated
tags (Boss / Raid / Tier / Slayer attached to CA goals, etc) from
user-created tags. Edit permissions:

| Origin | Category | Rename | Recolor | Delete |
|--------|----------|--------|---------|--------|
| User   | any      | ✓      | ✓       | ✓      |
| System | SKILLING | ✗      | ✗       | ✗      |
| System | other    | ✗      | ✓       | ✗      |

System tags cannot be deleted because goal creation flows depend on their
existence (each CA goal looks up its boss tag via `findOrCreateSystemTag`
on every creation, so deleting it would orphan the lookup). System tags
in the SKILLING category are fully read-only because they render as skill
icons — recoloring would break the visual recognition.

**Color rules (Mission 20).** Color responsibility split by category:

| Category | Color source | Edit path |
|----------|-------------|-----------|
| BOSS, RAID, CLUE, MINIGAME | **Per-category** color stored on `GoalStore.categoryColors` (Map<TagCategory.name, packedRgb>). All tags in the category share one color. | TagManagementDialog → category tab → "Category color" header → Edit / Reset |
| OTHER | **Per-tag** color stored on `Tag.colorRgb`. Each Other tag has its own color independent of others. | TagManagementDialog → Other tab → per-row Recolor button. Also right-click "Recolor Tag" on goal cards (only shown when at least one Other tag is on the goal). |
| SKILLING | Read-only — rendered as skill icons, color is irrelevant. | n/a |

**No per-goal color overrides.** Recoloring a non-OTHER tag affects every
goal that references any tag in that category. Recoloring an OTHER tag
affects every goal that references that specific tag.

The `setTagColor(goalId, label, rgb)` API method survives as a bridge —
it finds the tag entity referenced by the goal, branches on category
(OTHER → per-tag, others → per-category), and delegates accordingly.

**Tag management UI** lives in `TagManagementDialog`, accessible from a
header button. Lists every tag with per-row Rename / Recolor / Delete
actions; buttons are disabled per the system tag rules above.

**API surface:**
- `queryAllTags()` — read all tags as TagViews
- `createUserTag(label, categoryName)` — idempotent on case-insensitive (label, category)
- `renameTag(tagId, newLabel)` — fails on system tags
- `recolorTag(tagId, rgb)` — branches on category: OTHER → per-tag, others → per-category. Fails on SKILLING.
- `setCategoryColor(categoryName, rgb)` — per-category color (BOSS/RAID/CLUE/MINIGAME). Rejects SKILLING and OTHER.
- `resetCategoryColor(categoryName)` — clear category override
- `deleteTag(tagId)` — fails on system, cascades to all goals' tagIds
- `addTagWithCategory(goalId, label, categoryName)` — find-or-create + attach reference

## Color overrides

Three layers: Section, Goal, ItemTag. Each has a `colorRgb` field with -1
sentinel = "use default". DTOs expose three sibling fields:
- `colorRgb` — current effective color (override or default)
- `defaultColorRgb` — what reset would revert to
- `colorOverridden` — boolean for "is this an override"

Section header backgrounds darken the picked color by 0.55× before painting
so the existing light-grey label text always contrasts. Goal cards use the
color as a 40-alpha tint over a dark base. Tag pills use the color directly.

## Persistence schema notes

Both blobs are stored as Gson-serialized arrays under
`goaltracker.goals` and `goaltracker.sections`. Schema is implicit (no
version field yet) — when fields are added, missing fields deserialize to
their default values via Gson + `@Builder.Default`. Schema-incompatible
changes (renames, type changes) need migration logic in `load()`.

`java.awt.Color` is **not** serializable under JDK 17+ — it must be
persisted as a packed int (`0xRRGGBB`). The 4 model classes that hold
colors (Section, Goal, ItemTag, anything else added later) all use packed
ints and convert to `Color` only at render time.

## Test layout

```
src/test/java/com/goaltracker/
├── api/
│   └── GoalTrackerApiImplTest.java       # Public + internal API surface
├── model/
│   └── GoalTest.java                     # Model invariants
├── persistence/
│   └── GoalStoreTest.java                # Load/save/migrate/reconcile/section CRUD
├── service/
│   └── GoalReorderingServiceTest.java    # Skill chain ordering
├── tracker/
│   └── ItemTrackerTest.java              # Bank-null guard, growth-only, un-complete
├── testsupport/
│   └── InMemoryConfigManager.java        # Mockito-backed map fake for ConfigManager
└── util/
    └── FormatUtilTest.java               # Number formatting
```

92 tests as of Mission 18. Goal: API + Store + ItemTracker covered now;
secondary trackers (Skill/Quest/Diary/CA), color/selection/bulk specifics,
and property-based model tests are deferred to follow-up missions.

**Testing rules of thumb:**

- **Mocks for stateless deps** (`Client`, `ItemManager`, `ItemContainer`).
  Use `Mockito.mock(...)` and stub specific methods.
- **Real implementations against fakes for stateful deps** (`GoalStore` with
  `InMemoryConfigManager`). Don't write parallel "TestX" implementations
  that drift from the real semantics.
- **Test the API surface, not the internal helpers.** If a private helper
  has interesting behavior, expose it via the API (add a method) or test
  it indirectly through the API methods that call it.
- **Test names use `@DisplayName`** so the test runner output reads as
  English. The method name is incidental.

## Build / Run

JDK 21 required (Zulu recommended on macOS). Gradle 8.x.

```bash
./gradlew test    # Run all tests
./gradlew run     # Launch RuneLite in dev mode with the plugin loaded
./gradlew build   # Standard build
./gradlew shadowJar  # Build the side-loadable jar
```

The `run` task uses `--add-opens=java.desktop/com.apple.eawt=ALL-UNNAMED`
for macOS Cocoa integration. JDK 11 was the original target; it works on
21 with the add-opens flag.
