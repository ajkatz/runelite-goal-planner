package com.goaltracker.api;

import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalType;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Encapsulates goal-mutation methods extracted from {@link GoalTrackerApiImpl}.
 * Package-private — only {@link GoalTrackerApiImpl} instantiates and delegates to this.
 */
@Slf4j
class GoalMutationService
{
	/** Max level supported (1-99 normal, 100-126 virtual). */
	private static final int MAX_LEVEL = 126;

	/** Max experience per skill in OSRS. */
	private static final int MAX_XP = 200_000_000;

	private final GoalTrackerApiImpl api;

	GoalMutationService(GoalTrackerApiImpl api)
	{
		this.api = api;
	}

	boolean removeGoal(String goalId)
	{
		log.debug("API.public removeGoal(goalId={})", goalId);
		if (goalId == null) return false;
		Goal g = api.findGoal(goalId);
		if (g == null) return false;
		final com.goaltracker.persistence.GoalStore.RemoveGoalBypassSnapshot[] snapHolder =
			new com.goaltracker.persistence.GoalStore.RemoveGoalBypassSnapshot[1];
		final String name = g.getName();
		return api.executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				com.goaltracker.persistence.GoalStore.RemoveGoalBypassSnapshot snap =
					api.goalStore.removeGoalWithBypass(goalId);
				if (snap == null) return false;
				snapHolder[0] = snap;
				api.selectedGoalIds.remove(goalId);
				return true;
			}
			@Override public boolean revert()
			{
				com.goaltracker.persistence.GoalStore.RemoveGoalBypassSnapshot snap = snapHolder[0];
				if (snap == null) return false;
				if (api.findGoal(goalId) != null) return false;
				// 1. Remove the bypass edges we added
				for (String[] edge : snap.addedBypassEdges)
				{
					api.goalStore.removeRequirement(edge[0], edge[1]);
				}
				// 2. Re-insert the deleted goal at its original index. The
				//    snapshotted Goal carries its original requiredGoalIds so
				//    outgoing edges come back automatically.
				api.goalStore.insertGoalAt(snap.goal, snap.originalIndex);
				// 3. Restore incoming edges — re-add the deleted goal's id to
				//    each predecessor's requiredGoalIds.
				for (String predId : snap.predecessors)
				{
					api.goalStore.addRequirement(predId, goalId);
				}
				return true;
			}
			@Override public String getDescription() { return "Remove: " + name; }
		});
	}

	boolean editCustomGoal(String goalId, String newName, String newDescription)
	{
		log.debug("API.public editCustomGoal(goalId={}, newName={}, newDescription={})",
			goalId, newName, newDescription);
		if (goalId == null) return false;
		Goal g = api.findGoal(goalId);
		if (g == null || g.getType() != GoalType.CUSTOM) return false;
		final String prevName = g.getName();
		final String prevDesc = g.getDescription();
		final String resolvedName = newName != null && !newName.trim().isEmpty()
			? newName.trim() : prevName;
		final String resolvedDesc = newDescription != null ? newDescription.trim() : prevDesc;
		if (newName != null && newName.trim().isEmpty()) return false;
		if (resolvedName.equals(prevName) && resolvedDesc.equals(prevDesc)) return false;
		return api.executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				Goal cg = api.findGoal(goalId);
				if (cg == null) return false;
				cg.setName(resolvedName);
				cg.setDescription(resolvedDesc);
				api.goalStore.updateGoal(cg);
				return true;
			}
			@Override public boolean revert()
			{
				Goal cg = api.findGoal(goalId);
				if (cg == null) return false;
				cg.setName(prevName);
				cg.setDescription(prevDesc);
				api.goalStore.updateGoal(cg);
				return true;
			}
			@Override public String getDescription() { return "Edit: " + prevName; }
		});
	}

	boolean markGoalComplete(String goalId)
	{
		log.debug("API.public markGoalComplete(goalId={})", goalId);
		if (goalId == null) return false;
		Goal g = api.findGoal(goalId);
		if (g == null) return false;
		if (g.getType() != GoalType.CUSTOM && g.getType() != GoalType.ITEM_GRIND) return false;
		if (g.getStatus() == com.goaltracker.model.GoalStatus.COMPLETE) return false;
		final long prevCompletedAt = g.getCompletedAt();
		final String name = g.getName();
		return api.executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				return markCompleteInternal(goalId);
			}
			@Override public boolean revert()
			{
				return markIncompleteInternal(goalId, prevCompletedAt);
			}
			@Override public String getDescription() { return "Mark complete: " + name; }
		});
	}

	boolean markGoalIncomplete(String goalId)
	{
		log.debug("API.public markGoalIncomplete(goalId={})", goalId);
		if (goalId == null) return false;
		Goal g = api.findGoal(goalId);
		if (g == null) return false;
		if (g.getType() != GoalType.CUSTOM && g.getType() != GoalType.ITEM_GRIND) return false;
		if (g.getStatus() != com.goaltracker.model.GoalStatus.COMPLETE) return false;
		final long prevCompletedAt = g.getCompletedAt();
		final String name = g.getName();
		return api.executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				return markIncompleteInternal(goalId, 0L);
			}
			@Override public boolean revert()
			{
				return markCompleteInternalAt(goalId, prevCompletedAt);
			}
			@Override public String getDescription() { return "Mark incomplete: " + name; }
		});
	}

	boolean markCompleteInternal(String goalId)
	{
		return markCompleteInternalAt(goalId, System.currentTimeMillis());
	}

	boolean markCompleteInternalAt(String goalId, long completedAt)
	{
		Goal g = api.findGoal(goalId);
		if (g == null) return false;
		g.setCompletedAt(completedAt);
		g.setStatus(com.goaltracker.model.GoalStatus.COMPLETE);
		api.goalStore.updateGoal(g);
		api.goalStore.reconcileCompletedSection();
		return true;
	}

	boolean markIncompleteInternal(String goalId, long restoredCompletedAt)
	{
		Goal g = api.findGoal(goalId);
		if (g == null) return false;
		g.setCompletedAt(restoredCompletedAt);
		g.setStatus(com.goaltracker.model.GoalStatus.ACTIVE);
		api.goalStore.updateGoal(g);
		api.goalStore.reconcileCompletedSection();
		return true;
	}

	boolean changeTarget(String goalId, int newTarget)
	{
		log.debug("API.public changeTarget(goalId={}, newTarget={})", goalId, newTarget);
		if (goalId == null || newTarget < 1) return false;
		Goal g = api.findGoal(goalId);
		if (g == null) return false;
		if (g.getType() == GoalType.SKILL)
		{
			if (newTarget > MAX_XP) return false;
		}
		else if (g.getType() != GoalType.ITEM_GRIND)
		{
			return false; // CA/quest/diary targets are immutable
		}
		final int prevTarget = g.getTargetValue();
		final String prevName = g.getName();
		final String prevDescription = g.getDescription();
		final String label = g.getName();
		return api.executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply() { return changeTargetInternal(goalId, newTarget); }
			@Override public boolean revert()
			{
				Goal cg = api.findGoal(goalId);
				if (cg == null) return false;
				cg.setTargetValue(prevTarget);
				cg.setName(prevName);
				cg.setDescription(prevDescription);
				api.goalStore.updateGoal(cg);
				return true;
			}
			@Override public String getDescription() { return "Change target: " + label; }
		});
	}

	private boolean changeTargetInternal(String goalId, int newTarget)
	{
		Goal g = api.findGoal(goalId);
		if (g == null) return false;
		g.setTargetValue(newTarget);
		if (g.getType() == GoalType.SKILL && g.getSkillName() != null)
		{
			try
			{
				net.runelite.api.Skill skill = net.runelite.api.Skill.valueOf(g.getSkillName());
				int level = net.runelite.api.Experience.getLevelForXp(newTarget);
				g.setName(skill.getName() + " - Level " + level);
			}
			catch (IllegalArgumentException ignored) {}
		}
		else if (g.getType() == GoalType.ITEM_GRIND)
		{
			g.setDescription(com.goaltracker.util.FormatUtil.formatNumber(newTarget) + " total");
		}
		api.goalStore.updateGoal(g);
		return true;
	}

	boolean recordGoalProgress(String goalId, int newValue)
	{
		Goal g = api.findGoal(goalId);
		if (g == null) return false;
		if (g.getCurrentValue() == newValue) return false;

		g.setCurrentValue(newValue);

		boolean meetsTarget = g.meetsTarget();
		boolean wasComplete = g.isComplete();

		if (meetsTarget && !wasComplete)
		{
			g.setCompletedAt(System.currentTimeMillis());
			g.setStatus(com.goaltracker.model.GoalStatus.COMPLETE);
			log.info("API.internal recordGoalProgress: goal complete {} ({})",
				g.getId(), g.getName());
		}
		else if (!meetsTarget && wasComplete)
		{
			g.setCompletedAt(0);
			g.setStatus(com.goaltracker.model.GoalStatus.ACTIVE);
			log.info("API.internal recordGoalProgress: goal un-completed {} ({})",
				g.getId(), g.getName());
		}
		return true;
	}

	boolean isGoalOverridden(String goalId)
	{
		Goal g = api.findGoal(goalId);
		if (g == null) return false;
		if (g.getCustomColorRgb() >= 0) return true;
		List<String> defaults = g.getDefaultTagIds() != null ? g.getDefaultTagIds() : java.util.Collections.emptyList();
		List<String> current = g.getTagIds() != null ? g.getTagIds() : java.util.Collections.emptyList();
		return !new java.util.HashSet<>(current).equals(new java.util.HashSet<>(defaults));
	}

	int bulkRestoreDefaults(java.util.Set<String> goalIds)
	{
		log.debug("API.internal bulkRestoreDefaults({} goals)", goalIds == null ? 0 : goalIds.size());
		if (goalIds == null || goalIds.isEmpty()) return 0;
		final java.util.List<String[]> snapshots = new java.util.ArrayList<>();
		for (String goalId : goalIds)
		{
			Goal g = api.findGoal(goalId);
			if (g == null) continue;
			if (!isGoalOverridden(goalId)) continue;
			String prevTagsCsv = g.getTagIds() != null ? String.join(",", g.getTagIds()) : "";
			snapshots.add(new String[]{ goalId,
				String.valueOf(g.getCustomColorRgb()),
				prevTagsCsv });
		}
		if (snapshots.isEmpty()) return 0;
		boolean ok = api.executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				for (String[] snap : snapshots)
				{
					Goal g = api.findGoal(snap[0]);
					if (g == null) continue;
					List<String> defaults = g.getDefaultTagIds() != null ? g.getDefaultTagIds() : java.util.Collections.emptyList();
					g.setTagIds(new ArrayList<>(defaults));
					g.setCustomColorRgb(-1);
					api.goalStore.updateGoal(g);
				}
				return true;
			}
			@Override public boolean revert()
			{
				for (String[] snap : snapshots)
				{
					Goal g = api.findGoal(snap[0]);
					if (g == null) continue;
					g.setCustomColorRgb(Integer.parseInt(snap[1]));
					java.util.List<String> tags = snap[2].isEmpty()
						? new ArrayList<>() : new ArrayList<>(java.util.Arrays.asList(snap[2].split(",")));
					g.setTagIds(tags);
					api.goalStore.updateGoal(g);
				}
				return true;
			}
			@Override public String getDescription()
			{
				return "Restore defaults (" + snapshots.size() + " goals)";
			}
		});
		return ok ? snapshots.size() : 0;
	}

	int bulkRemoveGoals(java.util.Set<String> goalIds)
	{
		log.debug("API.internal bulkRemoveGoals({} goals)", goalIds == null ? 0 : goalIds.size());
		if (goalIds == null || goalIds.isEmpty()) return 0;
		final java.util.List<Goal> goalSnapshots = new ArrayList<>();
		final java.util.List<Integer> prioritySnapshots = new ArrayList<>();
		for (Goal g : api.goalStore.getGoals())
		{
			if (goalIds.contains(g.getId()))
			{
				goalSnapshots.add(g);
				prioritySnapshots.add(g.getPriority());
			}
		}
		if (goalSnapshots.isEmpty()) return 0;
		final java.util.Set<String> selectionSnapshot = new java.util.LinkedHashSet<>(api.selectedGoalIds);
		boolean ok = api.executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				for (Goal g : goalSnapshots)
				{
					api.goalStore.removeGoal(g.getId());
					api.selectedGoalIds.remove(g.getId());
				}
				return true;
			}
			@Override public boolean revert()
			{
				java.util.List<Integer> order = new ArrayList<>();
				for (int i = 0; i < goalSnapshots.size(); i++) order.add(i);
				order.sort((a, b) -> Integer.compare(prioritySnapshots.get(a), prioritySnapshots.get(b)));
				for (int idx : order)
				{
					api.goalStore.insertGoalAt(goalSnapshots.get(idx), prioritySnapshots.get(idx));
				}
				api.selectedGoalIds.addAll(selectionSnapshot);
				return true;
			}
			@Override public String getDescription()
			{
				return "Remove " + goalSnapshots.size() + " goals";
			}
		});
		return ok ? goalSnapshots.size() : 0;
	}

	int bulkMoveGoalsToSection(java.util.Set<String> goalIds, String targetSectionId)
	{
		log.debug("API.internal bulkMoveGoalsToSection({} goals → {})",
			goalIds == null ? 0 : goalIds.size(), targetSectionId);
		if (goalIds == null || goalIds.isEmpty() || targetSectionId == null) return 0;
		final java.util.List<String> affectedIds = new ArrayList<>();
		final java.util.List<String> prevSections = new ArrayList<>();
		final java.util.List<Integer> prevPriorities = new ArrayList<>();
		for (Goal g : api.goalStore.getGoals())
		{
			if (!goalIds.contains(g.getId())) continue;
			if (targetSectionId.equals(g.getSectionId())) continue;
			affectedIds.add(g.getId());
			prevSections.add(g.getSectionId());
			prevPriorities.add(g.getPriority());
		}
		if (affectedIds.isEmpty()) return 0;
		boolean ok = api.executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				for (String gid : affectedIds) api.sectionService.moveGoalToSectionInternal(gid, targetSectionId);
				return true;
			}
			@Override public boolean revert()
			{
				for (int i = 0; i < affectedIds.size(); i++)
				{
					String gid = affectedIds.get(i);
					api.goalStore.moveGoalToSection(gid, prevSections.get(i));
					Goal g = api.findGoal(gid);
					if (g != null) g.setPriority(prevPriorities.get(i));
				}
				api.goalStore.normalizeOrder();
				return true;
			}
			@Override public String getDescription()
			{
				return "Move " + affectedIds.size() + " goals";
			}
		});
		return ok ? affectedIds.size() : 0;
	}

	void removeAllGoals()
	{
		log.debug("API.internal removeAllGoals()");
		// Only remove incomplete goals — completed goals are preserved.
		final java.util.List<Goal> toRemove = new ArrayList<>();
		for (Goal g : api.goalStore.getGoals())
		{
			if (g.getStatus() != com.goaltracker.model.GoalStatus.COMPLETE)
			{
				toRemove.add(g);
			}
		}
		if (toRemove.isEmpty()) return;
		final java.util.Set<String> removedIds = new java.util.LinkedHashSet<>();
		for (Goal g : toRemove) removedIds.add(g.getId());
		final java.util.Set<String> selectionSnapshot = new java.util.LinkedHashSet<>(api.selectedGoalIds);
		api.executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				for (Goal g : toRemove)
				{
					api.goalStore.removeGoal(g.getId());
				}
				api.selectedGoalIds.removeAll(removedIds);
				return true;
			}
			@Override public boolean revert()
			{
				for (Goal g : toRemove)
				{
					if (api.findGoal(g.getId()) == null) api.goalStore.addGoal(g);
				}
				api.goalStore.normalizeOrder();
				api.selectedGoalIds.addAll(selectionSnapshot);
				return true;
			}
			@Override public String getDescription() { return "Remove all incomplete goals (" + toRemove.size() + ")"; }
		});
	}

	boolean moveGoal(String goalId, int newGlobalIndex)
	{
		log.debug("API.internal moveGoal(goalId={}, newGlobalIndex={})", goalId, newGlobalIndex);
		if (goalId == null) return false;
		List<Goal> goals = api.goalStore.getGoals();
		int currentIndex = -1;
		for (int i = 0; i < goals.size(); i++)
		{
			if (goals.get(i).getId().equals(goalId))
			{
				currentIndex = i;
				break;
			}
		}
		if (currentIndex < 0) return false;
		if (newGlobalIndex < 0 || newGlobalIndex >= goals.size()) return false;
		if (currentIndex == newGlobalIndex) return false;
		String sourceSectionId = goals.get(currentIndex).getSectionId();
		String targetSectionId = goals.get(newGlobalIndex).getSectionId();
		if (sourceSectionId == null || !sourceSectionId.equals(targetSectionId)) return false;

		final int snapshotFromIndex = currentIndex;
		final String name = goals.get(currentIndex).getName();
		return api.executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				List<Goal> gs = api.goalStore.getGoals();
				int from = -1;
				for (int i = 0; i < gs.size(); i++)
				{
					if (gs.get(i).getId().equals(goalId)) { from = i; break; }
				}
				if (from < 0 || newGlobalIndex >= gs.size()) return false;
				int delta = Math.abs(newGlobalIndex - from);
				if (delta == 1) api.reorderingService.moveGoal(from, newGlobalIndex);
				else api.reorderingService.moveGoalTo(from, newGlobalIndex);
				return true;
			}
			@Override public boolean revert()
			{
				List<Goal> gs = api.goalStore.getGoals();
				int currentPos = -1;
				for (int i = 0; i < gs.size(); i++)
				{
					if (gs.get(i).getId().equals(goalId)) { currentPos = i; break; }
				}
				if (currentPos < 0) return false;
				if (currentPos == snapshotFromIndex) return true;
				int delta = Math.abs(snapshotFromIndex - currentPos);
				if (delta == 1) api.reorderingService.moveGoal(currentPos, snapshotFromIndex);
				else api.reorderingService.moveGoalTo(currentPos, snapshotFromIndex);
				return true;
			}
			@Override public String getDescription() { return "Reorder: " + name; }
		});
	}

	boolean positionGoalInSection(String goalId, String sectionId, int positionInSection)
	{
		log.debug("API.internal positionGoalInSection(goalId={}, sectionId={}, pos={})",
			goalId, sectionId, positionInSection);
		if (goalId == null || sectionId == null) return false;
		Goal g = api.findGoal(goalId);
		if (g == null) return false;
		final String prevSectionId = g.getSectionId();
		final int prevPriority = g.getPriority();
		final String name = g.getName();
		return api.executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				return positionGoalInSectionInternal(goalId, sectionId, positionInSection);
			}
			@Override public boolean revert()
			{
				Goal cg = api.findGoal(goalId);
				if (cg == null) return false;
				if (!prevSectionId.equals(cg.getSectionId()))
				{
					api.goalStore.moveGoalToSection(goalId, prevSectionId);
				}
				cg.setPriority(prevPriority);
				api.goalStore.normalizeOrder();
				return true;
			}
			@Override public String getDescription() { return "Reposition: " + name; }
		});
	}

	private boolean positionGoalInSectionInternal(String goalId, String sectionId, int positionInSection)
	{
		Goal g = api.findGoal(goalId);
		if (g == null) return false;
		boolean changed = false;
		if (!sectionId.equals(g.getSectionId()))
		{
			if (api.goalStore.moveGoalToSection(goalId, sectionId)) changed = true;
		}
		api.goalStore.normalizeOrder();
		List<Goal> goals = api.goalStore.getGoals();
		List<Integer> sectionIndices = new ArrayList<>();
		int sourceIdx = -1;
		for (int i = 0; i < goals.size(); i++)
		{
			if (sectionId.equals(goals.get(i).getSectionId()))
			{
				sectionIndices.add(i);
				if (goalId.equals(goals.get(i).getId())) sourceIdx = i;
			}
		}
		if (sourceIdx < 0) return changed;
		int sectionSize = sectionIndices.size();
		int clampedPos = Math.max(0, Math.min(positionInSection, sectionSize - 1));
		int targetGlobal = sectionIndices.get(clampedPos);
		if (sourceIdx != targetGlobal)
		{
			api.reorderingService.moveGoalTo(sourceIdx, targetGlobal);
			changed = true;
		}
		return changed;
	}
}
