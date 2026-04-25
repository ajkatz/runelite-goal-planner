package com.goalplanner.tracker;

import com.goalplanner.api.GoalPlannerApiImpl;
import com.goalplanner.model.Goal;
import com.goalplanner.model.GoalType;
import net.runelite.api.Client;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Tracks achievement diary completion via varbits set by the game when a
 * diary tier is completed. Two varbit shapes are supported transparently:
 * boolean COMPLETE varbits (targetValue=1) and Karamja count varbits
 * (targetValue=tier task count). Goals with varbitId &lt;= 0 are skipped
 * and stay manual.
 */
@Singleton
public class DiaryTracker extends AbstractTracker
{
	@Inject
	public DiaryTracker(Client client, GoalPlannerApiImpl api)
	{
		super(client, api);
	}

	@Override
	protected GoalType targetType()
	{
		return GoalType.DIARY;
	}

	@Override
	protected boolean shouldTrack(Goal goal)
	{
		return goal.getVarbitId() > 0;
	}

	@Override
	protected int readCurrentValue(Goal goal)
	{
		return Math.max(0, client.getVarbitValue(goal.getVarbitId()));
	}
}
