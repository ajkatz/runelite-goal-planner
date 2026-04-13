package com.goaltracker.integration;

import com.goaltracker.api.GoalTrackerApi;
import com.goaltracker.api.GoalTrackerApiImpl;
import com.goaltracker.data.AchievementDiaryData;
import com.goaltracker.data.DiaryRequirementResolver;
import com.goaltracker.data.WikiCaRepository;
import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalType;
import com.goaltracker.persistence.GoalStore;
import com.goaltracker.service.GoalReorderingService;
import com.goaltracker.testsupport.InMemoryConfigManager;
import com.goaltracker.testsupport.MockClientFactory;
import com.goaltracker.testsupport.MockGameState;
import com.goaltracker.tracker.DiaryTracker;
import com.goaltracker.tracker.QuestTracker;
import com.goaltracker.tracker.SkillTracker;
import net.runelite.api.Client;
import net.runelite.api.Experience;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Integration test: simulate a fresh account completing the Ardougne Easy
 * diary by progressing through all prerequisites (skills + quests), then
 * completing the diary itself. Verifies that all goals move through
 * ACTIVE → COMPLETE and into the Completed section.
 *
 * <p>Ardougne Easy requires: Thieving 5, Quest Rune Mysteries, Quest Biohazard.
 * Biohazard itself requires Plague City, which requires no additional skills.
 */
class DiaryCompletionIntegrationTest
{
	private GoalStore store;
	private GoalTrackerApiImpl api;

	@BeforeEach
	void setUp()
	{
		ConfigManager configManager = InMemoryConfigManager.create();
		store = new GoalStore(configManager);
		store.load();

		// Use a Client mock for the API so resolveQuestRequirements works
		// (it needs client != null to resolve quest prereq trees).
		// Fresh account: all quests NOT_STARTED.
		MockGameState freshState = new MockGameState();
		Client client = MockClientFactory.createClient(freshState);

		ItemManager itemManager = mock(ItemManager.class);
		WikiCaRepository wikiCaRepository = mock(WikiCaRepository.class);
		GoalReorderingService reorderingService = new GoalReorderingService(store);
		api = new GoalTrackerApiImpl(store, reorderingService, itemManager, wikiCaRepository, client);
	}

	/**
	 * Run all relevant trackers against the current goal list using the
	 * given game state. Returns true if any tracker detected a change.
	 */
	private boolean runAllTrackers(MockGameState state)
	{
		Client client = MockClientFactory.createClient(state);
		boolean changed = false;
		changed |= new SkillTracker(client, api).checkGoals(store.getGoals());
		changed |= new QuestTracker(client, api).checkGoals(store.getGoals());
		changed |= new DiaryTracker(client, api).checkGoals(store.getGoals());
		return changed;
	}

	private List<Goal> goalsOfType(GoalType type)
	{
		return store.getGoals().stream()
			.filter(g -> g.getType() == type)
			.collect(Collectors.toList());
	}

	private List<Goal> completedGoals()
	{
		return store.getGoals().stream()
			.filter(Goal::isComplete)
			.collect(Collectors.toList());
	}

	private List<Goal> activeGoals()
	{
		return store.getGoals().stream()
			.filter(g -> !g.isComplete())
			.collect(Collectors.toList());
	}

	private String completedSectionId()
	{
		return store.getCompletedSection().getId();
	}

	// ================================================================
	// Full diary lifecycle: fresh account → all prereqs → diary done
	// ================================================================

