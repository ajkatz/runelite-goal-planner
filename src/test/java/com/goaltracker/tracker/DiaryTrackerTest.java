package com.goaltracker.tracker;

import com.goaltracker.data.AchievementDiaryData;
import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalType;
import com.goaltracker.testsupport.MockGameState;
import com.goaltracker.testsupport.TrackerTestHarness;
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
		int varbitId = AchievementDiaryData.completionVarbit(area, tier);
		return Goal.builder()
			.type(GoalType.DIARY)
			.name(area + " " + tier.getDisplayName() + " Diary")
			.varbitId(varbitId)
			.targetValue(1)
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

		// Simulate a Karamja Easy diary goal (no varbit exposed)
		Goal goal = Goal.builder()
			.type(GoalType.DIARY)
			.name("Karamja Easy Diary")
			.varbitId(0) // no completion varbit
			.targetValue(1)
			.currentValue(0)
			.build();
		h.store().addGoal(goal);

		assertFalse(h.tracker().checkGoals(h.store().getGoals()),
			"goals with varbitId <= 0 should be skipped");
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
