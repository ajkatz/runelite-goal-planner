package com.goalplanner.tracker;

import com.goalplanner.api.GoalPlannerApiImpl;
import com.goalplanner.model.AccountMetric;
import com.goalplanner.model.Goal;
import com.goalplanner.model.GoalType;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
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
	public AccountTracker(Client client, GoalPlannerApiImpl api)
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
		// Leagues-specific metrics only have meaningful values on leagues accounts.
		// On main worlds the varbits return 0, which would otherwise overwrite
		// prior leagues progress. Return -1 (skip) so the tracker leaves the
		// stored value alone. The profile-scoped store also keeps these goals
		// out of the main profile, so this is a belt-and-suspenders check.
		if (metric.isLeagues())
		{
			int leagueAccount = client.getVarbitValue(VarbitID.LEAGUE_ACCOUNT);
			boolean seasonal = client.getWorldType() != null
				&& client.getWorldType().contains(net.runelite.api.WorldType.SEASONAL);
			if (leagueAccount == 0 && !seasonal) return -1;
		}

		// The per-metric live reads live on AccountMetric (shared with the
		// requirement resolvers); the leagues guard above is tracker policy.
		return metric.currentValue(client);
	}
}
