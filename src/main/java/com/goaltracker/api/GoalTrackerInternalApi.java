package com.goaltracker.api;

/**
 * Internal-only mutation API. NOT bound publicly via {@code Plugin.configure}.
 * Used by the goal tracker plugin's own UI (panel, right-click handlers) so
 * those code paths go through a single canonical mutation surface, but external
 * consumer plugins cannot reach these methods.
 *
 * <p>Operations exposed here are either:
 * <ul>
 *   <li><b>Layout-coupled</b> — section reorder, where ordering semantics are
 *       coupled to the panel layout (external plugins shouldn't shuffle the
 *       user's UI), or</li>
 *   <li><b>Destructive bulk ops</b> — clear-all, where exposure to external
 *       plugins is too dangerous</li>
 * </ul>
 */
public interface GoalTrackerInternalApi
{
	/**
	 * Move a goal within its current section. The new global index must be within
	 * the goal's current section's bounds (cross-section moves are not supported by
	 * this method — they require explicit section reassignment which is Phase 2).
	 *
	 * @return true if the move happened, false if newGlobalIndex is out of bounds
	 *         or the goal doesn't exist
	 */
	boolean moveGoal(String goalId, int newGlobalIndex);

	/**
	 * Move a goal to a specific position within a section. Combines
	 * {@link #moveGoalToSection} + an in-section reorder. Used by the
	 * Mission 25 contextual Add Goal flow to drop newly-created goals at
	 * the user's chosen anchor point. Position is 0-based within the
	 * section's goal list (0 = top, size = bottom). Position values out
	 * of range are clamped.
	 *
	 * @return true if either the section OR the position changed
	 */
	boolean positionGoalInSection(String goalId, String sectionId, int positionInSection);

	// ---------------------------------------------------------------------
	// Undo / redo (Mission 26)
	// ---------------------------------------------------------------------

	/** True if there is at least one user action available to undo. */
	boolean canUndo();

	/** True if there is at least one undone action available to redo. */
	boolean canRedo();

	/** Short user-facing description of the next undo target, or null if empty. */
	String peekUndoDescription();

	/** Short user-facing description of the next redo target, or null if empty. */
	String peekRedoDescription();

	/**
	 * Reverse the most recent user action. No-op if the undo stack is empty
	 * or the revert fails (the failed entry is silently dropped from history).
	 *
	 * @return true if an action was successfully reverted
	 */
	boolean undo();

	/**
	 * Re-apply the most recently undone user action.
	 *
	 * @return true if an action was successfully re-applied
	 */
	boolean redo();

	/**
	 * Begin a compound command. Subsequent user-mutation API calls collect
	 * into a single undo entry instead of pushing individually. Pair with
	 * {@link #endCompound()} to close. Used by multi-step flows like
	 * "create goal and immediately reposition" where the user expects one
	 * undo to fully reverse the operation. Mission 26.
	 */
	void beginCompound(String description);

	/** Close the current compound and push it as one undo entry. */
	void endCompound();

	/**
	 * Remove all goals from the store. Backs the Clear All UI button. Idempotent.
	 */
	void removeAllGoals();

	/**
	 * Set a section's collapsed state. Backs the section header chevron toggle.
	 *
	 * @return true if the state changed, false if no such section or already in
	 *         the requested state
	 */
	boolean setSectionCollapsed(String sectionId, boolean collapsed);

	/**
	 * Flip a section's collapsed state. Convenience over {@link #setSectionCollapsed}
	 * — callers don't need to know the current state.
	 *
	 * @return the new collapsed state (true = now collapsed, false = now expanded);
	 *         returns the unchanged state if the section doesn't exist
	 */
	boolean toggleSectionCollapsed(String sectionId);

	// ---------------------------------------------------------------------
	// User-defined section CRUD (Phase 2)
	// ---------------------------------------------------------------------

	/**
	 * Create a user-defined section. Inserted at the bottom of the user-section
	 * band, just above the Completed built-in. Idempotent on case-insensitive
	 * name match: returns the existing section's id if a user section with the
	 * same name already exists.
	 *
	 * @param name display name; trimmed; must be non-empty after trim, max 40
	 *             characters, and not collide with built-in names
	 *             ("Incomplete"/"Completed", case-insensitive)
	 * @return section id (newly created or existing)
	 * @throws IllegalArgumentException if the name is invalid
	 */
	String createSection(String name);

