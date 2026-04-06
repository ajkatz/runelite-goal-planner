package com.goaltracker.tracker;

import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalStatus;
import com.goaltracker.model.GoalType;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Tracks item/resource quantity goals by checking inventory and bank.
 */
@Slf4j
@Singleton
public class ItemTracker
{
	private final Client client;

	@Inject
	public ItemTracker(Client client)
	{
		this.client = client;
	}

	/**
	 * Update all item grind goals with current counts from inventory + bank.
	 * Returns true if any goal was updated.
	 */
	public boolean checkGoals(List<Goal> goals)
	{
		// We can only compute a trustworthy total if the bank container has been
		// loaded at least once this session. Without it, inventory-only reads
		// (including those triggered by login-time INVENTORY ItemContainerChanged
		// events) would wipe persisted values to 0. Bail until the bank is open.
		if (client.getItemContainer(InventoryID.BANK) == null)
		{
			return false;
		}

		boolean anyUpdated = false;

		for (Goal goal : goals)
		{
			if (goal.getType() != GoalType.ITEM_GRIND || goal.getStatus() != GoalStatus.ACTIVE)
			{
				continue;
			}

			if (goal.getItemId() <= 0)
			{
				continue;
			}

			int totalCount = countItem(goal.getItemId());

			if (totalCount != goal.getCurrentValue())
			{
				goal.setCurrentValue(totalCount);
				anyUpdated = true;

				if (goal.isComplete())
				{
					goal.setStatus(GoalStatus.COMPLETE);
					goal.setCompletedAt(System.currentTimeMillis());
					log.info("Item goal complete: {}", goal.getName());
				}
			}
		}

		return anyUpdated;
	}

	/**
	 * Count total quantity of an item across inventory and bank.
	 */
	private int countItem(int itemId)
	{
		int count = 0;
		count += countInContainer(InventoryID.INVENTORY, itemId);
		count += countInContainer(InventoryID.BANK, itemId);
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
