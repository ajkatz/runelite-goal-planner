package com.goaltracker.tracker;

import com.goaltracker.api.GoalTrackerApiImpl;
import com.goaltracker.data.WikiCaRepository;
import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalType;
import com.goaltracker.persistence.GoalStore;
import com.goaltracker.service.GoalReorderingService;
import com.goaltracker.testsupport.InMemoryConfigManager;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link ItemTracker}, the inventory+bank quantity tracker.
 *
 * <p>Covers the Mission 17 fix: the bank-null guard was rewritten to allow
 * pre-bank-visit growth-only updates instead of an all-or-nothing block.
 * Pre-bank-visit, only growth past the persisted value is allowed (so a
 * partial inventory snapshot can't wipe a persisted higher value). After
 * the first bank visit, full bank+inventory counts apply normally.
 *
 * <p>Uses a real GoalStore + GoalTrackerApiImpl + ItemTracker against
 * Mockito-mocked Client / ItemContainer. The api is the canonical mutation
 * surface (Mission 14: trackers go through recordGoalProgress).
 */
class ItemTrackerTest
{
	private static final int CANNONBALL_ID = 2;

	private Client client;
	private ItemContainer inventoryContainer;
	private ItemContainer bankContainer;
	private GoalStore store;
	private GoalTrackerApiImpl api;
	private ItemTracker tracker;

	@BeforeEach
	void setUp()
	{
		client = mock(Client.class);
		inventoryContainer = mock(ItemContainer.class);
		bankContainer = mock(ItemContainer.class);

		ConfigManager configManager = InMemoryConfigManager.create();
		store = new GoalStore(configManager);
		store.load();

		ItemManager itemManager = mock(ItemManager.class);
		WikiCaRepository wikiCaRepository = mock(WikiCaRepository.class);
		GoalReorderingService reorderingService = new GoalReorderingService(store);
		api = new GoalTrackerApiImpl(store, reorderingService, itemManager, wikiCaRepository);

		tracker = new ItemTracker(client, api);
	}

	private Goal makeItemGoal(int itemId, int target)
	{
		Goal g = Goal.builder()
			.type(GoalType.ITEM_GRIND)
			.name("Cannonballs")
			.itemId(itemId)
			.targetValue(target)
			.currentValue(0)
			.build();
		store.addGoal(g);
		return g;
	}

	private Item mockItem(int id, int qty)
	{
		Item item = mock(Item.class);
		when(item.getId()).thenReturn(id);
		when(item.getQuantity()).thenReturn(qty);
		return item;
	}

	private void stubInventoryItems(Item... items)
	{
		when(client.getItemContainer(InventoryID.INVENTORY)).thenReturn(inventoryContainer);
		when(inventoryContainer.getItems()).thenReturn(items);
	}

	private void stubBankItems(Item... items)
	{
		when(client.getItemContainer(InventoryID.BANK)).thenReturn(bankContainer);
		when(bankContainer.getItems()).thenReturn(items);
	}

	private void stubBankNull()
	{
		when(client.getItemContainer(InventoryID.BANK)).thenReturn(null);
	}

	// ====================================================================
	// Bank loaded — full count behavior
	// ====================================================================

	@Test
	@DisplayName("With bank loaded, total count is inventory + bank")
	void fullCountSumsContainers()
	{
		Goal g = makeItemGoal(CANNONBALL_ID, 200);
		stubInventoryItems(mockItem(CANNONBALL_ID, 50));
		stubBankItems(mockItem(CANNONBALL_ID, 100));

		assertTrue(tracker.checkGoals(store.getGoals()));
		assertEquals(150, g.getCurrentValue());
	}

	@Test
	@DisplayName("With bank loaded, hitting target marks the goal complete")
	void fullCountFlipsCompleteOnTarget()
	{
		Goal g = makeItemGoal(CANNONBALL_ID, 200);
		stubInventoryItems(mockItem(CANNONBALL_ID, 100));
		stubBankItems(mockItem(CANNONBALL_ID, 100));

		assertTrue(tracker.checkGoals(store.getGoals()));
		assertEquals(200, g.getCurrentValue());
		assertTrue(g.isComplete());
	}

	@Test
	@DisplayName("Mission 25: completed item goals are sticky and do not revert on drop")
	void completedItemGoalIsSticky()
	{
		Goal g = makeItemGoal(CANNONBALL_ID, 200);
		// First pass: full target
		stubInventoryItems(mockItem(CANNONBALL_ID, 100));
		stubBankItems(mockItem(CANNONBALL_ID, 100));
		tracker.checkGoals(store.getGoals());
		assertTrue(g.isComplete());
		assertEquals(200, g.getCurrentValue());

		// Second pass: dropped to 150 — goal should STAY complete and frozen at 200
		stubInventoryItems(mockItem(CANNONBALL_ID, 50));
		stubBankItems(mockItem(CANNONBALL_ID, 100));
		assertFalse(tracker.checkGoals(store.getGoals()));
		assertEquals(200, g.getCurrentValue());
		assertTrue(g.isComplete());
	}

	// ====================================================================
	// Pre-bank-visit guard — growth-only updates
	// ====================================================================

	@Test
	@DisplayName("Before first bank visit, allow growth from inventory-only")
	void preBankVisitAllowsGrowth()
	{
		Goal g = makeItemGoal(CANNONBALL_ID, 200);
		assertEquals(0, g.getCurrentValue());

		// Bank null, inventory has 50 — growth from 0 → 50 should land
		stubBankNull();
		stubInventoryItems(mockItem(CANNONBALL_ID, 50));

		assertTrue(tracker.checkGoals(store.getGoals()));
		assertEquals(50, g.getCurrentValue());
	}

	@Test
	@DisplayName("Before first bank visit, reject decreases (protects persisted value)")
	void preBankVisitRejectsDecrease()
	{
		Goal g = makeItemGoal(CANNONBALL_ID, 200);
		// Pretend the persisted value from a previous session was 1000
		g.setCurrentValue(1000);

		stubBankNull();
		stubInventoryItems(mockItem(CANNONBALL_ID, 50));

		assertFalse(tracker.checkGoals(store.getGoals()));
		assertEquals(1000, g.getCurrentValue()); // unchanged
	}

	@Test
	@DisplayName("After first bank visit, full counts apply (sticky)")
	void bankSeenIsSticky()
	{
		Goal g = makeItemGoal(CANNONBALL_ID, 200);
		// First call: bank present → flag flips
		stubInventoryItems(mockItem(CANNONBALL_ID, 50));
		stubBankItems(mockItem(CANNONBALL_ID, 100));
		tracker.checkGoals(store.getGoals());
		assertEquals(150, g.getCurrentValue());

		// Second call: bank container would return null (edge case) but the
		// session flag should stay true
		stubBankNull();
		stubInventoryItems(mockItem(CANNONBALL_ID, 30));
		// countItem returns 30 from inventory, 0 from bank (null) = 30.
		// canTrustFullCount is true (sticky), so the update applies even though
		// it's a decrease — bank is just empty as far as the tracker can see.
		assertTrue(tracker.checkGoals(store.getGoals()));
		assertEquals(30, g.getCurrentValue());
	}

	// ====================================================================
	// Filtering / no-op cases
	// ====================================================================

	@Test
	@DisplayName("Skips non-ITEM_GRIND goals")
	void skipsNonItemGoals()
	{
		Goal skill = Goal.builder().type(GoalType.SKILL).name("Mining")
			.skillName("MINING").targetValue(100).build();
		store.addGoal(skill);

		stubInventoryItems();
		stubBankItems();

		assertFalse(tracker.checkGoals(store.getGoals()));
		assertEquals(0, skill.getCurrentValue());
	}

	@Test
	@DisplayName("Skips item goals with itemId <= 0")
	void skipsUnboundItemGoals()
	{
		Goal g = Goal.builder().type(GoalType.ITEM_GRIND).name("Unbound")
			.itemId(0).targetValue(100).build();
		store.addGoal(g);

		stubInventoryItems();
		stubBankItems();

		assertFalse(tracker.checkGoals(store.getGoals()));
	}

	@Test
	@DisplayName("Returns false when nothing changes (no-op detection)")
	void noopWhenCountUnchanged()
	{
		Goal g = makeItemGoal(CANNONBALL_ID, 200);
		stubInventoryItems(mockItem(CANNONBALL_ID, 50));
		stubBankItems(mockItem(CANNONBALL_ID, 100));

		// First pass: 0 → 150
		assertTrue(tracker.checkGoals(store.getGoals()));
		// Second pass with identical containers: should be no-op
		assertFalse(tracker.checkGoals(store.getGoals()));
	}

	// ====================================================================
	// Mission 23: comprehensive container counting
	// ====================================================================

	private void stubContainer(InventoryID id, Item... items)
	{
		ItemContainer c = mock(ItemContainer.class);
		when(c.getItems()).thenReturn(items);
		when(client.getItemContainer(id)).thenReturn(c);
	}

	@Test
	@DisplayName("countItem sums equipment + seed vault + group storage when present")
	void countItemSumsAllContainers()
	{
		stubInventoryItems(mockItem(CANNONBALL_ID, 10));
		stubBankItems(mockItem(CANNONBALL_ID, 20));
		stubContainer(InventoryID.EQUIPMENT, mockItem(CANNONBALL_ID, 5));
		stubContainer(InventoryID.SEED_VAULT, mockItem(CANNONBALL_ID, 100));
		stubContainer(InventoryID.GROUP_STORAGE, mockItem(CANNONBALL_ID, 200));
		stubContainer(InventoryID.KINGDOM_OF_MISCELLANIA, mockItem(CANNONBALL_ID, 3));

		assertEquals(338, tracker.countItem(CANNONBALL_ID));
	}

	@Test
	@DisplayName("countItem ignores containers that return null")
	void countItemIgnoresMissingContainers()
	{
		// Only inventory + equipment populated; bank/seed/group/misc all null.
		stubInventoryItems(mockItem(CANNONBALL_ID, 7));
		stubContainer(InventoryID.EQUIPMENT, mockItem(CANNONBALL_ID, 1));

		assertEquals(8, tracker.countItem(CANNONBALL_ID));
	}

	@Test
	@DisplayName("countItem returns 0 when no containers populated")
	void countItemZeroWhenAllNull()
	{
		assertEquals(0, tracker.countItem(CANNONBALL_ID));
	}

	@Test
	@DisplayName("countItem only counts matching itemId, ignores other items in containers")
	void countItemFiltersById()
	{
		stubInventoryItems(mockItem(CANNONBALL_ID, 50), mockItem(999, 1000));
		stubContainer(InventoryID.EQUIPMENT, mockItem(999, 1));
		assertEquals(50, tracker.countItem(CANNONBALL_ID));
	}
}
