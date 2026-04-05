package com.goaltracker.tracker;

import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalStatus;
import com.goaltracker.model.GoalType;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Experience;
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

	@Inject
	public SkillTracker(Client client)
	{
		this.client = client;
	}

	/**
	 * Update all skill goals with current XP from the game.
	 * Returns true if any goal was updated.
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

				if (currentXp != goal.getCurrentValue())
				{
					goal.setCurrentValue(currentXp);
					anyUpdated = true;

					if (goal.isComplete())
					{
						goal.setStatus(GoalStatus.COMPLETE);
						goal.setCompletedAt(System.currentTimeMillis());
						log.info("Goal complete: {}", goal.getName());
					}
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
