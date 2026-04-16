package com.goalplanner.tracker;

import com.goalplanner.api.GoalPlannerApiImpl;
import com.goalplanner.model.AccountMetric;
import com.goalplanner.model.Goal;
import com.goalplanner.model.GoalType;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Skill;
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
			case ATT_STR_COMBINED:
				return client.getRealSkillLevel(Skill.ATTACK)
					+ client.getRealSkillLevel(Skill.STRENGTH);
			case MISC_APPROVAL:
				return client.getVarbitValue(VarbitID.MISC_APPROVAL);
			case TOG_MAX_TEARS:
				return client.getVarbitValue(VarbitID.TOG_MAX_TEARS_COLLECTED);
			case CHOMPY_KILLS:
				return client.getVarpValue(VarPlayerID.CHOMPYBIRD);
			case COLOSSEUM_GLORY:
				return client.getVarpValue(VarPlayerID.COLOSSEUM_GLORY);
			case DOM_DEEPEST_LEVEL:
				return client.getVarpValue(VarPlayerID.DOM_DEEPEST_LEVEL);
			case LEAGUE_POINTS:
				// Lifetime league points earned from task completion. Spending
				// currency (LEAGUE_POINTS_CURRENCY = 2613) does not reduce this.
				return client.getVarpValue(VarPlayerID.LEAGUE_POINTS_COMPLETED);
			case LEAGUE_TASKS:
				return client.getVarbitValue(VarbitID.LEAGUE_TOTAL_TASKS_COMPLETED);
			default:
				return -1;
		}
	}
}
