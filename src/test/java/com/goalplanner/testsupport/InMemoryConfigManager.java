package com.goalplanner.testsupport;

import net.runelite.client.config.ConfigManager;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

/**
 * Test fake for {@link ConfigManager}: returns a Mockito spy whose
 * {@code getConfiguration} / {@code setConfiguration} methods are backed by an
 * in-memory {@code Map<String, String>}. The real GoalStore uses these two
 * methods only — everything else on ConfigManager is irrelevant for our tests.
 *
 * <p>Typical usage:
 * <pre>
 *   ConfigManager configManager = InMemoryConfigManager.create();
 *   GoalStore store = new GoalStore(configManager, new com.google.gson.Gson());
 *   store.load();
 * </pre>
 *
 * <p>This is a "spy on a mock" rather than a hand-rolled fake because
 * ConfigManager's constructor takes ~10 RuneLite singletons that we don't want
 * to wire up. Mockito's deep stubs would also work but explicit method stubs
 * are clearer.
 */
public final class InMemoryConfigManager
{
	private InMemoryConfigManager() {}

	public static ConfigManager create()
	{
		ConfigManager mock = Mockito.mock(ConfigManager.class);
		Map<String, String> store = new HashMap<>();

		Mockito.when(mock.getConfiguration(Mockito.anyString(), Mockito.anyString()))
			.thenAnswer(inv -> store.get(key(inv.getArgument(0), inv.getArgument(1))));

		Mockito.doAnswer(inv -> {
			Object value = inv.getArgument(2);
			store.put(key(inv.getArgument(0), inv.getArgument(1)),
				value == null ? null : value.toString());
			return null;
		}).when(mock).setConfiguration(Mockito.anyString(), Mockito.anyString(), Mockito.any());

		Mockito.doAnswer(inv -> {
			store.remove(key(inv.getArgument(0), inv.getArgument(1)));
			return null;
		}).when(mock).unsetConfiguration(Mockito.anyString(), Mockito.anyString());

		return mock;
	}

	private static String key(String group, String configKey)
	{
		return group + "." + configKey;
	}
}
