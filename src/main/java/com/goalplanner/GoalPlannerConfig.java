package com.goalplanner;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("goalplanner")
public interface GoalPlannerConfig extends Config
{
	@ConfigSection(
		name = "Appearance",
		description = "Side-panel font and readability options",
		position = 10
	)
	String appearanceSection = "appearance";

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

	@ConfigItem(
		keyName = "fontFamily",
		name = "Panel font",
		description = "Font family for the Goal Planner side panel. Try Sans-serif if the default is hard to read.",
		section = appearanceSection,
		position = 1
	)
	default PanelFontFamily fontFamily()
	{
		return PanelFontFamily.DEFAULT;
	}

	@ConfigItem(
		keyName = "fontScale",
		name = "Font size",
		description = "Scale the side-panel text up or down for readability",
		section = appearanceSection,
		position = 2
	)
	default PanelFontScale fontScale()
	{
		return PanelFontScale.NORMAL;
	}
}
