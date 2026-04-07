package com.goaltracker.ui;

import com.goaltracker.api.GoalView;
import com.goaltracker.api.TagView;
import com.goaltracker.util.FormatUtil;
import net.runelite.api.Skill;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Individual goal card. Consumes a {@link GoalView} (the public DTO) — does not
 * touch the internal {@code Goal} model directly. The panel obtains GoalViews
 * via {@code GoalTrackerApi.queryAllGoals()} so the read path is the same one
 * external consumer plugins use.
 */
public class GoalCard extends JPanel
{
	private static final Color BACKGROUND = new Color(30, 30, 30);
	private static final Color BACKGROUND_COMPLETE = new Color(50, 50, 50);
	private static final Color TEXT_PRIMARY = new Color(230, 230, 230);
	private static final Color TEXT_SECONDARY = new Color(160, 160, 160);
	private static final Color ARROW_COLOR = new Color(180, 180, 180);
	private static final Color ARROW_HOVER = Color.WHITE;
	private static final int CARD_HEIGHT = 48;
	private static final int TAG_ROW_HEIGHT = 18;
	private static final int CORNER_RADIUS = 8;

	/**
	 * Approximate width in pixels available to the goal-name label after icon,
	 * status, and arrow components are subtracted from the sidebar. Used by the
	 * pixel-based truncation helper so we don't lose characters to char-count
	 * truncation followed by Swing's hard clip on overflow.
	 */
	private static final int NAME_WIDTH_PX = 130;

	private GoalView view;
	private final JLabel nameLabel;
	private final JLabel statusLabel;
	private final JButton upButton;
	private final JButton downButton;

	private final SkillIconManager skillIconManager;
	private final SpriteManager spriteManager;

