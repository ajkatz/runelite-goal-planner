package com.goaltracker.tracker;

import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalType;
import com.goaltracker.testsupport.MockGameState;
import com.goaltracker.testsupport.TrackerTestHarness;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link QuestTracker} using the MockGameState framework.
 * Validates that the tracker correctly reads quest completion state
 * via Quest.getState(client) (which internally uses runScript/getIntStack).
 */
class QuestTrackerTest
{
	private Goal makeQuestGoal(Quest quest)
	{
		return Goal.builder()
			.type(GoalType.QUEST)
			.name(quest.getName())
			.questName(quest.name())
			.targetValue(1)
			.currentValue(0)
			.build();
	}

	@Test
	@DisplayName("detects finished quest")
	void detectsFinishedQuest()
	{
		MockGameState state = new MockGameState()
			.questFinished(Quest.COOKS_ASSISTANT);

		var h = TrackerTestHarness.forQuests(state);
		Goal goal = makeQuestGoal(Quest.COOKS_ASSISTANT);
		h.store().addGoal(goal);

		assertTrue(h.tracker().checkGoals(h.store().getGoals()));
		assertEquals(1, goal.getCurrentValue());
		assertTrue(goal.isComplete());
	}

	@Test
	@DisplayName("unfinished quest reads as 0")
	void unfinishedQuestReadsZero()
	{
		MockGameState state = new MockGameState()
			.questState(Quest.DRAGON_SLAYER_I, QuestState.IN_PROGRESS);

		var h = TrackerTestHarness.forQuests(state);
		Goal goal = makeQuestGoal(Quest.DRAGON_SLAYER_I);
		h.store().addGoal(goal);

		assertFalse(h.tracker().checkGoals(h.store().getGoals()),
			"no change from initial 0");
		assertEquals(0, goal.getCurrentValue());
	}

	@Test
	@DisplayName("detects quest completion between snapshots")
	void detectsCompletionBetweenSnapshots()
	{
		MockGameState before = new MockGameState()
			.questState(Quest.MONKEY_MADNESS_I, QuestState.IN_PROGRESS);
		MockGameState after = before.copy()
			.questFinished(Quest.MONKEY_MADNESS_I);

		var h = TrackerTestHarness.forQuests(before);
		Goal goal = makeQuestGoal(Quest.MONKEY_MADNESS_I);
		h.store().addGoal(goal);

		h.tracker().checkGoals(h.store().getGoals());
		assertFalse(goal.isComplete());

		h = h.withNewState(after);
		h.tracker().checkGoals(h.store().getGoals());
		assertTrue(goal.isComplete());
	}

	@Test
	@DisplayName("tracks multiple quests independently")
	void tracksMultipleQuests()
	{
		MockGameState state = new MockGameState()
			.questFinished(Quest.COOKS_ASSISTANT)
			.questState(Quest.DRAGON_SLAYER_I, QuestState.NOT_STARTED)
			.questFinished(Quest.ROMEO__JULIET);

		var h = TrackerTestHarness.forQuests(state);
		Goal cooks = makeQuestGoal(Quest.COOKS_ASSISTANT);
		Goal ds1 = makeQuestGoal(Quest.DRAGON_SLAYER_I);
		Goal romeo = makeQuestGoal(Quest.ROMEO__JULIET);
		h.store().addGoal(cooks);
		h.store().addGoal(ds1);
		h.store().addGoal(romeo);

		h.tracker().checkGoals(h.store().getGoals());
		assertTrue(cooks.isComplete(), "Cook's Assistant should be complete");
		assertFalse(ds1.isComplete(), "Dragon Slayer should not be complete");
		assertTrue(romeo.isComplete(), "Romeo & Juliet should be complete");
	}

	@Test
	@DisplayName("skips goals with null questName")
	void skipsNullQuestName()
	{
		MockGameState state = new MockGameState();
		var h = TrackerTestHarness.forQuests(state);

		Goal goal = Goal.builder()
			.type(GoalType.QUEST)
			.name("Bad quest goal")
			.questName(null)
			.targetValue(1)
			.build();
		h.store().addGoal(goal);

		assertFalse(h.tracker().checkGoals(h.store().getGoals()));
	}

	@Test
	@DisplayName("not-started quest defaults to NOT_STARTED (intStack = 1)")
	void notStartedQuestDefaultsCorrectly()
	{
		// Quest not registered in state at all → defaults to NOT_STARTED
		MockGameState state = new MockGameState();

		var h = TrackerTestHarness.forQuests(state);
		Goal goal = makeQuestGoal(Quest.UNDERGROUND_PASS);
		h.store().addGoal(goal);

		assertFalse(h.tracker().checkGoals(h.store().getGoals()));
		assertEquals(0, goal.getCurrentValue());
	}
}
