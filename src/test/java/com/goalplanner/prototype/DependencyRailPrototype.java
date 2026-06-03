package com.goalplanner.prototype;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Standalone PROTOTYPE — render a goal dependency DAG as a git-graph-style
 * connector RAIL: a fixed narrow left gutter of lanes + lines, with full-width
 * cards. Demonstrates the two hard cases in minimal width:
 *  - a DIAMOND (a shared prereq with two parents converging into one node), and
 *  - an OR edge (a goal satisfied by ANY of several alternatives, dashed).
 *
 * <p>Not shipped (test source, no @Test). Run it with: {@code ./gradlew rail}
 */
public final class DependencyRailPrototype
{
	private DependencyRailPrototype() {}

	static final class Node
	{
		final String id, label;
		final boolean complete;
		final List<String> requires;   // AND prereqs (need ALL)
		final List<String> orRequires; // OR prereqs (need ANY one)
		Node(String id, String label, boolean complete, List<String> requires, List<String> orRequires)
		{
			this.id = id; this.label = label; this.complete = complete;
			this.requires = requires; this.orRequires = orRequires;
		}
	}

	private static List<String> ids(String... s) { return new ArrayList<>(Arrays.asList(s)); }

	/** Sample graph in topo order (prereqs first). Diamond over "70 Agility"; OR on the Western diary. */
	private static List<Node> sample()
	{
		return Arrays.asList(
			new Node("agility", "70 Agility",          true,  ids(),                ids()),
			new Node("mm2",     "Monkey Madness II",   false, ids("agility"),       ids()),
			new Node("ds2",     "Dragon Slayer II",    true,  ids("agility"),       ids()),
			new Node("qpc",     "Quest Point Cape",    false, ids("mm2", "ds2"),    ids()),
			new Node("fire",    "Fire Cape",           true,  ids(),                ids()),
			new Node("inferno", "Infernal Cape",       false, ids(),                ids()),
			new Node("western", "Western Elite Diary", false, ids(),                ids("fire", "inferno"))
		);
	}

	// Palette (plugin-ish dark theme, no RuneLite deps so it runs standalone).
	private static final Color BG        = new Color(0x2b, 0x2b, 0x2b);
	private static final Color CARD      = new Color(0x38, 0x38, 0x38);
	private static final Color TEXT      = new Color(0xE0, 0xE0, 0xE0);
	private static final Color TEXT_DONE = new Color(0x8c, 0xc8, 0x6a);
	private static final Color AND_LINE  = new Color(0x6f, 0x9f, 0xd8);
	private static final Color OR_LINE   = new Color(0xd8, 0xa8, 0x55);
	private static final Color DONE_LINE = new Color(0x5a, 0x7a, 0x52);
	private static final Color DOT_DONE  = new Color(0x7e, 0xc6, 0x5a);
	private static final Color DOT_OPEN  = new Color(0xB0, 0xB0, 0xB0);
	private static final Font  FONT      = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

