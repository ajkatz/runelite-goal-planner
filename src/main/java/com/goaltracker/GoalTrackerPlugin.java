package com.goaltracker;

import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalType;
import com.goaltracker.persistence.GoalStore;
import com.goaltracker.tracker.ItemTracker;
import com.goaltracker.tracker.SkillTracker;
import com.goaltracker.ui.GoalPanel;
import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.chatbox.ChatboxItemSearch;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;

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
	private GoalStore goalStore;

	@Inject
	private SkillTracker skillTracker;

	@Inject
	private ItemTracker itemTracker;

	@Inject
	private SkillIconManager skillIconManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ChatboxItemSearch chatboxItemSearch;

	@Inject
	private ClientThread clientThread;

	private GoalPanel panel;
	private NavigationButton navButton;
	private int tickCounter = 0;
	private static final int SCAN_INTERVAL_TICKS = 25; // ~15 seconds (1 tick = 0.6s)

	@Override
	protected void startUp() throws Exception
	{
		log.info("Goal Tracker started");

		goalStore.load();

		// Pre-warm item image cache for existing item goals
		for (Goal goal : goalStore.getGoals())
		{
			if (goal.getType() == GoalType.ITEM_GRIND && goal.getItemId() > 0)
			{
				itemManager.getImage(goal.getItemId(), 1, false);
			}
		}

		panel = new GoalPanel(goalStore, skillIconManager, itemManager, this::openItemSearch, this::scanBankForGoal);
		panel.setClient(client);

		BufferedImage icon;
		try
		{
			icon = ImageUtil.loadImageResource(getClass(), "/goal_icon.png");
		}
		catch (Exception e)
		{
			log.warn("Could not load goal_icon.png, using fallback");
			icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		}

		navButton = NavigationButton.builder()
			.tooltip("Goal Tracker")
			.icon(icon)
			.priority(5)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);

		// Delayed rebuild to catch async item images
		new Thread(() ->
		{
			try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
			javax.swing.SwingUtilities.invokeLater(() -> panel.rebuild());
		}).start();
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Goal Tracker stopped");
		goalStore.save();
		if (navButton != null)
		{
			clientToolbar.removeNavigation(navButton);
		}
	}

	/**
	 * Opens the in-game chatbox item search.
	 * After selection, shows a confirmation dialog with item name + quantity.
	 */
	public void openItemSearch(int targetQty)
	{
		chatboxItemSearch
			.tooltipText("Select item for goal")
			.onItemSelected(itemId ->
			{
				clientThread.invokeLater(() ->
				{
					String itemName = itemManager.getItemComposition(itemId).getName();
					log.info("Item selected: {} (ID: {})", itemName, itemId);

					// Show confirmation on Swing thread
					javax.swing.SwingUtilities.invokeLater(() ->
					{
						int confirm = javax.swing.JOptionPane.showConfirmDialog(
							panel,
							"Add goal: " + GoalPanel.formatNumber(targetQty) + " x " + itemName + "?",
							"Confirm Item Goal",
							javax.swing.JOptionPane.OK_CANCEL_OPTION,
							javax.swing.JOptionPane.PLAIN_MESSAGE
						);

						if (confirm == javax.swing.JOptionPane.OK_OPTION)
						{
							Goal goal = Goal.builder()
								.type(GoalType.ITEM_GRIND)
								.name(itemName)
								.description(GoalPanel.formatNumber(targetQty) + " total")
								.itemId(itemId)
								.targetValue(targetQty)
								.currentValue(-1)  // -1 = unscanned, will update on next bank/inventory change
								.build();

							goalStore.addGoal(goal);
							panel.rebuild();
						}
					});
				});
			})
			.build();
	}

	/**
	 * Manually scan bank/inventory for a specific item goal.
	 */
	public void scanBankForGoal(String goalId)
	{
		clientThread.invokeLater(() ->
		{
			boolean updated = itemTracker.checkGoals(goalStore.getGoals());
			if (updated)
			{
				goalStore.save();
			}
			javax.swing.SwingUtilities.invokeLater(() -> panel.rebuild());
		});
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		tickCounter++;
		if (tickCounter >= SCAN_INTERVAL_TICKS)
		{
			tickCounter = 0;
			boolean updated = skillTracker.checkGoals(goalStore.getGoals());
			updated |= itemTracker.checkGoals(goalStore.getGoals());
			if (updated)
			{
				goalStore.save();
				javax.swing.SwingUtilities.invokeLater(() -> panel.rebuild());
			}
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		boolean updated = skillTracker.checkGoals(goalStore.getGoals());
		if (updated)
		{
			goalStore.save();
			panel.refresh();
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		boolean updated = itemTracker.checkGoals(goalStore.getGoals());
		if (updated)
		{
			goalStore.save();
			panel.refresh();
		}
	}

	@Provides
	GoalTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GoalTrackerConfig.class);
	}
}
