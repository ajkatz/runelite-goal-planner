package com.goaltracker.integration;

import com.goaltracker.api.GoalTrackerApiImpl;
import com.goaltracker.data.WikiCaRepository;
import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalType;
import com.goaltracker.persistence.GoalStore;
import com.goaltracker.service.GoalReorderingService;
import com.goaltracker.testsupport.InMemoryConfigManager;
import com.goaltracker.testsupport.MockClientFactory;
import com.goaltracker.testsupport.MockGameState;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Integration tests for {@code addBossGoal}'s prereq-seeding behavior.
 *
 * <p>Validates that when a boss has a quest prereq, the quest's OWN
 * prereqs are transitively seeded — same chain-through behavior as
 * {@code addQuestGoalWithPrereqs} and {@code addDiaryGoalWithPrereqs}.
 * Without this chain, the card list would display the boss's quest as a
 * goal but none of the deeper skill/quest requirements underneath it.
 */
class BossGoalPrereqSeedingTest
{
	private GoalStore store;
	private GoalTrackerApiImpl api;

	@BeforeEach
	void setUp()
	{
		ConfigManager configManager = InMemoryConfigManager.create();
		store = new GoalStore(configManager);
		store.load();

		MockGameState freshState = new MockGameState();
		Client client = MockClientFactory.createClient(freshState);

		ItemManager itemManager = mock(ItemManager.class);
		WikiCaRepository wikiCaRepository = mock(WikiCaRepository.class);
		GoalReorderingService reorderingService = new GoalReorderingService(store);
		api = new GoalTrackerApiImpl(store, reorderingService, itemManager,
			wikiCaRepository, client);
	}

	private Set<String> questNames()
	{
		return store.getGoals().stream()
			.filter(g -> g.getType() == GoalType.QUEST)
			.map(Goal::getQuestName)
			.collect(Collectors.toSet());
	}

	private List<Goal> skillGoals()
	{
		return store.getGoals().stream()
			.filter(g -> g.getType() == GoalType.SKILL)
			.collect(Collectors.toList());
	}

	@Test
	@DisplayName("Amoxliatl seeds Heart of Darkness AND its transitive prereqs")
	void amoxliatlChainsThroughHeartOfDarkness()
	{
		String bossId = api.addBossGoal("Amoxliatl", 50);
		assertNotNull(bossId, "boss goal should be created");

		Set<String> quests = questNames();
		assertTrue(quests.contains(Quest.THE_HEART_OF_DARKNESS.name()),
			"direct quest prereq should be seeded: " + quests);
		// HoD's own quest prereq per QuestRequirements.java:1466
		assertTrue(quests.contains(Quest.TWILIGHTS_PROMISE.name()),
			"transitive quest prereq (HoD → Twilight's Promise) should be seeded: " + quests);

		// HoD's skill prereqs per QuestRequirements.java:1460-1467
		// (Mining 55, Thieving 48, Slayer 48, Agility 46)
		List<Goal> skills = skillGoals();
		assertFalse(skills.isEmpty(),
			"HoD's own skill prereqs should be seeded transitively");
		Set<String> skillNames = skills.stream()
			.map(Goal::getSkillName).collect(Collectors.toSet());
		assertTrue(skillNames.contains("MINING"), "Mining 55 from HoD");
		assertTrue(skillNames.contains("THIEVING"), "Thieving 48 from HoD");
		assertTrue(skillNames.contains("SLAYER"), "Slayer 48 from HoD");
		assertTrue(skillNames.contains("AGILITY"), "Agility 46 from HoD");
	}

	@Test
	@DisplayName("Vorkath seeds Dragon Slayer II AND chain of DS2 prereq quests")
	void vorkathChainsThroughDragonSlayerII()
	{
		String bossId = api.addBossGoal("Vorkath", 100);
		assertNotNull(bossId);

		Set<String> quests = questNames();
		assertTrue(quests.contains(Quest.DRAGON_SLAYER_II.name()),
			"direct DS2 quest prereq: " + quests);
		// DS2's own prereq chain should be seeded; Bone Voyage is a
		// well-known DS2 prereq per QuestRequirements.java:409.
		assertTrue(quests.contains(Quest.BONE_VOYAGE.name()),
			"transitive DS2 → Bone Voyage prereq: " + quests);
	}

	@Test
	@DisplayName("Zalcano seeds SotE chain plus 70/70/70 skill reqs")
	void zalcanoChainsQuestsAndSkills()
	{
		String bossId = api.addBossGoal("Zalcano", 50);
		assertNotNull(bossId);

		Set<String> quests = questNames();
		assertTrue(quests.contains(Quest.SONG_OF_THE_ELVES.name()),
			"direct SotE prereq: " + quests);
		// SotE has many quest prereqs; assert the chain extends beyond
		// just the direct quest.
		assertTrue(quests.size() >= 2,
			"SotE's prereq chain should add quests beyond SotE itself: " + quests);

		// Zalcano's direct skill prereqs (70 Mining, 70 Smithing, 70 Runecraft)
		Set<String> skillNames = skillGoals().stream()
			.map(Goal::getSkillName).collect(Collectors.toSet());
		assertTrue(skillNames.contains("MINING"));
		assertTrue(skillNames.contains("SMITHING"));
		assertTrue(skillNames.contains("RUNECRAFT"));
	}
}
