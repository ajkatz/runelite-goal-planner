package com.goaltracker.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.api.gameval.VarPlayerID;

/**
 * Maps boss display names to their VarPlayerID kill count variables.
 * Used by BossKillTracker to read live kill counts and by the
 * Add Goal dialog to populate the boss dropdown.
 *
 * <p>Sorted alphabetically by display name for the dropdown.
 */
public final class BossKillData
{
	/** Boss name → VarPlayerID for kill count. */
	private static final Map<String, Integer> BOSSES = new LinkedHashMap<>();

	static
	{
		// GWD
		BOSSES.put("Commander Zilyana", VarPlayerID.TOTAL_SARADOMIN_KILLS);
		BOSSES.put("General Graardor", VarPlayerID.TOTAL_BANDOS_KILLS);
		BOSSES.put("K'ril Tsutsaroth", VarPlayerID.TOTAL_ZAMORAK_KILLS);
		BOSSES.put("Kree'arra", VarPlayerID.TOTAL_ARMADYL_KILLS);
		BOSSES.put("Nex", VarPlayerID.TOTAL_NEX_KILLS);

		// DKs
		BOSSES.put("Dagannoth Prime", VarPlayerID.TOTAL_PRIME_KILLS);
		BOSSES.put("Dagannoth Rex", VarPlayerID.TOTAL_REX_KILLS);
		BOSSES.put("Dagannoth Supreme", VarPlayerID.TOTAL_SUPREME_KILLS);

		// Wilderness bosses
		BOSSES.put("Artio", VarPlayerID.TOTAL_ARTIO_KILLS);
		BOSSES.put("Callisto", VarPlayerID.TOTAL_CALLISTO_KILLS);
		BOSSES.put("Calvar'ion", VarPlayerID.TOTAL_CALVARION_KILLS);
		BOSSES.put("Chaos Elemental", VarPlayerID.TOTAL_CHAOSELE_KILLS);
		BOSSES.put("Chaos Fanatic", VarPlayerID.TOTAL_CHAOSFANATIC_KILLS);
		BOSSES.put("Crazy Archaeologist", VarPlayerID.TOTAL_CRAZYARCHAEOLOGIST_KILLS);
		BOSSES.put("Scorpia", VarPlayerID.TOTAL_SCORPIA_KILLS);
		BOSSES.put("Spindel", VarPlayerID.TOTAL_SPINDEL_KILLS);
		BOSSES.put("Venenatis", VarPlayerID.TOTAL_VENENATIS_KILLS);
		BOSSES.put("Vet'ion", VarPlayerID.TOTAL_VETION_KILLS);

		// Slayer bosses
		BOSSES.put("Abyssal Sire", VarPlayerID.TOTAL_ABYSSALSIRE_KILLS);
		BOSSES.put("Cerberus", VarPlayerID.TOTAL_CERBERUS_KILLS);
		BOSSES.put("Grotesque Guardians", VarPlayerID.TOTAL_GARGBOSS_KILLS);
		BOSSES.put("Kraken", VarPlayerID.TOTAL_KRAKEN_BOSS_KILLS);
		BOSSES.put("Thermy", VarPlayerID.TOTAL_THERMY_KILLS);
		BOSSES.put("Alchemical Hydra", VarPlayerID.TOTAL_HYDRABOSS_KILLS);

		// Other bosses
		BOSSES.put("Araxxor", VarPlayerID.TOTAL_ARAXXOR_KILLS);
		BOSSES.put("Barrows", VarPlayerID.TOTAL_BARROWS_CHESTS);
		BOSSES.put("Bryophyta", VarPlayerID.TOTAL_BRYOPHYTA_KILLS);
		BOSSES.put("Corporeal Beast", VarPlayerID.TOTAL_CORP_KILLS);
		BOSSES.put("Deranged Arch.", VarPlayerID.TOTAL_DERANGEDARCHAEOLOGIST_KILLS);
		BOSSES.put("Giant Mole", VarPlayerID.TOTAL_MOLE_KILLS);
		BOSSES.put("Hespori", VarPlayerID.TOTAL_HESPORI_KILLS);
		BOSSES.put("Kalphite Queen", VarPlayerID.TOTAL_KALPHITE_KILLS);
		BOSSES.put("King Black Dragon", VarPlayerID.TOTAL_KBD_KILLS);
		BOSSES.put("Mimic", VarPlayerID.TOTAL_MIMIC_KILLS);
		BOSSES.put("Obor", VarPlayerID.TOTAL_HILLGIANT_BOSS_KILLS);
		BOSSES.put("Phantom Muspah", VarPlayerID.TOTAL_MUSPAH_KILLS);
		BOSSES.put("Sarachnis", VarPlayerID.TOTAL_SARACHNIS_KILLS);
		BOSSES.put("Skotizo", VarPlayerID.TOTAL_CATA_BOSS_KILLS);
		BOSSES.put("Vorkath", VarPlayerID.TOTAL_VORKATH_KILLS);
		BOSSES.put("Zalcano", VarPlayerID.TOTAL_ZALCANO_KILLS);
		BOSSES.put("Zulrah", VarPlayerID.TOTAL_SNAKEBOSS_KILLS);

		// DT2 bosses
		BOSSES.put("Duke Sucellus", VarPlayerID.TOTAL_DUKE_SUCELLUS_KILLS);
		BOSSES.put("The Leviathan", VarPlayerID.TOTAL_LEVIATHAN_KILLS);
		BOSSES.put("The Whisperer", VarPlayerID.TOTAL_WHISPERER_KILLS);
		BOSSES.put("Vardorvis", VarPlayerID.TOTAL_VARDORVIS_KILLS);
		BOSSES.put("Duke (Awake)", VarPlayerID.TOTAL_DUKE_SUCELLUS_AWAKENED_KILLS);
		BOSSES.put("Leviathan (Awake)", VarPlayerID.TOTAL_LEVIATHAN_AWAKENED_KILLS);
		BOSSES.put("Whisperer (Awake)", VarPlayerID.TOTAL_WHISPERER_AWAKENED_KILLS);
		BOSSES.put("Vardorvis (Awake)", VarPlayerID.TOTAL_VARDORVIS_AWAKENED_KILLS);

		// Raids
		BOSSES.put("CoX", VarPlayerID.TOTAL_COMPLETED_XERICCHAMBERS);
		BOSSES.put("CoX (CM)", VarPlayerID.TOTAL_COMPLETED_XERICCHAMBERS_CHALLENGE);
		BOSSES.put("ToB", VarPlayerID.TOTAL_COMPLETED_THEATREOFBLOOD);
		BOSSES.put("ToB (HM)", VarPlayerID.TOTAL_COMPLETED_THEATREOFBLOOD_HARD);
		BOSSES.put("ToB (Story)", VarPlayerID.TOTAL_COMPLETED_THEATREOFBLOOD_STORY);
		BOSSES.put("ToA (Entry)", VarPlayerID.TOTAL_COMPLETED_TOMBSOFAMASCUT_ENTRY);
		BOSSES.put("ToA", VarPlayerID.TOTAL_COMPLETED_TOMBSOFAMASCUT);
		BOSSES.put("ToA (Expert)", VarPlayerID.TOTAL_COMPLETED_TOMBSOFAMASCUT_EXPERT);

		// Waves / Colosseum
		BOSSES.put("TzTok-Jad", VarPlayerID.TOTAL_JAD_KILLS);
		BOSSES.put("TzKal-Zuk", VarPlayerID.TOTAL_ZUK_KILLS);
		BOSSES.put("Sol Heredit", VarPlayerID.TOTAL_SOL_KILLS);
		BOSSES.put("Fortis Colosseum (Waves)", VarPlayerID.TOTAL_COLOSSEUM_WAVES_COMPLETED);

		// Minigame bosses
		BOSSES.put("The Nightmare", VarPlayerID.TOTAL_NIGHTMARE_KILLS);
		BOSSES.put("Phosani's Nightmare", VarPlayerID.TOTAL_NIGHTMARE_CHALLENGE_KILLS);
		BOSSES.put("Tempoross", VarPlayerID.TOTAL_TEMPOROSS_KILLS);
		BOSSES.put("Wintertodt", VarPlayerID.TOTAL_WINTERTODT_KILLS);
		BOSSES.put("GotR", VarPlayerID.TOTAL_GOTR_KILLS);
		BOSSES.put("The Gauntlet", VarPlayerID.TOTAL_COMPLETED_GAUNTLET);
		BOSSES.put("Corrupted Gauntlet", VarPlayerID.TOTAL_COMPLETED_GAUNTLET_HM);

		// Newer bosses
		BOSSES.put("Amoxliatl", VarPlayerID.TOTAL_AMOXLIATL_KILLS);
		BOSSES.put("Hueycoatl", VarPlayerID.TOTAL_HUEY_KILLS);
		BOSSES.put("Royal Titans", VarPlayerID.TOTAL_ROYAL_TITAN_KILLS);
		BOSSES.put("Yama", VarPlayerID.TOTAL_YAMA_KILLS);
		BOSSES.put("Scurrius", VarPlayerID.TOTAL_RAT_BOSS_KILLS);
		BOSSES.put("Brutus", VarPlayerID.TOTAL_COWBOSS_KILLS);
		BOSSES.put("Demonic Brutus", VarPlayerID.TOTAL_COWBOSS_HARDMODE_KILLS);
		BOSSES.put("Shellbane Gryphon", VarPlayerID.TOTAL_GRYPHON_BOSS_KILLS);

		// Perilous Moons (per-boss + chest aggregate)
		BOSSES.put("Blue Moon", VarPlayerID.PMOON_BLUE_KILLS);
		BOSSES.put("Blood Moon", VarPlayerID.PMOON_BLOOD_KILLS);
		BOSSES.put("Eclipse Moon", VarPlayerID.PMOON_ECLIPSE_KILLS);
		BOSSES.put("Perilous Moons Chests", VarPlayerID.TOTAL_PMOON_CHESTS);

		// Doom of Mokhaiotl (per-level completions — delve-style activity)
		BOSSES.put("Doom of Mokhaiotl (L1)", VarPlayerID.DOM_LEVEL_1_COMPLETIONS);
		BOSSES.put("Doom of Mokhaiotl (L2)", VarPlayerID.DOM_LEVEL_2_COMPLETIONS);
		BOSSES.put("Doom of Mokhaiotl (L3)", VarPlayerID.DOM_LEVEL_3_COMPLETIONS);
		BOSSES.put("Doom of Mokhaiotl (L4)", VarPlayerID.DOM_LEVEL_4_COMPLETIONS);
		BOSSES.put("Doom of Mokhaiotl (L5)", VarPlayerID.DOM_LEVEL_5_COMPLETIONS);
		BOSSES.put("Doom of Mokhaiotl (L6)", VarPlayerID.DOM_LEVEL_6_COMPLETIONS);
		BOSSES.put("Doom of Mokhaiotl (L7)", VarPlayerID.DOM_LEVEL_7_COMPLETIONS);
		BOSSES.put("Doom of Mokhaiotl (L8)", VarPlayerID.DOM_LEVEL_8_COMPLETIONS);
		BOSSES.put("Doom of Mokhaiotl (L8+)", VarPlayerID.DOM_LEVEL_8_PLUS_COMPLETIONS);
	}

