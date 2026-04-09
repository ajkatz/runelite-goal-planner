package com.goaltracker.data;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

/**
 * Pre-defined quest → requirement associations.
 *
 * <p>Keyed on the RuneLite {@link Quest} enum — the same identity the
 * plugin uses in {@code Goal.questName} — so no string plumbing is
 * needed at the consumer. Consumed by the "Add Goal with Requirements"
 * flow in {@code GoalTrackerApiImpl.addQuestGoalWithPrereqs}, which
 * feeds each entry through {@code findOrCreateRequirement} inside a
 * compound command.
 *
 * <p><b>Proof-of-concept coverage.</b> This initial table is
 * intentionally small (a handful of quests spanning zero-req, single-
 * skill, multi-quest, and stress-test cases) to validate the data shape
 * and consumer flow. Full coverage is a follow-up and will likely
 * migrate to a resource file if the table grows enough to make .java
 * edits painful.
 *
 * <p><b>Stubbed non-goal requirements.</b> Two flavors of requirement
 * aren't yet expressible as goals and are recorded-but-not-seeded:
 * <ul>
 *   <li><b>Quest points</b> (e.g. Dragon Slayer II = 200 QP) — waits
 *       for the "Account-wide goals" roadmap item to land a QP goal
 *       type.</li>
 *   <li><b>Combat level</b> (e.g. Dream Mentor = 85 Combat) — combat
 *       level is derived from skill levels, so it'll probably become
 *       its own account-wide goal too.</li>
 * </ul>
 * Both are passed through the resolver as bookkeeping fields on
 * {@code Resolved} so the consumer can log a TODO with the parent
 * quest context. Grep {@code questPoints > 0} and
 * {@code combatLevel > 0} to find all the stub sites when those goal
 * types ship.
 */
public final class QuestRequirements
{
	/** A single skill requirement (skill + minimum level). */
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

	/** The full requirement set for a quest. Any field may be empty. */
	public static final class Reqs
	{
		public final List<SkillReq> skills;
		public final List<Quest> prereqQuests;
		/** Minimum total quest points required. 0 = none. Stubbed (see class javadoc). */
		public final int questPoints;
		/** Minimum combat level required. 0 = none. Stubbed (see class javadoc). */
		public final int combatLevel;

		public Reqs(List<SkillReq> skills, List<Quest> prereqQuests, int questPoints, int combatLevel)
		{
			this.skills = Collections.unmodifiableList(skills);
			this.prereqQuests = Collections.unmodifiableList(prereqQuests);
			this.questPoints = questPoints;
			this.combatLevel = combatLevel;
		}
	}

	private static final Map<Quest, Reqs> TABLE = new EnumMap<>(Quest.class);

