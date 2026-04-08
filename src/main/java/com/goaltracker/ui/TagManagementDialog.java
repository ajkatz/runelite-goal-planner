package com.goaltracker.ui;

import com.goaltracker.api.GoalTrackerApiImpl;
import com.goaltracker.api.TagView;
import com.goaltracker.model.TagCategory;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Tag management dialog. Lists every tag entity in the store with per-row
 * Rename / Recolor / Delete actions. Used for the entity-level operations
 * that don't fit naturally on individual goal cards.
 *
 * <p>System tag rules apply (Mission 19):
 * <ul>
 *   <li>System tags can NOT be renamed (button disabled)</li>
 *   <li>System tags in the SKILLING category can NOT be recolored (button disabled)</li>
 *   <li>System tags can NOT be deleted (button disabled)</li>
 * </ul>
 *
 * <p>The dialog rebuilds itself after every action via the API's
 * onGoalsChanged callback (which is shared with the panel rebuild).
 */
public class TagManagementDialog extends JDialog
{
	private final GoalTrackerApiImpl api;
	private final net.runelite.client.game.SkillIconManager skillIconManager;
	private final net.runelite.client.game.ItemManager itemManager;
	private final JTabbedPane tabs;

	public TagManagementDialog(Frame owner, GoalTrackerApiImpl api,
		net.runelite.client.game.SkillIconManager skillIconManager,
		net.runelite.client.game.ItemManager itemManager)
	{
		super(owner, "Manage Tags", true);
		this.api = api;
		this.skillIconManager = skillIconManager;
		this.itemManager = itemManager;

		setLayout(new BorderLayout());
		setSize(420, 540);
		setLocationRelativeTo(owner);

		// Header row: title on the left, "+ New Tag" button on the right
		JPanel headerRow = new JPanel(new BorderLayout());
		headerRow.setBorder(new EmptyBorder(10, 12, 8, 12));

		JLabel header = new JLabel("Tag Management", SwingConstants.LEFT);
		header.setFont(header.getFont().deriveFont(Font.BOLD, 14f));
		headerRow.add(header, BorderLayout.WEST);

		JButton newTagBtn = new JButton("+ New Tag");
		newTagBtn.setMargin(new java.awt.Insets(2, 8, 2, 8));
		newTagBtn.addActionListener(e -> handleNewTag());
		headerRow.add(newTagBtn, BorderLayout.EAST);

		add(headerRow, BorderLayout.NORTH);

		tabs = new JTabbedPane();
		add(tabs, BorderLayout.CENTER);

		JButton close = new JButton("Close");
		close.addActionListener(e -> dispose());
		JPanel buttonRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
		buttonRow.setBorder(new EmptyBorder(4, 8, 8, 8));
		buttonRow.add(close);
		add(buttonRow, BorderLayout.SOUTH);

		rebuildList();
	}

	/**
	 * Rebuild the per-category tabs from the current tag store. Called after
	 * every mutation so the UI reflects the latest state. Each TagCategory
	 * gets its own tab with the count appended (e.g. "Boss (3)"); the All
	 * tab combines everything.
	 */
	private void rebuildList()
	{
		// Remember which tab the user was on so the rebuild doesn't reset it
		int previousIndex = tabs.getSelectedIndex();
		tabs.removeAll();

		List<TagView> allTags = api.queryAllTags();

		// All tab — combined view, sorted by category then user-first then label
		List<TagView> sortedAll = new java.util.ArrayList<>(allTags);
		sortedAll.sort((a, b) -> {
			int catCompare = a.category.compareTo(b.category);
			if (catCompare != 0) return catCompare;
			if (a.system != b.system) return a.system ? 1 : -1;
			return a.label.compareToIgnoreCase(b.label);
		});
		tabs.addTab("All (" + allTags.size() + ")", buildTabContent(sortedAll, null));

		// Per-category tabs in enum declaration order. Empty categories still
		// get a tab so the user can create new tags directly into them.
		for (TagCategory cat : TagCategory.values())
		{
			List<TagView> filtered = new java.util.ArrayList<>();
			for (TagView t : allTags)
			{
				if (cat.name().equals(t.category)) filtered.add(t);
			}
			filtered.sort((a, b) -> {
				if (a.system != b.system) return a.system ? 1 : -1;
				return a.label.compareToIgnoreCase(b.label);
			});
			String tabLabel = cat.getDisplayName() + " (" + filtered.size() + ")";
			tabs.addTab(tabLabel, buildTabContent(filtered, cat));
		}

		// Restore the previously-selected tab if still valid
		if (previousIndex >= 0 && previousIndex < tabs.getTabCount())
		{
			tabs.setSelectedIndex(previousIndex);
		}

		tabs.revalidate();
		tabs.repaint();
	}

