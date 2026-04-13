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

	/** A resolved unlock with its unmet quest, skill, and account prereqs. */
	public static class ResolvedUnlock
	{
		public final String name;
		public final List<Goal> questTemplates;
		public final List<Goal> skillTemplates;
		public final List<Goal> accountTemplates;
		public final int itemId;

		public ResolvedUnlock(String name, List<Goal> questTemplates, int itemId)
		{
			this(name, questTemplates, List.of(), List.of(), itemId);
		}

		public ResolvedUnlock(String name, List<Goal> questTemplates,
			List<Goal> skillTemplates, int itemId)
		{
			this(name, questTemplates, skillTemplates, List.of(), itemId);
		}

		public ResolvedUnlock(String name, List<Goal> questTemplates,
			List<Goal> skillTemplates, List<Goal> accountTemplates, int itemId)
		{
			this.name = name;
			this.questTemplates = questTemplates;
			this.skillTemplates = skillTemplates;
			this.accountTemplates = accountTemplates;
			this.itemId = itemId;
		}
	}

	/** Resolution result. */
	public static class Resolved
	{
		public final List<Goal> templates;
		public final List<ResolvedUnlock> unlocks;
		public final int skippedSkills;
		public final int skippedQuests;

		public Resolved(List<Goal> templates, List<ResolvedUnlock> unlocks,
			int skippedSkills, int skippedQuests)
		{
			this.templates = templates;
			this.unlocks = unlocks;
			this.skippedSkills = skippedSkills;
			this.skippedQuests = skippedQuests;
		}

		public boolean isEmpty()
		{
			return templates.isEmpty() && unlocks.isEmpty();
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
			return new Resolved(List.of(), List.of(), 0, 0);
		}

		List<Goal> templates = new ArrayList<>();
		int skippedSkills = 0;
		int skippedQuests = 0;

		// Sort skills lowest-level-first so the card list reads bottom-to-top
		// (lowest at top = do first, highest at bottom = do last).
		List<DiaryRequirements.SkillReq> sortedSkills = new ArrayList<>(reqs.skills);
		sortedSkills.sort((a, b) -> Integer.compare(a.level, b.level));

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

		// Account metric requirements (e.g. combined Att+Str 130).
		for (DiaryRequirements.AccountReq accountReq : reqs.accountReqs)
		{
			templates.add(Goal.builder()
				.type(GoalType.ACCOUNT)
				.name(accountReq.metricName)
				.accountMetric(accountReq.metricName)
				.targetValue(accountReq.target)
				.build());
		}

		// Boss kills: create BOSS goal templates for unmet kill requirements.
		for (DiaryRequirements.BossReq bossReq : reqs.bossKills)
		{
			templates.add(Goal.builder()
				.type(GoalType.BOSS)
				.name(bossReq.bossName)
				.description(bossReq.killCount + " kills")
				.bossName(bossReq.bossName)
				.targetValue(bossReq.killCount)
				.itemId(BossKillData.getPetItemId(bossReq.bossName))
				.build());
		}

		// Item requirements: create ITEM_GRIND goal templates.
		for (DiaryRequirements.ItemReq itemReq : reqs.itemReqs)
		{
			templates.add(Goal.builder()
				.type(GoalType.ITEM_GRIND)
				.name(itemReq.displayName)
				.itemId(itemReq.itemId)
				.targetValue(itemReq.quantity)
				.build());
		}

		// Unlocks: virtual milestones with their own quest and skill prereq trees.
		// An unlock is considered "met" if ALL its prereq quests are FINISHED
		// and ALL its prereq skills are met.
		List<ResolvedUnlock> resolvedUnlocks = new ArrayList<>();
		for (DiaryRequirements.Unlock unlock : reqs.unlocks)
		{
			boolean allMet = true;
			List<Goal> unlockQuestTemplates = new ArrayList<>();
			for (Quest prereq : unlock.prereqQuests)
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
					continue;
				}
				allMet = false;
				unlockQuestTemplates.add(Goal.builder()
					.type(GoalType.QUEST)
					.name(prereq.getName())
					.description("Quest")
					.questName(prereq.name())
					.targetValue(1)
					.spriteId(QUEST_SPRITE_ID)
					.build());
			}
			List<Goal> unlockSkillTemplates = new ArrayList<>();
			for (DiaryRequirements.SkillReq skillReq : unlock.prereqSkills)
			{
				int currentLevel = skillLevelLookup.applyAsInt(skillReq.skill);
				if (currentLevel >= skillReq.level)
				{
					continue;
				}
				allMet = false;
				int targetXp = Experience.getXpForLevel(skillReq.level);
				unlockSkillTemplates.add(Goal.builder()
					.type(GoalType.SKILL)
					.name(skillReq.skill.getName() + " - Level " + skillReq.level)
					.skillName(skillReq.skill.name())
					.targetValue(targetXp)
					.build());
			}
			List<Goal> unlockAccountTemplates = new ArrayList<>();
			for (DiaryRequirements.AccountReq accountReq : unlock.prereqAccounts)
			{
				// Account reqs are always seeded (no live value check — that
				// happens at tracker runtime via AccountTracker).
				allMet = false;
				unlockAccountTemplates.add(Goal.builder()
					.type(GoalType.ACCOUNT)
					.name(accountReq.metricName)
					.accountMetric(accountReq.metricName)
					.targetValue(accountReq.target)
					.build());
			}
			if (!allMet)
			{
				resolvedUnlocks.add(new ResolvedUnlock(
					unlock.name, unlockQuestTemplates, unlockSkillTemplates,
					unlockAccountTemplates, unlock.itemId));
			}
		}

		return new Resolved(templates, resolvedUnlocks, skippedSkills, skippedQuests);
	}

	private DiaryRequirementResolver() {}
}