	static
	{
		// --- Simple: zero-req baseline ---
		put(Quest.COOKS_ASSISTANT,
			List.of(), List.of(), 0, 0);

		// --- HFTD chain ---
		// Horror from the Deep: 35 Agility + Alfred Grimhand's Barcrawl.
		put(Quest.HORROR_FROM_THE_DEEP,
			List.of(new SkillReq(Skill.AGILITY, 35)),
			List.of(Quest.ALFRED_GRIMHANDS_BARCRAWL),
			0, 0);
		put(Quest.ALFRED_GRIMHANDS_BARCRAWL,
			List.of(), List.of(), 0, 0);

		// --- MM1 chain ---
		put(Quest.MONKEY_MADNESS_I,
			List.of(),
			List.of(Quest.TREE_GNOME_VILLAGE, Quest.THE_GRAND_TREE),
			0, 0);
		put(Quest.TREE_GNOME_VILLAGE,
			List.of(), List.of(), 0, 0);
		put(Quest.THE_GRAND_TREE,
			List.of(new SkillReq(Skill.AGILITY, 25)),
			List.of(),
			0, 0);

		// --- DT1 chain (transitive quest reqs not yet populated) ---
		put(Quest.DESERT_TREASURE_I,
			List.of(
				new SkillReq(Skill.THIEVING, 53),
				new SkillReq(Skill.FIREMAKING, 50),
				new SkillReq(Skill.MAGIC, 50),
				new SkillReq(Skill.SLAYER, 10)),
			List.of(
				Quest.THE_DIG_SITE,
				Quest.TEMPLE_OF_IKOV,
				Quest.THE_TOURIST_TRAP,
				Quest.TROLL_STRONGHOLD,
				Quest.PRIEST_IN_PERIL,
				Quest.WATERFALL_QUEST),
			0, 0);

		// --- Dream Mentor full transitive chain (wiki-sourced 2026-04-08) ---
		// Dream Mentor's ONLY hard gate is 85 Combat — no skill reqs, no QP.
		// (Previous hand-entry of 125 QP was wrong; wiki confirms none.)
		put(Quest.DREAM_MENTOR,
			List.of(),
			List.of(Quest.LUNAR_DIPLOMACY, Quest.EADGARS_RUSE),
			0, 85);

		// Lunar Diplomacy branch
		put(Quest.LUNAR_DIPLOMACY,
			List.of(
				new SkillReq(Skill.HERBLORE, 5),
				new SkillReq(Skill.CRAFTING, 61),
				new SkillReq(Skill.DEFENCE, 40),
				new SkillReq(Skill.FIREMAKING, 49),
				new SkillReq(Skill.MAGIC, 65),
				new SkillReq(Skill.MINING, 60),
				new SkillReq(Skill.WOODCUTTING, 55)),
			List.of(
				Quest.THE_FREMENNIK_TRIALS,
				Quest.LOST_CITY,
				Quest.RUNE_MYSTERIES,
				Quest.SHILO_VILLAGE),
			0, 0);
		put(Quest.THE_FREMENNIK_TRIALS,
			List.of(
				new SkillReq(Skill.FLETCHING, 25),
				new SkillReq(Skill.WOODCUTTING, 40),
				new SkillReq(Skill.CRAFTING, 40)),
			List.of(),
			0, 0);
		put(Quest.LOST_CITY,
			List.of(
				new SkillReq(Skill.CRAFTING, 31),
				new SkillReq(Skill.WOODCUTTING, 36)),
			List.of(),
			0, 0);
		put(Quest.RUNE_MYSTERIES,
			List.of(), List.of(), 0, 0);
		put(Quest.SHILO_VILLAGE,
			List.of(
				new SkillReq(Skill.CRAFTING, 20),
				new SkillReq(Skill.AGILITY, 32)),
			List.of(Quest.JUNGLE_POTION),
			0, 0);
		put(Quest.JUNGLE_POTION,
			List.of(new SkillReq(Skill.HERBLORE, 3)),
			List.of(Quest.DRUIDIC_RITUAL),
			0, 0);
		put(Quest.DRUIDIC_RITUAL,
			List.of(), List.of(), 0, 0);

		// Eadgar's Ruse branch
		// Note: the wiki also lists Death Plateau as a "direct" prereq,
		// but it's already transitive via Troll Stronghold. We only
		// record the true-direct prereqs so the DAG stays clean — the
		// recursive seeder picks up Death Plateau automatically.
		put(Quest.EADGARS_RUSE,
			List.of(new SkillReq(Skill.HERBLORE, 31)),
			List.of(Quest.TROLL_STRONGHOLD),
			0, 0);
		put(Quest.TROLL_STRONGHOLD,
			List.of(new SkillReq(Skill.AGILITY, 15)),
			List.of(Quest.DEATH_PLATEAU),
			0, 0);
		put(Quest.DEATH_PLATEAU,
			List.of(), List.of(), 0, 0);

		// --- Legends' Quest (wiki-sourced 2026-04-09) ---
		// DS2 prereq. Heavy skill + quest + QP requirements.
		put(Quest.LEGENDS_QUEST,
			List.of(
				new SkillReq(Skill.AGILITY, 50),
				new SkillReq(Skill.CRAFTING, 50),
				new SkillReq(Skill.HERBLORE, 45),
				new SkillReq(Skill.MAGIC, 56),
				new SkillReq(Skill.MINING, 52),
				new SkillReq(Skill.PRAYER, 42),
				new SkillReq(Skill.SMITHING, 50),
				new SkillReq(Skill.STRENGTH, 50),
				new SkillReq(Skill.THIEVING, 50),
				new SkillReq(Skill.WOODCUTTING, 50)),
			List.of(
				Quest.FAMILY_CREST,
				Quest.HEROES_QUEST,
				Quest.SHILO_VILLAGE,
				Quest.UNDERGROUND_PASS,
				Quest.WATERFALL_QUEST),
			107, 0);

		// --- DS2 prereq chain (wiki-sourced 2026-04-09) ---

		// Bone Voyage: requires The Dig Site + 100 Kudos (not a goal type).
		put(Quest.BONE_VOYAGE,
			List.of(),
			List.of(Quest.THE_DIG_SITE),
			0, 0);

		// Client of Kourend: requires X Marks the Spot.
		put(Quest.CLIENT_OF_KOUREND,
			List.of(),
			List.of(Quest.X_MARKS_THE_SPOT),
			0, 0);
		put(Quest.X_MARKS_THE_SPOT,
			List.of(), List.of(), 0, 0);

		// Ghosts Ahoy: Agility 25 + Cooking 20 + Priest in Peril + Restless Ghost.
		put(Quest.GHOSTS_AHOY,
			List.of(
				new SkillReq(Skill.AGILITY, 25),
				new SkillReq(Skill.COOKING, 20)),
			List.of(Quest.PRIEST_IN_PERIL, Quest.THE_RESTLESS_GHOST),
			0, 0);
		put(Quest.PRIEST_IN_PERIL,
			List.of(), List.of(), 0, 0);
		put(Quest.THE_RESTLESS_GHOST,
			List.of(), List.of(), 0, 0);

		// A Tail of Two Cats: requires Icthlarin's Little Helper.
		put(Quest.A_TAIL_OF_TWO_CATS,
			List.of(),
			List.of(Quest.ICTHLARINS_LITTLE_HELPER),
			0, 0);
		put(Quest.ICTHLARINS_LITTLE_HELPER,
			List.of(),
			List.of(Quest.GERTRUDES_CAT),
			0, 0);
		put(Quest.GERTRUDES_CAT,
			List.of(), List.of(), 0, 0);

		// Animal Magnetism: requires The Restless Ghost + Ernest the Chicken
		// + Priest in Peril + skills. (Both Restless Ghost and Priest in
		// Peril already have entries above.)
		put(Quest.ANIMAL_MAGNETISM,
			List.of(
				new SkillReq(Skill.SLAYER, 18),
				new SkillReq(Skill.CRAFTING, 19),
				new SkillReq(Skill.RANGED, 30),
				new SkillReq(Skill.WOODCUTTING, 35)),
			List.of(Quest.THE_RESTLESS_GHOST, Quest.ERNEST_THE_CHICKEN, Quest.PRIEST_IN_PERIL),
			0, 0);
		put(Quest.ERNEST_THE_CHICKEN,
			List.of(), List.of(), 0, 0);

		// Family Crest: Mining 40 + Smithing 40 + Magic 59 + Crafting 40.
		// No quest prereqs.
		put(Quest.FAMILY_CREST,
			List.of(
				new SkillReq(Skill.MINING, 40),
				new SkillReq(Skill.SMITHING, 40),
				new SkillReq(Skill.MAGIC, 59),
				new SkillReq(Skill.CRAFTING, 40)),
			List.of(),
			0, 0);

		// Heroes' Quest: 4 skills + 4 quest prereqs + 55 QP.
		put(Quest.HEROES_QUEST,
			List.of(
				new SkillReq(Skill.COOKING, 53),
				new SkillReq(Skill.FISHING, 53),
				new SkillReq(Skill.HERBLORE, 25),
				new SkillReq(Skill.MINING, 50)),
			List.of(
				Quest.SHIELD_OF_ARRAV,
				Quest.LOST_CITY,
				Quest.MERLINS_CRYSTAL,
				Quest.DRAGON_SLAYER_I),
			55, 0);
		put(Quest.SHIELD_OF_ARRAV,
			List.of(), List.of(), 0, 0);
		put(Quest.MERLINS_CRYSTAL,
			List.of(), List.of(), 0, 0);
		put(Quest.DRAGON_SLAYER_I,
			List.of(), List.of(), 32, 0);

		// Underground Pass: Ranged 25 + Biohazard chain.
		put(Quest.UNDERGROUND_PASS,
			List.of(new SkillReq(Skill.RANGED, 25)),
			List.of(Quest.BIOHAZARD),
			0, 0);
		put(Quest.BIOHAZARD,
			List.of(),
			List.of(Quest.PLAGUE_CITY),
			0, 0);
		put(Quest.PLAGUE_CITY,
			List.of(), List.of(), 0, 0);

		// Waterfall Quest: no requirements.
		put(Quest.WATERFALL_QUEST,
			List.of(), List.of(), 0, 0);

		// --- Dragon Slayer II stress test ---
		put(Quest.DRAGON_SLAYER_II,
			List.of(
				new SkillReq(Skill.MAGIC, 75),
				new SkillReq(Skill.SMITHING, 70),
				new SkillReq(Skill.MINING, 68),
				new SkillReq(Skill.CRAFTING, 62),
				new SkillReq(Skill.AGILITY, 60),
				new SkillReq(Skill.THIEVING, 60),
				new SkillReq(Skill.HERBLORE, 55)),
			List.of(
				Quest.LEGENDS_QUEST,
				Quest.DREAM_MENTOR,
				Quest.A_TAIL_OF_TWO_CATS,
				Quest.ANIMAL_MAGNETISM,
				Quest.GHOSTS_AHOY,
				Quest.BONE_VOYAGE,
				Quest.CLIENT_OF_KOUREND),
			200, 0);

		// --- Song of the Elves + full Elf chain (wiki-sourced 2026-04-09) ---
		put(Quest.SONG_OF_THE_ELVES,
			List.of(
				new SkillReq(Skill.AGILITY, 70),
				new SkillReq(Skill.CONSTRUCTION, 70),
				new SkillReq(Skill.FARMING, 70),
				new SkillReq(Skill.HERBLORE, 70),
				new SkillReq(Skill.HUNTER, 70),
				new SkillReq(Skill.MINING, 70),
				new SkillReq(Skill.SMITHING, 70),
				new SkillReq(Skill.WOODCUTTING, 70)),
			List.of(Quest.MOURNINGS_END_PART_II),
			0, 0);
		put(Quest.MOURNINGS_END_PART_II,
			List.of(),
			List.of(Quest.MOURNINGS_END_PART_I),
			0, 0);
		put(Quest.MOURNINGS_END_PART_I,
			List.of(
				new SkillReq(Skill.RANGED, 60),
				new SkillReq(Skill.THIEVING, 50)),
			List.of(Quest.ROVING_ELVES, Quest.BIG_CHOMPY_BIRD_HUNTING, Quest.SHEEP_HERDER),
			0, 0);
		put(Quest.ROVING_ELVES,
			List.of(new SkillReq(Skill.AGILITY, 56)),
			List.of(Quest.REGICIDE, Quest.WATERFALL_QUEST),
			0, 0);
		put(Quest.REGICIDE,
			List.of(
				new SkillReq(Skill.CRAFTING, 10),
				new SkillReq(Skill.AGILITY, 56)),
			List.of(Quest.UNDERGROUND_PASS),
			0, 0);
		put(Quest.BIG_CHOMPY_BIRD_HUNTING,
			List.of(
				new SkillReq(Skill.FLETCHING, 5),
				new SkillReq(Skill.COOKING, 30),
				new SkillReq(Skill.RANGED, 30)),
			List.of(),
			0, 0);
		put(Quest.SHEEP_HERDER,
			List.of(), List.of(), 0, 0);
	}

	private static void put(Quest quest, List<SkillReq> skills, List<Quest> prereqQuests,
		int questPoints, int combatLevel)
	{
		TABLE.put(quest, new Reqs(skills, prereqQuests, questPoints, combatLevel));
	}

	/**
	 * Look up the requirements for a quest.
	 *
	 * @return the requirements, or {@code null} if this quest is not in
	 *         the data table. A null return means "no data", not "no
	 *         requirements" — an empty {@link Reqs} is used to express
	 *         the latter (see {@link Quest#COOKS_ASSISTANT}).
	 */
	public static Reqs lookup(Quest quest)
	{
		if (quest == null) return null;
		return TABLE.get(quest);
	}

	/** True iff the quest has a non-empty requirements entry. */
	public static boolean hasRequirements(Quest quest)
	{
		Reqs r = lookup(quest);
		if (r == null) return false;
		return !r.skills.isEmpty() || !r.prereqQuests.isEmpty()
			|| r.questPoints > 0 || r.combatLevel > 0;
	}

	private QuestRequirements() {}
}
