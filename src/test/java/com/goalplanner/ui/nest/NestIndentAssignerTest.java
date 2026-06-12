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
	@DisplayName("resolveVisiblePrereqs follows a chain through a hidden (completed) goal")
	void resolveVisiblePrereqsBridgesHiddenNode()
	{
		// a requires b, b requires c; b is hidden (completed) → a's visible prereq is c.
		java.util.Map<String, List<String>> edges = new java.util.HashMap<>();
		edges.put("a", Arrays.asList("b"));
		edges.put("b", Arrays.asList("c"));
		edges.put("c", new ArrayList<>());
		java.util.Set<String> hidden = new java.util.HashSet<>(Arrays.asList("b"));

		List<String> visible = NestIndentAssigner.resolveVisiblePrereqs(
			edges.get("a"), edges::get, hidden::contains);
		assertEquals(Arrays.asList("c"), visible);
	}

	@Test
	@DisplayName("resolveVisiblePrereqs keeps visible prereqs, drops fully-hidden chains, survives cycles")
	void resolveVisiblePrereqsMixedAndCycleSafe()
	{
		// a requires [b(hidden), d]; b requires c(hidden); c requires b (cycle) → just d.
		java.util.Map<String, List<String>> edges = new java.util.HashMap<>();
		edges.put("a", Arrays.asList("b", "d"));
		edges.put("b", Arrays.asList("c"));
		edges.put("c", Arrays.asList("b"));
		edges.put("d", new ArrayList<>());
		java.util.Set<String> hidden = new java.util.HashSet<>(Arrays.asList("b", "c"));

		List<String> visible = NestIndentAssigner.resolveVisiblePrereqs(
			edges.get("a"), edges::get, hidden::contains);
		assertEquals(Arrays.asList("d"), visible);
	}

	@Test
	@DisplayName("a chain through a completed goal still nests the dependent under its grandparent")
	void chainThroughCompletedGoalKeepsNesting()
	{
		// Section: c (incomplete), b (completed, requires c), a (requires b).
		// The completed b is excluded from the outline; expanding a's prereqs
		// through b must yield c, so a nests under c instead of flattening.
		java.util.Map<String, List<String>> edges = new java.util.HashMap<>();
		edges.put("c", new ArrayList<>());
		edges.put("b", Arrays.asList("c"));
		edges.put("a", Arrays.asList("b"));
		java.util.Set<String> completed = new java.util.HashSet<>(Arrays.asList("b"));

		List<NestIndentAssigner.Node> nodes = new ArrayList<>();
		for (String id : Arrays.asList("c", "a")) // topo order, completed excluded
		{
			nodes.add(new NestIndentAssigner.Node(id, NestIndentAssigner.resolveVisiblePrereqs(
				edges.get(id), edges::get, completed::contains)));
		}
		NestIndentAssigner.Result r = NestIndentAssigner.assign(nodes);
		assertEquals(Arrays.asList("c", "a"), r.ordered);
		assertEquals(0, r.level.get("c"));
		assertEquals(1, r.level.get("a"));
		assertEquals("c", r.primaryParent.get("a"));
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
