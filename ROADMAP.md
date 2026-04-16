# Goal Tracker — Roadmap

Planned future work. Not committed to an order; items are picked up
as appetite allows. See [CHANGELOG.md](CHANGELOG.md) for work that has
shipped in v0.1.0.

> ⚠️ **Experimental v0.1.0** — Persistence format and the public API
> may change in breaking ways before a stable 1.0 tag. Items below
> may become higher-priority if a breaking change is staged.

## Priority follow-ups (known bugs / obvious gaps)

### Collection-log multi-variant right-click submenu
Some collection-log entries map to multiple trackable bosses
(Callisto/Artio, Vet'ion/Calvar'ion, Venenatis/Spindel, Dagannoth
Prime/Rex/Supreme, maybe others). The right-click handler currently
picks one variant or offers no menu at all. Should offer a submenu so
the user can target a specific variant directly from the col log.
`COLLECTION_LOG_ALIASES` already encodes the variant mapping; the
handler just needs to consume it.

### Alternative OR-group UI rendering
`BossKillData.BossPrereqs.alternatives` and the diary equivalent now
seed OR-edges correctly, but the card list doesn't visually distinguish
OR-children from AND-children. "Also Completed By" shows in a tooltip,
but a clearer in-list grouping (nested subheader, dividing line, or
visual chevron) would make the semantics obvious at a glance.

### Corp Beast quest gate
Corporeal Beast requires "Summer's End" quest, but that enum value is
not exposed in `net.runelite.api.Quest` yet. Wrap as a custom Unlock
when the upstream API adds it, or ship a workaround (maybe an item
requirement on the Spirit shield from the quest).

## Quality-of-life

### Remove deletion / "remove all" confirmations
With undo/redo covering every user mutation, two-step confirm dialogs
on destructive actions are belt-and-suspenders. Ctrl+Z is a faster,
lower-friction recovery path. Expected to pair with a settings page so
users who want the confirmations can opt back in.

### "Move to" for single selection
The bulk context menu already has "Move to Section". The single-item
menu should too — it's currently only accessible by dragging, which is
fiddly for cards off-screen.

### Settings page
General settings for defaults and toggles. Replaces the current
"remove all" dropdown button (those options move into Settings as
"danger zone" entries). Houses the opt-in toggles for the deletion
confirmations above.

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
  persistent/non-persistent distinction below to be meaningful as
  goals. Examples:
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

### Persistent goals
Item / achievement goals that *re-open* if the underlying state drops
below the threshold. Use case: "keep 1000 sharks banked"; if you eat
below 1000, the goal should flip back to incomplete so you notice.
Likely a per-goal flag so users can mix persistent and terminal goals
in the same section.

### Absorption for auto-seeded same-skill goals
When multiple prereq chains seed same-skill goals at different targets
(e.g. a boss chain seeds 70 Ranged and a separate quest chain seeds 80
Ranged), adjacent `autoSeeded=true` same-skill goals should be merged:
delete the lower-target goal and rewire its inbound edges to the
higher-target one.

Not needed yet in practice — the current prereq chains mostly converge
at the root level. Revisit when user-authored goal bundles (below)
land and multiple independent chains can exist.

## New goal types

### Monotonic-down metrics
Fastest boss kill time, shortest run duration, etc. Requires the
tracking-semantics taxonomy above to land first so the comparison
direction is model-level, not per-call-site.

### Persistent bank/inventory threshold goals
Pair with the persistent-goals flag: "keep 50 prayer potions banked",
"have at least 1M coins for repairs". Shares the ITEM_GRIND reader
but uses the persistent semantics.

### Remaining account metrics
- Total collection log unlocks
- Prayers you don't have (progress toward unlocking the remaining
  prayerbook entries)
- Fremennik favor, Elven Clans favor, etc.
- Bounty Hunter rank
- Season / league rank if applicable

Each likely needs its own case branch in `AccountTracker.readMetric`.

## Related goals & templates

### User-defined related-goal templates
Let users create, save, and share named goal bundles ("Inferno prep",
"Max GWD loot", etc.). Creates multiple goals at once with predefined
targets and tags. Subsumes the "goal import/export" idea — the
templating infrastructure IS the sharing infrastructure.

**Order for this cluster:** build the "add N goals atomically" infra
first (extending `beginCompound`/`endCompound` to cover multi-create
flows cleanly), then use it to power both built-in bundles and
user-authored ones.

### Optional goals (partial)
`Goal.optional` flag exists and is set by prereq seeders for
recommended skills / recommended combat. Not yet honored in section
progress calculation denominators — optional goals should not count
against "6 of 8 complete" totals. Needs a small pass through the
panel's progress-indicator code.

## Release

### Plugin Hub submission
All of: screenshots, a README suitable for the hub listing, a license,
a CHANGELOG, and a PR to `runelite/plugin-hub` with the manifest
entry. Docs are now in place as of v0.1.0; this is the next
user-visible step.

### Stable 1.0 API tag
Before cutting 1.0, commit to:
- Final `AccountMetric` enum values (no more renames)
- Final `BossKillData.BOSSES` naming (new entries OK, renames not OK)
- Public `queryAllTags()` method
- Persistence-format version field + migration dispatch
- Any additional internal APIs promoted to public (probably
  `resolveQuestRequirements`, `resolveDiaryRequirements`)

Cutting 1.0 is a deliberate break: once tagged, method removal is a
major-version bump (2.0).