	/** Boss name → pet item ID for the card icon. 0 = no pet icon. */
	private static final Map<String, Integer> PET_ICONS = new HashMap<>();

	static
	{
		// GWD
		PET_ICONS.put("Commander Zilyana", ItemID.PET_ZILYANA);
		PET_ICONS.put("General Graardor", ItemID.PET_GENERAL_GRAARDOR);
		PET_ICONS.put("K'ril Tsutsaroth", ItemID.PET_KRIL_TSUTSAROTH);
		PET_ICONS.put("Kree'arra", ItemID.PET_KREEARRA);
		PET_ICONS.put("Nex", ItemID.NEXLING);

		// DKs
		PET_ICONS.put("Dagannoth Prime", ItemID.PET_DAGANNOTH_PRIME);
		PET_ICONS.put("Dagannoth Rex", ItemID.PET_DAGANNOTH_REX);
		PET_ICONS.put("Dagannoth Supreme", ItemID.PET_DAGANNOTH_SUPREME);

		// Wilderness
		PET_ICONS.put("Callisto", ItemID.CALLISTO_CUB);
		PET_ICONS.put("Chaos Elemental", ItemID.PET_CHAOS_ELEMENTAL);
		PET_ICONS.put("Chaos Fanatic", ItemID.PET_CHAOS_ELEMENTAL);
		PET_ICONS.put("Crazy Archaeologist", ItemID.FEDORA);
		PET_ICONS.put("Scorpia", ItemID.SCORPIAS_OFFSPRING);
		PET_ICONS.put("Obor", ItemID.HILL_GIANT_CLUB);
		PET_ICONS.put("Venenatis", ItemID.CALLISTO_CUB); // shares cub model
		PET_ICONS.put("Vet'ion", ItemID.VETION_JR);
		PET_ICONS.put("Artio", ItemID.CALLISTO_CUB);
		PET_ICONS.put("Calvar'ion", ItemID.VETION_JR);
		PET_ICONS.put("Spindel", ItemID.CALLISTO_CUB);

		// Slayer
		PET_ICONS.put("Abyssal Sire", ItemID.ABYSSAL_ORPHAN);
		PET_ICONS.put("Cerberus", 13247); // Hellpuppy
		PET_ICONS.put("Grotesque Guardians", ItemID.NOON);
		PET_ICONS.put("Kraken", ItemID.PET_KRAKEN);
		PET_ICONS.put("Thermy", ItemID.PET_SMOKE_DEVIL);
		PET_ICONS.put("Alchemical Hydra", ItemID.IKKLE_HYDRA);

		// Other bosses
		PET_ICONS.put("Araxxor", ItemID.NID);
		PET_ICONS.put("Bryophyta", ItemID.BRYOPHYTAS_ESSENCE);
		PET_ICONS.put("Deranged Arch.", ItemID.STEEL_RING);
		PET_ICONS.put("Corporeal Beast", ItemID.PET_DARK_CORE);
		PET_ICONS.put("Giant Mole", ItemID.BABY_MOLE);
		PET_ICONS.put("Kalphite Queen", ItemID.KALPHITE_PRINCESS);
		PET_ICONS.put("King Black Dragon", ItemID.PRINCE_BLACK_DRAGON);
		PET_ICONS.put("Phantom Muspah", ItemID.MUPHIN);
		PET_ICONS.put("Sarachnis", ItemID.SRARACHA);
		PET_ICONS.put("Skotizo", ItemID.SKOTOS);
		PET_ICONS.put("Mimic", ItemID.MIMIC);
		PET_ICONS.put("Vorkath", ItemID.VORKI);
		PET_ICONS.put("Zulrah", ItemID.PET_SNAKELING);
		PET_ICONS.put("Scurrius", ItemID.SCURRY); // not "Scurrius"

		// DT2
		PET_ICONS.put("Duke Sucellus", ItemID.BARON);
		PET_ICONS.put("The Leviathan", ItemID.LIL_CREATOR);
		PET_ICONS.put("The Whisperer", ItemID.WISP);
		PET_ICONS.put("Vardorvis", ItemID.BUTCH);
		PET_ICONS.put("Duke (Awake)", ItemID.BARON);
		PET_ICONS.put("Leviathan (Awake)", ItemID.LIL_CREATOR);
		PET_ICONS.put("Whisperer (Awake)", ItemID.WISP);
		PET_ICONS.put("Vardorvis (Awake)", ItemID.BUTCH);

		// Raids
		PET_ICONS.put("CoX", ItemID.OLMLET);
		PET_ICONS.put("CoX (CM)", ItemID.OLMLET);
		PET_ICONS.put("ToB", ItemID.LIL_ZIK);
		PET_ICONS.put("ToB (HM)", ItemID.LIL_ZIK);
		PET_ICONS.put("ToB (Story)", ItemID.LIL_ZIK);
		PET_ICONS.put("ToA (Entry)", ItemID.TUMEKENS_GUARDIAN);
		PET_ICONS.put("ToA", ItemID.TUMEKENS_GUARDIAN);
		PET_ICONS.put("ToA (Expert)", ItemID.TUMEKENS_GUARDIAN);

		// Waves / Colosseum
		PET_ICONS.put("TzTok-Jad", ItemID.TZREKJAD);
		PET_ICONS.put("TzKal-Zuk", ItemID.TZREKZUK);
		PET_ICONS.put("Sol Heredit", ItemID.SMOL_HEREDIT);
		PET_ICONS.put("Fortis Colosseum (Waves)", ItemID.SMOL_HEREDIT);

		// Minigame bosses
		PET_ICONS.put("The Nightmare", ItemID.LITTLE_NIGHTMARE);
		PET_ICONS.put("Phosani's Nightmare", ItemID.LITTLE_NIGHTMARE);
		PET_ICONS.put("Wintertodt", ItemID.PHOENIX);
		PET_ICONS.put("Tempoross", ItemID.TINY_TEMPOR);
		PET_ICONS.put("Zalcano", ItemID.SMOLCANO);
		PET_ICONS.put("GotR", ItemID.ABYSSAL_PROTECTOR);
		PET_ICONS.put("Hespori", ItemID.BOTTOMLESS_COMPOST_BUCKET);
		PET_ICONS.put("The Gauntlet", ItemID.YOUNGLLEF);
		PET_ICONS.put("Corrupted Gauntlet", ItemID.CORRUPTED_YOUNGLLEF);

		// Newer
		PET_ICONS.put("Amoxliatl", ItemID.MOXI);
		PET_ICONS.put("Hueycoatl", ItemID.HUBERTE);
		PET_ICONS.put("Royal Titans", ItemID.BRAN);
		PET_ICONS.put("Yama", ItemID.YAMI);
		PET_ICONS.put("Scurrius", ItemID.SCURRY);
		PET_ICONS.put("Shellbane Gryphon", ItemID.GRYPHON);
		// Brutus: pet is "Beef" — use the Beef item as icon.
		PET_ICONS.put("Brutus", ItemID.BEEF);
		PET_ICONS.put("Demonic Brutus", ItemID.BEEF);
		// Perilous Moons: no unified pet — use Eclipse Atlatl as a
		// recognizable Moons icon across all four entries.
		PET_ICONS.put("Blue Moon", ItemID.ECLIPSE_ATLATL);
		PET_ICONS.put("Blood Moon", ItemID.ECLIPSE_ATLATL);
		PET_ICONS.put("Eclipse Moon", ItemID.ECLIPSE_ATLATL);
		PET_ICONS.put("Perilous Moons Chests", ItemID.ECLIPSE_ATLATL);
		// Doom of Mokhaiotl's pet is "Dom" — no ItemID constant exposed
		// in API 1.12.23; leave unmapped until an api bump provides it.
	}