	@Test
	@DisplayName("fresh account completes Ardougne Easy diary through full prerequisite chain")
	void freshAccountCompletesArdougneEasy()
	{
		// ---- Phase 1: Resolve requirements for a brand-new account ----
		DiaryRequirementResolver.Resolved resolved = DiaryRequirementResolver.resolve(
			"Ardougne", AchievementDiaryData.Tier.EASY,
			skill -> 1,  // all skills at level 1
			quest -> QuestState.NOT_STARTED);

		// Seed the diary goal with all prerequisites
		api.addDiaryGoalWithPrereqs(
			"Ardougne", GoalTrackerApi.DiaryTier.EASY, resolved);

		// Find the diary goal by type (ID lookup is unreliable across compounds)
		Goal diaryGoal = store.getGoals().stream()
			.filter(g -> g.getType() == GoalType.DIARY)
			.findFirst().orElse(null);
		assertNotNull(diaryGoal, "diary goal should exist in store");
		assertTrue(diaryGoal.getRequiredGoalIds().size() > 0,
			"diary should have prerequisite links");

		List<Goal> allGoals = store.getGoals();
		assertTrue(allGoals.size() > 1,
			"should have diary + at least skill/quest prereqs");

		// Identify the prerequisite goals
		List<Goal> skillGoals = goalsOfType(GoalType.SKILL);
		List<Goal> questGoals = goalsOfType(GoalType.QUEST);

		// Ardougne Easy requires: Thieving 5, Rune Mysteries, Biohazard
		// Biohazard requires Plague City (seeded via quest prereq resolution)
		assertTrue(skillGoals.size() >= 1, "should have at least 1 skill prereq (Thieving)");
		assertTrue(questGoals.size() >= 2, "should have at least 2 quest prereqs");

		// Verify all goals start incomplete
		for (Goal g : allGoals)
		{
			assertFalse(g.isComplete(), "all goals should start incomplete: " + g.getName());
		}

		// ---- Phase 2: Fresh account, no progress ----
		MockGameState freshAccount = new MockGameState()
			.combatLevel(3)
			.totalLevel(32);

		runAllTrackers(freshAccount);

		// Nothing should be complete yet
		assertEquals(0, completedGoals().size(), "no goals complete on fresh account");

		// ---- Phase 3: Train Thieving to 5 ----
		int thievingXpTarget = Experience.getXpForLevel(5);
		MockGameState afterThieving = freshAccount.copy()
			.skillXp(Skill.THIEVING, thievingXpTarget);

		runAllTrackers(afterThieving);

		// The Thieving skill goal should now be complete
		Goal thievingGoal = skillGoals.stream()
			.filter(g -> g.getSkillName() != null && g.getSkillName().equals("THIEVING"))
			.findFirst().orElse(null);
		assertNotNull(thievingGoal, "should have a Thieving goal");
		assertTrue(thievingGoal.isComplete(),
			"Thieving goal should be complete at level 5 XP");

		// ---- Phase 4: Complete quest prerequisites ----
		// Plague City is a prereq of Biohazard, Rune Mysteries is standalone
		MockGameState afterQuests = afterThieving.copy()
			.questFinished(Quest.PLAGUE_CITY)
			.questFinished(Quest.RUNE_MYSTERIES)
			.questFinished(Quest.BIOHAZARD);

		runAllTrackers(afterQuests);

		// All quest goals should now be complete
		for (Goal q : questGoals)
		{
			assertTrue(q.isComplete(), "quest goal should be complete: " + q.getName());
		}

		// All prereqs done, but diary itself is NOT complete yet
		// (diary completion comes from the varbit, not from prereqs)
		assertFalse(diaryGoal.isComplete(),
			"diary goal should NOT be complete until varbit fires");

		// ---- Phase 5: Complete the diary in-game (varbit fires) ----
		MockGameState afterDiary = afterQuests.copy()
			.diaryComplete("Ardougne", AchievementDiaryData.Tier.EASY);

		runAllTrackers(afterDiary);

		// NOW the diary goal should be complete
		assertTrue(diaryGoal.isComplete(),
			"diary goal should be complete after varbit fires");

		// ---- Phase 6: Reconcile sections ----
		store.reconcileCompletedSection();

		// All completed goals should be in the Completed section
		String completedId = completedSectionId();
		for (Goal g : completedGoals())
		{
			assertEquals(completedId, g.getSectionId(),
				"completed goal should be in Completed section: " + g.getName());
		}

		// No active goals should remain in the Completed section
		for (Goal g : activeGoals())
		{
			assertNotEquals(completedId, g.getSectionId(),
				"active goal should NOT be in Completed section: " + g.getName());
		}
	}

	// ================================================================
	// Partial progress: skills done, quests not yet
	// ================================================================

	@Test
	@DisplayName("partial progress: skills complete but quests pending keeps diary incomplete")
	void partialProgressSkillsOnlyKeepsDiaryIncomplete()
	{
		DiaryRequirementResolver.Resolved resolved = DiaryRequirementResolver.resolve(
			"Ardougne", AchievementDiaryData.Tier.EASY,
			skill -> 1,
			quest -> QuestState.NOT_STARTED);

		api.addDiaryGoalWithPrereqs(
			"Ardougne", GoalTrackerApi.DiaryTier.EASY, resolved);

		Goal diaryGoal = store.getGoals().stream()
			.filter(g -> g.getType() == GoalType.DIARY)
			.findFirst().orElse(null);
		assertNotNull(diaryGoal, "diary goal should exist in store");

		// Complete all skills but no quests
		MockGameState state = new MockGameState()
			.skillXp(Skill.THIEVING, Experience.getXpForLevel(99));

		runAllTrackers(state);

		assertFalse(diaryGoal.isComplete());

		// Skill goals should be complete
		List<Goal> skills = goalsOfType(GoalType.SKILL);
		for (Goal s : skills)
		{
			assertTrue(s.isComplete(), "skill goal should be complete: " + s.getName());
		}

		// Quest goals should still be incomplete
		List<Goal> quests = goalsOfType(GoalType.QUEST);
		for (Goal q : quests)
		{
			assertFalse(q.isComplete(), "quest goal should still be incomplete: " + q.getName());
		}
	}

	// ================================================================
	// Account already meets some requirements
	// ================================================================

