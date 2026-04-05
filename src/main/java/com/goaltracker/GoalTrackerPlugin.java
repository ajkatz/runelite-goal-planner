package com.goaltracker;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Goal Tracker",
	description = "Track and manage OSRS goals with a visual priority list",
	tags = {"goals", "tracker", "progress", "skills", "quests"}
)
public class GoalTrackerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private GoalTrackerConfig config;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private OverlayManager overlayManager;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Goal Tracker started");
		// TODO: Register sidebar panel, overlay, load saved goals
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Goal Tracker stopped");
		// TODO: Unregister sidebar panel, overlay, save goals
	}

	@Provides
	GoalTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GoalTrackerConfig.class);
	}
}
