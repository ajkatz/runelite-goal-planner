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
	/** Packed RGB color (0xRRGGBB) for the card background tint. */
	public int backgroundColorRgb;
	/** Auto-generated tags from goal creation (boss/raid/tier/etc).
	 *  Cannot be removed by the user; restored by Restore Defaults. */
	public List<TagView> defaultTags;
	/** User-added tags layered on top of {@link #defaultTags}.
	 *  These are the only tags removable via the Remove Tag UI. */
	public List<TagView> customTags;

	// ----- type-specific extras -----

	public Map<String, Object> attributes = new HashMap<>();
}
