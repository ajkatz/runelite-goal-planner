package com.goaltracker.ui;

import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalStatus;
import com.goaltracker.model.GoalType;
import net.runelite.api.Skill;
import net.runelite.client.game.SkillIconManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Individual goal card with gradient progress fill, reorder arrows,
 * and chain indicator for related goals.
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

	public GoalCard(Goal goal, ActionListener onMoveUp, ActionListener onMoveDown, SkillIconManager skillIconManager)
	{
		this.goal = goal;

		setLayout(new BorderLayout(4, 0));
		setBorder(new EmptyBorder(4, 10, 4, 4));
		setPreferredSize(new Dimension(0, CARD_HEIGHT));
		setMaximumSize(new Dimension(Integer.MAX_VALUE, CARD_HEIGHT));
		setOpaque(false);

		// Left: icon + name (two lines)
		JPanel leftPanel = new JPanel(new BorderLayout(6, 0));
		leftPanel.setOpaque(false);

		// Icon: skill icon for skills, colored dot for everything else
		JLabel iconLabel;
		if (goal.getType() == GoalType.SKILL && goal.getSkillName() != null && skillIconManager != null)
		{
			try
			{
				Skill skill = Skill.valueOf(goal.getSkillName());
				iconLabel = new JLabel(new ImageIcon(skillIconManager.getSkillImage(skill, true)));
			}
			catch (Exception e)
			{
				iconLabel = makeColorDot(goal.getType().getColor());
			}
		}
		else
		{
			iconLabel = makeColorDot(goal.getType().getColor());
		}
		iconLabel.setPreferredSize(new Dimension(18, 18));
		leftPanel.add(iconLabel, BorderLayout.WEST);

		nameLabel = new JLabel(formatNameHtml());
		nameLabel.setForeground(TEXT_PRIMARY);
		nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 12f));
		if (goal.getName().length() > 22)
		{
			nameLabel.setToolTipText(goal.getName());
		}
		leftPanel.add(nameLabel, BorderLayout.CENTER);

		// Right side: percent
		JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
		centerPanel.setOpaque(false);

		progressLabel = new JLabel(); // kept for update() compatibility
		progressLabel.setVisible(false);

		statusLabel = new JLabel(formatPercent());
		statusLabel.setForeground(TEXT_PRIMARY);
		statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 11f));

		centerPanel.add(statusLabel);

		// Right: up/down arrows
		JPanel arrowPanel = new JPanel(new GridLayout(2, 1, 0, 0));
		arrowPanel.setOpaque(false);
		arrowPanel.setPreferredSize(new Dimension(20, CARD_HEIGHT - 12));

		upButton = createArrowButton("\u25B2", onMoveUp);
		downButton = createArrowButton("\u25BC", onMoveDown);

		arrowPanel.add(upButton);
		arrowPanel.add(downButton);

		add(leftPanel, BorderLayout.WEST);
		add(centerPanel, BorderLayout.CENTER);
		add(arrowPanel, BorderLayout.EAST);
	}

	private static JLabel makeColorDot(Color color)
	{
		return new JLabel()
		{
			@Override
			protected void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(color);
				int size = 12;
				int x = (getWidth() - size) / 2;
				int y = (getHeight() - size) / 2;
				g2.fillOval(x, y, size, size);
				g2.dispose();
			}
		};
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
		nameLabel.setText(formatNameHtml());
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
		Color baseColor = goal.getType().getColor();

		// Background: type-colored tint — same intensity for all goals of that type
		g2.setColor(BACKGROUND);
		g2.fillRoundRect(0, 0, w, h, CORNER_RADIUS, CORNER_RADIUS);

		Color tint = new Color(
			baseColor.getRed(),
			baseColor.getGreen(),
			baseColor.getBlue(),
			40
		);
		g2.setColor(tint);
		g2.fillRoundRect(0, 0, w, h, CORNER_RADIUS, CORNER_RADIUS);

		// Complete state: bright border
		if (goal.getStatus() == GoalStatus.COMPLETE)
		{
			g2.setColor(baseColor);
			g2.setStroke(new BasicStroke(2));
			g2.drawRoundRect(1, 1, w - 2, h - 2, CORNER_RADIUS, CORNER_RADIUS);
		}

		g2.dispose();
		super.paintComponent(g);
	}

	/**
	 * Format card name as two lines:
	 * Line 1: Name (bold) — skill name for skills, goal name for custom
	 * Line 2: Detail (small gray) — level/XP for skills, description for custom
	 */
	private String formatNameHtml()
	{
		String line1;
		String line2;

		switch (goal.getType())
		{
			case SKILL:
				// Line 1: skill name, Line 2: level/XP progress
				line1 = goal.getSkillName() != null
					? net.runelite.api.Skill.valueOf(goal.getSkillName()).getName()
					: goal.getName();
				line2 = formatProgress();
				break;
			case CUSTOM:
			default:
				line1 = truncate(goal.getName(), 22);
				line2 = (goal.getDescription() != null && !goal.getDescription().isEmpty())
					? truncate(goal.getDescription(), 30)
					: "";
				break;
		}

		if (line2.isEmpty())
		{
			return escapeHtml(line1);
		}

		return "<html>" + escapeHtml(line1) + "<br><span style='font-size:9px; color:#a0a0a0'>"
			+ escapeHtml(line2) + "</span></html>";
	}

	private static String truncate(String text, int maxLen)
	{
		if (text.length() <= maxLen) return text;
		return text.substring(0, maxLen - 1) + "\u2026"; // ellipsis
	}

	private static String escapeHtml(String text)
	{
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	/**
	 * Progress text for the second line of the name area.
	 * Used by formatNameHtml() for skill goals.
	 */
	private String formatProgress()
	{
		if (goal.getType() == GoalType.SKILL)
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
		return "";
	}

	private String formatPercent()
	{
		if (goal.getStatus() == GoalStatus.COMPLETE)
		{
			return "\u2713";
		}
		if (goal.getType() == GoalType.CUSTOM)
		{
			return ""; // custom goals just show ✓ when complete
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
