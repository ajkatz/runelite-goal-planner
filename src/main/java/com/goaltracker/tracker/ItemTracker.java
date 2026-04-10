package com.goaltracker.tracker;

import com.goaltracker.api.GoalTrackerApiImpl;
import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalStatus;
import com.goaltracker.model.GoalType;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Tracks item/resource quantity goals by checking inventory and bank.
 * Overrides the base checkGoals() because of the pre-bank-visit guard
 * and terminal completion logic that don't fit the standard loop.
 */
@Singleton
public class ItemTracker extends AbstractTracker
{
	/**
	 * Sticky for the lifetime of the plugin session: true once we've ever
	 * seen a non-null bank container.
	 */
	private boolean bankSeenThisSession = false;

	/**
	 * Persistent storage containers we sum across when counting items.
	 * Transient reward chests are intentionally excluded.
	 */
	private static final InventoryID[] COUNTED_CONTAINERS = {
		InventoryID.INVENTORY,
		InventoryID.BANK,
		InventoryID.EQUIPMENT,
		InventoryID.SEED_VAULT,
		InventoryID.GROUP_STORAGE,
		InventoryID.KINGDOM_OF_MISCELLANIA,
	};

	@Inject
	public ItemTracker(Client client, GoalTrackerApiImpl api)
	{
		super(client, api);
	}

	@Override
	protected GoalType targetType()
	{
		return GoalType.ITEM_GRIND;
	}

	@Override
	protected int readCurrentValue(Goal goal)
	{
		return countItem(goal.getItemId());
	}

	/**
	 * Overrides the base loop to add pre-bank-visit guard and terminal
	 * completion logic.
	 */
	@Override
	public boolean checkGoals(List<Goal> goals)
	{
		boolean bankAvailable = client.getItemContainer(InventoryID.BANK) != null;
		if (bankAvailable) bankSeenThisSession = true;
		boolean canTrustFullCount = bankSeenThisSession;

		boolean anyUpdated = false;

		for (Goal goal : goals)
		{
			if (goal.getType() != GoalType.ITEM_GRIND)
			{
				continue;
			}

			// Terminal once complete — dropping below target does NOT
			// auto-revert. User must manually mark incomplete.
			if (goal.getStatus() == GoalStatus.COMPLETE)
			{
				continue;
			}

			if (goal.getItemId() <= 0)
			{
				continue;
			}

			int totalCount = countItem(goal.getItemId());

			// Pre-bank-visit guard: only allow upward updates so we never
			// shrink a persisted value with a partial inventory-only snapshot.
			if (!canTrustFullCount && totalCount <= goal.getCurrentValue())
			{
				continue;
			}

			if (api.recordGoalProgress(goal.getId(), totalCount))
			{
				anyUpdated = true;
			}
		}

		return anyUpdated;
	}

	/**
	 * Count total quantity of an item across every storage container.
	 * Public so the create-goal UI can snapshot a baseline for relative
	 * item goals.
	 */
	public int countItem(int itemId)
	{
		int count = 0;
		for (InventoryID id : COUNTED_CONTAINERS)
		{
			count += countInContainer(id, itemId);
		}
		return count;
	}

	private int countInContainer(InventoryID containerId, int itemId)
	{
		ItemContainer container = client.getItemContainer(containerId);
		if (container == null)
		{
			return 0;
		}

		int count = 0;
		for (Item item : container.getItems())
		{
			if (item.getId() == itemId)
			{
				count += item.getQuantity();
			}
		}
		return count;
	}
}
