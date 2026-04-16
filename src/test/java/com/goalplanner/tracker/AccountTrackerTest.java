package com.goalplanner.tracker;

import com.goalplanner.model.AccountMetric;
import com.goalplanner.model.Goal;
import com.goalplanner.model.GoalType;
import com.goalplanner.testsupport.MockGameState;
import com.goalplanner.testsupport.TrackerTestHarness;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AccountTracker} using the MockGameState framework.
 * Covers all account metrics: quest points, combat level, total level,
 * CA points, slayer points, and museum kudos.
 */
class AccountTrackerTest
{
	private Goal makeAccountGoal(AccountMetric metric, int target)
	{
		return Goal.builder()
			.type(GoalType.ACCOUNT)
			.name(metric.getDisplayName() + " goal")
			.accountMetric(metric.name())
			.targetValue(target)
			.currentValue(0)
			.build();
	}

	@Nested
	@DisplayName("Quest Points")
	class QuestPointTests
	{
		@Test
		@DisplayName("reads quest points from VarPlayer")
		void readsQuestPoints()
		{
			MockGameState state = new MockGameState().questPoints(150);

			var h = TrackerTestHarness.forAccount(state);
			Goal goal = makeAccountGoal(AccountMetric.QUEST_POINTS, 200);
			h.store().addGoal(goal);

			assertTrue(h.tracker().checkGoals(h.store().getGoals()));
			assertEquals(150, goal.getCurrentValue());
		}

		@Test
		@DisplayName("detects QP increase between snapshots")
		void detectsQpIncrease()
		{
			MockGameState before = new MockGameState().questPoints(199);
			MockGameState after = before.copy().questPoints(200);

			var h = TrackerTestHarness.forAccount(before);
			Goal goal = makeAccountGoal(AccountMetric.QUEST_POINTS, 200);
			h.store().addGoal(goal);

			h.tracker().checkGoals(h.store().getGoals());
			assertFalse(goal.isComplete());

			h = h.withNewState(after);
			h.tracker().checkGoals(h.store().getGoals());
			assertTrue(goal.isComplete());
		}
	}

	@Nested
	@DisplayName("Combat Level")
	class CombatLevelTests
	{
		@Test
		@DisplayName("reads combat level from player")
		void readsCombatLevel()
		{
			MockGameState state = new MockGameState().combatLevel(85);

			var h = TrackerTestHarness.forAccount(state);
			Goal goal = makeAccountGoal(AccountMetric.COMBAT_LEVEL, 100);
			h.store().addGoal(goal);

			assertTrue(h.tracker().checkGoals(h.store().getGoals()));
			assertEquals(85, goal.getCurrentValue());
		}
	}

	@Nested
	@DisplayName("Total Level")
	class TotalLevelTests
	{
		@Test
		@DisplayName("reads total level from client")
		void readsTotalLevel()
		{
			MockGameState state = new MockGameState().totalLevel(1500);

			var h = TrackerTestHarness.forAccount(state);
			Goal goal = makeAccountGoal(AccountMetric.TOTAL_LEVEL, 2000);
			h.store().addGoal(goal);

			assertTrue(h.tracker().checkGoals(h.store().getGoals()));
			assertEquals(1500, goal.getCurrentValue());
		}
	}

	@Nested
	@DisplayName("CA Points")
	class CaPointTests
	{
		@Test
		@DisplayName("reads CA points from varbit")
		void readsCaPoints()
		{
			MockGameState state = new MockGameState()
				.varbit(VarbitID.CA_POINTS, 500);

			var h = TrackerTestHarness.forAccount(state);
			Goal goal = makeAccountGoal(AccountMetric.CA_POINTS, 1000);
			h.store().addGoal(goal);

			assertTrue(h.tracker().checkGoals(h.store().getGoals()));
			assertEquals(500, goal.getCurrentValue());
		}
	}

