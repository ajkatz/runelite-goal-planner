package com.goalplanner.data;

import net.runelite.api.gameval.VarbitID;

import java.util.HashMap;
import java.util.Map;

/**
 * Metadata for achievement diaries: widget IDs, sprite icon, and the
 * (area, tier) → completion-tracking lookup used by DiaryTracker.
 *
 * Widget IDs come from {@code InterfaceID.AreaTask} (group 259).
 *
 * <p>Two tracking modes:
 * <ul>
 *   <li><b>Boolean COMPLETE varbit</b> (most diaries): named
 *       {@code <AREA>_DIARY_<TIER>_COMPLETE} in {@link VarbitID}; flips
 *       0→1 when the tier is finished. Goal stores {@code targetValue=1}.</li>
 *   <li><b>Count varbit + task total</b> (Karamja Easy/Medium/Hard only):
 *       runelite-api doesn't expose boolean completes for these three tiers,
 *       only {@code KARAMJA_*_COUNT} task counters. Completion is detected
 *       by comparing the count against the tier's total task count, stored
 *       on the goal as {@code targetValue=<taskCount>}.</li>
 * </ul>
 */
public final class AchievementDiaryData
{
	private AchievementDiaryData() {}

	// InterfaceID.AreaTask constants — group 259
	public static final int GROUP_ID = 259;
	public static final int TASKBOX = 16973826; // clicked row layer (param1 on right-click)

	// Sprite icon: SpriteID.AchievementDiaryIcons.GREEN_ACHIEVEMENT_DIARIES
	public static final int DIARY_SPRITE_ID = 836;

	public enum Tier
	{
		EASY("Easy"),
		MEDIUM("Medium"),
		HARD("Hard"),
		ELITE("Elite");

		private final String displayName;

		Tier(String displayName) { this.displayName = displayName; }

		public String getDisplayName() { return displayName; }
	}

	/**
	 * Tracking spec for one (area, tier): which varbit to read and what
	 * value indicates completion. {@code requiredValue == 1} for boolean
	 * COMPLETE varbits; for Karamja Easy/Medium/Hard it's the tier's total
	 * task count compared against the {@code KARAMJA_*_COUNT} varbit.
	 */
	public static final class Tracking
	{
		public final int varbitId;
		public final int requiredValue;

		public Tracking(int varbitId, int requiredValue)
		{
			this.varbitId = varbitId;
			this.requiredValue = requiredValue;
		}
	}

	/**
	 * Map of "<AREA>|<TIER>" (uppercase, pipe-separated) to the boolean
	 * completion varbit ID. Karamja Easy/Medium/Hard are absent — those
	 * use {@link #KARAMJA_COUNT_VARBITS} instead.
	 */
	private static final Map<String, Integer> COMPLETION_VARBITS = new HashMap<>();

	/**
	 * Karamja Easy/Medium/Hard: count varbit ID and total task count. The
	 * tier is complete when {@code varbit value >= taskCount}.
	 */
	private static final Map<Tier, Tracking> KARAMJA_COUNT_VARBITS = new HashMap<>();
	static
	{
		KARAMJA_COUNT_VARBITS.put(Tier.EASY,   new Tracking(VarbitID.KARAMJA_EASY_COUNT, 10));
		KARAMJA_COUNT_VARBITS.put(Tier.MEDIUM, new Tracking(VarbitID.KARAMJA_MED_COUNT,  19));
		KARAMJA_COUNT_VARBITS.put(Tier.HARD,   new Tracking(VarbitID.KARAMJA_HARD_COUNT, 10));
	}

