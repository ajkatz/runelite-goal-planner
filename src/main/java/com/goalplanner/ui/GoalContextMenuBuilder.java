package com.goalplanner.ui;

import com.goalplanner.api.GoalPlannerApiImpl;
import com.goalplanner.model.Goal;
import com.goalplanner.model.GoalType;
import com.goalplanner.persistence.GoalStore;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Builds right-click context menus for goal cards and section headers.
 * Extracted from GoalPanel to keep menu construction separate from panel layout.
 */
@Slf4j
class GoalContextMenuBuilder
{
	private final GoalPlannerApiImpl api;
	private final GoalStore goalStore;
	private final GoalPanel panel;
	private final GoalDialogFactory dialogFactory;
	private final GoalReorderController reorderController;

	GoalContextMenuBuilder(GoalPlannerApiImpl api,
						   GoalStore goalStore,
						   GoalPanel panel,
						   GoalDialogFactory dialogFactory,
						   GoalReorderController reorderController)
	{
		this.api = api;
		this.goalStore = goalStore;
		this.panel = panel;
		this.dialogFactory = dialogFactory;
		this.reorderController = reorderController;
	}

	/**
	 * Build the tag-management menu entry. When both Add and Remove apply,
	 * collapse them into a single "Tag" submenu so the parent menu doesn't
	 * spend two lines on tagging. When only one applies, return that as a
	 * flat item ("Add Tag" / "Remove Tag") since a single-child submenu
	 * would be a hover step for nothing. Returns null when neither applies.
	 */
	private static JMenuItem buildTagMenuEntry(
		boolean canAdd, Runnable addAction,
		boolean canRemove, Runnable removeAction)
	{
		if (canAdd && canRemove)
		{
			JMenu submenu = new JMenu("Tag");
			JMenuItem add = new JMenuItem("Add");
			add.addActionListener(e -> addAction.run());
			submenu.add(add);
			JMenuItem remove = new JMenuItem("Remove");
			remove.addActionListener(e -> removeAction.run());
			submenu.add(remove);
			return submenu;
		}
		if (canAdd)
		{
			JMenuItem add = new JMenuItem("Add Tag");
			add.addActionListener(e -> addAction.run());
			return add;
		}
		if (canRemove)
		{
			JMenuItem remove = new JMenuItem("Remove Tag");
			remove.addActionListener(e -> removeAction.run());
			return remove;
		}
		return null;
	}