	/**
	 * Rename a user-defined section. Built-in sections cannot be renamed.
	 *
	 * @return true if renamed, false on: not found, built-in, invalid name,
	 *         duplicate name, or no-op (same name)
	 */
	boolean renameSection(String sectionId, String newName);

	/**
	 * Delete a user-defined section. All goals in the section are reassigned to
	 * the end of Incomplete (then reconcile may pull completed ones to
	 * Completed). Built-in sections cannot be deleted.
	 *
	 * @return true if deleted, false if not found or built-in
	 */
	boolean deleteSection(String sectionId);

	/**
	 * Reorder a user-defined section to a new position WITHIN the user-section
	 * band. {@code newUserIndex} is 0-based among user sections only; built-ins
	 * are not counted. Out-of-range values are clamped. Built-in sections
	 * cannot be reordered.
	 *
	 * @return true if reordered, false on: not found, built-in, or no-op
	 */
	boolean reorderSection(String sectionId, int newUserIndex);

	/**
	 * Move a goal to a different section. The goal is appended at the end of
	 * the destination section. If the goal is COMPLETE, the move is rejected
	 * unless the destination is the Completed section (reconcile would just
	 * pull it back otherwise).
	 *
	 * @return true if moved, false on: unknown goal id, unknown section id,
	 *         no-op (already in that section), or complete-goal-to-non-completed
	 */
	boolean moveGoalToSection(String goalId, String sectionId);

	/**
	 * Delete all user-defined sections in one shot. Goals belonging to deleted
	 * sections are reassigned to the end of Incomplete (reconcile then pulls
	 * any completed ones to Completed). Built-in sections are preserved.
	 * Idempotent — safe to call when no user sections exist.
	 *
	 * @return number of sections deleted
	 */
	int removeAllUserSections();

	// ---------------------------------------------------------------------
	// Color overrides (Phase 3)
	// ---------------------------------------------------------------------

	/**
	 * Set a section's color override. Works on user-defined and built-in
	 * sections alike. Pass -1 to clear the override and revert to the
	 * neutral default.
	 *
	 * @param colorRgb packed 0xRRGGBB, or -1 to clear the override
	 * @return true if the color changed, false on: not found or no-op
	 */
	boolean setSectionColor(String sectionId, int colorRgb);

	/**
	 * Set a goal's background color override. Works on all goal types —
	 * the override survives rebuilds because it's persisted on the goal model.
	 * Pass -1 to clear the override and revert to the GoalType default color.
	 *
	 * @param colorRgb packed 0xRRGGBB, or -1 to clear the override
	 * @return true if the color changed, false on: not found or no-op
	 */
	boolean setGoalColor(String goalId, int colorRgb);

	/**
	 * Set a tag's color override. Works on default and custom tags alike.
	 * Pass -1 to clear the override and revert to the TagCategory default.
	 *
	 * @param colorRgb packed 0xRRGGBB, or -1 to clear the override
	 * @return true if the color changed, false on: goal or tag not found,
	 *         or no-op
	 */
	boolean setTagColor(String goalId, String tagLabel, int colorRgb);

	// ---------------------------------------------------------------------
	// Tracker write path (Phase 4)
	// ---------------------------------------------------------------------

	/**
	 * Record a new progress value for a goal from a tracker loop.
	 *
	 * <p>Semantics:
	 * <ul>
	 *   <li>If {@code newValue} equals the goal's current value, no-op.</li>
	 *   <li>Otherwise sets {@code currentValue = newValue}.</li>
	 *   <li>If the new value meets target and the goal was NOT complete,
	 *       stamps {@code completedAt} and sets status to COMPLETE.</li>
	 *   <li>If the new value is below target and the goal WAS complete
	 *       (rare — happens on custom toggle or target change), clears
	 *       {@code completedAt} and reverts status to ACTIVE.</li>
	 * </ul>
	 *
	 * <p><b>Does NOT save, reconcile, or fire onGoalsChanged.</b> Trackers
	 * run in batches on each game tick; the plugin's GameTick handler
	 * performs a single {@code goalStore.save() + reconcileCompletedSection()
	 * + panel.rebuild()} once at the end of each tick if anything updated.
	 * Firing the callback per goal would defeat the over-querying cleanup
	 * from Mission 10.
	 *
	 * @return true if the goal was mutated, false if no change
	 */
	boolean recordGoalProgress(String goalId, int newValue);

