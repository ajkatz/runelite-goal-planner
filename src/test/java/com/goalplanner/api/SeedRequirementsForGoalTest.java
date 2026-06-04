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
		// Put the quest in a USER section (not the default Incomplete) so the test
		// actually catches seeds that strand in the default section.
		String sectionId = api.createSection("Guide");
		String questId = api.addQuestGoal(Quest.DRAGON_SLAYER_II);
		assertNotNull(questId);
		api.moveGoalToSection(questId, sectionId);

		int seeded = api.seedRequirementsForGoal(questId, true);
		assertTrue(seeded > 0, "expected DS2 to seed at least one prerequisite");

		// The quest goal is now linked to in-section prerequisites.
		Goal quest = store.findGoalById(questId);
		assertFalse(quest.getRequiredGoalIds().isEmpty(), "quest goal should gain requirement edges");

		// EVERY seeded goal (skill/quest/account/…) lands in the quest's user
		// section, not the default Incomplete section.
		for (Goal g : store.getGoals())
		{
			assertEquals(sectionId, g.getSectionId(),
				"seeded goal " + g.getName() + " should share the quest's section");
		}

		// No goal is duplicated: cross-section dedup reuses an equivalent already
		// in the target section instead of spawning a fresh copy on the 2nd+ path.
		assertNoDuplicateIdentities();
	}

	@Test
	@DisplayName("'All' reuses a shared transitive prereq once (no stranded duplicate) and reaches under completed quests")
	void allSeedsSharedTransitivePrereqExactlyOnce()
	{
		// Desert Treasure II reaches Troll Stronghold via TWO paths:
		//   DT2 → Desert Treasure I → Troll Stronghold
		//   DT2 → Secrets of the North → Devious Minds → Troll Stronghold
		// and Death Plateau sits transitively UNDER Troll Stronghold. Full-tree
		// (floor-lookup) recursion must reach Death Plateau, and the shared Troll
		// Stronghold must be reused — not duplicated and stranded in Incomplete.
		String sectionId = api.createSection("Guide");
		String questId = api.addQuestGoal(Quest.DESERT_TREASURE_II__THE_FALLEN_EMPIRE);
		assertNotNull(questId);
		api.moveGoalToSection(questId, sectionId);

		int seeded = api.seedRequirementsForGoal(questId, true);
		assertTrue(seeded > 0, "expected DT2 to seed prerequisites");

		// (a) The shared transitive prereq exists EXACTLY once.
		assertEquals(1, countByQuestName("TROLL_STRONGHOLD"),
			"shared transitive prereq Troll Stronghold should be seeded exactly once");

		// (b) Every seeded goal lives in the target section — nothing stranded in
		//     the default Incomplete by a blocked cross-namespace move.
		for (Goal g : store.getGoals())
		{
			assertEquals(sectionId, g.getSectionId(),
				"seeded goal " + g.getName() + " should share the quest's section");
		}
		assertNoDuplicateIdentities();

		// (c) A prereq sitting transitively under Troll Stronghold is present —
		//     proves full-tree recursion descends past it.
		assertEquals(1, countByQuestName("DEATH_PLATEAU"),
			"transitive prereq Death Plateau (under Troll Stronghold) should be seeded");
	}

	/** Count goals whose questName matches (QUEST goals only). */
	private long countByQuestName(String questEnumName)
	{
		return store.getGoals().stream()
			.filter(g -> questEnumName.equals(g.getQuestName()))
			.count();
	}

	/** Fail if any two goals denote the same objective (per {@link com.goalplanner.model.GoalIdentity}). */
	private void assertNoDuplicateIdentities()
	{
		java.util.List<Goal> goals = new java.util.ArrayList<>(store.getGoals());
		for (int i = 0; i < goals.size(); i++)
		{
			for (int j = i + 1; j < goals.size(); j++)
			{
				assertFalse(
					com.goalplanner.model.GoalIdentity.sameIdentity(goals.get(i), goals.get(j)),
					"duplicate goal identity: '" + goals.get(i).getName()
						+ "' and '" + goals.get(j).getName() + "'");
			}
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
	@DisplayName("'All' sets the section to keep completed goals inline (so they render as cards, not ghosts)")
	void allKeepsCompletedInline()
	{
		String sectionId = api.createSection("Guide");
		String questId = api.addQuestGoal(Quest.DRAGON_SLAYER_II);
		api.moveGoalToSection(questId, sectionId);

		api.seedRequirementsForGoal(questId, true);

		com.goalplanner.model.Section sec = store.findSection(sectionId);
		assertEquals(Boolean.FALSE, sec.getAutoArchiveOverride(),
			"'All' should flip the section to keep-inline so met requirements stay as cards");
	}

	@Test
	@DisplayName("'Incomplete only' leaves the section's auto-archive setting untouched")
	void incompleteOnlyLeavesArchiveUntouched()
	{
		String sectionId = api.createSection("Guide");
		String questId = api.addQuestGoal(Quest.DRAGON_SLAYER_II);
		api.moveGoalToSection(questId, sectionId);

		api.seedRequirementsForGoal(questId, false);

		com.goalplanner.model.Section sec = store.findSection(sectionId);
		assertNull(sec.getAutoArchiveOverride(), "incomplete-only must not change the archive override");
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
