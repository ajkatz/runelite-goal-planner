package com.goalplanner.ui.rail;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Renders one section's goals as a git-graph-style dependency "rail": the real
 * {@link com.goalplanner.ui.GoalCard} components are stacked full-width on the
 * right (so every existing card behaviour — menus, arrows, selection, progress
 * — is preserved untouched), while a fixed left gutter paints the connector
 * lines and lane dots derived from the goals' requires / orRequires edges.
 *
 * <p>The gutter width scales with PARALLELISM (lane count from
 * {@link RailLaneAssigner}), not DAG depth, so deep chains stay narrow and only
 * genuinely independent branches cost horizontal space. AND edges render as
 * solid lines, OR edges as dashed amber, and edges out of completed prereqs are
 * muted. Cross-section prereqs are ignored (can't be drawn here).
 */
public final class SectionRailContainer extends JPanel
{
	/** One goal row: its card plus the in-section edges feeding into it. */
	public static final class Row
	{
		public final String id;
		public final List<String> requires;   // in-section AND prereqs
		public final List<String> orRequires; // in-section OR prereqs
		public final boolean complete;
		public final JComponent card;

		public Row(String id, List<String> requires, List<String> orRequires,
				   boolean complete, JComponent card)
		{
			this.id = id;
			this.requires = requires != null ? requires : new ArrayList<>();
			this.orRequires = orRequires != null ? orRequires : new ArrayList<>();
			this.complete = complete;
			this.card = card;
		}
	}

	// Palette — tuned to read on the dark plugin panel; mirrors the prototype.
	private static final Color AND_LINE  = new Color(0x6f, 0x9f, 0xd8);
	private static final Color OR_LINE   = new Color(0xd8, 0xa8, 0x55);
	private static final Color DONE_LINE = new Color(0x5a, 0x7a, 0x52);
	private static final Color DOT_DONE  = new Color(0x7e, 0xc6, 0x5a);
	private static final Color DOT_OPEN  = new Color(0xB0, 0xB0, 0xB0);

	private static final int GUTTER_LEFT = 9;   // left margin before lane 0
	private static final int LANE_STEP   = 14;  // horizontal spacing between lanes
	private static final int DOT_R       = 4;   // lane dot radius
	private static final int CARD_GAP    = 8;   // space between gutter and cards
	private static final int ROW_GAP     = 4;   // vertical gap between cards

	private final List<Row> rows;
	private final Map<String, Integer> lane;
	private final int gutterW;

	public SectionRailContainer(List<Row> rows)
	{
		this.rows = rows;
		setOpaque(false);
		setLayout(null); // manual layout in doLayout()

		List<RailLaneAssigner.Node> nodes = new ArrayList<>(rows.size());
		for (Row r : rows)
		{
			List<String> prereqs = new ArrayList<>(r.requires);
			prereqs.addAll(r.orRequires);
			nodes.add(new RailLaneAssigner.Node(r.id, prereqs));
		}
		RailLaneAssigner.Result res = RailLaneAssigner.assign(nodes);
		this.lane = res.lane;
		this.gutterW = GUTTER_LEFT + res.maxLane * LANE_STEP + DOT_R + CARD_GAP;

		for (Row r : rows) add(r.card);
	}

	private int laneX(String id)
	{
		Integer l = lane.get(id);
		return GUTTER_LEFT + (l == null ? 0 : l) * LANE_STEP;
	}

	@Override
	public void doLayout()
	{
		int width = getWidth();
		int cardW = Math.max(40, width - gutterW);
		int y = 0;
		for (Row r : rows)
		{
			int h = r.card.getPreferredSize().height;
			r.card.setBounds(gutterW, y, cardW, h);
			y += h + ROW_GAP;
		}
	}

	@Override
	public Dimension getPreferredSize()
	{
		int h = 0;
		for (Row r : rows)
		{
			h += r.card.getPreferredSize().height + ROW_GAP;
		}
		// Width is driven by the parent (BoxLayout) — report the gutter plus a
		// modest card minimum so the section never collapses below usability.
		return new Dimension(gutterW + 120, Math.max(h, 1));
	}

	@Override
	public Dimension getMaximumSize()
	{
		return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
	}

	private int centerY(Row r)
	{
		return r.card.getY() + r.card.getHeight() / 2;
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (rows.isEmpty()) return;

		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Map<String, Row> byId = new HashMap<>();
		for (Row r : rows) byId.put(r.id, r);

		// 1) edges (under the dots)
		for (Row r : rows)
		{
			for (String p : r.requires)   drawEdge(g2, byId.get(p), r, false);
			for (String p : r.orRequires) drawEdge(g2, byId.get(p), r, true);
		}

		// 2) lane dots
		for (Row r : rows)
		{
			int dx = laneX(r.id), dy = centerY(r);
			if (r.complete)
			{
				g2.setColor(DOT_DONE);
				g2.fill(new Ellipse2D.Float(dx - DOT_R, dy - DOT_R, 2 * DOT_R, 2 * DOT_R));
			}
			else
			{
				g2.setColor(DOT_OPEN);
				g2.setStroke(new BasicStroke(1.6f));
				g2.draw(new Ellipse2D.Float(dx - DOT_R, dy - DOT_R, 2 * DOT_R, 2 * DOT_R));
			}
		}
		g2.dispose();
	}

	private void drawEdge(Graphics2D g2, Row prereq, Row dependent, boolean or)
	{
		if (prereq == null) return; // cross-section / missing — nothing to draw
		float px = laneX(prereq.id), py = centerY(prereq);
		float nx = laneX(dependent.id), ny = centerY(dependent);
		g2.setColor(or ? OR_LINE : (prereq.complete ? DONE_LINE : AND_LINE));
		g2.setStroke(or
			? new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{4f, 4f}, 0f)
			: new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		// S-curve: drop straight in the prereq's lane, bend into the dependent's.
		float midY = (py + ny) / 2f;
		g2.draw(new CubicCurve2D.Float(px, py, px, midY, nx, midY, nx, ny));
	}

	/** Convenience for callers that don't track child components separately. */
	public List<Component> cards()
	{
		List<Component> out = new ArrayList<>(rows.size());
		for (Row r : rows) out.add(r.card);
		return out;
	}
}