	/**
	 * Collection log name → list of matching boss data names.
	 * Used when right-clicking raid entries in the collection log
	 * to show one menu entry per tier.
	 */
	private static final Map<String, java.util.List<String>> COLLECTION_LOG_ALIASES = new HashMap<>();

	static
	{
		COLLECTION_LOG_ALIASES.put("Chambers of Xeric", java.util.List.of("CoX", "CoX (CM)"));
		COLLECTION_LOG_ALIASES.put("Theatre of Blood", java.util.List.of("ToB", "ToB (HM)", "ToB (Story)"));
		COLLECTION_LOG_ALIASES.put("Tombs of Amascut", java.util.List.of("ToA (Entry)", "ToA", "ToA (Expert)"));
		COLLECTION_LOG_ALIASES.put("The Nightmare", java.util.List.of("The Nightmare", "Phosani's Nightmare"));
		COLLECTION_LOG_ALIASES.put("Duke Sucellus", java.util.List.of("Duke Sucellus", "Duke (Awake)"));
		COLLECTION_LOG_ALIASES.put("The Leviathan", java.util.List.of("The Leviathan", "Leviathan (Awake)"));
		COLLECTION_LOG_ALIASES.put("The Whisperer", java.util.List.of("The Whisperer", "Whisperer (Awake)"));
		COLLECTION_LOG_ALIASES.put("Vardorvis", java.util.List.of("Vardorvis", "Vardorvis (Awake)"));
		COLLECTION_LOG_ALIASES.put("The Fight Caves", java.util.List.of("TzTok-Jad"));
		COLLECTION_LOG_ALIASES.put("The Inferno", java.util.List.of("TzKal-Zuk"));
		COLLECTION_LOG_ALIASES.put("Fortis Colosseum", java.util.List.of(
			"Sol Heredit", "Fortis Colosseum (Waves)"));
		COLLECTION_LOG_ALIASES.put("Perilous Moons", java.util.List.of(
			"Blue Moon", "Blood Moon", "Eclipse Moon", "Perilous Moons Chests"));
		COLLECTION_LOG_ALIASES.put("The Gauntlet", java.util.List.of(
			"The Gauntlet", "Corrupted Gauntlet"));
		COLLECTION_LOG_ALIASES.put("Brutus", java.util.List.of(
			"Brutus", "Demonic Brutus"));
		COLLECTION_LOG_ALIASES.put("Doom of Mokhaiotl", java.util.List.of(
			"Doom of Mokhaiotl (L1)", "Doom of Mokhaiotl (L2)",
			"Doom of Mokhaiotl (L3)", "Doom of Mokhaiotl (L4)",
			"Doom of Mokhaiotl (L5)", "Doom of Mokhaiotl (L6)",
			"Doom of Mokhaiotl (L7)", "Doom of Mokhaiotl (L8)",
			"Doom of Mokhaiotl (L8+)"));
	}

