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
