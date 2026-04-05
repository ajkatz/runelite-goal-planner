package com.goaltracker.tracker;

import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalStatus;
import com.goaltracker.model.GoalType;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Tracks quest completion via Quest.getState(client).
 * Quest goals are binary: not started/in progress → complete.
 */
@Slf4j
@Singleton
public class QuestTracker
{
	private final Client client;

	@Inject
	public QuestTracker(Client client)
	{
		this.client = client;
	}

	/**
	 * Update all quest goals with current state from the game.
	 * Returns true if any goal was updated.
	 */
	public boolean checkGoals(List<Goal> goals)
	{
		boolean anyUpdated = false;

		for (Goal goal : goals)
		{
			if (goal.getType() != GoalType.QUEST || goal.getStatus() != GoalStatus.ACTIVE)
			{
				continue;
			}

			if (goal.getQuestName() == null)
			{
				continue;
			}

			try
			{
				Quest quest = Quest.valueOf(goal.getQuestName());
				QuestState state = quest.getState(client);

				int newValue = state == QuestState.FINISHED ? 1 : 0;

				if (newValue != goal.getCurrentValue())
				{
					goal.setCurrentValue(newValue);
					anyUpdated = true;

					if (goal.isComplete())
					{
						goal.setStatus(GoalStatus.COMPLETE);
						goal.setCompletedAt(System.currentTimeMillis());
						log.info("Quest goal complete: {}", goal.getName());
					}
				}
			}
			catch (IllegalArgumentException e)
			{
				log.warn("Unknown quest: {}", goal.getQuestName());
			}
		}

		return anyUpdated;
	}
}
