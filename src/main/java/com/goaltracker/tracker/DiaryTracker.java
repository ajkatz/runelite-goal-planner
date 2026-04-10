package com.goaltracker.tracker;

import com.goaltracker.api.GoalTrackerApiImpl;
import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalType;
import net.runelite.api.Client;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Tracks achievement diary completion via varbits set by the game when a
 * diary tier is completed. Goals with varbitId &lt;= 0 (e.g. Karamja
 * Easy/Medium/Hard) are skipped and stay manual.
 */
@Singleton
public class DiaryTracker extends AbstractTracker
{
	@Inject
	public DiaryTracker(Client client, GoalTrackerApiImpl api)
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
		int varbitValue = client.getVarbitValue(goal.getVarbitId());
		return varbitValue > 0 ? 1 : 0;
	}
}
