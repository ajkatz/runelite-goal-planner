package com.goalplanner.tracker;

import com.goalplanner.api.GoalPlannerApiImpl;
import com.goalplanner.data.BossKillData;
import com.goalplanner.model.Goal;
import com.goalplanner.model.GoalType;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Tracks boss kill count goals by reading VarPlayerID values.
 * Each boss maps to a specific VarPlayerID via {@link BossKillData}.
 */
@Slf4j
@Singleton
public class BossKillTracker extends AbstractTracker
{
	@Inject
	public BossKillTracker(Client client, GoalPlannerApiImpl api)
	{
		super(client, api);
	}

	@Override
	protected GoalType targetType()
	{
		return GoalType.BOSS;
	}

	@Override
	protected boolean shouldTrack(Goal goal)
	{
		return goal.getBossName() != null
			&& BossKillData.isKnownBoss(goal.getBossName());
	}

	@Override
	protected int readCurrentValue(Goal goal)
	{
		int varpId = BossKillData.getVarpId(goal.getBossName());
		if (varpId < 0) return -1;
		return client.getVarpValue(varpId);
	}
}
