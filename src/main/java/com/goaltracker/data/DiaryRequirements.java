package com.goaltracker.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.ItemID;
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
		public final List<SkillReq> prereqSkills;
		public final List<AccountReq> prereqAccounts;
		/** Alternative paths (OR-group). Empty = no alternatives. */
		public final List<Alternative> alternatives;
		/** Item ID for the icon (e.g. fairy ring POH item). 0 = no icon. */
		public final int itemId;

		public Unlock(String name, List<Quest> prereqQuests, int itemId)
		{
			this(name, prereqQuests, Collections.emptyList(), Collections.emptyList(),
				Collections.emptyList(), itemId);
		}

		public Unlock(String name, List<Quest> prereqQuests, List<SkillReq> prereqSkills, int itemId)
		{
			this(name, prereqQuests, prereqSkills, Collections.emptyList(),
				Collections.emptyList(), itemId);
		}

		public Unlock(String name, List<Quest> prereqQuests, List<SkillReq> prereqSkills,
			List<AccountReq> prereqAccounts, int itemId)
		{
			this(name, prereqQuests, prereqSkills, prereqAccounts,
				Collections.emptyList(), itemId);
		}

		public Unlock(String name, List<Quest> prereqQuests, List<SkillReq> prereqSkills,
			List<AccountReq> prereqAccounts, List<Alternative> alternatives, int itemId)
		{
			this.name = name;
			this.prereqQuests = Collections.unmodifiableList(prereqQuests);
			this.prereqSkills = Collections.unmodifiableList(prereqSkills);
			this.prereqAccounts = Collections.unmodifiableList(prereqAccounts);
			this.alternatives = Collections.unmodifiableList(alternatives);
			this.itemId = itemId;
		}
	}

	/** A boss kill requirement (e.g. 1 Kalphite Queen kill for Desert Hard).
	 *  Boss-specific prereqs (skills, unlocks) are defined in
	 *  {@link BossKillData#getPrereqs} and auto-seeded by addBossGoal. */
	public static final class BossReq
	{
		public final String bossName;
		public final int killCount;

		public BossReq(String bossName, int killCount)
		{
			this.bossName = bossName;
			this.killCount = killCount;
		}
	}

	/** An account metric requirement (e.g. combined Att+Str 130). */
	public static final class AccountReq
	{
		public final String metricName; // AccountMetric enum name
		public final int target;
		public final List<Quest> prereqQuests;

		public AccountReq(String metricName, int target)
		{
			this(metricName, target, Collections.emptyList());
		}

		public AccountReq(String metricName, int target, List<Quest> prereqQuests)
		{
			this.metricName = metricName;
			this.target = target;
			this.prereqQuests = Collections.unmodifiableList(prereqQuests);
		}
	}

	/** An item requirement (e.g. KQ head for Desert Elite). */
	public static final class ItemReq
	{
		public final int itemId;
		public final String displayName;
		public final int quantity;

		public ItemReq(int itemId, String displayName, int quantity)
		{
			this.itemId = itemId;
			this.displayName = displayName;
			this.quantity = quantity;
		}
	}

	/** Full requirement set for a diary tier. */
	public static final class Reqs
	{
		public final List<SkillReq> skills;
		public final List<Quest> prereqQuests;
		public final List<Unlock> unlocks;
		public final List<BossReq> bossKills;
		public final List<ItemReq> itemReqs;
		public final List<AccountReq> accountReqs;

		public Reqs(List<SkillReq> skills, List<Quest> prereqQuests)
		{
			this(skills, prereqQuests, Collections.emptyList(), Collections.emptyList(),
				Collections.emptyList(), Collections.emptyList());
		}

		public Reqs(List<SkillReq> skills, List<Quest> prereqQuests, List<Unlock> unlocks)
		{
			this(skills, prereqQuests, unlocks, Collections.emptyList(),
				Collections.emptyList(), Collections.emptyList());
		}

		public Reqs(List<SkillReq> skills, List<Quest> prereqQuests,
			List<Unlock> unlocks, List<BossReq> bossKills)
		{
			this(skills, prereqQuests, unlocks, bossKills,
				Collections.emptyList(), Collections.emptyList());
		}

		public Reqs(List<SkillReq> skills, List<Quest> prereqQuests,
			List<Unlock> unlocks, List<BossReq> bossKills, List<ItemReq> itemReqs)
		{
			this(skills, prereqQuests, unlocks, bossKills, itemReqs, Collections.emptyList());
		}

		public Reqs(List<SkillReq> skills, List<Quest> prereqQuests,
			List<Unlock> unlocks, List<BossReq> bossKills,
			List<ItemReq> itemReqs, List<AccountReq> accountReqs)
		{
			this.skills = Collections.unmodifiableList(skills);
			this.prereqQuests = Collections.unmodifiableList(prereqQuests);
			this.unlocks = Collections.unmodifiableList(unlocks);
			this.bossKills = Collections.unmodifiableList(bossKills);
			this.itemReqs = Collections.unmodifiableList(itemReqs);
			this.accountReqs = Collections.unmodifiableList(accountReqs);
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

	/**
	 * An alternative path for an unlock. Multiple alternatives form an
	 * OR-group: ANY one being satisfied unlocks the goal.
	 */
	public static final class Alternative
	{
		public final String label;
		public final List<SkillReq> skills;
		public final List<AccountReq> accounts;

		public Alternative(String label, List<SkillReq> skills, List<AccountReq> accounts)
		{
			this.label = label;
			this.skills = Collections.unmodifiableList(skills);
			this.accounts = Collections.unmodifiableList(accounts);
		}
	}

	/** Warriors Guild entry: 130 combined Att+Str OR 99 Attack OR 99 Strength. */
	public static final Unlock WARRIORS_GUILD = new Unlock(
		"Warriors Guild Entry",
		List.of(), // no shared quest prereqs
		List.of(), // no shared skill prereqs
		List.of(), // no shared account prereqs
		List.of(
			new Alternative("Att+Str Combined >= 130",
				List.of(), List.of(new AccountReq("ATT_STR_COMBINED", 130))),
			new Alternative("99 Attack",
				List.of(new SkillReq(Skill.ATTACK, 99)), List.of()),
			new Alternative("99 Strength",
				List.of(new SkillReq(Skill.STRENGTH, 99)), List.of())
		),
		ItemID.STEEL_DEFENDER);

	/** Mith grapple: requires 59 Fletching + 59 Smithing to craft. */
	public static final Unlock MITH_GRAPPLE = new Unlock(
		"Mith Grapple",
		List.of(), // no quest prereqs
		List.of(new SkillReq(Skill.FLETCHING, 59), new SkillReq(Skill.SMITHING, 59)),
		ItemID.MITH_GRAPPLE);


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

	private static void put(String area, AchievementDiaryData.Tier tier,
		List<SkillReq> skills, List<Quest> quests, List<Unlock> unlocks, List<BossReq> bossKills)
	{
		TABLE.put(key(area, tier), new Reqs(skills, quests, unlocks, bossKills));
	}

	private static void put(String area, AchievementDiaryData.Tier tier,
		List<SkillReq> skills, List<Quest> quests, List<Unlock> unlocks,
		List<BossReq> bossKills, List<ItemReq> itemReqs)
	{
		TABLE.put(key(area, tier), new Reqs(skills, quests, unlocks, bossKills, itemReqs));
	}

	private static void put(String area, AchievementDiaryData.Tier tier,
		List<SkillReq> skills, List<Quest> quests, List<Unlock> unlocks,
		List<BossReq> bossKills, List<ItemReq> itemReqs, List<AccountReq> accountReqs)
	{
		TABLE.put(key(area, tier), new Reqs(skills, quests, unlocks, bossKills, itemReqs, accountReqs));
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

	// ============================================================
	// Desert Diary (wiki-sourced 2026-04-13)
	// ============================================================

	static
	{
		put("Desert", AchievementDiaryData.Tier.EASY,
			List.of(
				new SkillReq(Skill.HUNTER, 5),
				new SkillReq(Skill.THIEVING, 21)),
			List.of(
				Quest.ICTHLARINS_LITTLE_HELPER));

		put("Desert", AchievementDiaryData.Tier.MEDIUM,
			List.of(
				new SkillReq(Skill.HUNTER, 47),
				new SkillReq(Skill.HERBLORE, 36),
				new SkillReq(Skill.WOODCUTTING, 35),
				new SkillReq(Skill.AGILITY, 30),
				new SkillReq(Skill.THIEVING, 25),
				new SkillReq(Skill.SLAYER, 22),
				new SkillReq(Skill.CONSTRUCTION, 20)),
			List.of(
				Quest.EAGLES_PEAK,
				Quest.ENAKHRAS_LAMENT,
				Quest.THE_GOLEM,
				Quest.SPIRITS_OF_THE_ELID));

		put("Desert", AchievementDiaryData.Tier.HARD,
			List.of(
				new SkillReq(Skill.AGILITY, 70),
				new SkillReq(Skill.MAGIC, 68),
				new SkillReq(Skill.SMITHING, 68),
				new SkillReq(Skill.THIEVING, 65),
				new SkillReq(Skill.SLAYER, 65),
				new SkillReq(Skill.CRAFTING, 61),
				new SkillReq(Skill.FIREMAKING, 60),
				new SkillReq(Skill.MINING, 60),
				new SkillReq(Skill.ATTACK, 50),
				new SkillReq(Skill.PRAYER, 43),
				new SkillReq(Skill.RANGED, 40),
				new SkillReq(Skill.DEFENCE, 40)),
			List.of(
				Quest.CONTACT,
				Quest.DESERT_TREASURE_I,
				Quest.DREAM_MENTOR,
				Quest.THE_FEUD),
			List.of(), // no unlocks
			List.of(
				new BossReq("Kalphite Queen", 1)));

		put("Desert", AchievementDiaryData.Tier.ELITE,
			List.of(
				new SkillReq(Skill.FLETCHING, 95),
				new SkillReq(Skill.MAGIC, 94),
				new SkillReq(Skill.THIEVING, 91),
				new SkillReq(Skill.COOKING, 85),
				new SkillReq(Skill.PRAYER, 85),
				new SkillReq(Skill.CONSTRUCTION, 78)),
			List.of(
				Quest.DESERT_TREASURE_I,
				Quest.THE_TOURIST_TRAP,
				Quest.ICTHLARINS_LITTLE_HELPER,
				Quest.PRIEST_IN_PERIL),
			List.of(), // no unlocks
			List.of(), // no boss kills
			List.of(
				new ItemReq(ItemID.KQ_HEAD, "KQ Head", 1)));
	}

	// ============================================================
	// Falador Diary (wiki-sourced 2026-04-13)
	// ============================================================

	static
	{
		put("Falador", AchievementDiaryData.Tier.EASY,
			List.of(
				new SkillReq(Skill.CONSTRUCTION, 16),
				new SkillReq(Skill.SMITHING, 13),
				new SkillReq(Skill.MINING, 10),
				new SkillReq(Skill.AGILITY, 5)),
			List.of(
				Quest.DORICS_QUEST,
				Quest.THE_KNIGHTS_SWORD,
				Quest.RUNE_MYSTERIES));

		put("Falador", AchievementDiaryData.Tier.MEDIUM,
			List.of(
				new SkillReq(Skill.FIREMAKING, 49),
				new SkillReq(Skill.AGILITY, 42),
				new SkillReq(Skill.CRAFTING, 40),
				new SkillReq(Skill.MINING, 40),
				new SkillReq(Skill.THIEVING, 40),
				new SkillReq(Skill.MAGIC, 37),
				new SkillReq(Skill.STRENGTH, 37),
				new SkillReq(Skill.SLAYER, 32),
				new SkillReq(Skill.WOODCUTTING, 30),
				new SkillReq(Skill.FARMING, 23),
				new SkillReq(Skill.DEFENCE, 20),
				new SkillReq(Skill.RANGED, 19),
				new SkillReq(Skill.PRAYER, 10)),
			List.of(
				Quest.SKIPPY_AND_THE_MOGRES,
				Quest.RECRUITMENT_DRIVE,
				Quest.RATCATCHERS),
			List.of(MITH_GRAPPLE),
			List.of(), // no boss kills
			List.of(
				new ItemReq(ItemID.CRYSTAL_KEY, "Crystal Key", 1)));

		put("Falador", AchievementDiaryData.Tier.HARD,
			List.of(
				new SkillReq(Skill.SLAYER, 72),
				new SkillReq(Skill.PRAYER, 70),
				new SkillReq(Skill.MINING, 60),
				new SkillReq(Skill.AGILITY, 59),
				new SkillReq(Skill.THIEVING, 58),
				new SkillReq(Skill.RUNECRAFT, 56),
				new SkillReq(Skill.DEFENCE, 50)),
			List.of(
				Quest.HEROES_QUEST,
				Quest.THE_SLUG_MENACE,
				Quest.GRIM_TALES),
			List.of(WARRIORS_GUILD),
			List.of(
				new BossReq("Giant Mole", 1)));

		put("Falador", AchievementDiaryData.Tier.ELITE,
			List.of(
				new SkillReq(Skill.FARMING, 91),
				new SkillReq(Skill.RUNECRAFT, 88),
				new SkillReq(Skill.HERBLORE, 81),
				new SkillReq(Skill.AGILITY, 80),
				new SkillReq(Skill.WOODCUTTING, 75)),
			List.of(
				Quest.WANTED));
	}

	// ============================================================
	// Fremennik Diary (wiki-sourced 2026-04-13)
	// ============================================================

	static
	{
		put("Fremennik", AchievementDiaryData.Tier.EASY,
			List.of(
				new SkillReq(Skill.CRAFTING, 23),
				new SkillReq(Skill.MINING, 20),
				new SkillReq(Skill.SMITHING, 20),
				new SkillReq(Skill.WOODCUTTING, 15),
				new SkillReq(Skill.FIREMAKING, 15),
				new SkillReq(Skill.HUNTER, 11),
				new SkillReq(Skill.THIEVING, 5)),
			List.of(
				Quest.THE_FREMENNIK_TRIALS,
				Quest.THE_GIANT_DWARF,
				Quest.DEATH_PLATEAU,
				Quest.TROLL_STRONGHOLD));

		put("Fremennik", AchievementDiaryData.Tier.MEDIUM,
			List.of(
				new SkillReq(Skill.SMITHING, 50),
				new SkillReq(Skill.WOODCUTTING, 50),
				new SkillReq(Skill.SLAYER, 47),
				new SkillReq(Skill.THIEVING, 42),
				new SkillReq(Skill.FIREMAKING, 40),
				new SkillReq(Skill.MINING, 40),
				new SkillReq(Skill.CONSTRUCTION, 37),
				new SkillReq(Skill.AGILITY, 35),
				new SkillReq(Skill.HUNTER, 35),
				new SkillReq(Skill.CRAFTING, 31),
				new SkillReq(Skill.DEFENCE, 30)),
			List.of(
				Quest.BETWEEN_A_ROCK,
				Quest.EAGLES_PEAK,
				Quest.HORROR_FROM_THE_DEEP,
				Quest.OLAFS_QUEST),
			List.of(FAIRY_RINGS));

		put("Fremennik", AchievementDiaryData.Tier.HARD,
			List.of(
				new SkillReq(Skill.THIEVING, 75),
				new SkillReq(Skill.MAGIC, 72),
				new SkillReq(Skill.MINING, 70),
				new SkillReq(Skill.HERBLORE, 66),
				new SkillReq(Skill.CRAFTING, 61),
				new SkillReq(Skill.SMITHING, 60),
				new SkillReq(Skill.WOODCUTTING, 56),
				new SkillReq(Skill.HUNTER, 55),
				new SkillReq(Skill.FISHING, 53),
				new SkillReq(Skill.COOKING, 53),
				new SkillReq(Skill.SLAYER, 50),
				new SkillReq(Skill.FIREMAKING, 49)),
			List.of(
				Quest.EADGARS_RUSE,
				Quest.LUNAR_DIPLOMACY,
				Quest.THRONE_OF_MISCELLANIA,
				Quest.THE_FREMENNIK_ISLES,
				Quest.THE_GIANT_DWARF),
			List.of(), // no unlocks
			List.of(), // no boss kills
			List.of(), // no item reqs
			List.of(
				new AccountReq("MISC_APPROVAL", 127,
					List.of(Quest.THRONE_OF_MISCELLANIA))));

		put("Fremennik", AchievementDiaryData.Tier.ELITE,
			List.of(
				new SkillReq(Skill.SLAYER, 83),
				new SkillReq(Skill.RUNECRAFT, 82),
				new SkillReq(Skill.CRAFTING, 80),
				new SkillReq(Skill.AGILITY, 80),
				new SkillReq(Skill.HITPOINTS, 70),
				new SkillReq(Skill.RANGED, 70),
				new SkillReq(Skill.STRENGTH, 70)),
			List.of(
				Quest.TROLL_STRONGHOLD,
				Quest.THE_FREMENNIK_ISLES,
				Quest.LUNAR_DIPLOMACY),
			List.of(), // no unlocks
			List.of(
				new BossReq("Kree'arra", 1),
				new BossReq("General Graardor", 1),
				new BossReq("Commander Zilyana", 1),
				new BossReq("K'ril Tsutsaroth", 1),
				new BossReq("Dagannoth Prime", 1),
				new BossReq("Dagannoth Rex", 1),
				new BossReq("Dagannoth Supreme", 1)));
	}

	// ============================================================
	// Kandarin Diary (wiki-sourced 2026-04-13)
	// ============================================================

	static
	{
		put("Kandarin", AchievementDiaryData.Tier.EASY,
			List.of(
				new SkillReq(Skill.AGILITY, 20),
				new SkillReq(Skill.FISHING, 16),
				new SkillReq(Skill.FARMING, 13)),
			List.of(
				Quest.ELEMENTAL_WORKSHOP_I));

		put("Kandarin", AchievementDiaryData.Tier.MEDIUM,
			List.of(
				new SkillReq(Skill.FLETCHING, 50),
				new SkillReq(Skill.HERBLORE, 48),
				new SkillReq(Skill.THIEVING, 47),
				new SkillReq(Skill.FISHING, 46),
				new SkillReq(Skill.MAGIC, 45),
				new SkillReq(Skill.COOKING, 43),
				new SkillReq(Skill.RANGED, 40),
				new SkillReq(Skill.AGILITY, 36),
				new SkillReq(Skill.WOODCUTTING, 36),
				new SkillReq(Skill.MINING, 30),
				new SkillReq(Skill.FARMING, 26),
				new SkillReq(Skill.STRENGTH, 22)),
			List.of(
				Quest.ALFRED_GRIMHANDS_BARCRAWL,
				Quest.ELEMENTAL_WORKSHOP_II,
				Quest.WATERFALL_QUEST),
			List.of(FAIRY_RINGS));

		put("Kandarin", AchievementDiaryData.Tier.HARD,
			List.of(
				new SkillReq(Skill.SMITHING, 75),
				new SkillReq(Skill.FLETCHING, 70),
				new SkillReq(Skill.FISHING, 70),
				new SkillReq(Skill.PRAYER, 70),
				new SkillReq(Skill.DEFENCE, 70),
				new SkillReq(Skill.FIREMAKING, 65),
				new SkillReq(Skill.WOODCUTTING, 60),
				new SkillReq(Skill.AGILITY, 60),
				new SkillReq(Skill.MAGIC, 56),
				new SkillReq(Skill.CONSTRUCTION, 50),
				new SkillReq(Skill.STRENGTH, 50)),
			List.of(
				Quest.KINGS_RANSOM,
				Quest.DESERT_TREASURE_I));

		put("Kandarin", AchievementDiaryData.Tier.ELITE,
			List.of(
				new SkillReq(Skill.SMITHING, 90),
				new SkillReq(Skill.MAGIC, 87),
				new SkillReq(Skill.HERBLORE, 86),
				new SkillReq(Skill.CRAFTING, 85),
				new SkillReq(Skill.FIREMAKING, 85),
				new SkillReq(Skill.COOKING, 80),
				new SkillReq(Skill.FARMING, 79),
				new SkillReq(Skill.FISHING, 76)),
			List.of(
				Quest.FAMILY_CREST,
				Quest.LUNAR_DIPLOMACY));
	}

	// ============================================================
	// Karamja Diary (wiki-sourced 2026-04-13)
	// ============================================================

	static
	{
		put("Karamja", AchievementDiaryData.Tier.EASY,
			List.of(
				new SkillReq(Skill.MINING, 40),
				new SkillReq(Skill.AGILITY, 15)),
			List.of());

		put("Karamja", AchievementDiaryData.Tier.MEDIUM,
			List.of(
				new SkillReq(Skill.FISHING, 65),
				new SkillReq(Skill.WOODCUTTING, 50),
				new SkillReq(Skill.HUNTER, 41),
				new SkillReq(Skill.AGILITY, 32),
				new SkillReq(Skill.FARMING, 27),
				new SkillReq(Skill.CRAFTING, 20),
				new SkillReq(Skill.COOKING, 16)),
			List.of(
				Quest.DRAGON_SLAYER_I,
				Quest.JUNGLE_POTION,
				Quest.SHILO_VILLAGE,
				Quest.TAI_BWO_WANNAI_TRIO,
				Quest.THE_GRAND_TREE));

		put("Karamja", AchievementDiaryData.Tier.HARD,
			List.of(
				new SkillReq(Skill.MAGIC, 59),
				new SkillReq(Skill.AGILITY, 53),
				new SkillReq(Skill.COOKING, 53),
				new SkillReq(Skill.MINING, 52),
				new SkillReq(Skill.THIEVING, 50),
				new SkillReq(Skill.STRENGTH, 50),
				new SkillReq(Skill.SLAYER, 50),
				new SkillReq(Skill.RUNECRAFT, 44),
				new SkillReq(Skill.RANGED, 42)),
			List.of(
				Quest.LEGENDS_QUEST));

		put("Karamja", AchievementDiaryData.Tier.ELITE,
			List.of(
				new SkillReq(Skill.RUNECRAFT, 91),
				new SkillReq(Skill.HERBLORE, 87),
				new SkillReq(Skill.FARMING, 72)),
			List.of());
	}

	// ============================================================
	// Kourend & Kebos Diary (wiki-sourced 2026-04-13)
	// ============================================================

	static
	{
		put("Kourend & Kebos", AchievementDiaryData.Tier.EASY,
			List.of(
				new SkillReq(Skill.CONSTRUCTION, 25),
				new SkillReq(Skill.THIEVING, 25),
				new SkillReq(Skill.FISHING, 20),
				new SkillReq(Skill.MINING, 15),
				new SkillReq(Skill.HERBLORE, 12)),
			List.of(
				Quest.DRUIDIC_RITUAL));

		put("Kourend & Kebos", AchievementDiaryData.Tier.MEDIUM,
			List.of(
				new SkillReq(Skill.HUNTER, 53),
				new SkillReq(Skill.FIREMAKING, 50),
				new SkillReq(Skill.WOODCUTTING, 50),
				new SkillReq(Skill.AGILITY, 49),
				new SkillReq(Skill.FARMING, 45),
				new SkillReq(Skill.FISHING, 43),
				new SkillReq(Skill.MINING, 42),
				new SkillReq(Skill.CRAFTING, 31),
				new SkillReq(Skill.CONSTRUCTION, 30),
				new SkillReq(Skill.STRENGTH, 16)),
			List.of(
				Quest.EAGLES_PEAK,
				Quest.THE_ASCENT_OF_ARCEUUS,
				Quest.THE_DEPTHS_OF_DESPAIR,
				Quest.THE_FORSAKEN_TOWER,
				Quest.THE_QUEEN_OF_THIEVES,
				Quest.TALE_OF_THE_RIGHTEOUS),
			List.of(FAIRY_RINGS));

		put("Kourend & Kebos", AchievementDiaryData.Tier.HARD,
			List.of(
				new SkillReq(Skill.FARMING, 74),
				new SkillReq(Skill.SMITHING, 70),
				new SkillReq(Skill.MAGIC, 66),
				new SkillReq(Skill.MINING, 65),
				new SkillReq(Skill.SLAYER, 62),
				new SkillReq(Skill.CRAFTING, 61),
				new SkillReq(Skill.WOODCUTTING, 60),
				new SkillReq(Skill.THIEVING, 49),
				new SkillReq(Skill.DEFENCE, 40),
				new SkillReq(Skill.HERBLORE, 31)),
			List.of(
				Quest.DREAM_MENTOR));

		put("Kourend & Kebos", AchievementDiaryData.Tier.ELITE,
			List.of(
				new SkillReq(Skill.SLAYER, 95),
				new SkillReq(Skill.WOODCUTTING, 90),
				new SkillReq(Skill.MAGIC, 90),
				new SkillReq(Skill.FARMING, 85),
				new SkillReq(Skill.COOKING, 84),
				new SkillReq(Skill.FISHING, 82),
				new SkillReq(Skill.RUNECRAFT, 77),
				new SkillReq(Skill.FLETCHING, 40)),
			List.of());
	}

	// ============================================================
	// Lumbridge & Draynor Diary (wiki-sourced 2026-04-13)
	// ============================================================

	static
	{
		put("Lumbridge & Draynor", AchievementDiaryData.Tier.EASY,
			List.of(
				new SkillReq(Skill.FISHING, 15),
				new SkillReq(Skill.FIREMAKING, 15),
				new SkillReq(Skill.MINING, 15),
				new SkillReq(Skill.WOODCUTTING, 15),
				new SkillReq(Skill.SLAYER, 7),
				new SkillReq(Skill.RUNECRAFT, 5)),
			List.of(
				Quest.COOKS_ASSISTANT,
				Quest.RUNE_MYSTERIES));

		put("Lumbridge & Draynor", AchievementDiaryData.Tier.MEDIUM,
			List.of(
				new SkillReq(Skill.RANGED, 50),
				new SkillReq(Skill.HUNTER, 42),
				new SkillReq(Skill.CRAFTING, 38),
				new SkillReq(Skill.THIEVING, 38),
				new SkillReq(Skill.WOODCUTTING, 36),
				new SkillReq(Skill.MAGIC, 31),
				new SkillReq(Skill.FISHING, 30),
				new SkillReq(Skill.RUNECRAFT, 23),
				new SkillReq(Skill.AGILITY, 20),
				new SkillReq(Skill.STRENGTH, 19)),
			List.of(
				Quest.ANIMAL_MAGNETISM,
				Quest.LOST_CITY),
			List.of(FAIRY_RINGS));

		put("Lumbridge & Draynor", AchievementDiaryData.Tier.HARD,
			List.of(
				new SkillReq(Skill.COOKING, 70),
				new SkillReq(Skill.CRAFTING, 70),
				new SkillReq(Skill.FIREMAKING, 65),
				new SkillReq(Skill.FARMING, 63),
				new SkillReq(Skill.RUNECRAFT, 59),
				new SkillReq(Skill.WOODCUTTING, 57),
				new SkillReq(Skill.FISHING, 53),
				new SkillReq(Skill.THIEVING, 53),
				new SkillReq(Skill.PRAYER, 52),
				new SkillReq(Skill.MINING, 50),
				new SkillReq(Skill.MAGIC, 60),
				new SkillReq(Skill.AGILITY, 46)),
			List.of(
				Quest.ANOTHER_SLICE_OF_HAM,
				Quest.RECIPE_FOR_DISASTER,
				Quest.TEARS_OF_GUTHIX));

		put("Lumbridge & Draynor", AchievementDiaryData.Tier.ELITE,
			List.of(
				new SkillReq(Skill.SMITHING, 88),
				new SkillReq(Skill.THIEVING, 78),
				new SkillReq(Skill.RUNECRAFT, 76),
				new SkillReq(Skill.WOODCUTTING, 75),
				new SkillReq(Skill.AGILITY, 70),
				new SkillReq(Skill.RANGED, 70),
				new SkillReq(Skill.STRENGTH, 70)),
			List.of());
	}

	// ============================================================
	// Morytania Diary (wiki-sourced 2026-04-13)
	// ============================================================

	static
	{
		put("Morytania", AchievementDiaryData.Tier.EASY,
			List.of(
				new SkillReq(Skill.FARMING, 23),
				new SkillReq(Skill.CRAFTING, 15),
				new SkillReq(Skill.SLAYER, 15),
				new SkillReq(Skill.COOKING, 12)),
			List.of(
				Quest.NATURE_SPIRIT));

		put("Morytania", AchievementDiaryData.Tier.MEDIUM,
			List.of(
				new SkillReq(Skill.FISHING, 50),
				new SkillReq(Skill.SMITHING, 50),
				new SkillReq(Skill.PRAYER, 47),
				new SkillReq(Skill.CRAFTING, 45),
				new SkillReq(Skill.AGILITY, 42),
				new SkillReq(Skill.SLAYER, 42),
				new SkillReq(Skill.WOODCUTTING, 45),
				new SkillReq(Skill.COOKING, 40),
				new SkillReq(Skill.FARMING, 40),
				new SkillReq(Skill.RANGED, 40),
				new SkillReq(Skill.HUNTER, 29),
				new SkillReq(Skill.HERBLORE, 22),
				new SkillReq(Skill.MINING, 15)),
			List.of(
				Quest.CABIN_FEVER,
				Quest.DWARF_CANNON,
				Quest.GHOSTS_AHOY,
				Quest.IN_AID_OF_THE_MYREQUE,
				Quest.LAIR_OF_TARN_RAZORLOR));

		put("Morytania", AchievementDiaryData.Tier.HARD,
			List.of(
				new SkillReq(Skill.AGILITY, 71),
				new SkillReq(Skill.PRAYER, 70),
				new SkillReq(Skill.DEFENCE, 70),
				new SkillReq(Skill.MAGIC, 66),
				new SkillReq(Skill.SLAYER, 58),
				new SkillReq(Skill.MINING, 55),
				new SkillReq(Skill.FARMING, 53),
				new SkillReq(Skill.THIEVING, 53),
				new SkillReq(Skill.CONSTRUCTION, 50),
				new SkillReq(Skill.FIREMAKING, 50),
				new SkillReq(Skill.WOODCUTTING, 50)),
			List.of(
				Quest.DESERT_TREASURE_I,
				Quest.THE_GREAT_BRAIN_ROBBERY));

		put("Morytania", AchievementDiaryData.Tier.ELITE,
			List.of(
				new SkillReq(Skill.FISHING, 96),
				new SkillReq(Skill.SLAYER, 85),
				new SkillReq(Skill.CRAFTING, 84),
				new SkillReq(Skill.MAGIC, 83),
				new SkillReq(Skill.FIREMAKING, 80),
				new SkillReq(Skill.STRENGTH, 76)),
			List.of(
				Quest.LUNAR_DIPLOMACY,
				Quest.SHADES_OF_MORTTON));
	}

	// ============================================================
	// Varrock Diary (wiki-sourced 2026-04-13)
	// ============================================================

	static
	{
		put("Varrock", AchievementDiaryData.Tier.EASY,
			List.of(
				new SkillReq(Skill.FISHING, 20),
				new SkillReq(Skill.MINING, 15),
				new SkillReq(Skill.AGILITY, 13),
				new SkillReq(Skill.RUNECRAFT, 9),
				new SkillReq(Skill.CRAFTING, 8),
				new SkillReq(Skill.THIEVING, 5)),
			List.of(
				Quest.RUNE_MYSTERIES));

		put("Varrock", AchievementDiaryData.Tier.MEDIUM,
			List.of(
				new SkillReq(Skill.FIREMAKING, 40),
				new SkillReq(Skill.FARMING, 30),
				new SkillReq(Skill.AGILITY, 30),
				new SkillReq(Skill.MAGIC, 25),
				new SkillReq(Skill.THIEVING, 25),
				new SkillReq(Skill.HERBLORE, 10)),
			List.of(
				Quest.A_SOULS_BANE,
				Quest.ENLIGHTENED_JOURNEY,
				Quest.GARDEN_OF_TRANQUILLITY,
				Quest.GERTRUDES_CAT,
				Quest.THE_DIG_SITE,
				Quest.TREE_GNOME_VILLAGE));

		put("Varrock", AchievementDiaryData.Tier.HARD,
			List.of(
				new SkillReq(Skill.FARMING, 68),
				new SkillReq(Skill.HUNTER, 66),
				new SkillReq(Skill.FIREMAKING, 60),
				new SkillReq(Skill.WOODCUTTING, 60),
				new SkillReq(Skill.MAGIC, 54),
				new SkillReq(Skill.THIEVING, 53),
				new SkillReq(Skill.PRAYER, 52),
				new SkillReq(Skill.AGILITY, 51),
				new SkillReq(Skill.CONSTRUCTION, 50),
				new SkillReq(Skill.RANGED, 40),
				new SkillReq(Skill.SMITHING, 20)),
			List.of(
				Quest.DESERT_TREASURE_I));

		put("Varrock", AchievementDiaryData.Tier.ELITE,
			List.of(
				new SkillReq(Skill.COOKING, 95),
				new SkillReq(Skill.HERBLORE, 90),
				new SkillReq(Skill.SMITHING, 89),
				new SkillReq(Skill.MAGIC, 86),
				new SkillReq(Skill.FLETCHING, 81),
				new SkillReq(Skill.RUNECRAFT, 78),
				new SkillReq(Skill.CRAFTING, 61),
				new SkillReq(Skill.MINING, 60),
				new SkillReq(Skill.DEFENCE, 40)),
			List.of(
				Quest.DREAM_MENTOR));
	}

	// ============================================================
	// Western Provinces Diary (wiki-sourced 2026-04-13)
	// ============================================================

	static
	{
		put("Western Provinces", AchievementDiaryData.Tier.EASY,
			List.of(
				new SkillReq(Skill.FLETCHING, 20),
				new SkillReq(Skill.HUNTER, 9)),
			List.of(
				Quest.BIG_CHOMPY_BIRD_HUNTING,
				Quest.RUNE_MYSTERIES));

		put("Western Provinces", AchievementDiaryData.Tier.MEDIUM,
			List.of(
				new SkillReq(Skill.FISHING, 46),
				new SkillReq(Skill.MAGIC, 46),
				new SkillReq(Skill.COOKING, 42),
				new SkillReq(Skill.MINING, 40),
				new SkillReq(Skill.AGILITY, 37),
				new SkillReq(Skill.FIREMAKING, 35),
				new SkillReq(Skill.WOODCUTTING, 35),
				new SkillReq(Skill.HUNTER, 31),
				new SkillReq(Skill.RANGED, 30),
				new SkillReq(Skill.SMITHING, 30),
				new SkillReq(Skill.CRAFTING, 25)),
			List.of(
				Quest.EAGLES_PEAK,
				Quest.THE_EYES_OF_GLOUPHRIE,
				Quest.THE_GRAND_TREE,
				Quest.TREE_GNOME_VILLAGE,
				Quest.MONKEY_MADNESS_I));

		put("Western Provinces", AchievementDiaryData.Tier.HARD,
			List.of(
				new SkillReq(Skill.THIEVING, 75),
				new SkillReq(Skill.MINING, 70),
				new SkillReq(Skill.COOKING, 70),
				new SkillReq(Skill.RANGED, 70),
				new SkillReq(Skill.HUNTER, 69),
				new SkillReq(Skill.FARMING, 68),
				new SkillReq(Skill.MAGIC, 66),
				new SkillReq(Skill.CONSTRUCTION, 65),
				new SkillReq(Skill.FISHING, 62),
				new SkillReq(Skill.AGILITY, 56),
				new SkillReq(Skill.FIREMAKING, 50),
				new SkillReq(Skill.WOODCUTTING, 50),
				new SkillReq(Skill.SMITHING, 45),
				new SkillReq(Skill.CRAFTING, 40)),
			List.of(
				Quest.ROVING_ELVES,
				Quest.SWAN_SONG,
				Quest.REGICIDE));

		put("Western Provinces", AchievementDiaryData.Tier.ELITE,
			List.of(
				new SkillReq(Skill.SLAYER, 93),
				new SkillReq(Skill.AGILITY, 85),
				new SkillReq(Skill.FLETCHING, 85),
				new SkillReq(Skill.THIEVING, 85),
				new SkillReq(Skill.FARMING, 75)),
			List.of(
				Quest.UNDERGROUND_PASS));
	}

	// ============================================================
	// Wilderness Diary (wiki-sourced 2026-04-13)
	// ============================================================

	static
	{
		put("Wilderness", AchievementDiaryData.Tier.EASY,
			List.of(
				new SkillReq(Skill.MAGIC, 21)),
			List.of(
				Quest.ENTER_THE_ABYSS));

		put("Wilderness", AchievementDiaryData.Tier.MEDIUM,
			List.of(
				new SkillReq(Skill.WOODCUTTING, 61),
				new SkillReq(Skill.MAGIC, 60),
				new SkillReq(Skill.MINING, 55),
				new SkillReq(Skill.AGILITY, 52),
				new SkillReq(Skill.SLAYER, 50)),
			List.of());

		put("Wilderness", AchievementDiaryData.Tier.HARD,
			List.of(
				new SkillReq(Skill.SLAYER, 68),
				new SkillReq(Skill.HUNTER, 67),
				new SkillReq(Skill.MAGIC, 66),
				new SkillReq(Skill.AGILITY, 64),
				new SkillReq(Skill.SMITHING, 75),
				new SkillReq(Skill.FISHING, 53),
				new SkillReq(Skill.HERBLORE, 25)),
			List.of(
				Quest.DEATH_PLATEAU,
				Quest.MAGE_ARENA_I));

		put("Wilderness", AchievementDiaryData.Tier.ELITE,
			List.of(
				new SkillReq(Skill.MAGIC, 96),
				new SkillReq(Skill.COOKING, 90),
				new SkillReq(Skill.SMITHING, 90),
				new SkillReq(Skill.FISHING, 85),
				new SkillReq(Skill.MINING, 85),
				new SkillReq(Skill.THIEVING, 84),
				new SkillReq(Skill.WOODCUTTING, 75),
				new SkillReq(Skill.FIREMAKING, 75)),
			List.of(
				Quest.DESERT_TREASURE_I));
	}

	private DiaryRequirements() {}
}
