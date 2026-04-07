package com.goaltracker.ui;

import com.goaltracker.api.SectionView;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A row rendered as a section header in the goal panel.
 * Click anywhere on the row to toggle collapse/expand. The chevron on the left
 * indicates current state (▼ expanded, ▶ collapsed).
 */
public class SectionHeaderRow extends JPanel
{
	private static final Color BORDER_COLOR = new Color(60, 60, 60);
	private static final Color TEXT_COLOR = new Color(200, 200, 200);
	private static final Color CHEVRON_COLOR = new Color(160, 160, 160);
	private static final int ROW_HEIGHT = 22;
	private static final int CHEVRON_WIDTH = 14;

	/**
	 * Multiplier applied to each RGB channel of a user-picked section color so
	 * the header always has enough darkness to contrast against the existing
	 * light-grey label text. Lower = darker; 0.55 keeps the hue obvious while
	 * guaranteeing WCAG-ish contrast with 200-grey text.
	 */
	private static final double DARKEN_FACTOR = 0.55;

	/** Whether this header has a user color override; if so, paint a darkened fill. */
	private final boolean hasColor;
	private final Color darkenedFill;

	public SectionHeaderRow(SectionView section, int goalCount, Runnable onToggle)
	{
		setLayout(new BorderLayout());

		// User-set color fills the header face with a darkened version of the
		// picked color so the existing light-grey label text always contrasts.
		// Default sections stay transparent with a 1px neutral underline.
		this.hasColor = section.colorOverridden;
		if (hasColor)
		{
			Color picked = new Color(section.colorRgb);
			this.darkenedFill = new Color(
				(int) (picked.getRed() * DARKEN_FACTOR),
				(int) (picked.getGreen() * DARKEN_FACTOR),
				(int) (picked.getBlue() * DARKEN_FACTOR));
			setOpaque(false); // paintComponent draws the fill manually
		}
		else
		{
			this.darkenedFill = null;
			setOpaque(false);
		}
		setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
			new EmptyBorder(4, 8, 4, 8)
		));
		// Match GoalCard's sizing idiom so BoxLayout treats the row identically:
		// preferred width 0 + max width MAX_VALUE + default CENTER alignment lets the
		// row stretch to the full panel width.
		setPreferredSize(new Dimension(0, ROW_HEIGHT));
		setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));
		setAlignmentX(Component.CENTER_ALIGNMENT);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		// Programmatic shape icon avoids font-fallback failure on macOS Tahoe
		// where ▶/▼ Unicode glyphs render as missing-glyph placeholders.
		JLabel chevron = new JLabel(section.collapsed
			? ShapeIcons.rightTriangle(8, CHEVRON_COLOR)
			: ShapeIcons.downTriangle(8, CHEVRON_COLOR));
		chevron.setPreferredSize(new Dimension(CHEVRON_WIDTH, ROW_HEIGHT));

		JLabel nameLabel = new JLabel(section.name.toUpperCase(), SwingConstants.CENTER);
		nameLabel.setForeground(TEXT_COLOR);
		nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 10f));

		// Spacer of equal width on the right so the centered name visually stays centered
		// despite the chevron occupying the left edge.
		JLabel spacer = new JLabel();
		spacer.setPreferredSize(new Dimension(CHEVRON_WIDTH, ROW_HEIGHT));

		add(chevron, BorderLayout.WEST);
		add(nameLabel, BorderLayout.CENTER);
		add(spacer, BorderLayout.EAST);

		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				// Only left-click toggles. Right-click is reserved for the
				// context menu attached by GoalPanel for user-defined sections.
				if (e.getButton() == MouseEvent.BUTTON1 && onToggle != null)
				{
					onToggle.run();
				}
			}
		});
	}

	@Override
	protected void paintComponent(java.awt.Graphics g)
	{
		// Paint the darkened user-color fill ourselves so FlatLaf/UI delegates
		// don't override it. Matches the pattern in ColorPickerField swatches
		// and GoalCard's background paint.
		if (hasColor && darkenedFill != null)
		{
			g.setColor(darkenedFill);
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		super.paintComponent(g);
	}
}
