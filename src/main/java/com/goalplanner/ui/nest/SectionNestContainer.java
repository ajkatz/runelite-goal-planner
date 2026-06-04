package com.goalplanner.ui.nest;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Renders one section's goals as a <em>subtly nested</em> list: each real
 * {@link com.goalplanner.ui.GoalCard} is stacked full-width (so every card
 * behaviour — menus, arrows, selection, progress — is preserved untouched) but
 * left-indented by its dependency depth ({@link NestIndentAssigner}), with a
 * thin, low-contrast vertical guide drawn at each ancestor level (file-tree
 * style). This is the quiet alternative to the connector-rail view: no dots, no
 * S-curves — just indentation + a faint guide so the hierarchy is scannable.
 */
public final class SectionNestContainer extends JPanel
{
	/** One goal row: its indent level (0 = root) and its card. */
	public static final class Row
	{
		public final String id;
		public final int level;
		public final JComponent card;

		public Row(String id, int level, JComponent card)
		{
			this.id = id;
			this.level = Math.max(0, level);
			this.card = card;
		}
	}

	// Faint, theme-friendly guide on the dark panel — low contrast on purpose.
	private static final Color GUIDE = new Color(0x6a, 0x6a, 0x6a, 0x66);

	private static final int LEFT_PAD    = 3;   // margin before level-0 cards
	private static final int INDENT_STEP = 13;  // px added per nesting level
	private static final int MAX_LEVEL   = 6;   // cap indent so deep chains don't run off a narrow panel
	private static final int ROW_GAP     = 4;   // vertical gap between cards
	private static final int TICK        = 5;   // short horizontal stub from the parent guide into the card

	private final List<Row> rows;
	private final int indentW; // widest indent, for preferred-size bookkeeping

	public SectionNestContainer(List<Row> rows)
	{
		this.rows = rows;
		setOpaque(false);
		setLayout(null); // manual layout in doLayout()

		int maxLevel = 0;
		for (Row r : rows) maxLevel = Math.max(maxLevel, capped(r.level));
		this.indentW = LEFT_PAD + maxLevel * INDENT_STEP;

		for (Row r : rows) add(r.card);
	}

	private static int capped(int level) { return Math.min(level, MAX_LEVEL); }

	/** Left x of a card at the given (capped) level. */
	private int cardX(int level) { return LEFT_PAD + capped(level) * INDENT_STEP; }

	/** X of the vertical guide for ancestor level k (1-based). */
	private int guideX(int k) { return LEFT_PAD + (k - 1) * INDENT_STEP + INDENT_STEP / 2; }

	@Override
	public void doLayout()
	{
		int width = getWidth();
		int y = 0;
		for (Row r : rows)
		{
			int x = cardX(r.level);
			int h = r.card.getPreferredSize().height;
			r.card.setBounds(x, y, Math.max(40, width - x), h);
			y += h + ROW_GAP;
		}
	}

	@Override
	public Dimension getPreferredSize()
	{
		int h = 0;
		for (Row r : rows) h += r.card.getPreferredSize().height + ROW_GAP;
		// Width driven by the parent (BoxLayout); report indent + a modest card
		// minimum so a section never collapses below usability.
		return new Dimension(indentW + 120, Math.max(h, 1));
	}

	@Override
	public Dimension getMaximumSize()
	{
		return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (rows.isEmpty()) return;

		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(GUIDE);
		g2.setStroke(new BasicStroke(1f));

		for (Row r : rows)
		{
			int lvl = capped(r.level);
			if (lvl == 0) continue;

			int top = r.card.getY();
			int bottom = top + r.card.getHeight() + ROW_GAP; // span the gap so guides read continuous
			int midY = top + r.card.getHeight() / 2;

			// Vertical guide at every ancestor level (file-tree spine). Consecutive
			// rows sharing a level stack into one continuous-looking line.
			for (int k = 1; k <= lvl; k++)
			{
				int gx = guideX(k);
				g2.drawLine(gx, top, gx, bottom);
			}

			// Short horizontal stub from this card's immediate parent guide into it.
			int stubX = guideX(lvl);
			g2.drawLine(stubX, midY, stubX + TICK, midY);
		}
		g2.dispose();
	}

	/** Convenience for callers that don't track child components separately. */
	public List<Component> cards()
	{
		List<Component> out = new ArrayList<>(rows.size());
		for (Row r : rows) out.add(r.card);
		return out;
	}
}
