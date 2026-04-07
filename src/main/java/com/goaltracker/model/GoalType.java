package com.goaltracker.model;

import java.awt.Color;

public enum GoalType
{
	SKILL("Skill", new Color(76, 175, 80)),           // Green
	QUEST("Quest", new Color(66, 133, 244)),           // Blue
	DIARY("Diary", new Color(85, 139, 47)),            // Olive green
	COLLECTION_LOG("Collection Log", new Color(156, 39, 176)),  // Purple
	ITEM_GRIND("Item", new Color(255, 193, 7)),                  // Gold
	COMBAT_ACHIEVEMENT("Combat Achievement", new Color(139, 69, 19)),  // Saddle brown
	CUSTOM("Custom", new Color(158, 158, 158));        // Gray

	private final String displayName;
	private final Color color;

	GoalType(String displayName, Color color)
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
