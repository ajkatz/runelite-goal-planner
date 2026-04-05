package com.goaltracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("goaltracker")
public interface GoalTrackerConfig extends Config
{
	@ConfigItem(
		keyName = "showOverlay",
		name = "Show Overlay",
		description = "Show compact goal progress overlay in-game"
	)
	default boolean showOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "maxOverlayGoals",
		name = "Max Overlay Goals",
		description = "Maximum number of goals shown in the overlay"
	)
	default int maxOverlayGoals()
	{
		return 3;
	}
}