	// ============================================================
	// Boss prerequisites — skill/unlock requirements to fight a boss.
	// Applied automatically by addBossGoal regardless of call site.
	// ============================================================

	/**
	 * Prerequisites to fight a specific boss. Mirrors the shape of
	 * {@link DiaryRequirements.Reqs} so boss gating has parity with
	 * diary-tier gating (skills, quests, unlocks, item reqs, account
	 * metrics, boss-kill prereqs, and OR-alternatives).
	 */
	public static final class BossPrereqs
	{
		public final List<SkillReq> skills;
		public final List<UnlockRef> unlocks;
		public final List<net.runelite.api.Quest> quests;
		public final List<ItemReq> itemReqs;
		public final List<AccountReq> accountReqs;
		public final List<BossReq> bossKills;
		/** OR-groups: if non-empty, ANY one satisfied unlocks the boss. */
		public final List<Alternative> alternatives;

		public BossPrereqs(List<SkillReq> skills, List<UnlockRef> unlocks)
		{
			this(skills, unlocks, Collections.emptyList());
		}

		public BossPrereqs(List<SkillReq> skills)
		{
			this(skills, Collections.emptyList(), Collections.emptyList());
		}

		public BossPrereqs(List<SkillReq> skills, List<UnlockRef> unlocks,
			List<net.runelite.api.Quest> quests)
		{
			this(skills, unlocks, quests,
				Collections.emptyList(), Collections.emptyList(),
				Collections.emptyList(), Collections.emptyList());
		}