	/**
	 * Build the scrollable content for a single tab. For non-OTHER non-SKILLING
	 * categories, prepends a "Category color" header so the user can recolor
	 * the entire category from the tab. The All tab (category == null) and
	 * the OTHER + SKILLING tabs skip the header.
	 */
	private JScrollPane buildTabContent(List<TagView> tags, TagCategory category)
	{
		JPanel listPanel = new JPanel();
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		listPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

		// Category color header — Mission 20. Skipped for the All tab and the
		// OTHER tab (which uses per-tag colors instead). SKILLING DOES get
		// the header — system skill tags render as icons (color ignored) but
		// user-created SKILLING tags fall through to colored pills where the
		// category color applies.
		if (category != null && category != TagCategory.OTHER && category != TagCategory.SKILLING)
		{
			listPanel.add(buildCategoryColorHeader(category));
			listPanel.add(Box.createVerticalStrut(8));
		}

		if (tags.isEmpty())
		{
			JLabel empty = new JLabel("No tags in this category.");
			empty.setForeground(new Color(140, 140, 140));
			empty.setAlignmentX(Component.LEFT_ALIGNMENT);
			empty.setBorder(new EmptyBorder(8, 4, 8, 4));
			listPanel.add(empty);
		}
		else
		{
			for (TagView t : tags)
			{
				listPanel.add(buildRow(t));
				listPanel.add(Box.createVerticalStrut(4));
			}
		}

		JScrollPane scroll = new JScrollPane(listPanel);
		scroll.setBorder(null);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		return scroll;
	}

