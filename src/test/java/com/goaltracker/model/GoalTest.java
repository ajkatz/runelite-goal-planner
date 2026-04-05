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
		Goal goal = Goal.builder().type(GoalType.CUSTOM).name("Test").targetValue(0)
			.status(GoalStatus.COMPLETE).build();
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
	public void testIsCompleteByValue()
	{
		Goal goal = Goal.builder().type(GoalType.SKILL).name("Mining").targetValue(100)
			.currentValue(100).build();
		assertTrue(goal.isComplete());
	}

	@Test
	public void testIsCompleteByStatus()
	{
		Goal goal = Goal.builder().type(GoalType.CUSTOM).name("Test")
			.status(GoalStatus.COMPLETE).build();
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
