package com.goaltracker.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

/**
 * Pre-defined achievement diary tier requirements (skill levels and
 * quest prerequisites). Keyed on "{Area}|{TIER}" strings matching
 * {@link AchievementDiaryData.Tier}. Consumed by the "Add Goal with
 * Requirements" flow for diaries.
 *
 * <p>Requirements are the HIGHEST skill level needed for any task in
 * that tier, and all quests that must be completed (or started — we
 * treat "started" as "completed" for simplicity since the player
 * will need it done eventually).
 */
public final class DiaryRequirements
{
	/** A single skill requirement. */
	public static final class SkillReq
	{
		public final Skill skill;
		public final int level;

		public SkillReq(Skill skill, int level)
		{
			this.skill = skill;
			this.level = level;
		}
	}

	/** Full requirement set for a diary tier. */
	public static final class Reqs
	{
		public final List<SkillReq> skills;
		public final List<Quest> prereqQuests;

		public Reqs(List<SkillReq> skills, List<Quest> prereqQuests)
		{
			this.skills = Collections.unmodifiableList(skills);
			this.prereqQuests = Collections.unmodifiableList(prereqQuests);
		}
	}

	private static final Map<String, Reqs> TABLE = new HashMap<>();

	private static String key(String area, AchievementDiaryData.Tier tier)
	{
		return area + "|" + tier.name();
	}

	private static void put(String area, AchievementDiaryData.Tier tier,
		List<SkillReq> skills, List<Quest> quests)
	{
		TABLE.put(key(area, tier), new Reqs(skills, quests));
	}

	/**
	 * Look up the requirements for a diary tier.
	 *
	 * @return the requirements, or {@code null} if not in the table
	 */
	public static Reqs lookup(String area, AchievementDiaryData.Tier tier)
	{
		if (area == null || tier == null) return null;
		return TABLE.get(key(area, tier));
	}

	/** True iff the diary tier has non-empty requirements. */
	public static boolean hasRequirements(String area, AchievementDiaryData.Tier tier)
	{
		Reqs r = lookup(area, tier);
		if (r == null) return false;
		return !r.skills.isEmpty() || !r.prereqQuests.isEmpty();
	}

	// ============================================================
	// Ardougne Diary (wiki-sourced 2026-04-11)
	// ============================================================

	static
	{
		put("Ardougne", AchievementDiaryData.Tier.EASY,
			List.of(
				new SkillReq(Skill.THIEVING, 5)),
			List.of(
				Quest.RUNE_MYSTERIES,
				Quest.BIOHAZARD));

		put("Ardougne", AchievementDiaryData.Tier.MEDIUM,
			List.of(
				new SkillReq(Skill.MAGIC, 51),
				new SkillReq(Skill.FIREMAKING, 50),
				new SkillReq(Skill.CRAFTING, 49),
				new SkillReq(Skill.AGILITY, 39),
				new SkillReq(Skill.STRENGTH, 38),
				new SkillReq(Skill.THIEVING, 38),
				new SkillReq(Skill.FARMING, 31),
				new SkillReq(Skill.RANGED, 21)),
			List.of(
				Quest.FAIRYTALE_II__CURE_A_QUEEN,
				Quest.SEA_SLUG,
				Quest.WATCHTOWER,
				Quest.ENLIGHTENED_JOURNEY,
				Quest.THE_HAND_IN_THE_SAND,
				Quest.RUNE_MYSTERIES,
				Quest.TOWER_OF_LIFE,
				Quest.UNDERGROUND_PASS));

		put("Ardougne", AchievementDiaryData.Tier.HARD,
			List.of(
				new SkillReq(Skill.THIEVING, 72),
				new SkillReq(Skill.FARMING, 70),
				new SkillReq(Skill.SMITHING, 68),
				new SkillReq(Skill.MAGIC, 66),
				new SkillReq(Skill.RUNECRAFT, 65),
				new SkillReq(Skill.RANGED, 60),
				new SkillReq(Skill.HUNTER, 59),
				new SkillReq(Skill.AGILITY, 56),
				new SkillReq(Skill.FISHING, 53),
				new SkillReq(Skill.COOKING, 53),
				new SkillReq(Skill.MINING, 52),
				new SkillReq(Skill.CRAFTING, 50),
				new SkillReq(Skill.CONSTRUCTION, 50),
				new SkillReq(Skill.FIREMAKING, 50),
				new SkillReq(Skill.STRENGTH, 50),
				new SkillReq(Skill.WOODCUTTING, 50),
				new SkillReq(Skill.HERBLORE, 45),
				new SkillReq(Skill.PRAYER, 42),
				new SkillReq(Skill.FLETCHING, 5)),
			List.of(
				Quest.ENLIGHTENED_JOURNEY,
				Quest.THE_HAND_IN_THE_SAND,
				Quest.LEGENDS_QUEST,
				Quest.MONKEY_MADNESS_I,
				Quest.MOURNINGS_END_PART_II,
				Quest.RUNE_MYSTERIES,
				Quest.TOWER_OF_LIFE,
				Quest.WATCHTOWER,
				Quest.FAIRYTALE_II__CURE_A_QUEEN,
				Quest.SEA_SLUG));

		put("Ardougne", AchievementDiaryData.Tier.ELITE,
			List.of(
				new SkillReq(Skill.MAGIC, 94),
				new SkillReq(Skill.COOKING, 91),
				new SkillReq(Skill.SMITHING, 91),
				new SkillReq(Skill.AGILITY, 90),
				new SkillReq(Skill.FARMING, 85),
				new SkillReq(Skill.THIEVING, 82),
				new SkillReq(Skill.FISHING, 81),
				new SkillReq(Skill.FLETCHING, 69),
				new SkillReq(Skill.RUNECRAFT, 65),
				new SkillReq(Skill.RANGED, 60),
				new SkillReq(Skill.HUNTER, 59),
				new SkillReq(Skill.MINING, 52),
				new SkillReq(Skill.CRAFTING, 50),
				new SkillReq(Skill.CONSTRUCTION, 50),
				new SkillReq(Skill.FIREMAKING, 50),
				new SkillReq(Skill.STRENGTH, 50),
				new SkillReq(Skill.WOODCUTTING, 50),
				new SkillReq(Skill.HERBLORE, 45),
				new SkillReq(Skill.PRAYER, 42)),
			List.of(
				Quest.DESERT_TREASURE_I,
				Quest.ENLIGHTENED_JOURNEY,
				Quest.THE_HAND_IN_THE_SAND,
				Quest.HAUNTED_MINE,
				Quest.LEGENDS_QUEST,
				Quest.MONKEY_MADNESS_I,
				Quest.MOURNINGS_END_PART_II,
				Quest.RUNE_MYSTERIES,
				Quest.TOWER_OF_LIFE,
				Quest.WATCHTOWER,
				Quest.FAIRYTALE_II__CURE_A_QUEEN,
				Quest.SEA_SLUG));
	}

	private DiaryRequirements() {}
}
