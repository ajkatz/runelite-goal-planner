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
}
