# Decisions

Significant architectural and design decisions, newest first.

## 2026-06-11: GPSHARE2 carries cross-section dependency edges

**Decision:** The GPSHARE2 (multi-section) share-code wire format carries
dependency edges between goals in different sections via a bundle-level
`crossEdges` list — `{fromSection, fromRef, toSection, toRef, or}` entries,
where section values index the bundle's `sections` list and refs are the
per-section goal refs. Per-goal `requires`/`orRequires` ref lists remain
section-scoped. The field was added to v2 in place (no v3), since zero
GPSHARE2 codes existed outside local testing at decision time. The MCP
server (`goalplanner-share-mcp`) must emit/decode the same field.

**Alternatives considered:**
- *Section-scoped refs only, drop cross-section edges with an export-time
  warning* — rejected: sharing a multi-select that spans sections preserves
  the source sections (user requirement), which makes cross-section edges
  common in real exports; silently or even loudly losing plan structure is
  data loss the recipient can't recover.
- *Make all refs bundle-global instead of section-scoped* — rejected:
  restructures every existing field's semantics, breaks the property that a
  section entry is understandable standalone, and touches far more
  encode/decode/test surface than an additive field.
- *Defer to a future GPSHARE3* — rejected: once codes circulate (planned
  request-share-codes Discord channel), the format is frozen by archived
  messages; a v3 migration later costs far more than an additive field now.

**Rationale:** The format-freeze moment is the public release, not the code
change — this was the last cheap opportunity. The additive shape keeps v1
untouched, keeps section entries self-contained, and lets older v2 decoders
(none exist) degrade by ignoring the field. Import resolves the edges in a
third pass through per-section ref maps and tracks them in the one-undo
journal like all imported relations.

**Context:** A user bug report ("goals selected across two sections imported
as one combined section") led to per-section selection export, which made
the cross-section edge gap acute: the fix preserved sections but initially
dropped the edges between them. Implemented in commit 3a0790e for the 0.3.0
release; the wire is considered frozen once 0.3.0 ships.
