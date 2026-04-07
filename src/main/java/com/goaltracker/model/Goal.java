package com.goaltracker.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Goal
{
	@Builder.Default
	private String id = UUID.randomUUID().toString();

	private GoalType type;

	@Builder.Default
	private GoalStatus status = GoalStatus.ACTIVE;

	private String name;
	private String description;

	@Builder.Default
	private int priority = Integer.MAX_VALUE;

	// Progress
	private int targetValue;
	private int currentValue;

	// Type-specific references (nullable)
	private String skillName;     // For SKILL goals — matches net.runelite.api.Skill name
	private String questName;     // For QUEST goals — matches net.runelite.api.Quest name
	private int varbitId;         // For DIARY/COMBAT_ACHIEVEMENT goals
	private int itemId;           // For ITEM_GRIND goals
	private int spriteId;         // Optional sprite icon (e.g. CA tier sword); 0 = unset
	private String tooltip;       // Optional hover tooltip text (e.g. CA full description)
	private String sectionId;     // Section this goal belongs to; null = unassigned (migrated on load)

	// For COMBAT_ACHIEVEMENT goals: the wiki / in-game CA task id. Used to look up
	// the bit-packed completion state from one of the 20 CA_TASK_COMPLETED varplayers.
	// Sentinel -1 = not set; tracker skips when negative.
	@Builder.Default
	private int caTaskId = -1;

	// Tags (source/category labels for filtering and display)
	@Builder.Default
	private List<ItemTag> tags = new ArrayList<>();

	// Default tags from auto-generation (for "Restore Defaults")
	@Builder.Default
	private List<ItemTag> defaultTags = new ArrayList<>();

	// Integrations
	private String wikiUrl;
	private String inventorySetup;  // Inventory Setups loadout name

	// Metadata
	@Builder.Default
	private long createdAt = System.currentTimeMillis();
	private long completedAt;

	public double getProgressPercent()
	{
		if (targetValue <= 0)
		{
			return isComplete() ? 100.0 : 0.0;
		}
		return Math.max(0.0, Math.min(100.0, (currentValue * 100.0) / targetValue));
	}

	/**
	 * A goal is complete iff it has a non-zero completion timestamp.
	 * This is the canonical check used everywhere for completion state.
	 * The {@link GoalStatus#COMPLETE} value is kept in sync by setters but is no
	 * longer authoritative — completedAt is.
	 */
	public boolean isComplete()
	{
		return completedAt > 0;
	}

	/**
	 * Whether the goal's current value has reached or exceeded its target.
	 * Used by trackers to decide when to <em>set</em> the completion timestamp;
	 * separate from {@link #isComplete()} which is a state check.
	 */
	public boolean meetsTarget()
	{
		return targetValue > 0 && currentValue >= targetValue;
	}
}