	@Nested
	@DisplayName("Slayer Points")
	class SlayerPointTests
	{
		@Test
		@DisplayName("reads slayer points from varbit")
		void readsSlayerPoints()
		{
			MockGameState state = new MockGameState()
				.varbit(VarbitID.SLAYER_POINTS, 2000);

			var h = TrackerTestHarness.forAccount(state);
			Goal goal = makeAccountGoal(AccountMetric.SLAYER_POINTS, 5000);
			h.store().addGoal(goal);

			assertTrue(h.tracker().checkGoals(h.store().getGoals()));
			assertEquals(2000, goal.getCurrentValue());
		}
	}

	@Nested
	@DisplayName("Museum Kudos")
	class KudosTests
	{
		@Test
		@DisplayName("reads kudos from varbit")
		void readsKudos()
		{
			MockGameState state = new MockGameState()
				.varbit(VarbitID.VM_KUDOS, 153);

			var h = TrackerTestHarness.forAccount(state);
			Goal goal = makeAccountGoal(AccountMetric.KUDOS, 200);
			h.store().addGoal(goal);

			assertTrue(h.tracker().checkGoals(h.store().getGoals()));
			assertEquals(153, goal.getCurrentValue());
		}
	}

	@Nested
	@DisplayName("League Points")
	class LeaguePointTests
	{
		@Test
		@DisplayName("reads lifetime league points from VarPlayer (on seasonal world)")
		void readsLeaguePoints()
		{
			MockGameState state = new MockGameState()
				.seasonal(true)
				.varp(VarPlayerID.LEAGUE_POINTS_COMPLETED, 42000);

			var h = TrackerTestHarness.forAccount(state);
			Goal goal = makeAccountGoal(AccountMetric.LEAGUE_POINTS, 50000);
			h.store().addGoal(goal);

			assertTrue(h.tracker().checkGoals(h.store().getGoals()));
			assertEquals(42000, goal.getCurrentValue());
		}
	}

	@Nested
	@DisplayName("Leagues Tasks")
	class LeagueTaskTests
	{
		@Test
		@DisplayName("reads total tasks completed from varbit (on seasonal world)")
		void readsLeagueTasks()
		{
			MockGameState state = new MockGameState()
				.seasonal(true)
				.varbit(VarbitID.LEAGUE_TOTAL_TASKS_COMPLETED, 250);

			var h = TrackerTestHarness.forAccount(state);
			Goal goal = makeAccountGoal(AccountMetric.LEAGUE_TASKS, 500);
			h.store().addGoal(goal);

			assertTrue(h.tracker().checkGoals(h.store().getGoals()));
			assertEquals(250, goal.getCurrentValue());
		}
	}

	@Test
	@DisplayName("tracks multiple metrics simultaneously")
	void tracksMultipleMetrics()
	{
		MockGameState state = new MockGameState()
			.questPoints(100)
			.combatLevel(75)
			.totalLevel(1200)
			.varbit(VarbitID.CA_POINTS, 300);

		var h = TrackerTestHarness.forAccount(state);
		Goal qp = makeAccountGoal(AccountMetric.QUEST_POINTS, 200);
		Goal combat = makeAccountGoal(AccountMetric.COMBAT_LEVEL, 126);
		Goal total = makeAccountGoal(AccountMetric.TOTAL_LEVEL, 2000);
		Goal ca = makeAccountGoal(AccountMetric.CA_POINTS, 500);
		h.store().addGoal(qp);
		h.store().addGoal(combat);
		h.store().addGoal(total);
		h.store().addGoal(ca);

		h.tracker().checkGoals(h.store().getGoals());
		assertEquals(100, qp.getCurrentValue());
		assertEquals(75, combat.getCurrentValue());
		assertEquals(1200, total.getCurrentValue());
		assertEquals(300, ca.getCurrentValue());
	}

	@Test
	@DisplayName("skips goals with null accountMetric")
	void skipsNullMetric()
	{
		MockGameState state = new MockGameState();
		var h = TrackerTestHarness.forAccount(state);

		Goal goal = Goal.builder()
			.type(GoalType.ACCOUNT)
			.name("Bad account goal")
			.accountMetric(null)
			.targetValue(100)
			.build();
		h.store().addGoal(goal);

		assertFalse(h.tracker().checkGoals(h.store().getGoals()));
	}
}
