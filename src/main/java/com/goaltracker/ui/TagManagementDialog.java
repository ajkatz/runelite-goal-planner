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
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
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
	private final JTabbedPane tabs;

	public TagManagementDialog(Frame owner, GoalTrackerApiImpl api)
	{
		super(owner, "Manage Tags", true);
		this.api = api;

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
		tabs.addTab("All (" + allTags.size() + ")", buildTabContent(sortedAll));

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
			tabs.addTab(tabLabel, buildTabContent(filtered));
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
	 * Build the scrollable content for a single tab from a (sorted) tag list.
	 * Empty lists get a "no tags in this category" placeholder.
	 */
	private JScrollPane buildTabContent(List<TagView> tags)
	{
		JPanel listPanel = new JPanel();
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		listPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

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

	private JPanel buildRow(TagView tag)
	{
		JPanel row = new JPanel(new BorderLayout(6, 0));
		row.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 60, 60)),
			new EmptyBorder(6, 4, 6, 4)
		));
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
		row.setAlignmentX(Component.LEFT_ALIGNMENT);

		// Color swatch on the left — paintComponent so FlatLaf doesn't override it
		final int rgb = tag.colorRgb;
		JPanel swatch = new JPanel()
		{
			@Override
			protected void paintComponent(Graphics g)
			{
				g.setColor(new Color(rgb));
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		};
		swatch.setOpaque(false);
		swatch.setPreferredSize(new Dimension(20, 20));
		swatch.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1));

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

		JButton recolor = new JButton("Recolor");
		recolor.setMargin(new java.awt.Insets(2, 6, 2, 6));
		boolean recolorAllowed = !(tag.system && "SKILLING".equals(tag.category));
		recolor.setEnabled(recolorAllowed);
		recolor.addActionListener(e -> handleRecolor(tag));
		actions.add(recolor);

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

		TagCategory[] categories = TagCategory.values();
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
