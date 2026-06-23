package com.goalplanner.tracker;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MiscellaniaAutoGoal.shouldAdd — when to auto-add a 100% approval goal")
class MiscellaniaAutoGoalTest
{
	private static boolean add(int prev, int cur, int increases, boolean enabled, boolean hasGoal)
	{
		return MiscellaniaAutoGoal.shouldAdd(prev, cur, increases, enabled, hasGoal);
	}

	@Test
	@DisplayName("fires on the 2nd+ increase (a real favour gain), not the 1st")
	void firesOnSecondIncrease()
	{
		assertFalse(add(50, 60, 1, true, false)); // 1st increase = login data sync
		assertTrue(add(50, 60, 2, true, false));  // 2nd increase = real gain
		assertTrue(add(50, 51, 3, true, false));  // later gains keep firing (re-add)
	}

	@Test
	@DisplayName("does not fire on the login baseline read (previous == -1)")
	void noFireOnBaseline()
	{
		assertFalse(add(-1, 127, 0, true, false));
		assertFalse(add(-1, 0, 0, true, false));
	}

	@Test
	@DisplayName("does not fire without an increase (same or lower)")
	void noFireWithoutIncrease()
	{
		assertFalse(add(60, 60, 2, true, false));
		assertFalse(add(60, 50, 2, true, false));
	}

	@Test
	@DisplayName("respects the toggle and an existing goal, but re-adds whenever missing")
	void respectsGuards()
	{
		assertFalse(add(50, 60, 2, false, false)); // toggle off
		assertFalse(add(50, 60, 2, true, true));   // a Misc. Approval goal already exists
		assertTrue(add(50, 60, 2, true, false));   // missing + 2nd increase -> re-add
	}

	@Test
	@DisplayName("FULL_APPROVAL is 127 (100%)")
	void fullApprovalConstant()
	{
		assertTrue(MiscellaniaAutoGoal.FULL_APPROVAL == 127);
	}
}
