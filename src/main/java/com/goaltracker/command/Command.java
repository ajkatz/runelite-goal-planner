package com.goaltracker.command;

/**
 * A reversible user action on the goal tracker (Mission 26).
 *
 * <p>Commands carry the state needed to revert themselves at construction
 * time — typically a snapshot of "before" values plus the parameters of the
 * action — so applying the command does not depend on any external state
 * that may have changed by the time {@link #revert()} runs.
 *
 * <p>Both {@link #apply()} and {@link #revert()} return a boolean. False
 * means "the operation failed at the store level" (e.g. the entity it was
 * trying to mutate is gone). The {@code CommandHistory} treats a false
 * return on revert as a failed undo: it logs the failure and drops the
 * entry from history rather than crashing or rolling further back.
 *
 * <p>{@link #getDescription()} is a short user-facing string for the
 * undo/redo button tooltip ("Undo: Add goal 'Vorkath KC'").
 *
 * <p>Tracker-driven mutations (XP gain, quest tick, item count change) do
 * NOT go through Command — they call the underlying store mutation
 * primitives directly so they never appear in undo history.
 */
public interface Command
{
	/** Run the action. Returns true if it actually changed state. */
	boolean apply();

	/** Reverse the action. Returns true if the revert succeeded. */
	boolean revert();

	/** Short user-facing label, e.g. "Add goal 'Vorkath KC'". */
	String getDescription();
}
