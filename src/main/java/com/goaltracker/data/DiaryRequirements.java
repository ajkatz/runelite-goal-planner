package com.goaltracker.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

/**
 * Pre-defined achievement diary tier requirements (skill levels,
 * quest prerequisites, and unlock milestones). Keyed on
 * "{Area}|{TIER}" strings matching {@link AchievementDiaryData.Tier}.
 *
 * <p><b>Unlocks</b> are virtual milestones like "Fairy Rings" that
 * represent in-game capabilities with their own prerequisite trees.
 * They're seeded as CUSTOM goals with quest requirements attached,
 * replacing misleading quest entries (e.g. Fairytale II when only
 * fairy ring access is needed).
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

	/**
	 * A virtual unlock milestone (e.g. "Fairy Rings Unlocked").
	 * Seeded as a CUSTOM goal with quest prerequisites.
	 */
	public static final class Unlock
	{
		public final String name;
		public final List<Quest> prereqQuests;
		/** Item ID for the icon (e.g. fairy ring POH item). 0 = no icon. */
		public final int itemId;

		public Unlock(String name, List<Quest> prereqQuests, int itemId)
		{
			this.name = name;
			this.prereqQuests = Collections.unmodifiableList(prereqQuests);
			this.itemId = itemId;
		}
	}

	/** Full requirement set for a diary tier. */
	public static final class Reqs
	{
		public final List<SkillReq> skills;
		public final List<Quest> prereqQuests;
		public final List<Unlock> unlocks;

		public Reqs(List<SkillReq> skills, List<Quest> prereqQuests)
		{
			this(skills, prereqQuests, Collections.emptyList());
		}

		public Reqs(List<SkillReq> skills, List<Quest> prereqQuests, List<Unlock> unlocks)
		{
			this.skills = Collections.unmodifiableList(skills);
			this.prereqQuests = Collections.unmodifiableList(prereqQuests);
			this.unlocks = Collections.unmodifiableList(unlocks);
		}
	}

	// ============================================================
	// Predefined unlocks — reusable across multiple diary areas
	// ============================================================

	/** Fairy rings: requires Fairytale I + Lost City (NOT full Fairytale II). */
	public static final Unlock FAIRY_RINGS = new Unlock(
		"Fairy Rings Unlocked",
		List.of(Quest.FAIRYTALE_I__GROWING_PAINS, Quest.LOST_CITY),
		772); // ItemID.DRAMEN_STAFF

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

	private static void put(String area, AchievementDiaryData.Tier tier,
		List<SkillReq> skills, List<Quest> quests, List<Unlock> unlocks)
	{
		TABLE.put(key(area, tier), new Reqs(skills, quests, unlocks));
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
		return !r.skills.isEmpty() || !r.prereqQuests.isEmpty() || !r.unlocks.isEmpty();
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
				Quest.SEA_SLUG,
				Quest.WATCHTOWER,
				Quest.ENLIGHTENED_JOURNEY,
				Quest.THE_HAND_IN_THE_SAND,
				Quest.RUNE_MYSTERIES,
				Quest.TOWER_OF_LIFE,
				Quest.UNDERGROUND_PASS),
			List.of(FAIRY_RINGS));

		// Hard: only NEW requirements beyond Medium tier.
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
				Quest.LEGENDS_QUEST,
				Quest.MONKEY_MADNESS_I,
				Quest.MOURNINGS_END_PART_II));

		// Elite: only NEW requirements beyond Hard tier.
		// Skills listed only where Elite level > Hard level.
		// Quests listed only if not already in Hard/Medium/Easy.
		put("Ardougne", AchievementDiaryData.Tier.ELITE,
			List.of(
				new SkillReq(Skill.MAGIC, 94),
				new SkillReq(Skill.COOKING, 91),
				new SkillReq(Skill.SMITHING, 91),
				new SkillReq(Skill.AGILITY, 90),
				new SkillReq(Skill.FARMING, 85),
				new SkillReq(Skill.THIEVING, 82),
				new SkillReq(Skill.FISHING, 81),
				new SkillReq(Skill.FLETCHING, 69)),
			List.of(
				Quest.DESERT_TREASURE_I,
				Quest.HAUNTED_MINE));
	}

	private DiaryRequirements() {}
}
