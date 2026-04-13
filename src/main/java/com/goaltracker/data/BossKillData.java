package com.goaltracker.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.runelite.api.ItemID;
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
		BOSSES.put("Thermonuclear Smoke Devil", VarPlayerID.TOTAL_THERMY_KILLS);
		BOSSES.put("Alchemical Hydra", VarPlayerID.TOTAL_HYDRABOSS_KILLS);

		// Other bosses
		BOSSES.put("Araxxor", VarPlayerID.TOTAL_ARAXXOR_KILLS);
		BOSSES.put("Bryophyta", VarPlayerID.TOTAL_BRYOPHYTA_KILLS);
		BOSSES.put("Corporeal Beast", VarPlayerID.TOTAL_CORP_KILLS);
		BOSSES.put("Deranged Archaeologist", VarPlayerID.TOTAL_DERANGEDARCHAEOLOGIST_KILLS);
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
		BOSSES.put("Duke Sucellus (Awakened)", VarPlayerID.TOTAL_DUKE_SUCELLUS_AWAKENED_KILLS);
		BOSSES.put("The Leviathan (Awakened)", VarPlayerID.TOTAL_LEVIATHAN_AWAKENED_KILLS);
		BOSSES.put("The Whisperer (Awakened)", VarPlayerID.TOTAL_WHISPERER_AWAKENED_KILLS);
		BOSSES.put("Vardorvis (Awakened)", VarPlayerID.TOTAL_VARDORVIS_AWAKENED_KILLS);

		// Raids
		BOSSES.put("Chambers of Xeric", VarPlayerID.TOTAL_COMPLETED_XERICCHAMBERS);
		BOSSES.put("Chambers of Xeric (CM)", VarPlayerID.TOTAL_COMPLETED_XERICCHAMBERS_CHALLENGE);
		BOSSES.put("Theatre of Blood", VarPlayerID.TOTAL_COMPLETED_THEATREOFBLOOD);
		BOSSES.put("Theatre of Blood (HM)", VarPlayerID.TOTAL_COMPLETED_THEATREOFBLOOD_HARD);
		BOSSES.put("Theatre of Blood (Story)", VarPlayerID.TOTAL_COMPLETED_THEATREOFBLOOD_STORY);
		BOSSES.put("Tombs of Amascut (Entry)", VarPlayerID.TOTAL_COMPLETED_TOMBSOFAMASCUT_ENTRY);
		BOSSES.put("Tombs of Amascut", VarPlayerID.TOTAL_COMPLETED_TOMBSOFAMASCUT);
		BOSSES.put("Tombs of Amascut (Expert)", VarPlayerID.TOTAL_COMPLETED_TOMBSOFAMASCUT_EXPERT);

		// Waves
		BOSSES.put("TzTok-Jad", VarPlayerID.TOTAL_JAD_KILLS);
		BOSSES.put("TzKal-Zuk", VarPlayerID.TOTAL_ZUK_KILLS);
		BOSSES.put("Sol Heredit", VarPlayerID.TOTAL_SOL_KILLS);

		// Minigame bosses
		BOSSES.put("The Nightmare", VarPlayerID.TOTAL_NIGHTMARE_KILLS);
		BOSSES.put("Phosani's Nightmare", VarPlayerID.TOTAL_NIGHTMARE_CHALLENGE_KILLS);
		BOSSES.put("Tempoross", VarPlayerID.TOTAL_TEMPOROSS_KILLS);
		BOSSES.put("Wintertodt", VarPlayerID.TOTAL_WINTERTODT_KILLS);
		BOSSES.put("Guardians of the Rift", VarPlayerID.TOTAL_GOTR_KILLS);

		// Newer bosses
		BOSSES.put("Amoxliatl", VarPlayerID.TOTAL_AMOXLIATL_KILLS);
		BOSSES.put("Hueycoatl", VarPlayerID.TOTAL_HUEY_KILLS);
		BOSSES.put("Royal Titans", VarPlayerID.TOTAL_ROYAL_TITAN_KILLS);
		BOSSES.put("Yama", VarPlayerID.TOTAL_YAMA_KILLS);
		BOSSES.put("The Scurrius", VarPlayerID.TOTAL_RAT_BOSS_KILLS);
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
		PET_ICONS.put("Scorpia", ItemID.SCORPIAS_OFFSPRING);
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
		PET_ICONS.put("Thermonuclear Smoke Devil", ItemID.PET_SMOKE_DEVIL);
		PET_ICONS.put("Alchemical Hydra", ItemID.IKKLE_HYDRA);

		// Other bosses
		PET_ICONS.put("Araxxor", ItemID.SMOL_HEREDIT); // closest available
		PET_ICONS.put("Corporeal Beast", ItemID.PET_DARK_CORE);
		PET_ICONS.put("Giant Mole", ItemID.BABY_MOLE);
		PET_ICONS.put("Kalphite Queen", ItemID.KALPHITE_PRINCESS);
		PET_ICONS.put("King Black Dragon", ItemID.PRINCE_BLACK_DRAGON);
		PET_ICONS.put("Phantom Muspah", ItemID.MUPHIN);
		PET_ICONS.put("Sarachnis", ItemID.SRARACHA);
		PET_ICONS.put("Skotos", ItemID.SKOTOS); // Skotizo pet = Skotos
		PET_ICONS.put("Vorkath", ItemID.VORKI);
		PET_ICONS.put("Zulrah", ItemID.PET_SNAKELING);
		PET_ICONS.put("Scurrius", ItemID.SCURRY); // not "The Scurrius"

		// DT2
		PET_ICONS.put("Duke Sucellus", ItemID.BARON);
		PET_ICONS.put("The Leviathan", ItemID.LIL_CREATOR);
		PET_ICONS.put("The Whisperer", ItemID.WISP);
		PET_ICONS.put("Vardorvis", ItemID.BUTCH);
		PET_ICONS.put("Duke Sucellus (Awakened)", ItemID.BARON);
		PET_ICONS.put("The Leviathan (Awakened)", ItemID.LIL_CREATOR);
		PET_ICONS.put("The Whisperer (Awakened)", ItemID.WISP);
		PET_ICONS.put("Vardorvis (Awakened)", ItemID.BUTCH);

		// Raids
		PET_ICONS.put("Chambers of Xeric", ItemID.OLMLET);
		PET_ICONS.put("Chambers of Xeric (CM)", ItemID.OLMLET);
		PET_ICONS.put("Theatre of Blood", ItemID.LIL_ZIK);
		PET_ICONS.put("Theatre of Blood (HM)", ItemID.LIL_ZIK);
		PET_ICONS.put("Theatre of Blood (Story)", ItemID.LIL_ZIK);
		PET_ICONS.put("Tombs of Amascut (Entry)", ItemID.TUMEKENS_GUARDIAN);
		PET_ICONS.put("Tombs of Amascut", ItemID.TUMEKENS_GUARDIAN);
		PET_ICONS.put("Tombs of Amascut (Expert)", ItemID.TUMEKENS_GUARDIAN);

		// Waves
		PET_ICONS.put("TzTok-Jad", ItemID.TZREKJAD);
		PET_ICONS.put("TzKal-Zuk", ItemID.TZREKZUK);
		PET_ICONS.put("Sol Heredit", ItemID.SMOL_HEREDIT);

		// Minigame bosses
		PET_ICONS.put("The Nightmare", ItemID.LITTLE_NIGHTMARE);
		PET_ICONS.put("Phosani's Nightmare", ItemID.LITTLE_NIGHTMARE);
		PET_ICONS.put("Wintertodt", ItemID.PHOENIX);
		PET_ICONS.put("Tempoross", ItemID.TINY_TEMPOR);
		PET_ICONS.put("Zalcano", ItemID.SMOLCANO);

		// Newer
		PET_ICONS.put("Amoxliatl", ItemID.MOXI);
		PET_ICONS.put("The Scurrius", ItemID.SCURRY);
		PET_ICONS.put("Gryphon", ItemID.GRYPHON);
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