	static
	{
		put("ARDOUGNE", Tier.EASY,   VarbitID.ARDOUGNE_DIARY_EASY_COMPLETE);
		put("ARDOUGNE", Tier.MEDIUM, VarbitID.ARDOUGNE_DIARY_MEDIUM_COMPLETE);
		put("ARDOUGNE", Tier.HARD,   VarbitID.ARDOUGNE_DIARY_HARD_COMPLETE);
		put("ARDOUGNE", Tier.ELITE,  VarbitID.ARDOUGNE_DIARY_ELITE_COMPLETE);

		put("DESERT", Tier.EASY,   VarbitID.DESERT_DIARY_EASY_COMPLETE);
		put("DESERT", Tier.MEDIUM, VarbitID.DESERT_DIARY_MEDIUM_COMPLETE);
		put("DESERT", Tier.HARD,   VarbitID.DESERT_DIARY_HARD_COMPLETE);
		put("DESERT", Tier.ELITE,  VarbitID.DESERT_DIARY_ELITE_COMPLETE);

		put("FALADOR", Tier.EASY,   VarbitID.FALADOR_DIARY_EASY_COMPLETE);
		put("FALADOR", Tier.MEDIUM, VarbitID.FALADOR_DIARY_MEDIUM_COMPLETE);
		put("FALADOR", Tier.HARD,   VarbitID.FALADOR_DIARY_HARD_COMPLETE);
		put("FALADOR", Tier.ELITE,  VarbitID.FALADOR_DIARY_ELITE_COMPLETE);

		put("FREMENNIK", Tier.EASY,   VarbitID.FREMENNIK_DIARY_EASY_COMPLETE);
		put("FREMENNIK", Tier.MEDIUM, VarbitID.FREMENNIK_DIARY_MEDIUM_COMPLETE);
		put("FREMENNIK", Tier.HARD,   VarbitID.FREMENNIK_DIARY_HARD_COMPLETE);
		put("FREMENNIK", Tier.ELITE,  VarbitID.FREMENNIK_DIARY_ELITE_COMPLETE);

		put("KANDARIN", Tier.EASY,   VarbitID.KANDARIN_DIARY_EASY_COMPLETE);
		put("KANDARIN", Tier.MEDIUM, VarbitID.KANDARIN_DIARY_MEDIUM_COMPLETE);
		put("KANDARIN", Tier.HARD,   VarbitID.KANDARIN_DIARY_HARD_COMPLETE);
		put("KANDARIN", Tier.ELITE,  VarbitID.KANDARIN_DIARY_ELITE_COMPLETE);

		// Karamja: only Elite has a standard *_DIARY_*_COMPLETE varbit in runelite-api.
		// Easy/Medium/Hard are tracked via KARAMJA_COUNT_VARBITS instead.
		put("KARAMJA", Tier.ELITE, VarbitID.KARAMJA_DIARY_ELITE_COMPLETE);

		put("KOUREND", Tier.EASY,   VarbitID.KOUREND_DIARY_EASY_COMPLETE);
		put("KOUREND", Tier.MEDIUM, VarbitID.KOUREND_DIARY_MEDIUM_COMPLETE);
		put("KOUREND", Tier.HARD,   VarbitID.KOUREND_DIARY_HARD_COMPLETE);
		put("KOUREND", Tier.ELITE,  VarbitID.KOUREND_DIARY_ELITE_COMPLETE);

		put("LUMBRIDGE", Tier.EASY,   VarbitID.LUMBRIDGE_DIARY_EASY_COMPLETE);
		put("LUMBRIDGE", Tier.MEDIUM, VarbitID.LUMBRIDGE_DIARY_MEDIUM_COMPLETE);
		put("LUMBRIDGE", Tier.HARD,   VarbitID.LUMBRIDGE_DIARY_HARD_COMPLETE);
		put("LUMBRIDGE", Tier.ELITE,  VarbitID.LUMBRIDGE_DIARY_ELITE_COMPLETE);

		put("MORYTANIA", Tier.EASY,   VarbitID.MORYTANIA_DIARY_EASY_COMPLETE);
		put("MORYTANIA", Tier.MEDIUM, VarbitID.MORYTANIA_DIARY_MEDIUM_COMPLETE);
		put("MORYTANIA", Tier.HARD,   VarbitID.MORYTANIA_DIARY_HARD_COMPLETE);
		put("MORYTANIA", Tier.ELITE,  VarbitID.MORYTANIA_DIARY_ELITE_COMPLETE);

		put("VARROCK", Tier.EASY,   VarbitID.VARROCK_DIARY_EASY_COMPLETE);
		put("VARROCK", Tier.MEDIUM, VarbitID.VARROCK_DIARY_MEDIUM_COMPLETE);
		put("VARROCK", Tier.HARD,   VarbitID.VARROCK_DIARY_HARD_COMPLETE);
		put("VARROCK", Tier.ELITE,  VarbitID.VARROCK_DIARY_ELITE_COMPLETE);

		put("WESTERN", Tier.EASY,   VarbitID.WESTERN_DIARY_EASY_COMPLETE);
		put("WESTERN", Tier.MEDIUM, VarbitID.WESTERN_DIARY_MEDIUM_COMPLETE);
		put("WESTERN", Tier.HARD,   VarbitID.WESTERN_DIARY_HARD_COMPLETE);
		put("WESTERN", Tier.ELITE,  VarbitID.WESTERN_DIARY_ELITE_COMPLETE);

		put("WILDERNESS", Tier.EASY,   VarbitID.WILDERNESS_DIARY_EASY_COMPLETE);
		put("WILDERNESS", Tier.MEDIUM, VarbitID.WILDERNESS_DIARY_MEDIUM_COMPLETE);
		put("WILDERNESS", Tier.HARD,   VarbitID.WILDERNESS_DIARY_HARD_COMPLETE);
		put("WILDERNESS", Tier.ELITE,  VarbitID.WILDERNESS_DIARY_ELITE_COMPLETE);
	}

