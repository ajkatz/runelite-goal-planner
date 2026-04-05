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
 * Tracks skill XP/level goals against the current game state.
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
	 * Update all skill goals with current values from the game.
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
				int currentLevel = client.getRealSkillLevel(skill);

				// Determine current value based on whether target is XP or level
				int currentValue;
				if (goal.getTargetValue() > 99)
				{
					// XP-based goal (targets over 99 are XP amounts)
					currentValue = currentXp;
				}
				else
				{
					// Level-based goal
					currentValue = currentLevel;
				}

				if (currentValue != goal.getCurrentValue())
				{
					goal.setCurrentValue(currentValue);
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
