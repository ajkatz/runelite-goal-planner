package com.goalplanner.ui.nest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("NestIndentAssigner — outline-tree nesting from dependencies")
class NestIndentAssignerTest
{
	private static NestIndentAssigner.Node node(String id, String... prereqs)
	{
		return new NestIndentAssigner.Node(id, new ArrayList<>(Arrays.asList(prereqs)));
	}

	@Test
	@DisplayName("a goal with no in-section prerequisites is a root at level 0")
	void rootsAreLevelZero()
	{
		NestIndentAssigner.Result r = NestIndentAssigner.assign(Arrays.asList(
			node("a"), node("b")));
		assertEquals(0, r.level.get("a"));
		assertEquals(0, r.level.get("b"));
		assertNull(r.primaryParent.get("a"));
	}

	@Test
	@DisplayName("a straight chain nests each goal under the one it needs")
	void chainNestsUnderParent()
	{
		NestIndentAssigner.Result r = NestIndentAssigner.assign(Arrays.asList(
			node("a"), node("b", "a"), node("c", "b")));
		assertEquals(Arrays.asList("a", "b", "c"), r.ordered);
		assertEquals(0, r.level.get("a"));
		assertEquals(1, r.level.get("b"));
		assertEquals(2, r.level.get("c"));
		assertEquals("a", r.primaryParent.get("b"));
		assertEquals("b", r.primaryParent.get("c"));
	}

	@Test
	@DisplayName("a child is emitted immediately after its parent (pre-order)")
	void preOrderPlacesChildUnderParent()
	{
		// two independent roots, each with a child — children follow their own parent
		NestIndentAssigner.Result r = NestIndentAssigner.assign(Arrays.asList(
			node("root1"), node("root2"), node("child1", "root1"), node("child2", "root2")));
		assertEquals(Arrays.asList("root1", "child1", "root2", "child2"), r.ordered);
	}

	@Test
	@DisplayName("a diamond goal nests under its deepest prerequisite; the rest are 'extra'")
	void diamondNestsUnderPrimaryAndCountsExtras()
	{
		NestIndentAssigner.Result r = NestIndentAssigner.assign(Arrays.asList(
			node("agility"), node("combat"), node("ranged"),
			node("mm2", "agility", "combat"),
			node("dt2", "combat", "ranged"),
			node("sote", "agility", "ranged"),
			node("qpc", "mm2", "dt2", "sote"),
			node("fire"), node("inferno"),
			node("gm", "qpc", "fire", "inferno")));

		// Quest Cape's three prereqs are all level 1 → primary is the first (mm2),
		// the other two are extras. It sits one level past mm2.
		assertEquals("mm2", r.primaryParent.get("qpc"));
		assertEquals(2, r.level.get("qpc"));
		assertEquals(2, r.extraPrereqs.get("qpc"));

		// Grandmaster nests under Quest Cape (depth 2), not Fire/Infernal (depth 0).
		assertEquals("qpc", r.primaryParent.get("gm"));
		assertEquals(3, r.level.get("gm"));
		assertEquals(2, r.extraPrereqs.get("gm")); // fire + inferno

		// Spine reads parent-then-child: agility → mm2 → qpc → gm.
		List<String> o = r.ordered;
		assertEquals(true, o.indexOf("agility") < o.indexOf("mm2"));
		assertEquals(true, o.indexOf("mm2") < o.indexOf("qpc"));
		assertEquals(true, o.indexOf("qpc") < o.indexOf("gm"));
	}

	@Test
	@DisplayName("OR prerequisites nest just like AND prerequisites")
	void orPrereqsNestToo()
	{
		NestIndentAssigner.Result r = NestIndentAssigner.assign(Arrays.asList(
			node("a"), node("b", "a"), node("gm", "b")));
		assertEquals(2, r.level.get("gm"));
		assertEquals("b", r.primaryParent.get("gm"));
	}

	@Test
	@DisplayName("prerequisites not present in the section are ignored")
	void crossSectionPrereqsIgnored()
	{
		NestIndentAssigner.Result r = NestIndentAssigner.assign(Arrays.asList(
			node("b", "external")));
		assertEquals(0, r.level.get("b"));
		assertNull(r.primaryParent.get("b"));
		assertEquals(0, r.extraPrereqs.get("b"));
	}

	@Test
	@DisplayName("every input goal appears exactly once in the order")
	void everyGoalEmittedOnce()
	{
		NestIndentAssigner.Result r = NestIndentAssigner.assign(Arrays.asList(
			node("a"), node("b", "a"), node("c", "a", "b")));
		assertEquals(3, r.ordered.size());
		assertEquals(3, r.level.size());
	}
}
