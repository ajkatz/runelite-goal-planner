package com.goaltracker.data;

import java.awt.Color;

/**
 * Metadata for combat achievement tiers and widget IDs.
 *
 * Widget IDs come from net.runelite.api.gameval.InterfaceID.CaTasks, inlined here
 * to avoid a hard dependency on the gameval package.
 *
 * Tier sprite IDs come from SpriteID.CaTierSwordsSmall (_0.._5 = 3399..3404),
 * ordered Easy, Medium, Hard, Elite, Master, Grandmaster.
 */
public final class CombatAchievementData
{
	private CombatAchievementData() {}

	// InterfaceID.CaTasks constants — CA_TASKS group = 715
	public static final int GROUP_ID = 715;
	public static final int TASKS_BACKGROUND = 46858249; // clicked row layer (param1)
	public static final int TASKS_NAME = 46858250;
	public static final int TASKS_MONSTER = 46858252;
	public static final int TASKS_TIER = 46858255;

	// Tier sprite range
	private static final int TIER_SPRITE_BASE = 3399;

	public enum Tier
	{
		EASY("Easy", "E", new Color(76, 175, 80)),               // Green
		MEDIUM("Medium", "M", new Color(66, 133, 244)),          // Blue
		HARD("Hard", "H", new Color(156, 39, 176)),              // Purple
		ELITE("Elite", "E", new Color(255, 152, 0)),             // Orange
		MASTER("Master", "Ma", new Color(220, 60, 60)),          // Red
		GRANDMASTER("Grandmaster", "GM", new Color(255, 193, 7)); // Gold

		private final String displayName;
		private final String shortLabel;
		private final Color color;

		Tier(String displayName, String shortLabel, Color color)
		{
			this.displayName = displayName;
			this.shortLabel = shortLabel;
			this.color = color;
		}

		public String getDisplayName()
		{
			return displayName;
		}

		public String getShortLabel()
		{
			return shortLabel;
		}

		public Color getColor()
		{
			return color;
		}
	}

	/**
	 * Raid monster prefixes as returned by TASKS_MONSTER. The widget uses raid-level
	 * names (sometimes with a mode suffix like ": CM", ": Entry Mode", ": Hard Mode",
	 * ": Expert Mode"), so we match by prefix to cover all variants.
	 */
	private static final String[] RAID_PREFIXES = {
		"Chambers of Xeric",
		"Theatre of Blood",
		"Tombs of Amascut"
	};

	/**
	 * True if the given monster string (as parsed from the CA widget) belongs to a raid.
	 */
	public static boolean isRaidBoss(String monster)
	{
		if (monster == null) return false;
		for (String prefix : RAID_PREFIXES)
		{
			if (monster.startsWith(prefix))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Abbreviate a raid monster string for tag display.
	 * Examples:
	 *   "Chambers of Xeric"          -> "CoX"
	 *   "Chambers of Xeric: CM"      -> "CoX: CM"
	 *   "Theatre of Blood"           -> "ToB"
	 *   "Theatre of Blood: Entry Mode" -> "ToB: Entry"
	 *   "Theatre of Blood: Hard Mode"  -> "ToB: Hard"
	 *   "Tombs of Amascut"           -> "ToA"
	 *   "Tombs of Amascut: Entry Mode"  -> "ToA: Entry"
	 *   "Tombs of Amascut: Expert Mode" -> "ToA: Expert"
	 * Returns the input unchanged if not a known raid.
	 */
	public static String abbreviateRaid(String monster)
	{
		if (monster == null) return null;
		String abbrev;
		String suffix;
		if (monster.startsWith("Chambers of Xeric"))
		{
			abbrev = "CoX";
			suffix = monster.substring("Chambers of Xeric".length());
		}
		else if (monster.startsWith("Theatre of Blood"))
		{
			abbrev = "ToB";
			suffix = monster.substring("Theatre of Blood".length());
		}
		else if (monster.startsWith("Tombs of Amascut"))
		{
			abbrev = "ToA";
			suffix = monster.substring("Tombs of Amascut".length());
		}
		else
		{
			return monster;
		}
		// suffix is either "" or ": <mode>". Strip " Mode" if present.
		if (suffix.isEmpty())
		{
			return abbrev;
		}
		String trimmed = suffix.replace(" Mode", "");
		return abbrev + trimmed;
	}

	/**
	 * Resolve a CaTierSwordsSmall sprite ID to its tier, or null if out of range.
	 */
	public static Tier tierFromSpriteId(int spriteId)
	{
		int idx = spriteId - TIER_SPRITE_BASE;
		Tier[] tiers = Tier.values();
		if (idx < 0 || idx >= tiers.length)
		{
			return null;
		}
		return tiers[idx];
	}

	/**
	 * Strip "Monster: " prefix and all &lt;col=...&gt; tags from a monster field.
	 * Input:  "Monster: &lt;col=ffffff&gt;Aberrant Spectre&lt;/col&gt;"
	 * Output: "Aberrant Spectre"
	 * Returns null if input is null or result is empty.
	 */
	public static String parseMonsterName(String raw)
	{
		if (raw == null)
		{
			return null;
		}
		String stripped = raw.replaceFirst("^Monster:\\s*", "")
			.replaceAll("<col=[^>]*>", "")
			.replaceAll("</col>", "")
			.trim();
		return stripped.isEmpty() ? null : stripped;
	}
}