	@Test
	@DisplayName("mid-game account with existing skills seeds fewer prerequisites")
	void midGameAccountSeedsFewerPrereqs()
	{
		// Account already has Thieving 50 and completed Rune Mysteries
		DiaryRequirementResolver.Resolved resolved = DiaryRequirementResolver.resolve(
			"Ardougne", AchievementDiaryData.Tier.EASY,
			skill -> skill == Skill.THIEVING ? 50 : 1,
			quest -> quest == Quest.RUNE_MYSTERIES ? QuestState.FINISHED : QuestState.NOT_STARTED);

		assertTrue(resolved.skippedSkills > 0,
			"Thieving should be skipped (already met)");
		assertTrue(resolved.skippedQuests > 0,
			"Rune Mysteries should be skipped (already finished)");

		String diaryGoalId = api.addDiaryGoalWithPrereqs(
			"Ardougne", GoalTrackerApi.DiaryTier.EASY, resolved);

		// Should have fewer seeded goals since some were already met
		List<Goal> skillGoals = goalsOfType(GoalType.SKILL);
		assertEquals(0, skillGoals.size(),
			"no skill goals needed — Thieving already at 50");

		// Should still have Biohazard (+ Plague City prereq) but not Rune Mysteries
		List<Goal> questGoals = goalsOfType(GoalType.QUEST);
		boolean hasRuneMysteries = questGoals.stream()
			.anyMatch(g -> g.getQuestName() != null
				&& g.getQuestName().equals(Quest.RUNE_MYSTERIES.name()));
		assertFalse(hasRuneMysteries, "Rune Mysteries should be skipped");
	}

	// ================================================================
	// Incremental state transitions
	// ================================================================

	@Test
	@DisplayName("incremental state changes update goals one at a time")
	void incrementalStateChanges()
	{
		DiaryRequirementResolver.Resolved resolved = DiaryRequirementResolver.resolve(
			"Ardougne", AchievementDiaryData.Tier.EASY,
			skill -> 1,
			quest -> QuestState.NOT_STARTED);

		api.addDiaryGoalWithPrereqs(
			"Ardougne", GoalTrackerApi.DiaryTier.EASY, resolved);

		int totalGoals = store.getGoals().size();

		// Step 1: Train Thieving partially (level 3, not 5 yet)
		MockGameState step1 = new MockGameState()
			.skillXp(Skill.THIEVING, Experience.getXpForLevel(3));
		runAllTrackers(step1);

		// Thieving goal should have progress but NOT be complete
		Goal thievingGoal = goalsOfType(GoalType.SKILL).stream()
			.filter(g -> "THIEVING".equals(g.getSkillName()))
			.findFirst().orElse(null);
		assertNotNull(thievingGoal);
		assertTrue(thievingGoal.getCurrentValue() > 0,
			"should have some XP progress");
		assertFalse(thievingGoal.isComplete(),
			"level 3 should not meet level 5 target");

		// Step 2: Finish Thieving training
		MockGameState step2 = step1.copy()
			.skillXp(Skill.THIEVING, Experience.getXpForLevel(5));
		runAllTrackers(step2);
		assertTrue(thievingGoal.isComplete(), "level 5 should meet target");

		// Step 3: Complete one quest
		MockGameState step3 = step2.copy()
			.questFinished(Quest.RUNE_MYSTERIES);
		runAllTrackers(step3);

		long completedCount = completedGoals().size();
		assertTrue(completedCount >= 2,
			"at least Thieving + Rune Mysteries should be complete");
		assertTrue(completedCount < totalGoals,
			"not everything should be complete yet");

		// Step 4: Complete remaining quests
		MockGameState step4 = step3.copy()
			.questFinished(Quest.PLAGUE_CITY)
			.questFinished(Quest.BIOHAZARD);
		runAllTrackers(step4);

		// Step 5: Diary varbit fires
		MockGameState step5 = step4.copy()
			.diaryComplete("Ardougne", AchievementDiaryData.Tier.EASY);
		runAllTrackers(step5);

		// The diary goal and its direct prereqs (skills + quests) should
		// all be complete. Recommended combat levels from quest prereq trees
		// may remain incomplete — they are optional/recommended goals.
		Goal diaryGoal = store.getGoals().stream()
			.filter(g -> g.getType() == GoalType.DIARY)
			.findFirst().orElse(null);
		assertNotNull(diaryGoal);
		assertTrue(diaryGoal.isComplete(), "diary goal should be complete");

		// All skill and quest goals should be complete
		for (Goal g : goalsOfType(GoalType.SKILL))
		{
			// Only assert required skills (Thieving) — recommended combat
			// levels from quest prereq trees are auto-seeded but optional.
			if ("THIEVING".equals(g.getSkillName()))
			{
				assertTrue(g.isComplete(),
					"required skill should be complete: " + g.getName());
			}
		}
		for (Goal g : goalsOfType(GoalType.QUEST))
		{
			assertTrue(g.isComplete(),
				"quest should be complete: " + g.getName());
		}

		// Reconcile and verify completed goals move to Completed section
		store.reconcileCompletedSection();
		for (Goal g : completedGoals())
		{
			assertEquals(completedSectionId(), g.getSectionId(),
				"completed goal should be in Completed section: " + g.getName());
		}
	}
}
