package com.goaltracker.tracker;

import com.goaltracker.api.GoalTrackerApiImpl;
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
	private final GoalTrackerApiImpl api;

	/**
	 * Sticky for the lifetime of the plugin session: true once we've ever seen
	 * a non-null bank container. Used to drop the bank-null guard once the
	 * user has opened their bank at least once.
	 */
	private boolean bankSeenThisSession = false;

	@Inject
	public ItemTracker(Client client, GoalTrackerApiImpl api)
	{
		this.client = client;
		this.api = api;
	}

	/**
	 * Update all item grind goals with current counts from inventory + bank.
	 * Returns true if any goal was updated.
	 *
	 * <p>Pre-bank-visit policy: before the bank has ever been seen this
	 * session, we allow updates ONLY when the new (inventory-only) count is
	 * strictly greater than the persisted value. This lets users see their
	 * inventory grow on a fresh session without risking a wipe of persisted
	 * values from a partial inventory-only snapshot. After the first bank
	 * visit, the guard drops and full bank+inventory counts apply.
	 */
	public boolean checkGoals(List<Goal> goals)
	{
		boolean bankAvailable = client.getItemContainer(InventoryID.BANK) != null;
		if (bankAvailable) bankSeenThisSession = true;
		boolean canTrustFullCount = bankSeenThisSession;

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
