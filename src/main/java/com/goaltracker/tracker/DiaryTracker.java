package com.goaltracker.tracker;

import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalStatus;
import com.goaltracker.model.GoalType;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Tracks achievement diary completion via varbits set by the game when a
 * diary tier is completed. The varbit ID for each (area, tier) is stored on
 * the Goal itself at creation time (see GoalTrackerPlugin.buildDiaryGoal),
 * so this tracker just polls each goal's stored varbit.
 *
 * Diary goals with varbitId == 0 (e.g. Karamja Easy/Medium/Hard, which lack
 * a named completion varbit in runelite-api) are skipped and stay manual.
 */
@Slf4j
@Singleton
public class DiaryTracker
{
	private final Client client;

	@Inject
	public DiaryTracker(Client client)
	{
		this.client = client;
	}

	/**
	 * Update all diary goals with current state from the game.
	 * Returns true if any goal was updated.
	 */
	public boolean checkGoals(List<Goal> goals)
	{
		boolean anyUpdated = false;

		for (Goal goal : goals)
		{
			if (goal.getType() != GoalType.DIARY || goal.getStatus() != GoalStatus.ACTIVE)
			{
				continue;
			}

			int varbitId = goal.getVarbitId();
			if (varbitId <= 0)
			{
				// No tracking varbit (e.g. Karamja E/M/H) — leave manual.
				continue;
			}

			int varbitValue = client.getVarbitValue(varbitId);
			int newValue = varbitValue > 0 ? 1 : 0;

			if (newValue != goal.getCurrentValue())
			{
				goal.setCurrentValue(newValue);
				anyUpdated = true;

				if (goal.meetsTarget() && !goal.isComplete())
				{
					goal.setCompletedAt(System.currentTimeMillis());
					goal.setStatus(GoalStatus.COMPLETE);
					log.info("Diary goal complete: {}", goal.getName());
				}
			}
		}

		return anyUpdated;
	}
}
