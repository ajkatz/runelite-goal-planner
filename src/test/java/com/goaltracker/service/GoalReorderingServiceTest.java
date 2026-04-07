package com.goaltracker.service;

import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalStatus;
import com.goaltracker.model.GoalType;
import com.goaltracker.persistence.GoalStore;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for GoalReorderingService.
 * Uses a TestGoalStore that doesn't need ConfigManager.
 */
public class GoalReorderingServiceTest
{
	private TestGoalStore goalStore;
	private GoalReorderingService service;

	@Before
	public void setUp()
	{
		goalStore = new TestGoalStore();
		service = new GoalReorderingService(goalStore);
	}

	// ---- moveGoal: basic movement ----

	@Test
	public void testMoveGoalDown()
	{
		goalStore.addGoal(makeSkill("Prayer", 99));
		goalStore.addGoal(makeSkill("Attack", 94));
		goalStore.addGoal(makeCustom("Custom goal"));

		service.moveGoal(0, 1); // Move Prayer down

		List<Goal> goals = goalStore.getGoals();
		assertEquals("Attack", goals.get(0).getSkillName());
		assertEquals("Prayer", goals.get(1).getSkillName());
	}

	@Test
	public void testMoveGoalUp()
	{
		goalStore.addGoal(makeCustom("First"));
		goalStore.addGoal(makeCustom("Second"));
		goalStore.addGoal(makeCustom("Third"));

		service.moveGoal(2, 1); // Move Third up

		assertEquals("Third", goalStore.getGoals().get(1).getName());
		assertEquals("Second", goalStore.getGoals().get(2).getName());
	}

	// ---- moveGoal: skill chain behavior ----

	@Test
	public void testMoveSkillDownPastPartner()
	{
		// [Prayer99, Attack94, Attack99, Ranged99]
		goalStore.addGoal(makeSkill("Prayer", xpFor(99)));
		goalStore.addGoal(makeSkill("Attack", xpFor(94)));
		goalStore.addGoal(makeSkill("Attack", xpFor(99)));
		goalStore.addGoal(makeSkill("Ranged", xpFor(99)));

		// Click DOWN on Attack94 — should push Attack99 down first, then move Attack94
		service.moveGoal(1, 2);

		List<Goal> goals = goalStore.getGoals();
		// Expected: [Prayer99, Ranged99, Attack94, Attack99]
		assertEquals("Prayer", goals.get(0).getSkillName());
		assertEquals("Ranged", goals.get(1).getSkillName());
		assertEquals("Attack", goals.get(2).getSkillName());
		assertTrue(goals.get(2).getTargetValue() < goals.get(3).getTargetValue());
		assertEquals("Attack", goals.get(3).getSkillName());
	}

	@Test
	public void testMoveSkillUpPastPartner()
	{
		// [Prayer99, Attack94, Attack99, Ranged99]
		goalStore.addGoal(makeSkill("Prayer", xpFor(99)));
		goalStore.addGoal(makeSkill("Attack", xpFor(94)));
		goalStore.addGoal(makeSkill("Attack", xpFor(99)));
		goalStore.addGoal(makeSkill("Ranged", xpFor(99)));

		// Click UP on Attack99 — should push Attack94 up first, then move Attack99
		service.moveGoal(2, 1);

		List<Goal> goals = goalStore.getGoals();
		// Expected: [Attack94, Attack99, Prayer99, Ranged99]
		assertEquals("Attack", goals.get(0).getSkillName());
		assertEquals("Attack", goals.get(1).getSkillName());
		assertTrue(goals.get(0).getTargetValue() < goals.get(1).getTargetValue());
		assertEquals("Prayer", goals.get(2).getSkillName());
	}

	@Test
	public void testMoveUnrelatedGoalThroughChain()
	{
		// [Attack94, Attack99, Prayer99]
		goalStore.addGoal(makeSkill("Attack", xpFor(94)));
		goalStore.addGoal(makeSkill("Attack", xpFor(99)));
		goalStore.addGoal(makeSkill("Prayer", xpFor(99)));

		// Move Prayer up — should just swap with Attack99 (unrelated)
		service.moveGoal(2, 1);

		List<Goal> goals = goalStore.getGoals();
		assertEquals("Attack", goals.get(0).getSkillName());
		assertEquals("Prayer", goals.get(1).getSkillName());
		assertEquals("Attack", goals.get(2).getSkillName());
	}