	private static void put(String area, Tier tier, int varbitId)
	{
		COMPLETION_VARBITS.put(area + "|" + tier.name(), varbitId);
	}

	/**
	 * Look up the tracking spec (varbit + required value) for a given area
	 * and tier. Returns null when neither a boolean COMPLETE varbit nor a
	 * Karamja count varbit is available (in which case the goal is manual).
	 */
	public static Tracking tracking(String areaDisplayName, Tier tier)
	{
		if (areaDisplayName == null || tier == null) return null;
		String areaKey = normalizeAreaKey(areaDisplayName);
		Integer boolVarbit = COMPLETION_VARBITS.get(areaKey + "|" + tier.name());
		if (boolVarbit != null) return new Tracking(boolVarbit, 1);
		if ("KARAMJA".equals(areaKey)) return KARAMJA_COUNT_VARBITS.get(tier);
		return null;
	}

	/**
	 * Parse the tier out of a diary goal's description (e.g.
	 * "Medium Achievement Diary" → MEDIUM). Returns null if the description
	 * doesn't start with a recognized tier word. Used by the load-time
	 * backfill to recover tier info from existing stored goals.
	 */
	public static Tier parseTierFromDescription(String description)
	{
		if (description == null) return null;
		String trimmed = description.trim();
		int space = trimmed.indexOf(' ');
		String firstWord = (space < 0 ? trimmed : trimmed.substring(0, space)).toUpperCase();
		try { return Tier.valueOf(firstWord); }
		catch (IllegalArgumentException e) { return null; }
	}

	/**
	 * Normalize a display area name into the constant key used in the varbit map.
	 * Handles single-word names ("Desert" → "DESERT") and the few multi-word
	 * aliases used by the diary interface vs. the VarbitID naming.
	 */
	public static String normalizeAreaKey(String displayName)
	{
		if (displayName == null) return "";
		String trimmed = displayName.trim();
		// "Western Provinces" → "WESTERN"
		if (trimmed.equalsIgnoreCase("Western Provinces")) return "WESTERN";
		// "Kourend & Kebos" → "KOUREND"
		if (trimmed.toLowerCase().startsWith("kourend")) return "KOUREND";
		return trimmed.toUpperCase().replace(' ', '_');
	}

	/**
	 * Parse the area display name out of an AreaTask menu entry's option string.
	 * The option looks like "Open &lt;col=ff9040&gt;Desert Journal&lt;/col&gt;" or
	 * "Wiki &lt;col=ff9040&gt;Desert Journal&lt;/col&gt;".
	 *
	 * Returns the trimmed area name (e.g. "Desert") or null if the option doesn't match.
	 */
	public static String parseAreaFromOption(String rawOption)
	{
		if (rawOption == null) return null;
		String stripped = rawOption.replaceAll("<col=[^>]*>", "")
			.replaceAll("</col>", "")
			.trim();
		// Drop leading verb (Open / Wiki / any single word + space)
		int firstSpace = stripped.indexOf(' ');
		if (firstSpace < 0) return null;
		String afterVerb = stripped.substring(firstSpace + 1).trim();
		// Drop trailing " Journal"
		if (afterVerb.toLowerCase().endsWith(" journal"))
		{
			afterVerb = afterVerb.substring(0, afterVerb.length() - " journal".length()).trim();
		}
		return afterVerb.isEmpty() ? null : afterVerb;
	}
}
