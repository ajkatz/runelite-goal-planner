package com.goalplanner.ui.rail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure git-graph lane assignment for the dependency connector rail. Given the
 * goals of a section in topological order (prereqs first) plus each goal's
 * in-section prerequisite ids, it assigns every goal a horizontal "lane" so the
 * connector gutter scales with PARALLELISM rather than DAG depth: a linear
 * chain stays in one lane; independent branches fan out into extra lanes; a
 * diamond (a prereq shared by several dependents) is just multiple lines
 * converging on one lane.
 *
 * <p>No Swing or model dependencies — unit-testable in isolation. The caller is
 * responsible for (a) supplying nodes already in topo order and (b) filtering
 * prereq ids to those present in the same section (cross-section edges can't be
 * drawn in this gutter and are simply ignored).
 */
public final class RailLaneAssigner
{
	private RailLaneAssigner() {}

	/** A goal and the ids of its in-section prerequisites (requires + orRequires). */
	public static final class Node
	{
		public final String id;
		public final List<String> prereqs;

		public Node(String id, List<String> prereqs)
		{
			this.id = id;
			this.prereqs = prereqs != null ? prereqs : new ArrayList<>();
		}
	}

	/** Lane index per goal id, plus the highest lane used (gutter width driver). */
	public static final class Result
	{
		public final Map<String, Integer> lane;
		public final int maxLane;

		Result(Map<String, Integer> lane, int maxLane)
		{
			this.lane = lane;
			this.maxLane = maxLane;
		}
	}

	/**
	 * Assign lanes over a topo-ordered node list.
	 *
	 * <p>Each node claims the lowest free lane and holds it until its LAST
	 * dependent is placed, at which point the lane is released for reuse. A node
	 * with no dependents releases its lane immediately. Unknown prereq ids (e.g.
	 * already filtered cross-section edges that slipped through) are ignored.
	 */
	public static Result assign(List<Node> nodesInTopoOrder)
	{
		Map<String, Integer> lane = new HashMap<>();
		int maxLane = 0;

		if (nodesInTopoOrder == null || nodesInTopoOrder.isEmpty())
		{
			return new Result(lane, 0);
		}

		// Count remaining dependents per node so a prereq's lane is freed only
		// once its final dependent has been placed.
		Map<String, Integer> remDeps = new HashMap<>();
		for (Node n : nodesInTopoOrder) remDeps.put(n.id, 0);
		for (Node n : nodesInTopoOrder)
		{
			for (String p : n.prereqs)
			{
				if (remDeps.containsKey(p)) remDeps.merge(p, 1, Integer::sum);
			}
		}

		boolean[] active = new boolean[nodesInTopoOrder.size() + 2];
		for (Node n : nodesInTopoOrder)
		{
			// Release the lane of any prereq whose last dependent is this node.
			for (String p : n.prereqs)
			{
				if (!remDeps.containsKey(p)) continue;
				if (remDeps.merge(p, -1, Integer::sum) == 0 && lane.containsKey(p))
				{
					active[lane.get(p)] = false;
				}
			}
			int l = 0;
			while (active[l]) l++;
			lane.put(n.id, l);
			active[l] = true;
			maxLane = Math.max(maxLane, l);
			// No dependents → release immediately so a sibling can reuse the lane.
			if (remDeps.get(n.id) == 0) active[l] = false;
		}

		return new Result(lane, maxLane);
	}
}
