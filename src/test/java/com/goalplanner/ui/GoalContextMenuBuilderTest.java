package com.goalplanner.ui;

import com.goalplanner.model.Goal;
import com.goalplanner.model.GoalType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure-function tests for the bulk Relations submenu's "common edge"
 * intersection rule. The rule: an edge is offered as a bulk-removable
 * common requirement (or dependent) only when every selected goal
 * carries it.
 */
class GoalContextMenuBuilderTest
{
	@Nested
	@DisplayName("commonEdges (Remove Requirement intersection rule)")
	class CommonRequirements
	{
		// Scenario the user described: A and B both depend on C; D does not.
		// "Depends on C" here means A and B's requirement lists contain C.
		private final Goal goalA = goal("A");
		private final Goal goalB = goal("B");
		private final Goal goalC = goal("C");
		private final Goal goalD = goal("D");

		private final Map<String, List<String>> requirements = new HashMap<>();

		CommonRequirements()
		{
			requirements.put("A", Collections.singletonList("C"));
			requirements.put("B", Collections.singletonList("C"));
			requirements.put("C", Collections.emptyList());
			requirements.put("D", Collections.emptyList());
		}

		private final Function<String, List<String>> reqReader = id ->
			requirements.getOrDefault(id, Collections.emptyList());

		@Test
		@DisplayName("single A → [C] (C is removable)")
		void singleADependsOnC()
		{
			List<String> common = GoalContextMenuBuilder.commonEdges(
				reqReader, Collections.singletonList(goalA));
			assertEquals(Collections.singletonList("C"), common);
		}

		@Test
		@DisplayName("single B → [C] (C is removable)")
		void singleBDependsOnC()
		{
			List<String> common = GoalContextMenuBuilder.commonEdges(
				reqReader, Collections.singletonList(goalB));
			assertEquals(Collections.singletonList("C"), common);
		}

		@Test
		@DisplayName("bulk A+B → [C] (both share C, so it's removable)")
		void bulkABShareC()
		{
			List<String> common = GoalContextMenuBuilder.commonEdges(
				reqReader, Arrays.asList(goalA, goalB));
			assertEquals(Collections.singletonList("C"), common);
		}

		@Test
		@DisplayName("bulk A+D → [] (D doesn't depend on C, so option hidden)")
		void bulkADDoesNotShareC()
		{
			List<String> common = GoalContextMenuBuilder.commonEdges(
				reqReader, Arrays.asList(goalA, goalD));
			assertTrue(common.isEmpty(),
				"D doesn't share A's requirement on C, so the intersection must be empty");
		}

		@Test
		@DisplayName("empty selection → empty list")
		void emptySelection()
		{
			List<String> common = GoalContextMenuBuilder.commonEdges(
				reqReader, Collections.emptyList());
			assertTrue(common.isEmpty());
		}

		@Test
		@DisplayName("bulk A+B+D → [] (one mismatch is enough to drop the edge)")
		void oneMismatchKillsTheCommon()
		{
			List<String> common = GoalContextMenuBuilder.commonEdges(
				reqReader, Arrays.asList(goalA, goalB, goalD));
			assertTrue(common.isEmpty());
		}
	}

	@Nested
	@DisplayName("commonEdges (multiple shared edges)")
	class MultipleShared
	{
		@Test
		@DisplayName("two requirements common to both → both returned")
		void twoCommonRequirements()
		{
			Goal a = goal("A");
			Goal b = goal("B");
			Map<String, List<String>> reqs = new HashMap<>();
			reqs.put("A", Arrays.asList("X", "Y", "Z"));
			reqs.put("B", Arrays.asList("X", "Y", "W"));
			Function<String, List<String>> reader = id ->
				reqs.getOrDefault(id, Collections.emptyList());

			List<String> common = GoalContextMenuBuilder.commonEdges(
				reader, Arrays.asList(a, b));

			// X and Y appear in both; Z only in A; W only in B.
			assertEquals(Arrays.asList("X", "Y"), common);
		}
	}

