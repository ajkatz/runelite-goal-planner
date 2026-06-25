package com.goalplanner.tracker;

import com.goalplanner.api.GoalPlannerApiImpl;
import com.goalplanner.model.Goal;
import com.goalplanner.model.GoalStatus;
import com.goalplanner.model.GoalType;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.client.game.ItemVariationMapping;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public ItemTracker(Client client, GoalPlannerApiImpl api)
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
	 * Reset per-session bank visibility. Call on logout / world-hop: the bank
	 * ItemContainer is cleared when leaving a world, so until the player reopens
	 * the bank in the new session a full count is missing the bank. Without this
	 * reset the (sticky) "bank seen" flag stays true across a relog and lets a
	 * bank-less count overwrite a persisted item-goal value with 0 - the goals
	 * "reset to 0 on login" bug. After the reset the pre-bank guard re-engages,
	 * holding each goal's persisted value until the bank is seen again.
	 */
	public void onLogout()
	{
		bankSeenThisSession = false;
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
		// Sum every container ONCE into base-variation-id → quantity, then each
		// goal is a single lookup, so N item goals cost one container scan rather
		// than N. Built lazily so a tick with no active item goals scans nothing.
		Map<Integer, Integer> variantCounts = null;

		for (Goal goal : goals)
		{
			if (goal.getType() != GoalType.ITEM_GRIND)
			{
				continue;
			}

			// Terminal once complete - dropping below target does NOT
			// auto-revert. User must manually mark incomplete.
			if (goal.getStatus() == GoalStatus.COMPLETE)
			{
				continue;
			}

			if (goal.getItemId() <= 0)
			{
				continue;
			}

			if (variantCounts == null)
			{
				variantCounts = snapshotVariantCounts();
			}
			int totalCount = variantCounts.getOrDefault(ItemVariationMapping.map(goal.getItemId()), 0);

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
	 * Sum every counted container once into {@code base variation id → total
	 * quantity}. Variations of one item (charge/degrade states) collapse to a
	 * single base id via {@link ItemVariationMapping}, so a goal looks up its
	 * total in O(1). Null (unseen) containers contribute nothing.
	 */
	private Map<Integer, Integer> snapshotVariantCounts()
	{
		Map<Integer, Integer> counts = new HashMap<>();
		for (InventoryID id : COUNTED_CONTAINERS)
		{
			ItemContainer container = client.getItemContainer(id);
			if (container == null)
			{
				continue;
			}
			for (Item item : container.getItems())
			{
				counts.merge(ItemVariationMapping.map(item.getId()), item.getQuantity(), Integer::sum);
			}
		}
		return counts;
	}

	/**
	 * Count total quantity of an item across every storage container.
	 * Public so the create-goal UI can snapshot a baseline for relative
	 * item goals.
	 *
	 * <p>Counts ALL variations of the item, not just the exact id: a goal for a
	 * degradable/chargeable item completes whether the owned copy is pristine,
	 * degraded, broken, charged, or uncharged. RuneLite's {@link
	 * ItemVariationMapping} groups these under one base id (e.g. Blood moon
	 * tassets / (degraded) / (broken)); an item with no variations maps to
	 * itself, so non-variant items count exactly as before.
	 */
	public int countItem(int itemId)
	{
		int base = ItemVariationMapping.map(itemId);
		int count = 0;
		for (InventoryID id : COUNTED_CONTAINERS)
		{
			count += countInContainer(id, base);
		}
		return count;
	}

	private int countInContainer(InventoryID containerId, int base)
	{
		ItemContainer container = client.getItemContainer(containerId);
		if (container == null)
		{
			return 0;
		}

		int count = 0;
		for (Item item : container.getItems())
		{
			// Same variation family as the goal's item → counts toward it.
			if (ItemVariationMapping.map(item.getId()) == base)
			{
				count += item.getQuantity();
			}
		}
		return count;
	}
}
