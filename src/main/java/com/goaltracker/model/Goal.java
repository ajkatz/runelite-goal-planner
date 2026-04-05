package com.goaltracker.model;

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
			return status == GoalStatus.COMPLETE ? 100.0 : 0.0;
		}
		return Math.max(0.0, Math.min(100.0, (currentValue * 100.0) / targetValue));
	}

	public boolean isComplete()
	{
		return status == GoalStatus.COMPLETE || (targetValue > 0 && currentValue >= targetValue);
	}
}
