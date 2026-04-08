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
	/** Free-text filter applied to the goal list. Empty = show all. Mission 22. */
	private String searchFilter = "";
	/** Most recent simple-click goal id, used as the anchor for shift-click range
	 *  selection. Cleared on rebuilds when the goal no longer exists. Mission 24. */
	private String selectionAnchorId = null;

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

		JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
		headerButtons.setOpaque(false);

		// Visual separator between the section-button pair (blue) and the
		// goal-button pair (gray). 8px strut renders as a thin gap.
		java.awt.Component buttonGroupSeparator = Box.createHorizontalStrut(6);

		JButton clearButton = new JButton(ShapeIcons.minus(10, new Color(200, 200, 200)));
		clearButton.setToolTipText("Clear all goals");
		clearButton.setMargin(new Insets(3, 6, 3, 6));
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
		clearSectionsButton.setMargin(new Insets(3, 6, 3, 6));
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
		addSectionButton.setMargin(new Insets(3, 6, 3, 6));
		addSectionButton.addActionListener(e -> showCreateSectionDialog());

		JButton manageTagsButton = new JButton(ShapeIcons.plus(10, new Color(220, 180, 140)));
		manageTagsButton.setToolTipText("Manage tags");
		manageTagsButton.setMargin(new Insets(3, 6, 3, 6));
		manageTagsButton.addActionListener(e -> {
			java.awt.Window window = SwingUtilities.getWindowAncestor(GoalPanel.this);
			java.awt.Frame owner = window instanceof java.awt.Frame ? (java.awt.Frame) window : null;
			TagManagementDialog dialog = new TagManagementDialog(owner, api, skillIconManager, itemManager);
			dialog.setVisible(true);
		});

		JButton addButton = new JButton(ShapeIcons.plus(10, new Color(200, 200, 200)));
		addButton.setToolTipText("Add a new goal");
		addButton.setMargin(new Insets(3, 6, 3, 6));
		addButton.addActionListener(e -> showAddGoalDialog(null));

		// Order: gray pair (clear/add goal) | separator | blue pair (clear/add section)
		headerButtons.add(clearButton);
		headerButtons.add(addButton);
		headerButtons.add(buttonGroupSeparator);
		headerButtons.add(clearSectionsButton);
		headerButtons.add(addSectionButton);
		headerButtons.add(Box.createHorizontalStrut(6));
		headerButtons.add(manageTagsButton);

		header.add(title, BorderLayout.WEST);
		header.add(headerButtons, BorderLayout.EAST);

		// Mission 22: free-text search row beneath the toolbar. Filters
		// goals by name/description/tags/category/type/section title.
		JPanel searchRow = new JPanel(new BorderLayout(4, 0));
		searchRow.setBackground(ColorScheme.DARK_GRAY_COLOR);
		searchRow.setBorder(new EmptyBorder(0, 8, 8, 8));
		final javax.swing.JTextField searchField = new javax.swing.JTextField();
		searchField.setToolTipText("Search goals by name, description, tag, category, type, or section");
		searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener()
		{
			private void update()
			{
				searchFilter = searchField.getText();
				rebuild();
			}
			@Override public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
			@Override public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
			@Override public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
		});
		JButton clearSearchBtn = new JButton(ShapeIcons.closeX(10, new Color(200, 200, 200)));
		clearSearchBtn.setToolTipText("Clear search");
		clearSearchBtn.setMargin(new Insets(3, 6, 3, 6));
		clearSearchBtn.addActionListener(e -> searchField.setText(""));
		searchRow.add(searchField, BorderLayout.CENTER);
		searchRow.add(clearSearchBtn, BorderLayout.EAST);

		JPanel headerStack = new JPanel(new BorderLayout());
		headerStack.setBackground(ColorScheme.DARK_GRAY_COLOR);
		headerStack.add(header, BorderLayout.NORTH);
		headerStack.add(searchRow, BorderLayout.CENTER);

		// Scrollable goal list
		goalListPanel = new JPanel();
		goalListPanel.setLayout(new BoxLayout(goalListPanel, BoxLayout.Y_AXIS));
		goalListPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		goalListPanel.setBorder(new EmptyBorder(4, 8, 8, 8));

		JScrollPane scrollPane = new JScrollPane(goalListPanel);
		scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
		scrollPane.setBorder(null);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);

		add(headerStack, BorderLayout.NORTH);
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
		// Mission 22: when a search filter is active, route reads through
		// searchGoals so the visible list is always filter(text). Empty
		// query → all goals (matches queryAllGoals).
		boolean filterActive = searchFilter != null && !searchFilter.trim().isEmpty();
		java.util.List<com.goaltracker.api.GoalView> goalViews = filterActive
			? api.searchGoals(searchFilter) : api.queryAllGoals();
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
			// Mission 22: when filtering, hide ALL empty sections (user + built-in).
			if (sectionCount == 0 && (section.builtIn || filterActive)) continue;
			final String sectionIdRef = section.id;
			SectionHeaderRow headerRow = new SectionHeaderRow(section, sectionCount, () -> {
				api.toggleSectionCollapsed(sectionIdRef);
				// API callback rebuilds the panel.
			});
			// All sections get a right-click menu. User sections get the full
			// rename/move/delete/color menu; built-ins get only Change Color.
			attachSectionContextMenu(headerRow, section, sectionViews);
			goalListPanel.add(headerRow);
			goalListPanel.add(Box.createVerticalStrut(2));

			// Empty user-section placeholder: a single italic hint row directly under
			// the header, so a freshly created section doesn't look broken.
			if (sectionCount == 0 && !section.builtIn && !section.collapsed)
			{
				JLabel placeholder = new JLabel("Empty — right-click goals to move them here");
				placeholder.setForeground(new Color(120, 120, 120));
				placeholder.setFont(placeholder.getFont().deriveFont(Font.ITALIC, 10f));
				placeholder.setAlignmentX(Component.CENTER_ALIGNMENT);
				placeholder.setBorder(new EmptyBorder(2, 4, 6, 4));
				goalListPanel.add(placeholder);
				continue;
			}

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
				attachSelectionClick(card, view);
				cardMap.put(goal.getId(), card);

				goalListPanel.add(card);
				goalListPanel.add(Box.createVerticalStrut(4));
			}
		}

		if (goalViews.isEmpty())
		{
			JPanel emptyPanel = new JPanel();
			emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
			emptyPanel.setOpaque(false);
			emptyPanel.setBorder(new EmptyBorder(32, 8, 8, 8));
			emptyPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

			JLabel headline = new JLabel("No goals yet");
			headline.setForeground(new Color(180, 180, 180));
			headline.setFont(headline.getFont().deriveFont(Font.BOLD, 13f));
			headline.setAlignmentX(Component.CENTER_ALIGNMENT);

			JLabel hintGoal = new JLabel("Click + to add a goal");
			hintGoal.setForeground(new Color(130, 130, 130));
			hintGoal.setFont(hintGoal.getFont().deriveFont(11f));
			hintGoal.setAlignmentX(Component.CENTER_ALIGNMENT);
			hintGoal.setBorder(new EmptyBorder(8, 0, 0, 0));

			JLabel hintSection = new JLabel("Or + to add a custom section");
			hintSection.setForeground(new Color(130, 130, 130));
			hintSection.setFont(hintSection.getFont().deriveFont(11f));
			hintSection.setAlignmentX(Component.CENTER_ALIGNMENT);
			hintSection.setBorder(new EmptyBorder(2, 0, 0, 0));

			emptyPanel.add(headline);
			emptyPanel.add(hintGoal);
			emptyPanel.add(hintSection);
			goalListPanel.add(emptyPanel);
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
		clearSelectionIfNotMember(goalId);
		api.moveGoal(goalId, toIndex);
		// API callback rebuilds the panel.
	}

	/** Move a goal directly to a target index within its section. */
	private void moveGoalTo(String goalId, int toIndex)
	{
		clearSelectionIfNotMember(goalId);
		api.moveGoal(goalId, toIndex);
		// API callback rebuilds the panel.
	}

	/**
	 * Rule 1 enforcement: any action on an unselected card auto-deselects the
	 * existing multi-selection first. Used by arrow-button moves; the right-
	 * click menu show path applies the same rule inline.
	 */
	private void clearSelectionIfNotMember(String goalId)
	{
		java.util.Set<String> sel = api.getSelectedGoalIds();
		if (!sel.isEmpty() && !sel.contains(goalId))
		{
			api.clearGoalSelection();
		}
	}


	/**
	 * Attach a left-click MouseListener that routes selection clicks through
	 * the API. Coexists with the existing right-click context menu — only
	 * BUTTON1 events are handled here, BUTTON3 falls through to the popup.
	 *
	 * <p>Click semantics:
	 * <ul>
	 *   <li>Plain click on UNSELECTED → replace selection with just this card</li>
	 *   <li>Plain click on SELECTED → clear selection entirely</li>
	 *   <li>Cmd/Ctrl+click on UNSELECTED → add this card to selection</li>
	 *   <li>Cmd/Ctrl+click on SELECTED → remove this card from selection</li>
	 * </ul>
	 */
	private void attachSelectionClick(GoalCard card, com.goaltracker.api.GoalView view)
	{
		final String goalId = view.id;
		final boolean wasSelected = view.selected;
		card.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getButton() != MouseEvent.BUTTON1) return;
				boolean cmdCtrl = e.isMetaDown() || e.isControlDown();
				boolean shift = e.isShiftDown();
				// Mission 24: Excel-style shift-click extends selection from
				// the anchor to the clicked goal in linear panel order.
				if (shift && selectionAnchorId != null && !cmdCtrl)
				{
					java.util.Set<String> range = computeRangeSelection(selectionAnchorId, goalId);
					if (!range.isEmpty()) api.replaceGoalSelection(range);
					// Mission 24: anchor follows the last click, so the next
					// shift-click extends from where you just landed.
					selectionAnchorId = goalId;
					return;
				}
				if (cmdCtrl)
				{
					if (wasSelected) api.removeFromGoalSelection(goalId);
					else api.addToGoalSelection(goalId);
					selectionAnchorId = goalId;
				}
				else
				{
					if (wasSelected) api.clearGoalSelection();
					else api.replaceGoalSelection(java.util.Collections.singleton(goalId));
					selectionAnchorId = goalId;
				}
			}
		});
	}

	/**
	 * Walk the canonical goal order from the API and return the slice of ids
	 * between (and including) anchorId and clickedId. The order is the same
	 * one used to render the panel — sections in section.order, goals within
	 * each section in priority order. Returns an empty set if either id is
	 * missing from the canonical list (e.g. just deleted). Mission 24.
	 */
	private java.util.Set<String> computeRangeSelection(String anchorId, String clickedId)
	{
		java.util.List<com.goaltracker.api.GoalView> all = api.queryAllGoals();
		int aIdx = -1, bIdx = -1;
		for (int i = 0; i < all.size(); i++)
		{
			String id = all.get(i).id;
			if (id.equals(anchorId)) aIdx = i;
			if (id.equals(clickedId)) bIdx = i;
		}
		if (aIdx < 0 || bIdx < 0) return java.util.Collections.emptySet();
		int lo = Math.min(aIdx, bIdx), hi = Math.max(aIdx, bIdx);
		java.util.LinkedHashSet<String> out = new java.util.LinkedHashSet<>();
		for (int i = lo; i <= hi; i++) out.add(all.get(i).id);
		return out;
	}

	private void addContextMenu(GoalCard card, Goal goal, int index, int sectionStart, int sectionEnd)
	{
		card.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e) { maybeShowPopup(e); }

			@Override
			public void mouseReleased(MouseEvent e) { maybeShowPopup(e); }

			private void maybeShowPopup(MouseEvent e)
			{
				if (!e.isPopupTrigger()) return;
				// Rule 1: action on an unselected card auto-deselects all first.
				// Right-clicking is "the action" here for the menu-driven flow.
				java.util.Set<String> sel = api.getSelectedGoalIds();
				if (!sel.contains(goal.getId()) && !sel.isEmpty())
				{
					api.clearGoalSelection();
					sel = api.getSelectedGoalIds();
				}
				JPopupMenu popup;
				if (sel.contains(goal.getId()) && sel.size() >= 2)
				{
					popup = buildBulkMenu(goal.getId());
				}
				else
				{
					popup = buildSingleItemMenu(goal, index, sectionStart, sectionEnd);
				}
				popup.show(card, e.getX(), e.getY());
			}
		});
	}

	/**
	 * Builds the normal per-card right-click menu. Called lazily on each
	 * popup show so the contents reflect current selection / completion / tag
	 * state without needing to be rebuilt at panel.rebuild() time.
	 */
	private JPopupMenu buildSingleItemMenu(Goal goal, int index, int sectionStart, int sectionEnd)
	{
		JPopupMenu menu = new JPopupMenu();

		// Selection toggle — first item so it's predictable. Label flips based on
		// the goal's current selection state. Routes through the same internal API
		// the click handler uses, so multi-select state stays consistent.
		java.util.Set<String> selectedIds = api.getSelectedGoalIds();
		boolean currentlySelected = selectedIds.contains(goal.getId());
		JMenuItem selectToggle = new JMenuItem(currentlySelected ? "Deselect" : "Select");
		selectToggle.addActionListener(e -> {
			if (currentlySelected) api.removeFromGoalSelection(goal.getId());
			else api.addToGoalSelection(goal.getId());
		});
		menu.add(selectToggle);

		// "Deselect All" appears on every card whenever ANY card is selected,
		// so the user has a quick escape from a multi-selection.
		if (!selectedIds.isEmpty())
		{
			JMenuItem deselectAll = new JMenuItem("Deselect All");
			deselectAll.addActionListener(e -> api.clearGoalSelection());
			menu.add(deselectAll);
		}

		menu.addSeparator();

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

		// Manual completion: CUSTOM and ITEM_GRIND. Skill/quest/diary/CA are
		// purely game-driven. ITEM_GRIND is "sticky" — the next tracker pass
		// will revert if inventory+bank don't actually have the target qty.
		boolean manuallyToggleable = goal.getType() == GoalType.CUSTOM
			|| goal.getType() == GoalType.ITEM_GRIND;
		if (manuallyToggleable)
		{
			if (goal.isComplete())
			{
				JMenuItem reopen = new JMenuItem("Mark Incomplete");
				reopen.addActionListener(e -> api.markGoalIncomplete(goal.getId()));
				menu.add(reopen);
			}
			else
			{
				JMenuItem complete = new JMenuItem("Mark Complete");
				complete.addActionListener(e -> api.markGoalComplete(goal.getId()));
				menu.add(complete);
			}
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

		// Change Color is available on ALL goal types — override persists on the
		// goal model so rebuilds don't clobber it.
		{
			JMenuItem changeGoalColor = new JMenuItem("Change Color");
			changeGoalColor.addActionListener(e -> showGoalColorDialog(goal));
			menu.add(changeGoalColor);
		}

		// Skill-specific options
		if (goal.getType() == GoalType.SKILL && !goal.isComplete())
		{
			JMenuItem editLevel = new JMenuItem("Change Amount");
			editLevel.addActionListener(e -> showChangeSkillTargetDialog(goal));
			menu.add(editLevel);
		}

		// Item-specific options
		if (goal.getType() == GoalType.ITEM_GRIND && !goal.isComplete())
		{
			JMenuItem editQty = new JMenuItem("Change Amount");
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
							// changeTarget regenerates the description from the new
							// quantity as of Mission 19; no follow-up mutation needed.
							api.changeTarget(goal.getId(), newQty);
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

			// All categories except SKILLING (skill tags are system-seeded only).
			com.goaltracker.model.TagCategory[] categories =
				java.util.Arrays.stream(com.goaltracker.model.TagCategory.values())
					.filter(c -> c != com.goaltracker.model.TagCategory.SKILLING)
					.toArray(com.goaltracker.model.TagCategory[]::new);
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

			// Mission 21 follow-up: dropdown now shows EXISTING tags in the
			// chosen category so users can reuse the same Tag entity across
			// goals. The combo is editable so users can also type a new label
			// — addTagWithCategory does find-or-create downstream.
			Runnable updateField = () -> {
				com.goaltracker.model.TagCategory cat =
					(com.goaltracker.model.TagCategory) catCombo.getSelectedItem();
				// SKILLING is system-only — pick from existing, no free typing.
				dropdownField.setEditable(cat != com.goaltracker.model.TagCategory.SKILLING);
				dropdownField.removeAllItems();
				java.util.List<com.goaltracker.api.TagView> all = api.queryAllTags();
				for (com.goaltracker.api.TagView t : all)
				{
					if (cat != null && cat.name().equals(t.category))
					{
						dropdownField.addItem(t.label);
					}
				}
				if (dropdownField.isEditable()) dropdownField.setSelectedItem("");
				else if (dropdownField.getItemCount() > 0) dropdownField.setSelectedIndex(0);
				((java.awt.CardLayout) fieldSwap.getLayout()).show(fieldSwap, "DROPDOWN");
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
				// Editable combo: getEditor().getItem() captures typed text.
				// Non-editable (SKILLING): use getSelectedItem() which returns
				// the picked dropdown option.
				Object raw = dropdownField.isEditable()
					? dropdownField.getEditor().getItem() : dropdownField.getSelectedItem();
				String tagText = raw == null ? "" : raw.toString().trim();
				if (!tagText.isEmpty() && selectedCat != null)
				{
					api.addTagWithCategory(goal.getId(), tagText, selectedCat.name());
				}
			}
		});
		// Completed goals are tag-frozen.
		if (!goal.isComplete())
		{
			menu.add(addTag);
		}

		// Removable tags: for CUSTOM goals, anything. For everything else, only
		// user-added tags (not in defaultTagIds). Mission 19: dereference tag ids
		// through the store and operate on Tag entities.
		java.util.List<com.goaltracker.model.Tag> removableTags = new java.util.ArrayList<>();
		java.util.List<com.goaltracker.model.Tag> allGoalTags = new java.util.ArrayList<>();
		if (goal.getTagIds() != null && !goal.getTagIds().isEmpty())
		{
			java.util.List<String> defaults = goal.getDefaultTagIds() != null
				? goal.getDefaultTagIds() : java.util.Collections.emptyList();
			for (String tagId : goal.getTagIds())
			{
				com.goaltracker.model.Tag t = goalStore.findTag(tagId);
				if (t == null) continue;
				allGoalTags.add(t);
				if (goal.getType() == GoalType.CUSTOM || !defaults.contains(tagId))
				{
					removableTags.add(t);
				}
			}
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

		// Mission 24: Restore Defaults — gated on isGoalOverridden (tag drift
		// OR color override). Routes through the bulk API so the single-item
		// path resets BOTH tags and color in one shot.
		if (api.isGoalOverridden(goal.getId()))
		{
			JMenuItem restore = new JMenuItem("Restore Defaults");
			restore.addActionListener(e ->
				api.bulkRestoreDefaults(java.util.Collections.singleton(goal.getId())));
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

		JMenuItem remove = new JMenuItem("Remove Goal");
		remove.addActionListener(e -> {
			int confirm = JOptionPane.showConfirmDialog(
				this,
				"Remove \"" + goal.getName() + "\"?",
				"Remove Goal",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE);
			if (confirm == JOptionPane.YES_OPTION) api.removeGoal(goal.getId());
		});
		menu.add(remove);

		return menu;
	}

	/**
	 * Builds the streamlined bulk-action menu shown when the right-clicked
	 * card is part of a multi-selection (size >= 2). Five items only:
	 * Move to Section, Add Tag, Change Color, Remove, Mark as Complete.
	 */
	private JPopupMenu buildBulkMenu(String rightClickedGoalId)
	{
		JPopupMenu menu = new JPopupMenu();
		java.util.Set<String> selectedIds = api.getSelectedGoalIds();
		int selectionSize = selectedIds.size();

		// Snapshot the selected Goal objects up front so all handlers operate
		// on a consistent set even if the underlying selection mutates between
		// menu open and item pick.
		java.util.List<Goal> selectedGoals = new java.util.ArrayList<>();
		for (Goal g : goalStore.getGoals())
		{
			if (selectedIds.contains(g.getId())) selectedGoals.add(g);
		}

		// Header label so the user knows the menu applies to N cards
		JMenuItem header = new JMenuItem(selectionSize + " selected");
		header.setEnabled(false);
		menu.add(header);

		// Mission 24: selection toggle + deselect all on the bulk menu so the
		// user can drop one card or escape the whole multi-selection without
		// having to find a single-card popup.
		menu.addSeparator();
		JMenuItem deselectThis = new JMenuItem("Deselect this");
		deselectThis.addActionListener(e -> api.removeFromGoalSelection(rightClickedGoalId));
		menu.add(deselectThis);
		JMenuItem deselectAll = new JMenuItem("Deselect All");
		deselectAll.addActionListener(e -> api.clearGoalSelection());
		menu.add(deselectAll);
		menu.addSeparator();

		// 1. Move to Section — only if at least one selected goal is non-complete.
		// Completed goals are pinned to Completed and the API rejects the move,
		// so showing the option for an all-completed selection would be a no-op.
		boolean anyMovable = false;
		for (Goal g : selectedGoals)
		{
			if (!g.isComplete()) { anyMovable = true; break; }
		}
		java.util.List<com.goaltracker.api.SectionView> allSections = api.queryAllSections();
		java.util.List<com.goaltracker.api.SectionView> destinations = new java.util.ArrayList<>();
		for (com.goaltracker.api.SectionView sv : allSections)
		{
			// Completed is auto-managed; bulk-move can't target it.
			if ("COMPLETED".equals(sv.kind)) continue;
			destinations.add(sv);
		}
		if (anyMovable && !destinations.isEmpty())
		{
			JMenu moveToSection = new JMenu("Move to Section");
			for (com.goaltracker.api.SectionView dest : destinations)
			{
				JMenuItem item = new JMenuItem(dest.name);
				item.addActionListener(e -> {
					for (Goal g : selectedGoals)
					{
						api.moveGoalToSection(g.getId(), dest.id);
					}
				});
				moveToSection.add(item);
			}
			menu.add(moveToSection);
		}

		// 2. Add Tag
		JMenuItem addTag = new JMenuItem("Add Tag");
		addTag.addActionListener(e -> showBulkAddTagDialog(selectedGoals));
		menu.add(addTag);

		// 3. Change Color
		JMenuItem changeColor = new JMenuItem("Change Color");
		changeColor.addActionListener(e -> showBulkChangeColorDialog(selectedGoals));
		menu.add(changeColor);

		// Mission 24: bulk Remove Tag — show only if at least one selected
		// goal has a removable tag.
		java.util.List<com.goaltracker.api.GoalTrackerInternalApi.TagRemovalOption> removableOpts =
			api.getRemovableTagsForSelection(selectedIds);
		if (!removableOpts.isEmpty())
		{
			JMenuItem bulkRemoveTag = new JMenuItem("Remove Tag");
			bulkRemoveTag.addActionListener(e -> showBulkRemoveTagDialog(selectedIds, removableOpts));
			menu.add(bulkRemoveTag);
		}

		// Mission 24: bulk Restore Defaults — show only if at least one
		// selected goal is overridden (tag drift OR color override).
		boolean anyOverridden = false;
		for (String id : selectedIds)
		{
			if (api.isGoalOverridden(id)) { anyOverridden = true; break; }
		}
		if (anyOverridden)
		{
			JMenuItem restoreDefaults = new JMenuItem("Restore Defaults");
			restoreDefaults.addActionListener(e -> {
				int changed = api.bulkRestoreDefaults(selectedIds);
				log.debug("bulkRestoreDefaults changed {} of {} selected goals",
					changed, selectionSize);
			});
			menu.add(restoreDefaults);
		}

		// 4. Mark as Complete — only when ALL selected are CUSTOM (per locked design)
		boolean allCustom = !selectedGoals.isEmpty();
		for (Goal g : selectedGoals)
		{
			if (g.getType() != GoalType.CUSTOM) { allCustom = false; break; }
		}
		if (allCustom)
		{
			JMenuItem markComplete = new JMenuItem("Mark as Complete");
			markComplete.addActionListener(e -> {
				for (Goal g : selectedGoals)
				{
					api.markGoalComplete(g.getId());
				}
			});
			menu.add(markComplete);
		}

		menu.addSeparator();

		// 5. Remove
		JMenuItem remove = new JMenuItem("Remove Goals");
		remove.addActionListener(e -> {
			int confirm = JOptionPane.showConfirmDialog(
				this,
				"Remove " + selectionSize + " goals?",
				"Remove Goals",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE
			);
			if (confirm != JOptionPane.YES_OPTION) return;
			for (Goal g : selectedGoals)
			{
				api.removeGoal(g.getId());
			}
		});
		menu.add(remove);

		return menu;
	}

	/**
	 * Bulk Add Tag dialog. Reuses the single-item Add Tag dialog's structure
	 * but applies the result to every selected goal. Category dropdown is
	 * locked to OTHER unless ALL selected are CUSTOM (mirrors single-item rule).
	 */
	/**
	 * Change Amount dialog for SKILL goals. Mirrors the Add Goal dialog —
	 * SkillTargetForm with synced Level/XP fields plus a Mode toggle so the
	 * user can target an absolute level/XP OR a delta gain. Mission 24.
	 */
	private void showChangeSkillTargetDialog(Goal goal)
	{
		net.runelite.api.Skill skill;
		try
		{
			skill = net.runelite.api.Skill.valueOf(goal.getSkillName());
		}
		catch (Exception ex) { return; }

		int currentXp = client != null ? client.getSkillExperience(skill) : 0;
		int currentTargetLevel = goal.getTargetValue() > 0
			? net.runelite.api.Experience.getLevelForXp(goal.getTargetValue()) : 1;

		SkillTargetForm form = new SkillTargetForm(currentTargetLevel);

		javax.swing.JRadioButton modeAbsolute = new javax.swing.JRadioButton("Reach X", true);
		javax.swing.JRadioButton modeRelative = new javax.swing.JRadioButton("Gain X more");
		modeAbsolute.setOpaque(false);
		modeRelative.setOpaque(false);
		javax.swing.ButtonGroup grp = new javax.swing.ButtonGroup();
		grp.add(modeAbsolute); grp.add(modeRelative);
		JPanel modeRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 0));
		modeRow.setOpaque(false);
		modeRow.add(modeAbsolute);
		modeRow.add(modeRelative);
		modeAbsolute.addActionListener(ev -> form.setRelativeBaseline(-1));
		modeRelative.addActionListener(ev -> form.setRelativeBaseline(currentXp));

		JPanel panel = new JPanel(new java.awt.GridBagLayout());
		java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = java.awt.GridBagConstraints.WEST;
		gbc.gridx = 0; gbc.gridy = 0;
		panel.add(new JLabel("Mode:"), gbc);
		gbc.gridx = 1;
		panel.add(modeRow, gbc);
		gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
		panel.add(form, gbc);

		int result = JOptionPane.showConfirmDialog(this, panel,
			"Change " + skill.getName() + " Target",
			JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result != JOptionPane.OK_OPTION) return;

		int formValue = form.getTargetXp();
		if (formValue < 0) return;
		int newXp = modeRelative.isSelected()
			? RelativeTargetResolver.resolveSkillXp(currentXp, formValue)
			: formValue;
		if (newXp < 0) return;
		api.changeTarget(goal.getId(), newXp);
	}

	/**
	 * Mission 24: bulk Remove Tag dialog. Shows the merged set of removable
	 * tags across the selection with a count badge ("Slayer (3)") so the user
	 * knows how many of their selection have it. Picking a tag fires a single
	 * bulk API call.
	 */
	private void showBulkRemoveTagDialog(java.util.Set<String> selectedIds,
		java.util.List<com.goaltracker.api.GoalTrackerInternalApi.TagRemovalOption> opts)
	{
		String[] labels = new String[opts.size()];
		for (int i = 0; i < opts.size(); i++)
		{
			com.goaltracker.api.GoalTrackerInternalApi.TagRemovalOption o = opts.get(i);
			labels[i] = o.label + " (" + o.count + ")";
		}
		String picked = (String) JOptionPane.showInputDialog(
			this, "Remove which tag from the selection?", "Bulk Remove Tag",
			JOptionPane.PLAIN_MESSAGE, null, labels, labels[0]);
		if (picked == null) return;
		int idx = java.util.Arrays.asList(labels).indexOf(picked);
		if (idx < 0) return;
		String tagId = opts.get(idx).tagId;
		int removed = api.bulkRemoveTagFromGoals(selectedIds, tagId);
		log.debug("bulkRemoveTagFromGoals removed {} from {}", opts.get(idx).label, removed);
	}

	private void showBulkAddTagDialog(java.util.List<Goal> selectedGoals)
	{
		boolean allCustom = !selectedGoals.isEmpty();
		for (Goal g : selectedGoals)
		{
			if (g.getType() != GoalType.CUSTOM) { allCustom = false; break; }
		}
		final boolean forceFreeform = !allCustom;

		JPanel tagPanel = new JPanel(new java.awt.GridBagLayout());
		java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = java.awt.GridBagConstraints.WEST;

		JLabel catLabel = new JLabel("Category:");
		catLabel.setPreferredSize(new Dimension(80, 24));
		gbc.gridx = 0; gbc.gridy = 0; gbc.fill = java.awt.GridBagConstraints.NONE;
		tagPanel.add(catLabel, gbc);

		com.goaltracker.model.TagCategory[] categories =
			com.goaltracker.model.TagCategory.values();
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

		// Mission 21: editable combo populated from existing tags in the
		// selected category (find-or-create downstream). SKILLING is system-only.
		Runnable updateField = () -> {
			com.goaltracker.model.TagCategory cat =
				(com.goaltracker.model.TagCategory) catCombo.getSelectedItem();
			dropdownField.setEditable(cat != com.goaltracker.model.TagCategory.SKILLING);
			dropdownField.removeAllItems();
			java.util.List<com.goaltracker.api.TagView> all = api.queryAllTags();
			for (com.goaltracker.api.TagView t : all)
			{
				if (cat != null && cat.name().equals(t.category))
				{
					dropdownField.addItem(t.label);
				}
			}
			dropdownField.setSelectedItem("");
			((java.awt.CardLayout) fieldSwap.getLayout()).show(fieldSwap, "DROPDOWN");
		};
		catCombo.addActionListener(ev -> updateField.run());
		updateField.run();

		tagPanel.setPreferredSize(new Dimension(300, tagPanel.getPreferredSize().height));

		int result = JOptionPane.showConfirmDialog(
			this, tagPanel, "Add Tag to " + selectedGoals.size() + " goals",
			JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
		);
		if (result != JOptionPane.OK_OPTION) return;

		com.goaltracker.model.TagCategory selectedCat =
			(com.goaltracker.model.TagCategory) catCombo.getSelectedItem();
		Object raw = dropdownField.isEditable()
			? dropdownField.getEditor().getItem() : dropdownField.getSelectedItem();
		String tagText = raw == null ? "" : raw.toString().trim();
		if (tagText.isEmpty() || selectedCat == null) return;

		// Route through the internal API so the bulk path matches the single-item
		// path post-Mission 19. addTagWithCategory preserves the user-picked
		// category (api.addTag would force OTHER). Each call fires onGoalsChanged,
		// which fires N rebuilds for N selected goals — acceptable tradeoff for
		// keeping the API the canonical mutation surface; the user clicks OK once
		// so the cumulative work is bounded.
		for (Goal g : selectedGoals)
		{
			api.addTagWithCategory(g.getId(), tagText, selectedCat.name());
		}
	}

	/**
	 * Bulk Change Color dialog. Opens the ColorPickerField with a neutral
	 * default (mixed selections have no single sensible default), then applies
	 * the chosen color via api.setGoalColor for every selected goal.
	 */
	private void showBulkChangeColorDialog(java.util.List<Goal> selectedGoals)
	{
		ColorPickerField picker = new ColorPickerField(-1, 0x3C3C3C);
		int result = JOptionPane.showConfirmDialog(this, picker,
			"Color for " + selectedGoals.size() + " goals",
			JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result != JOptionPane.OK_OPTION) return;
		int rgb = picker.getSelectedRgb();
		for (Goal g : selectedGoals)
		{
			api.setGoalColor(g.getId(), rgb);
		}
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

		// Add Goal — opens the regular Add Goal dialog and drops the new goal
		// directly into this section. Hidden on Completed (auto-managed).
		if (!"COMPLETED".equals(section.kind))
		{
			JMenuItem addGoalHere = new JMenuItem("Add Goal");
			addGoalHere.addActionListener(e -> showAddGoalDialog(section.id));
			menu.add(addGoalHere);
			menu.addSeparator();
		}

		// Select / Deselect All in Section — label flips when every goal in the
		// section is already selected. Computed against the current selection
		// snapshot at menu-build time.
		java.util.Set<String> currentSel = api.getSelectedGoalIds();
		java.util.List<String> sectionGoalIds = new java.util.ArrayList<>();
		for (Goal g : goalStore.getGoals())
		{
			if (section.id.equals(g.getSectionId())) sectionGoalIds.add(g.getId());
		}
		boolean allSelected = !sectionGoalIds.isEmpty() && currentSel.containsAll(sectionGoalIds);
		JMenuItem selectAll = new JMenuItem(allSelected
			? "Deselect All in Section"
			: "Select All in Section");
		selectAll.addActionListener(e -> {
			if (allSelected) api.deselectAllInSection(section.id);
			else api.selectAllInSection(section.id);
		});
		// Disable on empty sections — nothing to (de)select.
		if (sectionGoalIds.isEmpty()) selectAll.setEnabled(false);
		menu.add(selectAll);

		menu.addSeparator();

		// Change Color is available on every section, built-in or user.
		JMenuItem changeColor = new JMenuItem("Change Color");
		changeColor.addActionListener(e -> showSectionColorDialog(section));
		menu.add(changeColor);

		// User-section-only items: rename, move up/down, delete.
		if (!section.builtIn)
		{
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
		}

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

	private void showSectionColorDialog(com.goaltracker.api.SectionView section)
	{
		int current = section.colorOverridden ? section.colorRgb : -1;
		ColorPickerField picker = new ColorPickerField(current, section.defaultColorRgb);
		int result = JOptionPane.showConfirmDialog(this, picker,
			"Section Color — " + section.name,
			JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result != JOptionPane.OK_OPTION) return;
		api.setSectionColor(section.id, picker.getSelectedRgb());
	}

	private void showGoalColorDialog(Goal goal)
	{
		int defaultRgb;
		java.awt.Color c = goal.getType().getColor();
		defaultRgb = (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();
		ColorPickerField picker = new ColorPickerField(goal.getCustomColorRgb(), defaultRgb);
		int result = JOptionPane.showConfirmDialog(this, picker,
			"Goal Color — " + goal.getName(),
			JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result != JOptionPane.OK_OPTION) return;
		api.setGoalColor(goal.getId(), picker.getSelectedRgb());
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

	private void showAddGoalDialog(String preferredSectionId)
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

		// Row 1.5 (Mission 23): Mode toggle for relative goals.
		// "Reach X" = absolute (existing behavior). "Gain X more" = compute
		// resolved target as currentValue + entered delta. SKILL/ITEM/CUSTOM
		// all support this; the actual math runs in the submit handlers.
		gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
		JLabel modeLabel = new JLabel("Mode:");
		modeLabel.setPreferredSize(new Dimension(labelWidth, 24));
		panel.add(modeLabel, gbc);

		javax.swing.JRadioButton modeAbsolute = new javax.swing.JRadioButton("Reach X", true);
		javax.swing.JRadioButton modeRelative = new javax.swing.JRadioButton("Gain X more");
		modeAbsolute.setOpaque(false);
		modeRelative.setOpaque(false);
		javax.swing.ButtonGroup modeGroup = new javax.swing.ButtonGroup();
		modeGroup.add(modeAbsolute);
		modeGroup.add(modeRelative);
		JPanel modeRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 0));
		modeRow.setOpaque(false);
		modeRow.add(modeAbsolute);
		modeRow.add(modeRelative);
		gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
		panel.add(modeRow, gbc);

		// Row 3 (was Row 2): Field 2
		JLabel label2 = new JLabel("Target:");
		label2.setPreferredSize(new Dimension(labelWidth, 24));
		gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
		panel.add(label2, gbc);

		JTextField descField = new JTextField(15);
		JLabel itemHint = new JLabel("<html><i>Item search opens in-game</i></html>");
		itemHint.setForeground(new Color(140, 140, 140));

		// Skill row uses the shared SkillTargetForm with synced Level/XP fields.
		SkillTargetForm skillTargetForm = new SkillTargetForm(99);

		JPanel field2Panel = new JPanel(new CardLayout());
		field2Panel.add(skillTargetForm, "SKILL");
		field2Panel.add(itemHint, "ITEM_GRIND");
		field2Panel.add(descField, "CUSTOM");
		gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
		panel.add(field2Panel, gbc);

		// Swap fields when type changes. Mission 23: also relabel based on
		// current mode so "Quantity:" → "Gain Quantity:" when relative.
		Runnable updateLabels = () ->
		{
			GoalType selected = (GoalType) typeCombo.getSelectedItem();
			((CardLayout) field1Panel.getLayout()).show(field1Panel, selected.name());
			((CardLayout) field2Panel.getLayout()).show(field2Panel, selected.name());
			// Mission 23: relative mode is only meaningful for SKILL right now
			// (and will support BOSS_KILL_COUNT in a future mission). Hide the
			// row for ITEM_GRIND (no reliable baseline) and CUSTOM (no int target).
			boolean modeRowVisible = selected == GoalType.SKILL;
			modeLabel.setVisible(modeRowVisible);
			modeRow.setVisible(modeRowVisible);
			if (!modeRowVisible) modeAbsolute.setSelected(true);
			boolean rel = modeRelative.isSelected();
			// Mission 23: if relative + SKILL, hand the form the player's
			// current XP for the chosen skill so deltas resolve correctly.
			if (rel && selected == GoalType.SKILL && client != null)
			{
				Skill chosen = (Skill) skillCombo.getSelectedItem();
				int currentXp = chosen != null ? client.getSkillExperience(chosen) : 0;
				skillTargetForm.setRelativeBaseline(currentXp);
			}
			else
			{
				skillTargetForm.setRelativeBaseline(-1);
			}
			switch (selected)
			{
				case SKILL:
					label1.setText("Skill:");
					label2.setText(rel ? "Add XP:" : "Target:");
					break;
				case ITEM_GRIND:
					label1.setText(rel ? "Gain qty:" : "Quantity:");
					label2.setText("");
					break;
				default:
					label1.setText("Goal Name:");
					label2.setText(rel ? "Description (gain target via Custom value):" : "Description:");
					break;
			}
			Window w = SwingUtilities.getWindowAncestor(panel);
			if (w != null) w.pack();
		};
		typeCombo.addActionListener(e -> updateLabels.run());
		modeAbsolute.addActionListener(e -> updateLabels.run());
		modeRelative.addActionListener(e -> updateLabels.run());
		skillCombo.addActionListener(e -> updateLabels.run());

		panel.setPreferredSize(new Dimension(320, panel.getPreferredSize().height));

		int result = JOptionPane.showConfirmDialog(
			this, panel, "Add Goal", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
		);

		if (result == JOptionPane.OK_OPTION)
		{
			GoalType selectedType = (GoalType) typeCombo.getSelectedItem();
			boolean relative = modeRelative.isSelected();

			if (selectedType == GoalType.SKILL)
			{
				addSkillGoal(skillCombo, skillTargetForm, preferredSectionId, relative);
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
					itemSearchCallback.accept(qty);
				}
				catch (NumberFormatException e)
				{
					JOptionPane.showMessageDialog(this, "Invalid quantity.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			else if (selectedType == GoalType.CUSTOM)
			{
				// CUSTOM goals are boolean (target=1) — relative mode is a no-op.
				addCustomGoal(nameField, descField, preferredSectionId);
			}
		}
	}

	private void addSkillGoal(JComboBox<Skill> skillCombo, SkillTargetForm form, String preferredSectionId, boolean relative)
	{
		Skill skill = (Skill) skillCombo.getSelectedItem();
		int formValue = form.getTargetXp();
		if (formValue < 0)
		{
			JOptionPane.showMessageDialog(this,
				relative ? "Enter a valid XP delta (1–200,000,000)."
					: "Enter a valid target level (1–99) or XP (0–200,000,000).",
				"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Mission 23: in relative mode, the form returns a delta. Resolve to
		// absolute by adding the player's current XP for the chosen skill.
		int targetXp;
		if (relative)
		{
			int currentXp = client != null ? client.getSkillExperience(skill) : 0;
			targetXp = RelativeTargetResolver.resolveSkillXp(currentXp, formValue);
			if (targetXp < 0)
			{
				JOptionPane.showMessageDialog(this, "XP delta must be greater than 0.",
					"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		else
		{
			targetXp = formValue;
		}

		String conflict = checkSkillConflict(skill, targetXp);
		if (conflict != null)
		{
			JOptionPane.showMessageDialog(this, conflict, "Conflict", JOptionPane.WARNING_MESSAGE);
			return;
		}

		String createdId = api.addSkillGoal(skill, targetXp);
		moveToPreferredSection(createdId, preferredSectionId);
		// API callback rebuilds the panel.
	}

	private void addCustomGoal(JTextField nameField, JTextField descField, String preferredSectionId)
	{
		String name = nameField.getText().trim();
		if (name.isEmpty())
		{
			JOptionPane.showMessageDialog(this, "Goal name is required.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		String createdId = api.addCustomGoal(name, descField.getText().trim());
		moveToPreferredSection(createdId, preferredSectionId);
		// API callback rebuilds the panel.
	}

	/**
	 * Move a freshly-created goal to a section other than the default Incomplete.
	 * Used by the section header "Add Goal" entry to drop new goals directly
	 * into the section the user right-clicked. No-op when preferredSectionId is
	 * null (the toolbar + button) or the goal didn't actually get created.
	 */
	private void moveToPreferredSection(String goalId, String preferredSectionId)
	{
		if (goalId != null && preferredSectionId != null)
		{
			api.moveGoalToSection(goalId, preferredSectionId);
		}
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