	/**
	 * Build the category color header shown at the top of non-OTHER non-SKILLING
	 * tag tabs. Displays the current category color swatch + Edit + Reset buttons.
	 * Recoloring affects every tag in the category.
	 */
	private JPanel buildCategoryColorHeader(TagCategory category)
	{
		JPanel header = new JPanel(new BorderLayout(8, 0));
		header.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 60, 60)),
			new EmptyBorder(4, 4, 8, 4)
		));
		header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
		header.setAlignmentX(Component.LEFT_ALIGNMENT);

		// Query directly from the API so empty categories still get accurate
		// override info (otherwise the header is stuck on the enum default).
		int currentRgb = api.getCategoryColor(category.name());
		int defaultRgb = api.getCategoryDefaultColor(category.name());
		boolean overridden = api.isCategoryColorOverridden(category.name());

		final int finalCurrent = currentRgb;
		JPanel swatch = new JPanel()
		{
			@Override
			protected void paintComponent(java.awt.Graphics g)
			{
				g.setColor(new Color(finalCurrent));
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		};
		swatch.setOpaque(false);
		swatch.setPreferredSize(new Dimension(24, 24));
		swatch.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1));

		String labelText = "Category color " + (overridden ? "(custom)" : "(default)");
		JLabel label = new JLabel(labelText);
		label.setForeground(Color.WHITE);

		JPanel left = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
		left.setOpaque(false);
		left.add(swatch);
		left.add(label);
		header.add(left, BorderLayout.CENTER);

		JPanel actions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 4, 0));
		actions.setOpaque(false);
		final int finalDefault = defaultRgb;

		JButton edit = new JButton("Edit");
		edit.setMargin(new java.awt.Insets(2, 8, 2, 8));
		edit.addActionListener(e -> {
			ColorPickerField picker = new ColorPickerField(
				api.isCategoryColorOverridden(category.name())
					? api.getCategoryColor(category.name()) : -1,
				finalDefault);
			int result = JOptionPane.showConfirmDialog(this, picker,
				category.getDisplayName() + " category color (affects every "
					+ category.getDisplayName() + " tag)",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (result != JOptionPane.OK_OPTION) return;
			api.setCategoryColor(category.name(), picker.getSelectedRgb());
			rebuildList();
		});
		actions.add(edit);

		JButton reset = new JButton("Reset");
		reset.setMargin(new java.awt.Insets(2, 8, 2, 8));
		reset.setEnabled(overridden);
		reset.addActionListener(e -> {
			api.resetCategoryColor(category.name());
			rebuildList();
		});
		actions.add(reset);

		header.add(actions, BorderLayout.EAST);
		return header;
	}

	private JPanel buildRow(TagView tag)
	{
		JPanel row = new JPanel(new BorderLayout(6, 0));
		row.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 60, 60)),
			new EmptyBorder(6, 4, 6, 4)
		));
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
		row.setAlignmentX(Component.LEFT_ALIGNMENT);

		// Mission 21: when an icon is set, render the icon in place of the
		// color swatch. The resolved icon mirrors the in-card render path so
		// the user sees the same image they'll see on goal cards.
		java.awt.Component swatch;
		java.awt.image.BufferedImage iconImage = (tag.iconKey != null && !tag.iconKey.isEmpty())
			? resolveIconForRow(tag.iconKey) : null;
		if (iconImage != null)
		{
			java.awt.image.BufferedImage scaled = new java.awt.image.BufferedImage(
				20, 20, java.awt.image.BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = scaled.createGraphics();
			g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
				java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.drawImage(iconImage, 0, 0, 20, 20, null);
			g2.dispose();
			JLabel iconLabel = new JLabel(new javax.swing.ImageIcon(scaled));
			iconLabel.setPreferredSize(new Dimension(20, 20));
			iconLabel.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1));
			swatch = iconLabel;
		}
		else
		{
			final int rgb = tag.colorRgb;
			JPanel pillSwatch = new JPanel()
			{
				@Override
				protected void paintComponent(Graphics g)
				{
					g.setColor(new Color(rgb));
					g.fillRect(0, 0, getWidth(), getHeight());
				}
			};
			pillSwatch.setOpaque(false);
			pillSwatch.setPreferredSize(new Dimension(20, 20));
			pillSwatch.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1));
			swatch = pillSwatch;
		}

		// Label + category + system marker
		String displayLabel = tag.label
			+ "  (" + TagCategory.valueOf(tag.category).getDisplayName() + ")"
			+ (tag.system ? "  [system]" : "");
		JLabel label = new JLabel(displayLabel);
		label.setForeground(tag.system ? new Color(160, 160, 160) : Color.WHITE);

		JPanel left = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
		left.setOpaque(false);
		left.add(swatch);
		left.add(label);
		row.add(left, BorderLayout.CENTER);

		// Action buttons (Rename / Recolor / Delete)
		JPanel actions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 4, 0));
		actions.setOpaque(false);

		JButton rename = new JButton("Rename");
		rename.setMargin(new java.awt.Insets(2, 6, 2, 6));
		rename.setEnabled(!tag.system);
		rename.addActionListener(e -> handleRename(tag));
		actions.add(rename);

		// Mission 20: per-tag recolor is OTHER-only. Other categories use the
		// per-tab category color header. SKILLING is fully read-only.
		JButton recolor = new JButton("Recolor");
		recolor.setMargin(new java.awt.Insets(2, 6, 2, 6));
		recolor.setEnabled("OTHER".equals(tag.category));
		recolor.addActionListener(e -> handleRecolor(tag));
		actions.add(recolor);

		// Icon edit is allowed on any tag EXCEPT SKILLING. SKILLING tags are
		// locked because seedCanonicalSystemTags reseats their icon to the
		// matching skill image on every plugin start, so user edits would be
		// silently overwritten. Every other category (BOSS/RAID/MINIGAME/CLUE/
		// OTHER), system or user-created, can have its icon edited freely.
		JButton icon = new JButton("Icon");
		icon.setMargin(new java.awt.Insets(2, 6, 2, 6));
		icon.setEnabled(!"SKILLING".equals(tag.category));
		icon.addActionListener(e -> handleIcon(tag));
		actions.add(icon);

		JButton delete = new JButton("Delete");
		delete.setMargin(new java.awt.Insets(2, 6, 2, 6));
		delete.setEnabled(!tag.system);
		delete.addActionListener(e -> handleDelete(tag));
		actions.add(delete);

		row.add(actions, BorderLayout.EAST);
		return row;
	}

	private void handleNewTag()
	{
		// Two-field dialog: label + category dropdown.
		javax.swing.JPanel form = new javax.swing.JPanel(new java.awt.GridBagLayout());
		java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
		gbc.insets = new java.awt.Insets(4, 4, 4, 4);
		gbc.anchor = java.awt.GridBagConstraints.WEST;

		JLabel labelLbl = new JLabel("Label:");
		labelLbl.setPreferredSize(new Dimension(80, 24));
		gbc.gridx = 0; gbc.gridy = 0; gbc.fill = java.awt.GridBagConstraints.NONE; gbc.weightx = 0;
		form.add(labelLbl, gbc);

		javax.swing.JTextField labelField = new javax.swing.JTextField(15);
		gbc.gridx = 1; gbc.fill = java.awt.GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
		form.add(labelField, gbc);

		JLabel catLbl = new JLabel("Category:");
		catLbl.setPreferredSize(new Dimension(80, 24));
		gbc.gridx = 0; gbc.gridy = 1; gbc.fill = java.awt.GridBagConstraints.NONE; gbc.weightx = 0;
		form.add(catLbl, gbc);

		// SKILLING is excluded — skill tags are seeded by the plugin and
		// users can't create new ones.
		java.util.List<TagCategory> catList = new java.util.ArrayList<>();
		for (TagCategory c : TagCategory.values())
		{
			if (c != TagCategory.SKILLING) catList.add(c);
		}
		TagCategory[] categories = catList.toArray(new TagCategory[0]);
		javax.swing.JComboBox<TagCategory> catCombo = new javax.swing.JComboBox<>(categories);
		catCombo.setRenderer(new javax.swing.DefaultListCellRenderer()
		{
			@Override
			public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value,
				int index, boolean isSelected, boolean cellHasFocus)
			{
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof TagCategory)
				{
					setText(((TagCategory) value).getDisplayName());
				}
				return this;
			}
		});
		catCombo.setSelectedItem(TagCategory.OTHER);
		gbc.gridx = 1; gbc.fill = java.awt.GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
		form.add(catCombo, gbc);

		form.setPreferredSize(new Dimension(280, form.getPreferredSize().height));

		int result = JOptionPane.showConfirmDialog(this, form, "New Tag",
			JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result != JOptionPane.OK_OPTION) return;

		String label = labelField.getText();
		TagCategory cat = (TagCategory) catCombo.getSelectedItem();
		if (cat == null) return;
		try
		{
			String tagId = api.createUserTag(label, cat.name());
			if (tagId == null)
			{
				JOptionPane.showMessageDialog(this,
					"Could not create tag. Label may be invalid.",
					"Create failed", JOptionPane.WARNING_MESSAGE);
			}
		}
		catch (IllegalArgumentException ex)
		{
			JOptionPane.showMessageDialog(this, ex.getMessage(),
				"Invalid label", JOptionPane.WARNING_MESSAGE);
		}
		rebuildList();
	}

	/**
	 * Resolve an iconKey to a BufferedImage for row preview. Mirrors
	 * GoalCard.resolveIcon: item:N → ItemManager, Skill enum → SkillIconManager,
	 * else /icons/<key>.png. Returns null if all lookups fail.
	 */
	private java.awt.image.BufferedImage resolveIconForRow(String iconKey)
	{
		if (iconKey.startsWith("item:") && itemManager != null)
		{
			try
			{
				int itemId = Integer.parseInt(iconKey.substring("item:".length()));
				return itemManager.getImage(itemId);
			}
			catch (Exception ignored) {}
		}
		if (skillIconManager != null)
		{
			try
			{
				net.runelite.api.Skill skill = net.runelite.api.Skill.valueOf(iconKey.toUpperCase());
				return skillIconManager.getSkillImage(skill, true);
			}
			catch (IllegalArgumentException ignored) {}
		}
		try (java.io.InputStream in = getClass().getResourceAsStream("/icons/" + iconKey + ".png"))
		{
			if (in != null) return javax.imageio.ImageIO.read(in);
		}
		catch (java.io.IOException ignored) {}
		return null;
	}

	private void handleIcon(TagView tag)
	{
		java.awt.Window w = SwingUtilities.getWindowAncestor(this);
		java.awt.Frame owner = (w instanceof java.awt.Frame) ? (java.awt.Frame) w : null;
		String picked = IconPickerDialog.show(owner, tag.iconKey, skillIconManager, itemManager);
		if (java.util.Objects.equals(picked, tag.iconKey)) return; // no change / cancel
		if (picked == null)
		{
			api.clearTagIcon(tag.id);
		}
		else
		{
			api.setTagIcon(tag.id, picked);
		}
		rebuildList();
	}

	private void handleRename(TagView tag)
	{
		String input = (String) JOptionPane.showInputDialog(this, "New label:",
			"Rename Tag", JOptionPane.PLAIN_MESSAGE, null, null, tag.label);
		if (input == null) return;
		boolean ok = api.renameTag(tag.id, input);
		if (!ok)
		{
			JOptionPane.showMessageDialog(this,
				"Could not rename. Label may be invalid, duplicate, or unchanged.",
				"Rename failed", JOptionPane.WARNING_MESSAGE);
		}
		rebuildList();
	}

	private void handleRecolor(TagView tag)
	{
		ColorPickerField picker = new ColorPickerField(
			tag.colorOverridden ? tag.colorRgb : -1, tag.defaultColorRgb);
		int result = JOptionPane.showConfirmDialog(this, picker,
			"Color for " + tag.label + " (affects all goals using this tag)",
			JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result != JOptionPane.OK_OPTION) return;
		api.recolorTag(tag.id, picker.getSelectedRgb());
		rebuildList();
	}

	private void handleDelete(TagView tag)
	{
		int confirm = JOptionPane.showConfirmDialog(this,
			"Delete tag \"" + tag.label + "\"?\nIt will be removed from every goal using it.",
			"Delete Tag", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (confirm != JOptionPane.YES_OPTION) return;
		api.deleteTag(tag.id);
		rebuildList();
	}
}