	public GoalCard(GoalView view, ActionListener onMoveUp, ActionListener onMoveDown,
					SkillIconManager skillIconManager, ItemManager itemManager,
					SpriteManager spriteManager)
	{
		this.view = view;
		this.skillIconManager = skillIconManager;
		this.spriteManager = spriteManager;

		// Tags are hidden on completed cards to save vertical space — completed
		// goals are reference history, not active tracking, so the tag row is noise.
		List<TagView> allTags = view.completedAt > 0 ? java.util.Collections.emptyList() : combinedTags(view);
		boolean hasTags = !allTags.isEmpty();
		int height = hasTags ? CARD_HEIGHT + TAG_ROW_HEIGHT : CARD_HEIGHT;

		setLayout(new BorderLayout(4, 0));
		setBorder(new EmptyBorder(4, 10, 4, 4));
		setPreferredSize(new Dimension(0, height));
		setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
		setOpaque(false);

		// Left: icon + name (two lines)
		JPanel leftPanel = new JPanel(new BorderLayout(6, 0));
		leftPanel.setOpaque(false);

		JLabel iconLabel = buildIcon(itemManager);
		iconLabel.setPreferredSize(new Dimension(18, 18));
		leftPanel.add(iconLabel, BorderLayout.WEST);

		nameLabel = new JLabel(formatNameHtml());
		nameLabel.setForeground(TEXT_PRIMARY);
		nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 12f));
		nameLabel.setVerticalAlignment(SwingConstants.CENTER);

		// Tooltip: prefer the type-specific tooltip from attributes, fall back to
		// truncated-name tooltip when the name is too long to fit.
		String tooltip = (String) view.attributes.get("tooltip");
		if (tooltip != null && !tooltip.isEmpty())
		{
			setToolTipText(tooltip);
		}
		else if (view.name != null && view.name.length() > 22)
		{
			setToolTipText(view.name);
		}

		JPanel nameAndTags = new JPanel(new BorderLayout(0, 0));
		nameAndTags.setOpaque(false);
		nameAndTags.add(nameLabel, BorderLayout.CENTER);

		if (hasTags)
		{
			nameAndTags.add(buildTagRow(allTags), BorderLayout.SOUTH);
		}

		leftPanel.add(nameAndTags, BorderLayout.CENTER);

		// Right side: status (XP, percent, checkmark, etc.)
		statusLabel = new JLabel(formatPercent());
		statusLabel.setForeground(TEXT_PRIMARY);
		statusLabel.setFont(statusLabel.getFont().deriveFont(11f));
		statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		statusLabel.setVerticalAlignment(SwingConstants.CENTER);

		// Right: up/down arrows. Hidden for completed cards (no reordering) so the
		// status label gets full right-side width instead of leaving a dead gap.
		upButton = createArrowButton(true, onMoveUp);
		downButton = createArrowButton(false, onMoveDown);

		add(leftPanel, BorderLayout.WEST);
		add(statusLabel, BorderLayout.CENTER);

		if (!isComplete())
		{
			JPanel arrowPanel = new JPanel(new GridLayout(2, 1, 0, 0));
			arrowPanel.setOpaque(false);
			arrowPanel.setPreferredSize(new Dimension(20, CARD_HEIGHT - 12));
			arrowPanel.add(upButton);
			arrowPanel.add(downButton);
			add(arrowPanel, BorderLayout.EAST);
		}
	}

	/** Combine default + custom tags into a single render list. */
	private static List<TagView> combinedTags(GoalView view)
	{
		List<TagView> out = new ArrayList<>();
		if (view.defaultTags != null) out.addAll(view.defaultTags);
		if (view.customTags != null) out.addAll(view.customTags);
		return out;
	}

	private JLabel buildIcon(ItemManager itemManager)
	{
		// Skill icon
		if ("SKILL".equals(view.type))
		{
			String skillName = (String) view.attributes.get("skillName");
			if (skillName != null && skillIconManager != null)
			{
				try
				{
					Skill skill = Skill.valueOf(skillName);
					return new JLabel(new ImageIcon(skillIconManager.getSkillImage(skill, true)));
				}
				catch (Exception ignored) {}
			}
			return makeColorDot(backgroundColor());
		}

		// Item image
		if ("ITEM_GRIND".equals(view.type))
		{
			Object idObj = view.attributes.get("itemId");
			if (idObj instanceof Number && itemManager != null)
			{
				int itemId = ((Number) idObj).intValue();
				if (itemId > 0)
				{
					JLabel label = new JLabel();
					ItemImageCache.applyTo(label, itemId, itemManager);
					return label;
				}
			}
		}

		// Sprite (CA tier sword, quest book, diary book, etc.)
		if (view.spriteId > 0 && spriteManager != null)
		{
			final JLabel spriteLabel = new JLabel();
			spriteManager.getSpriteAsync(view.spriteId, 0, img ->
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
			return spriteLabel;
		}

		return makeColorDot(backgroundColor());
	}

	private JPanel buildTagRow(List<TagView> allTags)
	{
		JPanel tagRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
		tagRow.setOpaque(false);
		tagRow.setBorder(new EmptyBorder(0, 0, 0, 0));

		// Sort: SKILLING first, then others
		List<TagView> sorted = new ArrayList<>(allTags);
		sorted.sort((a, b) -> {
			boolean aSkill = "SKILLING".equals(a.category);
			boolean bSkill = "SKILLING".equals(b.category);
			if (aSkill != bSkill) return aSkill ? -1 : 1;
			return 0;
		});

		// Collapse 3+ boss tags into "Multiple"
		List<TagView> bossTags = new ArrayList<>();
		List<TagView> otherTags = new ArrayList<>();
		for (TagView tag : sorted)
		{
			if ("BOSS".equals(tag.category)) bossTags.add(tag);
			else otherTags.add(tag);
		}

		for (TagView tag : otherTags)
		{
			tagRow.add(createTagComponent(tag));
		}

		if (bossTags.size() >= 3)
		{
			// Synthesize a "Multiple" pill using the BOSS category color from the
			// first boss tag (they all share the BOSS color).
			TagView multi = new TagView("Multiple", "BOSS", bossTags.get(0).colorRgb);
			JComponent pill = createTagComponent(multi);
			StringBuilder tooltip = new StringBuilder("<html>Dropped by:<br>");
			for (TagView bt : bossTags)
			{
				tooltip.append("• ").append(FormatUtil.escapeHtml(bt.label)).append("<br>");
			}
			tooltip.append("</html>");
			pill.setToolTipText(tooltip.toString());
			tagRow.add(pill);
		}
		else
		{
			for (TagView tag : bossTags) tagRow.add(createTagComponent(tag));
		}

		return tagRow;
	}

	/** Convert the packed RGB on a TagView to a Swing Color. */
	private static Color tagColor(TagView tag)
	{
		return new Color(tag.colorRgb);
	}

	private JComponent createTagComponent(TagView tag)
	{
		// For Skilling tags, try to show the skill icon
		if ("SKILLING".equals(tag.category) && skillIconManager != null)
		{
			try
			{
				Skill skill = Skill.valueOf(tag.label.toUpperCase());
				java.awt.image.BufferedImage img = skillIconManager.getSkillImage(skill, true);
				java.awt.image.BufferedImage scaled = new java.awt.image.BufferedImage(11, 11, java.awt.image.BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2d = scaled.createGraphics();
				g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2d.drawImage(img, 0, 0, 11, 11, null);
				g2d.dispose();
				JLabel iconLabel = new JLabel(new ImageIcon(scaled));
				iconLabel.setPreferredSize(new Dimension(11, 11));
				iconLabel.setMaximumSize(new Dimension(11, 11));
				iconLabel.setToolTipText(tag.label);
				return iconLabel;
			}
			catch (Exception ignored) {}
		}

		return createTagPill(tag);
	}

	private static JLabel createTagPill(TagView tag)
	{
		JLabel pill = new JLabel(tag.label)
		{
			@Override
			protected void paintComponent(Graphics g)
			{
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				Color c = tagColor(tag);
				g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 50));
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
				g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 120));
				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
				g2.dispose();
				super.paintComponent(g);
			}
		};
		pill.setForeground(tagColor(tag));
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

	private JButton createArrowButton(boolean up, ActionListener action)
	{
		final int iconSize = 7;
		final javax.swing.Icon idle = up
			? ShapeIcons.upTriangle(iconSize, ARROW_COLOR)
			: ShapeIcons.downTriangle(iconSize, ARROW_COLOR);
		final javax.swing.Icon hover = up
			? ShapeIcons.upTriangle(iconSize, ARROW_HOVER)
			: ShapeIcons.downTriangle(iconSize, ARROW_HOVER);

		JButton btn = new JButton(idle);
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		btn.setFocusPainted(false);
		btn.setMargin(new Insets(0, 0, 0, 0));
		btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btn.addActionListener(action);

		btn.addMouseListener(new java.awt.event.MouseAdapter()
		{
			@Override
			public void mouseEntered(java.awt.event.MouseEvent e) { btn.setIcon(hover); }
			@Override
			public void mouseExited(java.awt.event.MouseEvent e) { btn.setIcon(idle); }
		});

		return btn;
	}

	public void update(GoalView view)
	{
		this.view = view;
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

	private Color backgroundColor()
	{
		return new Color(view.backgroundColorRgb);
	}

	private boolean isComplete()
	{
		return view.completedAt > 0;
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int w = getWidth();
		int h = getHeight();
		Color baseColor = backgroundColor();

		// Background: type-colored tint over a flat base
		g2.setColor(isComplete() ? BACKGROUND_COMPLETE : BACKGROUND);
		g2.fillRoundRect(0, 0, w, h, CORNER_RADIUS, CORNER_RADIUS);

		Color tint = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 40);
		g2.setColor(tint);
		g2.fillRoundRect(0, 0, w, h, CORNER_RADIUS, CORNER_RADIUS);

		g2.dispose();
		super.paintComponent(g);
	}

	/**
	 * Truncate {@code text} to fit within {@code maxPx} pixels using the given
	 * font, appending an ellipsis when truncation is needed. Uses
	 * {@link FontMetrics#stringWidth} for accurate per-glyph width — replaces
	 * char-count truncation which double-clipped wide CA titles.
	 */
	/** Throwaway component used solely to obtain FontMetrics in a label-independent
	 *  way. Avoids the order-of-init issue where formatNameHtml() runs as an
	 *  argument to {@code new JLabel(...)} before the nameLabel field is assigned.
	 */
	private static final Canvas FONT_METRICS_CANVAS = new Canvas();

	private String truncateToWidth(String text, Font font, int maxPx)
	{
		if (text == null || text.isEmpty()) return text;
		FontMetrics fm = FONT_METRICS_CANVAS.getFontMetrics(font);
		if (fm.stringWidth(text) <= maxPx) return text;
		String ellipsis = "\u2026";
		int ellipsisWidth = fm.stringWidth(ellipsis);
		int budget = maxPx - ellipsisWidth;
		if (budget <= 0) return ellipsis;
		StringBuilder sb = new StringBuilder();
		int width = 0;
		for (int i = 0; i < text.length(); i++)
		{
			int cw = fm.charWidth(text.charAt(i));
			if (width + cw > budget) break;
			sb.append(text.charAt(i));
			width += cw;
		}
		return sb.toString().trim() + ellipsis;
	}

	// Use FlatLaf default UI font as the base — matches what JLabel.getFont()
	// resolves to without depending on the nameLabel field being initialized.
	private static final Font NAME_FONT = UIManager.getFont("Label.font") != null
		? UIManager.getFont("Label.font").deriveFont(Font.BOLD, 12f)
		: new Font(Font.DIALOG, Font.BOLD, 12);
	private static final Font DESC_FONT = UIManager.getFont("Label.font") != null
		? UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 9f)
		: new Font(Font.DIALOG, Font.PLAIN, 9);

	private String fitName(String text)
	{
		return truncateToWidth(text, NAME_FONT, NAME_WIDTH_PX);
	}

	private String fitDescription(String text)
	{
		return truncateToWidth(text, DESC_FONT, NAME_WIDTH_PX);
	}

	private String formatNameHtml()
	{
		String line1;
		String line2;

		switch (view.type == null ? "CUSTOM" : view.type)
		{
			case "SKILL":
				String skillName = (String) view.attributes.get("skillName");
				line1 = skillName != null ? Skill.valueOf(skillName).getName() : view.name;
				int currentLevel = view.currentValue > 0
					? net.runelite.api.Experience.getLevelForXp(view.currentValue) : 0;
				int targetLevel = view.targetValue > 0
					? net.runelite.api.Experience.getLevelForXp(view.targetValue) : 0;
				line2 = "Lv " + currentLevel + " / " + targetLevel;
				break;
			case "ITEM_GRIND":
				line1 = fitName(view.name);
				if (view.currentValue < 0)
				{
					line2 = "? / " + FormatUtil.formatNumber(view.targetValue);
				}
				else
				{
					line2 = FormatUtil.formatNumber(view.currentValue) + " / "
						+ FormatUtil.formatNumber(view.targetValue);
				}
				break;
			case "DIARY":
				// Diary title carries tier suffix so the date description on completed
				// cards doesn't drop the tier info.
				String tier = (String) view.attributes.get("tier");
				String tierWord = tier != null
					? tier.substring(0, 1) + tier.substring(1).toLowerCase()
					: "";
				line1 = tierWord.isEmpty()
					? fitName(view.name)
					: fitName(view.name + " - " + tierWord);
				line2 = (view.description != null && !view.description.isEmpty())
					? fitDescription(view.description)
					: "";
				break;
			case "CUSTOM":
			default:
				line1 = fitName(view.name);
				line2 = (view.description != null && !view.description.isEmpty())
					? fitDescription(view.description)
					: "";
				break;
		}

		// Completed goals: replace line 2 with the completion date.
		if (isComplete())
		{
			line2 = "Completed " + formatCompletionDate(view.completedAt);
		}

		if (line2.isEmpty())
		{
			return FormatUtil.escapeHtml(line1);
		}

		return "<html>" + FormatUtil.escapeHtml(line1)
			+ "<br><span style='font-size:9px; color:#a0a0a0'>"
			+ FormatUtil.escapeHtml(line2) + "</span></html>";
	}

	private static String formatCompletionDate(long epochMillis)
	{
		java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("MMM d, yyyy");
		return fmt.format(new java.util.Date(epochMillis));
	}

	private String formatPercent()
	{
		if (isComplete())
		{
			return "\u2713";
		}
		String type = view.type == null ? "CUSTOM" : view.type;
		if ("CUSTOM".equals(type))
		{
			return "";
		}
		// Combat achievements, quests, and diaries are binary; no progress shown
		// until complete (handled above via the check mark).
		if ("COMBAT_ACHIEVEMENT".equals(type) || "QUEST".equals(type) || "DIARY".equals(type))
		{
			return "";
		}
		if ("ITEM_GRIND".equals(type) && view.currentValue < 0)
		{
			return "?";
		}
		if ("SKILL".equals(type) && view.targetValue > 0)
		{
			int remaining = Math.max(0, view.targetValue - view.currentValue);
			double pct = view.targetValue == 0 ? 0
				: Math.max(0.0, Math.min(100.0, (view.currentValue * 100.0) / view.targetValue));
			return "<html>"
				+ FormatUtil.formatNumber(view.currentValue) + " / " + FormatUtil.formatNumber(view.targetValue)
				+ " (" + String.format("%.0f%%", pct) + ")"
				+ "<br><span style='font-size:9px; color:#a0a0a0'>"
				+ FormatUtil.formatXp(remaining) + " left</span></html>";
		}
		double pct = view.targetValue == 0 ? 0
			: Math.max(0.0, Math.min(100.0, (view.currentValue * 100.0) / view.targetValue));
		return String.format("%.0f%%", pct);
	}
}