	// ---- moveGoalTo: direct placement ----

	@Test
	public void testMoveToTopEnforcesOrdering()
	{
		goalStore.addGoal(makeSkill("Prayer", xpFor(99)));
		goalStore.addGoal(makeSkill("Attack", xpFor(94)));
		goalStore.addGoal(makeSkill("Attack", xpFor(99)));

		// Move Attack99 to top — should enforce Attack94 above Attack99
		service.moveGoalTo(2, 0);

		List<Goal> goals = goalStore.getGoals();
		// Attack94 must be above Attack99
		int idx94 = -1, idx99 = -1;
		for (int i = 0; i < goals.size(); i++)
		{
			if ("Attack".equals(goals.get(i).getSkillName()))
			{
				if (goals.get(i).getTargetValue() == xpFor(94)) idx94 = i;
				if (goals.get(i).getTargetValue() == xpFor(99)) idx99 = i;
			}
		}
		assertTrue("94 Attack should be above 99 Attack", idx94 < idx99);
	}

	// ---- findInsertionIndex ----

	@Test
	public void testFindInsertionIndex()
	{
		goalStore.addGoal(makeSkill("Prayer", xpFor(99)));
		goalStore.addGoal(makeSkill("Attack", xpFor(99)));

		// Adding Attack94 should go before Attack99 — same section as the
		// existing goals (Incomplete, the default for goalStore.addGoal).
		String sectionId = goalStore.getIncompleteSection().getId();
		int idx = service.findInsertionIndex("Attack", xpFor(94), sectionId);
		assertEquals(1, idx);
	}

	@Test
	public void testFindInsertionIndexNoMatch()
	{
		goalStore.addGoal(makeSkill("Prayer", xpFor(99)));

		// No existing Attack goals in the Incomplete section
		String sectionId = goalStore.getIncompleteSection().getId();
		int idx = service.findInsertionIndex("Attack", xpFor(94), sectionId);
		assertEquals(-1, idx);
	}

	// ---- Helpers ----

	private static int xpFor(int level)
	{
		// Simplified XP values for testing
		if (level == 94) return 8_771_558;
		if (level == 99) return 13_034_431;
		return level * 100_000;
	}

	private static Goal makeSkill(String skillName, int targetXp)
	{
		return Goal.builder()
			.type(GoalType.SKILL)
			.name(skillName + " goal")
			.skillName(skillName)
			.targetValue(targetXp)
			.build();
	}

	private static Goal makeCustom(String name)
	{
		return Goal.builder()
			.type(GoalType.CUSTOM)
			.name(name)
			.targetValue(1)
			.build();
	}

	/**
	 * Simple GoalStore for testing that doesn't need ConfigManager.
	 */
	private static class TestGoalStore extends GoalStore
	{
		private final java.util.List<Goal> goals = new java.util.ArrayList<>();

		public TestGoalStore()
		{
			super(null); // no ConfigManager needed
		}

		@Override
		public void load() {}

		@Override
		public void save() {}

		@Override
		public java.util.List<Goal> getGoals()
		{
			return goals;
		}

		@Override
		public void addGoal(Goal goal)
		{
			// Mirror real GoalStore.addGoal: drop into Incomplete by default so
			// section-scoped reordering logic has a section to operate on.
			if (goal.getSectionId() == null)
			{
				goal.setSectionId(getIncompleteSection().getId());
			}
			goal.setPriority(goals.size());
			goals.add(goal);
		}

		@Override
		public void removeGoal(String goalId)
		{
			goals.removeIf(g -> g.getId().equals(goalId));
		}

		@Override
		public void reorder(int fromIndex, int toIndex)
		{
			if (fromIndex < 0 || fromIndex >= goals.size() || toIndex < 0 || toIndex >= goals.size())
			{
				return;
			}
			Goal moved = goals.remove(fromIndex);
			goals.add(toIndex, moved);
		}
	}
}
