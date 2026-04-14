package com.goaltracker.testsupport;

import net.runelite.api.Client;
import net.runelite.api.Experience;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Converts a {@link MockGameState} into a fully-configured Mockito
 * {@link Client} mock. All VarPlayer/Varbit/Skill/Quest/Item reads
 * are stubbed to return the values defined in the state snapshot.
 *
 * <p>Typical usage:
 * <pre>
 *   MockGameState state = new MockGameState().bossKills("Zulrah", 150);
 *   Client client = MockClientFactory.createClient(state);
 *   int kc = client.getVarpValue(VarPlayerID.TOTAL_ZULRAH_KILLS); // 150
 * </pre>
 */
public final class MockClientFactory
{
	private MockClientFactory() {}

	/**
	 * Create a Client mock configured to return values from the given state.
	 */
	public static Client createClient(MockGameState state)
	{
		Client client = mock(Client.class);

		// Game state: default to LOGGED_IN so trackers don't skip
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);

		// VarPlayer values
		for (Map.Entry<Integer, Integer> entry : state.getVarps().entrySet())
		{
			when(client.getVarpValue(entry.getKey())).thenReturn(entry.getValue());
		}

		// Varbit values
		for (Map.Entry<Integer, Integer> entry : state.getVarbits().entrySet())
		{
			when(client.getVarbitValue(entry.getKey())).thenReturn(entry.getValue());
		}

		// Skill XP and derived levels
		for (Map.Entry<Skill, Integer> entry : state.getSkillXp().entrySet())
		{
			when(client.getSkillExperience(entry.getKey())).thenReturn(entry.getValue());
			// Derive level from XP for getRealSkillLevel (used by ATT_STR_COMBINED etc.)
			int level = Experience.getLevelForXp(entry.getValue());
			when(client.getRealSkillLevel(entry.getKey())).thenReturn(level);
		}

		// Account scalars
		when(client.getTotalLevel()).thenReturn(state.getTotalLevel());
		Player localPlayer = mock(Player.class);
		when(localPlayer.getCombatLevel()).thenReturn(state.getCombatLevel());
		when(client.getLocalPlayer()).thenReturn(localPlayer);

		// Quest states via runScript/getIntStack
		stubQuestStates(client, state);

		// Item containers
		stubItemContainers(client, state);

		return client;
	}

	/**
	 * Quest.getState(client) internally calls:
	 *   client.runScript(4029, quest.getId())
	 *   return client.getIntStack()[0]  // 2=FINISHED, 1=NOT_STARTED, else IN_PROGRESS
	 *
	 * We intercept runScript to populate a shared intStack array based on
	 * the quest ID argument. Because runScript is varargs (Object...),
	 * Mockito expands the arguments — inv.getArguments() returns the
	 * individual elements, not a wrapped array.
	 */
	private static void stubQuestStates(Client client, MockGameState state)
	{
		Map<Integer, Integer> questIdToIntStackValue = new HashMap<>();
		for (Map.Entry<Quest, QuestState> entry : state.getQuestStates().entrySet())
		{
			int intStackValue;
			switch (entry.getValue())
			{
				case FINISHED:
					intStackValue = 2;
					break;
				case NOT_STARTED:
					intStackValue = 1;
					break;
				default:
					intStackValue = 0; // IN_PROGRESS
					break;
			}
			questIdToIntStackValue.put(entry.getKey().getId(), intStackValue);
		}

		int[] intStack = new int[1];
		when(client.getIntStack()).thenReturn(intStack);

		// runScript is varargs (Object...). Quest.getState creates an
		// Object[]{4029, questId} and passes it directly. We intercept
		// all runScript calls and check the arguments manually.
		doAnswer(inv ->
		{
			Object[] rawArgs = inv.getArguments();
			// Varargs are received as a single Object[] element
			Object[] scriptArgs = rawArgs;
			if (rawArgs.length == 1 && rawArgs[0] instanceof Object[])
			{
				scriptArgs = (Object[]) rawArgs[0];
			}
			if (scriptArgs.length >= 2
				&& scriptArgs[0] instanceof Integer
				&& (int) scriptArgs[0] == 4029
				&& scriptArgs[1] instanceof Integer)
			{
				int questId = (int) scriptArgs[1];
				intStack[0] = questIdToIntStackValue.getOrDefault(questId, 1);
			}
			return null;
		}).when(client).runScript(any(Object[].class));
	}

	/**
	 * Stub getItemContainer for each container defined in the state.
	 * Containers not in the state return null (Mockito default).
	 */
	private static void stubItemContainers(Client client, MockGameState state)
	{
		for (Map.Entry<InventoryID, List<MockGameState.MockItem>> entry
			: state.getItemContainers().entrySet())
		{
			ItemContainer container = mock(ItemContainer.class);
			Item[] items = entry.getValue().stream()
				.map(mi ->
				{
					Item item = mock(Item.class);
					when(item.getId()).thenReturn(mi.itemId);
					when(item.getQuantity()).thenReturn(mi.quantity);
					return item;
				})
				.toArray(Item[]::new);
			when(container.getItems()).thenReturn(items);
			when(client.getItemContainer(entry.getKey())).thenReturn(container);
		}
	}
}
