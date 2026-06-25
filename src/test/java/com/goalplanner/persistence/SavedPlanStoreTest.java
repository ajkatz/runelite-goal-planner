package com.goalplanner.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.goalplanner.testsupport.InMemoryConfigManager;
import com.google.gson.Gson;
import java.util.Arrays;
import java.util.List;
import net.runelite.client.config.ConfigManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SavedPlanStore — saved share-code library (per config profile)")
class SavedPlanStoreTest
{
	private ConfigManager config;
	private SavedPlanStore store;

	@BeforeEach
	void setUp()
	{
		config = InMemoryConfigManager.create();
		store = new SavedPlanStore(config, new Gson());
		store.load();
	}

	@Test
	@DisplayName("add() stores a plan, assigns an id, and keeps fields")
	void addStores()
	{
		SavedPlan p = store.add("Inferno prep", "GPSHARE1:abc", Arrays.asList("Slayer"));
		assertNotNull(p.getId());
		assertEquals(1, store.getPlans().size());
		SavedPlan got = store.getPlans().get(0);
		assertEquals("Inferno prep", got.getName());
		assertEquals("GPSHARE1:abc", got.getCode());
		assertEquals(List.of("Slayer"), got.getSectionNames());
	}

	@Test
	@DisplayName("persists across a fresh load (key not prefixed with main/leagues)")
	void persistsAcrossReload()
	{
		store.add("Plan A", "GPSHARE1:aaa", Arrays.asList("A"));
		store.add("Plan B", "GPSHARE1:bbb", Arrays.asList("B"));

		SavedPlanStore reopened = new SavedPlanStore(config, new Gson());
		reopened.load();
		assertEquals(2, reopened.getPlans().size());
		assertEquals("Plan A", reopened.getPlans().get(0).getName());
		assertEquals("Plan B", reopened.getPlans().get(1).getName());
	}

	@Test
	@DisplayName("reload() re-reads the library after the backing store changes (profile switch)")
	void reloadRereadsLibrary()
	{
		SavedPlan a = store.add("Plan A", "GPSHARE1:aaa", null);

		// A second store on the SAME config stands in for RuneLite swapping the
		// active profile's backing store underneath us: it removes A and adds B.
		SavedPlanStore other = new SavedPlanStore(config, new Gson());
		other.load();
		other.remove(a.getId());
		other.add("Plan B", "GPSHARE1:bbb", null);

		// Stale until reload: our store still shows A and has never seen B.
		assertEquals(1, store.getPlans().size());
		assertEquals("Plan A", store.getPlans().get(0).getName());

		store.reload();

		assertEquals(1, store.getPlans().size());
		assertEquals("Plan B", store.getPlans().get(0).getName());
	}

	@Test
	@DisplayName("rename and setSectionNames mutate and persist")
	void renameAndOverridesPersist()
	{
		SavedPlan p = store.add("old", "GPSHARE1:x", Arrays.asList("S1"));
		assertTrue(store.rename(p.getId(), "new name"));
		assertTrue(store.setSectionNames(p.getId(), Arrays.asList("Renamed Section")));

		SavedPlanStore reopened = new SavedPlanStore(config, new Gson());
		reopened.load();
		SavedPlan loaded = reopened.getPlans().get(0);
		assertEquals("new name", loaded.getName());
		assertEquals(List.of("Renamed Section"), loaded.getSectionNames());
	}

	@Test
	@DisplayName("rename/setSectionNames on an unknown id is a no-op returning false")
	void unknownIdNoOp()
	{
		assertFalse(store.rename("nope", "x"));
		assertFalse(store.setSectionNames("nope", Arrays.asList("y")));
	}

	@Test
	@DisplayName("remove deletes by id; unknown id returns false")
	void removeById()
	{
		SavedPlan p = store.add("x", "GPSHARE1:x", null);
		assertFalse(store.remove("nope"));
		assertEquals(1, store.getPlans().size());
		assertTrue(store.remove(p.getId()));
		assertTrue(store.getPlans().isEmpty());
	}

	@Test
	@DisplayName("corrupt config value loads an empty library without throwing")
	void corruptLoadsEmpty()
	{
		config.setConfiguration("goalplanner", "savedPlans", "{not valid json");
		SavedPlanStore s = new SavedPlanStore(config, new Gson());
		s.load();
		assertTrue(s.getPlans().isEmpty());
	}
}
