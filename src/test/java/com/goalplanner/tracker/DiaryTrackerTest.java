package com.goalplanner.tracker;

import com.goalplanner.data.AchievementDiaryData;
import com.goalplanner.model.Goal;
import com.goalplanner.model.GoalType;
import com.goalplanner.testsupport.MockGameState;
import com.goalplanner.testsupport.TrackerTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DiaryTracker} using the MockGameState framework.
 * Validates that the tracker correctly reads diary completion varbits.
 */
class DiaryTrackerTest
{
	private Goal makeDiaryGoal(String area, AchievementDiaryData.Tier tier)
	{
		AchievementDiaryData.Tracking tracking = AchievementDiaryData.tracking(area, tier);
		return Goal.builder()
			.type(GoalType.DIARY)
			.name(area + " " + tier.getDisplayName() + " Diary")
			.varbitId(tracking != null ? tracking.varbitId : 0)
			.targetValue(tracking != null ? tracking.requiredValue : 1)
			.currentValue(0)
			.build();
	}

	@Test
	@DisplayName("detects diary completion from varbit")
	void detectsDiaryCompletion()
	{
		MockGameState state = new MockGameState()
			.diaryComplete("Ardougne", AchievementDiaryData.Tier.EASY);

		var h = TrackerTestHarness.forDiaries(state);
		Goal goal = makeDiaryGoal("Ardougne", AchievementDiaryData.Tier.EASY);
		h.store().addGoal(goal);

		assertTrue(h.tracker().checkGoals(h.store().getGoals()));
		assertEquals(1, goal.getCurrentValue());
		assertTrue(goal.isComplete());
	}

	@Test
	@DisplayName("incomplete diary reads as 0")
	void incompleteDiaryReadsZero()
	{
		// State with no diary completions
		MockGameState state = new MockGameState();

		var h = TrackerTestHarness.forDiaries(state);
		Goal goal = makeDiaryGoal("Falador", AchievementDiaryData.Tier.HARD);
		h.store().addGoal(goal);

		assertFalse(h.tracker().checkGoals(h.store().getGoals()),
			"no change from initial 0 value");
		assertEquals(0, goal.getCurrentValue());
	}

	@Test
	@DisplayName("detects diary completion between snapshots")
	void detectsCompletionBetweenSnapshots()
	{
		MockGameState before = new MockGameState();
		MockGameState after = before.copy()
			.diaryComplete("Desert", AchievementDiaryData.Tier.MEDIUM);

		var h = TrackerTestHarness.forDiaries(before);
		Goal goal = makeDiaryGoal("Desert", AchievementDiaryData.Tier.MEDIUM);
		h.store().addGoal(goal);

		h.tracker().checkGoals(h.store().getGoals());
		assertFalse(goal.isComplete());

		h = h.withNewState(after);
		h.tracker().checkGoals(h.store().getGoals());
		assertTrue(goal.isComplete());
	}

	@Test
	@DisplayName("skips goals with varbitId <= 0 (manual tracking)")
	void skipsManualDiaries()
	{
		MockGameState state = new MockGameState();
		var h = TrackerTestHarness.forDiaries(state);

		// Custom diary entry with no tracking varbit available — caller built
		// the goal without a varbit, so the tracker must leave it manual.
		Goal goal = Goal.builder()
			.type(GoalType.DIARY)
			.name("Custom Diary")
			.varbitId(0)
			.targetValue(1)
			.currentValue(0)
			.build();
		h.store().addGoal(goal);

		assertFalse(h.tracker().checkGoals(h.store().getGoals()),
			"goals with varbitId <= 0 should be skipped");
	}

	@Test
	@DisplayName("Karamja Medium completes via count varbit reaching 19")
	void karamjaMediumCompletesViaCountVarbit()
	{
		MockGameState state = new MockGameState()
			.diaryComplete("Karamja", AchievementDiaryData.Tier.MEDIUM);

		var h = TrackerTestHarness.forDiaries(state);
		Goal goal = makeDiaryGoal("Karamja", AchievementDiaryData.Tier.MEDIUM);
		assertEquals(19, goal.getTargetValue(),
			"Karamja Medium should track against the 19-task count");
		h.store().addGoal(goal);

		assertTrue(h.tracker().checkGoals(h.store().getGoals()));
		assertEquals(19, goal.getCurrentValue());
		assertTrue(goal.isComplete());
	}

	@Test
	@DisplayName("Karamja Hard partial progress doesn't complete")
	void karamjaHardPartialProgress()
	{
		// 9 of 10 tasks done — should track progress but not complete
		MockGameState state = new MockGameState()
			.varbit(net.runelite.api.gameval.VarbitID.KARAMJA_HARD_COUNT, 9);

		var h = TrackerTestHarness.forDiaries(state);
		Goal goal = makeDiaryGoal("Karamja", AchievementDiaryData.Tier.HARD);
		h.store().addGoal(goal);

		h.tracker().checkGoals(h.store().getGoals());
		assertEquals(9, goal.getCurrentValue());
		assertFalse(goal.isComplete(), "9/10 tasks should not be complete");
	}

	@Test
	@DisplayName("tracks multiple diary tiers independently")
	void tracksMultipleTiers()
	{
		MockGameState state = new MockGameState()
			.diaryComplete("Ardougne", AchievementDiaryData.Tier.EASY)
			.diaryComplete("Ardougne", AchievementDiaryData.Tier.MEDIUM);

		var h = TrackerTestHarness.forDiaries(state);
		Goal easy = makeDiaryGoal("Ardougne", AchievementDiaryData.Tier.EASY);
		Goal medium = makeDiaryGoal("Ardougne", AchievementDiaryData.Tier.MEDIUM);
		Goal hard = makeDiaryGoal("Ardougne", AchievementDiaryData.Tier.HARD);
		h.store().addGoal(easy);
		h.store().addGoal(medium);
		h.store().addGoal(hard);

		h.tracker().checkGoals(h.store().getGoals());
		assertTrue(easy.isComplete(), "easy should be complete");
		assertTrue(medium.isComplete(), "medium should be complete");
		assertFalse(hard.isComplete(), "hard should not be complete");
	}
}