	private static final int ROW_H = 34, LANE_STEP = 18, GUTTER_LEFT = 16, DOT_R = 5, TOP_PAD = 8;

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(() ->
		{
			JFrame f = new JFrame("Goal Planner — dependency rail prototype");
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.setLayout(new BorderLayout());
			f.add(new JScrollPane(new RailPanel(sample())), BorderLayout.CENTER);

			JLabel legend = new JLabel(
				"<html><div style='color:#bbb;font-family:sans-serif;font-size:10px'>"
				+ "&#9679; done &nbsp; &#9711; to-do &nbsp;|&nbsp; "
				+ "<font color='#6f9fd8'>&#9472;&#9472;</font> needs ALL &nbsp; "
				+ "<font color='#d8a855'>&#9476;&#9476;</font> needs ANY (OR)</div></html>");
			legend.setBorder(new EmptyBorder(6, 10, 6, 10));
			legend.setOpaque(true);
			legend.setBackground(new Color(0x22, 0x22, 0x22));
			f.add(legend, BorderLayout.SOUTH);

			f.setSize(260, 360); // deliberately narrow — like the RuneLite side panel
			f.setLocationRelativeTo(null);
			f.setVisible(true);
		});
	}

	static final class RailPanel extends JPanel
	{
		private final List<Node> nodes;
		private final Map<String, Node> byId = new HashMap<>();
		private final Map<String, Integer> lane = new HashMap<>();
		private final Map<String, Integer> row = new HashMap<>();
		private int maxLane = 0;

		RailPanel(List<Node> nodes)
		{
			this.nodes = nodes;
			for (Node n : nodes) byId.put(n.id, n);
			assignLanes();
			setBackground(BG);
			setPreferredSize(new Dimension(244, nodes.size() * ROW_H + 2 * TOP_PAD));
		}

		private List<String> prereqs(Node n)
		{
			List<String> all = new ArrayList<>(n.requires);
			all.addAll(n.orRequires);
			return all;
		}

		/**
		 * git-graph lane sweep over the topo order: each node claims a lane and
		 * holds it until its last dependent is placed (so chains stay in one lane
		 * and the gutter scales with PARALLELISM, not depth). A prereq shared by
		 * several dependents (a diamond) just has multiple lines converge on it.
		 */
		private void assignLanes()
		{
			Map<String, Integer> remDeps = new HashMap<>();
			for (Node n : nodes) remDeps.put(n.id, 0);
			for (Node n : nodes) for (String p : prereqs(n)) remDeps.merge(p, 1, Integer::sum);

			boolean[] active = new boolean[nodes.size() + 2];
			int r = 0;
			for (Node n : nodes)
			{
				row.put(n.id, r++);
				// A prereq frees its lane once its LAST dependent (this node) is placed.
				for (String p : prereqs(n))
				{
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
				if (remDeps.get(n.id) == 0) active[l] = false; // no dependents → release now
			}
		}

		private int dotX(String id) { return GUTTER_LEFT + lane.get(id) * LANE_STEP; }
		private int dotY(String id) { return TOP_PAD + row.get(id) * ROW_H + ROW_H / 2; }

		@Override
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setFont(FONT);

			int gutterW = GUTTER_LEFT + (maxLane + 1) * LANE_STEP;
			int cardX = gutterW + 6;

			// 1) edges (under the dots)
			for (Node n : nodes)
			{
				for (String p : n.requires) drawEdge(g2, byId.get(p), n, false);
				for (String p : n.orRequires) drawEdge(g2, byId.get(p), n, true);
			}

			// 2) cards + dots
			for (Node n : nodes)
			{
				int y = TOP_PAD + row.get(n.id) * ROW_H;
				g2.setColor(CARD);
				g2.fillRoundRect(cardX, y + 3, getWidth() - cardX - 6, ROW_H - 6, 8, 8);
				g2.setColor(n.complete ? TEXT_DONE : TEXT);
				g2.drawString(n.label, cardX + 10, y + ROW_H / 2 + 4);

				int dx = dotX(n.id), dy = dotY(n.id);
				if (n.complete)
				{
					g2.setColor(DOT_DONE);
					g2.fill(new Ellipse2D.Float(dx - DOT_R, dy - DOT_R, 2 * DOT_R, 2 * DOT_R));
				}
				else
				{
					g2.setColor(BG);
					g2.fill(new Ellipse2D.Float(dx - DOT_R, dy - DOT_R, 2 * DOT_R, 2 * DOT_R));
					g2.setColor(DOT_OPEN);
					g2.setStroke(new BasicStroke(1.6f));
					g2.draw(new Ellipse2D.Float(dx - DOT_R, dy - DOT_R, 2 * DOT_R, 2 * DOT_R));
				}
			}
			g2.dispose();
		}

		private void drawEdge(Graphics2D g2, Node prereq, Node dependent, boolean or)
		{
			if (prereq == null) return;
			float px = dotX(prereq.id), py = dotY(prereq.id);   // prereq is above
			float nx = dotX(dependent.id), ny = dotY(dependent.id); // dependent below
			g2.setColor(or ? OR_LINE : (prereq.complete ? DONE_LINE : AND_LINE));
			g2.setStroke(or
				? new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{4f, 4f}, 0f)
				: new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			float midY = (py + ny) / 2f; // S-curve: down in the prereq's lane, then bend into the dependent's
			g2.draw(new CubicCurve2D.Float(px, py, px, midY, nx, midY, nx, ny));
		}
	}
}
