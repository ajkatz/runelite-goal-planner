package com.goalplanner.data;

import net.runelite.api.gameval.VarbitID;

import java.util.HashMap;
import java.util.Map;

/**
 * Metadata for achievement diaries: widget IDs, sprite icon, and the
 * (area, tier) → completion varbit map used by DiaryTracker.
 *
 * Widget IDs come from {@code InterfaceID.AreaTask} (group 259).
 * Completion varbits are named {@code <AREA>_DIARY_<TIER>_COMPLETE} in
 * {@link VarbitID}. Karamja Easy/Medium/Hard are NOT exposed as completion
 * constants in runelite-api — only Elite is — so those three tiers are
 * left unmapped and tracked manually.
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
	 * Map of "<AREA>|<TIER>" (uppercase, pipe-separated) to the completion varbit ID.
	 * Not every combination is present — Karamja Easy/Medium/Hard are intentionally
	 * absent because runelite-api doesn't expose them as named constants.
	 */
	private static final Map<String, Integer> COMPLETION_VARBITS = new HashMap<>();

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
		// Easy/Medium/Hard fall through to manual tracking (goal builds fine, varbit lookup null).
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
	 * Look up the completion varbit for a given area (display name) and tier.
	 * Returns 0 when no completion varbit is exposed (e.g. Karamja Easy/Medium/Hard).
	 */
	public static int completionVarbit(String areaDisplayName, Tier tier)
	{
		if (areaDisplayName == null || tier == null) return 0;
		String key = normalizeAreaKey(areaDisplayName) + "|" + tier.name();
		Integer id = COMPLETION_VARBITS.get(key);
		return id != null ? id : 0;
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
