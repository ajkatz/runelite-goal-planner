package com.goaltracker.ui;

import com.goaltracker.api.GoalTrackerApiImpl;
import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalType;
import com.goaltracker.persistence.GoalStore;
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
	private final GoalTrackerApiImpl api;
	private final GoalStore goalStore;
	private final GoalPanel panel;
	private final GoalDialogFactory dialogFactory;
	private final GoalReorderController reorderController;

	GoalContextMenuBuilder(GoalTrackerApiImpl api,
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
				// Right-click exits relation-pick mode. The
				// user is clearly navigating away from the relation they
				// started; show the normal context menu of the clicked
				// card instead of stranding them in mode.
				if (panel.pendingRelationSourceId != null) panel.exitRelationMode();
				// Right-click does NOT touch the current selection. If the
				// clicked card is part of the existing multi-selection, show
				// the bulk menu so its actions apply to the whole set.
				// Otherwise show the single-item menu for the clicked card —
				// its actions only affect that one card, leaving any existing
				// selection intact so the user can right-click+Select to
				// build up a multi-select gradually.
				Set<String> sel = api.getSelectedGoalIds();
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

		// Reorder options are hidden in the Completed section (read-only ordering)
		// and gated on section bounds so they don't appear when there's nowhere to
		// move within the section. Move-to-Top/Bottom now stay inside the section.
		if (!goal.isComplete())
		{
			if (index > sectionStart)
			{
				JMenuItem moveFirst = new JMenuItem("Move to Top");
				moveFirst.addActionListener(e -> {
					reorderController.moveGoalTo(goal.getId(), sectionStart);
				});
				menu.add(moveFirst);
			}

			if (index < sectionEnd)
			{
				JMenuItem moveLast = new JMenuItem("Move to Bottom");
				moveLast.addActionListener(e -> {
					reorderController.moveGoalTo(goal.getId(), sectionEnd);
				});
				menu.add(moveLast);
			}
		}

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

			menu.add(addGoalMenu);
		}

		if (menu.getComponentCount() > 0)
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

		// Optional toggle
		{
			boolean isOptional = goal.isOptional();
			JMenuItem optionalItem = new JMenuItem(isOptional ? "Mark Required" : "Mark Optional");
			optionalItem.addActionListener(e -> api.setGoalOptional(goal.getId(), !isOptional));
			menu.add(optionalItem);
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
			menu.add(editName);

			JMenuItem editDesc = new JMenuItem("Change Description");
			editDesc.addActionListener(e -> {
				String input = JOptionPane.showInputDialog(panel, "New description:",
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
			changeGoalColor.addActionListener(e -> dialogFactory.showGoalColorDialog(goal));
			menu.add(changeGoalColor);
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

		// Quest Helper link — only shown for incomplete quest goals when
		// Quest Helper is available.
		if (goal.getType() == GoalType.QUEST && goal.getQuestName() != null
			&& !goal.isComplete() && panel.questHelperCallback != null
			&& panel.questHelperAvailable != null && panel.questHelperAvailable.get())
		{
			JMenuItem qhItem = new JMenuItem("Open in Quest Helper");
			qhItem.addActionListener(e -> panel.questHelperCallback.accept(goal.getQuestName()));
			menu.add(qhItem);
		}

		// Tag management — routes through the shared TagPickerDialog so the
		// single-item and bulk Add Tag flows stay in lockstep (category list,
		// SKILLING lock, freeform/dropdown switch).
		JMenuItem addTag = new JMenuItem("Add Tag");
		addTag.addActionListener(e -> {
			TagPickerDialog.Result picked = TagPickerDialog.show(panel, "Add Tag", api);
			if (picked != null)
			{
				api.addTagWithCategory(goal.getId(), picked.label, picked.category.name());
			}
		});
		// Completed goals are tag-frozen.
		if (!goal.isComplete())
		{
			menu.add(addTag);
		}

		// Removable tags: for CUSTOM goals, anything. For everything else, only
		// user-added tags (not in defaultTagIds). Dereference tag ids
		// through the store and operate on Tag entities.
		List<com.goaltracker.model.Tag> removableTags = new ArrayList<>();
		List<com.goaltracker.model.Tag> allGoalTags = new ArrayList<>();
		if (goal.getTagIds() != null && !goal.getTagIds().isEmpty())
		{
			List<String> defaults = goal.getDefaultTagIds() != null
				? goal.getDefaultTagIds() : Collections.emptyList();
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
					panel, "Select tag to remove:", "Remove Tag",
					JOptionPane.PLAIN_MESSAGE, null, tagNames, tagNames[0]
				);
				if (selected != null)
				{
					int idx = Arrays.asList(tagNames).indexOf(selected);
					if (idx >= 0)
					{
						api.removeTag(goal.getId(), removableTags.get(idx).getLabel());
					}
				}
			});
			menu.add(removeTag);
		}

		// Relations. "Requires..." and "Required by..." enter a
		// click-mode where the user clicks another goal to link. The Remove
		// submenus below are direct pick-to-remove lists of the current edges.
		{
			JMenuItem addRequirement = new JMenuItem("Requires\u2026");
			addRequirement.setToolTipText(
				"Click, then click another goal to mark it as a requirement of this one.");
			addRequirement.addActionListener(e ->
				panel.enterRelationMode(goal.getId(), /*sourceRequiresTarget=*/true));
			menu.add(addRequirement);

			JMenuItem addDependent = new JMenuItem("Required by\u2026");
			addDependent.setToolTipText(
				"Click, then click another goal that should require this one.");
			addDependent.addActionListener(e ->
				panel.enterRelationMode(goal.getId(), /*sourceRequiresTarget=*/false));
			menu.add(addDependent);

			// Remove requirement submenu — only when there's something to remove.
			List<String> currentRequirements = api.getRequirements(goal.getId());
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
				menu.add(removeReqMenu);
			}

			// Remove dependent submenu — only when this goal is depended-on.
			List<String> currentDependents = api.getDependents(goal.getId());
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
				menu.add(removeDepMenu);
			}
		}

		// Restore Defaults — gated on isGoalOverridden (tag drift
		// OR color override). Routes through the bulk API so the single-item
		// path resets BOTH tags and color in one shot.
		if (api.isGoalOverridden(goal.getId()))
		{
			JMenuItem restore = new JMenuItem("Restore Defaults");
			restore.addActionListener(e ->
				api.bulkRestoreDefaults(Collections.singleton(goal.getId())));
			menu.add(restore);
		}

		// "Move to section →" submenu — only for non-completed goals, only if there
		// is at least one valid destination section (Incomplete + user sections,
		// excluding the goal's current section, excluding Completed).
		if (!goal.isComplete())
		{
			List<com.goaltracker.api.SectionView> allSections = api.queryAllSections();
			List<com.goaltracker.api.SectionView> destinations = new ArrayList<>();
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
		remove.addActionListener(e -> api.removeGoal(goal.getId()));
		menu.add(remove);

		return menu;
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

		// 1. Move to Section — only if at least one selected goal is non-complete.
		// Completed goals are pinned to Completed and the API rejects the move,
		// so showing the option for an all-completed selection would be a no-op.
		boolean anyMovable = false;
		for (Goal g : selectedGoals)
		{
			if (!g.isComplete()) { anyMovable = true; break; }
		}
		List<com.goaltracker.api.SectionView> allSections = api.queryAllSections();
		List<com.goaltracker.api.SectionView> destinations = new ArrayList<>();
		for (com.goaltracker.api.SectionView sv : allSections)
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
		if (anyMovable && !destinations.isEmpty())
		{
			JMenu moveToSection = new JMenu("Move to Section");
			for (com.goaltracker.api.SectionView dest : destinations)
			{
				JMenuItem item = new JMenuItem(dest.name);
				item.addActionListener(e -> {
					LinkedHashSet<String> ids = new LinkedHashSet<>();
					for (Goal g : selectedGoals) ids.add(g.getId());
					api.bulkMoveGoalsToSection(ids, dest.id);
				});
				moveToSection.add(item);
			}
			menu.add(moveToSection);
		}

		// 2. Add Tag
		JMenuItem addTag = new JMenuItem("Add Tag");
		addTag.addActionListener(e -> dialogFactory.showBulkAddTagDialog(selectedGoals));
		menu.add(addTag);

		// 3. Change Color
		JMenuItem changeColor = new JMenuItem("Change Color");
		changeColor.addActionListener(e -> dialogFactory.showBulkChangeColorDialog(selectedGoals));
		menu.add(changeColor);

		// Bulk Remove Tag — show only if at least one selected
		// goal has a removable tag.
		List<com.goaltracker.api.GoalTrackerInternalApi.TagRemovalOption> removableOpts =
			api.getRemovableTagsForSelection(selectedIds);
		if (!removableOpts.isEmpty())
		{
			JMenuItem bulkRemoveTag = new JMenuItem("Remove Tag");
			bulkRemoveTag.addActionListener(e -> dialogFactory.showBulkRemoveTagDialog(selectedIds, removableOpts));
			menu.add(bulkRemoveTag);
		}

		// Bulk Restore Defaults — show only if at least one
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
				api.beginCompound("Mark " + selectedGoals.size() + " complete");
				try
				{
					for (Goal g : selectedGoals) api.markGoalComplete(g.getId());
				}
				finally { api.endCompound(); }
			});
			menu.add(markComplete);
		}

		menu.addSeparator();

		// 5. Remove
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
								  com.goaltracker.api.SectionView section,
								  List<com.goaltracker.api.SectionView> allSections)
	{
		// Compute this section's index within the user-section band so we can
		// gate the move-up / move-down items correctly.
		List<com.goaltracker.api.SectionView> userSections = new ArrayList<>();
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
}
