package com.goaltracker.tracker;

import com.goaltracker.api.GoalTrackerApiImpl;
import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalType;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Tracks quest completion via Quest.getState(client).
 * Quest goals are binary: not started/in progress -> complete.
 */
@Slf4j
@Singleton
public class QuestTracker extends AbstractTracker
{
	@Inject
	public QuestTracker(Client client, GoalTrackerApiImpl api)
	{
		super(client, api);
	}

	@Override
	protected GoalType targetType()
	{
		return GoalType.QUEST;
	}

	@Override
	protected boolean shouldTrack(Goal goal)
	{
		return goal.getQuestName() != null;
	}

	@Override
	protected int readCurrentValue(Goal goal)
	{
		try
		{
			Quest quest = Quest.valueOf(goal.getQuestName());
			QuestState state = quest.getState(client);
			return state == QuestState.FINISHED ? 1 : 0;
		}
		catch (IllegalArgumentException e)
		{
			log.warn("Unknown quest: {}", goal.getQuestName());
			return -1;
		}
	}
}
