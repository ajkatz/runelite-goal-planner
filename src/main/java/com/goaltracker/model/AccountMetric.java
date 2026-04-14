package com.goaltracker.model;

import java.awt.Color;

/**
 * Account-wide metrics that can be tracked as goals. Each metric
 * defines how to display the goal and what client API to read.
 * The actual client API reading is done by
 * {@link com.goaltracker.tracker.AccountTracker}.
 */
public enum AccountMetric
{
	QUEST_POINTS("Quest Points", new Color(65, 155, 222), 899, null, 1, 333),
	COMBAT_LEVEL("Combat Level", new Color(200, 60, 60), 168, null, 3, 126),
	TOTAL_LEVEL("Total Level", new Color(76, 175, 80), 898, null, 1, 2376),
	// MUSIC_TRACKS deferred — no reliable bulk API for counting unlocked tracks
	CA_POINTS("CA Points", new Color(139, 69, 19), 0, null, 1, 2630),
	SLAYER_POINTS("Slayer Points", new Color(0, 160, 160), 0, null, 1, 64000),
	KUDOS("Museum Kudos", new Color(200, 170, 50), 0, "item:11182", 1, 243),
	/** Combined Attack + Strength level (e.g. 130 for Warriors Guild entry). */
	ATT_STR_COMBINED("Att + Str", new Color(46, 125, 50), 0, "item:8850", 2, 198),
	/** Kingdom of Miscellania approval rating (0-127, where 127 = 100%). */
	MISC_APPROVAL("Misc. Approval", new Color(200, 170, 50), 0, null, 1, 127),
	/** Tears of Guthix personal best (max tears collected in a single game). */
	TOG_MAX_TEARS("Tears of Guthix PB", new Color(100, 180, 220), 0, null, 1, 300),
	/** Chompy bird kill count. */
	CHOMPY_KILLS("Chompy Kills", new Color(139, 69, 19), 0, "item:" + net.runelite.api.ItemID.RAW_CHOMPY, 1, 4000);

	private final String displayName;
	private final Color color;
	private final int spriteId;
	/** Item or bundled icon key (e.g. "item:11182"). Null = use spriteId. */
	private final String iconKey;
	private final int minTarget;
	private final int maxTarget;

	AccountMetric(String displayName, Color color, int spriteId, String iconKey,
		int minTarget, int maxTarget)
	{
		this.displayName = displayName;
		this.color = color;
		this.spriteId = spriteId;
		this.iconKey = iconKey;
		this.minTarget = minTarget;
		this.maxTarget = maxTarget;
	}

	public String getDisplayName() { return displayName; }
	public Color getColor() { return color; }
	public int getMinTarget() { return minTarget; }
	public int getMaxTarget() { return maxTarget; }

	/** RuneLite sprite ID for the goal card icon, or 0 if no sprite. */
	public int getSpriteId() { return spriteId; }

	/** Item or bundled icon key, or null to use spriteId instead. */
	public String getIconKey() { return iconKey; }

	/** Packed 0xRRGGBB for use as a Goal background color override. */
	public int getColorRgb()
	{
		return (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
	}

	/**
	 * Resolve the sprite ID for a CA Points goal based on the target value.
	 * Returns the appropriate tier sword sprite, or 0 for non-CA metrics.
	 */
	public int resolveSpriteForTarget(int target)
	{
		if (this != CA_POINTS) return spriteId;
		return caTierSprite(target);
	}

	/**
	 * Resolve the icon key for a goal based on the target value.
	 * CA_POINTS below Easy tier uses a bronze knife item icon.
	 */
	public String resolveIconKeyForTarget(int target)
	{
		if (this == CA_POINTS && target <= 40) return "item:864";
		return iconKey;
	}

	// --- CA tier constants ---

	/** CA tier sword sprites: Easy=3399 .. Grandmaster=3404. */
	private static final int CA_SPRITE_BASE = 3399;

	private static final String[] CA_TIER_LABELS = {
		"None", "Easy", "Med", "Hard", "Elite", "Master", "GM"
	};
	private static final int[] CA_TIER_THRESHOLDS = {
		0, 41, 161, 416, 1064, 1904, 2630
	};

	private static int caTierIndex(int points)
	{
		for (int i = CA_TIER_THRESHOLDS.length - 1; i >= 1; i--)
		{
			if (points >= CA_TIER_THRESHOLDS[i]) return i;
		}
		return 0;
	}

	private static int caTierSprite(int points)
	{
		int idx = caTierIndex(points);
		return idx >= 1 ? CA_SPRITE_BASE + (idx - 1) : 0;
	}

	/**
	 * Returns the CA tier label for a given point target
	 * (e.g. 41 → "Easy", 2630 → "Grandmaster", 40 → "None").
	 */
	public static String caTierLabel(int points)
	{
		return CA_TIER_LABELS[caTierIndex(points)];
	}

	/** CA tier shortcut labels for the UI. */
	public static final String[] CA_TIER_NAMES = {
		"Easy (41)", "Medium (161)", "Hard (416)",
		"Elite (1064)", "Master (1904)", "Grandmaster (2630)"
	};
	/** CA tier shortcut values matching CA_TIER_NAMES. */
	public static final int[] CA_TIER_VALUES = {
		41, 161, 416, 1064, 1904, 2630
	};
}
