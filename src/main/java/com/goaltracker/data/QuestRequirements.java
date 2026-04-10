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
 * <p><b>Coverage.</b> This table covers all 209 quests in the
 * RuneLite {@link Quest} enum. Requirements are wiki-sourced.
 * Sailing skill requirements are omitted (not yet in RuneLite API).
 *
 * <p><b>Account-wide requirements.</b> Quest points, combat level,
 * and museum kudos are recorded as numeric fields on {@link Reqs} and
 * resolved into {@code GoalType.ACCOUNT} goal templates by the
 * {@link QuestRequirementResolver}.
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
		/** Minimum total quest points required. 0 = none. */
		public final int questPoints;
		/** Minimum combat level required. 0 = none. */
		public final int combatLevel;
		/** Minimum museum kudos required. 0 = none. */
		public final int kudos;
		public Reqs(List<SkillReq> skills, List<Quest> prereqQuests, int questPoints, int combatLevel)
		{
			this(skills, prereqQuests, questPoints, combatLevel, 0);
		}

		public Reqs(List<SkillReq> skills, List<Quest> prereqQuests, int questPoints, int combatLevel, int kudos)
		{
			this.skills = Collections.unmodifiableList(skills);
			this.prereqQuests = Collections.unmodifiableList(prereqQuests);
			this.questPoints = questPoints;
			this.combatLevel = combatLevel;
			this.kudos = kudos;
		}
	}

	private static final Map<Quest, Reqs> TABLE = new EnumMap<>(Quest.class);

	/** The complete set of F2P quests, used to auto-tag quest goals. */
	private static final java.util.Set<Quest> F2P_QUESTS = java.util.EnumSet.of(
		Quest.BELOW_ICE_MOUNTAIN,
		Quest.BLACK_KNIGHTS_FORTRESS,
		Quest.COOKS_ASSISTANT,
		Quest.THE_CORSAIR_CURSE,
		Quest.DEMON_SLAYER,
		Quest.DORICS_QUEST,
		Quest.DRAGON_SLAYER_I,
		Quest.ERNEST_THE_CHICKEN,
		Quest.GOBLIN_DIPLOMACY,
		Quest.IMP_CATCHER,
		Quest.THE_IDES_OF_MILK,
		Quest.THE_KNIGHTS_SWORD,
		Quest.MISTHALIN_MYSTERY,
		Quest.PIRATES_TREASURE,
		Quest.PRINCE_ALI_RESCUE,
		Quest.THE_RESTLESS_GHOST,
		Quest.ROMEO__JULIET,
		Quest.RUNE_MYSTERIES,
		Quest.SHEEP_SHEARER,
		Quest.SHIELD_OF_ARRAV,
		Quest.VAMPYRE_SLAYER,
		Quest.WITCHS_POTION,
		Quest.X_MARKS_THE_SPOT
	);

	static
	{
		// ============================================================
		// F2P quests (22 total)
		// ============================================================

		put(Quest.COOKS_ASSISTANT,
			List.of(), List.of(), 0, 0);
		put(Quest.DEMON_SLAYER,
			List.of(), List.of(), 0, 0);
		put(Quest.DORICS_QUEST,
			List.of(), List.of(), 0, 0);
		put(Quest.GOBLIN_DIPLOMACY,
			List.of(), List.of(), 0, 0);
		put(Quest.IMP_CATCHER,
			List.of(), List.of(), 0, 0);
		put(Quest.THE_KNIGHTS_SWORD,
			List.of(new SkillReq(Skill.MINING, 10)),
			List.of(), 0, 0);
		put(Quest.MISTHALIN_MYSTERY,
			List.of(), List.of(), 0, 0);
		put(Quest.PIRATES_TREASURE,
			List.of(), List.of(), 0, 0);
		put(Quest.PRINCE_ALI_RESCUE,
			List.of(), List.of(), 0, 0);
		put(Quest.ROMEO__JULIET,
			List.of(), List.of(), 0, 0);
		put(Quest.SHEEP_SHEARER,
			List.of(), List.of(), 0, 0);
		put(Quest.VAMPYRE_SLAYER,
			List.of(), List.of(), 0, 0);
		put(Quest.WITCHS_POTION,
			List.of(), List.of(), 0, 0);
		put(Quest.BLACK_KNIGHTS_FORTRESS,
			List.of(), List.of(), 12, 0);
		put(Quest.THE_CORSAIR_CURSE,
			List.of(), List.of(), 0, 0);
		put(Quest.BELOW_ICE_MOUNTAIN,
			List.of(new SkillReq(Skill.MINING, 10)),
			List.of(), 0, 0);
		put(Quest.THE_IDES_OF_MILK,
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

		// Bone Voyage: requires The Dig Site + 100 Kudos.
		TABLE.put(Quest.BONE_VOYAGE, new Reqs(
			List.of(),
			List.of(Quest.THE_DIG_SITE),
			0, 0, 100));

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

		// ============================================================
		// A-D members quests + dependencies (wiki-sourced 2026-04-09)
		// ============================================================

		// --- A quests ---

		// A Porcine of Interest: no requirements.
		put(Quest.A_PORCINE_OF_INTEREST,
			List.of(), List.of(), 0, 0);

		// A Soul's Bane: no requirements.
		put(Quest.A_SOULS_BANE,
			List.of(), List.of(), 0, 0);

		// Another Slice of H.A.M.: Attack 15, Prayer 25 + quest chain.
		put(Quest.ANOTHER_SLICE_OF_HAM,
			List.of(
				new SkillReq(Skill.ATTACK, 15),
				new SkillReq(Skill.PRAYER, 25)),
			List.of(
				Quest.DEATH_TO_THE_DORGESHUUN,
				Quest.THE_GIANT_DWARF,
				Quest.THE_DIG_SITE),
			0, 0);

		// Death to the Dorgeshuun: Agility 23, Thieving 23.
		put(Quest.DEATH_TO_THE_DORGESHUUN,
			List.of(
				new SkillReq(Skill.AGILITY, 23),
				new SkillReq(Skill.THIEVING, 23)),
			List.of(Quest.THE_LOST_TRIBE),
			0, 0);
		put(Quest.THE_LOST_TRIBE,
			List.of(
				new SkillReq(Skill.AGILITY, 13),
				new SkillReq(Skill.THIEVING, 13),
				new SkillReq(Skill.MINING, 17)),
			List.of(Quest.GOBLIN_DIPLOMACY, Quest.RUNE_MYSTERIES),
			0, 0);
		put(Quest.THE_GIANT_DWARF,
			List.of(
				new SkillReq(Skill.CRAFTING, 12),
				new SkillReq(Skill.FIREMAKING, 16),
				new SkillReq(Skill.MAGIC, 33),
				new SkillReq(Skill.THIEVING, 14)),
			List.of(),
			0, 0);
		put(Quest.THE_DIG_SITE,
			List.of(
				new SkillReq(Skill.AGILITY, 10),
				new SkillReq(Skill.HERBLORE, 10),
				new SkillReq(Skill.THIEVING, 25)),
			List.of(Quest.DRUIDIC_RITUAL),
			0, 0);

		// At First Light: Hunter 46, Herblore 30, Construction 27.
		put(Quest.AT_FIRST_LIGHT,
			List.of(
				new SkillReq(Skill.HUNTER, 46),
				new SkillReq(Skill.HERBLORE, 30),
				new SkillReq(Skill.CONSTRUCTION, 27)),
			List.of(Quest.CHILDREN_OF_THE_SUN, Quest.EAGLES_PEAK),
			0, 0);
		put(Quest.CHILDREN_OF_THE_SUN,
			List.of(), List.of(), 0, 0);
		put(Quest.EAGLES_PEAK,
			List.of(new SkillReq(Skill.HUNTER, 27)),
			List.of(),
			0, 0);

		// A Taste of Hope: Crafting 48, Agility 45, Attack 40, Herblore 40, Slayer 38.
		put(Quest.A_TASTE_OF_HOPE,
			List.of(
				new SkillReq(Skill.CRAFTING, 48),
				new SkillReq(Skill.AGILITY, 45),
				new SkillReq(Skill.ATTACK, 40),
				new SkillReq(Skill.HERBLORE, 40),
				new SkillReq(Skill.SLAYER, 38)),
			List.of(Quest.DARKNESS_OF_HALLOWVALE),
			0, 0);

		// A Night at the Theatre: no skill reqs, requires A Taste of Hope chain.
		put(Quest.A_NIGHT_AT_THE_THEATRE,
			List.of(),
			List.of(Quest.A_TASTE_OF_HOPE),
			0, 0);

		// A Kingdom Divided: heavy skill + Kourend quest line.
		put(Quest.A_KINGDOM_DIVIDED,
			List.of(
				new SkillReq(Skill.AGILITY, 54),
				new SkillReq(Skill.THIEVING, 52),
				new SkillReq(Skill.WOODCUTTING, 52),
				new SkillReq(Skill.HERBLORE, 50),
				new SkillReq(Skill.MINING, 42),
				new SkillReq(Skill.CRAFTING, 38),
				new SkillReq(Skill.MAGIC, 35)),
			List.of(
				Quest.THE_DEPTHS_OF_DESPAIR,
				Quest.THE_QUEEN_OF_THIEVES,
				Quest.THE_ASCENT_OF_ARCEUUS,
				Quest.THE_FORSAKEN_TOWER,
				Quest.TALE_OF_THE_RIGHTEOUS),
			0, 0);
		put(Quest.THE_DEPTHS_OF_DESPAIR,
			List.of(new SkillReq(Skill.AGILITY, 18)),
			List.of(Quest.CLIENT_OF_KOUREND),
			0, 0);
		put(Quest.THE_QUEEN_OF_THIEVES,
			List.of(new SkillReq(Skill.THIEVING, 20)),
			List.of(Quest.CLIENT_OF_KOUREND),
			0, 0);
		put(Quest.THE_ASCENT_OF_ARCEUUS,
			List.of(new SkillReq(Skill.HUNTER, 12)),
			List.of(Quest.CLIENT_OF_KOUREND),
			0, 0);
		put(Quest.THE_FORSAKEN_TOWER,
			List.of(),
			List.of(Quest.CLIENT_OF_KOUREND),
			0, 0);
		put(Quest.TALE_OF_THE_RIGHTEOUS,
			List.of(),
			List.of(Quest.CLIENT_OF_KOUREND),
			0, 0);

		// --- Myreque chain (dependency of A Taste of Hope) ---

		put(Quest.DARKNESS_OF_HALLOWVALE,
			List.of(
				new SkillReq(Skill.MINING, 20),
				new SkillReq(Skill.THIEVING, 22),
				new SkillReq(Skill.AGILITY, 26),
				new SkillReq(Skill.CRAFTING, 32),
				new SkillReq(Skill.MAGIC, 33),
				new SkillReq(Skill.STRENGTH, 40),
				new SkillReq(Skill.CONSTRUCTION, 5)),
			List.of(Quest.IN_AID_OF_THE_MYREQUE),
			0, 0);
		put(Quest.IN_AID_OF_THE_MYREQUE,
			List.of(
				new SkillReq(Skill.CRAFTING, 25),
				new SkillReq(Skill.MINING, 15),
				new SkillReq(Skill.MAGIC, 7)),
			List.of(Quest.IN_SEARCH_OF_THE_MYREQUE),
			0, 0);
		put(Quest.IN_SEARCH_OF_THE_MYREQUE,
			List.of(new SkillReq(Skill.AGILITY, 25)),
			List.of(Quest.NATURE_SPIRIT),
			0, 0);
		put(Quest.NATURE_SPIRIT,
			List.of(new SkillReq(Skill.CRAFTING, 18)),
			List.of(Quest.PRIEST_IN_PERIL, Quest.THE_RESTLESS_GHOST),
			0, 0);

		// --- B quests ---

		// Barbarian Training (miniquest): various skills, partial quest deps.
		put(Quest.BARBARIAN_TRAINING,
			List.of(
				new SkillReq(Skill.FISHING, 55),
				new SkillReq(Skill.FIREMAKING, 35),
				new SkillReq(Skill.STRENGTH, 35),
				new SkillReq(Skill.AGILITY, 15),
				new SkillReq(Skill.CRAFTING, 11),
				new SkillReq(Skill.SMITHING, 5),
				new SkillReq(Skill.HERBLORE, 4)),
			List.of(Quest.DRUIDIC_RITUAL, Quest.TAI_BWO_WANNAI_TRIO),
			0, 0);
		put(Quest.TAI_BWO_WANNAI_TRIO,
			List.of(
				new SkillReq(Skill.AGILITY, 15),
				new SkillReq(Skill.COOKING, 30),
				new SkillReq(Skill.FISHING, 5)),
			List.of(Quest.JUNGLE_POTION),
			0, 0);

		// Bear Your Soul (miniquest): no requirements.
		put(Quest.BEAR_YOUR_SOUL,
			List.of(), List.of(), 0, 0);

		// Beneath Cursed Sands: Agility 62, Crafting 55, Firemaking 55.
		put(Quest.BENEATH_CURSED_SANDS,
			List.of(
				new SkillReq(Skill.AGILITY, 62),
				new SkillReq(Skill.CRAFTING, 55),
				new SkillReq(Skill.FIREMAKING, 55)),
			List.of(Quest.CONTACT),
			0, 0);

		// Between a Rock: Defence 30, Mining 40, Smithing 50.
		put(Quest.BETWEEN_A_ROCK,
			List.of(
				new SkillReq(Skill.DEFENCE, 30),
				new SkillReq(Skill.MINING, 40),
				new SkillReq(Skill.SMITHING, 50)),
			List.of(Quest.DWARF_CANNON, Quest.FISHING_CONTEST),
			0, 0);

		// --- C quests ---

		// Cabin Fever: Agility 42, Crafting 45, Smithing 50, Ranged 40.
		put(Quest.CABIN_FEVER,
			List.of(
				new SkillReq(Skill.AGILITY, 42),
				new SkillReq(Skill.CRAFTING, 45),
				new SkillReq(Skill.SMITHING, 50),
				new SkillReq(Skill.RANGED, 40)),
			List.of(Quest.PIRATES_TREASURE, Quest.RUM_DEAL),
			0, 0);
		put(Quest.RUM_DEAL,
			List.of(
				new SkillReq(Skill.CRAFTING, 42),
				new SkillReq(Skill.FISHING, 50),
				new SkillReq(Skill.FARMING, 40),
				new SkillReq(Skill.PRAYER, 47),
				new SkillReq(Skill.SLAYER, 42)),
			List.of(Quest.ZOGRE_FLESH_EATERS),
			0, 0);
		put(Quest.ZOGRE_FLESH_EATERS,
			List.of(
				new SkillReq(Skill.SMITHING, 4),
				new SkillReq(Skill.HERBLORE, 8),
				new SkillReq(Skill.RANGED, 30)),
			List.of(Quest.BIG_CHOMPY_BIRD_HUNTING, Quest.JUNGLE_POTION),
			0, 0);

		// Clock Tower: no requirements.
		put(Quest.CLOCK_TOWER,
			List.of(), List.of(), 0, 0);

		// Cold War: Hunter 10, Agility 30, Crafting 30, Construction 34, Thieving 15.
		put(Quest.COLD_WAR,
			List.of(
				new SkillReq(Skill.HUNTER, 10),
				new SkillReq(Skill.AGILITY, 30),
				new SkillReq(Skill.CRAFTING, 30),
				new SkillReq(Skill.CONSTRUCTION, 34),
				new SkillReq(Skill.THIEVING, 15)),
			List.of(),
			0, 0);

		// Contact!: no skill reqs, quest prereqs.
		put(Quest.CONTACT,
			List.of(),
			List.of(Quest.PRINCE_ALI_RESCUE, Quest.ICTHLARINS_LITTLE_HELPER),
			0, 0);

		// Creature of Fenkenstrain: Crafting 20, Thieving 25.
		put(Quest.CREATURE_OF_FENKENSTRAIN,
			List.of(
				new SkillReq(Skill.CRAFTING, 20),
				new SkillReq(Skill.THIEVING, 25)),
			List.of(Quest.PRIEST_IN_PERIL, Quest.THE_RESTLESS_GHOST),
			0, 0);

		// Curse of the Empty Lord (miniquest): Thieving 53, Prayer 31.
		put(Quest.CURSE_OF_THE_EMPTY_LORD,
			List.of(
				new SkillReq(Skill.THIEVING, 53),
				new SkillReq(Skill.PRAYER, 31)),
			List.of(Quest.DESERT_TREASURE_I, Quest.THE_RESTLESS_GHOST),
			0, 0);

		// --- D quests ---

		// Daddy's Home (miniquest): no requirements.
		put(Quest.DADDYS_HOME,
			List.of(), List.of(), 0, 0);

		// Death on the Isle: Thieving 34, Agility 32.
		put(Quest.DEATH_ON_THE_ISLE,
			List.of(
				new SkillReq(Skill.THIEVING, 34),
				new SkillReq(Skill.AGILITY, 32)),
			List.of(Quest.CHILDREN_OF_THE_SUN),
			0, 0);

		// Defender of Varrock: Smithing 55, Hunter 52 + quest chain.
		put(Quest.DEFENDER_OF_VARROCK,
			List.of(
				new SkillReq(Skill.SMITHING, 55),
				new SkillReq(Skill.HUNTER, 52)),
			List.of(
				Quest.SHIELD_OF_ARRAV,
				Quest.TEMPLE_OF_IKOV,
				Quest.WHAT_LIES_BELOW,
				Quest.FAMILY_CREST,
				Quest.GARDEN_OF_TRANQUILLITY,
				Quest.CREATURE_OF_FENKENSTRAIN),
			0, 0);
		put(Quest.TEMPLE_OF_IKOV,
			List.of(new SkillReq(Skill.THIEVING, 42)),
			List.of(),
			0, 0);
		put(Quest.WHAT_LIES_BELOW,
			List.of(new SkillReq(Skill.RUNECRAFT, 35)),
			List.of(Quest.RUNE_MYSTERIES),
			0, 0);
		put(Quest.GARDEN_OF_TRANQUILLITY,
			List.of(new SkillReq(Skill.FARMING, 25)),
			List.of(Quest.CREATURE_OF_FENKENSTRAIN),
			0, 0);

		// Desert Treasure II: Firemaking 75, Magic 75, Thieving 70,
		// Herblore 62, Runecraft 60, Construction 60 + quest chain.
		put(Quest.DESERT_TREASURE_II__THE_FALLEN_EMPIRE,
			List.of(
				new SkillReq(Skill.FIREMAKING, 75),
				new SkillReq(Skill.MAGIC, 75),
				new SkillReq(Skill.THIEVING, 70),
				new SkillReq(Skill.HERBLORE, 62),
				new SkillReq(Skill.RUNECRAFT, 60),
				new SkillReq(Skill.CONSTRUCTION, 60)),
			List.of(
				Quest.DESERT_TREASURE_I,
				Quest.SECRETS_OF_THE_NORTH,
				Quest.ENAKHRAS_LAMENT,
				Quest.TEMPLE_OF_THE_EYE,
				Quest.THE_GARDEN_OF_DEATH,
				Quest.BELOW_ICE_MOUNTAIN,
				Quest.HIS_FAITHFUL_SERVANTS),
			0, 0);
		// --- Secrets of the North chain (wiki-sourced 2026-04-09) ---

		// Secrets of the North: Agility 69, Thieving 64, Hunter 56 + quest chain.
		put(Quest.SECRETS_OF_THE_NORTH,
			List.of(
				new SkillReq(Skill.AGILITY, 69),
				new SkillReq(Skill.THIEVING, 64),
				new SkillReq(Skill.HUNTER, 56)),
			List.of(
				Quest.MAKING_FRIENDS_WITH_MY_ARM,
				Quest.THE_GENERALS_SHADOW,
				Quest.DEVIOUS_MINDS,
				Quest.HAZEEL_CULT),
			0, 0);

		// Making Friends with My Arm: Firemaking 66, Mining 72, Construction 35, Agility 68.
		put(Quest.MAKING_FRIENDS_WITH_MY_ARM,
			List.of(
				new SkillReq(Skill.FIREMAKING, 66),
				new SkillReq(Skill.MINING, 72),
				new SkillReq(Skill.CONSTRUCTION, 35),
				new SkillReq(Skill.AGILITY, 68)),
			List.of(
				Quest.MY_ARMS_BIG_ADVENTURE,
				Quest.SWAN_SONG,
				Quest.COLD_WAR,
				Quest.ROMEO__JULIET),
			0, 0);
		// My Arm's Big Adventure: Farming 29, Woodcutting 10.
		put(Quest.MY_ARMS_BIG_ADVENTURE,
			List.of(
				new SkillReq(Skill.FARMING, 29),
				new SkillReq(Skill.WOODCUTTING, 10)),
			List.of(
				Quest.EADGARS_RUSE,
				Quest.THE_FEUD,
				Quest.JUNGLE_POTION),
			0, 0);
		// The Feud: Thieving 30.
		put(Quest.THE_FEUD,
			List.of(new SkillReq(Skill.THIEVING, 30)),
			List.of(),
			0, 0);
		// Swan Song: Magic 66, Cooking 62, Fishing 62, Smithing 45,
		// Firemaking 42, Crafting 40 + 100 QP.
		put(Quest.SWAN_SONG,
			List.of(
				new SkillReq(Skill.MAGIC, 66),
				new SkillReq(Skill.COOKING, 62),
				new SkillReq(Skill.FISHING, 62),
				new SkillReq(Skill.SMITHING, 45),
				new SkillReq(Skill.FIREMAKING, 42),
				new SkillReq(Skill.CRAFTING, 40)),
			List.of(
				Quest.ONE_SMALL_FAVOUR,
				Quest.GARDEN_OF_TRANQUILLITY),
			100, 0);
		// One Small Favour: Agility 36, Crafting 25, Herblore 18, Smithing 30.
		put(Quest.ONE_SMALL_FAVOUR,
			List.of(
				new SkillReq(Skill.AGILITY, 36),
				new SkillReq(Skill.CRAFTING, 25),
				new SkillReq(Skill.HERBLORE, 18),
				new SkillReq(Skill.SMITHING, 30)),
			List.of(Quest.RUNE_MYSTERIES, Quest.SHILO_VILLAGE),
			0, 0);

		// The General's Shadow (miniquest): no skills, quest prereqs.
		put(Quest.THE_GENERALS_SHADOW,
			List.of(),
			List.of(
				Quest.CURSE_OF_THE_EMPTY_LORD,
				Quest.DESERT_TREASURE_I,
				Quest.FIGHT_ARENA),
			0, 0);
		// Fight Arena: no requirements.
		put(Quest.FIGHT_ARENA,
			List.of(), List.of(), 0, 0);

		// Hazeel Cult: no requirements.
		put(Quest.HAZEEL_CULT,
			List.of(), List.of(), 0, 0);

		put(Quest.ENAKHRAS_LAMENT,
			List.of(
				new SkillReq(Skill.CRAFTING, 50),
				new SkillReq(Skill.FIREMAKING, 45),
				new SkillReq(Skill.PRAYER, 43),
				new SkillReq(Skill.MAGIC, 39)),
			List.of(),
			0, 0);
		put(Quest.TEMPLE_OF_THE_EYE,
			List.of(), List.of(Quest.ENTER_THE_ABYSS),
			0, 0);
		put(Quest.ENTER_THE_ABYSS,
			List.of(), List.of(Quest.RUNE_MYSTERIES),
			0, 0);
		// The Garden of Death: Farming 20.
		put(Quest.THE_GARDEN_OF_DEATH,
			List.of(new SkillReq(Skill.FARMING, 20)),
			List.of(), 0, 0);
		// His Faithful Servants (miniquest): Priest in Peril.
		put(Quest.HIS_FAITHFUL_SERVANTS,
			List.of(),
			List.of(Quest.PRIEST_IN_PERIL),
			0, 0);

		// Devious Minds: Smithing 65, Runecraft 50, Fletching 50.
		put(Quest.DEVIOUS_MINDS,
			List.of(
				new SkillReq(Skill.SMITHING, 65),
				new SkillReq(Skill.RUNECRAFT, 50),
				new SkillReq(Skill.FLETCHING, 50)),
			List.of(
				Quest.WANTED,
				Quest.TROLL_STRONGHOLD,
				Quest.DORICS_QUEST,
				Quest.ENTER_THE_ABYSS),
			0, 0);
		put(Quest.WANTED,
			List.of(),
			List.of(Quest.RECRUITMENT_DRIVE, Quest.THE_LOST_TRIBE, Quest.PRIEST_IN_PERIL),
			32, 0);
		put(Quest.RECRUITMENT_DRIVE,
			List.of(),
			List.of(Quest.BLACK_KNIGHTS_FORTRESS, Quest.DRUIDIC_RITUAL),
			0, 0);

		// Dwarf Cannon: no requirements.
		put(Quest.DWARF_CANNON,
			List.of(), List.of(), 0, 0);

		// Fishing Contest: Fishing 10.
		put(Quest.FISHING_CONTEST,
			List.of(new SkillReq(Skill.FISHING, 10)),
			List.of(),
			0, 0);

		// ============================================================
		// E-H members quests + dependencies (wiki-sourced 2026-04-09)
		// ============================================================

		// Elemental Workshop I: no hard requirements (skills boostable, not required to start).
		put(Quest.ELEMENTAL_WORKSHOP_I,
			List.of(), List.of(), 0, 0);
		// Elemental Workshop II: Magic 20, Smithing 30.
		put(Quest.ELEMENTAL_WORKSHOP_II,
			List.of(
				new SkillReq(Skill.MAGIC, 20),
				new SkillReq(Skill.SMITHING, 30)),
			List.of(Quest.ELEMENTAL_WORKSHOP_I),
			0, 0);

		// Enlightened Journey: Firemaking 20, Farming 30, Crafting 36, 20 QP.
		put(Quest.ENLIGHTENED_JOURNEY,
			List.of(
				new SkillReq(Skill.FIREMAKING, 20),
				new SkillReq(Skill.FARMING, 30),
				new SkillReq(Skill.CRAFTING, 36)),
			List.of(),
			20, 0);

		// The Enchanted Key (miniquest): Making History prereq.
		put(Quest.THE_ENCHANTED_KEY,
			List.of(),
			List.of(Quest.MAKING_HISTORY),
			0, 0);
		// Making History: Priest in Peril + Restless Ghost.
		put(Quest.MAKING_HISTORY,
			List.of(),
			List.of(Quest.PRIEST_IN_PERIL, Quest.THE_RESTLESS_GHOST),
			0, 0);

		// The Eyes of Glouphrie: Construction 5, Magic 46.
		put(Quest.THE_EYES_OF_GLOUPHRIE,
			List.of(
				new SkillReq(Skill.CONSTRUCTION, 5),
				new SkillReq(Skill.MAGIC, 46)),
			List.of(Quest.THE_GRAND_TREE),
			0, 0);

		// --- Fairytale chain ---
		put(Quest.FAIRYTALE_I__GROWING_PAINS,
			List.of(),
			List.of(Quest.LOST_CITY, Quest.NATURE_SPIRIT),
			0, 0);
		put(Quest.FAIRYTALE_II__CURE_A_QUEEN,
			List.of(
				new SkillReq(Skill.THIEVING, 40),
				new SkillReq(Skill.FARMING, 49),
				new SkillReq(Skill.HERBLORE, 57)),
			List.of(Quest.FAIRYTALE_I__GROWING_PAINS),
			0, 0);

		// Family Pest (miniquest): Family Crest prereq.
		put(Quest.FAMILY_PEST,
			List.of(),
			List.of(Quest.FAMILY_CREST),
			0, 0);

		// Forgettable Tale: Cooking 22, Farming 17.
		put(Quest.FORGETTABLE_TALE,
			List.of(
				new SkillReq(Skill.COOKING, 22),
				new SkillReq(Skill.FARMING, 17)),
			List.of(Quest.THE_GIANT_DWARF, Quest.FISHING_CONTEST),
			0, 0);

		// --- Fremennik chain ---
		put(Quest.THE_FREMENNIK_ISLES,
			List.of(new SkillReq(Skill.CONSTRUCTION, 20)),
			List.of(Quest.THE_FREMENNIK_TRIALS),
			0, 0);
		put(Quest.THE_FREMENNIK_EXILES,
			List.of(
				new SkillReq(Skill.CRAFTING, 65),
				new SkillReq(Skill.SLAYER, 60),
				new SkillReq(Skill.SMITHING, 60),
				new SkillReq(Skill.FISHING, 60),
				new SkillReq(Skill.RUNECRAFT, 55)),
			List.of(
				Quest.THE_FREMENNIK_ISLES,
				Quest.LUNAR_DIPLOMACY,
				Quest.MOUNTAIN_DAUGHTER,
				Quest.HEROES_QUEST),
			0, 0);
		// Mountain Daughter: no hard requirements.
		put(Quest.MOUNTAIN_DAUGHTER,
			List.of(), List.of(), 0, 0);

		// Getting Ahead: Crafting 30, Construction 26.
		put(Quest.GETTING_AHEAD,
			List.of(
				new SkillReq(Skill.CRAFTING, 30),
				new SkillReq(Skill.CONSTRUCTION, 26)),
			List.of(),
			0, 0);

		// The Golem: no hard requirements.
		put(Quest.THE_GOLEM,
			List.of(), List.of(), 0, 0);

		// Grim Tales: Farming 45, Herblore 52, Thieving 58, Agility 59, Woodcutting 71.
		put(Quest.GRIM_TALES,
			List.of(
				new SkillReq(Skill.FARMING, 45),
				new SkillReq(Skill.HERBLORE, 52),
				new SkillReq(Skill.THIEVING, 58),
				new SkillReq(Skill.AGILITY, 59),
				new SkillReq(Skill.WOODCUTTING, 71)),
			List.of(Quest.WITCHS_HOUSE),
			0, 0);
		put(Quest.WITCHS_HOUSE,
			List.of(), List.of(), 0, 0);

		// The Great Brain Robbery: Crafting 16, Construction 30, Prayer 50.
		put(Quest.THE_GREAT_BRAIN_ROBBERY,
			List.of(
				new SkillReq(Skill.CRAFTING, 16),
				new SkillReq(Skill.CONSTRUCTION, 30),
				new SkillReq(Skill.PRAYER, 50)),
			List.of(
				Quest.CREATURE_OF_FENKENSTRAIN,
				Quest.CABIN_FEVER,
				Quest.RECIPE_FOR_DISASTER__PIRATE_PETE),
			0, 0);

		// The Hand in the Sand: Thieving 17, Crafting 49.
		put(Quest.THE_HAND_IN_THE_SAND,
			List.of(
				new SkillReq(Skill.THIEVING, 17),
				new SkillReq(Skill.CRAFTING, 49)),
			List.of(),
			0, 0);

		// Haunted Mine: Priest in Peril.
		put(Quest.HAUNTED_MINE,
			List.of(),
			List.of(Quest.PRIEST_IN_PERIL),
			0, 0);

		// Holy Grail: Attack 20 + Merlin's Crystal.
		put(Quest.HOLY_GRAIL,
			List.of(new SkillReq(Skill.ATTACK, 20)),
			List.of(Quest.MERLINS_CRYSTAL),
			0, 0);

		// Hopespear's Will (miniquest): Prayer 50 + quest chain.
		put(Quest.HOPESPEARS_WILL,
			List.of(new SkillReq(Skill.PRAYER, 50)),
			List.of(
				Quest.DESERT_TREASURE_I,
				Quest.FAIRYTALE_II__CURE_A_QUEEN,
				Quest.LAND_OF_THE_GOBLINS),
			0, 0);

		// ============================================================
		// I-O members quests + dependencies (wiki-sourced 2026-04-09)
		// ============================================================

		// In Search of Knowledge (miniquest): no requirements.
		put(Quest.IN_SEARCH_OF_KNOWLEDGE,
			List.of(), List.of(), 0, 0);

		// King's Ransom: Magic 45, Defence 65 + quest chain.
		put(Quest.KINGS_RANSOM,
			List.of(
				new SkillReq(Skill.MAGIC, 45),
				new SkillReq(Skill.DEFENCE, 65)),
			List.of(
				Quest.BLACK_KNIGHTS_FORTRESS,
				Quest.HOLY_GRAIL,
				Quest.MURDER_MYSTERY,
				Quest.ONE_SMALL_FAVOUR),
			0, 0);
		put(Quest.MURDER_MYSTERY,
			List.of(), List.of(), 0, 0);

		// Lair of Tarn Razorlor (miniquest): Slayer 40 + Haunted Mine.
		put(Quest.LAIR_OF_TARN_RAZORLOR,
			List.of(new SkillReq(Skill.SLAYER, 40)),
			List.of(Quest.HAUNTED_MINE),
			0, 0);

		// Land of the Goblins: Agility 38, Fishing 40, Thieving 45, Herblore 48.
		put(Quest.LAND_OF_THE_GOBLINS,
			List.of(
				new SkillReq(Skill.AGILITY, 38),
				new SkillReq(Skill.FISHING, 40),
				new SkillReq(Skill.THIEVING, 45),
				new SkillReq(Skill.HERBLORE, 48)),
			List.of(Quest.ANOTHER_SLICE_OF_HAM),
			0, 0);

		// Mage Arena I (miniquest): Magic 60.
		put(Quest.MAGE_ARENA_I,
			List.of(new SkillReq(Skill.MAGIC, 60)),
			List.of(),
			0, 0);
		// Mage Arena II (miniquest): Magic 75.
		put(Quest.MAGE_ARENA_II,
			List.of(new SkillReq(Skill.MAGIC, 75)),
			List.of(Quest.MAGE_ARENA_I),
			0, 0);

		// Monkey Madness II: Slayer 69, Crafting 70, Hunter 60,
		// Agility 55, Thieving 55, Firemaking 60 + quest chain.
		put(Quest.MONKEY_MADNESS_II,
			List.of(
				new SkillReq(Skill.SLAYER, 69),
				new SkillReq(Skill.CRAFTING, 70),
				new SkillReq(Skill.HUNTER, 60),
				new SkillReq(Skill.AGILITY, 55),
				new SkillReq(Skill.THIEVING, 55),
				new SkillReq(Skill.FIREMAKING, 60)),
			List.of(
				Quest.ENLIGHTENED_JOURNEY,
				Quest.THE_EYES_OF_GLOUPHRIE,
				Quest.RECIPE_FOR_DISASTER__KING_AWOWOGEI,
				Quest.MONKEY_MADNESS_I,
				Quest.TROLL_STRONGHOLD,
				Quest.WATCHTOWER),
			0, 0);
		// Watchtower: no hard requirements.
		put(Quest.WATCHTOWER,
			List.of(), List.of(), 0, 0);

		// Monk's Friend: no requirements.
		put(Quest.MONKS_FRIEND,
			List.of(), List.of(), 0, 0);

		// Observatory Quest: no requirements.
		put(Quest.OBSERVATORY_QUEST,
			List.of(), List.of(), 0, 0);

		// Olaf's Quest: Firemaking 40, Woodcutting 50 + Fremennik Trials.
		put(Quest.OLAFS_QUEST,
			List.of(
				new SkillReq(Skill.FIREMAKING, 40),
				new SkillReq(Skill.WOODCUTTING, 50)),
			List.of(Quest.THE_FREMENNIK_TRIALS),
			0, 0);

		// ============================================================
		// P-T members quests + dependencies (wiki-sourced 2026-04-09)
		// ============================================================

		// --- Recipe for Disaster chain ---

		// RFD: Another Cook's Quest (initial): Cooking 10 + Cook's Assistant.
		put(Quest.RECIPE_FOR_DISASTER__ANOTHER_COOKS_QUEST,
			List.of(new SkillReq(Skill.COOKING, 10)),
			List.of(Quest.COOKS_ASSISTANT),
			0, 0);
		// RFD wrapper (same prereqs as initial subquest).
		put(Quest.RECIPE_FOR_DISASTER,
			List.of(new SkillReq(Skill.COOKING, 10)),
			List.of(Quest.COOKS_ASSISTANT),
			0, 0);
		// RFD: Mountain Dwarf: Fishing Contest.
		put(Quest.RECIPE_FOR_DISASTER__MOUNTAIN_DWARF,
			List.of(),
			List.of(Quest.FISHING_CONTEST),
			0, 0);
		// RFD: Wartface & Bentnoze: Goblin Diplomacy.
		put(Quest.RECIPE_FOR_DISASTER__WARTFACE__BENTNOZE,
			List.of(),
			List.of(Quest.GOBLIN_DIPLOMACY),
			0, 0);
		// RFD: Pirate Pete: Cooking 31.
		put(Quest.RECIPE_FOR_DISASTER__PIRATE_PETE,
			List.of(new SkillReq(Skill.COOKING, 31)),
			List.of(),
			0, 0);
		// RFD: Lumbridge Guide: Cooking 40 + quest chain.
		put(Quest.RECIPE_FOR_DISASTER__LUMBRIDGE_GUIDE,
			List.of(new SkillReq(Skill.COOKING, 40)),
			List.of(
				Quest.BIG_CHOMPY_BIRD_HUNTING,
				Quest.PRIEST_IN_PERIL,
				Quest.BIOHAZARD,
				Quest.DEMON_SLAYER,
				Quest.NATURE_SPIRIT,
				Quest.THE_RESTLESS_GHOST,
				Quest.WITCHS_HOUSE,
				Quest.MURDER_MYSTERY),
			0, 0);
		// RFD: Evil Dave: Shadow of the Storm.
		put(Quest.RECIPE_FOR_DISASTER__EVIL_DAVE,
			List.of(),
			List.of(Quest.SHADOW_OF_THE_STORM),
			0, 0);
		// Shadow of the Storm: Crafting 30 + quest chain.
		put(Quest.SHADOW_OF_THE_STORM,
			List.of(new SkillReq(Skill.CRAFTING, 30)),
			List.of(Quest.THE_GOLEM, Quest.DEMON_SLAYER),
			0, 0);
		// RFD: Skrach Uglogwee: Firemaking 20, Cooking 41 + Big Chompy.
		put(Quest.RECIPE_FOR_DISASTER__SKRACH_UGLOGWEE,
			List.of(
				new SkillReq(Skill.FIREMAKING, 20),
				new SkillReq(Skill.COOKING, 41)),
			List.of(Quest.BIG_CHOMPY_BIRD_HUNTING),
			0, 0);
		// RFD: Sir Amik Varze: Lost City + started Legends' Quest.
		put(Quest.RECIPE_FOR_DISASTER__SIR_AMIK_VARZE,
			List.of(),
			List.of(Quest.LOST_CITY, Quest.LEGENDS_QUEST),
			0, 0);
		// RFD: King Awowogei: Agility 48, Cooking 70 + MM1.
		put(Quest.RECIPE_FOR_DISASTER__KING_AWOWOGEI,
			List.of(
				new SkillReq(Skill.AGILITY, 48),
				new SkillReq(Skill.COOKING, 70)),
			List.of(Quest.MONKEY_MADNESS_I),
			0, 0);
		// RFD: Culinaromancer (final): 175 QP + all subquests + DT1 + HFTD.
		put(Quest.RECIPE_FOR_DISASTER__CULINAROMANCER,
			List.of(),
			List.of(
				Quest.DESERT_TREASURE_I,
				Quest.HORROR_FROM_THE_DEEP),
			175, 0);

		// Rag and Bone Man I: no requirements.
		put(Quest.RAG_AND_BONE_MAN_I,
			List.of(), List.of(), 0, 0);
		// Rag and Bone Man II: Defence 20 + Rag and Bone Man I.
		put(Quest.RAG_AND_BONE_MAN_II,
			List.of(new SkillReq(Skill.DEFENCE, 20)),
			List.of(Quest.RAG_AND_BONE_MAN_I),
			0, 0);

		// Ratcatchers: Icthlarin's Little Helper.
		put(Quest.RATCATCHERS,
			List.of(),
			List.of(Quest.ICTHLARINS_LITTLE_HELPER),
			0, 0);

		// Royal Trouble: Agility 40, Slayer 40 + Throne of Miscellania.
		put(Quest.ROYAL_TROUBLE,
			List.of(
				new SkillReq(Skill.AGILITY, 40),
				new SkillReq(Skill.SLAYER, 40)),
			List.of(Quest.THRONE_OF_MISCELLANIA),
			0, 0);
		put(Quest.THRONE_OF_MISCELLANIA,
			List.of(),
			List.of(Quest.HEROES_QUEST, Quest.THE_FREMENNIK_TRIALS),
			0, 0);

		// Scorpion Catcher: Prayer 31 + Barcrawl.
		put(Quest.SCORPION_CATCHER,
			List.of(new SkillReq(Skill.PRAYER, 31)),
			List.of(Quest.ALFRED_GRIMHANDS_BARCRAWL),
			0, 0);

		// Sea Slug: Firemaking 30.
		put(Quest.SEA_SLUG,
			List.of(new SkillReq(Skill.FIREMAKING, 30)),
			List.of(),
			0, 0);

		// The Slug Menace: Crafting 30, Runecraft 30, Slayer 30, Thieving 30.
		put(Quest.THE_SLUG_MENACE,
			List.of(
				new SkillReq(Skill.CRAFTING, 30),
				new SkillReq(Skill.RUNECRAFT, 30),
				new SkillReq(Skill.SLAYER, 30),
				new SkillReq(Skill.THIEVING, 30)),
			List.of(Quest.WANTED, Quest.SEA_SLUG),
			0, 0);

		// Shades of Mort'ton: Herblore 15, Crafting 20, Firemaking 5.
		put(Quest.SHADES_OF_MORTTON,
			List.of(
				new SkillReq(Skill.HERBLORE, 15),
				new SkillReq(Skill.CRAFTING, 20),
				new SkillReq(Skill.FIREMAKING, 5)),
			List.of(Quest.PRIEST_IN_PERIL),
			0, 0);

		// Sins of the Father: Woodcutting 62, Fletching 60, Crafting 56,
		// Agility 52, Attack 50, Slayer 50, Magic 49.
		put(Quest.SINS_OF_THE_FATHER,
			List.of(
				new SkillReq(Skill.WOODCUTTING, 62),
				new SkillReq(Skill.FLETCHING, 60),
				new SkillReq(Skill.CRAFTING, 56),
				new SkillReq(Skill.AGILITY, 52),
				new SkillReq(Skill.ATTACK, 50),
				new SkillReq(Skill.SLAYER, 50),
				new SkillReq(Skill.MAGIC, 49)),
			List.of(Quest.VAMPYRE_SLAYER, Quest.A_TASTE_OF_HOPE),
			0, 0);

		// Skippy and the Mogres (miniquest): Cooking 20.
		put(Quest.SKIPPY_AND_THE_MOGRES,
			List.of(new SkillReq(Skill.COOKING, 20)),
			List.of(),
			0, 0);

		// Sleeping Giants: Smithing 15.
		put(Quest.SLEEPING_GIANTS,
			List.of(new SkillReq(Skill.SMITHING, 15)),
			List.of(),
			0, 0);

		// Spirits of the Elid: Magic 33, Ranged 37, Mining 37, Thieving 37.
		put(Quest.SPIRITS_OF_THE_ELID,
			List.of(
				new SkillReq(Skill.MAGIC, 33),
				new SkillReq(Skill.RANGED, 37),
				new SkillReq(Skill.MINING, 37),
				new SkillReq(Skill.THIEVING, 37)),
			List.of(),
			0, 0);

		// Tears of Guthix: Firemaking 49, Crafting 20, Mining 20, 43 QP.
		put(Quest.TEARS_OF_GUTHIX,
			List.of(
				new SkillReq(Skill.FIREMAKING, 49),
				new SkillReq(Skill.CRAFTING, 20),
				new SkillReq(Skill.MINING, 20)),
			List.of(),
			43, 0);

		// The Tourist Trap: Fletching 10, Smithing 20.
		put(Quest.THE_TOURIST_TRAP,
			List.of(
				new SkillReq(Skill.FLETCHING, 10),
				new SkillReq(Skill.SMITHING, 20)),
			List.of(),
			0, 0);

		// Tower of Life: Construction 10.
		put(Quest.TOWER_OF_LIFE,
			List.of(new SkillReq(Skill.CONSTRUCTION, 10)),
			List.of(),
			0, 0);

		// Tribal Totem: Thieving 21.
		put(Quest.TRIBAL_TOTEM,
			List.of(new SkillReq(Skill.THIEVING, 21)),
			List.of(),
			0, 0);

		// Troll Romance: Agility 28 + Troll Stronghold.
		put(Quest.TROLL_ROMANCE,
			List.of(new SkillReq(Skill.AGILITY, 28)),
			List.of(Quest.TROLL_STRONGHOLD),
			0, 0);

		// Into the Tombs (miniquest): Beneath Cursed Sands.
		put(Quest.INTO_THE_TOMBS,
			List.of(),
			List.of(Quest.BENEATH_CURSED_SANDS),
			0, 0);

		// The Frozen Door (miniquest): Agility 70, Ranged 70, Strength 70 + DT1.
		put(Quest.THE_FROZEN_DOOR,
			List.of(
				new SkillReq(Skill.AGILITY, 70),
				new SkillReq(Skill.RANGED, 70),
				new SkillReq(Skill.STRENGTH, 70)),
			List.of(Quest.DESERT_TREASURE_I),
			0, 0);

		// ============================================================
		// U-Z + newer quests (wiki-sourced 2026-04-09)
		// ============================================================

		// The Path of Glouphrie: Slayer 56, Agility 45, Strength 60,
		// Thieving 56, Ranged 47.
		put(Quest.THE_PATH_OF_GLOUPHRIE,
			List.of(
				new SkillReq(Skill.SLAYER, 56),
				new SkillReq(Skill.AGILITY, 45),
				new SkillReq(Skill.STRENGTH, 60),
				new SkillReq(Skill.THIEVING, 56),
				new SkillReq(Skill.RANGED, 47)),
			List.of(
				Quest.THE_EYES_OF_GLOUPHRIE,
				Quest.WATERFALL_QUEST,
				Quest.TREE_GNOME_VILLAGE),
			0, 0);

		// While Guthix Sleeps: Thieving 72, Agility 66, Farming 65,
		// Herblore 65, Hunter 62, Magic 67 + 180 QP + quest chain.
		put(Quest.WHILE_GUTHIX_SLEEPS,
			List.of(
				new SkillReq(Skill.THIEVING, 72),
				new SkillReq(Skill.AGILITY, 66),
				new SkillReq(Skill.FARMING, 65),
				new SkillReq(Skill.HERBLORE, 65),
				new SkillReq(Skill.HUNTER, 62),
				new SkillReq(Skill.MAGIC, 67)),
			List.of(
				Quest.DEFENDER_OF_VARROCK,
				Quest.THE_PATH_OF_GLOUPHRIE,
				Quest.FIGHT_ARENA,
				Quest.DREAM_MENTOR,
				Quest.THE_HAND_IN_THE_SAND,
				Quest.WANTED,
				Quest.TEMPLE_OF_THE_EYE,
				Quest.TEARS_OF_GUTHIX,
				Quest.NATURE_SPIRIT,
				Quest.A_TAIL_OF_TWO_CATS),
			180, 0);

		// The Curse of Arrav: Mining 64, Ranged 62, Thieving 62,
		// Agility 61, Strength 58, Slayer 37.
		put(Quest.THE_CURSE_OF_ARRAV,
			List.of(
				new SkillReq(Skill.MINING, 64),
				new SkillReq(Skill.RANGED, 62),
				new SkillReq(Skill.THIEVING, 62),
				new SkillReq(Skill.AGILITY, 61),
				new SkillReq(Skill.STRENGTH, 58),
				new SkillReq(Skill.SLAYER, 37)),
			List.of(Quest.DEFENDER_OF_VARROCK, Quest.TROLL_ROMANCE),
			0, 0);

		// --- Varlamore quests ---

		// Twilight's Promise: Children of the Sun prereq.
		put(Quest.TWILIGHTS_PROMISE,
			List.of(),
			List.of(Quest.CHILDREN_OF_THE_SUN),
			0, 0);
		// Perilous Moons: Slayer 48, Hunter 20, Fishing 20,
		// Runecraft 20, Construction 10.
		put(Quest.PERILOUS_MOONS,
			List.of(
				new SkillReq(Skill.SLAYER, 48),
				new SkillReq(Skill.HUNTER, 20),
				new SkillReq(Skill.FISHING, 20),
				new SkillReq(Skill.RUNECRAFT, 20),
				new SkillReq(Skill.CONSTRUCTION, 10)),
			List.of(Quest.TWILIGHTS_PROMISE),
			0, 0);
		// The Heart of Darkness: Mining 55, Thieving 48, Slayer 48, Agility 46.
		put(Quest.THE_HEART_OF_DARKNESS,
			List.of(
				new SkillReq(Skill.MINING, 55),
				new SkillReq(Skill.THIEVING, 48),
				new SkillReq(Skill.SLAYER, 48),
				new SkillReq(Skill.AGILITY, 46)),
			List.of(Quest.TWILIGHTS_PROMISE),
			0, 0);
		// The Final Dawn: Thieving 66, Runecraft 52, Fletching 52.
		put(Quest.THE_FINAL_DAWN,
			List.of(
				new SkillReq(Skill.THIEVING, 66),
				new SkillReq(Skill.RUNECRAFT, 52),
				new SkillReq(Skill.FLETCHING, 52)),
			List.of(Quest.THE_HEART_OF_DARKNESS, Quest.PERILOUS_MOONS),
			0, 0);
		// Meat and Greet: Children of the Sun prereq.
		put(Quest.MEAT_AND_GREET,
			List.of(),
			List.of(Quest.CHILDREN_OF_THE_SUN),
			0, 0);
		// Ethically Acquired Antiquities: Thieving 25.
		put(Quest.ETHICALLY_ACQUIRED_ANTIQUITIES,
			List.of(new SkillReq(Skill.THIEVING, 25)),
			List.of(Quest.CHILDREN_OF_THE_SUN, Quest.SHIELD_OF_ARRAV),
			0, 0);
		// The Ribbiting Tale: Woodcutting 15.
		put(Quest.THE_RIBBITING_TALE_OF_A_LILY_PAD_LABOUR_DISPUTE,
			List.of(new SkillReq(Skill.WOODCUTTING, 15)),
			List.of(Quest.CHILDREN_OF_THE_SUN),
			0, 0);
		// Shadows of Custodia: Slayer 54, Fishing 45, Hunter 36.
		put(Quest.SHADOWS_OF_CUSTODIA,
			List.of(
				new SkillReq(Skill.SLAYER, 54),
				new SkillReq(Skill.FISHING, 45),
				new SkillReq(Skill.HUNTER, 36)),
			List.of(Quest.CHILDREN_OF_THE_SUN),
			0, 0);
		// Scrambled: Construction 38, Cooking 36, Smithing 35.
		put(Quest.SCRAMBLED,
			List.of(
				new SkillReq(Skill.CONSTRUCTION, 38),
				new SkillReq(Skill.COOKING, 36),
				new SkillReq(Skill.SMITHING, 35)),
			List.of(Quest.CHILDREN_OF_THE_SUN),
			0, 0);

		// --- Sailing quests (Sailing skill not in RuneLite API yet) ---
		// Pandemonium: no representable requirements.
		put(Quest.PANDEMONIUM,
			List.of(), List.of(), 0, 0);
		// Prying Times: Smithing 30 (Sailing 12 omitted).
		put(Quest.PRYING_TIMES,
			List.of(new SkillReq(Skill.SMITHING, 30)),
			List.of(Quest.PANDEMONIUM, Quest.THE_KNIGHTS_SWORD),
			0, 0);
		// Current Affairs: Fishing 10 (Sailing 22 omitted).
		put(Quest.CURRENT_AFFAIRS,
			List.of(new SkillReq(Skill.FISHING, 10)),
			List.of(Quest.PANDEMONIUM),
			0, 0);
		// Troubled Tortugans: Slayer 51, Construction 48, Hunter 45,
		// Woodcutting 40, Crafting 34 (Sailing 45 omitted).
		put(Quest.TROUBLED_TORTUGANS,
			List.of(
				new SkillReq(Skill.SLAYER, 51),
				new SkillReq(Skill.CONSTRUCTION, 48),
				new SkillReq(Skill.HUNTER, 45),
				new SkillReq(Skill.WOODCUTTING, 40),
				new SkillReq(Skill.CRAFTING, 34)),
			List.of(Quest.PANDEMONIUM),
			0, 0);
		// The Red Reef: Smithing 48 (Sailing 52 omitted).
		put(Quest.THE_RED_REEF,
			List.of(new SkillReq(Skill.SMITHING, 48)),
			List.of(Quest.TROUBLED_TORTUGANS),
			0, 0);
		// Vale Totems (miniquest): Fletching 20.
		put(Quest.VALE_TOTEMS,
			List.of(new SkillReq(Skill.FLETCHING, 20)),
			List.of(),
			0, 0);

		// Learning the Ropes: no requirements.
		put(Quest.LEARNING_THE_ROPES,
			List.of(), List.of(), 0, 0);
	}

	// ============================================================
	// XP reward tags: skills that a quest rewards XP in (fixed
	// rewards only — choice lamps are excluded).
	// ============================================================
	private static final Map<Quest, List<Skill>> XP_REWARDS = new EnumMap<>(Quest.class);

	static
	{
		// F2P quests with fixed XP rewards
		XP_REWARDS.put(Quest.COOKS_ASSISTANT, List.of(Skill.COOKING));
		XP_REWARDS.put(Quest.DORICS_QUEST, List.of(Skill.MINING));
		XP_REWARDS.put(Quest.DRAGON_SLAYER_I, List.of(Skill.DEFENCE));
		XP_REWARDS.put(Quest.GOBLIN_DIPLOMACY, List.of(Skill.CRAFTING));
		XP_REWARDS.put(Quest.IMP_CATCHER, List.of(Skill.MAGIC));
		XP_REWARDS.put(Quest.THE_KNIGHTS_SWORD, List.of(Skill.SMITHING));
		XP_REWARDS.put(Quest.MISTHALIN_MYSTERY, List.of(Skill.CRAFTING));
		XP_REWARDS.put(Quest.THE_RESTLESS_GHOST, List.of(Skill.PRAYER));
		XP_REWARDS.put(Quest.SHEEP_SHEARER, List.of(Skill.CRAFTING));
		XP_REWARDS.put(Quest.VAMPYRE_SLAYER, List.of(Skill.ATTACK));
		XP_REWARDS.put(Quest.WITCHS_POTION, List.of(Skill.MAGIC));

		// A-D members quests with fixed XP rewards
		XP_REWARDS.put(Quest.A_PORCINE_OF_INTEREST, List.of(Skill.SLAYER));
		XP_REWARDS.put(Quest.A_SOULS_BANE, List.of(Skill.DEFENCE, Skill.HITPOINTS));
		XP_REWARDS.put(Quest.ANOTHER_SLICE_OF_HAM, List.of(Skill.MINING, Skill.PRAYER));
		XP_REWARDS.put(Quest.AT_FIRST_LIGHT, List.of(Skill.HUNTER, Skill.CONSTRUCTION, Skill.HERBLORE));
		XP_REWARDS.put(Quest.BENEATH_CURSED_SANDS, List.of(Skill.AGILITY));
		XP_REWARDS.put(Quest.BETWEEN_A_ROCK, List.of(Skill.DEFENCE, Skill.MINING, Skill.SMITHING));
		XP_REWARDS.put(Quest.CABIN_FEVER, List.of(Skill.CRAFTING, Skill.SMITHING, Skill.AGILITY));
		XP_REWARDS.put(Quest.COLD_WAR, List.of(Skill.CRAFTING, Skill.AGILITY, Skill.CONSTRUCTION));
		XP_REWARDS.put(Quest.CREATURE_OF_FENKENSTRAIN, List.of(Skill.THIEVING));
		XP_REWARDS.put(Quest.DADDYS_HOME, List.of(Skill.CONSTRUCTION));
		XP_REWARDS.put(Quest.DEATH_ON_THE_ISLE, List.of(Skill.THIEVING, Skill.AGILITY, Skill.CRAFTING));
		XP_REWARDS.put(Quest.DEATH_TO_THE_DORGESHUUN, List.of(Skill.THIEVING, Skill.RANGED));
		XP_REWARDS.put(Quest.DEVIOUS_MINDS, List.of(Skill.FLETCHING, Skill.RUNECRAFT, Skill.SMITHING));
		XP_REWARDS.put(Quest.DWARF_CANNON, List.of(Skill.CRAFTING));
		XP_REWARDS.put(Quest.HORROR_FROM_THE_DEEP, List.of(Skill.MAGIC, Skill.STRENGTH, Skill.RANGED));
		XP_REWARDS.put(Quest.DESERT_TREASURE_I, List.of(Skill.MAGIC));

		// Existing members quests backfill
		XP_REWARDS.put(Quest.TREE_GNOME_VILLAGE, List.of(Skill.ATTACK));
		XP_REWARDS.put(Quest.THE_GRAND_TREE, List.of(Skill.ATTACK, Skill.AGILITY, Skill.MAGIC));
		XP_REWARDS.put(Quest.DREAM_MENTOR, List.of(Skill.HITPOINTS, Skill.MAGIC));
		XP_REWARDS.put(Quest.LUNAR_DIPLOMACY, List.of(Skill.MAGIC, Skill.RUNECRAFT));
		XP_REWARDS.put(Quest.THE_FREMENNIK_TRIALS, List.of(
			Skill.AGILITY, Skill.ATTACK, Skill.CRAFTING, Skill.DEFENCE,
			Skill.FISHING, Skill.FLETCHING, Skill.HITPOINTS, Skill.STRENGTH,
			Skill.THIEVING, Skill.WOODCUTTING));
		XP_REWARDS.put(Quest.SHILO_VILLAGE, List.of(Skill.CRAFTING));
		XP_REWARDS.put(Quest.JUNGLE_POTION, List.of(Skill.HERBLORE));
		XP_REWARDS.put(Quest.DRUIDIC_RITUAL, List.of(Skill.HERBLORE));
		XP_REWARDS.put(Quest.EADGARS_RUSE, List.of(Skill.HERBLORE));
		XP_REWARDS.put(Quest.DEATH_PLATEAU, List.of(Skill.ATTACK));
		XP_REWARDS.put(Quest.GHOSTS_AHOY, List.of(Skill.PRAYER));
		XP_REWARDS.put(Quest.PRIEST_IN_PERIL, List.of(Skill.PRAYER));
		XP_REWARDS.put(Quest.ICTHLARINS_LITTLE_HELPER, List.of(Skill.THIEVING, Skill.AGILITY, Skill.WOODCUTTING));
		XP_REWARDS.put(Quest.GERTRUDES_CAT, List.of(Skill.COOKING));
		XP_REWARDS.put(Quest.ANIMAL_MAGNETISM, List.of(Skill.CRAFTING, Skill.FLETCHING, Skill.SLAYER, Skill.WOODCUTTING));
		XP_REWARDS.put(Quest.HEROES_QUEST, List.of(
			Skill.ATTACK, Skill.DEFENCE, Skill.STRENGTH, Skill.HITPOINTS,
			Skill.RANGED, Skill.FISHING, Skill.COOKING, Skill.WOODCUTTING,
			Skill.FIREMAKING, Skill.SMITHING, Skill.MINING, Skill.HERBLORE));
		XP_REWARDS.put(Quest.UNDERGROUND_PASS, List.of(Skill.AGILITY, Skill.ATTACK));
		XP_REWARDS.put(Quest.BIOHAZARD, List.of(Skill.THIEVING));
		XP_REWARDS.put(Quest.PLAGUE_CITY, List.of(Skill.MINING));
		XP_REWARDS.put(Quest.WATERFALL_QUEST, List.of(Skill.ATTACK, Skill.STRENGTH));
		XP_REWARDS.put(Quest.DRAGON_SLAYER_II, List.of(Skill.SMITHING, Skill.MINING, Skill.AGILITY, Skill.THIEVING));
		XP_REWARDS.put(Quest.SONG_OF_THE_ELVES, List.of(
			Skill.AGILITY, Skill.CONSTRUCTION, Skill.FARMING, Skill.HERBLORE,
			Skill.HUNTER, Skill.MINING, Skill.SMITHING, Skill.WOODCUTTING));
		XP_REWARDS.put(Quest.MOURNINGS_END_PART_II, List.of(Skill.AGILITY));
		XP_REWARDS.put(Quest.MOURNINGS_END_PART_I, List.of(Skill.THIEVING, Skill.HITPOINTS));
		XP_REWARDS.put(Quest.ROVING_ELVES, List.of(Skill.STRENGTH));
		XP_REWARDS.put(Quest.REGICIDE, List.of(Skill.AGILITY));
		XP_REWARDS.put(Quest.BIG_CHOMPY_BIRD_HUNTING, List.of(Skill.FLETCHING, Skill.COOKING, Skill.RANGED));

		// SotN chain + new dependencies
		XP_REWARDS.put(Quest.SECRETS_OF_THE_NORTH, List.of(Skill.AGILITY, Skill.THIEVING, Skill.HUNTER));
		XP_REWARDS.put(Quest.THE_GARDEN_OF_DEATH, List.of(Skill.FARMING));
		XP_REWARDS.put(Quest.MAKING_FRIENDS_WITH_MY_ARM, List.of(Skill.CONSTRUCTION, Skill.FIREMAKING, Skill.MINING, Skill.AGILITY));
		XP_REWARDS.put(Quest.MY_ARMS_BIG_ADVENTURE, List.of(Skill.HERBLORE, Skill.FARMING));
		XP_REWARDS.put(Quest.SWAN_SONG, List.of(Skill.MAGIC, Skill.PRAYER, Skill.FISHING));
		XP_REWARDS.put(Quest.THE_FEUD, List.of(Skill.THIEVING));
		XP_REWARDS.put(Quest.THE_GENERALS_SHADOW, List.of(Skill.SLAYER));
		XP_REWARDS.put(Quest.HAZEEL_CULT, List.of(Skill.THIEVING));
		XP_REWARDS.put(Quest.FIGHT_ARENA, List.of(Skill.ATTACK, Skill.THIEVING));

		// E-H quests
		XP_REWARDS.put(Quest.ELEMENTAL_WORKSHOP_I, List.of(Skill.CRAFTING, Skill.SMITHING));
		XP_REWARDS.put(Quest.ELEMENTAL_WORKSHOP_II, List.of(Skill.SMITHING, Skill.CRAFTING));
		XP_REWARDS.put(Quest.ENLIGHTENED_JOURNEY, List.of(Skill.CRAFTING, Skill.FARMING, Skill.WOODCUTTING, Skill.FIREMAKING));
		XP_REWARDS.put(Quest.THE_EYES_OF_GLOUPHRIE, List.of(Skill.MAGIC, Skill.WOODCUTTING, Skill.RUNECRAFT, Skill.CONSTRUCTION));
		XP_REWARDS.put(Quest.FAIRYTALE_I__GROWING_PAINS, List.of(Skill.FARMING, Skill.ATTACK, Skill.MAGIC));
		XP_REWARDS.put(Quest.FAIRYTALE_II__CURE_A_QUEEN, List.of(Skill.HERBLORE, Skill.THIEVING));
		XP_REWARDS.put(Quest.FORGETTABLE_TALE, List.of(Skill.COOKING, Skill.FARMING));
		XP_REWARDS.put(Quest.THE_FREMENNIK_ISLES, List.of(Skill.CONSTRUCTION, Skill.CRAFTING, Skill.WOODCUTTING));
		XP_REWARDS.put(Quest.THE_FREMENNIK_EXILES, List.of(Skill.SLAYER, Skill.CRAFTING, Skill.RUNECRAFT));
		XP_REWARDS.put(Quest.GETTING_AHEAD, List.of(Skill.CRAFTING, Skill.CONSTRUCTION));
		XP_REWARDS.put(Quest.THE_GOLEM, List.of(Skill.THIEVING, Skill.CRAFTING));
		XP_REWARDS.put(Quest.GRIM_TALES, List.of(Skill.WOODCUTTING, Skill.AGILITY, Skill.THIEVING, Skill.HERBLORE, Skill.FARMING, Skill.HITPOINTS));
		XP_REWARDS.put(Quest.THE_GREAT_BRAIN_ROBBERY, List.of(Skill.PRAYER, Skill.CRAFTING, Skill.CONSTRUCTION));
		XP_REWARDS.put(Quest.THE_HAND_IN_THE_SAND, List.of(Skill.THIEVING, Skill.CRAFTING));
		XP_REWARDS.put(Quest.HAUNTED_MINE, List.of(Skill.STRENGTH));
		XP_REWARDS.put(Quest.HOLY_GRAIL, List.of(Skill.PRAYER, Skill.DEFENCE));
		XP_REWARDS.put(Quest.HOPESPEARS_WILL, List.of(Skill.PRAYER));
		// I-O quests
		XP_REWARDS.put(Quest.KINGS_RANSOM, List.of(Skill.DEFENCE, Skill.MAGIC));
		XP_REWARDS.put(Quest.LAIR_OF_TARN_RAZORLOR, List.of(Skill.SLAYER));
		XP_REWARDS.put(Quest.LAND_OF_THE_GOBLINS, List.of(Skill.AGILITY, Skill.FISHING, Skill.THIEVING, Skill.HERBLORE));
		XP_REWARDS.put(Quest.MAKING_HISTORY, List.of(Skill.CRAFTING, Skill.PRAYER));
		XP_REWARDS.put(Quest.MONKEY_MADNESS_II, List.of(Skill.SLAYER, Skill.AGILITY, Skill.THIEVING, Skill.HUNTER));
		XP_REWARDS.put(Quest.MONKS_FRIEND, List.of(Skill.WOODCUTTING));
		XP_REWARDS.put(Quest.MOUNTAIN_DAUGHTER, List.of(Skill.PRAYER, Skill.ATTACK));
		XP_REWARDS.put(Quest.MURDER_MYSTERY, List.of(Skill.CRAFTING));
		XP_REWARDS.put(Quest.OBSERVATORY_QUEST, List.of(Skill.CRAFTING));
		XP_REWARDS.put(Quest.OLAFS_QUEST, List.of(Skill.DEFENCE));
		// P-T quests
		XP_REWARDS.put(Quest.RAG_AND_BONE_MAN_I, List.of(Skill.COOKING, Skill.PRAYER));
		XP_REWARDS.put(Quest.RAG_AND_BONE_MAN_II, List.of(Skill.PRAYER));
		XP_REWARDS.put(Quest.RATCATCHERS, List.of(Skill.THIEVING));
		XP_REWARDS.put(Quest.ROYAL_TROUBLE, List.of(Skill.AGILITY, Skill.SLAYER, Skill.HITPOINTS));
		XP_REWARDS.put(Quest.SCORPION_CATCHER, List.of(Skill.STRENGTH));
		XP_REWARDS.put(Quest.SEA_SLUG, List.of(Skill.FISHING));
		XP_REWARDS.put(Quest.THE_SLUG_MENACE, List.of(Skill.CRAFTING, Skill.RUNECRAFT, Skill.THIEVING));
		XP_REWARDS.put(Quest.SHADES_OF_MORTTON, List.of(Skill.HERBLORE, Skill.CRAFTING));
		XP_REWARDS.put(Quest.SPIRITS_OF_THE_ELID, List.of(Skill.PRAYER, Skill.THIEVING, Skill.MAGIC));
		XP_REWARDS.put(Quest.TEARS_OF_GUTHIX, List.of(Skill.CRAFTING));
		XP_REWARDS.put(Quest.TOWER_OF_LIFE, List.of(Skill.CONSTRUCTION, Skill.CRAFTING, Skill.THIEVING));
		XP_REWARDS.put(Quest.TROLL_ROMANCE, List.of(Skill.AGILITY, Skill.STRENGTH));
		XP_REWARDS.put(Quest.WITCHS_HOUSE, List.of(Skill.HITPOINTS));
		XP_REWARDS.put(Quest.WATCHTOWER, List.of(Skill.MAGIC));
		XP_REWARDS.put(Quest.TRIBAL_TOTEM, List.of(Skill.THIEVING));
		XP_REWARDS.put(Quest.SLEEPING_GIANTS, List.of(Skill.SMITHING));
		// U-Z + newer quests
		XP_REWARDS.put(Quest.THE_PATH_OF_GLOUPHRIE, List.of(Skill.SLAYER, Skill.THIEVING, Skill.STRENGTH, Skill.MAGIC));
		XP_REWARDS.put(Quest.WHILE_GUTHIX_SLEEPS, List.of(Skill.THIEVING, Skill.FARMING, Skill.HERBLORE, Skill.HUNTER));
		XP_REWARDS.put(Quest.THE_CURSE_OF_ARRAV, List.of(Skill.MINING, Skill.THIEVING, Skill.AGILITY));
		XP_REWARDS.put(Quest.TWILIGHTS_PROMISE, List.of(Skill.THIEVING));
		XP_REWARDS.put(Quest.PERILOUS_MOONS, List.of(Skill.SLAYER, Skill.RUNECRAFT, Skill.HUNTER, Skill.FISHING));
		XP_REWARDS.put(Quest.THE_HEART_OF_DARKNESS, List.of(Skill.MINING, Skill.THIEVING, Skill.SLAYER, Skill.AGILITY));
		XP_REWARDS.put(Quest.THE_FINAL_DAWN, List.of(Skill.THIEVING, Skill.RUNECRAFT, Skill.FLETCHING));
		XP_REWARDS.put(Quest.MEAT_AND_GREET, List.of(Skill.COOKING));
		XP_REWARDS.put(Quest.ETHICALLY_ACQUIRED_ANTIQUITIES, List.of(Skill.THIEVING));
		XP_REWARDS.put(Quest.THE_RIBBITING_TALE_OF_A_LILY_PAD_LABOUR_DISPUTE, List.of(Skill.WOODCUTTING));
		XP_REWARDS.put(Quest.SHADOWS_OF_CUSTODIA, List.of(Skill.SLAYER, Skill.HUNTER, Skill.FISHING, Skill.CONSTRUCTION));
		XP_REWARDS.put(Quest.SCRAMBLED, List.of(Skill.CONSTRUCTION, Skill.COOKING, Skill.SMITHING));
		XP_REWARDS.put(Quest.SHADOW_OF_THE_STORM, List.of()); // choice-based combat XP
	}

	// ============================================================
	// Quest point rewards
	// ============================================================
	private static final Map<Quest, Integer> QP_REWARDS = new EnumMap<>(Quest.class);

	static
	{
		// F2P quests
		QP_REWARDS.put(Quest.BELOW_ICE_MOUNTAIN, 1);
		QP_REWARDS.put(Quest.BLACK_KNIGHTS_FORTRESS, 3);
		QP_REWARDS.put(Quest.COOKS_ASSISTANT, 1);
		QP_REWARDS.put(Quest.THE_CORSAIR_CURSE, 2);
		QP_REWARDS.put(Quest.DEMON_SLAYER, 3);
		QP_REWARDS.put(Quest.DORICS_QUEST, 1);
		QP_REWARDS.put(Quest.DRAGON_SLAYER_I, 2);
		QP_REWARDS.put(Quest.ERNEST_THE_CHICKEN, 4);
		QP_REWARDS.put(Quest.GOBLIN_DIPLOMACY, 5);
		QP_REWARDS.put(Quest.IMP_CATCHER, 1);
		QP_REWARDS.put(Quest.THE_IDES_OF_MILK, 1);
		QP_REWARDS.put(Quest.THE_KNIGHTS_SWORD, 1);
		QP_REWARDS.put(Quest.MISTHALIN_MYSTERY, 1);
		QP_REWARDS.put(Quest.PIRATES_TREASURE, 2);
		QP_REWARDS.put(Quest.PRINCE_ALI_RESCUE, 3);
		QP_REWARDS.put(Quest.THE_RESTLESS_GHOST, 1);
		QP_REWARDS.put(Quest.ROMEO__JULIET, 5);
		QP_REWARDS.put(Quest.RUNE_MYSTERIES, 1);
		QP_REWARDS.put(Quest.SHEEP_SHEARER, 1);
		QP_REWARDS.put(Quest.SHIELD_OF_ARRAV, 1);
		QP_REWARDS.put(Quest.VAMPYRE_SLAYER, 3);
		QP_REWARDS.put(Quest.WITCHS_POTION, 1);
		QP_REWARDS.put(Quest.X_MARKS_THE_SPOT, 1);

		// A-D members quests
		QP_REWARDS.put(Quest.A_PORCINE_OF_INTEREST, 1);
		QP_REWARDS.put(Quest.A_SOULS_BANE, 1);
		QP_REWARDS.put(Quest.A_KINGDOM_DIVIDED, 2);
		QP_REWARDS.put(Quest.A_NIGHT_AT_THE_THEATRE, 2);
		QP_REWARDS.put(Quest.A_TASTE_OF_HOPE, 1);
		QP_REWARDS.put(Quest.ANOTHER_SLICE_OF_HAM, 1);
		QP_REWARDS.put(Quest.AT_FIRST_LIGHT, 1);
		QP_REWARDS.put(Quest.BENEATH_CURSED_SANDS, 2);
		QP_REWARDS.put(Quest.BETWEEN_A_ROCK, 2);
		QP_REWARDS.put(Quest.CABIN_FEVER, 2);
		QP_REWARDS.put(Quest.CHILDREN_OF_THE_SUN, 1);
		QP_REWARDS.put(Quest.CLOCK_TOWER, 1);
		QP_REWARDS.put(Quest.COLD_WAR, 1);
		QP_REWARDS.put(Quest.CONTACT, 1);
		QP_REWARDS.put(Quest.CREATURE_OF_FENKENSTRAIN, 2);
		QP_REWARDS.put(Quest.DARKNESS_OF_HALLOWVALE, 2);
		QP_REWARDS.put(Quest.DEATH_ON_THE_ISLE, 2);
		QP_REWARDS.put(Quest.DEATH_TO_THE_DORGESHUUN, 1);
		QP_REWARDS.put(Quest.DEFENDER_OF_VARROCK, 2);
		QP_REWARDS.put(Quest.DESERT_TREASURE_II__THE_FALLEN_EMPIRE, 5);
		QP_REWARDS.put(Quest.DEVIOUS_MINDS, 1);
		QP_REWARDS.put(Quest.DWARF_CANNON, 1);
		// Miniquests: 0 QP (not added — questPointReward returns 0 by default)
		// Dependencies
		// Existing members quests backfill
		QP_REWARDS.put(Quest.HORROR_FROM_THE_DEEP, 2);
		QP_REWARDS.put(Quest.MONKEY_MADNESS_I, 3);
		QP_REWARDS.put(Quest.TREE_GNOME_VILLAGE, 2);
		QP_REWARDS.put(Quest.THE_GRAND_TREE, 5);
		QP_REWARDS.put(Quest.DESERT_TREASURE_I, 3);
		QP_REWARDS.put(Quest.DREAM_MENTOR, 2);
		QP_REWARDS.put(Quest.LUNAR_DIPLOMACY, 2);
		QP_REWARDS.put(Quest.THE_FREMENNIK_TRIALS, 3);
		QP_REWARDS.put(Quest.LOST_CITY, 3);
		QP_REWARDS.put(Quest.SHILO_VILLAGE, 2);
		QP_REWARDS.put(Quest.JUNGLE_POTION, 1);
		QP_REWARDS.put(Quest.DRUIDIC_RITUAL, 4);
		QP_REWARDS.put(Quest.EADGARS_RUSE, 1);
		QP_REWARDS.put(Quest.TROLL_STRONGHOLD, 1);
		QP_REWARDS.put(Quest.DEATH_PLATEAU, 1);
		QP_REWARDS.put(Quest.LEGENDS_QUEST, 4);
		QP_REWARDS.put(Quest.BONE_VOYAGE, 1);
		QP_REWARDS.put(Quest.CLIENT_OF_KOUREND, 1);
		QP_REWARDS.put(Quest.GHOSTS_AHOY, 2);
		QP_REWARDS.put(Quest.PRIEST_IN_PERIL, 1);
		QP_REWARDS.put(Quest.A_TAIL_OF_TWO_CATS, 2);
		QP_REWARDS.put(Quest.ICTHLARINS_LITTLE_HELPER, 2);
		QP_REWARDS.put(Quest.GERTRUDES_CAT, 1);
		QP_REWARDS.put(Quest.ANIMAL_MAGNETISM, 1);
		QP_REWARDS.put(Quest.FAMILY_CREST, 1);
		QP_REWARDS.put(Quest.HEROES_QUEST, 1);
		QP_REWARDS.put(Quest.MERLINS_CRYSTAL, 6);
		QP_REWARDS.put(Quest.UNDERGROUND_PASS, 5);
		QP_REWARDS.put(Quest.BIOHAZARD, 3);
		QP_REWARDS.put(Quest.PLAGUE_CITY, 1);
		QP_REWARDS.put(Quest.WATERFALL_QUEST, 1);
		QP_REWARDS.put(Quest.DRAGON_SLAYER_II, 5);
		QP_REWARDS.put(Quest.SONG_OF_THE_ELVES, 4);
		QP_REWARDS.put(Quest.MOURNINGS_END_PART_II, 2);
		QP_REWARDS.put(Quest.MOURNINGS_END_PART_I, 2);
		QP_REWARDS.put(Quest.ROVING_ELVES, 1);
		QP_REWARDS.put(Quest.REGICIDE, 3);
		QP_REWARDS.put(Quest.BIG_CHOMPY_BIRD_HUNTING, 2);
		QP_REWARDS.put(Quest.SHEEP_HERDER, 4);
		// A-D dependencies
		QP_REWARDS.put(Quest.THE_LOST_TRIBE, 1);
		QP_REWARDS.put(Quest.THE_GIANT_DWARF, 2);
		QP_REWARDS.put(Quest.THE_DIG_SITE, 2);
		QP_REWARDS.put(Quest.EAGLES_PEAK, 2);
		QP_REWARDS.put(Quest.THE_DEPTHS_OF_DESPAIR, 1);
		QP_REWARDS.put(Quest.THE_QUEEN_OF_THIEVES, 1);
		QP_REWARDS.put(Quest.THE_ASCENT_OF_ARCEUUS, 1);
		QP_REWARDS.put(Quest.THE_FORSAKEN_TOWER, 1);
		QP_REWARDS.put(Quest.TALE_OF_THE_RIGHTEOUS, 1);
		QP_REWARDS.put(Quest.IN_AID_OF_THE_MYREQUE, 2);
		QP_REWARDS.put(Quest.IN_SEARCH_OF_THE_MYREQUE, 2);
		QP_REWARDS.put(Quest.NATURE_SPIRIT, 2);
		QP_REWARDS.put(Quest.RUM_DEAL, 2);
		QP_REWARDS.put(Quest.FISHING_CONTEST, 1);
		QP_REWARDS.put(Quest.TEMPLE_OF_IKOV, 1);
		QP_REWARDS.put(Quest.WHAT_LIES_BELOW, 1);
		QP_REWARDS.put(Quest.ENAKHRAS_LAMENT, 2);
		QP_REWARDS.put(Quest.RECRUITMENT_DRIVE, 1);
		QP_REWARDS.put(Quest.WANTED, 1);
		// SotN chain + new dependencies
		QP_REWARDS.put(Quest.SECRETS_OF_THE_NORTH, 2);
		QP_REWARDS.put(Quest.THE_GARDEN_OF_DEATH, 1);
		QP_REWARDS.put(Quest.MAKING_FRIENDS_WITH_MY_ARM, 2);
		QP_REWARDS.put(Quest.MY_ARMS_BIG_ADVENTURE, 1);
		QP_REWARDS.put(Quest.SWAN_SONG, 2);
		QP_REWARDS.put(Quest.ONE_SMALL_FAVOUR, 2);
		QP_REWARDS.put(Quest.THE_FEUD, 1);
		QP_REWARDS.put(Quest.FIGHT_ARENA, 2);
		QP_REWARDS.put(Quest.HAZEEL_CULT, 1);
		// Miniquests: His Faithful Servants, The General's Shadow = 0 QP
		// E-H quests
		QP_REWARDS.put(Quest.ELEMENTAL_WORKSHOP_I, 1);
		QP_REWARDS.put(Quest.ELEMENTAL_WORKSHOP_II, 1);
		QP_REWARDS.put(Quest.ENLIGHTENED_JOURNEY, 1);
		QP_REWARDS.put(Quest.THE_EYES_OF_GLOUPHRIE, 2);
		QP_REWARDS.put(Quest.FAIRYTALE_I__GROWING_PAINS, 2);
		QP_REWARDS.put(Quest.FAIRYTALE_II__CURE_A_QUEEN, 2);
		QP_REWARDS.put(Quest.FORGETTABLE_TALE, 2);
		QP_REWARDS.put(Quest.THE_FREMENNIK_ISLES, 1);
		QP_REWARDS.put(Quest.THE_FREMENNIK_EXILES, 2);
		QP_REWARDS.put(Quest.GETTING_AHEAD, 1);
		QP_REWARDS.put(Quest.THE_GOLEM, 1);
		QP_REWARDS.put(Quest.GRIM_TALES, 1);
		QP_REWARDS.put(Quest.THE_GREAT_BRAIN_ROBBERY, 2);
		QP_REWARDS.put(Quest.THE_HAND_IN_THE_SAND, 1);
		QP_REWARDS.put(Quest.HAUNTED_MINE, 2);
		QP_REWARDS.put(Quest.HOLY_GRAIL, 2);
		// I-O quests
		QP_REWARDS.put(Quest.KINGS_RANSOM, 1);
		QP_REWARDS.put(Quest.LAND_OF_THE_GOBLINS, 2);
		QP_REWARDS.put(Quest.MAKING_HISTORY, 3);
		QP_REWARDS.put(Quest.MONKEY_MADNESS_II, 4);
		QP_REWARDS.put(Quest.MONKS_FRIEND, 1);
		QP_REWARDS.put(Quest.MOUNTAIN_DAUGHTER, 2);
		QP_REWARDS.put(Quest.MURDER_MYSTERY, 3);
		QP_REWARDS.put(Quest.OBSERVATORY_QUEST, 2);
		QP_REWARDS.put(Quest.OLAFS_QUEST, 1);
		// P-T quests
		QP_REWARDS.put(Quest.RAG_AND_BONE_MAN_I, 1);
		QP_REWARDS.put(Quest.RAG_AND_BONE_MAN_II, 1);
		QP_REWARDS.put(Quest.RATCATCHERS, 2);
		QP_REWARDS.put(Quest.ROYAL_TROUBLE, 1);
		QP_REWARDS.put(Quest.SCORPION_CATCHER, 1);
		QP_REWARDS.put(Quest.SEA_SLUG, 1);
		QP_REWARDS.put(Quest.THE_SLUG_MENACE, 1);
		QP_REWARDS.put(Quest.SHADES_OF_MORTTON, 3);
		QP_REWARDS.put(Quest.SHADOW_OF_THE_STORM, 1);
		QP_REWARDS.put(Quest.SINS_OF_THE_FATHER, 2);
		QP_REWARDS.put(Quest.SLEEPING_GIANTS, 1);
		QP_REWARDS.put(Quest.SPIRITS_OF_THE_ELID, 2);
		QP_REWARDS.put(Quest.TEARS_OF_GUTHIX, 1);
		QP_REWARDS.put(Quest.THE_TOURIST_TRAP, 2);
		QP_REWARDS.put(Quest.THRONE_OF_MISCELLANIA, 1);
		QP_REWARDS.put(Quest.TOWER_OF_LIFE, 2);
		QP_REWARDS.put(Quest.TRIBAL_TOTEM, 1);
		QP_REWARDS.put(Quest.TROLL_ROMANCE, 2);
		QP_REWARDS.put(Quest.WATCHTOWER, 4);
		QP_REWARDS.put(Quest.WITCHS_HOUSE, 4);
		// U-Z + newer quests
		QP_REWARDS.put(Quest.THE_PATH_OF_GLOUPHRIE, 2);
		QP_REWARDS.put(Quest.WHILE_GUTHIX_SLEEPS, 5);
		QP_REWARDS.put(Quest.THE_CURSE_OF_ARRAV, 2);
		QP_REWARDS.put(Quest.TWILIGHTS_PROMISE, 1);
		QP_REWARDS.put(Quest.PERILOUS_MOONS, 2);
		QP_REWARDS.put(Quest.THE_HEART_OF_DARKNESS, 2);
		QP_REWARDS.put(Quest.THE_FINAL_DAWN, 3);
		QP_REWARDS.put(Quest.MEAT_AND_GREET, 1);
		QP_REWARDS.put(Quest.ETHICALLY_ACQUIRED_ANTIQUITIES, 1);
		QP_REWARDS.put(Quest.THE_RIBBITING_TALE_OF_A_LILY_PAD_LABOUR_DISPUTE, 1);
		QP_REWARDS.put(Quest.SHADOWS_OF_CUSTODIA, 2);
		QP_REWARDS.put(Quest.SCRAMBLED, 1);
		QP_REWARDS.put(Quest.PANDEMONIUM, 1);
		QP_REWARDS.put(Quest.PRYING_TIMES, 1);
		QP_REWARDS.put(Quest.CURRENT_AFFAIRS, 1);
		QP_REWARDS.put(Quest.TROUBLED_TORTUGANS, 1);
		QP_REWARDS.put(Quest.THE_RED_REEF, 2);
		QP_REWARDS.put(Quest.LEARNING_THE_ROPES, 1);
		QP_REWARDS.put(Quest.THE_GARDEN_OF_DEATH, 1);
	}

	/** Quests that reward an XP lamp (choice-based, not fixed skill). */
	private static final java.util.Set<Quest> LAMP_REWARD_QUESTS = java.util.EnumSet.of(
		Quest.X_MARKS_THE_SPOT,
		Quest.THE_IDES_OF_MILK,
		Quest.SHIELD_OF_ARRAV,
		// A-D members
		Quest.A_KINGDOM_DIVIDED,
		Quest.A_NIGHT_AT_THE_THEATRE,
		Quest.A_TASTE_OF_HOPE,
		Quest.CONTACT,
		Quest.CURSE_OF_THE_EMPTY_LORD,
		Quest.DARKNESS_OF_HALLOWVALE,
		Quest.DEFENDER_OF_VARROCK,
		Quest.DESERT_TREASURE_II__THE_FALLEN_EMPIRE,
		Quest.MONKEY_MADNESS_I,
		// Existing quests backfill
		Quest.DREAM_MENTOR,
		Quest.LEGENDS_QUEST,
		Quest.CLIENT_OF_KOUREND,
		Quest.A_TAIL_OF_TWO_CATS,
		Quest.DRAGON_SLAYER_II,
		// SotN chain
		Quest.ONE_SMALL_FAVOUR,
		Quest.HIS_FAITHFUL_SERVANTS,
		// E-Z quests
		Quest.FAIRYTALE_II__CURE_A_QUEEN,
		Quest.THE_GREAT_BRAIN_ROBBERY,
		Quest.IN_SEARCH_OF_KNOWLEDGE,
		Quest.KINGS_RANSOM,
		Quest.MAKING_HISTORY,
		Quest.INTO_THE_TOMBS,
		Quest.THE_PATH_OF_GLOUPHRIE,
		Quest.SINS_OF_THE_FATHER,
		Quest.THE_FINAL_DAWN,
		Quest.SHADOW_OF_THE_STORM
	);

	// ============================================================
	// Recommended combat levels (wiki-sourced suggestions, not hard
	// requirements). Seeded as optional goals.
	// ============================================================
	private static final Map<Quest, Integer> RECOMMENDED_COMBAT = new EnumMap<>(Quest.class);

	static
	{
		RECOMMENDED_COMBAT.put(Quest.DEMON_SLAYER, 15);
		RECOMMENDED_COMBAT.put(Quest.VAMPYRE_SLAYER, 20);
		RECOMMENDED_COMBAT.put(Quest.THE_CORSAIR_CURSE, 20);
		RECOMMENDED_COMBAT.put(Quest.DRAGON_SLAYER_I, 45);
		RECOMMENDED_COMBAT.put(Quest.MONKEY_MADNESS_I, 65);
		RECOMMENDED_COMBAT.put(Quest.TREE_GNOME_VILLAGE, 45);
		RECOMMENDED_COMBAT.put(Quest.THE_GRAND_TREE, 50);
		RECOMMENDED_COMBAT.put(Quest.HORROR_FROM_THE_DEEP, 50);
		RECOMMENDED_COMBAT.put(Quest.LOST_CITY, 45);
		RECOMMENDED_COMBAT.put(Quest.DESERT_TREASURE_I, 70);
		RECOMMENDED_COMBAT.put(Quest.DRAGON_SLAYER_II, 100);
		RECOMMENDED_COMBAT.put(Quest.SONG_OF_THE_ELVES, 95);
		RECOMMENDED_COMBAT.put(Quest.LEGENDS_QUEST, 65);
		RECOMMENDED_COMBAT.put(Quest.HEROES_QUEST, 50);
		RECOMMENDED_COMBAT.put(Quest.UNDERGROUND_PASS, 60);
		RECOMMENDED_COMBAT.put(Quest.REGICIDE, 60);
		RECOMMENDED_COMBAT.put(Quest.MOURNINGS_END_PART_I, 60);
		RECOMMENDED_COMBAT.put(Quest.MOURNINGS_END_PART_II, 65);
		RECOMMENDED_COMBAT.put(Quest.FAMILY_CREST, 55);
		RECOMMENDED_COMBAT.put(Quest.WATERFALL_QUEST, 25);
		RECOMMENDED_COMBAT.put(Quest.FIGHT_ARENA, 50);
		RECOMMENDED_COMBAT.put(Quest.SHILO_VILLAGE, 45);
		RECOMMENDED_COMBAT.put(Quest.HAUNTED_MINE, 50);
		RECOMMENDED_COMBAT.put(Quest.CABIN_FEVER, 35);
		RECOMMENDED_COMBAT.put(Quest.RUM_DEAL, 50);
		RECOMMENDED_COMBAT.put(Quest.ZOGRE_FLESH_EATERS, 45);
		RECOMMENDED_COMBAT.put(Quest.CREATURE_OF_FENKENSTRAIN, 30);
		RECOMMENDED_COMBAT.put(Quest.NATURE_SPIRIT, 20);
		RECOMMENDED_COMBAT.put(Quest.IN_SEARCH_OF_THE_MYREQUE, 45);
		RECOMMENDED_COMBAT.put(Quest.IN_AID_OF_THE_MYREQUE, 45);
		RECOMMENDED_COMBAT.put(Quest.DARKNESS_OF_HALLOWVALE, 50);
		RECOMMENDED_COMBAT.put(Quest.A_TASTE_OF_HOPE, 70);
		RECOMMENDED_COMBAT.put(Quest.SINS_OF_THE_FATHER, 95);
		RECOMMENDED_COMBAT.put(Quest.A_NIGHT_AT_THE_THEATRE, 95);
		RECOMMENDED_COMBAT.put(Quest.HOLY_GRAIL, 50);
		RECOMMENDED_COMBAT.put(Quest.MONKEY_MADNESS_II, 90);
		RECOMMENDED_COMBAT.put(Quest.THE_FREMENNIK_TRIALS, 40);
		RECOMMENDED_COMBAT.put(Quest.THE_FREMENNIK_ISLES, 50);
		RECOMMENDED_COMBAT.put(Quest.THE_FREMENNIK_EXILES, 80);
		RECOMMENDED_COMBAT.put(Quest.MOUNTAIN_DAUGHTER, 40);
		RECOMMENDED_COMBAT.put(Quest.CONTACT, 70);
		RECOMMENDED_COMBAT.put(Quest.BENEATH_CURSED_SANDS, 85);
		RECOMMENDED_COMBAT.put(Quest.SHADOW_OF_THE_STORM, 50);
		RECOMMENDED_COMBAT.put(Quest.DESERT_TREASURE_II__THE_FALLEN_EMPIRE, 100);
		RECOMMENDED_COMBAT.put(Quest.SECRETS_OF_THE_NORTH, 85);
		RECOMMENDED_COMBAT.put(Quest.DEFENDER_OF_VARROCK, 65);
		RECOMMENDED_COMBAT.put(Quest.THE_GREAT_BRAIN_ROBBERY, 60);
		RECOMMENDED_COMBAT.put(Quest.SCORPION_CATCHER, 40);
		RECOMMENDED_COMBAT.put(Quest.GRIM_TALES, 55);
		RECOMMENDED_COMBAT.put(Quest.THE_SLUG_MENACE, 35);
		RECOMMENDED_COMBAT.put(Quest.OLAFS_QUEST, 50);
		RECOMMENDED_COMBAT.put(Quest.MAKING_FRIENDS_WITH_MY_ARM, 65);
		RECOMMENDED_COMBAT.put(Quest.SWAN_SONG, 60);
		RECOMMENDED_COMBAT.put(Quest.BIG_CHOMPY_BIRD_HUNTING, 25);
		RECOMMENDED_COMBAT.put(Quest.A_KINGDOM_DIVIDED, 70);
		RECOMMENDED_COMBAT.put(Quest.ELEMENTAL_WORKSHOP_I, 15);
		RECOMMENDED_COMBAT.put(Quest.FAIRYTALE_I__GROWING_PAINS, 50);
		RECOMMENDED_COMBAT.put(Quest.FAIRYTALE_II__CURE_A_QUEEN, 55);
		RECOMMENDED_COMBAT.put(Quest.TROLL_STRONGHOLD, 50);
		RECOMMENDED_COMBAT.put(Quest.THE_TOURIST_TRAP, 20);
		RECOMMENDED_COMBAT.put(Quest.WITCHS_HOUSE, 35);
		RECOMMENDED_COMBAT.put(Quest.HIS_FAITHFUL_SERVANTS, 70);
		RECOMMENDED_COMBAT.put(Quest.THE_GARDEN_OF_DEATH, 20);
		RECOMMENDED_COMBAT.put(Quest.WHILE_GUTHIX_SLEEPS, 95);
		RECOMMENDED_COMBAT.put(Quest.THE_CURSE_OF_ARRAV, 85);
		RECOMMENDED_COMBAT.put(Quest.LAND_OF_THE_GOBLINS, 65);
		RECOMMENDED_COMBAT.put(Quest.PERILOUS_MOONS, 75);
		RECOMMENDED_COMBAT.put(Quest.THE_HEART_OF_DARKNESS, 65);
		RECOMMENDED_COMBAT.put(Quest.THE_FINAL_DAWN, 85);
		RECOMMENDED_COMBAT.put(Quest.MEAT_AND_GREET, 60);
		RECOMMENDED_COMBAT.put(Quest.SHADOWS_OF_CUSTODIA, 60);
		RECOMMENDED_COMBAT.put(Quest.ROYAL_TROUBLE, 50);
		RECOMMENDED_COMBAT.put(Quest.TROLL_ROMANCE, 50);
		RECOMMENDED_COMBAT.put(Quest.ICTHLARINS_LITTLE_HELPER, 50);
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

	/**
	 * Skills that a quest rewards fixed XP in. Returns an empty list
	 * for quests with no fixed XP rewards or quests not in the table.
	 * Choice lamps are excluded.
	 */
	public static List<Skill> xpRewards(Quest quest)
	{
		if (quest == null) return Collections.emptyList();
		return XP_REWARDS.getOrDefault(quest, Collections.emptyList());
	}

	/**
	 * Recommended combat level for a quest (wiki suggestion), or 0 if none.
	 * Not a hard requirement — seeded as an optional goal.
	 */
	public static int recommendedCombatLevel(Quest quest)
	{
		if (quest == null) return 0;
		return RECOMMENDED_COMBAT.getOrDefault(quest, 0);
	}

	/**
	 * Quest point reward for a quest, or 0 if not in the table.
	 */
	public static int questPointReward(Quest quest)
	{
		if (quest == null) return 0;
		return QP_REWARDS.getOrDefault(quest, 0);
	}

	/** True iff the quest rewards an XP lamp (choice-based). */
	public static boolean rewardsLamp(Quest quest)
	{
		return quest != null && LAMP_REWARD_QUESTS.contains(quest);
	}

	/** True iff the quest is free-to-play. */
	public static boolean isF2P(Quest quest)
	{
		return quest != null && F2P_QUESTS.contains(quest);
	}

	/** True iff the quest has a non-empty requirements entry. */
	public static boolean hasRequirements(Quest quest)
	{
		Reqs r = lookup(quest);
		if (r == null) return false;
		return !r.skills.isEmpty() || !r.prereqQuests.isEmpty()
			|| r.questPoints > 0 || r.combatLevel > 0 || r.kudos > 0;
	}

	private QuestRequirements() {}
}
