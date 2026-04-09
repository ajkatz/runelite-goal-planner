package com.goaltracker.tracker;

import com.goaltracker.api.GoalTrackerApiImpl;
import com.goaltracker.model.AccountMetric;
import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalStatus;
import com.goaltracker.model.GoalType;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Tracks account-wide goals: quest points, combat level, total level,
 * slayer points, museum kudos, CA tasks completed, and music tracks.
 */
@Slf4j
@Singleton
public class AccountTracker
{
	private final Client client;
	private final GoalTrackerApiImpl api;

	@Inject
	public AccountTracker(Client client, GoalTrackerApiImpl api)
	{
		this.client = client;
		this.api = api;
	}

	public boolean checkGoals(List<Goal> goals)
	{
		boolean anyUpdated = false;

		for (Goal goal : goals)
		{
			if (goal.getType() != GoalType.ACCOUNT || goal.getStatus() != GoalStatus.ACTIVE)
			{
				continue;
			}
			if (goal.getAccountMetric() == null)
			{
				continue;
			}

			AccountMetric metric;
			try
			{
				metric = AccountMetric.valueOf(goal.getAccountMetric());
			}
			catch (IllegalArgumentException e)
			{
				log.warn("Unknown account metric: {}", goal.getAccountMetric());
				continue;
			}

			int currentValue = readMetric(metric);
			if (currentValue >= 0 && api.recordGoalProgress(goal.getId(), currentValue))
			{
				anyUpdated = true;
			}
		}

		return anyUpdated;
	}

	/**
	 * Read the current value for an account metric from the game client.
	 * Returns -1 if the value cannot be read (e.g., not logged in).
	 */
	private int readMetric(AccountMetric metric)
	{
		switch (metric)
		{
			case QUEST_POINTS:
				return client.getVarpValue(VarPlayerID.QP);

			case COMBAT_LEVEL:
			{
				Player local = client.getLocalPlayer();
				return local != null ? local.getCombatLevel() : -1;
			}

			case TOTAL_LEVEL:
				return client.getTotalLevel();

			case CA_POINTS:
				return client.getVarbitValue(VarbitID.CA_POINTS);

			case SLAYER_POINTS:
				return client.getVarbitValue(VarbitID.SLAYER_POINTS);

			case KUDOS:
				return client.getVarbitValue(VarbitID.VM_KUDOS);

			default:
				return -1;
		}
	}

}