	/**
	 * Attach a right-click popup trigger to a goal card. Delegates to
	 * {@link #buildSingleItemMenu} or {@link #buildBulkMenu} depending on
	 * whether the card is part of an active multi-selection.
	 */
	void addContextMenu(GoalCard card, Goal goal, int index, int sectionStart, int sectionEnd)
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
				// Right-click exits whichever pick mode is active. The
				// user is clearly navigating away from the action they
				// started; show the normal context menu of the clicked
				// card instead of stranding them in mode.
				if (!panel.pendingRelationSourceIds.isEmpty()) panel.exitRelationMode();
				if (panel.pendingMoveSourceId != null) panel.exitMoveMode();
				// Bulk menu (multi-selection) renders through ColumnMenu —
				// click-driven, hover-stable side-by-side columns instead
				// of cascading JPopupMenu submenus. Single-item menu still
				// uses JPopupMenu for now (prototype phase). The
				// auto-deselect rule lives at the API layer, so the menu
				// model rebuild needs no special wrapping either way.
				Set<String> sel = api.getSelectedGoalIds();
				if (sel.contains(goal.getId()) && sel.size() >= 2)
				{
					JPopupMenu bulkSource = buildBulkMenu(goal.getId());
					com.goalplanner.ui.columnmenu.ColumnMenu.show(card, e.getX(), e.getY(),
						com.goalplanner.ui.columnmenu.MenuTreeAdapter.fromPopup(bulkSource));
				}
				else
				{
					JPopupMenu popup = buildSingleItemMenu(goal, index, sectionStart, sectionEnd);
					popup.show(card, e.getX(), e.getY());
				}
			}
		});
	}

	/**
	 * Compute the intersection of an edge-list reader's results across a
	 * set of goals. Used by the bulk Relations submenu: a common
	 * requirement (or dependent) is one every selected goal carries.
	 * Returns an empty list when {@code goals} is empty so callers can
	 * gate "show submenu" on emptiness.
	 *
	 * <p>Package-private + static so a test can verify the intersection
	 * rule without standing up a full popup-menu rendering pipeline.
	 */
	static List<String> commonEdges(
		java.util.function.Function<String, List<String>> edgeReader,
		Iterable<Goal> goals)
	{
		List<String> common = null;
		for (Goal g : goals)
		{
			List<String> edges = edgeReader.apply(g.getId());
			if (common == null)
			{
				common = new ArrayList<>(edges);
			}
			else
			{
				common.retainAll(edges);
			}
			if (common.isEmpty()) break;
		}
		return common != null ? common : Collections.emptyList();
	}

	/**
	 * Builds the normal per-card right-click menu. Called lazily on each
	 * popup show so the contents reflect current selection / completion / tag
	 * state without needing to be rebuilt at panel.rebuild() time.
	 */
	JPopupMenu buildSingleItemMenu(Goal goal, int index, int sectionStart, int sectionEnd)
	{
		JPopupMenu menu = new JPopupMenu();

		// Selection toggle — first item so it's predictable. Label flips based on
		// the goal's current selection state. Routes through the same internal API
		// the click handler uses, so multi-select state stays consistent.
		Set<String> selectedIds = api.getSelectedGoalIds();
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

		// Move-to-Top / Move-to-Bottom are accessed via right-clicking the
		// up/down arrow buttons on the card itself (see GoalCard.createArrowButton).

		// Add Goal submenu — Top/Bottom of section, Above/Below
		// the right-clicked card. Above/Below grayed at section boundaries.
		if (!goal.isComplete() && goal.getSectionId() != null)
		{
			final String secId = goal.getSectionId();
			final int posInSection = index - sectionStart;
			JMenu addGoalMenu = new JMenu("Add Goal");

			JMenuItem addTop = new JMenuItem("At Top of Section");
			addTop.addActionListener(e -> {
				dialogFactory.pendingAddPositionInSection = 0;
				dialogFactory.showAddGoalDialog(secId);
			});
			addGoalMenu.add(addTop);

			JMenuItem addBottom = new JMenuItem("At Bottom of Section");
			addBottom.addActionListener(e -> {
				dialogFactory.pendingAddPositionInSection = Integer.MAX_VALUE;
				dialogFactory.showAddGoalDialog(secId);
			});
			addGoalMenu.add(addBottom);

			// Above/Below are always valid: "above the first" lands at the
			// top, "below the last" lands at the bottom — both equivalent to
			// the dedicated Top/Bottom items, but enabling them avoids the
			// surprise of a greyed-out option on edge-of-section goals.
			JMenuItem addAbove = new JMenuItem("Above This Goal");
			addAbove.addActionListener(e -> {
				dialogFactory.pendingAddPositionInSection = posInSection;
				dialogFactory.showAddGoalDialog(secId);
			});
			addGoalMenu.add(addAbove);

			JMenuItem addBelow = new JMenuItem("Below This Goal");
			addBelow.addActionListener(e -> {
				dialogFactory.pendingAddPositionInSection = posInSection + 1;
				dialogFactory.showAddGoalDialog(secId);
			});
			addGoalMenu.add(addBelow);

			// Leagues shortcut — direct-create both goal types at the tier/area
			// milestones without opening the dialog. Lands just below the
			// right-clicked card so the user can rapidly stack related leagues
			// goals next to each other.
			addGoalMenu.addSeparator();
			JMenu leaguesMenu = new JMenu("Leagues Goal");

			JMenu pointsMenu = new JMenu("League Points");
			for (int i = 0; i < com.goalplanner.model.AccountMetric.LEAGUE_TIER_NAMES.length; i++)
			{
				final int target = com.goalplanner.model.AccountMetric.LEAGUE_TIER_VALUES[i];
				JMenuItem item = new JMenuItem(com.goalplanner.model.AccountMetric.LEAGUE_TIER_NAMES[i]);
				item.addActionListener(e -> createLeaguesShortcut(
					com.goalplanner.model.AccountMetric.LEAGUE_POINTS, target, secId, posInSection + 1));
				pointsMenu.add(item);
			}
			leaguesMenu.add(pointsMenu);

			JMenu tasksMenu = new JMenu("Leagues Tasks");
			for (int i = 0; i < com.goalplanner.model.AccountMetric.LEAGUE_AREA_NAMES.length; i++)
			{
				final int target = com.goalplanner.model.AccountMetric.LEAGUE_AREA_VALUES[i];
				JMenuItem item = new JMenuItem(com.goalplanner.model.AccountMetric.LEAGUE_AREA_NAMES[i]);
				item.addActionListener(e -> createLeaguesShortcut(
					com.goalplanner.model.AccountMetric.LEAGUE_TASKS, target, secId, posInSection + 1));
				tasksMenu.add(item);
			}
			leaguesMenu.add(tasksMenu);

			addGoalMenu.add(leaguesMenu);

			menu.add(addGoalMenu);
		}

		if (menu.getComponentCount() > 0)
		{
			menu.addSeparator();
		}

		// Quest Helper link — surfaced at the top of the per-goal block so it
		// reads as a navigation/launch action, distinct from the property-edit
		// items below. Quest goals link by name; diary goals link by (area,
		// tier) parsed from the description.
		boolean addedQuestHelper = false;
		if (goal.getType() == GoalType.QUEST && goal.getQuestName() != null
			&& !goal.isComplete() && panel.questHelperCallback != null
			&& panel.questHelperAvailable != null && panel.questHelperAvailable.get())
		{
			JMenuItem qhItem = new JMenuItem("Open in Quest Helper");
			qhItem.addActionListener(e -> panel.questHelperCallback.accept(goal.getQuestName()));
			menu.add(qhItem);
			addedQuestHelper = true;
		}
		else if (goal.getType() == GoalType.DIARY && goal.getName() != null
			&& !goal.isComplete() && panel.diaryHelperCallback != null
			&& panel.questHelperAvailable != null && panel.questHelperAvailable.get())
		{
			com.goalplanner.data.AchievementDiaryData.Tier dhTier =
				com.goalplanner.data.AchievementDiaryData.parseTierFromDescription(goal.getDescription());
			if (dhTier != null)
			{
				JMenuItem dhItem = new JMenuItem("Open in Quest Helper");
				final String dhTierDisplay = dhTier.getDisplayName();
				dhItem.addActionListener(e -> panel.diaryHelperCallback.accept(goal.getName(), dhTierDisplay));
				menu.add(dhItem);
				addedQuestHelper = true;
			}
		}
		if (addedQuestHelper)
		{
			menu.addSeparator();
		}

		// Manual completion: CUSTOM and ITEM_GRIND. Skill/quest/diary/CA are
		// purely game-driven. ITEM_GRIND is terminal once complete:
		// dropping below the target does NOT auto-revert. The user must
		// manually mark the goal incomplete to let the tracker re-evaluate.
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

		// Customize submenu — groups general property edits (optional flag,
		// name/description for CUSTOM, color, tags, relations, restore defaults)
		// under a single hover so the top-level menu stays scannable. Mark
		// Complete/Incomplete and Change Amount stay top-level: completion is
		// the primary lifecycle action, and Change Amount is the most-edited
		// field for skill/item goals.
		JMenu customizeMenu = new JMenu("Customize");

		// Optional toggle — hidden on completed goals; the optional/required
		// distinction only affects how active goals are weighted/displayed,
		// so it's noise on completed cards.
		if (!goal.isComplete())
		{
			boolean isOptional = goal.isOptional();
			JMenuItem optionalItem = new JMenuItem(isOptional ? "Mark Required" : "Mark Optional");
			optionalItem.addActionListener(e -> api.setGoalOptional(goal.getId(), !isOptional));
			customizeMenu.add(optionalItem);
		}

		if (goal.getType() == GoalType.CUSTOM && !goal.isComplete())
		{
			JMenuItem editName = new JMenuItem("Change Name");
			editName.addActionListener(e -> {
				String input = JOptionPane.showInputDialog(panel, "New name:", goal.getName());
				if (input != null && !input.trim().isEmpty())
				{
					api.editCustomGoal(goal.getId(), input.trim(), null);
				}
			});
			customizeMenu.add(editName);

			JMenuItem editDesc = new JMenuItem("Change Description");
			editDesc.addActionListener(e -> {
				String input = JOptionPane.showInputDialog(panel, "New description:",
					goal.getDescription() != null ? goal.getDescription() : "");
				if (input != null)
				{
					api.editCustomGoal(goal.getId(), null, input.trim());
				}
			});
			customizeMenu.add(editDesc);
		}

		// Change Color is available on all active goal types — override persists
		// on the goal model so rebuilds don't clobber it. Hidden on completed
		// goals to keep the menu lean (re-coloring history is noise).
		if (!goal.isComplete())
		{
			JMenuItem changeGoalColor = new JMenuItem("Change Color");
			changeGoalColor.addActionListener(e -> dialogFactory.showGoalColorDialog(goal));
			customizeMenu.add(changeGoalColor);
		}

		// Skill-specific options
		if (goal.getType() == GoalType.SKILL && !goal.isComplete())
		{
			JMenuItem editLevel = new JMenuItem("Change Amount");
			editLevel.addActionListener(e -> dialogFactory.showChangeSkillTargetDialog(goal));
			menu.add(editLevel);
		}

		// Item-specific options
		if (goal.getType() == GoalType.ITEM_GRIND && !goal.isComplete())
		{
			JMenuItem editQty = new JMenuItem("Change Amount");
			editQty.addActionListener(e -> {
				String input = JOptionPane.showInputDialog(
					panel,
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
							// quantity; no follow-up mutation needed.
							api.changeTarget(goal.getId(), newQty);
						}
					}
					catch (NumberFormatException ignored) {}
				}
			});
			menu.add(editQty);
		}

		// Tag management — routes through the shared TagPickerDialog so the
		// single-item and bulk Add Tag flows stay in lockstep (category list,
		// SKILLING lock, freeform/dropdown switch).
		// Removable tags: for CUSTOM goals, anything. For everything else, only
		// user-added tags (not in defaultTagIds). Dereference tag ids
		// through the store and operate on Tag entities.
		List<com.goalplanner.model.Tag> removableTags = new ArrayList<>();
		List<com.goalplanner.model.Tag> allGoalTags = new ArrayList<>();
		if (goal.getTagIds() != null && !goal.getTagIds().isEmpty())
		{
			List<String> defaults = goal.getDefaultTagIds() != null
				? goal.getDefaultTagIds() : Collections.emptyList();
			for (String tagId : goal.getTagIds())
			{
				com.goalplanner.model.Tag t = goalStore.findTag(tagId);
				if (t == null) continue;
				allGoalTags.add(t);
				if (goal.getType() == GoalType.CUSTOM || !defaults.contains(tagId))
				{
					removableTags.add(t);
				}
			}
		}

		// Completed goals are tag-frozen, so no Add. Remove still allowed
		// (cleanup of stale tags). When both Add and Remove apply, collapse
		// them under a single "Tag" submenu to save a menu line.
		final List<com.goalplanner.model.Tag> finalRemovable = removableTags;
		Runnable addTagAction = () -> {
			TagPickerDialog.Result picked = TagPickerDialog.show(panel, "Add Tag", api);
			if (picked != null)
			{
				api.addTagWithCategory(goal.getId(), picked.label, picked.category.name());
			}
		};
		Runnable removeTagAction = () -> {
			String[] tagNames = finalRemovable.stream()
				.map(t -> t.getLabel() + " (" + t.getCategory().getDisplayName() + ")")
				.toArray(String[]::new);

			String selected = (String) JOptionPane.showInputDialog(
				panel, "Select tag to remove:", "Remove Tag",
				JOptionPane.PLAIN_MESSAGE, null, tagNames, tagNames[0]
			);
			if (selected != null)
			{
				int idx = Arrays.asList(tagNames).indexOf(selected);
				if (idx >= 0)
				{
					api.removeTag(goal.getId(), finalRemovable.get(idx).getLabel());
				}
			}
		};
		JMenuItem tagEntry = buildTagMenuEntry(
			!goal.isComplete(), addTagAction,
			!removableTags.isEmpty(), removeTagAction);
		if (tagEntry != null) customizeMenu.add(tagEntry);

		// Relations. All requirement-graph editing collapsed under a single
		// "Relations" parent so the top-level menu stays short. "Requires..."
		// and "Required by..." enter a click-mode where the user clicks
		// another goal to link. The Remove children are direct pick-to-remove
		// lists of the current edges. Hidden for completed goals: completed
		// items are reference history, not active tracking — editing their
		// prerequisite graph adds noise with no behavioral payoff.
		if (!goal.isComplete())
		{
			JMenu relationsMenu = new JMenu("Relations");

			JMenuItem addRequirement = new JMenuItem("Requires\u2026");
			addRequirement.setToolTipText(
				"Click, then click another goal to mark it as a requirement of this one.");
			addRequirement.addActionListener(e ->
				panel.enterRelationMode(goal.getId(), /*sourceRequiresTarget=*/true));
			relationsMenu.add(addRequirement);

			JMenuItem addDependent = new JMenuItem("Required by\u2026");
			addDependent.setToolTipText(
				"Click, then click another goal that should require this one.");
			addDependent.addActionListener(e ->
				panel.enterRelationMode(goal.getId(), /*sourceRequiresTarget=*/false));
			relationsMenu.add(addDependent);

			List<String> currentRequirements = api.getRequirements(goal.getId());
			List<String> currentDependents = api.getDependents(goal.getId());
			if (!currentRequirements.isEmpty() || !currentDependents.isEmpty())
			{
				relationsMenu.addSeparator();
			}

			// Remove requirement submenu — only when there's something to remove.
			if (!currentRequirements.isEmpty())
			{
				JMenu removeReqMenu = new JMenu("Remove Requirement");
				for (String reqId : currentRequirements)
				{
					String label = reorderController.goalNameById(reqId);
					JMenuItem item = new JMenuItem(label);
					item.addActionListener(e -> api.removeRequirement(goal.getId(), reqId));
					removeReqMenu.add(item);
				}
				relationsMenu.add(removeReqMenu);
			}

			// Remove dependent submenu — only when this goal is depended-on.
			if (!currentDependents.isEmpty())
			{
				JMenu removeDepMenu = new JMenu("Remove Dependent");
				for (String depId : currentDependents)
				{
					String label = reorderController.goalNameById(depId);
					JMenuItem item = new JMenuItem(label);
					item.addActionListener(e -> api.removeRequirement(depId, goal.getId()));
					removeDepMenu.add(item);
				}
				relationsMenu.add(removeDepMenu);
			}

			customizeMenu.add(relationsMenu);
		}

		// Restore Defaults — gated on isGoalOverridden (tag drift
		// OR color override). Routes through the bulk API so the single-item
		// path resets BOTH tags and color in one shot.
		if (api.isGoalOverridden(goal.getId()))
		{
			JMenuItem restore = new JMenuItem("Restore Defaults");
			restore.addActionListener(e ->
				api.bulkRestoreDefaults(Collections.singleton(goal.getId())));
			customizeMenu.add(restore);
		}

		// Add the Customize submenu only if it actually has content. On a
		// completed goal with no removable tags and no overrides, every item
		// inside is gated off and the submenu would be a dead hover.
		if (customizeMenu.getMenuComponentCount() > 0)
		{
			menu.add(customizeMenu);
		}

		// Move submenu — sibling of Customize. Collects all relocation actions
		// under one hover: Move to Top/Bottom reorder within the current
		// section; Move to… enters click-mode; Move to Section ▶ lists every
		// other valid section plus a "New Section" option, so almost every
		// incomplete goal has at least one valid destination. Hidden on
		// completed goals — Completed is terminal and reordering completed
		// history adds noise.
		if (!goal.isComplete())
		{
			JMenu moveMenu = new JMenu("Move");

			int sectionSize = sectionEnd - sectionStart + 1;
			if (sectionSize > 1)
			{
				JMenuItem moveToTop = new JMenuItem("Move to Top");
				moveToTop.addActionListener(e ->
					reorderController.moveGoalTo(goal.getId(), sectionStart));
				moveMenu.add(moveToTop);

				JMenuItem moveToBottom = new JMenuItem("Move to Bottom");
				moveToBottom.addActionListener(e ->
					reorderController.moveGoalTo(goal.getId(), sectionEnd));
				moveMenu.add(moveToBottom);
			}

			JMenuItem moveToPicker = new JMenuItem("Move to…");
			moveToPicker.setToolTipText(
				"Click, then click another goal to place this one above it, or click + New Section.");
			moveToPicker.addActionListener(e -> panel.enterMoveMode(goal.getId()));
			moveMenu.add(moveToPicker);

			JMenu moveToSection = new JMenu("Move to Section");

			List<com.goalplanner.api.SectionView> allSections = api.queryAllSections();
			List<com.goalplanner.api.SectionView> destinations = new ArrayList<>();
			for (com.goalplanner.api.SectionView sv : allSections)
			{
				if ("COMPLETED".equals(sv.kind)) continue;
				if (sv.id.equals(goal.getSectionId())) continue;
				destinations.add(sv);
			}
			for (com.goalplanner.api.SectionView dest : destinations)
			{
				JMenuItem item = new JMenuItem(dest.name);
				item.addActionListener(e -> api.moveGoalToSection(goal.getId(), dest.id));
				moveToSection.add(item);
			}
			if (!destinations.isEmpty())
			{
				moveToSection.addSeparator();
			}
			JMenuItem newSectionItem = new JMenuItem("Move to New Section…");
			newSectionItem.addActionListener(e -> {
				String input = JOptionPane.showInputDialog(panel, "New section name:", "");
				if (input != null && !input.trim().isEmpty())
				{
					String newId = api.createSection(input.trim());
					if (newId != null)
					{
						api.moveGoalToSection(goal.getId(), newId);
					}
				}
			});
			moveToSection.add(newSectionItem);

			moveMenu.add(moveToSection);

			menu.add(moveMenu);
		}

		JMenuItem remove = new JMenuItem("Remove Goal");
		remove.addActionListener(e -> api.removeGoal(goal.getId()));
		menu.add(remove);

		return menu;
	}

	/**
	 * Direct-create an account goal from the Leagues shortcut submenu and
	 * drop it into the given section at the given slot. Wrapped in a
	 * compound so undo treats it as one step.
	 */
	private void createLeaguesShortcut(com.goalplanner.model.AccountMetric metric,
									   int target, String sectionId, int positionInSection)
	{
		api.beginCompound("Add " + metric.getDisplayName() + " (" + target + ")");
		try
		{
			String createdId = api.addAccountGoal(metric.name(), target);
			if (createdId != null && sectionId != null)
			{
				api.positionGoalInSection(createdId, sectionId, positionInSection);
			}
		}
		finally
		{
			api.endCompound();
		}
	}

	/**
	 * Builds the streamlined bulk-action menu shown when the right-clicked
	 * card is part of a multi-selection (size >= 2). Five items only:
	 * Move to Section, Add Tag, Change Color, Remove, Mark as Complete.
	 */
	JPopupMenu buildBulkMenu(String rightClickedGoalId)
	{
		JPopupMenu menu = new JPopupMenu();
		Set<String> selectedIds = api.getSelectedGoalIds();
		int selectionSize = selectedIds.size();

		// Snapshot the selected Goal objects up front so all handlers operate
		// on a consistent set even if the underlying selection mutates between
		// menu open and item pick.
		List<Goal> selectedGoals = new ArrayList<>();
		for (Goal g : goalStore.getGoals())
		{
			if (selectedIds.contains(g.getId())) selectedGoals.add(g);
		}

		// Header label so the user knows the menu applies to N cards
		JMenuItem header = new JMenuItem(selectionSize + " selected");
		header.setEnabled(false);
		menu.add(header);

		// Selection toggle + deselect all on the bulk menu so the
		// user can drop one card or escape the whole multi-selection without
		// having to find a single-card popup.
		menu.addSeparator();
		JMenuItem deselectThis = new JMenuItem("Deselect this");
		deselectThis.addActionListener(e -> api.removeFromGoalSelection(rightClickedGoalId));
		menu.add(deselectThis);
		JMenuItem deselectOthers = new JMenuItem("Deselect all but this");
		deselectOthers.addActionListener(e ->
			api.replaceGoalSelection(Collections.singleton(rightClickedGoalId)));
		menu.add(deselectOthers);
		JMenuItem deselectAll = new JMenuItem("Deselect All");
		deselectAll.addActionListener(e -> api.clearGoalSelection());
		menu.add(deselectAll);
		menu.addSeparator();

		// Mark as Complete — top-level (mirrors single-item menu where
		// Mark Complete/Incomplete is a primary lifecycle action). Only
		// when ALL selected are CUSTOM (per locked design).
		boolean allCustom = !selectedGoals.isEmpty();
		for (Goal g : selectedGoals)
		{
			if (g.getType() != GoalType.CUSTOM) { allCustom = false; break; }
		}
		if (allCustom)
		{
			JMenuItem markComplete = new JMenuItem("Mark as Complete");
			markComplete.addActionListener(e -> {
				api.beginCompound("Mark " + selectedGoals.size() + " complete");
				try
				{
					for (Goal g : selectedGoals) api.markGoalComplete(g.getId());
				}
				finally { api.endCompound(); }
			});
			menu.add(markComplete);
		}

		// Customize submenu — collects property-edit actions that apply to
		// the whole selection (optional flag, color, tags, restore defaults).
		// Mirrors the single-item Customize submenu so the menu shape is the
		// same whether the user has one or many cards selected.
		JMenu customizeMenu = new JMenu("Customize");

		// Mark Optional/Required — only applies to non-completed goals
		// (matches single-item gating). When the selection is uniform
		// (all required or all optional), show a single flip action;
		// when mixed, show a submenu so the user can force either state.
		List<Goal> optionalTargets = new ArrayList<>();
		for (Goal g : selectedGoals)
		{
			if (!g.isComplete()) optionalTargets.add(g);
		}
		if (!optionalTargets.isEmpty())
		{
			boolean anyOptional = false;
			boolean anyRequired = false;
			for (Goal g : optionalTargets)
			{
				if (g.isOptional()) anyOptional = true;
				else anyRequired = true;
				if (anyOptional && anyRequired) break;
			}
			final List<Goal> finalOptionalTargets = optionalTargets;
			Runnable markAllOptional = () -> {
				api.beginCompound("Mark " + finalOptionalTargets.size() + " optional");
				try
				{
					for (Goal g : finalOptionalTargets) api.setGoalOptional(g.getId(), true);
				}
				finally { api.endCompound(); }
			};
			Runnable markAllRequired = () -> {
				api.beginCompound("Mark " + finalOptionalTargets.size() + " required");
				try
				{
					for (Goal g : finalOptionalTargets) api.setGoalOptional(g.getId(), false);
				}
				finally { api.endCompound(); }
			};
			if (anyRequired && !anyOptional)
			{
				JMenuItem item = new JMenuItem("Mark as Optional");
				item.addActionListener(e -> markAllOptional.run());
				customizeMenu.add(item);
			}
			else if (anyOptional && !anyRequired)
			{
				JMenuItem item = new JMenuItem("Mark as Required");
				item.addActionListener(e -> markAllRequired.run());
				customizeMenu.add(item);
			}
			else
			{
				JMenu optionalSubmenu = new JMenu("Optional");
				JMenuItem makeOptional = new JMenuItem("Optional");
				makeOptional.addActionListener(e -> markAllOptional.run());
				optionalSubmenu.add(makeOptional);
				JMenuItem makeRequired = new JMenuItem("Required");
				makeRequired.addActionListener(e -> markAllRequired.run());
				optionalSubmenu.add(makeRequired);
				customizeMenu.add(optionalSubmenu);
			}
		}

		// Change Color — applies only to active goals in the selection;
		// completed goals are recolor-frozen (matches single-item menu).
		List<Goal> recolorTargets = new ArrayList<>();
		for (Goal g : selectedGoals)
		{
			if (!g.isComplete()) recolorTargets.add(g);
		}
		if (!recolorTargets.isEmpty())
		{
			JMenuItem changeColor = new JMenuItem("Change Color");
			changeColor.addActionListener(e -> dialogFactory.showBulkChangeColorDialog(recolorTargets));
			customizeMenu.add(changeColor);
		}

		// Tag — Add applies only to active goals in the selection (completed
		// are tag-frozen, matching the single-item menu); Remove still applies
		// to any tagged goal so users can clean up stale tags on completed cards.
		List<Goal> tagAddTargets = new ArrayList<>();
		for (Goal g : selectedGoals)
		{
			if (!g.isComplete()) tagAddTargets.add(g);
		}
		List<com.goalplanner.api.GoalPlannerInternalApi.TagRemovalOption> removableOpts =
			api.getRemovableTagsForSelection(selectedIds);
		JMenuItem bulkTagEntry = buildTagMenuEntry(
			!tagAddTargets.isEmpty(),
			() -> dialogFactory.showBulkAddTagDialog(tagAddTargets),
			!removableOpts.isEmpty(),
			() -> dialogFactory.showBulkRemoveTagDialog(selectedIds, removableOpts));
		if (bulkTagEntry != null) customizeMenu.add(bulkTagEntry);

		// Relations — bulk add only. Each selected non-completed goal
		// gets the same edge to/from the click-mode target. Cycle and
		// duplicate rejections fail open per source so a partial success
		// still applies. Bulk relation removal is not yet exposed —
		// per-goal edges remove via the single-item Relations submenu.
		List<Goal> relationSources = new ArrayList<>();
		for (Goal g : selectedGoals)
		{
			if (!g.isComplete()) relationSources.add(g);
		}
		if (!relationSources.isEmpty())
		{
			JMenu relationsMenu = new JMenu("Relations");

			LinkedHashSet<String> relationSourceIds = new LinkedHashSet<>();
			for (Goal g : relationSources) relationSourceIds.add(g.getId());

			JMenuItem requires = new JMenuItem("Requires…");
			requires.setToolTipText(
				"Click, then click another goal to mark as a requirement of every selected goal.");
			requires.addActionListener(e ->
				panel.enterRelationMode(relationSourceIds, /*sourceRequiresTarget=*/true));
			relationsMenu.add(requires);

			JMenuItem requiredBy = new JMenuItem("Required by…");
			requiredBy.setToolTipText(
				"Click, then click another goal that should require every selected goal.");
			requiredBy.addActionListener(e ->
				panel.enterRelationMode(relationSourceIds, /*sourceRequiresTarget=*/false));
			relationsMenu.add(requiredBy);

			// Compute intersections — an edge is "common" only if every
			// selected non-completed goal has it. Anything less would be
			// ambiguous: removing a requirement that only some selected
			// goals carry is better expressed via the single-item menu
			// on those specific goals.
			List<String> commonRequirements = commonEdges(api::getRequirements, relationSources);
			List<String> commonDependents = commonEdges(api::getDependents, relationSources);

			if (!commonRequirements.isEmpty() || !commonDependents.isEmpty())
			{
				relationsMenu.addSeparator();
			}

			if (!commonRequirements.isEmpty())
			{
				JMenu removeReqMenu = new JMenu("Remove Requirement");
				final List<Goal> finalSources = relationSources;
				for (String reqId : commonRequirements)
				{
					String label = reorderController.goalNameById(reqId);
					JMenuItem item = new JMenuItem(label);
					item.addActionListener(e -> {
						api.beginCompound("Remove requirement from " + finalSources.size() + " goals");
						try
						{
							for (Goal g : finalSources) api.removeRequirement(g.getId(), reqId);
						}
						finally { api.endCompound(); }
					});
					removeReqMenu.add(item);
				}
				relationsMenu.add(removeReqMenu);
			}

			if (!commonDependents.isEmpty())
			{
				JMenu removeDepMenu = new JMenu("Remove Dependent");
				final List<Goal> finalSources = relationSources;
				for (String depId : commonDependents)
				{
					String label = reorderController.goalNameById(depId);
					JMenuItem item = new JMenuItem(label);
					item.addActionListener(e -> {
						api.beginCompound("Remove dependent from " + finalSources.size() + " goals");
						try
						{
							for (Goal g : finalSources) api.removeRequirement(depId, g.getId());
						}
						finally { api.endCompound(); }
					});
					removeDepMenu.add(item);
				}
				relationsMenu.add(removeDepMenu);
			}

			customizeMenu.add(relationsMenu);
		}

		// Restore Defaults — show only if at least one selected goal is
		// overridden (tag drift OR color override).
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
			customizeMenu.add(restoreDefaults);
		}

		if (customizeMenu.getMenuComponentCount() > 0)
		{
			menu.add(customizeMenu);
		}

		// Move submenu — sibling of Customize (mirrors single-item menu).
		// Hidden when no selected goal is movable (all completed) or no
		// destination section exists.
		boolean anyMovable = false;
		for (Goal g : selectedGoals)
		{
			if (!g.isComplete()) { anyMovable = true; break; }
		}
		List<com.goalplanner.api.SectionView> allSections = api.queryAllSections();
		List<com.goalplanner.api.SectionView> destinations = new ArrayList<>();
		for (com.goalplanner.api.SectionView sv : allSections)
		{
			// Completed is auto-managed; bulk-move can't target it.
			if ("COMPLETED".equals(sv.kind)) continue;
			// Skip sections where every selected goal already lives.
			boolean allAlreadyHere = true;
			for (Goal g : selectedGoals)
			{
				if (!sv.id.equals(g.getSectionId())) { allAlreadyHere = false; break; }
			}
			if (allAlreadyHere) continue;
			destinations.add(sv);
		}

		// In-section bulk reordering — only when every selected goal is in
		// the same section and non-completed, and there's room to move
		// (section has more goals than the selection). All four operations
		// preserve the relative priority order of the selected block.
		boolean allInOneSection = !selectedGoals.isEmpty();
		String commonSectionId = selectedGoals.isEmpty() ? null : selectedGoals.get(0).getSectionId();
		boolean allActive = true;
		for (Goal g : selectedGoals)
		{
			if (!commonSectionId.equals(g.getSectionId())) allInOneSection = false;
			if (g.isComplete()) allActive = false;
		}
		boolean canInSectionMove = allInOneSection && allActive && commonSectionId != null;
		int sectionStart = -1;
		int sectionEnd = -1;
		if (canInSectionMove)
		{
			List<Goal> allGoals = goalStore.getGoals();
			for (int i = 0; i < allGoals.size(); i++)
			{
				if (commonSectionId.equals(allGoals.get(i).getSectionId()))
				{
					if (sectionStart == -1) sectionStart = i;
					sectionEnd = i;
				}
			}
			int sectionSize = (sectionStart == -1) ? 0 : (sectionEnd - sectionStart + 1);
			// "Same goals as the section" → every move is a no-op; hide
			// the in-section items entirely.
			if (sectionSize <= selectedGoals.size()) canInSectionMove = false;
		}

		if (anyMovable && (canInSectionMove || !destinations.isEmpty()))
		{
			JMenu moveMenu = new JMenu("Move");

			if (canInSectionMove)
			{
				final String moveSectionId = commonSectionId;
				final int moveSectionStart = sectionStart;
				final int moveSectionEnd = sectionEnd;

				JMenuItem moveToTop = new JMenuItem("Move to Top");
				moveToTop.addActionListener(e ->
					bulkMoveToTop(selectedGoals, moveSectionId, moveSectionStart));
				moveMenu.add(moveToTop);

				JMenuItem moveToBottom = new JMenuItem("Move to Bottom");
				moveToBottom.addActionListener(e ->
					bulkMoveToBottom(selectedGoals, moveSectionId,
						moveSectionStart, moveSectionEnd));
				moveMenu.add(moveToBottom);

				JMenuItem moveUp = new JMenuItem("Move Up");
				moveUp.addActionListener(e ->
					bulkMoveUp(selectedGoals, moveSectionId, moveSectionStart));
				moveMenu.add(moveUp);

				JMenuItem moveDown = new JMenuItem("Move Down");
				moveDown.addActionListener(e ->
					bulkMoveDown(selectedGoals, moveSectionId,
						moveSectionStart, moveSectionEnd));
				moveMenu.add(moveDown);
			}

			if (!destinations.isEmpty())
			{
				JMenu moveToSection = new JMenu("Move to Section");
				for (com.goalplanner.api.SectionView dest : destinations)
				{
					JMenuItem item = new JMenuItem(dest.name);
					item.addActionListener(e -> {
						LinkedHashSet<String> ids = new LinkedHashSet<>();
						for (Goal g : selectedGoals) ids.add(g.getId());
						api.bulkMoveGoalsToSection(ids, dest.id);
					});
					moveToSection.add(item);
				}
				moveMenu.add(moveToSection);
			}

			menu.add(moveMenu);
		}

		menu.addSeparator();

		// Remove
		JMenuItem removeItem = new JMenuItem("Remove Goals");
		removeItem.addActionListener(e -> {
			LinkedHashSet<String> ids = new LinkedHashSet<>();
			for (Goal g : selectedGoals) ids.add(g.getId());
			api.bulkRemoveGoals(ids);
		});
		menu.add(removeItem);

		return menu;
	}

	/**
	 * Attach a right-click context menu to a section header row.
	 * User sections get the full rename/move/delete/color menu;
	 * built-ins get Add Goal, Add Section, Select All, and Change Color.
	 */
	void attachSectionContextMenu(SectionHeaderRow row,
								  com.goalplanner.api.SectionView section,
								  List<com.goalplanner.api.SectionView> allSections)
	{
		// Compute this section's index within the user-section band so we can
		// gate the move-up / move-down items correctly.
		List<com.goalplanner.api.SectionView> userSections = new ArrayList<>();
		for (com.goalplanner.api.SectionView sv : allSections)
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

		// Add Goal submenu — Top of Section / Bottom of Section.
		// Hidden on Completed (auto-managed).
		if (!"COMPLETED".equals(section.kind))
		{
			JMenu addGoalMenu = new JMenu("Add Goal");
			JMenuItem addTop = new JMenuItem("At Top of Section");
			addTop.addActionListener(e -> {
				dialogFactory.pendingAddPositionInSection = 0;
				dialogFactory.showAddGoalDialog(section.id);
			});
			addGoalMenu.add(addTop);
			JMenuItem addBottom = new JMenuItem("At Bottom of Section");
			addBottom.addActionListener(e -> {
				dialogFactory.pendingAddPositionInSection = Integer.MAX_VALUE;
				dialogFactory.showAddGoalDialog(section.id);
			});
			addGoalMenu.add(addBottom);
			menu.add(addGoalMenu);
		}

		// Add Section submenu. User sections get Above/Below;
		// built-ins (Incomplete, Completed) get a single entry that creates
		// the new section at the end of the user-band.
		if (!section.builtIn)
		{
			JMenu addSectionMenu = new JMenu("Add Section");
			JMenuItem addSectionAbove = new JMenuItem("Above");
			addSectionAbove.addActionListener(e -> dialogFactory.showCreateSectionDialog(currentUserIndex));
			addSectionMenu.add(addSectionAbove);
			JMenuItem addSectionBelow = new JMenuItem("Below");
			addSectionBelow.addActionListener(e -> dialogFactory.showCreateSectionDialog(currentUserIndex + 1));
			addSectionMenu.add(addSectionBelow);
			menu.add(addSectionMenu);
		}
		else
		{
			JMenuItem addSection = new JMenuItem("Add Section");
			addSection.addActionListener(e -> dialogFactory.showCreateSectionDialog(-1));
			menu.add(addSection);
		}

		menu.addSeparator();

		// Select / Deselect All in Section — label flips when every goal in the
		// section is already selected. Computed against the current selection
		// snapshot at menu-build time.
		Set<String> currentSel = api.getSelectedGoalIds();
		List<String> sectionGoalIds = new ArrayList<>();
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
		changeColor.addActionListener(e -> dialogFactory.showSectionColorDialog(section));
		menu.add(changeColor);

		// User-section-only items: rename, move up/down, delete.
		if (!section.builtIn)
		{
			JMenuItem rename = new JMenuItem("Rename");
			rename.addActionListener(e -> dialogFactory.showRenameSectionDialog(section));
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
					panel,
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

	// --------------------------------------------------------------
	// Bulk in-section move helpers
	// --------------------------------------------------------------
	//
	// All four operations preserve the relative priority order of the
	// selected block. The "iteration index as min/max bound" trick
	// (target = max(currentRel - 1, i) for up; target = min(currentRel
	// + 1, sectionSize - 1 - i) for down) prevents one move from
	// displacing another selected goal — when the goal directly above
	// (or below) is also selected, the bound clamps the move out and
	// the relative order is preserved.

	/**
	 * Compute target positions UP-FRONT in section-relative space, then
	 * apply via positionGoalInSection. We use planned positions rather
	 * than re-reading goal.getPriority() between calls because earlier
	 * moves shift other goals' priorities and the math for "what should
	 * this goal's new position be" is much simpler before any moves.
	 */
	static List<Integer> planMoveUp(List<Integer> sectionRelativeAsc, int sectionSize)
	{
		List<Integer> targets = new ArrayList<>(sectionRelativeAsc.size());
		for (int i = 0; i < sectionRelativeAsc.size(); i++)
		{
			int currentRel = sectionRelativeAsc.get(i);
			targets.add(Math.max(currentRel - 1, i));
		}
		return targets;
	}

	static List<Integer> planMoveDown(List<Integer> sectionRelativeAsc, int sectionSize)
	{
		// Process desc by reversing the input. Iteration index i maps to
		// "this many already-processed selecteds are below us in the
		// final order", so the floor for our target is sectionSize-1-i.
		List<Integer> reversed = new ArrayList<>(sectionRelativeAsc);
		Collections.reverse(reversed);
		List<Integer> targetsDesc = new ArrayList<>(reversed.size());
		for (int i = 0; i < reversed.size(); i++)
		{
			int currentRel = reversed.get(i);
			targetsDesc.add(Math.min(currentRel + 1, sectionSize - 1 - i));
		}
		// Reverse back to ASC alignment.
		Collections.reverse(targetsDesc);
		return targetsDesc;
	}

	private void bulkMoveToTop(List<Goal> selected, String sectionId, int sectionStart)
	{
		List<Goal> sorted = new ArrayList<>(selected);
		sorted.sort(java.util.Comparator.comparingInt(Goal::getPriority));
		api.beginCompound("Move " + sorted.size() + " to top");
		try
		{
			for (int i = 0; i < sorted.size(); i++)
			{
				api.positionGoalInSection(sorted.get(i).getId(), sectionId, i);
			}
		}
		finally { api.endCompound(); }
	}

	private void bulkMoveToBottom(List<Goal> selected, String sectionId,
								  int sectionStart, int sectionEnd)
	{
		int sectionSize = sectionEnd - sectionStart + 1;
		List<Goal> sortedDesc = new ArrayList<>(selected);
		sortedDesc.sort(java.util.Comparator.comparingInt(Goal::getPriority).reversed());
		api.beginCompound("Move " + sortedDesc.size() + " to bottom");
		try
		{
			for (int i = 0; i < sortedDesc.size(); i++)
			{
				api.positionGoalInSection(sortedDesc.get(i).getId(), sectionId,
					sectionSize - 1 - i);
			}
		}
		finally { api.endCompound(); }
	}

	private void bulkMoveUp(List<Goal> selected, String sectionId, int sectionStart)
	{
		List<Goal> sorted = new ArrayList<>(selected);
		sorted.sort(java.util.Comparator.comparingInt(Goal::getPriority));
		List<Integer> currentRels = new ArrayList<>();
		for (Goal g : sorted) currentRels.add(g.getPriority() - sectionStart);
		List<Integer> targets = planMoveUp(currentRels, /*sectionSize=*/0);
		api.beginCompound("Move " + sorted.size() + " up");
		try
		{
			for (int i = 0; i < sorted.size(); i++)
			{
				int target = targets.get(i);
				if (target != currentRels.get(i))
				{
					api.positionGoalInSection(sorted.get(i).getId(), sectionId, target);
				}
			}
		}
		finally { api.endCompound(); }
	}

	private void bulkMoveDown(List<Goal> selected, String sectionId,
							  int sectionStart, int sectionEnd)
	{
		int sectionSize = sectionEnd - sectionStart + 1;
		List<Goal> sorted = new ArrayList<>(selected);
		sorted.sort(java.util.Comparator.comparingInt(Goal::getPriority));
		List<Integer> currentRels = new ArrayList<>();
		for (Goal g : sorted) currentRels.add(g.getPriority() - sectionStart);
		List<Integer> targets = planMoveDown(currentRels, sectionSize);
		// Process desc so the bottommost goal moves first — it can't be
		// blocked by a goal-being-moved sitting where it wants to land.
		api.beginCompound("Move " + sorted.size() + " down");
		try
		{
			for (int i = sorted.size() - 1; i >= 0; i--)
			{
				int target = targets.get(i);
				if (target != currentRels.get(i))
				{
					api.positionGoalInSection(sorted.get(i).getId(), sectionId, target);
				}
			}
		}
		finally { api.endCompound(); }
	}
}