	// ---------------------------------------------------------------------
	// Selection (Phase 5) — ephemeral, not persisted
	// ---------------------------------------------------------------------

	/**
	 * Replace the current selection with exactly this set of goal ids.
	 * Used by single-click "select only this card" semantics. Pass an empty
	 * collection to clear selection.
	 *
	 * @return true if the selection actually changed
	 */
	boolean replaceGoalSelection(java.util.Collection<String> goalIds);

	/**
	 * Add a single goal id to the current selection (no-op if already in).
	 * Used by cmd/ctrl-click "add to multi-selection" semantics.
	 *
	 * @return true if the goal was newly added
	 */
	boolean addToGoalSelection(String goalId);

	/**
	 * Remove a single goal id from the current selection (no-op if not in).
	 * Used by cmd/ctrl-click on an already-selected card.
	 *
	 * @return true if the goal was removed
	 */
	boolean removeFromGoalSelection(String goalId);

	/**
	 * Clear the entire selection. Used by single-click on an already-selected
	 * card per the click semantics in Mission 15.
	 *
	 * @return true if the selection was non-empty before
	 */
	boolean clearGoalSelection();

	/** @return an unmodifiable snapshot of the currently-selected goal ids. */
	java.util.Set<String> getSelectedGoalIds();

	/**
	 * Add every goal currently in the given section to the selection. Existing
	 * selection is preserved (additive); use {@link #clearGoalSelection()}
	 * first if you want to replace it. Used by the section header right-click
	 * "Select All in Section" entry.
	 *
	 * @return the number of goals newly added to the selection
	 */
	int selectAllInSection(String sectionId);

	/**
	 * Remove every goal currently in the given section from the selection.
	 * Symmetric counterpart to {@link #selectAllInSection(String)}; lets the
	 * section header right-click toggle between select-all and deselect-all
	 * based on whether the section is fully selected.
	 *
	 * @return the number of goals removed from the selection
	 */
	int deselectAllInSection(String sectionId);

	/**
	 * Add a tag with an explicit category. The public {@link GoalTrackerApi#addTag}
	 * forces every external-API tag to {@link com.goaltracker.model.TagCategory#OTHER}
	 * because external consumers don't have a stable contract for the category enum.
	 * The plugin's own UI needs to preserve user-picked categories from the Add Tag
	 * dialog (BOSS/RAID/CLUE/etc), so it uses this internal variant.
	 *
	 * @param goalId target goal id
	 * @param label tag label; trimmed; must be non-empty
	 * @param categoryName one of the {@link com.goaltracker.model.TagCategory} enum names
	 * @return true if the tag was added, false on: not found, blank label,
	 *         unknown category
	 */
	boolean addTagWithCategory(String goalId, String label, String categoryName);

	// ---------------------------------------------------------------------
	// Tag entity CRUD (Mission 19)
	// ---------------------------------------------------------------------

	/**
	 * Snapshot of every tag in the store. Includes both system and user tags
	 * across all categories. Used by the tag management UI.
	 */
	java.util.List<TagView> queryAllTags();

	/**
	 * Free-text search across goals. Case-insensitive partial substring match
	 * with OR semantics across these fields:
	 * <ul>
	 *   <li>Goal name and description</li>
	 *   <li>Any tag label on the goal</li>
	 *   <li>Tag category display names of any tag on the goal</li>
	 *   <li>{@link com.goaltracker.model.GoalType#getDisplayName()}</li>
	 *   <li>The title of the section the goal belongs to</li>
	 * </ul>
	 *
	 * <p>An empty or null query returns every goal in canonical order — this
	 * matches {@link #queryAllGoals()} and lets the UI use one render path.
	 *
	 * @param query free-text search; null/blank returns all goals
	 * @return filtered list of GoalViews in canonical order
	 */
	java.util.List<GoalView> searchGoals(String query);

	// ---------------------------------------------------------------------
	// Bulk multi-selection actions (Mission 24)
	// ---------------------------------------------------------------------

	/**
	 * True if the goal has diverged from its defaults — either its tagIds set
	 * differs from defaultTagIds, or it has a custom color override. Used to
	 * gate the (single + bulk) Restore Defaults menu items.
	 */
	boolean isGoalOverridden(String goalId);

