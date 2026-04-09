# Goal Tracker — Roadmap

Captured 2026-04-08. Not committed to an order; items get promoted to
missions as appetite allows. See the mission summaries in the
`claude-config` repo for completed work.

## Quality-of-life

### Remove deletion / "remove all" confirmations
Now that undo/redo covers every user mutation (mission 26), the
two-step confirm dialogs on destructive actions are belt-and-suspenders.
Ctrl+Z is a faster, lower-friction recovery path. Expected to pair with
a settings page so users who want the confirmations can opt back in.

### "Move to" for single selection
The bulk context menu already has "Move to Section". The single-item
menu should too — it's currently only accessible by dragging, which is
fiddly for cards off-screen.

### Settings page
General settings for defaults and toggles. Replaces the current "remove
all" dropdown button (those options move into Settings as "danger zone"
entries). Houses the opt-in toggles for the deletion confirmations above.

## New goal types

### Boss kill count goals
Track kills on a per-boss basis via the in-game kill count counter or
chat parsing. Distinct from ITEM_GRIND because drops aren't the target —
the count itself is. Likely shares the Skill goal's relative/absolute
mode pattern (reach X kills / gain X more kills).

### Account-wide goals
Meta-goals that aggregate across the account rather than tracking a
single skill or item:
- **Total level**
- **Total collection log unlocks**
- **Combat log levels** (total Combat Achievement points across tiers)
- **Prayers you don't have** (progress toward unlocking the remaining
  prayers in the prayer book)
- **Kudos** (Varrock Museum)
- **Kingdom of Miscellania** approval / favor
Each likely needs its own tracker class hooking the right varbits or
chat/UI events. Tracker-per-type pattern already established.

## Goal state machine improvements

### Tracking semantics taxonomy (2026-04-08 design note)

Before implementing new goal types, it's worth having a framework for
classifying the metrics we track. Two independent axes:

**Axis 1 — Monotonicity of the underlying metric:**

- **Monotonic-up** (numbers that only go up): easy to target, naturally
  terminal. Examples:
  - Boss kill counts, quest points, experience, levels
  - Combat log points / tier, kudos, combat level, total level
  - Total quests / achievements / collection logs completed
  - Time played, cast counts, "X made", games played
  - Achievement diary status (tasks don't un-complete)
- **Monotonic-down** (numbers that only go down): inverted comparison
  (currentValue ≤ targetValue). Examples:
  - Fastest kill time per boss / encounter
  - Probably "minimum X achieved" style goals in general
- **Non-monotonic** (numbers that can fluctuate): require the
  persistent/non-persistent distinction below to be meaningful as goals.
  Examples:
  - Currency (GP), slayer points, item counts
  - Miscellania approval (has ceiling + decay)

**Axis 2 — Goal persistence (orthogonal to axis 1):**

- **Terminal**: once completed, stays completed. Default behavior today.
- **Persistent**: re-opens if the underlying metric drops below the
  target. Only meaningful for non-monotonic (or monotonic-down with
  caveats) metrics. Examples: "keep 1000 sharks banked", "maintain ≥75%
  Miscellania approval".

**Implementation implication:** single mutually-exclusive enum on each
goal expressing the tracking semantics:

- `IS_MONOTONIC_UP` — number only goes up, terminal once hit
- `IS_MONOTONIC_DOWN` — number only goes down (fastest kill time),
  inverted `meetsTarget` comparison
- `NON_MONOTONIC_PERSISTENT` — can fluctuate, goal re-opens if drops
  below target ("keep 1000 sharks banked")
- `NON_MONOTONIC_NON_PERSISTENT` — can fluctuate, terminal once hit
  ("earn 100M GP ever")

A single `ACTIVITY_COUNTER` or numeric goal type can handle most
monotonic-up cases with varying identity fields (kill count per boss,
cast count per spell, etc.).

**Ambiguous cases:**
- **Currency** is interpreted as *current balance* by default
  (non-monotonic), but "earn 100M GP lifetime" would be monotonic if we
  tracked lifetime earned. The goal EXPRESSION determines the category,
  not the metric itself.
- **Quest points** is a *derived* metric (sum over completed quests).
  Could be a view over the quest goal subset rather than a first-class
  goal type.

### Absorption for auto-seeded same-skill goals
When the wiki-based auto-create flow lands, multiple chains may each
auto-seed their own same-skill prerequisites (e.g. both the HFTD chain
and a separate quest chain seed their own "35 Agility"). If those
seeded goals end up sitting adjacent to each other in the local-repair
sort output, the lower-target one should be absorbed into the higher.

Rule (revised 2026-04-08 after switching to local-repair sort):
> After `queryGoalsTopologicallySorted` returns, scan adjacent pairs.
> For each pair where BOTH are `autoSeeded=true`, BOTH are the same
> goal type (SKILL or ITEM_GRIND), and BOTH have the same identity
> (skillName or itemId), delete the lower-target goal and rewire its
> inbound edges to the higher-target one.

No "topological tier" check needed — the original design was tied to
Kahn's tier-based sort, but the sort is now stable local-repair so
tiers don't exist as a concept. Positional adjacency is the only test.

Not needed yet in practice: the UI's "Requires…" picker only links
to existing goals, so there's no path today that creates `autoSeeded=true`
goals. Revisit when the wiki auto-create flow is built.

### Persistent goals
Item / achievement goals that *re-open* if the underlying state drops
below the threshold — opposite of mission 25's terminal-once-complete
rule. Use case: "keep 1000 sharks banked"; if you eat below 1000, the
goal should flip back to incomplete so you notice. Likely a per-goal
flag (not per-section, not global) so users can mix persistent and
terminal goals in the same section.

Interaction with undo: reopening should fire as a tracker-driven
mutation (bypasses undo stack), same as the existing close path.

### Optional goals
Goals the user explicitly marks as "nice to have, not required for
section completion". Affects section progress calculation (denominators
ignore optional items). Probably a per-goal flag in the model, rendered
as a visual distinction on the card (muted color, badge, or italics).

## Related goals & templates

### Quest requirements
Quests have minimum skill requirements. Adding a quest goal should
optionally auto-create the required skill goals too, linked to the
quest so completing the quest clears them.

### Achievement diary requirements
Same pattern, but diaries have task-level requirements that are much
harder to model than quest-level ones. Probably a phase 2 after quest
requirements ships, if it ships at all.

### User-defined related-goal templates
Generalization of the above: let users create, save, and share named
goal bundles ("Inferno prep", "Max GWD loot", etc.). Creates multiple
goals at once with predefined targets and tags. Subsumes the earlier
"goal import/export" idea — the templating infrastructure IS the
sharing infrastructure.

**Order for this cluster:** build the "add N goals atomically" infra
first (extending `beginCompound`/`endCompound` to cover multi-create
flows cleanly), then quest requirements as the first consumer, then
the templating UI once the infra is proven by a real use case.

## Release

### Prepare for Plugin Hub submission
Milestone 5 from the original scope (2026-04-04). Not yet started.
Likely its own mission once the roadmap above has matured — review
process usually surfaces its own work (naming, deps, license, perf,
security, screenshots, description).
