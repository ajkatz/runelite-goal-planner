package com.goaltracker.ui;

import com.goaltracker.model.Section;

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

	public SectionHeaderRow(Section section, int goalCount, Runnable onToggle)
	{
		setLayout(new BorderLayout());
		setOpaque(false);
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

		JLabel chevron = new JLabel(section.isCollapsed() ? "\u25B6" : "\u25BC");
		chevron.setForeground(CHEVRON_COLOR);
		chevron.setFont(chevron.getFont().deriveFont(Font.PLAIN, 9f));
		chevron.setPreferredSize(new Dimension(CHEVRON_WIDTH, ROW_HEIGHT));

		JLabel nameLabel = new JLabel(section.getName().toUpperCase(), SwingConstants.CENTER);
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
				if (onToggle != null) onToggle.run();
			}
		});
	}
}
