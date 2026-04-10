package com.goaltracker.tracker;

import com.goaltracker.api.GoalTrackerApiImpl;
import com.goaltracker.model.AccountMetric;
import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalType;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Tracks account-wide goals: quest points, combat level, total level,
 * CA points, slayer points, museum kudos.
 */
@Slf4j
@Singleton
public class AccountTracker extends AbstractTracker
{
	@Inject
	public AccountTracker(Client client, GoalTrackerApiImpl api)
	{
		super(client, api);
	}

	@Override
	protected GoalType targetType()
	{
		return GoalType.ACCOUNT;
	}

	@Override
	protected boolean shouldTrack(Goal goal)
	{
		return goal.getAccountMetric() != null;
	}

	@Override
	protected int readCurrentValue(Goal goal)
	{
		AccountMetric metric;
		try
		{
			metric = AccountMetric.valueOf(goal.getAccountMetric());
		}
		catch (IllegalArgumentException e)
		{
			log.warn("Unknown account metric: {}", goal.getAccountMetric());
			return -1;
		}
		return readMetric(metric);
	}

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
