package com.goalplanner.ui.rail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RailLaneAssigner — git-graph lane assignment for the dependency rail")
class RailLaneAssignerTest
{
	private static RailLaneAssigner.Node node(String id, String... prereqs)
	{
		return new RailLaneAssigner.Node(id, new ArrayList<>(Arrays.asList(prereqs)));
	}

	@Test
	@DisplayName("empty graph yields no lanes and maxLane 0")
	void emptyGraph()
	{
		RailLaneAssigner.Result r = RailLaneAssigner.assign(new ArrayList<>());
		assertTrue(r.lane.isEmpty());
		assertEquals(0, r.maxLane);
	}

	@Test
	@DisplayName("a linear chain stays in a single lane (depth does not widen the gutter)")
	void linearChainSingleLane()
	{
		List<RailLaneAssigner.Node> nodes = Arrays.asList(
			node("a"),
			node("b", "a"),
			node("c", "b"));
		RailLaneAssigner.Result r = RailLaneAssigner.assign(nodes);
		assertEquals(0, r.maxLane, "a 3-deep chain must occupy one lane");
		assertEquals(0, (int) r.lane.get("a"));
		assertEquals(0, (int) r.lane.get("b"));
		assertEquals(0, (int) r.lane.get("c"));
	}

	@Test
	@DisplayName("independent roots each get their own lane while both stay live")
	void independentRootsFanOut()
	{
		// Two roots with no shared release point both depend on by a later merge,
		// so both lanes are held simultaneously → 2 lanes.
		List<RailLaneAssigner.Node> nodes = Arrays.asList(
			node("a"),
			node("b"),
			node("merge", "a", "b"));
		RailLaneAssigner.Result r = RailLaneAssigner.assign(nodes);
		assertEquals(1, r.maxLane, "two live roots need two lanes");
		assertEquals(0, (int) r.lane.get("a"));
		assertEquals(1, (int) r.lane.get("b"));
	}

	@Test
	@DisplayName("a diamond reuses lanes: shared prereq frees its lane for the convergence")
	void diamondReusesLanes()
	{
		// a -> b, a -> c, {b,c} -> d
		List<RailLaneAssigner.Node> nodes = Arrays.asList(
			node("a"),
			node("b", "a"),
			node("c", "a"),
			node("d", "b", "c"));
		RailLaneAssigner.Result r = RailLaneAssigner.assign(nodes);
		assertEquals(1, r.maxLane, "a simple diamond fits in two lanes");
		assertEquals(0, (int) r.lane.get("a"));
		assertEquals(1, (int) r.lane.get("b"));
		assertEquals(0, (int) r.lane.get("c"), "c reuses a's freed lane");
		assertEquals(0, (int) r.lane.get("d"));
	}

	@Test
	@DisplayName("interconnected lattice (3 overlapping diamonds + OR tail) packs into 4 lanes")
	void interconnectedLattice()
	{
		// Mirrors the live demo graph: 3 skills each feed two of three quests,
		// which merge into Quest Cape, then a final goal needs that plus an OR.
		List<RailLaneAssigner.Node> nodes = Arrays.asList(
			node("agility"),
			node("combat"),
			node("ranged"),
			node("mm2", "agility", "combat"),
			node("dt2", "combat", "ranged"),
			node("sote", "agility", "ranged"),
			node("qpc", "mm2", "dt2", "sote"),
			node("fire"),
			node("inferno"),
			node("gm", "qpc", "fire", "inferno")); // qpc AND (fire OR inferno) — combined prereqs
		RailLaneAssigner.Result r = RailLaneAssigner.assign(nodes);
		assertEquals(3, r.maxLane, "the lattice should peak at 4 lanes (max lane index 3)");
		assertEquals(0, (int) r.lane.get("agility"));
		assertEquals(1, (int) r.lane.get("combat"));
		assertEquals(2, (int) r.lane.get("ranged"));
		assertEquals(3, (int) r.lane.get("mm2"));
		assertEquals(0, (int) r.lane.get("qpc"));
		assertEquals(0, (int) r.lane.get("gm"));
	}

	@Test
	@DisplayName("unknown prereq ids (cross-section edges) are ignored, not crashed on")
	void unknownPrereqsIgnored()
	{
		List<RailLaneAssigner.Node> nodes = Arrays.asList(
			node("a", "not-in-section"),
			node("b", "a", "also-missing"));
		RailLaneAssigner.Result r = RailLaneAssigner.assign(nodes);
		assertEquals(0, r.maxLane);
		assertEquals(0, (int) r.lane.get("a"));
		assertEquals(0, (int) r.lane.get("b"));
	}
}
