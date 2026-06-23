package com.goalplanner;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("goalplanner")
public interface GoalPlannerConfig extends Config
{
	@ConfigSection(
		name = "Behaviour",
		description = "How goals and sections behave.",
		position = 5
	)
	String behaviourSection = "behaviour";

	@ConfigItem(
		keyName = "autoArchiveCompleted",
		name = "Auto-archive completed",
		description = "Global default: when on, completed goals graduate out to the Completed list as they finish. Turn off to keep completed goals inline in their section as a checklist. Individual sections can override this.",
		section = behaviourSection,
		position = 1
	)
	default boolean autoArchiveCompleted()
	{
		return true;
	}

	@ConfigItem(
		keyName = "autoTrackMiscellania",
		name = "Auto-track Miscellania",
		description = "When you raise your Kingdom of Miscellania approval, automatically add a "
			+ "Miscellania 100% approval goal to your default section (once). Turn off to never "
			+ "auto-add it.",
		section = behaviourSection,
		position = 2
	)
	default boolean autoTrackMiscellania()
	{
		return true;
	}

	@ConfigSection(
		name = "Appearance",
		description = "Readability options (experimental). Report issues: discord.gg/CFQsA3fmh7",
		position = 10
	)
	String appearanceSection = "appearance";

	@ConfigItem(
		keyName = "fontFamily",
		name = "Panel font",
		description = "Side-panel font family. Try Sans-serif if the default is hard to read.",
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
		description = "Scale side-panel text up or down.",
		section = appearanceSection,
		position = 2
	)
	default PanelFontScale fontScale()
	{
		return PanelFontScale.NORMAL;
	}

	@ConfigItem(
		keyName = "showDependenciesIndented",
		name = "Indent dependencies by default",
		description = "Default for every section: show goals nested under the goals they require, "
			+ "indented with a faint guide. Each section can override this (Nested / Not nested "
			+ "/ Use default) from its right-click menu.",
		section = appearanceSection,
		position = 3
	)
	default boolean showDependenciesIndented()
	{
		return false;
	}
}
