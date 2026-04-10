package com.goaltracker.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Public DTO for a goal — the canonical "render-friendly snapshot" returned by
 * {@link GoalTrackerApi#queryAllGoals()}.
 *
 * <p>Core fields are type-agnostic. Type-specific extras live in {@link #attributes}
 * with documented per-type schemas:
 *
 * <ul>
 *   <li><b>SKILL</b>: {@code skillName} (String, net.runelite.api.Skill enum name)</li>
 *   <li><b>QUEST</b>: {@code questName} (String, net.runelite.api.Quest enum name),
 *       {@code tooltip} (String, optional)</li>
 *   <li><b>DIARY</b>: {@code area} (String), {@code tier} (String — "EASY"/"MEDIUM"/"HARD"/"ELITE"),
 *       {@code varbitId} (Integer), {@code tooltip} (String, optional)</li>
 *   <li><b>ITEM_GRIND</b>: {@code itemId} (Integer)</li>
 *   <li><b>COMBAT_ACHIEVEMENT</b>: {@code caTaskId} (Integer), {@code tier} (String — "EASY".."GRANDMASTER"),
 *       {@code monster} (String), {@code tooltip} (String, optional)</li>
 *   <li><b>CUSTOM</b>: no extras</li>
 * </ul>
 *
 * <p>Optional keys are <em>absent</em> from the map, not null. Consumers can use
 * {@link Map#containsKey(Object)} or {@link Map#getOrDefault(Object, Object)}.
 */
public class GoalView
{
	public String id;
	/** Goal type as a string: SKILL, QUEST, DIARY, ITEM_GRIND, COMBAT_ACHIEVEMENT, CUSTOM. */
	public String type;
	public String name;
	public String description;
	public int currentValue;
	public int targetValue;
	/** 0 if not complete; nonzero millis-since-epoch when the goal was marked complete. */
	public long completedAt;
	public String sectionId;

	// ----- display hints -----

	/** RuneLite sprite id for the icon, or 0 if no sprite. */
	public int spriteId;
	/** Packed RGB color (0xRRGGBB) for the card background tint — override or default. */
	public int backgroundColorRgb;
	/** The type-default background color, even when an override is set. Lets
	 *  the UI preview what "reset to default" would revert to. */
	public int defaultBackgroundColorRgb;
	/** True when {@link #backgroundColorRgb} is a user override (not the type default). */
	public boolean backgroundColorOverridden;
	/** Ephemeral UI selection state — true when this card is currently selected
	 *  in the panel. Lost on plugin restart. */
	public boolean selected;
	/** True when the user has marked this goal as optional. */
	public boolean optional;
	/** Auto-generated tags from goal creation (boss/raid/tier/etc).
	 *  Cannot be removed by the user; restored by Restore Defaults. */
	public List<TagView> defaultTags;
	/** User-added tags layered on top of {@link #defaultTags}.
	 *  These are the only tags removable via the Remove Tag UI. */
	public List<TagView> customTags;

	// ----- relations -----

	/** Goals this one requires (outgoing edges), resolved at queryAllGoals
	 *  time. Empty list if none. Used by the card hover tooltip to show
	 *  the "Requires:" line. Implicit skill-chain edges (same skill,
	 *  different level) are excluded — those are internal bookkeeping. */
	public List<RelationView> requiresNames;
	/** Goals that require this one (incoming edges), resolved at
	 *  queryAllGoals time. Empty list if none. Used by the card hover
	 *  tooltip to show the "Required by:" line. Implicit skill-chain
	 *  edges are excluded. */
	public List<RelationView> requiredByNames;

	/**
	 * Lightweight DTO for a single relation edge, carrying enough metadata
	 * to render skill icons in tooltips (quest→skill links) or fall back
	 * to the goal name for non-skill relations.
	 */
	public static class RelationView
	{
		/** Display name of the related goal (fallback text). */
		public String name;
		/** Non-null for SKILL goals — the Skill enum name. Enables compact
		 *  icon+level rendering in tooltips. */
		public String skillName;
		/** Target level for SKILL goals (used for compact display). */
		public int targetLevel;
		/** True when this relation target is an optional/recommended goal. */
		public boolean optional;

		public RelationView(String name, String skillName, int targetLevel, boolean optional)
		{
			this.name = name;
			this.skillName = skillName;
			this.targetLevel = targetLevel;
			this.optional = optional;
		}
	}
	/** Topological tier within this goal's section, assigned by
	 *  {@link com.goaltracker.api.GoalTrackerInternalApi#queryGoalsTopologicallySorted}.
	 *  Leaves (nothing required in-section) are tier 0; each subsequent
	 *  tier consists of goals whose in-section requirements are all in
	 *  earlier tiers. -1 when the view was not produced by the topo-sort
	 *  query (e.g. from {@code queryAllGoals}). Used by the panel to
	 *  enable/disable the up/down arrows based on whether the visually
	 *  adjacent card is in the same tier. */
	public int topoTier = -1;

	// ----- type-specific extras -----

	public Map<String, Object> attributes = new HashMap<>();
}