		public BossPrereqs(List<SkillReq> skills, List<UnlockRef> unlocks,
			List<net.runelite.api.Quest> quests, List<BossReq> bossKills)
		{
			this(skills, unlocks, quests, bossKills,
				Collections.emptyList(), Collections.emptyList(),
				Collections.emptyList());
		}

		public BossPrereqs(List<SkillReq> skills, List<UnlockRef> unlocks,
			List<net.runelite.api.Quest> quests, List<BossReq> bossKills,
			List<ItemReq> itemReqs)
		{
			this(skills, unlocks, quests, bossKills, itemReqs,
				Collections.emptyList(), Collections.emptyList());
		}

		public BossPrereqs(List<SkillReq> skills, List<UnlockRef> unlocks,
			List<net.runelite.api.Quest> quests, List<BossReq> bossKills,
			List<ItemReq> itemReqs, List<AccountReq> accountReqs)
		{
			this(skills, unlocks, quests, bossKills, itemReqs, accountReqs,
				Collections.emptyList());
		}

		public BossPrereqs(List<SkillReq> skills, List<UnlockRef> unlocks,
			List<net.runelite.api.Quest> quests, List<BossReq> bossKills,
			List<ItemReq> itemReqs, List<AccountReq> accountReqs,
			List<Alternative> alternatives)
		{
			this.skills = Collections.unmodifiableList(skills);
			this.unlocks = Collections.unmodifiableList(unlocks);
			this.quests = Collections.unmodifiableList(quests);
			this.bossKills = Collections.unmodifiableList(bossKills);
			this.itemReqs = Collections.unmodifiableList(itemReqs);
			this.accountReqs = Collections.unmodifiableList(accountReqs);
			this.alternatives = Collections.unmodifiableList(alternatives);
		}
	}

	/** A skill requirement for a boss. */
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

	/** Reference to an unlock (by name + item icon). */
	public static final class UnlockRef
	{
		public final String name;
		public final int itemId;
		public final List<SkillReq> optionalSkills;

		public UnlockRef(String name, int itemId, List<SkillReq> optionalSkills)
		{
			this.name = name;
			this.itemId = itemId;
			this.optionalSkills = Collections.unmodifiableList(optionalSkills);
		}
	}

	/** An item requirement (e.g. specific gear/key/scroll to enter fight). */
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

	/** An account metric requirement (e.g. combat level threshold). */
	public static final class AccountReq
	{
		public final String metricName; // AccountMetric enum name
		public final int target;

		public AccountReq(String metricName, int target)
		{
			this.metricName = metricName;
			this.target = target;
		}
	}

	/** A boss-kill prerequisite (e.g. base DT2 kill before awakened). */
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

	/**
	 * An alternative path within a boss prereq set. Multiple alternatives
	 * form an OR-group: ANY one being satisfied unlocks the boss.
	 */
	public static final class Alternative
	{
		public final String label;
		public final List<SkillReq> skills;
		public final List<AccountReq> accounts;
		public final List<BossReq> bosses;
		public final List<net.runelite.api.Quest> quests;

		public Alternative(String label, List<SkillReq> skills,
			List<AccountReq> accounts)
		{
			this(label, skills, accounts, Collections.emptyList(), Collections.emptyList());
		}

		public Alternative(String label, List<SkillReq> skills,
			List<AccountReq> accounts, List<BossReq> bosses)
		{
			this(label, skills, accounts, bosses, Collections.emptyList());
		}

		public Alternative(String label, List<SkillReq> skills,
			List<AccountReq> accounts, List<BossReq> bosses,
			List<net.runelite.api.Quest> quests)
		{
			this.label = label;
			this.skills = Collections.unmodifiableList(skills);
			this.accounts = Collections.unmodifiableList(accounts);
			this.bosses = Collections.unmodifiableList(bosses);
			this.quests = Collections.unmodifiableList(quests);
		}
	}

	private static final Map<String, BossPrereqs> BOSS_PREREQS = new HashMap<>();

