package com.goalplanner.data;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
 * flow in {@code GoalPlannerApiImpl.addQuestGoalWithPrereqs}, which
 * feeds each entry through {@code findOrCreateRequirement} inside a
 * compound command.
 *
 * <p><b>Coverage.</b> This table covers all 209 quests in the
 * RuneLite {@link Quest} enum. Requirements are wiki-sourced.
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

	/** Miniquests — 0 QP, shorter, different description in the goal card. */
	private static final java.util.Set<Quest> MINIQUESTS = java.util.EnumSet.of(
		Quest.ALFRED_GRIMHANDS_BARCRAWL,
		Quest.BARBARIAN_TRAINING,
		Quest.BEAR_YOUR_SOUL,
		Quest.CURSE_OF_THE_EMPTY_LORD,
		Quest.DADDYS_HOME,
		Quest.ENTER_THE_ABYSS,
		Quest.FAMILY_PEST,
		Quest.THE_ENCHANTED_KEY,
		Quest.THE_FROZEN_DOOR,
		Quest.THE_GENERALS_SHADOW,
		Quest.HIS_FAITHFUL_SERVANTS,
		Quest.HOPESPEARS_WILL,
		Quest.IN_SEARCH_OF_KNOWLEDGE,
		Quest.INTO_THE_TOMBS,
		Quest.LAIR_OF_TARN_RAZORLOR,
		Quest.LEARNING_THE_ROPES,
		Quest.MAGE_ARENA_I,
		Quest.MAGE_ARENA_II,
		Quest.SKIPPY_AND_THE_MOGRES,
		Quest.VALE_TOTEMS
	);

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
		try (InputStream in = QuestRequirements.class.getResourceAsStream("quest-requirements.tsv"))
		{
			if (in == null)
			{
				throw new IllegalStateException("missing resource: quest-requirements.tsv");
			}
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)))
			{
				String line;
				while ((line = reader.readLine()) != null)
				{
					if (line.isEmpty())
					{
						continue;
					}
					// quest \t skill:lvl;... \t prereqQuest;... \t questPoints \t combatLevel
					String[] f = line.split("\t", -1);
					List<SkillReq> skills = new ArrayList<>();
					if (!f[1].isEmpty())
					{
						for (String sk : f[1].split(";"))
						{
							int c = sk.indexOf(':');
							skills.add(new SkillReq(Skill.valueOf(sk.substring(0, c)),
								Integer.parseInt(sk.substring(c + 1))));
						}
					}
					List<Quest> prereqs = new ArrayList<>();
					if (!f[2].isEmpty())
					{
						for (String q : f[2].split(";"))
						{
							prereqs.add(Quest.valueOf(q));
						}
					}
					put(Quest.valueOf(f[0]), skills, prereqs,
						Integer.parseInt(f[3]), Integer.parseInt(f[4]));
				}
			}
		}
		catch (java.io.IOException e)
		{
			throw new java.io.UncheckedIOException("failed to load quest-requirements.tsv", e);
		}
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
		XP_REWARDS.put(Quest.RECRUITMENT_DRIVE, List.of(Skill.PRAYER, Skill.HERBLORE, Skill.AGILITY));
		XP_REWARDS.put(Quest.ROYAL_TROUBLE, List.of(Skill.AGILITY, Skill.SLAYER, Skill.HITPOINTS));
		XP_REWARDS.put(Quest.SCORPION_CATCHER, List.of(Skill.STRENGTH));
		XP_REWARDS.put(Quest.SEA_SLUG, List.of(Skill.FISHING));
		XP_REWARDS.put(Quest.THE_SLUG_MENACE, List.of(Skill.CRAFTING, Skill.RUNECRAFT, Skill.THIEVING));
		XP_REWARDS.put(Quest.SHADES_OF_MORTTON, List.of(Skill.HERBLORE, Skill.CRAFTING));
		XP_REWARDS.put(Quest.SPIRITS_OF_THE_ELID, List.of(Skill.PRAYER, Skill.THIEVING, Skill.MAGIC));
		XP_REWARDS.put(Quest.TAI_BWO_WANNAI_TRIO, List.of(Skill.COOKING, Skill.FISHING, Skill.ATTACK, Skill.STRENGTH));
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
		QP_REWARDS.put(Quest.TAI_BWO_WANNAI_TRIO, 2);
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
	// Recommended skills (wiki-sourced suggestions, not hard
	// requirements). Seeded as optional goals.
	// ============================================================
	private static final Map<Quest, List<SkillReq>> RECOMMENDED_SKILLS = new EnumMap<>(Quest.class);

	static
	{
		RECOMMENDED_SKILLS.put(Quest.DRAGON_SLAYER_I, List.of(new SkillReq(Skill.MAGIC, 33)));
		RECOMMENDED_SKILLS.put(Quest.DORICS_QUEST, List.of(new SkillReq(Skill.MINING, 15)));
		RECOMMENDED_SKILLS.put(Quest.CONTACT, List.of(
			new SkillReq(Skill.AGILITY, 50),
			new SkillReq(Skill.THIEVING, 50)));
	}

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
		// Batch 2 (wiki-sourced 2026-04-10)
		RECOMMENDED_COMBAT.put(Quest.BELOW_ICE_MOUNTAIN, 15);
		RECOMMENDED_COMBAT.put(Quest.SCRAMBLED, 50);
		RECOMMENDED_COMBAT.put(Quest.ANOTHER_SLICE_OF_HAM, 35);
		RECOMMENDED_COMBAT.put(Quest.A_PORCINE_OF_INTEREST, 20);
		RECOMMENDED_COMBAT.put(Quest.A_SOULS_BANE, 30);
		RECOMMENDED_COMBAT.put(Quest.BETWEEN_A_ROCK, 50);
		RECOMMENDED_COMBAT.put(Quest.COLD_WAR, 30);
		RECOMMENDED_COMBAT.put(Quest.DEATH_ON_THE_ISLE, 40);
		RECOMMENDED_COMBAT.put(Quest.DEATH_TO_THE_DORGESHUUN, 30);
		RECOMMENDED_COMBAT.put(Quest.DEVIOUS_MINDS, 30);
		RECOMMENDED_COMBAT.put(Quest.EADGARS_RUSE, 50);
		RECOMMENDED_COMBAT.put(Quest.EAGLES_PEAK, 10);
		RECOMMENDED_COMBAT.put(Quest.ELEMENTAL_WORKSHOP_II, 15);
		RECOMMENDED_COMBAT.put(Quest.ENAKHRAS_LAMENT, 25);
		RECOMMENDED_COMBAT.put(Quest.GHOSTS_AHOY, 15);
		RECOMMENDED_COMBAT.put(Quest.HAZEEL_CULT, 10);
		RECOMMENDED_COMBAT.put(Quest.INTO_THE_TOMBS, 95);
		RECOMMENDED_COMBAT.put(Quest.LAIR_OF_TARN_RAZORLOR, 50);
		RECOMMENDED_COMBAT.put(Quest.LUNAR_DIPLOMACY, 50);
		RECOMMENDED_COMBAT.put(Quest.MAGE_ARENA_I, 55);
		RECOMMENDED_COMBAT.put(Quest.MAGE_ARENA_II, 80);
		RECOMMENDED_COMBAT.put(Quest.MY_ARMS_BIG_ADVENTURE, 55);
		RECOMMENDED_COMBAT.put(Quest.ONE_SMALL_FAVOUR, 45);
		RECOMMENDED_COMBAT.put(Quest.RECIPE_FOR_DISASTER__KING_AWOWOGEI, 65);
		RECOMMENDED_COMBAT.put(Quest.RECIPE_FOR_DISASTER__SIR_AMIK_VARZE, 70);
		RECOMMENDED_COMBAT.put(Quest.ROVING_ELVES, 60);
		RECOMMENDED_COMBAT.put(Quest.SHADES_OF_MORTTON, 25);
		RECOMMENDED_COMBAT.put(Quest.SPIRITS_OF_THE_ELID, 40);
		RECOMMENDED_COMBAT.put(Quest.THE_FEUD, 40);
		RECOMMENDED_COMBAT.put(Quest.THE_FROZEN_DOOR, 90);
		RECOMMENDED_COMBAT.put(Quest.THE_GENERALS_SHADOW, 60);
		RECOMMENDED_COMBAT.put(Quest.WANTED, 15);
		RECOMMENDED_COMBAT.put(Quest.WATCHTOWER, 35);
		RECOMMENDED_COMBAT.put(Quest.WHAT_LIES_BELOW, 30);
		RECOMMENDED_COMBAT.put(Quest.TEMPLE_OF_IKOV, 45);
		RECOMMENDED_COMBAT.put(Quest.TROUBLED_TORTUGANS, 60);
		RECOMMENDED_COMBAT.put(Quest.THE_RED_REEF, 65);
		RECOMMENDED_COMBAT.put(Quest.HOPESPEARS_WILL, 90);
		RECOMMENDED_COMBAT.put(Quest.RECRUITMENT_DRIVE, 10);
		RECOMMENDED_COMBAT.put(Quest.CURSE_OF_THE_EMPTY_LORD, 50);
		RECOMMENDED_COMBAT.put(Quest.BLACK_KNIGHTS_FORTRESS, 15);
		RECOMMENDED_COMBAT.put(Quest.PRINCE_ALI_RESCUE, 10);
		RECOMMENDED_COMBAT.put(Quest.SHIELD_OF_ARRAV, 10);
		RECOMMENDED_COMBAT.put(Quest.THE_ASCENT_OF_ARCEUUS, 15);
		RECOMMENDED_COMBAT.put(Quest.BIOHAZARD, 10);
		RECOMMENDED_COMBAT.put(Quest.THE_IDES_OF_MILK, 15);
		RECOMMENDED_COMBAT.put(Quest.IN_SEARCH_OF_KNOWLEDGE, 45);
		RECOMMENDED_COMBAT.put(Quest.BARBARIAN_TRAINING, 70);
	}

	// ============================================================
	// Display names — short labels for quests whose RuneLite enum
	// name is too long for tags or UI labels. Falls back to
	// Quest.getName() when not overridden.
	// ============================================================
	private static final Map<Quest, String> DISPLAY_NAMES = new EnumMap<>(Quest.class);

	static
	{
		DISPLAY_NAMES.put(Quest.DESERT_TREASURE_II__THE_FALLEN_EMPIRE, "Desert Treasure II");
		DISPLAY_NAMES.put(Quest.THE_RIBBITING_TALE_OF_A_LILY_PAD_LABOUR_DISPUTE, "Ribbiting Tale");
		DISPLAY_NAMES.put(Quest.RECIPE_FOR_DISASTER__ANOTHER_COOKS_QUEST, "RFD: Another Cook's Quest");
		DISPLAY_NAMES.put(Quest.RECIPE_FOR_DISASTER__MOUNTAIN_DWARF, "RFD: Mountain Dwarf");
		DISPLAY_NAMES.put(Quest.RECIPE_FOR_DISASTER__WARTFACE__BENTNOZE, "RFD: Goblin Generals");
		DISPLAY_NAMES.put(Quest.RECIPE_FOR_DISASTER__PIRATE_PETE, "RFD: Pirate Pete");
		DISPLAY_NAMES.put(Quest.RECIPE_FOR_DISASTER__LUMBRIDGE_GUIDE, "RFD: Lumbridge Guide");
		DISPLAY_NAMES.put(Quest.RECIPE_FOR_DISASTER__EVIL_DAVE, "RFD: Evil Dave");
		DISPLAY_NAMES.put(Quest.RECIPE_FOR_DISASTER__SKRACH_UGLOGWEE, "RFD: Skrach Uglogwee");
		DISPLAY_NAMES.put(Quest.RECIPE_FOR_DISASTER__SIR_AMIK_VARZE, "RFD: Sir Amik Varze");
		DISPLAY_NAMES.put(Quest.RECIPE_FOR_DISASTER__KING_AWOWOGEI, "RFD: King Awowogei");
		DISPLAY_NAMES.put(Quest.RECIPE_FOR_DISASTER__CULINAROMANCER, "RFD: Culinaromancer");
	}

	/**
	 * Short display name for a quest, suitable for tags and UI labels.
	 * Falls back to {@code quest.getName()} if no override is defined.
	 */
	public static String displayName(Quest quest)
	{
		if (quest == null) return null;
		return DISPLAY_NAMES.getOrDefault(quest, quest.getName());
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
	 * Recommended skills for a quest (wiki suggestions, not hard requirements).
	 * Seeded as optional goals. Returns an empty list if none.
	 */
	public static List<SkillReq> recommendedSkills(Quest quest)
	{
		if (quest == null) return Collections.emptyList();
		return RECOMMENDED_SKILLS.getOrDefault(quest, Collections.emptyList());
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
	/** True iff the quest is a miniquest (not a full quest). */
	public static boolean isMiniquest(Quest quest)
	{
		return quest != null && MINIQUESTS.contains(quest);
	}

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
			|| r.questPoints > 0 || r.combatLevel > 0 || r.kudos > 0
			|| recommendedCombatLevel(quest) > 0
			|| !recommendedSkills(quest).isEmpty();
	}

	private QuestRequirements() {}
}
