package com.goaltracker.ui;

import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalStatus;
import com.goaltracker.model.GoalType;
import com.goaltracker.persistence.GoalStore;
import com.goaltracker.service.GoalReorderingService;
import com.goaltracker.util.FormatUtil;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sidebar panel — priority list of goals with gradient cards and arrow reordering.
 */
@Slf4j
public class GoalPanel extends PluginPanel
{
	private final GoalStore goalStore;
	private final GoalReorderingService reorderingService;
	private final com.goaltracker.api.GoalTrackerApiImpl api;
	private final SkillIconManager skillIconManager;
	private final ItemManager itemManager;
	private final net.runelite.client.game.SpriteManager spriteManager;
	private final java.util.function.IntConsumer itemSearchCallback;
	private Client client;
	private final JPanel goalListPanel;
	private final Map<String, GoalCard> cardMap = new HashMap<>();

	public GoalPanel(GoalStore goalStore, SkillIconManager skillIconManager, ItemManager itemManager,
					 net.runelite.client.game.SpriteManager spriteManager,
					 com.goaltracker.api.GoalTrackerApiImpl api,
					 GoalReorderingService reorderingService,
					 java.util.function.IntConsumer itemSearchCallback)
	{
		super(false);
		this.goalStore = goalStore;
		this.reorderingService = reorderingService;
		this.api = api;
		this.skillIconManager = skillIconManager;
		this.itemManager = itemManager;
		this.spriteManager = spriteManager;
		this.itemSearchCallback = itemSearchCallback;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Header with add button
		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(ColorScheme.DARK_GRAY_COLOR);
		header.setBorder(new EmptyBorder(8, 8, 8, 8));

		JLabel title = new JLabel("Goal Tracker");
		title.setForeground(Color.WHITE);
		title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));

		JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
		headerButtons.setOpaque(false);

		JButton clearButton = new JButton(ShapeIcons.minus(10, new Color(200, 200, 200)));
		clearButton.setToolTipText("Clear all goals");
		clearButton.setMargin(new Insets(2, 4, 2, 4));
		clearButton.addActionListener(e -> {
			int confirm = JOptionPane.showConfirmDialog(
				this, "Remove ALL goals?", "Clear All",
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE
			);
			if (confirm == JOptionPane.YES_OPTION)
			{
				api.removeAllGoals();
				// API callback (onGoalsChanged) triggers an EDT rebuild.
			}
		});

		JButton clearSectionsButton = new JButton(ShapeIcons.minus(10, new Color(140, 180, 220)));
		clearSectionsButton.setToolTipText("Delete all custom sections");
		clearSectionsButton.setMargin(new Insets(2, 4, 2, 4));
		clearSectionsButton.addActionListener(e -> {
			int confirm = JOptionPane.showConfirmDialog(
				this,
				"Delete ALL custom sections?\nGoals in them will be moved to Incomplete.",
				"Delete All Sections",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE
			);
			if (confirm == JOptionPane.YES_OPTION)
			{
				api.removeAllUserSections();
			}
		});

		JButton addSectionButton = new JButton(ShapeIcons.plus(10, new Color(140, 180, 220)));
		addSectionButton.setToolTipText("Add a new section");
		addSectionButton.setMargin(new Insets(2, 4, 2, 4));
		addSectionButton.addActionListener(e -> showCreateSectionDialog());

		JButton addButton = new JButton(ShapeIcons.plus(10, new Color(200, 200, 200)));
		addButton.setToolTipText("Add a new goal");
		addButton.setMargin(new Insets(2, 4, 2, 4));
		addButton.addActionListener(e -> showAddGoalDialog());

		headerButtons.add(clearButton);
		headerButtons.add(clearSectionsButton);
		headerButtons.add(addSectionButton);
		headerButtons.add(addButton);

		header.add(title, BorderLayout.WEST);
		header.add(headerButtons, BorderLayout.EAST);

		// Scrollable goal list
		goalListPanel = new JPanel();
		goalListPanel.setLayout(new BoxLayout(goalListPanel, BoxLayout.Y_AXIS));
		goalListPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		goalListPanel.setBorder(new EmptyBorder(4, 8, 8, 8));

		JScrollPane scrollPane = new JScrollPane(goalListPanel);
		scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
		scrollPane.setBorder(null);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);

		add(header, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);

		rebuild();
	}

	public void setClient(Client client)
	{
		this.client = client;
	}

	public void rebuild()
	{
		goalListPanel.removeAll();
		cardMap.clear();

		// Read path goes through the public API — the panel is now a consumer of
		// GoalTrackerApi just like external plugins would be. The internal mutation
		// paths still touch goalStore directly (refactored to API in T5).
		java.util.List<com.goaltracker.api.GoalView> goalViews = api.queryAllGoals();
		java.util.List<com.goaltracker.api.SectionView> sectionViews = api.queryAllSections();

		// We still need Goal objects for the right-click context menu (T5 will route
		// those mutations through the API too). Look up by id from goalStore.
		java.util.Map<String, Goal> goalById = new java.util.HashMap<>();
		for (Goal g : goalStore.getGoals())
		{
			goalById.put(g.getId(), g);
		}

		for (com.goaltracker.api.SectionView section : sectionViews)
		{
			// Find contiguous slice of goalViews in this section
			int sectionStart = -1;
			int sectionEnd = -1;
			for (int i = 0; i < goalViews.size(); i++)
			{
				if (section.id.equals(goalViews.get(i).sectionId))
				{
					if (sectionStart == -1) sectionStart = i;
					sectionEnd = i;
				}
			}
			int sectionCount = (sectionStart == -1) ? 0 : (sectionEnd - sectionStart + 1);

			// Hide section headers for empty BUILT-IN sections only. User-defined
			// sections are always shown — even when empty — so a freshly created
			// section is visible and can be right-clicked / dropped into.
			if (sectionCount == 0 && section.builtIn) continue;
			final String sectionIdRef = section.id;
			SectionHeaderRow headerRow = new SectionHeaderRow(section, sectionCount, () -> {
				api.toggleSectionCollapsed(sectionIdRef);
				// API callback rebuilds the panel.
			});
			// User-defined sections get a right-click menu (rename/delete/reorder).
			// Built-in sections (Incomplete/Completed) are immutable.
			if (!section.builtIn)
			{
				attachSectionContextMenu(headerRow, section, sectionViews);
			}
			goalListPanel.add(headerRow);
			goalListPanel.add(Box.createVerticalStrut(2));

			// Skip rendering goal cards while the section is collapsed, or when
			// the section is empty (sectionStart == -1 → guard against the
			// goalViews.get(i) loop below running with i = -1).
			if (section.collapsed || sectionCount == 0)
			{
				continue;
			}

			boolean isCompletedSection = "COMPLETED".equals(section.kind);

			for (int i = sectionStart; i <= sectionEnd; i++)
			{
				final int index = i;
				com.goaltracker.api.GoalView view = goalViews.get(i);
				Goal goal = goalById.get(view.id);
				if (goal == null) continue; // shouldn't happen but defensive

				final int secStart = sectionStart;
				final int secEnd = sectionEnd;

				final String goalIdRef = view.id;
				GoalCard card = new GoalCard(
					view,
					e -> moveGoalBounded(goalIdRef, index, index - 1, secStart, secEnd),
					e -> moveGoalBounded(goalIdRef, index, index + 1, secStart, secEnd),
					skillIconManager,
					itemManager,
					spriteManager
				);

				// Completed section is read-only ordering — no reorder arrows.
				if (isCompletedSection)
				{
					card.setFirstInList(true);
					card.setLastInList(true);
				}
				else
				{
					card.setFirstInList(i == sectionStart);
					card.setLastInList(i == sectionEnd);
				}

				addContextMenu(card, goal, index, sectionStart, sectionEnd);
				cardMap.put(goal.getId(), card);

				goalListPanel.add(card);
				goalListPanel.add(Box.createVerticalStrut(4));
			}
		}

		if (goalViews.isEmpty())
		{
			JLabel empty = new JLabel("No goals yet. Click + to add one.");
			empty.setForeground(new Color(120, 120, 120));
			empty.setAlignmentX(Component.CENTER_ALIGNMENT);
			empty.setBorder(new EmptyBorder(20, 0, 0, 0));
			goalListPanel.add(empty);
		}

		goalListPanel.revalidate();
		goalListPanel.repaint();
	}

	/**
	 * Move a goal by one slot, bounded to its current section. Routes through the
	 * internal API so the reorder is the same canonical mutation path external
	 * plugins would use (they can't, but the panel pretends it's an external user).
	 */
	private void moveGoalBounded(String goalId, int fromIndex, int toIndex, int minIndex, int maxIndex)
	{
		if (toIndex < minIndex || toIndex > maxIndex) return;
		api.moveGoal(goalId, toIndex);
		// API callback rebuilds the panel.
	}

	/** Move a goal directly to a target index within its section. */
	private void moveGoalTo(String goalId, int toIndex)
	{
		api.moveGoal(goalId, toIndex);
		// API callback rebuilds the panel.
	}


	private void addContextMenu(GoalCard card, Goal goal, int index, int sectionStart, int sectionEnd)
	{
		JPopupMenu menu = new JPopupMenu();

		// Reorder options are hidden in the Completed section (read-only ordering)
		// and gated on section bounds so they don't appear when there's nowhere to
		// move within the section. Move-to-Top/Bottom now stay inside the section.
		if (!goal.isComplete())
		{
			if (index > sectionStart)
			{
				JMenuItem moveFirst = new JMenuItem("Move to Top");
				moveFirst.addActionListener(e -> {
					moveGoalTo(goal.getId(), sectionStart);
				});
				menu.add(moveFirst);
			}

			if (index < sectionEnd)
			{
				JMenuItem moveLast = new JMenuItem("Move to Bottom");
				moveLast.addActionListener(e -> {
					moveGoalTo(goal.getId(), sectionEnd);
				});
				menu.add(moveLast);
			}
		}

		if (menu.getComponentCount() > 0)
		{
			menu.addSeparator();
		}

		// Manual completion is CUSTOM-only. Verifiable types (quest/diary/CA/item/skill)
		// are purely game-driven; CAs without a tracking varbit simply stay incomplete
		// until the runelite-api version is updated to expose them.
		if (goal.isComplete() && goal.getType() == GoalType.CUSTOM)
		{
			JMenuItem reopen = new JMenuItem("Mark Incomplete");
			reopen.addActionListener(e -> api.markGoalIncomplete(goal.getId()));
			menu.add(reopen);
		}
		else if (!goal.isComplete() && goal.getType() == GoalType.CUSTOM)
		{
			JMenuItem complete = new JMenuItem("Mark Complete");
			complete.addActionListener(e -> api.markGoalComplete(goal.getId()));
			menu.add(complete);
		}

		if (goal.getType() == GoalType.CUSTOM && !goal.isComplete())
		{
			JMenuItem editName = new JMenuItem("Change Name");
			editName.addActionListener(e -> {
				String input = JOptionPane.showInputDialog(this, "New name:", goal.getName());
				if (input != null && !input.trim().isEmpty())
				{
					api.editCustomGoal(goal.getId(), input.trim(), null);
				}
			});
			menu.add(editName);

			JMenuItem editDesc = new JMenuItem("Change Description");
			editDesc.addActionListener(e -> {
				String input = JOptionPane.showInputDialog(this, "New description:",
					goal.getDescription() != null ? goal.getDescription() : "");
				if (input != null)
				{
					api.editCustomGoal(goal.getId(), null, input.trim());
				}
			});
			menu.add(editDesc);
		}

		// Skill-specific options
		if (goal.getType() == GoalType.SKILL && !goal.isComplete())
		{
			JMenuItem editLevel = new JMenuItem("Change Target");
			editLevel.addActionListener(e -> {
				int currentTargetLevel = goal.getTargetValue() > 0
					? net.runelite.api.Experience.getLevelForXp(goal.getTargetValue()) : 99;
				String input = JOptionPane.showInputDialog(
					this,
					"New target level for " + (goal.getSkillName() != null
					? net.runelite.api.Skill.valueOf(goal.getSkillName()).getName() : goal.getName()) + ":",
					String.valueOf(currentTargetLevel)
				);
				if (input != null)
				{
					try
					{
						int newLevel = Integer.parseInt(input.trim());
						if (newLevel >= 1 && newLevel <= 99)
						{
							int newXp = net.runelite.api.Experience.getXpForLevel(newLevel);
							api.changeTarget(goal.getId(), newXp);
							// Name update isn't part of the public changeTarget contract
							// (it's a display-side concern), so still update directly here.
							goal.setName(net.runelite.api.Skill.valueOf(goal.getSkillName()).getName()
								+ " \u2192 Level " + newLevel);
							goalStore.updateGoal(goal);
							rebuild();
						}
					}
					catch (NumberFormatException ignored) {}
				}
			});
			menu.add(editLevel);
		}

		// Item-specific options
		if (goal.getType() == GoalType.ITEM_GRIND && !goal.isComplete())
		{
			JMenuItem editQty = new JMenuItem("Change Target");
			editQty.addActionListener(e -> {
				String input = JOptionPane.showInputDialog(
					this,
					"New target quantity for " + goal.getName() + ":",
					String.valueOf(goal.getTargetValue())
				);
				if (input != null)
				{
					try
					{
						int newQty = Integer.parseInt(input.trim().replace(",", ""));
						if (newQty > 0)
						{
							api.changeTarget(goal.getId(), newQty);
							// Description update is a display concern
							goal.setDescription(FormatUtil.formatNumber(newQty) + " total");
							goalStore.updateGoal(goal);
							rebuild();
						}
					}
					catch (NumberFormatException ignored) {}
				}
			});
			menu.add(editQty);
		}

		// Tag management
		JMenuItem addTag = new JMenuItem("Add Tag");
		addTag.addActionListener(e -> {
			JPanel tagPanel = new JPanel(new java.awt.GridBagLayout());
			java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
			gbc.insets = new Insets(4, 4, 4, 4);
			gbc.anchor = java.awt.GridBagConstraints.WEST;

			// Category selector
			JLabel catLabel = new JLabel("Category:");
			catLabel.setPreferredSize(new Dimension(80, 24));
			gbc.gridx = 0; gbc.gridy = 0; gbc.fill = java.awt.GridBagConstraints.NONE;
			tagPanel.add(catLabel, gbc);

			// Tag categories: full list for CUSTOM goals; restricted to OTHER ("custom"
			// catch-all) for everything else, since auto-generated goal types should not
			// have user-added boss/raid/skilling/etc. tags layered on.
			com.goaltracker.model.TagCategory[] categories;
			if (goal.getType() == GoalType.CUSTOM)
			{
				categories = java.util.Arrays.stream(com.goaltracker.model.TagCategory.values())
					.filter(c -> c != com.goaltracker.model.TagCategory.SPECIAL)
					.toArray(com.goaltracker.model.TagCategory[]::new);
			}
			else
			{
				categories = new com.goaltracker.model.TagCategory[]{
					com.goaltracker.model.TagCategory.OTHER
				};
			}
			JComboBox<com.goaltracker.model.TagCategory> catCombo = new JComboBox<>(categories);
			catCombo.setRenderer(new DefaultListCellRenderer()
			{
				@Override
				public Component getListCellRendererComponent(JList<?> list, Object value, int index,
					boolean isSelected, boolean cellHasFocus)
				{
					super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					if (value instanceof com.goaltracker.model.TagCategory)
					{
						setText(((com.goaltracker.model.TagCategory) value).getDisplayName());
					}
					return this;
				}
			});
			gbc.gridx = 1; gbc.fill = java.awt.GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
			tagPanel.add(catCombo, gbc);

			// Tag label — dropdown or freeform based on category
			JLabel nameLabel = new JLabel("Tag:");
			nameLabel.setPreferredSize(new Dimension(80, 24));
			gbc.gridx = 0; gbc.gridy = 1; gbc.fill = java.awt.GridBagConstraints.NONE; gbc.weightx = 0;
			tagPanel.add(nameLabel, gbc);

			JComboBox<String> dropdownField = new JComboBox<>();
			JTextField freeField = new JTextField(15);
			JPanel fieldSwap = new JPanel(new java.awt.CardLayout());
			fieldSwap.add(dropdownField, "DROPDOWN");
			fieldSwap.add(freeField, "FREEFORM");
			gbc.gridx = 1; gbc.fill = java.awt.GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
			tagPanel.add(fieldSwap, gbc);

			// Update field when category changes. For non-custom goals the category
			// dropdown is locked to OTHER and the value should be freeform text — there
			// are no curated TagOptions to pick from at that point.
			final boolean forceFreeform = goal.getType() != GoalType.CUSTOM;
			Runnable updateField = () -> {
				com.goaltracker.model.TagCategory cat =
					(com.goaltracker.model.TagCategory) catCombo.getSelectedItem();
				String[] opts = com.goaltracker.data.TagOptions.getOptions(cat);
				if (!forceFreeform && opts.length > 0)
				{
					dropdownField.removeAllItems();
					for (String opt : opts) dropdownField.addItem(opt);
					((java.awt.CardLayout) fieldSwap.getLayout()).show(fieldSwap, "DROPDOWN");
				}
				else
				{
					((java.awt.CardLayout) fieldSwap.getLayout()).show(fieldSwap, "FREEFORM");
				}
			};
			catCombo.addActionListener(ev -> updateField.run());
			updateField.run();

			tagPanel.setPreferredSize(new Dimension(300, tagPanel.getPreferredSize().height));

			int result = JOptionPane.showConfirmDialog(
				this, tagPanel, "Add Tag", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
			);
			if (result == JOptionPane.OK_OPTION)
			{
				com.goaltracker.model.TagCategory selectedCat =
					(com.goaltracker.model.TagCategory) catCombo.getSelectedItem();
				String[] opts = com.goaltracker.data.TagOptions.getOptions(selectedCat);
				String tagText = (!forceFreeform && opts.length > 0)
					? (String) dropdownField.getSelectedItem()
					: freeField.getText().trim();

				if (tagText != null && !tagText.isEmpty())
				{
					if (goal.getTags() == null)
					{
						goal.setTags(new java.util.ArrayList<>());
					}
					goal.getTags().add(new com.goaltracker.model.ItemTag(tagText, selectedCat));
					goalStore.updateGoal(goal);
					rebuild();
				}
			}
		});
		// Completed goals are tag-frozen.
		if (!goal.isComplete())
		{
			menu.add(addTag);
		}

		// Removable tags: for CUSTOM goals, anything. For everything else, only
		// user-added tags (not in defaultTags). This prevents users from accidentally
		// stripping the auto-generated boss/raid/tier tags off a quest/diary/CA goal.
		java.util.List<com.goaltracker.model.ItemTag> removableTags;
		if (goal.getTags() != null && !goal.getTags().isEmpty())
		{
			if (goal.getType() == GoalType.CUSTOM)
			{
				removableTags = new java.util.ArrayList<>(goal.getTags());
			}
			else
			{
				java.util.List<com.goaltracker.model.ItemTag> defaults = goal.getDefaultTags() != null
					? goal.getDefaultTags()
					: java.util.Collections.emptyList();
				removableTags = goal.getTags().stream()
					.filter(t -> !defaults.contains(t))
					.collect(java.util.stream.Collectors.toList());
			}
		}
		else
		{
			removableTags = java.util.Collections.emptyList();
		}

		if (!removableTags.isEmpty())
		{
			JMenuItem removeTag = new JMenuItem("Remove Tag");
			removeTag.addActionListener(e -> {
				String[] tagNames = removableTags.stream()
					.map(t -> t.getLabel() + " (" + t.getCategory().getDisplayName() + ")")
					.toArray(String[]::new);

				String selected = (String) JOptionPane.showInputDialog(
					this, "Select tag to remove:", "Remove Tag",
					JOptionPane.PLAIN_MESSAGE, null, tagNames, tagNames[0]
				);
				if (selected != null)
				{
					int idx = java.util.Arrays.asList(tagNames).indexOf(selected);
					if (idx >= 0)
					{
						api.removeTag(goal.getId(), removableTags.get(idx).getLabel());
					}
				}
			});
			menu.add(removeTag);
		}

		// Restore Defaults (only if tags have been customized)
		if (goal.getDefaultTags() != null && !goal.getDefaultTags().isEmpty()
			&& goal.getTags() != null && !goal.getTags().equals(goal.getDefaultTags()))
		{
			JMenuItem restore = new JMenuItem("Restore Defaults");
			restore.addActionListener(e -> api.restoreDefaultTags(goal.getId()));
			menu.add(restore);
		}

		// "Move to section →" submenu — only for non-completed goals, only if there
		// is at least one valid destination section (Incomplete + user sections,
		// excluding the goal's current section, excluding Completed).
		if (!goal.isComplete())
		{
			java.util.List<com.goaltracker.api.SectionView> allSections = api.queryAllSections();
			java.util.List<com.goaltracker.api.SectionView> destinations = new java.util.ArrayList<>();
			for (com.goaltracker.api.SectionView sv : allSections)
			{
				if ("COMPLETED".equals(sv.kind)) continue;
				if (sv.id.equals(goal.getSectionId())) continue;
				destinations.add(sv);
			}
			if (!destinations.isEmpty())
			{
				JMenu moveToSection = new JMenu("Move to Section");
				for (com.goaltracker.api.SectionView dest : destinations)
				{
					JMenuItem item = new JMenuItem(dest.name);
					item.addActionListener(e -> api.moveGoalToSection(goal.getId(), dest.id));
					moveToSection.add(item);
				}
				menu.add(moveToSection);
			}
		}

		JMenuItem remove = new JMenuItem("Remove");
		remove.addActionListener(e -> api.removeGoal(goal.getId()));
		menu.add(remove);

		card.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					menu.show(card, e.getX(), e.getY());
				}
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					menu.show(card, e.getX(), e.getY());
				}
			}
		});
	}

	/**
	 * Attach a right-click context menu to a user-defined section header
	 * (rename, delete, move up, move down). Built-in sections never call this.
	 */
	private void attachSectionContextMenu(SectionHeaderRow row,
		com.goaltracker.api.SectionView section,
		java.util.List<com.goaltracker.api.SectionView> allSections)
	{
		// Compute this section's index within the user-section band so we can
		// gate the move-up / move-down items correctly.
		java.util.List<com.goaltracker.api.SectionView> userSections = new java.util.ArrayList<>();
		for (com.goaltracker.api.SectionView sv : allSections)
		{
			if (!sv.builtIn) userSections.add(sv);
		}
		int userIndex = -1;
		for (int i = 0; i < userSections.size(); i++)
		{
			if (userSections.get(i).id.equals(section.id)) { userIndex = i; break; }
		}
		final int currentUserIndex = userIndex;

		JPopupMenu menu = new JPopupMenu();

		JMenuItem rename = new JMenuItem("Rename");
		rename.addActionListener(e -> showRenameSectionDialog(section));
		menu.add(rename);

		if (currentUserIndex > 0)
		{
			JMenuItem moveUp = new JMenuItem("Move Up");
			moveUp.addActionListener(e -> api.reorderSection(section.id, currentUserIndex - 1));
			menu.add(moveUp);
		}
		if (currentUserIndex >= 0 && currentUserIndex < userSections.size() - 1)
		{
			JMenuItem moveDown = new JMenuItem("Move Down");
			moveDown.addActionListener(e -> api.reorderSection(section.id, currentUserIndex + 1));
			menu.add(moveDown);
		}

		menu.addSeparator();

		JMenuItem delete = new JMenuItem("Delete Section");
		delete.addActionListener(e -> {
			int confirm = JOptionPane.showConfirmDialog(
				this,
				"Delete section \"" + section.name + "\"?\nGoals in it will be moved to Incomplete.",
				"Delete Section",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE
			);
			if (confirm == JOptionPane.YES_OPTION)
			{
				api.deleteSection(section.id);
			}
		});
		menu.add(delete);

		row.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				if (e.isPopupTrigger()) menu.show(row, e.getX(), e.getY());
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (e.isPopupTrigger()) menu.show(row, e.getX(), e.getY());
			}
		});
	}

	private void showCreateSectionDialog()
	{
		String input = JOptionPane.showInputDialog(this, "Section name:", "New Section",
			JOptionPane.PLAIN_MESSAGE);
		if (input == null) return;
		try
		{
			api.createSection(input);
		}
		catch (IllegalArgumentException ex)
		{
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid name",
				JOptionPane.WARNING_MESSAGE);
		}
	}

	private void showRenameSectionDialog(com.goaltracker.api.SectionView section)
	{
		String input = (String) JOptionPane.showInputDialog(this, "New name:", "Rename Section",
			JOptionPane.PLAIN_MESSAGE, null, null, section.name);
		if (input == null) return;
		boolean ok = api.renameSection(section.id, input);
		if (!ok)
		{
			JOptionPane.showMessageDialog(this,
				"Could not rename section. Name may be invalid, duplicate, or unchanged.",
				"Rename failed", JOptionPane.WARNING_MESSAGE);
		}
	}

	private void showAddGoalDialog()
	{
		// Use GridBagLayout for reliable sizing in JOptionPane
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.WEST;

		// Labels column
		int labelWidth = 100;

		// Row 0: Type
		gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		JLabel typeLabel = new JLabel("Type:");
		typeLabel.setPreferredSize(new Dimension(labelWidth, 24));
		panel.add(typeLabel, gbc);

		gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
		JComboBox<GoalType> typeCombo = new JComboBox<>(new GoalType[]{GoalType.SKILL, GoalType.ITEM_GRIND, GoalType.CUSTOM});
		typeCombo.setRenderer(new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof GoalType)
				{
					setText(((GoalType) value).getDisplayName());
				}
				return this;
			}
		});
		panel.add(typeCombo, gbc);

		// Row 1: Field 1 label + input
		JLabel label1 = new JLabel("Skill:");
		label1.setPreferredSize(new Dimension(labelWidth, 24));
		gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
		panel.add(label1, gbc);

		// Filter out skills already at 99
		Skill[] availableSkills = java.util.Arrays.stream(Skill.values())
			.filter(s -> {
				if (client == null) return true;
				try { return client.getRealSkillLevel(s) < 99; }
				catch (Exception e) { return true; }
			})
			.toArray(Skill[]::new);
		JComboBox<Skill> skillCombo = new JComboBox<>(availableSkills);
		skillCombo.setRenderer(new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof Skill)
				{
					setText(((Skill) value).getName());
				}
				return this;
			}
		});
		JTextField nameField = new JTextField(15);
		JTextField itemQtyField = new JTextField("1", 15);

		// CardLayout to swap between types
		JPanel field1Panel = new JPanel(new CardLayout());
		field1Panel.add(skillCombo, "SKILL");
		field1Panel.add(itemQtyField, "ITEM_GRIND");
		field1Panel.add(nameField, "CUSTOM");
		gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
		panel.add(field1Panel, gbc);

		// Row 2: Field 2
		JLabel label2 = new JLabel("Target Level/XP:");
		label2.setPreferredSize(new Dimension(labelWidth, 24));
		gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
		panel.add(label2, gbc);

		JTextField targetField = new JTextField("99", 15);
		JTextField descField = new JTextField(15);
		JLabel itemHint = new JLabel("<html><i>Item search opens in-game</i></html>");
		itemHint.setForeground(new Color(140, 140, 140));

		JPanel field2Panel = new JPanel(new CardLayout());
		field2Panel.add(targetField, "SKILL");
		field2Panel.add(itemHint, "ITEM_GRIND");
		field2Panel.add(descField, "CUSTOM");
		gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
		panel.add(field2Panel, gbc);

		// Swap fields when type changes
		typeCombo.addActionListener(e ->
		{
			GoalType selected = (GoalType) typeCombo.getSelectedItem();

			((CardLayout) field1Panel.getLayout()).show(field1Panel, selected.name());
			((CardLayout) field2Panel.getLayout()).show(field2Panel, selected.name());

			switch (selected)
			{
				case SKILL:
					label1.setText("Skill:");
					label2.setText("Target Level:");
					break;
				case ITEM_GRIND:
					label1.setText("Quantity:");
					label2.setText("");
					break;
				default:
					label1.setText("Goal Name:");
					label2.setText("Description:");
					break;
			}

			Window w = SwingUtilities.getWindowAncestor(panel);
			if (w != null) w.pack();
		});

		panel.setPreferredSize(new Dimension(320, panel.getPreferredSize().height));

		int result = JOptionPane.showConfirmDialog(
			this, panel, "Add Goal", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
		);

		if (result == JOptionPane.OK_OPTION)
		{
			GoalType selectedType = (GoalType) typeCombo.getSelectedItem();

			if (selectedType == GoalType.SKILL)
			{
				addSkillGoal(skillCombo, targetField);
			}
			else if (selectedType == GoalType.ITEM_GRIND)
			{
				try
				{
					int qty = Integer.parseInt(itemQtyField.getText().trim().replace(",", ""));
					if (qty <= 0)
					{
						JOptionPane.showMessageDialog(this, "Quantity must be greater than 0.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					// Open the in-game chatbox item search
					itemSearchCallback.accept(qty);
				}
				catch (NumberFormatException e)
				{
					JOptionPane.showMessageDialog(this, "Invalid quantity.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			else if (selectedType == GoalType.CUSTOM)
			{
				addCustomGoal(nameField, descField);
			}
		}
	}

	private void addSkillGoal(JComboBox<Skill> skillCombo, JTextField targetField)
	{
		try
		{
			Skill skill = (Skill) skillCombo.getSelectedItem();
			int targetLevel = Integer.parseInt(targetField.getText().trim().replace(",", ""));

			if (targetLevel < 1 || targetLevel > 99)
			{
				JOptionPane.showMessageDialog(this, "Target level must be between 1 and 99.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			int targetXp = net.runelite.api.Experience.getXpForLevel(targetLevel);

			String conflict = checkSkillConflict(skill, targetXp);
			if (conflict != null)
			{
				JOptionPane.showMessageDialog(this, conflict, "Conflict", JOptionPane.WARNING_MESSAGE);
				return;
			}

			// Route through the public API. Returns the goal id (or existing id on dup).
			api.addSkillGoalForLevel(skill, targetLevel);
			// API callback rebuilds the panel.
		}
		catch (NumberFormatException e)
		{
			JOptionPane.showMessageDialog(this, "Invalid target value.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void addCustomGoal(JTextField nameField, JTextField descField)
	{
		String name = nameField.getText().trim();
		if (name.isEmpty())
		{
			JOptionPane.showMessageDialog(this, "Goal name is required.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		api.addCustomGoal(name, descField.getText().trim());
		// API callback rebuilds the panel.
	}

	/**
	 * Check if a new skill goal conflicts with existing goals.
	 * Blocks exact duplicates only. Multiple levels for the same skill are fine.
	 * Returns an error message if conflicting, null if OK.
	 */
	private String checkSkillConflict(Skill skill, int target)
	{
		for (Goal existing : goalStore.getGoals())
		{
			if (existing.getType() != GoalType.SKILL || existing.getSkillName() == null)
			{
				continue;
			}
			if (!existing.getSkillName().equals(skill.name()))
			{
				continue;
			}
			if (existing.isComplete())
			{
				continue;
			}

			if (existing.getTargetValue() == target)
			{
				return String.format("You already have a %s goal for %s.",
					skill.getName(), target > 99 ? FormatUtil.formatNumber(target) + " XP" : "Level " + target);
			}
		}
		return null;
	}



}
