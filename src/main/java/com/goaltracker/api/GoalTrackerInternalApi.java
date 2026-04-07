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
}