	static
	{
		// ====================================================
		// GWD
		// ====================================================
		BOSS_PREREQS.put("Kree'arra", new BossPrereqs(
			List.of(new SkillReq(Skill.RANGED, 70)),
			List.of(new UnlockRef("Mith Grapple", ItemID.MITH_GRAPPLE,
				List.of(new SkillReq(Skill.FLETCHING, 59), new SkillReq(Skill.SMITHING, 59))))));
		BOSS_PREREQS.put("General Graardor", new BossPrereqs(
			List.of(new SkillReq(Skill.STRENGTH, 70))));
		BOSS_PREREQS.put("Commander Zilyana", new BossPrereqs(
			List.of(new SkillReq(Skill.AGILITY, 70))));
		BOSS_PREREQS.put("K'ril Tsutsaroth", new BossPrereqs(
			List.of(new SkillReq(Skill.HITPOINTS, 70))));
		// Nex: The Frozen Door requires at least 1 kill in each GWD room
		// (the 4 Frozen key pieces drop from any kill in their respective
		// room; 1 KC per boss gates the attempt).
		BOSS_PREREQS.put("Nex", new BossPrereqs(
			List.of(), List.of(), List.of(),
			List.of(
				new BossReq("Kree'arra", 1),
				new BossReq("General Graardor", 1),
				new BossReq("Commander Zilyana", 1),
				new BossReq("K'ril Tsutsaroth", 1))));

		// ====================================================
		// Slayer bosses (slayer-level gated)
		// ====================================================
		BOSS_PREREQS.put("Abyssal Sire", new BossPrereqs(
			List.of(new SkillReq(Skill.SLAYER, 85)),
			List.of(),
			List.of(net.runelite.api.Quest.ENTER_THE_ABYSS)));
		BOSS_PREREQS.put("Cerberus", new BossPrereqs(
			List.of(new SkillReq(Skill.SLAYER, 91))));
		BOSS_PREREQS.put("Kraken", new BossPrereqs(
			List.of(new SkillReq(Skill.SLAYER, 87))));
		BOSS_PREREQS.put("Grotesque Guardians", new BossPrereqs(
			List.of(new SkillReq(Skill.SLAYER, 75)),
			List.of(),
			List.of(),
			List.of(),
			List.of(new ItemReq(ItemID.BRITTLE_KEY, "Brittle key", 1))));
		BOSS_PREREQS.put("Alchemical Hydra", new BossPrereqs(
			List.of(new SkillReq(Skill.SLAYER, 95))));
		BOSS_PREREQS.put("Araxxor", new BossPrereqs(
			List.of(new SkillReq(Skill.SLAYER, 92))));
		BOSS_PREREQS.put("Thermy", new BossPrereqs(
			List.of(new SkillReq(Skill.SLAYER, 93))));

		// ====================================================
		// Classic / other bosses (quest and item gated)
		// ====================================================
		BOSS_PREREQS.put("Zulrah", new BossPrereqs(List.of(), List.of(),
			List.of(net.runelite.api.Quest.REGICIDE)));
		BOSS_PREREQS.put("Vorkath", new BossPrereqs(List.of(), List.of(),
			List.of(net.runelite.api.Quest.DRAGON_SLAYER_II)));
		BOSS_PREREQS.put("Phantom Muspah", new BossPrereqs(List.of(), List.of(),
			List.of(net.runelite.api.Quest.SECRETS_OF_THE_NORTH)));
		BOSS_PREREQS.put("Deranged Arch.", new BossPrereqs(List.of(), List.of(),
			List.of(net.runelite.api.Quest.BONE_VOYAGE)));
		BOSS_PREREQS.put("Skotizo", new BossPrereqs(
			List.of(), List.of(), List.of(), List.of(),
			List.of(new ItemReq(ItemID.DARK_TOTEM, "Dark totem", 1))));
		BOSS_PREREQS.put("Obor", new BossPrereqs(
			List.of(), List.of(), List.of(), List.of(),
			List.of(new ItemReq(ItemID.GIANT_KEY, "Giant key", 1))));
		BOSS_PREREQS.put("Bryophyta", new BossPrereqs(
			List.of(), List.of(), List.of(), List.of(),
			List.of(new ItemReq(ItemID.BRYOPHYTAS_ESSENCE, "Bryophyta's essence", 1))));
		BOSS_PREREQS.put("Hespori", new BossPrereqs(
			List.of(new SkillReq(Skill.FARMING, 65)),
			List.of(), List.of(), List.of(),
			List.of(new ItemReq(ItemID.HESPORI_SEED, "Hespori seed", 1))));

		// ====================================================
		// Desert Treasure II base bosses
		// ====================================================
		BOSS_PREREQS.put("Duke Sucellus", new BossPrereqs(List.of(), List.of(),
			List.of(net.runelite.api.Quest.DESERT_TREASURE_II__THE_FALLEN_EMPIRE)));
		BOSS_PREREQS.put("The Leviathan", new BossPrereqs(List.of(), List.of(),
			List.of(net.runelite.api.Quest.DESERT_TREASURE_II__THE_FALLEN_EMPIRE)));
		BOSS_PREREQS.put("The Whisperer", new BossPrereqs(List.of(), List.of(),
			List.of(net.runelite.api.Quest.DESERT_TREASURE_II__THE_FALLEN_EMPIRE)));
		BOSS_PREREQS.put("Vardorvis", new BossPrereqs(List.of(), List.of(),
			List.of(net.runelite.api.Quest.DESERT_TREASURE_II__THE_FALLEN_EMPIRE)));

		// ====================================================
		// DT2 awakened variants (base kill + Awakener's orb)
		// ====================================================
		BOSS_PREREQS.put("Duke (Awake)", new BossPrereqs(
			List.of(), List.of(),
			List.of(net.runelite.api.Quest.DESERT_TREASURE_II__THE_FALLEN_EMPIRE),
			List.of(new BossReq("Duke Sucellus", 1)),
			List.of(new ItemReq(ItemID.AWAKENERS_ORB, "Awakener's orb", 1))));
		BOSS_PREREQS.put("Leviathan (Awake)", new BossPrereqs(
			List.of(), List.of(),
			List.of(net.runelite.api.Quest.DESERT_TREASURE_II__THE_FALLEN_EMPIRE),
			List.of(new BossReq("The Leviathan", 1)),
			List.of(new ItemReq(ItemID.AWAKENERS_ORB, "Awakener's orb", 1))));
		BOSS_PREREQS.put("Whisperer (Awake)", new BossPrereqs(
			List.of(), List.of(),
			List.of(net.runelite.api.Quest.DESERT_TREASURE_II__THE_FALLEN_EMPIRE),
			List.of(new BossReq("The Whisperer", 1)),
			List.of(new ItemReq(ItemID.AWAKENERS_ORB, "Awakener's orb", 1))));
		BOSS_PREREQS.put("Vardorvis (Awake)", new BossPrereqs(
			List.of(), List.of(),
			List.of(net.runelite.api.Quest.DESERT_TREASURE_II__THE_FALLEN_EMPIRE),
			List.of(new BossReq("Vardorvis", 1)),
			List.of(new ItemReq(ItemID.AWAKENERS_ORB, "Awakener's orb", 1))));

		// ====================================================
		// Skilling / minigame bosses
		// ====================================================
		BOSS_PREREQS.put("Tempoross", new BossPrereqs(
			List.of(new SkillReq(Skill.FISHING, 35))));
		BOSS_PREREQS.put("Wintertodt", new BossPrereqs(
			List.of(new SkillReq(Skill.FIREMAKING, 50))));
		BOSS_PREREQS.put("GotR", new BossPrereqs(
			List.of(new SkillReq(Skill.RUNECRAFT, 27))));
		BOSS_PREREQS.put("Zalcano", new BossPrereqs(
			List.of(
				new SkillReq(Skill.MINING, 70),
				new SkillReq(Skill.SMITHING, 70),
				new SkillReq(Skill.RUNECRAFT, 70)),
			List.of(),
			List.of(net.runelite.api.Quest.SONG_OF_THE_ELVES)));

		// ====================================================
		// Nightmare — Morytania access via Priest in Peril.
		// ====================================================
		BOSS_PREREQS.put("The Nightmare", new BossPrereqs(List.of(), List.of(),
			List.of(net.runelite.api.Quest.PRIEST_IN_PERIL)));
		BOSS_PREREQS.put("Phosani's Nightmare", new BossPrereqs(
			List.of(), List.of(),
			List.of(net.runelite.api.Quest.PRIEST_IN_PERIL),
			List.of(new BossReq("The Nightmare", 1))));

		// ====================================================
		// Waves — Inferno requires completing the Fight Caves first
		// (TzTok-Jad kill, which grants the Fire cape entry ticket).
		// ====================================================
		BOSS_PREREQS.put("TzKal-Zuk", new BossPrereqs(
			List.of(), List.of(), List.of(),
			List.of(new BossReq("TzTok-Jad", 1))));

		// ====================================================
		// Raids
		// ====================================================
		// CoX Challenge Mode: requires a completed CoX raid to start.
		BOSS_PREREQS.put("CoX (CM)", new BossPrereqs(
			List.of(), List.of(), List.of(),
			List.of(new BossReq("CoX", 1))));
		// Theatre of Blood: A Taste of Hope for Ver Sinhaza access.
		BOSS_PREREQS.put("ToB", new BossPrereqs(List.of(), List.of(),
			List.of(net.runelite.api.Quest.A_TASTE_OF_HOPE)));
		BOSS_PREREQS.put("ToB (Story)", new BossPrereqs(List.of(), List.of(),
			List.of(net.runelite.api.Quest.A_TASTE_OF_HOPE)));
		// ToB Hard Mode: requires a completed standard ToB.
		BOSS_PREREQS.put("ToB (HM)", new BossPrereqs(
			List.of(), List.of(),
			List.of(net.runelite.api.Quest.A_TASTE_OF_HOPE),
			List.of(new BossReq("ToB", 1))));
		// Tombs of Amascut: Beneath Cursed Sands for access to Het's Oasis.
		BOSS_PREREQS.put("ToA", new BossPrereqs(List.of(), List.of(),
			List.of(net.runelite.api.Quest.BENEATH_CURSED_SANDS)));
		BOSS_PREREQS.put("ToA (Entry)", new BossPrereqs(List.of(), List.of(),
			List.of(net.runelite.api.Quest.BENEATH_CURSED_SANDS)));
		BOSS_PREREQS.put("ToA (Expert)", new BossPrereqs(List.of(), List.of(),
			List.of(net.runelite.api.Quest.BENEATH_CURSED_SANDS)));

		// ====================================================
		// Varlamore content (quest-gated)
		// ====================================================
		BOSS_PREREQS.put("Amoxliatl", new BossPrereqs(List.of(), List.of(),
			List.of(net.runelite.api.Quest.THE_HEART_OF_DARKNESS)));
		BOSS_PREREQS.put("Yama", new BossPrereqs(List.of(), List.of(),
			List.of(net.runelite.api.Quest.A_KINGDOM_DIVIDED)));
		BOSS_PREREQS.put("Fortis Colosseum (Waves)", new BossPrereqs(List.of(), List.of(),
			List.of(net.runelite.api.Quest.CHILDREN_OF_THE_SUN)));
		BOSS_PREREQS.put("Shellbane Gryphon", new BossPrereqs(List.of(), List.of(),
			List.of(net.runelite.api.Quest.TROUBLED_TORTUGANS)));

		// ====================================================
		// Perilous Moons (Varlamore quest + 3 moons + chest aggregate)
		// ====================================================
		BOSS_PREREQS.put("Blue Moon", new BossPrereqs(List.of(), List.of(),
			List.of(net.runelite.api.Quest.PERILOUS_MOONS)));
		BOSS_PREREQS.put("Blood Moon", new BossPrereqs(List.of(), List.of(),
			List.of(net.runelite.api.Quest.PERILOUS_MOONS)));
		BOSS_PREREQS.put("Eclipse Moon", new BossPrereqs(List.of(), List.of(),
			List.of(net.runelite.api.Quest.PERILOUS_MOONS)));
		BOSS_PREREQS.put("Perilous Moons Chests", new BossPrereqs(List.of(), List.of(),
			List.of(net.runelite.api.Quest.PERILOUS_MOONS)));

		// ====================================================
		// Zemouregal's Fort — Brutus + Demonic Brutus (HM)
		// Access via Defender of Varrock quest.
		// ====================================================
		BOSS_PREREQS.put("Brutus", new BossPrereqs(List.of(), List.of(),
			List.of(net.runelite.api.Quest.DEFENDER_OF_VARROCK)));
		BOSS_PREREQS.put("Demonic Brutus", new BossPrereqs(
			List.of(), List.of(),
			List.of(net.runelite.api.Quest.DEFENDER_OF_VARROCK),
			List.of(new BossReq("Brutus", 1))));

		// ====================================================
		// The Gauntlet + Corrupted (via Song of the Elves / Prifddinas)
		// ====================================================
		BOSS_PREREQS.put("The Gauntlet", new BossPrereqs(List.of(), List.of(),
			List.of(net.runelite.api.Quest.SONG_OF_THE_ELVES)));
		BOSS_PREREQS.put("Corrupted Gauntlet", new BossPrereqs(
			List.of(), List.of(),
			List.of(net.runelite.api.Quest.SONG_OF_THE_ELVES),
			List.of(new BossReq("The Gauntlet", 1))));

		// ====================================================
		// Doom of Mokhaiotl — delve-style activity gated by
		// "The Final Dawn" (Varlamore Pt.2 endpoint). Higher levels
		// self-gate via the delve mechanic (varp authoritative), so
		// no cross-level BossReq.
		// ====================================================
		for (String domLevel : List.of(
			"Doom of Mokhaiotl (L1)", "Doom of Mokhaiotl (L2)",
			"Doom of Mokhaiotl (L3)", "Doom of Mokhaiotl (L4)",
			"Doom of Mokhaiotl (L5)", "Doom of Mokhaiotl (L6)",
			"Doom of Mokhaiotl (L7)", "Doom of Mokhaiotl (L8)",
			"Doom of Mokhaiotl (L8+)"))
		{
			BOSS_PREREQS.put(domLevel, new BossPrereqs(List.of(), List.of(),
				List.of(net.runelite.api.Quest.THE_FINAL_DAWN)));
		}

		// ====================================================
		// Bosses intentionally left without prereqs (getPrereqs
		// returns null = "no hard requirement to fight this boss"):
		// - Wilderness: Artio, Callisto, Calvar'ion, Chaos Elemental,
		//   Chaos Fanatic, Crazy Archaeologist, Scorpia, Spindel,
		//   Venenatis, Vet'ion (wilderness boss access has no gate
		//   beyond entering the wilderness)
		// - Low-barrier: Giant Mole, Kalphite Queen, King Black Dragon,
		//   Mimic, Sarachnis, Barrows (no gating skill/quest/item)
		// - Dagannoth Kings: Waterbirth Island reachable via fairy ring
		//   (AJR), Lunar teleport, or Jarvald's ferry — no single hard
		//   gate
		// - Newest content with no hard prereq: Hueycoatl, Royal Titans,
		//   Scurrius, Sol Heredit
		// - Waves/Raids with no quest: TzTok-Jad, CoX
		// - Corporeal Beast requires Summer's End quest, but that
		//   enum value is not exposed in net.runelite.api.Quest
		//   (follow-up: wrap as a custom Unlock when Quest enum adds it).
		// ====================================================
	}

