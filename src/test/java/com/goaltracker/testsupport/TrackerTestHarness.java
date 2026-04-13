package com.goaltracker.testsupport;

import com.goaltracker.api.GoalTrackerApiImpl;
import com.goaltracker.data.WikiCaRepository;
import com.goaltracker.persistence.GoalStore;
import com.goaltracker.service.GoalReorderingService;
import com.goaltracker.tracker.AbstractTracker;
import com.goaltracker.tracker.AccountTracker;
import com.goaltracker.tracker.BossKillTracker;
import com.goaltracker.tracker.CombatAchievementTracker;
import com.goaltracker.tracker.DiaryTracker;
import com.goaltracker.tracker.ItemTracker;
import com.goaltracker.tracker.QuestTracker;
import com.goaltracker.tracker.SkillTracker;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;

import java.util.function.BiFunction;

import static org.mockito.Mockito.mock;

/**
 * Wires up the full GoalStore + GoalTrackerApiImpl + Tracker stack
 * against a {@link MockGameState}-derived Client mock. Follows the
 * same pattern as {@code ItemTrackerTest.setUp()} but is reusable
 * across all tracker types.
 *
 * <p>Factory methods create the entire stack from a state snapshot:
 * <pre>
 *   var h = TrackerTestHarness.forBossKills(state);
 *   h.store().addGoal(goal);
 *   h.tracker().checkGoals(h.store().getGoals());
 *   assertEquals(150, goal.getCurrentValue());
 * </pre>
 *
 * <p>For before/after scenarios, use {@link #withNewState(MockGameState)}
 * to swap the Client and tracker while keeping the same GoalStore/API:
 * <pre>
 *   h = h.withNewState(afterState);
 *   h.tracker().checkGoals(h.store().getGoals());
 * </pre>
 *
 * @param <T> the tracker type
 */
public final class TrackerTestHarness<T extends AbstractTracker>
{
	private final GoalStore store;
	private final GoalTrackerApiImpl api;
	private final T tracker;
	private final Client client;
	private final BiFunction<Client, GoalTrackerApiImpl, T> trackerFactory;

	private TrackerTestHarness(
		GoalStore store,
		GoalTrackerApiImpl api,
		T tracker,
		Client client,
		BiFunction<Client, GoalTrackerApiImpl, T> trackerFactory)
	{
		this.store = store;
		this.api = api;
		this.tracker = tracker;
		this.client = client;
		this.trackerFactory = trackerFactory;
	}

	// -----------------------------------------------------------------
	// Factory methods — one per tracker type
	// -----------------------------------------------------------------

	public static TrackerTestHarness<BossKillTracker> forBossKills(MockGameState state)
	{
		return create(state, BossKillTracker::new);
	}

	public static TrackerTestHarness<SkillTracker> forSkills(MockGameState state)
	{
		return create(state, SkillTracker::new);
	}

	public static TrackerTestHarness<QuestTracker> forQuests(MockGameState state)
	{
		return create(state, QuestTracker::new);
	}

	public static TrackerTestHarness<DiaryTracker> forDiaries(MockGameState state)
	{
		return create(state, DiaryTracker::new);
	}

	public static TrackerTestHarness<CombatAchievementTracker> forCombatAchievements(MockGameState state)
	{
		return create(state, CombatAchievementTracker::new);
	}

	public static TrackerTestHarness<AccountTracker> forAccount(MockGameState state)
	{
		return create(state, AccountTracker::new);
	}

	public static TrackerTestHarness<ItemTracker> forItems(MockGameState state)
	{
		return create(state, ItemTracker::new);
	}

	// -----------------------------------------------------------------
	// State change support
	// -----------------------------------------------------------------

	/**
	 * Create a new harness with a different game state but the same
	 * GoalStore and API. Goal progress persists across the transition.
	 * A fresh tracker instance is created against the new Client mock.
	 */
	public TrackerTestHarness<T> withNewState(MockGameState newState)
	{
		Client newClient = MockClientFactory.createClient(newState);
		T newTracker = trackerFactory.apply(newClient, this.api);
		return new TrackerTestHarness<>(this.store, this.api, newTracker, newClient, this.trackerFactory);
	}

	// -----------------------------------------------------------------
	// Accessors
	// -----------------------------------------------------------------

	public GoalStore store() { return store; }
	public GoalTrackerApiImpl api() { return api; }
	public T tracker() { return tracker; }
	public Client client() { return client; }

	// -----------------------------------------------------------------
	// Internal wiring
	// -----------------------------------------------------------------

	private static <T extends AbstractTracker> TrackerTestHarness<T> create(
		MockGameState state,
		BiFunction<Client, GoalTrackerApiImpl, T> trackerFactory)
	{
		Client client = MockClientFactory.createClient(state);

		ConfigManager configManager = InMemoryConfigManager.create();
		GoalStore store = new GoalStore(configManager);
		store.load();

		ItemManager itemManager = mock(ItemManager.class);
		WikiCaRepository wikiCaRepository = mock(WikiCaRepository.class);
		GoalReorderingService reorderingService = new GoalReorderingService(store);
		GoalTrackerApiImpl api = new GoalTrackerApiImpl(
			store, reorderingService, itemManager, wikiCaRepository);

		T tracker = trackerFactory.apply(client, api);

		return new TrackerTestHarness<>(store, api, tracker, client, trackerFactory);
	}
}
