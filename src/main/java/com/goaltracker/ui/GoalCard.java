package com.goaltracker.ui;

import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalStatus;
import com.goaltracker.model.GoalType;
import com.goaltracker.model.ItemTag;
import com.goaltracker.model.TagCategory;
import com.goaltracker.util.FormatUtil;
import net.runelite.api.Skill;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;

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
	private static final int TAG_ROW_HEIGHT = 18;
	private static final int CORNER_RADIUS = 8;

	private Goal goal;
	private final JLabel nameLabel;
	private final JLabel statusLabel;
	private final JButton upButton;
	private final JButton downButton;

	private final SkillIconManager skillIconManager;
	private final SpriteManager spriteManager;

	public GoalCard(Goal goal, ActionListener onMoveUp, ActionListener onMoveDown,
					SkillIconManager skillIconManager, ItemManager itemManager,
					SpriteManager spriteManager)
	{
		this.goal = goal;
		this.skillIconManager = skillIconManager;
		this.spriteManager = spriteManager;

		boolean hasTags = goal.getTags() != null && !goal.getTags().isEmpty();
		int height = hasTags ? CARD_HEIGHT + TAG_ROW_HEIGHT : CARD_HEIGHT;

		setLayout(new BorderLayout(4, 0));
		setBorder(new EmptyBorder(4, 10, 4, 4));
		setPreferredSize(new Dimension(0, height));
		setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
		setOpaque(false);

		// Left: icon + name (two lines)
		JPanel leftPanel = new JPanel(new BorderLayout(6, 0));
		leftPanel.setOpaque(false);

		// Icon: skill icon for skills, item sprite for items, colored dot for others
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
		else if (goal.getType() == GoalType.ITEM_GRIND && goal.getItemId() > 0 && itemManager != null)
		{
			iconLabel = new JLabel();
			ItemImageCache.applyTo(iconLabel, goal.getItemId(), itemManager);
		}
		else if (goal.getSpriteId() > 0 && spriteManager != null)
		{
			final JLabel spriteLabel = new JLabel();
			spriteManager.getSpriteAsync(goal.getSpriteId(), 0, img ->
				SwingUtilities.invokeLater(() -> {
					if (img == null) return;
					java.awt.image.BufferedImage scaled = new java.awt.image.BufferedImage(
						18, 18, java.awt.image.BufferedImage.TYPE_INT_ARGB);
					Graphics2D g2d = scaled.createGraphics();
					g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					g2d.drawImage(img, 0, 0, 18, 18, null);
					g2d.dispose();
					spriteLabel.setIcon(new ImageIcon(scaled));
				}));
			iconLabel = spriteLabel;
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
		nameLabel.setVerticalAlignment(SwingConstants.CENTER);
		// Prefer a custom tooltip (e.g. CA full description) over the name-truncation fallback.
		// Applied to the whole card so hover works anywhere on it, not just the name label.
		String customTooltip = goal.getTooltip();
		if (customTooltip != null && !customTooltip.isEmpty())
		{
			setToolTipText(customTooltip);
		}
		else if (goal.getName().length() > 22)
		{
			setToolTipText(goal.getName());
		}
		// Name + tags in a vertical BorderLayout
		JPanel nameAndTags = new JPanel(new BorderLayout(0, 0));
		nameAndTags.setOpaque(false);
		nameAndTags.add(nameLabel, BorderLayout.CENTER);

		if (hasTags)
		{
			JPanel tagRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
			tagRow.setOpaque(false);
			tagRow.setBorder(new EmptyBorder(0, 0, 0, 0));

			// Sort: SKILLING first, then others
			java.util.List<ItemTag> sorted = new java.util.ArrayList<>(goal.getTags());
			sorted.sort((a, b) -> {
				boolean aSkill = a.getCategory() == TagCategory.SKILLING;
				boolean bSkill = b.getCategory() == TagCategory.SKILLING;
				if (aSkill != bSkill) return aSkill ? -1 : 1;
				return 0;
			});

			// Collapse 3+ boss tags into "Multiple" with tooltip
			java.util.List<ItemTag> bossTags = new java.util.ArrayList<>();
			java.util.List<ItemTag> otherTags = new java.util.ArrayList<>();
			for (ItemTag tag : sorted)
			{
				if (tag.getCategory() == TagCategory.BOSS)
				{
					bossTags.add(tag);
				}
				else
				{
					otherTags.add(tag);
				}
			}

			// Add non-boss tags
			for (ItemTag tag : otherTags)
			{
				tagRow.add(createTagComponent(tag));
			}

			// Add boss tags (collapsed if 3+)
			if (bossTags.size() >= 3)
			{
				ItemTag multiTag = new ItemTag("Multiple", TagCategory.BOSS);
				JComponent pill = createTagComponent(multiTag);
				StringBuilder tooltip = new StringBuilder("<html>Dropped by:<br>");
				for (ItemTag bt : bossTags)
				{
					tooltip.append("• ").append(FormatUtil.escapeHtml(bt.getLabel())).append("<br>");
				}
				tooltip.append("</html>");
				pill.setToolTipText(tooltip.toString());
				tagRow.add(pill);
			}
			else
			{
				for (ItemTag tag : bossTags)
				{
					tagRow.add(createTagComponent(tag));
				}
			}

			nameAndTags.add(tagRow, BorderLayout.SOUTH);
		}

		leftPanel.add(nameAndTags, BorderLayout.CENTER);

		// Right side: status (XP, percent, etc.)
		statusLabel = new JLabel(formatPercent());
		statusLabel.setForeground(TEXT_PRIMARY);
		statusLabel.setFont(statusLabel.getFont().deriveFont(11f));
		statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		statusLabel.setVerticalAlignment(SwingConstants.CENTER);

		// Right: up/down arrows
		JPanel arrowPanel = new JPanel(new GridLayout(2, 1, 0, 0));
		arrowPanel.setOpaque(false);
		arrowPanel.setPreferredSize(new Dimension(20, CARD_HEIGHT - 12));

		upButton = createArrowButton("\u25B2", onMoveUp);
		downButton = createArrowButton("\u25BC", onMoveDown);

		arrowPanel.add(upButton);
		arrowPanel.add(downButton);

		add(leftPanel, BorderLayout.WEST);
		add(statusLabel, BorderLayout.CENTER);
		add(arrowPanel, BorderLayout.EAST);
	}

	private JComponent createTagComponent(ItemTag tag)
	{
		// For Skilling tags, try to show the skill icon
		if (tag.getCategory() == com.goaltracker.model.TagCategory.SKILLING && skillIconManager != null)
		{
			try
			{
				net.runelite.api.Skill skill = net.runelite.api.Skill.valueOf(tag.getLabel().toUpperCase());
				java.awt.image.BufferedImage img = skillIconManager.getSkillImage(skill, true);
				java.awt.image.BufferedImage scaled = new java.awt.image.BufferedImage(11, 11, java.awt.image.BufferedImage.TYPE_INT_ARGB);
				java.awt.Graphics2D g2d = scaled.createGraphics();
				g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2d.drawImage(img, 0, 0, 11, 11, null);
				g2d.dispose();
				JLabel iconLabel = new JLabel(new ImageIcon(scaled));
				iconLabel.setPreferredSize(new Dimension(11, 11));
				iconLabel.setMaximumSize(new Dimension(11, 11));
				iconLabel.setToolTipText(tag.getLabel());
				return iconLabel;
			}
			catch (Exception ignored) {}
		}

		return createTagPill(tag);
	}

	private static JLabel createTagPill(ItemTag tag)
	{
		JLabel pill = new JLabel(tag.getLabel())
		{
			@Override
			protected void paintComponent(Graphics g)
			{
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				Color c = tag.getCategory().getColor();
				g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 50));
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
				g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 120));
				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
				g2.dispose();
				super.paintComponent(g);
			}
		};
		pill.setForeground(tag.getCategory().getColor());
		pill.setFont(pill.getFont().deriveFont(Font.PLAIN, 9f));
		pill.setBorder(new EmptyBorder(1, 5, 1, 5));
		pill.setOpaque(false);
		return pill;
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

		// Green glow: when goal is complete OR item goal meets/exceeds target
		boolean showGreen = goal.getStatus() == GoalStatus.COMPLETE
			|| (goal.getType() == GoalType.ITEM_GRIND && goal.getCurrentValue() >= 0
				&& goal.getCurrentValue() >= goal.getTargetValue());

		if (showGreen)
		{
			// Green tint overlay
			g2.setColor(new Color(76, 175, 80, 50));
			g2.fillRoundRect(0, 0, w, h, CORNER_RADIUS, CORNER_RADIUS);

			// Green border
			g2.setColor(new Color(76, 175, 80));
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
				line1 = goal.getSkillName() != null
					? net.runelite.api.Skill.valueOf(goal.getSkillName()).getName()
					: goal.getName();
				int currentLevel = goal.getCurrentValue() > 0
					? net.runelite.api.Experience.getLevelForXp(goal.getCurrentValue()) : 0;
				int targetLevel = goal.getTargetValue() > 0
					? net.runelite.api.Experience.getLevelForXp(goal.getTargetValue()) : 0;
				line2 = "Lv " + currentLevel + " / " + targetLevel;
				break;
			case ITEM_GRIND:
				line1 = FormatUtil.truncate(goal.getName(), 22);
				if (goal.getCurrentValue() < 0)
				{
					line2 = "? / " + FormatUtil.formatNumber(goal.getTargetValue());
				}
				else
				{
					line2 = FormatUtil.formatNumber(goal.getCurrentValue()) + " / " + FormatUtil.formatNumber(goal.getTargetValue());
				}
				break;
			case CUSTOM:
			default:
				line1 = FormatUtil.truncate(goal.getName(), 22);
				line2 = (goal.getDescription() != null && !goal.getDescription().isEmpty())
					? FormatUtil.truncate(goal.getDescription(), 30)
					: "";
				break;
		}

		if (line2.isEmpty())
		{
			return FormatUtil.escapeHtml(line1);
		}

		return "<html>" + FormatUtil.escapeHtml(line1)
			+ "<br><span style='font-size:9px; color:#a0a0a0'>"
			+ FormatUtil.escapeHtml(line2) + "</span></html>";
	}



	private String formatPercent()
	{
		if (goal.getStatus() == GoalStatus.COMPLETE)
		{
			return "\u2713";
		}
		if (goal.getType() == GoalType.CUSTOM)
		{
			return "";
		}
		// Combat achievements are binary and have no meaningful progress metric; show nothing
		// until the goal is marked complete (handled above via the check mark).
		if (goal.getType() == GoalType.COMBAT_ACHIEVEMENT)
		{
			return "";
		}
		if (goal.getType() == GoalType.ITEM_GRIND && goal.getCurrentValue() < 0)
		{
			return "?";
		}
		if (goal.getType() == GoalType.SKILL && goal.getTargetValue() > 0)
		{
			int remaining = Math.max(0, goal.getTargetValue() - goal.getCurrentValue());
			return "<html>"
				+ FormatUtil.formatNumber(goal.getCurrentValue()) + " / " + FormatUtil.formatNumber(goal.getTargetValue())
				+ " (" + String.format("%.0f%%", goal.getProgressPercent()) + ")"
				+ "<br><span style='font-size:9px; color:#a0a0a0'>"
				+ FormatUtil.formatXp(remaining) + " left</span></html>";
		}
		return String.format("%.0f%%", goal.getProgressPercent());
	}

}