	/**
	 * Get prerequisites for a boss. Returns null if no prereqs defined.
	 */
	public static BossPrereqs getPrereqs(String bossName)
	{
		return BOSS_PREREQS.get(bossName);
	}

	/**
	 * Resolve a collection log entry name to matching boss data names.
	 * Returns a list of boss names (one per tier/variant) if the name
	 * maps to multiple entries, or a single-element list for exact
	 * matches. Returns empty if unknown.
	 */
	public static java.util.List<String> resolveCollectionLogName(String name)
	{
		if (name == null) return java.util.Collections.emptyList();
		java.util.List<String> aliases = COLLECTION_LOG_ALIASES.get(name);
		if (aliases != null) return aliases;
		if (BOSSES.containsKey(name)) return java.util.List.of(name);
		return java.util.Collections.emptyList();
	}

	/**
	 * All boss names in alphabetical order for the dropdown.
	 */
	public static String[] getBossNames()
	{
		String[] names = BOSSES.keySet().toArray(new String[0]);
		java.util.Arrays.sort(names);
		return names;
	}

	/**
	 * Look up the VarPlayerID for a boss. Returns -1 if unknown.
	 */
	public static int getVarpId(String bossName)
	{
		Integer id = BOSSES.get(bossName);
		return id != null ? id : -1;
	}

	/**
	 * Check if a boss name is known.
	 */
	public static boolean isKnownBoss(String bossName)
	{
		return BOSSES.containsKey(bossName);
	}

	/**
	 * Get the pet item ID for a boss (used as the goal card icon).
	 * Returns 0 if no pet mapping exists.
	 */
	public static int getPetItemId(String bossName)
	{
		Integer id = PET_ICONS.get(bossName);
		return id != null ? id : 0;
	}

	private BossKillData() {}
}
