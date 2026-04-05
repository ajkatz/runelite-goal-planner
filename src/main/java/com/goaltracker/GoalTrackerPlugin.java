package com.goaltracker;

import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalType;
import com.goaltracker.persistence.GoalStore;
import com.goaltracker.tracker.ItemTracker;
import com.goaltracker.tracker.SkillTracker;
import com.goaltracker.ui.GoalPanel;
import com.goaltracker.util.FormatUtil;
import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.StatChanged;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
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

		panel = new GoalPanel(goalStore, skillIconManager, itemManager, this::openItemSearch);
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
							"Add goal: " + FormatUtil.formatNumber(targetQty) + " x " + itemName + "?",
							"Confirm Item Goal",
							javax.swing.JOptionPane.OK_CANCEL_OPTION,
							javax.swing.JOptionPane.PLAIN_MESSAGE
						);

						if (confirm == javax.swing.JOptionPane.OK_OPTION)
						{
							Goal goal = Goal.builder()
								.type(GoalType.ITEM_GRIND)
								.name(itemName)
								.description(FormatUtil.formatNumber(targetQty) + " total")
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

	@Subscribe
	public void onGameTick(GameTick event)
	{
		tickCounter++;
		if (tickCounter >= SCAN_INTERVAL_TICKS)
		{
			tickCounter = 0;
			boolean updated = skillTracker.checkGoals(goalStore.getGoals());
			if (updated)
			{
				goalStore.save();
			}

			// Rebuild to refresh state and images
			javax.swing.SwingUtilities.invokeLater(() -> panel.rebuild());
		}
	}

	@Subscribe
	public void onMenuOpened(MenuOpened event)
	{
		MenuEntry[] entries = event.getMenuEntries();
		if (entries.length == 0)
		{
			return;
		}

		// Find the first entry to determine context
		MenuEntry first = entries[entries.length - 1];

		// Look for item context: check each entry for item-related widgets
		for (MenuEntry entry : entries)
		{
			int widgetGroupId = entry.getParam1() >> 16;
			int itemId = entry.getItemId();

			// Check if this is an inventory, bank, or collection log item
			boolean isInventory = widgetGroupId == WidgetID.INVENTORY_GROUP_ID;
			boolean isBank = widgetGroupId == WidgetID.BANK_GROUP_ID;
			boolean isCollectionLog = widgetGroupId == WidgetID.COLLECTION_LOG_ID;

			if (!isInventory && !isBank && !isCollectionLog)
			{
				continue;
			}

			// For collection log, itemId might be in identifier
			if (isCollectionLog && itemId <= 0)
			{
				itemId = entry.getIdentifier();
			}

			if (itemId <= 0)
			{
				continue;
			}

			// Get the real item ID (noted items have different IDs)
			final int realItemId = itemManager.canonicalize(itemId);
			String itemName = itemManager.getItemComposition(realItemId).getName();
			int defaultQty = 1;

			// Add at index 1 to put it near the bottom of the menu
			client.createMenuEntry(1)
				.setOption("Add Goal")
				.setTarget(entry.getTarget())
				.setType(MenuAction.RUNELITE)
				.onClick(e ->
				{
					String input = javax.swing.JOptionPane.showInputDialog(
						panel,
						"Target quantity for " + itemName + ":",
						String.valueOf(defaultQty)
					);
					if (input != null)
					{
						try
						{
							int qty = Integer.parseInt(input.trim().replace(",", ""));
							if (qty > 0)
							{
								Goal goal = Goal.builder()
									.type(GoalType.ITEM_GRIND)
									.name(itemName)
									.description(FormatUtil.formatNumber(qty) + " total")
									.itemId(realItemId)
									.targetValue(qty)
									.currentValue(-1)
									.build();

								goalStore.addGoal(goal);
								javax.swing.SwingUtilities.invokeLater(() -> panel.rebuild());
							}
						}
						catch (NumberFormatException ignored) {}
					}
				});

			// Only add one "Add Goal" entry
			break;
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		boolean updated = skillTracker.checkGoals(goalStore.getGoals());
		if (updated)
		{
			goalStore.save();
			javax.swing.SwingUtilities.invokeLater(() -> panel.rebuild());
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		// Only check items when bank or inventory changes
		int containerId = event.getContainerId();
		if (containerId != InventoryID.BANK.getId() && containerId != InventoryID.INVENTORY.getId())
		{
			return;
		}

		boolean updated = itemTracker.checkGoals(goalStore.getGoals());
		if (updated)
		{
			goalStore.save();
			javax.swing.SwingUtilities.invokeLater(() -> panel.rebuild());
		}
	}

	@Provides
	GoalTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GoalTrackerConfig.class);
	}
}
