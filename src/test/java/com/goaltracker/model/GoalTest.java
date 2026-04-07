package com.goaltracker.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class GoalTest
{
	@Test
	public void testProgressPercentZeroTarget()
	{
		Goal goal = Goal.builder().type(GoalType.CUSTOM).name("Test").targetValue(0).build();
		assertEquals(0.0, goal.getProgressPercent(), 0.01);
	}

	@Test
	public void testProgressPercentCompleteCustom()
	{
		// Mission 11+: completion is canonically tracked by completedAt > 0,
		// not status. Build the goal with both for clarity.
		Goal goal = Goal.builder().type(GoalType.CUSTOM).name("Test").targetValue(0)
			.status(GoalStatus.COMPLETE).completedAt(System.currentTimeMillis()).build();
		assertEquals(100.0, goal.getProgressPercent(), 0.01);
	}

	@Test
	public void testProgressPercentPartial()
	{
		Goal goal = Goal.builder().type(GoalType.SKILL).name("Attack").targetValue(1000)
			.currentValue(500).build();
		assertEquals(50.0, goal.getProgressPercent(), 0.01);
	}

	@Test
	public void testProgressPercentOver100()
	{
		Goal goal = Goal.builder().type(GoalType.ITEM_GRIND).name("Coal").targetValue(100)
			.currentValue(200).build();
		assertEquals(100.0, goal.getProgressPercent(), 0.01);
	}

	@Test
	public void testIsCompleteByCompletedAt()
	{
		// Mission 11+: completion is iff completedAt > 0. The tracker's
		// recordGoalProgress sets this when meetsTarget() flips true.
		Goal goal = Goal.builder().type(GoalType.SKILL).name("Mining").targetValue(100)
			.currentValue(100).completedAt(System.currentTimeMillis()).build();
		assertTrue(goal.isComplete());
	}

	@Test
	public void testMeetsTargetWithoutCompletedAt()
	{
		// A goal that meets its target but hasn't been stamped (e.g. before the
		// next tracker tick) is NOT considered complete. meetsTarget and
		// isComplete are intentionally separate concerns.
		Goal goal = Goal.builder().type(GoalType.SKILL).name("Mining").targetValue(100)
			.currentValue(100).build();
		assertTrue(goal.meetsTarget());
		assertFalse(goal.isComplete());
	}

	@Test
	public void testIsCompleteByCompletedAtCustom()
	{
		// Custom goals are marked complete via the API which stamps completedAt.
		Goal goal = Goal.builder().type(GoalType.CUSTOM).name("Test")
			.status(GoalStatus.COMPLETE).completedAt(System.currentTimeMillis()).build();
		assertTrue(goal.isComplete());
	}

	@Test
	public void testIsNotComplete()
	{
		Goal goal = Goal.builder().type(GoalType.SKILL).name("Mining").targetValue(100)
			.currentValue(50).build();
		assertFalse(goal.isComplete());
	}

	@Test
	public void testUnscannedItemGoal()
	{
		Goal goal = Goal.builder().type(GoalType.ITEM_GRIND).name("Coal").targetValue(1000)
			.currentValue(-1).build();
		assertFalse(goal.isComplete());
		// -1 should result in 0% progress (clamped)
		assertEquals(0.0, goal.getProgressPercent(), 0.01);
	}

	@Test
	public void testDefaultId()
	{
		Goal goal = Goal.builder().type(GoalType.CUSTOM).name("Test").build();
		assertNotNull(goal.getId());
		assertFalse(goal.getId().isEmpty());
	}

	@Test
	public void testDefaultStatus()
	{
		Goal goal = Goal.builder().type(GoalType.CUSTOM).name("Test").build();
		assertEquals(GoalStatus.ACTIVE, goal.getStatus());
	}
}