	/**
	 * Reset every eligible goal in the selection back to its defaults: tagIds
	 * = defaultTagIds, customColorRgb = -1. Ineligible goals (none of those
	 * are overridden) are skipped silently. Fires a single onGoalsChanged at
	 * the end. Mission 24.
	 *
	 * @return number of goals actually changed
	 */
	int bulkRestoreDefaults(java.util.Set<String> goalIds);

	/**
	 * Remove a tag from every selected goal where it is both present and
	 * removable. CUSTOM goals can drop any tag; non-CUSTOM goals can only
	 * drop tags NOT in defaultTagIds. Skips silently otherwise. Fires a
	 * single onGoalsChanged. Mission 24.
	 *
	 * @return number of goals from which the tag was removed
	 */
	int bulkRemoveTagFromGoals(java.util.Set<String> goalIds, String tagId);

	/**
	 * Remove a batch of goals as a single atomic command. Unlike calling
	 * {@link #removeGoal} in a compound loop, this captures every goal's
	 * original priority BEFORE any removals so undo restores them to their
	 * exact original positions (no collapsing at an intermediate index).
	 * Mission 26 follow-up.
	 *
	 * @return number of goals actually removed
	 */
	int bulkRemoveGoals(java.util.Set<String> goalIds);

	/**
	 * Move a batch of goals into the same target section as a single atomic
	 * command. Captures every goal's original section + priority upfront so
	 * undo restores them to their exact original positions. Mission 26
	 * follow-up.
	 *
	 * @return number of goals actually moved
	 */
	int bulkMoveGoalsToSection(java.util.Set<String> goalIds, String targetSectionId);

	/**
	 * For a set of selected goals, return every removable tag (deduped) with
	 * a count of how many goals in the selection have it (and where it's
	 * removable). Sorted by count descending then by label ascending. Used
	 * to populate the bulk Remove Tag dropdown. Mission 24.
	 */
	java.util.List<TagRemovalOption> getRemovableTagsForSelection(java.util.Set<String> goalIds);

	/**
	 * Lightweight DTO returned by {@link #getRemovableTagsForSelection}.
	 * Mission 24.
	 */
	final class TagRemovalOption
	{
		public final String tagId;
		public final String label;
		public final String category;
		public final int count;

		public TagRemovalOption(String tagId, String label, String category, int count)
		{
			this.tagId = tagId;
			this.label = label;
			this.category = category;
			this.count = count;
		}
	}

	/**
	 * Create a user tag (idempotent on case-insensitive label+category match).
	 *
	 * @return tag id (newly created or existing)
	 * @throws IllegalArgumentException if label is invalid
	 */
	String createUserTag(String label, String categoryName);

	/**
	 * Rename a tag entity. Affects every goal that references it. System tags
	 * cannot be renamed (returns false).
	 *
	 * @return true if renamed, false on: not found, system tag, invalid name,
	 *         duplicate name, or no-op
	 */
	boolean renameTag(String tagId, String newLabel);

	/**
	 * Recolor a tag entity. Affects every goal that references it. System tags
	 * in the SKILLING category are fully read-only (skill icons); other system
	 * tags can be recolored. Pass -1 to clear the override.
	 *
	 * @return true if changed, false on: not found, read-only, or no-op
	 */
	boolean recolorTag(String tagId, int colorRgb);

	/**
	 * Delete a tag entity. Cascades to remove the reference from every goal.
	 * System tags cannot be deleted (returns false) — they're auto-attached
	 * by goal creation flows.
	 *
	 * @return true if deleted, false on: not found or system tag
	 */
	boolean deleteTag(String tagId);

	/**
	 * Set a per-category color override (Mission 20). Affects every tag in
	 * the given category. SKILLING is read-only (returns false) — skill icon
	 * tags ignore the category color anyway.
	 *
	 * @param categoryName one of the TagCategory enum names (excluding SKILLING)
	 * @param colorRgb packed 0xRRGGBB; -1 to clear the override and revert
	 *                 to the TagCategory default
	 * @return true if the color changed
	 */
	boolean setCategoryColor(String categoryName, int colorRgb);

	/** Equivalent to {@link #setCategoryColor(String, int)} with -1. */
	boolean resetCategoryColor(String categoryName);

	/** Current packed RGB for a category (override if set, else enum default). */
	int getCategoryColor(String categoryName);

	/** Enum default packed RGB for a category. */
	int getCategoryDefaultColor(String categoryName);

	/** True if the category has a user override set. */
	boolean isCategoryColorOverridden(String categoryName);

