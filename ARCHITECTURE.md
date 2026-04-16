# Architecture

This document describes the runelite-goal-planner plugin's internal layering,
the public/internal API split, the data flow on each event type, and the
invariants that keep things consistent.

If you're looking for the public API method reference, see [API.md](API.md).
If you're looking for an overview of features, see [README.md](README.md).

## Layers (top to bottom)

```
                 ┌──────────────────────────────────────┐
                 │           GoalPlannerPlugin          │  RuneLite event handlers,
                 │  (event subscribe + MenuEntryAdded)  │  startUp/shutDown lifecycle
                 └────────────────┬─────────────────────┘
                                  │
              ┌───────────────────┼───────────────────────┐
              ▼                   ▼                       ▼
       ┌────────────┐   ┌──────────────────┐   ┌──────────────────┐
       │   Panel    │   │     Trackers     │   │  Right-click     │
       │ (Swing UI) │   │  (8 subclasses)  │   │  MenuEntry inject │
       └─────┬──────┘   └────────┬─────────┘   └────────┬─────────┘
             │                   │                      │
             │                   ▼                      │
             │           ┌──────────────────┐           │
             └──────────▶│  GoalPlannerApi  │◀──────────┘
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
`GoalPlannerApiImpl`. The panel calls it, the trackers call it, the right-
click handlers call it. There are zero direct `goalStore.X()` mutations
from the UI layer. Two bridge methods on the internal API exist
specifically to support panel patterns that needed more than the
public API offered:

- `addTagWithCategory(goalId, label, categoryName)` — the public `addTag`
  forces every external-API tag to `OTHER`; the panel's Add Tag dialog
  preserves user-picked categories via this internal variant
- `changeTarget` regenerates the display string (`SKILL` name from XP
  level, `ITEM_GRIND` description from quantity) so callers don't need to
  re-mutate the goal after a target change

## Public vs internal API

`GoalPlannerApi` (the public interface) is bound via
`Plugin.configure(Binder)` so external consumer plugins can `@Inject` it
after declaring `@PluginDependency(GoalPlannerPlugin.class)`. It's deliberately
limited to:
- Read: `queryAllGoals`, `queryAllSections`
- Create: `addSkillGoal`, `addSkillGoalForLevel`, `addItemGoal`,
  `addQuestGoal`, `addDiaryGoal`, `addCombatAchievementGoal`,
  `addBossGoal`, `addAccountGoal`, `addCustomGoal`
- Prereq-chain create: `addQuestGoalWithPrereqs`,
  `addDiaryGoalWithPrereqs`
- Light mutate: `changeTarget`, `editCustomGoal`, `markGoalComplete`,
  `markGoalIncomplete`, `addTag`, `removeTag`, `restoreDefaultTags`,
  `removeGoal`

Relation edges (`addRequirement`, `addOrRequirement`), requirement
resolvers, section CRUD, color overrides, and tracker write paths live
on `GoalPlannerInternalApi` — not exposed to external plugins.

`GoalPlannerInternalApi` is a separate interface implemented by the SAME
concrete class (`GoalPlannerApiImpl`). It is **not bound** in the Guice
injector; the plugin and its UI inject the concrete impl class directly.
Internal-only methods include: `removeGoal`, `markGoalComplete`,
`markGoalIncomplete`, section CRUD, color overrides, selection, bulk move,
and `recordGoalProgress` (the silent tracker write path).

This split lets external consumers exercise the safe surface while keeping
destructive / layout-coupled / batch-internal operations off-limits.

## The model layer

`Goal` is the persisted entity. Lombok `@Data @Builder`. Important fields:

- `id` — UUID, stable across renames/moves
- `type` — SKILL / QUEST / DIARY / COMBAT_ACHIEVEMENT / ITEM_GRIND / BOSS / ACCOUNT / CUSTOM
- `status` — ACTIVE / COMPLETE / BLOCKED / PAUSED. **Decorative.** The
  canonical completion check is `completedAt > 0`, not `status ==
  COMPLETE`. New code should never read or write `status` for
  completion logic.
- `currentValue` / `targetValue` — progress
- `completedAt` — long timestamp (millis). 0 means not complete.
- `sectionId` — UUID of the owning section. `null` is migrated to Incomplete on load.
- `priority` — list-position int, reindexed by GoalStore on every reorder
- `customColorRgb` — packed 0xRRGGBB user override; -1 means "use type default"
- `tagIds` / `defaultTagIds` — references to tag entities (see Tags section).
  defaultTagIds is the auto-generated snapshot from creation; tagIds is the
  live list. Restore Defaults copies defaultTagIds back to tagIds.
- `requiredGoalIds` — AND-edge prereqs: all must complete before this goal
  is considered satisfiable.
- `orRequiredGoalIds` — OR-edge prereqs: ANY one satisfying unlocks this
  goal (in addition to the AND-list). Used for "99 Attack OR 99 Strength
  OR 130 Att+Str combined" style gates.
- `bossName` / `questName` / `skillName` / `accountMetric` / `itemId` —
  type-specific discriminator fields; the tracker for each type reads
  the relevant one to know what live state to map to this goal.

`Section` mirrors the same shape: id, name, order int, collapsed bool,
builtInKind enum (INCOMPLETE / COMPLETED / null for user), colorRgb override.
Built-in section orders are pinned to constants (`ORDER_INCOMPLETE` =
`Integer.MAX_VALUE - 1`, `ORDER_COMPLETED` = `Integer.MAX_VALUE`); user
sections fill the 1..N band above them.

`ItemTag` carries label, category enum, and optional colorRgb override.

## GoalStore (the persistence layer)

`GoalStore` is the only thing that touches `ConfigManager`. Three
configuration keys: `goalplanner.goals`, `goalplanner.sections`, and
`goalplanner.tags` (tags are a first-class entity — see Tags section
below). All three are JSON arrays.

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

Eight trackers, each `@Singleton @Inject`. Seven type-specific
subclasses extend **`AbstractTracker`** which owns the
`checkGoals(List<Goal>) -> boolean` template. Common shape:

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
- **QuestTracker, DiaryTracker, CombatAchievementTracker, BossKillTracker, AccountTracker** → `onGameTick`
- **ItemTracker** → `onItemContainerChanged` (sums every persistent storage container exposed by RuneLite)

**BossKillTracker** reads `VarPlayerID.TOTAL_X_KILLS` / per-level
completion counters via `BossKillData.getVarpId(bossName)`. 89 bosses
and activities mapped as of v0.1.0.

**AccountTracker** reads account-wide metrics via a switch over the
`AccountMetric` enum: quest points, combat level, total level, CA
points, slayer points, museum kudos, combined Att+Str, Misc approval,
Tears of Guthix PB, Chompy kills, Colosseum Glory, DoM Deepest Level,
League Points, League Tasks. Adding a new metric requires a case
branch here plus an enum entry on `AccountMetric`.

The tracker batch contract:

> `recordGoalProgress` is **silent** — it does NOT save, reconcile, or fire
> the panel-rebuild callback. Trackers iterate over their goals, and the
> event handler that called the tracker is responsible for a single
> `goalStore.save() + goalStore.reconcileCompletedSection() +
> SwingUtilities.invokeLater(panel::rebuild)` once at the end if anything
> updated. Firing per-goal would create N rebuilds per tick.

The handlers in `GoalPlannerPlugin` enforce this contract uniformly. Adding
a new event-driven tracker means following the same pattern.

**ItemTracker has special pre-bank-visit logic:** before the user has opened
their bank once this session, the tracker can only see inventory contents.
A naive read would wipe a persisted bank+inventory total down to inventory-
only on every login. The `bankSeenThisSession` flag + a growth-only guard
prevents this: pre-bank-visit, only updates that strictly increase the
persisted value are applied. After the first bank visit, the flag flips
permanently and full counts apply.

**ItemTracker counts across multiple containers:** the sum
covers `INVENTORY`, `BANK`, `EQUIPMENT`, `SEED_VAULT`, `GROUP_STORAGE`, and
`KINGDOM_OF_MISCELLANIA`. Each is only summed when its `ItemContainer` is
non-null — RuneLite only populates one when the player has interacted with
the corresponding interface this session. Combined with the bank-null
guard, this gives a "best effort, monotonically growing as new containers
are sighted" model. Transient reward chests (Barrows, ToA, CoX, Wildy
loot, etc) are intentionally excluded — they're populated briefly and
would double-count items the player is about to bank. `countItem` is
public so the create-goal UI can snapshot a baseline for relative item
goals.

**Relative goal targets:** the Add Goal dialog has a Mode toggle
("Reach X" / "Gain X more") for SKILL and ITEM_GRIND goals. Relative
input is resolved at creation time — the math runs in the UI via
`RelativeTargetResolver` (a pure helper) and the resulting absolute
target hits the existing `addSkillGoal` / item-search flows unchanged.
Once stored, a relative goal is indistinguishable from an absolute
one with the same target. No new API surface, no new fields on Goal.
CUSTOM and skill level-deltas are deferred.

**ItemTracker also does NOT filter on `status != ACTIVE`** unlike the other
4 trackers. Item quantities can decrease (drop, sell, use as material) so a
previously-COMPLETE item goal must still be re-checked every event —
`recordGoalProgress`'s symmetric un-complete branch reverts it if the value
drops below target. Other trackers can skip COMPLETE goals because their
underlying state never decreases (once 99 attack, always 99 attack; once
quest done, always quest done).

## Requirement resolvers and prereq seeding

Quest, diary, and boss goals can all seed a prereq tree on creation
(the "Add Goal with Requirements" right-click path). Three data tables
drive it:

- **`QuestRequirements`** — per-quest skill/quest prereqs, QP, hard
  combat level, kudos, recommended skills, recommended combat level.
- **`DiaryRequirements`** — per (area, tier) skill/quest/unlock reqs,
  bossKills, itemReqs, accountReqs, `Alternative` OR-groups.
- **`BossKillData`** — per-boss `BossPrereqs` with the same shape as
  `DiaryRequirements.Reqs`: skills, unlocks, quests, bossKills,
  itemReqs, accountReqs, and `Alternative` OR-groups.

Two resolvers convert data tables into `Goal` templates, pre-filtering
against live player state:

- **`QuestRequirementResolver.resolve(Quest, Client)`** — emits
  templates for every unmet skill / unfinished quest prereq, plus
  ACCOUNT templates for QP / hard combat / kudos, plus optional
  templates for recommended combat level (pre-filtered against the
  player's current combat level to avoid emitting already-met
  recommendations).
- **`DiaryRequirementResolver.resolve(area, tier, Client)`** — same
  shape, returns `Resolved` with templates + `ResolvedUnlock` list
  (each carrying its own quest/skill/account prereqs + OR-alternatives).

**Pre-filter, not post-filter.** Every recommendation / requirement
is filtered against live player state in the resolver *before*
emission, not after the seeder creates the goal. This is because
freshly-created ACCOUNT goals have `currentValue=0` until the next
`AccountTracker` tick — so the post-creation `isComplete` check misses
already-met account requirements.

### `seedPrereqsInto` — the priority-queue BFS

`GoalCreationService.seedPrereqsInto` is the central engine that
consumes resolver templates and creates goals linked to a root. Used
by `addDiaryGoalWithPrereqs`, `addQuestGoalWithPrereqs`, and
`addBossGoal`. Three-queue priority BFS:

1. **Optional queue** (recommendations, combat-level pills) — drain first
2. **High-priority queue** (SKILL / ACCOUNT / ITEM / BOSS templates)
3. **Low-priority queue** (QUEST templates) — drain last

When a QUEST template is dequeued, its own prereqs are resolved via
`QuestRequirementResolver.resolve` and pushed onto the appropriate
queues with the quest as parent. This produces a card-list ordering
where leaf skills surface at the top of the card list and the deep
quest chain reads top-down.

Cycle guard: every quest processed goes into a `visited` set before
we resolve its prereqs. Resolving a quest that's already in `visited`
is a no-op.

### OR-groups (alternatives)

`DiaryRequirements.Alternative` and `BossKillData.Alternative` describe
"any one of these paths satisfies the gate" — e.g. Warriors Guild entry
is "130 Att+Str combined OR 99 Attack OR 99 Strength". The seeder wires
these via `api.addOrRequirement(parentId, childId)` (a separate edge
list from the AND-required list).

`GoalMutationService.checkOrPrereqCompletion` applies the semantics:
the parent auto-completes when **ANY one** OR-child is complete AND
**all** AND-children are complete. The UI renders OR-children under
an "Also Completed By" heading in the parent's card tooltip.

**Tracker-thread-affinity rule.** Menu `onClick` handlers that end up
calling resolvers MUST run on the RuneLite client thread (not the
Swing EDT), because `Quest.getState(client)` and
`client.getRealSkillLevel` assert the client thread. Paths that open
an input dialog via `SwingUtilities.invokeLater` must hop back to
`clientThread.invokeLater` before calling `addBossGoal` / etc.
Violating this causes the resolver calls to throw `IllegalStateException`
which the EDT swallows — the feature appears to do nothing with no
error visible.

## Undo / Redo

User-driven mutations are routed through a Command pattern so they can be
undone and redone. Tracker-driven mutations (skill XP, quest tick, item
count change, diary tier, CA flag) bypass the command system entirely —
they call store primitives directly so they never appear in undo history.

The pieces:

- **`com.goalplanner.command.Command`** — interface with `apply()`,
  `revert()`, `getDescription()`. Each command captures the state needed
  to revert itself at construction time (snapshots of "before" values
  plus the action's parameters).
- **`CommandHistory`** — in-memory undo + redo stacks. `execute(cmd)`
  runs the command and pushes onto undo, clearing redo. `undo()` pops
  from undo, calls `revert()`, pushes onto redo. `redo()` pops from redo,
  calls `apply()`, pushes back onto undo. Capped at 50 entries; oldest
  trimmed first. Failure-mode: if `revert()` returns false or throws,
  the entry is dropped from history (fail-open) — caller's state is
  left as-is.
- **`GoalPlannerApiImpl.executeCommand(Command)`** — internal entry
  point for user-mutation API methods. Runs the command via
  `CommandHistory.execute` and fires `onGoalsChanged` once on success.
  Tracker code paths bypass this and call store primitives directly.
- **`undo()` / `redo()` / `canUndo()` / `canRedo()` / `peekUndoDescription()`**
  on the API for the toolbar buttons.

Each undoable mutation method (e.g. `setGoalColor`, `removeGoal`,
`bulkRestoreDefaults`) snapshots its before-state inline, builds a
Command whose `apply()` and `revert()` close over the snapshot, and
hands it to `executeCommand`. Bulk actions are a single undo step:
the Command captures every change it made and reverts them all in one
shot. Reverting must be the exact opposite of the forward action — a
bulk-add reversed = remove of exactly the goals/tags that were added,
not the broader bulk-remove API.

ID stability matters for redo: when you undo "Add goal", the goal is
removed from the store. When you redo, it must be re-added with the
SAME id so any later commands referencing it still resolve. Add/remove
commands cache the original Goal entity (preserving its id) for the
revert + redo cycle.

Tracker-driven mutations are NOT undoable by design. Undoing "I gained
10k Slayer XP" would create a confusing rubber-band effect with the
real game state, and there's no obvious "redo" semantic. Only actions
the user explicitly performed go on the stack.

Currently undoable:
- addCustomGoal, removeGoal, changeTarget, setGoalColor
- markGoalComplete, markGoalIncomplete
- addTag, removeTag, restoreDefaultTags
- bulkRestoreDefaults, bulkRemoveTagFromGoals

Deferred: section CRUD, tag entity CRUD, reorder commands.

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

Selection is a `LinkedHashSet<String>` on `GoalPlannerApiImpl`. Not
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

Tags are first-class entities. The store has three top-level
collections: `goals`, `sections`, `tags`.

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

**Color rules.** Color responsibility split by category:

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

### Icons

Tags can render as either a color OR an icon. The Tag entity carries a
nullable `iconKey` String:

- `iconKey == null` → render as a colored pill (category color or per-tag for OTHER)
- `iconKey` matches a `Skill` enum name (case-insensitive) → render via
  `SkillIconManager.getSkillImage(skill)`. The 24 SKILLING system tags are
  seeded with iconKey set to the skill's enum name (`ATTACK`, `STRENGTH`, etc).
- `iconKey` is anything else → render via classpath resource at
  `/icons/<iconKey>.png`. Bundled icons can ship in
  `src/main/resources/icons/`; the project currently ships zero, so this
  path is wired but only the SkillIconManager case is exercised in practice.

The render branch lives in `GoalCard.resolveIcon` and `createTagComponent`.
Icons entirely replace the colored pill — when an icon resolves
successfully, neither the per-category nor the per-tag color is shown.
If both lookups fail, the tag falls through to color rendering.

The icon picker UI lives in `IconPickerDialog`, opened from the
TagManagementDialog row's "Icon" button. Shows a grid of skill icons +
a section for bundled icons + a "Clear icon" button. Returns the picked
iconKey (or null for clear), or the original key on cancel.

**Why a single iconKey String instead of a typed source enum?** The
resolver is two cheap fallback lookups (Skill enum name → bundled
resource → fail). Adding a `iconType: SKILL/RESOURCE/SPRITE` field
would require migration whenever new sources are added; the string
key is sourceless and forward-compatible.

**API surface:**
- `queryAllTags()` — read all tags as TagViews
- `createUserTag(label, categoryName)` — idempotent on case-insensitive (label, category)
- `renameTag(tagId, newLabel)` — fails on system tags
- `recolorTag(tagId, rgb)` — branches on category: OTHER → per-tag, others → per-category. Fails on SKILLING.
- `setCategoryColor(categoryName, rgb)` — per-category color (BOSS/RAID/CLUE/MINIGAME). Rejects SKILLING and OTHER.
- `resetCategoryColor(categoryName)` — clear category override
- `deleteTag(tagId)` — fails on system, cascades to all goals' tagIds
- `addTagWithCategory(goalId, label, categoryName)` — find-or-create + attach reference
- `setTagIcon(tagId, iconKey)` — set/clear an icon on any tag
- `clearTagIcon(tagId)` — equivalent to setTagIcon(tagId, null)

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
`goalplanner.goals` and `goalplanner.sections`. Schema is implicit (no
version field yet) — when fields are added, missing fields deserialize to
their default values via Gson + `@Builder.Default`. Schema-incompatible
changes (renames, type changes) need migration logic in `load()`.

`java.awt.Color` is **not** serializable under JDK 17+ — it must be
persisted as a packed int (`0xRRGGBB`). The 4 model classes that hold
colors (Section, Goal, ItemTag, anything else added later) all use packed
ints and convert to `Color` only at render time.

## Test layout

```
src/test/java/com/goalplanner/
├── api/                                  # Public + internal API surface tests
├── data/                                 # BossKillData, QuestRequirements,
│                                         #   BossAlternativeSeedingTest
├── integration/                          # Deep end-to-end flows
│                                         #   (quest chains, boss prereq seeding,
│                                         #   diary completion, warriors-guild OR)
├── model/                                # Model invariants
├── persistence/                          # Load/save/migrate/reconcile + section CRUD
├── service/                              # Skill-chain ordering
├── tracker/                              # All 8 trackers
├── testsupport/                          # InMemoryConfigManager, MockClientFactory,
│                                         #   MockGameState, TrackerTestHarness
├── ui/                                   # Dialog + picker behavior
└── util/                                 # Number formatting
```

399 tests as of v0.1.0. All 8 trackers have smoke tests; transitive
prereq chaining has integration coverage at Gauntlet → SotE depth;
OR-alternative seeding is validated via a synthetic `BossPrereqs`
injected through `BossKillData.swapPrereqsForTest` (package-private
test hook).

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
- **MockClient is thread-agnostic** — it will NOT catch the
  "Quest.getState called off the client thread" bug. Integration tests
  pair with an `EDT-hop` check on any path that goes through a menu
  handler. See [TESTING.md](TESTING.md) for the full fixture playbook.

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
