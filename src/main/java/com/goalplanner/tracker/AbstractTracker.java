package com.goalplanner.tracker;

import com.goalplanner.api.GoalPlannerApiImpl;
import com.goalplanner.model.Goal;
import com.goalplanner.model.GoalStatus;
import com.goalplanner.model.GoalType;
import java.util.List;
import net.runelite.api.Client;

/**
 * Base class for goal trackers. Provides the shared loop that filters
 * goals by type and status, reads the current value from the game
 * client, and records progress via the API. Subclasses implement only
 * the value-extraction logic.
 *
 * <p>Leagues/main goal isolation is handled at the persistence layer
 * via profile-scoped storage, not here — when on a leagues world, only
 * leagues-profile goals are loaded into memory, so this tracker only
 * ever sees the goals relevant to the current world.
 */
public abstract class AbstractTracker
{
	protected final Client client;
	protected final GoalPlannerApiImpl api;

	protected AbstractTracker(Client client, GoalPlannerApiImpl api)
	{
		this.client = client;
		this.api = api;
	}

	/** The goal type this tracker handles. */
	protected abstract GoalType targetType();

	/**
	 * Read the current value for a goal from the game client.
	 * Return -1 to skip this goal (e.g. unreadable state).
	 */
	protected abstract int readCurrentValue(Goal goal);

	/**
	 * Extra skip logic beyond type and status filtering.
	 * Override to add guards (e.g. null field checks).
	 * Default: track everything of the right type.
	 */
	protected boolean shouldTrack(Goal goal)
	{
		return true;
	}

	/**
	 * Check all goals of this tracker's type and update progress.
	 * Returns true if any goal was updated.
	 */
	public boolean checkGoals(List<Goal> goals)
	{
		boolean anyUpdated = false;

		// Snapshot to avoid ConcurrentModificationException if the list
		// is modified by a compound transaction on another thread.
		//
		// We intentionally do NOT skip goals with status=COMPLETE here:
		// completion is a derived property of (currentValue vs targetValue),
		// and api.recordGoalProgress handles both directions — it will flip
		// a complete goal back to ACTIVE when the current value drops below
		// target. Skipping completed goals would freeze the wrong status
		// after a profile switch, manual edit, or any event that causes the
		// backing varbit/xp/kill-count to differ from what completed it.
		//
		// ItemTracker overrides this method to keep ITEM_GRIND terminal on
		// complete (intentional, documented in its javadoc).
		List<Goal> snapshot = new java.util.ArrayList<>(goals);
		for (Goal goal : snapshot)
		{
			if (goal.getType() != targetType()) continue;
			// Skip goals that are explicitly archived/dismissed rather than
			// merely complete. If future statuses are added, be explicit.
			if (goal.getStatus() != GoalStatus.ACTIVE && goal.getStatus() != GoalStatus.COMPLETE)
			{
				continue;
			}

			if (!shouldTrack(goal))
			{
				continue;
			}

			int currentValue = readCurrentValue(goal);
			if (currentValue >= 0 && api.recordGoalProgress(goal.getId(), currentValue))
			{
				anyUpdated = true;
			}
		}

		return anyUpdated;
	}
}
