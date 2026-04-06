package com.goaltracker;

import com.goaltracker.data.CombatAchievementData;
import com.goaltracker.data.CombatAchievementData.Tier;
import com.goaltracker.data.ItemSourceData;
import com.goaltracker.data.SourceAttributes;
import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalType;
import com.goaltracker.model.ItemTag;
import com.goaltracker.model.TagCategory;
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
import net.runelite.client.game.SpriteManager;
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
	private SpriteManager spriteManager;

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

		panel = new GoalPanel(goalStore, skillIconManager, itemManager, spriteManager, this::openItemSearch);
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
							java.util.List<ItemTag> autoTags = buildItemTags(itemId);
							Goal goal = Goal.builder()
								.type(GoalType.ITEM_GRIND)
								.name(itemName)
								.description(FormatUtil.formatNumber(targetQty) + " total")
								.itemId(itemId)
								.targetValue(targetQty)
								.currentValue(-1)
								.tags(new java.util.ArrayList<>(autoTags))
								.defaultTags(new java.util.ArrayList<>(autoTags))
								.build();

							goalStore.addGoal(goal);
							panel.rebuild();
							refreshItemGoalsNow();
						}
					});
				});
			})
			.build();
	}

	/**
	 * Build tags for an item, including source tags and inherited attributes (e.g., Slayer Task).
	 */
	private java.util.List<ItemTag> buildItemTags(int itemId)
	{
		java.util.List<ItemTag> tags = new java.util.ArrayList<>(ItemSourceData.getTags(itemId));
		if (tags.isEmpty())
		{
			// Try canonicalized ID
			int canonical = itemManager.canonicalize(itemId);
			if (canonical != itemId)
			{
				tags = new java.util.ArrayList<>(ItemSourceData.getTags(canonical));
			}
		}

		// Check for inherited attributes
		boolean needsSlayerTag = false;
		boolean isPet = false;
		for (ItemTag tag : tags)
		{
			if (SourceAttributes.isSlayerTask(tag.getLabel()))
			{
				needsSlayerTag = true;
			}
			if ("All Pets".equals(tag.getLabel()))
			{
				isPet = true;
			}
		}
		if (needsSlayerTag)
		{
			tags.add(new ItemTag("Slayer", TagCategory.SKILLING));
		}
		if (isPet)
		{
			tags.removeIf(t -> "All Pets".equals(t.getLabel()));
			tags.add(0, new ItemTag("Pet", TagCategory.SPECIAL));
		}

		return tags;
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

	/**
	 * Read a row's child widget from a per-row dynamic-children container.
	 * Returns null if the widget or the row index is out of range.
	 */
	private net.runelite.api.widgets.Widget getCaRowChild(int widgetId, int row)
	{
		net.runelite.api.widgets.Widget w = client.getWidget(widgetId);
		if (w == null) return null;
		net.runelite.api.widgets.Widget[] children = w.getDynamicChildren();
		if (children == null || row < 0 || row >= children.length) return null;
		return children[row];
	}

	/**
	 * Build a combat achievement Goal from the clicked row index in the CA tasks interface.
	 * Returns null if the row can't be resolved.
	 */
	private Goal buildCombatAchievementGoal(int row)
	{
		net.runelite.api.widgets.Widget nameW = getCaRowChild(CombatAchievementData.TASKS_NAME, row);
		net.runelite.api.widgets.Widget tierW = getCaRowChild(CombatAchievementData.TASKS_TIER, row);
		net.runelite.api.widgets.Widget monsterW = getCaRowChild(CombatAchievementData.TASKS_MONSTER, row);

		if (nameW == null || tierW == null)
		{
			log.warn("CA row {} missing name/tier widget", row);
			return null;
		}

		String name = nameW.getText();
		if (name == null || name.isEmpty())
		{
			log.warn("CA row {} has empty name", row);
			return null;
		}

		int tierSpriteId = tierW.getSpriteId();
		Tier tier = CombatAchievementData.tierFromSpriteId(tierSpriteId);
		String monster = monsterW != null ? CombatAchievementData.parseMonsterName(monsterW.getText()) : null;

		java.util.List<ItemTag> tags = new java.util.ArrayList<>();
		if (monster != null)
		{
			boolean isRaid = CombatAchievementData.isRaidBoss(monster);
			String tagLabel = isRaid ? CombatAchievementData.abbreviateRaid(monster) : monster;
			tags.add(new ItemTag(tagLabel, isRaid ? TagCategory.RAID : TagCategory.BOSS));
		}

		String description = tier != null
			? tier.getDisplayName() + " Combat Achievement"
			: "Combat Achievement";

		return Goal.builder()
			.type(GoalType.COMBAT_ACHIEVEMENT)
			.name(name)
			.description(description)
			.targetValue(1)
			.currentValue(0)
			.spriteId(tierSpriteId > 0 ? tierSpriteId : 0)
			.tags(tags)
			.defaultTags(new java.util.ArrayList<>(tags))
			.build();
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

		// Look for item or combat achievement context
		for (MenuEntry entry : entries)
		{
			int widgetGroupId = entry.getParam1() >> 16;
			int itemId = entry.getItemId();

			// Combat achievements (CA_TASKS widget group 715)
			boolean isCombatAchievement = widgetGroupId == CombatAchievementData.GROUP_ID
				&& entry.getParam1() == CombatAchievementData.TASKS_BACKGROUND;
			if (isCombatAchievement)
			{
				final int row = entry.getParam0();
				final Goal preview = buildCombatAchievementGoal(row);
				if (preview == null)
				{
					continue;
				}

				client.createMenuEntry(1)
					.setOption("Add Goal")
					.setTarget(entry.getTarget())
					.setType(MenuAction.RUNELITE)
					.onClick(e ->
					{
						// Re-read at click time in case the list was re-filtered
						Goal goal = buildCombatAchievementGoal(row);
						if (goal == null)
						{
							log.warn("CA goal row {} no longer resolvable at click time", row);
							return;
						}
						goalStore.addGoal(goal);
						javax.swing.SwingUtilities.invokeLater(() -> panel.rebuild());
						refreshItemGoalsNow();
					});
				break;
			}

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
			final boolean fromCollectionLog = isCollectionLog;

			// Add at index 1 to put it near the bottom of the menu
			client.createMenuEntry(1)
				.setOption("Add Goal")
				.setTarget(entry.getTarget())
				.setType(MenuAction.RUNELITE)
				.onClick(e ->
				{
					int qty;
					if (fromCollectionLog)
					{
						// Collection log items default to 1, skip the dialog
						qty = 1;
					}
					else
					{
						String input = javax.swing.JOptionPane.showInputDialog(
							panel,
							"Target quantity for " + itemName + ":",
							"1"
						);
						if (input == null) return;
						try
						{
							qty = Integer.parseInt(input.trim().replace(",", ""));
							if (qty <= 0) return;
						}
						catch (NumberFormatException ignored) { return; }
					}

					java.util.List<ItemTag> autoTags = buildItemTags(realItemId);
					Goal goal = Goal.builder()
						.type(GoalType.ITEM_GRIND)
						.name(itemName)
						.description(FormatUtil.formatNumber(qty) + " total")
						.itemId(realItemId)
						.targetValue(qty)
						.currentValue(-1)
						.tags(new java.util.ArrayList<>(autoTags))
						.defaultTags(new java.util.ArrayList<>(autoTags))
						.build();

					goalStore.addGoal(goal);
					javax.swing.SwingUtilities.invokeLater(() -> panel.rebuild());
					refreshItemGoalsNow();
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

	/**
	 * Immediately recheck item goals against the currently-cached bank/inventory
	 * containers. Runs on the client thread so ItemContainer reads are safe.
	 * This lets a newly-added ITEM_GRIND goal get its count populated right away
	 * if the bank is already open (otherwise it waits for the next
	 * ItemContainerChanged event, which only fires on open).
	 */
	private void refreshItemGoalsNow()
	{
		clientThread.invokeLater(() ->
		{
			boolean updated = itemTracker.checkGoals(goalStore.getGoals());
			if (updated)
			{
				goalStore.save();
				javax.swing.SwingUtilities.invokeLater(() -> panel.rebuild());
			}
		});
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
