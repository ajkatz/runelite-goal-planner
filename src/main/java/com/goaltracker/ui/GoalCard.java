package com.goaltracker.ui;

import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Individual goal card with gradient progress fill and reorder arrows.
 *
 * The card IS the progress bar — color fills left-to-right with
 * increasing saturation as progress grows.
 */
public class GoalCard extends JPanel
{
	private static final Color BACKGROUND = new Color(30, 30, 30);
	private static final Color TEXT_PRIMARY = new Color(230, 230, 230);
	private static final Color TEXT_SECONDARY = new Color(160, 160, 160);
	private static final Color ARROW_COLOR = new Color(180, 180, 180);
	private static final Color ARROW_HOVER = Color.WHITE;
	private static final int CARD_HEIGHT = 48;
	private static final int CORNER_RADIUS = 8;

	private Goal goal;
	private final JLabel nameLabel;
	private final JLabel progressLabel;
	private final JLabel statusLabel;
	private final JButton upButton;
	private final JButton downButton;

	public GoalCard(Goal goal, ActionListener onMoveUp, ActionListener onMoveDown)
	{
		this.goal = goal;

		setLayout(new BorderLayout(4, 0));
		setBorder(new EmptyBorder(6, 10, 6, 4));
		setPreferredSize(new Dimension(0, CARD_HEIGHT));
		setMaximumSize(new Dimension(Integer.MAX_VALUE, CARD_HEIGHT));
		setOpaque(false);

		// Left: goal name
		nameLabel = new JLabel(goal.getName());
		nameLabel.setForeground(TEXT_PRIMARY);
		nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 12f));

		// Center: progress info
		JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
		centerPanel.setOpaque(false);

		progressLabel = new JLabel(formatProgress());
		progressLabel.setForeground(TEXT_SECONDARY);
		progressLabel.setFont(progressLabel.getFont().deriveFont(11f));

		statusLabel = new JLabel(formatPercent());
		statusLabel.setForeground(TEXT_PRIMARY);
		statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 11f));

		centerPanel.add(progressLabel);
		centerPanel.add(statusLabel);

		// Right: up/down arrows
		JPanel arrowPanel = new JPanel(new GridLayout(2, 1, 0, 0));
		arrowPanel.setOpaque(false);
		arrowPanel.setPreferredSize(new Dimension(20, CARD_HEIGHT - 12));

		upButton = createArrowButton("\u25B2", onMoveUp);   // ▲
		downButton = createArrowButton("\u25BC", onMoveDown); // ▼

		arrowPanel.add(upButton);
		arrowPanel.add(downButton);

		add(nameLabel, BorderLayout.WEST);
		add(centerPanel, BorderLayout.CENTER);
		add(arrowPanel, BorderLayout.EAST);
	}

	private JButton createArrowButton(String text, ActionListener action)
	{
		JButton btn = new JButton(text);
		btn.setFont(btn.getFont().deriveFont(8f));
		btn.setForeground(ARROW_COLOR);
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		btn.setFocusPainted(false);
		btn.setMargin(new Insets(0, 0, 0, 0));
		btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btn.addActionListener(action);

		btn.addMouseListener(new java.awt.event.MouseAdapter()
		{
			@Override
			public void mouseEntered(java.awt.event.MouseEvent e)
			{
				btn.setForeground(ARROW_HOVER);
			}

			@Override
			public void mouseExited(java.awt.event.MouseEvent e)
			{
				btn.setForeground(ARROW_COLOR);
			}
		});

		return btn;
	}

	public void update(Goal goal)
	{
		this.goal = goal;
		nameLabel.setText(goal.getName());
		progressLabel.setText(formatProgress());
		statusLabel.setText(formatPercent());
		repaint();
	}

	public void setFirstInList(boolean first)
	{
		upButton.setVisible(!first);
	}

	public void setLastInList(boolean last)
	{
		downButton.setVisible(!last);
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

		// Progress fill
		if (progress > 0)
		{
			Color baseColor = goal.getType().getColor();
			int fillWidth = (int) (w * progress);

			int alpha = (int) (38 + (217 * progress));
			Color fillColor = new Color(
				baseColor.getRed(),
				baseColor.getGreen(),
				baseColor.getBlue(),
				Math.min(255, alpha)
			);

			g2.setColor(fillColor);
			g2.fillRoundRect(0, 0, fillWidth, h, CORNER_RADIUS, CORNER_RADIUS);

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
			return String.format("%s / %s XP",
				formatNumber(goal.getCurrentValue()),
				formatNumber(goal.getTargetValue()));
		}
		else
		{
			return String.format("Lv %d / %d", goal.getCurrentValue(), goal.getTargetValue());
		}
	}

	private String formatPercent()
	{
		if (goal.getStatus() == GoalStatus.COMPLETE)
		{
			return "\u2713"; // ✓
		}
		return String.format("%.0f%%", goal.getProgressPercent());
	}

	private static String formatNumber(int n)
	{
		if (n >= 1_000_000) return String.format("%.1fM", n / 1_000_000.0);
		if (n >= 1_000) return String.format("%.0fK", n / 1_000.0);
		return String.valueOf(n);
	}
}
