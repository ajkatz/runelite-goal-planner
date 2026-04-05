package com.goaltracker.ui;

import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Individual goal card with gradient progress fill.
 *
 * The card IS the progress bar — color fills left-to-right with
 * increasing saturation as progress grows. A nearly complete goal
 * glows with vivid color; a new goal is barely tinted.
 */
public class GoalCard extends JPanel
{
	private static final Color BACKGROUND = new Color(30, 30, 30);
	private static final Color TEXT_PRIMARY = new Color(230, 230, 230);
	private static final Color TEXT_SECONDARY = new Color(160, 160, 160);
	private static final int CARD_HEIGHT = 48;
	private static final int CORNER_RADIUS = 8;

	private Goal goal;
	private final JLabel nameLabel;
	private final JLabel progressLabel;
	private final JLabel statusLabel;

	public GoalCard(Goal goal)
	{
		this.goal = goal;

		setLayout(new BorderLayout(8, 0));
		setBorder(new EmptyBorder(6, 10, 6, 10));
		setPreferredSize(new Dimension(0, CARD_HEIGHT));
		setMaximumSize(new Dimension(Integer.MAX_VALUE, CARD_HEIGHT));
		setOpaque(false);

		// Left: goal name
		nameLabel = new JLabel(goal.getName());
		nameLabel.setForeground(TEXT_PRIMARY);
		nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 12f));

		// Right: progress info
		JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
		rightPanel.setOpaque(false);

		progressLabel = new JLabel(formatProgress());
		progressLabel.setForeground(TEXT_SECONDARY);
		progressLabel.setFont(progressLabel.getFont().deriveFont(11f));

		statusLabel = new JLabel(formatPercent());
		statusLabel.setForeground(TEXT_PRIMARY);
		statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 11f));

		rightPanel.add(progressLabel);
		rightPanel.add(statusLabel);

		add(nameLabel, BorderLayout.WEST);
		add(rightPanel, BorderLayout.EAST);
	}

	public void update(Goal goal)
	{
		this.goal = goal;
		nameLabel.setText(goal.getName());
		progressLabel.setText(formatProgress());
		statusLabel.setText(formatPercent());
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int w = getWidth();
		int h = getHeight();
		double progress = goal.getProgressPercent() / 100.0;

		// Background
		g2.setColor(BACKGROUND);
		g2.fillRoundRect(0, 0, w, h, CORNER_RADIUS, CORNER_RADIUS);

		// Progress fill — color sweeps left-to-right with increasing saturation
		if (progress > 0)
		{
			Color baseColor = goal.getType().getColor();
			int fillWidth = (int) (w * progress);

			// Alpha increases with progress: 0.15 at 0% → 1.0 at 100%
			int alpha = (int) (38 + (217 * progress)); // 38 = 0.15*255, 255 = 1.0*255
			Color fillColor = new Color(
				baseColor.getRed(),
				baseColor.getGreen(),
				baseColor.getBlue(),
				Math.min(255, alpha)
			);

			g2.setColor(fillColor);
			g2.fillRoundRect(0, 0, fillWidth, h, CORNER_RADIUS, CORNER_RADIUS);

			// Clean edge: if fill doesn't cover full card, clip the right corners
			if (fillWidth < w - CORNER_RADIUS)
			{
				g2.fillRect(fillWidth - CORNER_RADIUS, 0, CORNER_RADIUS, h);
			}
		}

		// Complete state: bright border
		if (goal.getStatus() == GoalStatus.COMPLETE)
		{
			g2.setColor(goal.getType().getColor());
			g2.setStroke(new BasicStroke(2));
			g2.drawRoundRect(1, 1, w - 2, h - 2, CORNER_RADIUS, CORNER_RADIUS);
		}

		g2.dispose();

		// Paint children (labels) on top
		super.paintComponent(g);
	}

	private String formatProgress()
	{
		if (goal.getTargetValue() <= 0)
		{
			return "";
		}

		if (goal.getTargetValue() > 99)
		{
			// XP format
			return String.format("%s / %s XP",
				formatNumber(goal.getCurrentValue()),
				formatNumber(goal.getTargetValue()));
		}
		else
		{
			// Level format
			return String.format("Lv %d / %d", goal.getCurrentValue(), goal.getTargetValue());
		}
	}

	private String formatPercent()
	{
		double pct = goal.getProgressPercent();
		if (goal.getStatus() == GoalStatus.COMPLETE)
		{
			return "✓";
		}
		return String.format("%.0f%%", pct);
	}

	private static String formatNumber(int n)
	{
		if (n >= 1_000_000)
		{
			return String.format("%.1fM", n / 1_000_000.0);
		}
		if (n >= 1_000)
		{
			return String.format("%.0fK", n / 1_000.0);
		}
		return String.valueOf(n);
	}
}
