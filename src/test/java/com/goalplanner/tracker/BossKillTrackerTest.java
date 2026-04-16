package com.goalplanner.tracker;

import com.goalplanner.model.Goal;
import com.goalplanner.model.GoalType;
import com.goalplanner.testsupport.MockGameState;
import com.goalplanner.testsupport.TrackerTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link BossKillTracker} using the MockGameState framework.
 * Validates that the tracker correctly reads VarPlayer kill counts
 * and records goal progress.
 */
class BossKillTrackerTest
{
	private Goal makeBossGoal(String bossName, int targetKills)
	{
		return Goal.builder()
			.type(GoalType.BOSS)
			.name(bossName + " " + targetKills + "kc")
			.bossName(bossName)
			.targetValue(targetKills)
			.currentValue(0)
			.build();
	}

	@Test
	@DisplayName("reads kill count from varp and records progress")
	void readsKillCountFromVarp()
	{
		MockGameState state = new MockGameState()
			.bossKills("Zulrah", 150);

		TrackerTestHarness<BossKillTracker> h = TrackerTestHarness.forBossKills(state);
		Goal goal = makeBossGoal("Zulrah", 500);
		h.store().addGoal(goal);

		assertTrue(h.tracker().checkGoals(h.store().getGoals()),
			"tracker should report change from 0 → 150");
		assertEquals(150, goal.getCurrentValue());
		assertFalse(goal.isComplete());
	}

	@Test
	@DisplayName("detects kill count increase between two state snapshots")
	void detectsKillCountIncrease()
	{
		MockGameState before = new MockGameState().bossKills("Zulrah", 499);
		MockGameState after = before.copy().bossKills("Zulrah", 500);

		TrackerTestHarness<BossKillTracker> h = TrackerTestHarness.forBossKills(before);
		Goal goal = makeBossGoal("Zulrah", 500);
		h.store().addGoal(goal);

		// Before: 499 kills
		h.tracker().checkGoals(h.store().getGoals());
		assertEquals(499, goal.getCurrentValue());
		assertFalse(goal.isComplete());

		// After: 500 kills — swap to new state, same store
		h = h.withNewState(after);
		h.tracker().checkGoals(h.store().getGoals());
		assertEquals(500, goal.getCurrentValue());
		assertTrue(goal.isComplete(), "goal should be complete at target KC");
	}

	@Test
	@DisplayName("no-op when kill count unchanged between checks")
	void noOpWhenUnchanged()
	{
		MockGameState state = new MockGameState().bossKills("Vorkath", 42);

		TrackerTestHarness<BossKillTracker> h = TrackerTestHarness.forBossKills(state);
		Goal goal = makeBossGoal("Vorkath", 100);
		h.store().addGoal(goal);

		assertTrue(h.tracker().checkGoals(h.store().getGoals()),
			"first check should detect change from 0 → 42");
		assertFalse(h.tracker().checkGoals(h.store().getGoals()),
			"second check with same state should be no-op");
	}

	@Test
	@DisplayName("skips goals with unknown boss name")
	void skipsUnknownBoss()
	{
		MockGameState state = new MockGameState();
		TrackerTestHarness<BossKillTracker> h = TrackerTestHarness.forBossKills(state);

		Goal goal = Goal.builder()
			.type(GoalType.BOSS)
			.name("FakeBoss 100kc")
			.bossName("FakeBoss")
			.targetValue(100)
			.build();
		h.store().addGoal(goal);

		assertFalse(h.tracker().checkGoals(h.store().getGoals()),
			"unknown boss should be skipped");
		assertEquals(0, goal.getCurrentValue());
	}

	@Test
	@DisplayName("skips goals with null bossName")
	void skipsNullBossName()
	{
		MockGameState state = new MockGameState();
		TrackerTestHarness<BossKillTracker> h = TrackerTestHarness.forBossKills(state);

		Goal goal = Goal.builder()
			.type(GoalType.BOSS)
			.name("No boss")
			.bossName(null)
			.targetValue(100)
			.build();
		h.store().addGoal(goal);

		assertFalse(h.tracker().checkGoals(h.store().getGoals()));
	}

	@Test
	@DisplayName("tracks multiple bosses independently")
	void tracksMultipleBosses()
	{
		MockGameState state = new MockGameState()
			.bossKills("Zulrah", 150)
			.bossKills("Vorkath", 300)
			.bossKills("CoX", 50);

		TrackerTestHarness<BossKillTracker> h = TrackerTestHarness.forBossKills(state);

		Goal zulrah = makeBossGoal("Zulrah", 500);
		Goal vorkath = makeBossGoal("Vorkath", 1000);
		Goal cox = makeBossGoal("CoX", 100);
		h.store().addGoal(zulrah);
		h.store().addGoal(vorkath);
		h.store().addGoal(cox);

		assertTrue(h.tracker().checkGoals(h.store().getGoals()));
		assertEquals(150, zulrah.getCurrentValue());
		assertEquals(300, vorkath.getCurrentValue());
		assertEquals(50, cox.getCurrentValue());
	}

	@Test
	@DisplayName("goal completes when KC equals target")
	void goalCompletesAtTarget()
	{
		MockGameState state = new MockGameState().bossKills("Giant Mole", 1);

		TrackerTestHarness<BossKillTracker> h = TrackerTestHarness.forBossKills(state);
		Goal goal = makeBossGoal("Giant Mole", 1);
		h.store().addGoal(goal);

		h.tracker().checkGoals(h.store().getGoals());
		assertEquals(1, goal.getCurrentValue());
		assertTrue(goal.isComplete());
	}

	@Test
	@DisplayName("copy-modify pattern preserves original state")
	void copyPreservesOriginal()
	{
		MockGameState original = new MockGameState()
			.bossKills("Zulrah", 100)
			.bossKills("Vorkath", 200);

		MockGameState modified = original.copy()
			.bossKills("Zulrah", 200);

		// Verify original is untouched by creating two harnesses
		TrackerTestHarness<BossKillTracker> hOrig = TrackerTestHarness.forBossKills(original);
		TrackerTestHarness<BossKillTracker> hMod = TrackerTestHarness.forBossKills(modified);

		Goal goalOrig = makeBossGoal("Zulrah", 500);
		Goal goalMod = makeBossGoal("Zulrah", 500);
		hOrig.store().addGoal(goalOrig);
		hMod.store().addGoal(goalMod);

		hOrig.tracker().checkGoals(hOrig.store().getGoals());
		hMod.tracker().checkGoals(hMod.store().getGoals());

		assertEquals(100, goalOrig.getCurrentValue(), "original state should be 100");
		assertEquals(200, goalMod.getCurrentValue(), "modified copy should be 200");
	}
}