	/**
	 * Set an icon on a tag (Mission 21). Works on any tag including system tags.
	 * Pass null or empty to clear. The iconKey is resolved at render time:
	 * Skill enum names go to SkillIconManager; everything else looks up
	 * {@code /icons/<key>.png} from the bundled classpath resources. Icons
	 * entirely replace the colored pill rendering.
	 *
	 * @return true if the icon changed
	 */
	boolean setTagIcon(String tagId, String iconKey);

	/** Equivalent to {@link #setTagIcon(String, String)} with null. */
	boolean clearTagIcon(String tagId);

	// =====================================================================
	// Relations — Mission 30
	// Goal-to-goal "requires" edges forming a DAG. Relations cross sections
	// freely; topo sort is per-section (session 2).
	// =====================================================================

	/**
	 * Add an edge: {@code fromGoalId} requires {@code toGoalId}. Rejects
	 * self-loops, missing goals, duplicates, and any edge that would
	 * create a cycle. Undoable.
	 *
	 * @return true if the edge was added
	 */
	boolean addRequirement(String fromGoalId, String toGoalId);

	/**
	 * Remove an existing {@code fromGoalId → toGoalId} edge. Undoable.
	 *
	 * @return true if the edge existed and was removed
	 */
	boolean removeRequirement(String fromGoalId, String toGoalId);

	/**
	 * Get the goals that {@code goalId} requires (outgoing edges).
	 */
	java.util.List<String> getRequirements(String goalId);

	/**
	 * Get the goals that require {@code goalId} (incoming edges, derived
	 * by scanning all goals).
	 */
	java.util.List<String> getDependents(String goalId);

	/**
	 * Return value from {@link #findOrCreateRequirement}. {@code goalId} is
	 * the id of the goal that now satisfies the template (either an
	 * existing one that matched structurally, or a newly-created seed).
	 * {@code wasCreated} distinguishes the two cases so callers can build
	 * the correct undo inverse — link-only if false, delete-seed-plus-link
	 * if true.
	 */
	final class FindOrCreateResult
	{
		public final String goalId;
		public final boolean wasCreated;

		public FindOrCreateResult(String goalId, boolean wasCreated)
		{
			this.goalId = goalId;
			this.wasCreated = wasCreated;
		}
	}

	/**
	 * Return the goals in the given section sorted topologically by their
	 * relation DAG, with priority as a tiebreaker within each topological
	 * tier. Leaves (goals with no in-section requirements) come first;
	 * their dependents follow after.
	 *
	 * <p>Only edges where BOTH endpoints are in the section are considered
	 * for ordering — cross-section requirements are still visible in the
	 * hover tooltip but don't affect the sort. This matches the design
	 * decision from the relation Q&amp;A: topo sort is per-section.
	 *
	 * <p>If the graph contains a cycle (shouldn't happen post-scrub), the
	 * remaining goals are emitted in priority order as a fallback so the
	 * panel still renders.
	 *
	 * @param sectionId the section to sort
	 * @return ordered list of GoalViews, or empty list if section is unknown
	 */
	java.util.List<com.goaltracker.api.GoalView> queryGoalsTopologicallySorted(String sectionId);

	/**
	 * Resolve a requirement template to a concrete goal id, creating a
	 * seed goal if no existing goal satisfies the template structurally.
	 *
	 * <p>"Satisfies" uses the per-type rules in
	 * {@link com.goaltracker.persistence.GoalStore#findMatchingGoal}: skill
	 * and item goals use numeric "target ≥" comparison; quests/diaries/CAs/
	 * custom use case-insensitive name equality (CAs prefer caTaskId).
	 *
	 * <p>If a new seed is created, it's marked {@code autoSeeded=true} for
	 * the future absorption rule (session 2) and added to the store in the
	 * specified section (or Incomplete if {@code preferredSectionId} is
	 * null). Undoable — link-only on existing match, or compound (create +
	 * link) on no-match.
	 *
	 * @param template           a Goal object describing WHAT the caller needs;
	 *                           only identity + target fields are consulted
	 * @param preferredSectionId section for the seed goal if created, or null
	 *                           for Incomplete
	 * @return the resolved goal id + whether it was newly created, or null
	 *         if the template is invalid
	 */
	FindOrCreateResult findOrCreateRequirement(
		com.goaltracker.model.Goal template, String preferredSectionId);
}
