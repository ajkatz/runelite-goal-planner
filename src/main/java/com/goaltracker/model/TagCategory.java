package com.goaltracker.model;

import java.awt.Color;

/**
 * Categories for item source tags.
 * Each category has a distinct color for the tag pill.
 */
public enum TagCategory
{
	BOSS("Boss", new Color(220, 60, 60)),          // Red
	RAID("Raid", new Color(140, 80, 220)),         // Purple
	CLUE("Clue", new Color(0, 188, 212)),          // Cyan
	MINIGAME("Minigame", new Color(255, 152, 0)),  // Orange
	SKILLING("Skilling", new Color(76, 175, 80)),  // Green
	QUEST("Quest", new Color(65, 155, 222)),       // Quest blue — quest associations
	OTHER("Other", new Color(120, 120, 120));        // Gray
	// SPECIAL was removed. The canonical "Pet" tag now lives in
	// OTHER with a per-tag pink color override (seeded by GoalTrackerPlugin).

	private final String displayName;
	private final Color color;

	TagCategory(String displayName, Color color)
	{
		this.displayName = displayName;
		this.color = color;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public Color getColor()
	{
		return color;
	}
}
