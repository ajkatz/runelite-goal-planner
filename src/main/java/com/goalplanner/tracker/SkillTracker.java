package com.goalplanner.tracker;

import com.goalplanner.api.GoalPlannerApiImpl;
import com.goalplanner.model.Goal;
import com.goalplanner.model.GoalType;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Skill;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Tracks skill goals by XP. Target is always stored as XP
 * (converted from level at goal creation time).
 */
@Slf4j
@Singleton
public class SkillTracker extends AbstractTracker
{
	@Inject
	public SkillTracker(Client client, GoalPlannerApiImpl api)
	{
		super(client, api);
	}

	@Override
	protected GoalType targetType()
	{
		return GoalType.SKILL;
	}

	@Override
	protected boolean shouldTrack(Goal goal)
	{
		return goal.getSkillName() != null;
	}

	@Override
	protected int readCurrentValue(Goal goal)
	{
		try
		{
			Skill skill = Skill.valueOf(goal.getSkillName());
			return client.getSkillExperience(skill);
		}
		catch (IllegalArgumentException e)
		{
			log.warn("Unknown skill: {}", goal.getSkillName());
			return -1;
		}
	}
}
