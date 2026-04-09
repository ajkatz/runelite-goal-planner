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
 * Resolves a {@link QuestRequirements.Reqs} entry into concrete goal
 * templates, filtering out any requirement the player already meets.
 *
 * <p>Split from {@code GoalTrackerApiImpl} so the API layer stays free
 * of {@link Client} dependencies — the API receives pre-filtered
 * templates and doesn't know or care about live player state. The
 * resolver lives next to {@link QuestRequirements} because its output
 * shape is dictated by that data.
 *
 * <p><b>Lookup injection.</b> The core {@link #resolve(Quest, ToIntFunction,
 * Function)} overload takes functional lookups so tests can fake
 * player state without mocking {@link Quest#getState(Client)} (which
 * is a final method reading a varp — painful to stub without
 * mockito-inline). Production code uses the {@link #resolve(Quest,
 * Client)} convenience overload which builds the lambdas from a live
 * {@link Client}.
 *
 * <p><b>Skipping rules.</b>
 * <ul>
 *   <li><b>Skill req</b>: skipped if {@code skillLevelLookup} returns
 *       a value &gt;= the required level. The "real" (unboosted) level
 *       is used because skill goals target base XP, not boosted XP.</li>
 *   <li><b>Quest prereq</b>: skipped if {@code questStateLookup}
 *       returns {@link QuestState#FINISHED}. Any {@link Exception}
 *       thrown during lookup is treated as "not finished" (defensive).</li>
 *   <li><b>Quest points</b>: never resolved to a goal (goal type not
 *       supported yet). Passed through as
 *       {@link Resolved#stubbedQuestPoints} so the caller can log a
 *       TODO.</li>
 * </ul>
 */
public final class QuestRequirementResolver
{
	/** Sprite id for the blue quest book icon. Mirrors
	 *  {@code GoalTrackerApiImpl.QUEST_SPRITE_ID} — kept as a local
	 *  constant to avoid a dependency on the api package. */
	private static final int QUEST_SPRITE_ID = 899;


	/** Output of {@link #resolve}. */
	public static final class Resolved
	{
		/** Goal templates to feed into {@code findOrCreateRequirement}. */
		public final List<Goal> templates;
		/** Count of skill reqs skipped because the player already meets the level. */
		public final int skippedSkills;
		/** Count of quest prereqs skipped because the player already finished them. */
		public final int skippedQuests;
		/** Quest-point requirement from the data table (0 = none). Not turned
		 *  into a template — stubbed until QP goals ship. */
		public final int stubbedQuestPoints;
		/** Combat-level requirement from the data table (0 = none). Not turned
		 *  into a template — stubbed until combat-level goals ship. */
		public final int stubbedCombatLevel;

		public Resolved(List<Goal> templates, int skippedSkills, int skippedQuests,
			int stubbedQuestPoints, int stubbedCombatLevel)
		{
			this.templates = templates;
			this.skippedSkills = skippedSkills;
			this.skippedQuests = skippedQuests;
			this.stubbedQuestPoints = stubbedQuestPoints;
			this.stubbedCombatLevel = stubbedCombatLevel;
		}

		/** True iff there's nothing for the caller to do — no templates AND
		 *  no QP / combat-level stub. The secondary menu entry uses this
		 *  to decide whether to render. */
		public boolean isEmpty()
		{
			return templates.isEmpty() && stubbedQuestPoints == 0 && stubbedCombatLevel == 0;
		}
	}

	/**
	 * Production overload: resolve against a live RuneLite {@link Client}.
	 */
	public static Resolved resolve(Quest quest, Client client)
	{
		return resolve(
			quest,
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
	 * Core overload: resolve using injected lookups. Tests use this
	 * directly with fake lookups.
	 *
	 * @param quest             quest to look up in {@link QuestRequirements}
	 * @param skillLevelLookup  maps a {@link Skill} to the player's current
	 *                          unboosted level
	 * @param questStateLookup  maps a prereq {@link Quest} to the player's
	 *                          current {@link QuestState}; may return null
	 */
	public static Resolved resolve(
		Quest quest,
		ToIntFunction<Skill> skillLevelLookup,
		Function<Quest, QuestState> questStateLookup)
	{
		QuestRequirements.Reqs reqs = QuestRequirements.lookup(quest);
		if (reqs == null)
		{
			return new Resolved(List.of(), 0, 0, 0, 0);
		}

		List<Goal> templates = new ArrayList<>();
		int skippedSkills = 0;
		int skippedQuests = 0;

		for (QuestRequirements.SkillReq req : reqs.skills)
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
			// Mirrors GoalTrackerApiImpl.addQuestGoal: set the blue quest
			// book sprite so the card shows the proper icon instead of
			// the GoalType default color dot.
			templates.add(Goal.builder()
				.type(GoalType.QUEST)
				.name(prereq.getName())
				.description("Quest")
				.questName(prereq.name())
				.targetValue(1)
				.spriteId(QUEST_SPRITE_ID)
				.build());
		}

		return new Resolved(templates, skippedSkills, skippedQuests, reqs.questPoints, reqs.combatLevel);
	}

	private QuestRequirementResolver() {}
}
