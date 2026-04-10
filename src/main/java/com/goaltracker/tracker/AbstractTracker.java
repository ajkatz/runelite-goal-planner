package com.goaltracker.tracker;

import com.goaltracker.api.GoalTrackerApiImpl;
import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalStatus;
import com.goaltracker.model.GoalType;
import java.util.List;
import net.runelite.api.Client;

/**
 * Base class for goal trackers. Provides the shared loop that filters
 * goals by type and status, reads the current value from the game
 * client, and records progress via the API. Subclasses implement only
 * the value-extraction logic.
 */
public abstract class AbstractTracker
{
	protected final Client client;
	protected final GoalTrackerApiImpl api;

	protected AbstractTracker(Client client, GoalTrackerApiImpl api)
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

		for (Goal goal : goals)
		{
			if (goal.getType() != targetType() || goal.getStatus() != GoalStatus.ACTIVE)
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
