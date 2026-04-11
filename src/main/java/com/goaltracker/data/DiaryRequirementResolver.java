package com.goaltracker.data;

import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import net.runelite.api.Client;
import net.runelite.api.Experience;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;

/**
 * Resolves diary tier requirements against live player state, producing
 * goal templates for unmet requirements. Parallel to
 * {@link QuestRequirementResolver} but for achievement diaries.
 */
public final class DiaryRequirementResolver
{
	private static final int QUEST_SPRITE_ID = 899;

	/** Resolution result. */
	public static class Resolved
	{
		public final List<Goal> templates;
		public final int skippedSkills;
		public final int skippedQuests;

		public Resolved(List<Goal> templates, int skippedSkills, int skippedQuests)
		{
			this.templates = templates;
			this.skippedSkills = skippedSkills;
			this.skippedQuests = skippedQuests;
		}

		public boolean isEmpty()
		{
			return templates.isEmpty();
		}
	}

	/** Production overload: resolve against a live Client. */
	public static Resolved resolve(String area, AchievementDiaryData.Tier tier, Client client)
	{
		return resolve(
			area, tier,
			client::getRealSkillLevel,
			q ->
			{
				try
				{
					return q.getState(client);
				}
				catch (Exception e)
				{
					return null;
				}
			});
	}

	/**
	 * Core overload with injected lookups (testable without a Client).
	 */
	public static Resolved resolve(
		String area,
		AchievementDiaryData.Tier tier,
		ToIntFunction<Skill> skillLevelLookup,
		Function<Quest, QuestState> questStateLookup)
	{
		DiaryRequirements.Reqs reqs = DiaryRequirements.lookup(area, tier);
		if (reqs == null)
		{
			return new Resolved(List.of(), 0, 0);
		}

		List<Goal> templates = new ArrayList<>();
		int skippedSkills = 0;
		int skippedQuests = 0;

		// Sort skills highest-level-first for queue priority.
		List<DiaryRequirements.SkillReq> sortedSkills = new ArrayList<>(reqs.skills);
		sortedSkills.sort((a, b) -> Integer.compare(b.level, a.level));

		for (DiaryRequirements.SkillReq req : sortedSkills)
		{
			int currentLevel = skillLevelLookup.applyAsInt(req.skill);
			if (currentLevel >= req.level)
			{
				skippedSkills++;
				continue;
			}
			int targetXp = Experience.getXpForLevel(req.level);
			templates.add(Goal.builder()
				.type(GoalType.SKILL)
				.name(req.skill.getName() + " - Level " + req.level)
				.skillName(req.skill.name())
				.targetValue(targetXp)
				.build());
		}

		for (Quest prereq : reqs.prereqQuests)
		{
			QuestState state;
			try
			{
				state = questStateLookup.apply(prereq);
			}
			catch (Exception e)
			{
				state = null;
			}
			if (state == QuestState.FINISHED)
			{
				skippedQuests++;
				continue;
			}
			templates.add(Goal.builder()
				.type(GoalType.QUEST)
				.name(prereq.getName())
				.description("Quest")
				.questName(prereq.name())
				.targetValue(1)
				.spriteId(QUEST_SPRITE_ID)
				.build());
		}

		return new Resolved(templates, skippedSkills, skippedQuests);
	}

	private DiaryRequirementResolver() {}
}
