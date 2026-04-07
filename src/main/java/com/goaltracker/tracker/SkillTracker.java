package com.goaltracker.tracker;

import com.goaltracker.api.GoalTrackerApiImpl;
import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalStatus;
import com.goaltracker.model.GoalType;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Skill;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Tracks skill goals by XP. Target is always stored as XP
 * (converted from level at goal creation time).
 */
@Slf4j
@Singleton
public class SkillTracker
{
	private final Client client;
	private final GoalTrackerApiImpl api;

	@Inject
	public SkillTracker(Client client, GoalTrackerApiImpl api)
	{
		this.client = client;
		this.api = api;
	}

	/**
	 * Update all skill goals with current XP from the game.
	 * Returns true if any goal was updated. Mutations route through
	 * {@link GoalTrackerApiImpl#recordGoalProgress(String, int)} which does
	 * NOT save or fire the panel rebuild callback — the plugin's GameTick
	 * handler flushes once per tick.
	 */
	public boolean checkGoals(List<Goal> goals)
	{
		boolean anyUpdated = false;

		for (Goal goal : goals)
		{
			if (goal.getType() != GoalType.SKILL || goal.getStatus() != GoalStatus.ACTIVE)
			{
				continue;
			}

			if (goal.getSkillName() == null)
			{
				continue;
			}

			try
			{
				Skill skill = Skill.valueOf(goal.getSkillName());
				int currentXp = client.getSkillExperience(skill);
				if (api.recordGoalProgress(goal.getId(), currentXp))
				{
					anyUpdated = true;
				}
			}
			catch (IllegalArgumentException e)
			{
				log.warn("Unknown skill: {}", goal.getSkillName());
			}
		}

		return anyUpdated;
	}
}
