package com.goalplanner.api;

import com.goalplanner.data.WikiCaRepository;
import com.goalplanner.model.Goal;
import com.goalplanner.persistence.GoalStore;
import com.goalplanner.service.GoalReorderingService;
import com.goalplanner.testsupport.InMemoryConfigManager;
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link GoalPlannerApiImpl#seedRequirementsForGoal}, the "Add
 * requirements to this section" action. The "All" path (includeMet=true) uses
 * floor lookups and needs no live client, so it's deterministic in tests.
 */
@DisplayName("seedRequirementsForGoal — populate a quest goal's section with its requirements")
class SeedRequirementsForGoalTest
{
	private GoalStore store;
	private GoalPlannerApiImpl api;

	@BeforeEach
	void setUp()
	{
		ConfigManager configManager = InMemoryConfigManager.create();
		store = new GoalStore(configManager, new com.google.gson.Gson());
		store.load();
		ItemManager itemManager = mock(ItemManager.class);
		WikiCaRepository wikiCaRepository = mock(WikiCaRepository.class);
		GoalReorderingService reorderingService = new GoalReorderingService(store);
		api = new GoalPlannerApiImpl(store, reorderingService, itemManager, wikiCaRepository);
		api.setOnGoalsChanged(() -> {});
	}

	@Test
	@DisplayName("a non-quest goal is a no-op")
	void nonQuestGoalIsNoOp()
	{
		String skillId = api.addSkillGoalForLevel(Skill.ATTACK, 50);
		int before = store.getGoals().size();
		int seeded = api.seedRequirementsForGoal(skillId, true);
		assertEquals(0, seeded);
		assertEquals(before, store.getGoals().size());
	}

	@Test
	@DisplayName("an unknown goal id is a no-op")
	void unknownGoalIsNoOp()
	{
		assertEquals(0, api.seedRequirementsForGoal("does-not-exist", true));
	}

	@Test
	@DisplayName("'All' seeds the requirement tree into the quest goal's own section and links it")
	void allSeedsRequirementTreeIntoSection()
	{
		String questId = api.addQuestGoal(Quest.DRAGON_SLAYER_II);
		assertNotNull(questId);
		Goal quest = store.findGoalById(questId);
		String sectionId = quest.getSectionId();

		int seeded = api.seedRequirementsForGoal(questId, true);
		assertTrue(seeded > 0, "expected DS2 to seed at least one prerequisite");

		// The quest goal is now linked to in-section prerequisites.
		quest = store.findGoalById(questId);
		assertFalse(quest.getRequiredGoalIds().isEmpty(), "quest goal should gain requirement edges");

		// Every seeded goal lands in the quest's own section (parent's section).
		for (Goal g : store.getGoals())
		{
			assertEquals(sectionId, g.getSectionId(),
				"seeded goal " + g.getName() + " should share the quest's section");
		}
	}

	@Test
	@DisplayName("running 'All' twice reuses existing goals instead of duplicating")
	void rerunReusesGoals()
	{
		String questId = api.addQuestGoal(Quest.DRAGON_SLAYER_II);
		api.seedRequirementsForGoal(questId, true);
		int afterFirst = store.getGoals().size();
		api.seedRequirementsForGoal(questId, true);
		int afterSecond = store.getGoals().size();
		assertEquals(afterFirst, afterSecond, "re-running should not create duplicate goals");
	}

	@Test
	@DisplayName("one undo reverses the whole seeding gesture")
	void undoReversesSeeding()
	{
		String questId = api.addQuestGoal(Quest.DRAGON_SLAYER_II);
		int beforeSeed = store.getGoals().size();
		api.seedRequirementsForGoal(questId, true);
		assertTrue(store.getGoals().size() > beforeSeed);
		api.undo();
		assertEquals(beforeSeed, store.getGoals().size(), "undo should remove every seeded goal");
	}
}