	@Nested
	@DisplayName("planMoveUp (preserves relative order)")
	class PlanMoveUp
	{
		@Test
		@DisplayName("non-contiguous selection — each shifts up by 1")
		void nonContiguous()
		{
			// section [_,A,_,B,_,C,_], selection {A,B,C} at sectionRel 1,3,5
			List<Integer> targets = GoalContextMenuBuilder.planMoveUp(
				Arrays.asList(1, 3, 5), 7);
			assertEquals(Arrays.asList(0, 2, 4), targets);
		}

		@Test
		@DisplayName("selection already at top — clamped, no movement")
		void allAtTop()
		{
			// section [A,B,C,_,_], selection {A,B,C} at 0,1,2
			List<Integer> targets = GoalContextMenuBuilder.planMoveUp(
				Arrays.asList(0, 1, 2), 5);
			assertEquals(Arrays.asList(0, 1, 2), targets);
		}

		@Test
		@DisplayName("partial top — leading element clamped, others advance")
		void partialTopClamp()
		{
			// section [A,B,_,_,C], selection {A,B,C} at 0,1,4
			// A clamps at 0; B clamps at 1; C moves from 4 → 3.
			List<Integer> targets = GoalContextMenuBuilder.planMoveUp(
				Arrays.asList(0, 1, 4), 5);
			assertEquals(Arrays.asList(0, 1, 3), targets);
		}

		@Test
		@DisplayName("adjacent block — whole block shifts up by 1")
		void adjacentBlock()
		{
			// section [_,A,B,C,_], selection {A,B,C} at 1,2,3
			List<Integer> targets = GoalContextMenuBuilder.planMoveUp(
				Arrays.asList(1, 2, 3), 5);
			assertEquals(Arrays.asList(0, 1, 2), targets);
		}
	}

	@Nested
	@DisplayName("planMoveDown (preserves relative order)")
	class PlanMoveDown
	{
		@Test
		@DisplayName("non-contiguous — each shifts down by 1")
		void nonContiguous()
		{
			// section [_,A,_,B,_,C,_], selection {A,B,C} at 1,3,5; size=7
			List<Integer> targets = GoalContextMenuBuilder.planMoveDown(
				Arrays.asList(1, 3, 5), 7);
			assertEquals(Arrays.asList(2, 4, 6), targets);
		}

		@Test
		@DisplayName("selection already at bottom — clamped, no movement")
		void allAtBottom()
		{
			// section [_,_,A,B,C], selection {A,B,C} at 2,3,4; size=5
			List<Integer> targets = GoalContextMenuBuilder.planMoveDown(
				Arrays.asList(2, 3, 4), 5);
			assertEquals(Arrays.asList(2, 3, 4), targets);
		}

		@Test
		@DisplayName("partial bottom — trailing element clamped, others advance")
		void partialBottomClamp()
		{
			// section [A,_,_,B,C], selection {A,B,C} at 0,3,4; size=5
			// C clamps at 4; B clamps at 3; A moves from 0 → 1.
			List<Integer> targets = GoalContextMenuBuilder.planMoveDown(
				Arrays.asList(0, 3, 4), 5);
			assertEquals(Arrays.asList(1, 3, 4), targets);
		}

		@Test
		@DisplayName("adjacent block — whole block shifts down by 1")
		void adjacentBlock()
		{
			// section [_,A,B,C,_], selection {A,B,C} at 1,2,3; size=5
			List<Integer> targets = GoalContextMenuBuilder.planMoveDown(
				Arrays.asList(1, 2, 3), 5);
			assertEquals(Arrays.asList(2, 3, 4), targets);
		}
	}

	private static Goal goal(String id)
	{
		Goal g = Goal.builder()
			.type(GoalType.CUSTOM)
			.name(id)
			.build();
		g.setId(id);
		return g;
	}
}
