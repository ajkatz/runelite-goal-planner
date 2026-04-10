package com.goaltracker.api;

import com.goaltracker.model.Goal;
import com.goaltracker.model.Section;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Encapsulates section-management methods extracted from {@link GoalTrackerApiImpl}.
 * Package-private — only {@link GoalTrackerApiImpl} instantiates and delegates to this.
 */
@Slf4j
class SectionService
{
	private final GoalTrackerApiImpl api;

	SectionService(GoalTrackerApiImpl api)
	{
		this.api = api;
	}

	String createSection(String name)
	{
		log.debug("API.internal createSection(name={})", name);
		Section created = api.goalStore.createUserSection(name);
		if (created == null) return null;
		final String sectionId = created.getId();
		final String sectionName = created.getName();
		// Already created. Wrap in a Command for the undo path.
		api.executeCommand(new com.goaltracker.command.Command()
		{
			private boolean firstApply = true;
			@Override public boolean apply()
			{
				if (firstApply) { firstApply = false; return true; }
				if (api.goalStore.findSection(sectionId) != null) return false;
				api.goalStore.recreateUserSection(sectionId, sectionName);
				return true;
			}
			@Override public boolean revert()
			{
				return api.goalStore.deleteUserSection(sectionId);
			}
			@Override public String getDescription() { return "Add section: " + sectionName; }
		});
		return sectionId;
	}

	boolean renameSection(String sectionId, String newName)
	{
		log.debug("API.internal renameSection(sectionId={}, newName={})", sectionId, newName);
		Section sec = api.goalStore.findSection(sectionId);
		if (sec == null) return false;
		final String prevName = sec.getName();
		final String resolved = newName != null ? newName.trim() : "";
		if (resolved.isEmpty() || resolved.equals(prevName)) return false;
		return api.executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply() { return api.goalStore.renameUserSection(sectionId, resolved); }
			@Override public boolean revert() { return api.goalStore.renameUserSection(sectionId, prevName); }
			@Override public String getDescription() { return "Rename section: " + prevName + " → " + resolved; }
		});
	}

	boolean deleteSection(String sectionId)
	{
		log.debug("API.internal deleteSection(sectionId={})", sectionId);
		Section sec = api.goalStore.findSection(sectionId);
		if (sec == null) return false;
		final String name = sec.getName();
		final int order = sec.getOrder();
		final int colorRgb = sec.getColorRgb();
		// Snapshot which goals were in this section so revert can move them back.
		final java.util.List<String> displacedGoalIds = new ArrayList<>();
		for (Goal g : api.goalStore.getGoals())
		{
			if (sectionId.equals(g.getSectionId())) displacedGoalIds.add(g.getId());
		}
		return api.executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply() { return api.goalStore.deleteUserSection(sectionId); }
			@Override public boolean revert()
			{
				api.goalStore.recreateUserSection(sectionId, name);
				Section restored = api.goalStore.findSection(sectionId);
				if (restored != null)
				{
					restored.setOrder(order);
					restored.setColorRgb(colorRgb);
				}
				for (String gid : displacedGoalIds)
				{
					api.goalStore.moveGoalToSection(gid, sectionId);
				}
				api.goalStore.normalizeOrder();
				return true;
			}
			@Override public String getDescription() { return "Delete section: " + name; }
		});
	}

	boolean reorderSection(String sectionId, int newUserIndex)
	{
		log.debug("API.internal reorderSection(sectionId={}, newUserIndex={})", sectionId, newUserIndex);
		Section sec = api.goalStore.findSection(sectionId);
		if (sec == null) return false;
		// Compute current user index
		int prevUserIndex = -1;
		int idx = 0;
		for (Section s : api.goalStore.getSections())
		{
			if (s.getOrder() < Section.ORDER_INCOMPLETE) // skip built-ins (high order)
			{
				if (s.getId().equals(sectionId)) { prevUserIndex = idx; break; }
				idx++;
			}
		}
		final int snapshotPrev = prevUserIndex;
		final String name = sec.getName();
		return api.executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply() { return api.goalStore.reorderUserSection(sectionId, newUserIndex); }
			@Override public boolean revert()
			{
				if (snapshotPrev < 0) return false;
				return api.goalStore.reorderUserSection(sectionId, snapshotPrev);
			}
			@Override public String getDescription() { return "Reorder section: " + name; }
		});
	}

	boolean moveGoalToSection(String goalId, String sectionId)
	{
		log.debug("API.internal moveGoalToSection(goalId={}, sectionId={})", goalId, sectionId);
		Goal current = api.findGoal(goalId);
		if (current == null) return false;
		if (sectionId == null) return false;
		if (sectionId.equals(current.getSectionId())) return false;
		final String prevSectionId = current.getSectionId();
		final int prevPriority = current.getPriority();
		final String name = current.getName();
		return api.executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply() { return moveGoalToSectionInternal(goalId, sectionId); }
			@Override public boolean revert()
			{
				Goal g = api.findGoal(goalId);
				if (g == null) return false;
				api.goalStore.moveGoalToSection(goalId, prevSectionId);
				g.setPriority(prevPriority);
				api.goalStore.normalizeOrder();
				return true;
			}
			@Override public String getDescription() { return "Move: " + name; }
		});
	}

	boolean moveGoalToSectionInternal(String goalId, String sectionId)
	{
		boolean moved = api.goalStore.moveGoalToSection(goalId, sectionId);
		if (moved)
		{
			Goal g = api.findGoal(goalId);
			if (g != null && g.getType() == com.goaltracker.model.GoalType.SKILL)
			{
				api.reorderingService.enforceSkillOrderingInSection(sectionId);
			}
		}
		return moved;
	}

	int removeAllUserSections()
	{
		log.debug("API.internal removeAllUserSections()");
		final java.util.List<Section> sectionSnapshots = new ArrayList<>();
		final java.util.Map<String, java.util.List<String>> goalsBySection = new java.util.HashMap<>();
		for (Section s : api.goalStore.getSections())
		{
			if (s.getBuiltInKind() == null)
			{
				sectionSnapshots.add(s);
				java.util.List<String> ids = new ArrayList<>();
				for (Goal g : api.goalStore.getGoals())
				{
					if (s.getId().equals(g.getSectionId())) ids.add(g.getId());
				}
				goalsBySection.put(s.getId(), ids);
			}
		}
		if (sectionSnapshots.isEmpty()) return 0;
		boolean ok = api.executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply() { return api.goalStore.removeAllUserSections() > 0; }
			@Override public boolean revert()
			{
				for (Section s : sectionSnapshots)
				{
					api.goalStore.recreateUserSection(s.getId(), s.getName());
					Section restored = api.goalStore.findSection(s.getId());
					if (restored != null)
					{
						restored.setOrder(s.getOrder());
						restored.setColorRgb(s.getColorRgb());
					}
					for (String gid : goalsBySection.get(s.getId()))
					{
						api.goalStore.moveGoalToSection(gid, s.getId());
					}
				}
				api.goalStore.normalizeOrder();
				return true;
			}
			@Override public String getDescription() { return "Remove all sections (" + sectionSnapshots.size() + ")"; }
		});
		return ok ? sectionSnapshots.size() : 0;
	}

	boolean setSectionCollapsed(String sectionId, boolean collapsed)
	{
		log.debug("API.internal setSectionCollapsed(sectionId={}, collapsed={})", sectionId, collapsed);
		if (sectionId == null) return false;
		for (Section s : api.goalStore.getSections())
		{
			if (sectionId.equals(s.getId()))
			{
				if (s.isCollapsed() == collapsed) return false;
				s.setCollapsed(collapsed);
				api.goalStore.save();
				api.onGoalsChanged.run();
				return true;
			}
		}
		return false;
	}

	boolean toggleSectionCollapsed(String sectionId)
	{
		log.debug("API.internal toggleSectionCollapsed(sectionId={})", sectionId);
		if (sectionId == null) return false;
		for (Section s : api.goalStore.getSections())
		{
			if (sectionId.equals(s.getId()))
			{
				boolean next = !s.isCollapsed();
				s.setCollapsed(next);
				api.goalStore.save();
				api.onGoalsChanged.run();
				return next;
			}
		}
		return false;
	}

	boolean setSectionColor(String sectionId, int colorRgb)
	{
		log.debug("API.internal setSectionColor(sectionId={}, colorRgb={})", sectionId, colorRgb);
		Section section = api.goalStore.findSection(sectionId);
		if (section == null) return false;
		int normalized = colorRgb < 0 ? -1 : (colorRgb & 0xFFFFFF);
		if (section.getColorRgb() == normalized) return false;
		final int prevColor = section.getColorRgb();
		final String name = section.getName();
		return api.executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				Section s = api.goalStore.findSection(sectionId);
				if (s == null) return false;
				s.setColorRgb(normalized);
				api.goalStore.save();
				return true;
			}
			@Override public boolean revert()
			{
				Section s = api.goalStore.findSection(sectionId);
				if (s == null) return false;
				s.setColorRgb(prevColor);
				api.goalStore.save();
				return true;
			}
			@Override public String getDescription() { return "Recolor section: " + name; }
		});
	}

	boolean setGoalColor(String goalId, int colorRgb)
	{
		log.debug("API.internal setGoalColor(goalId={}, colorRgb={})", goalId, colorRgb);
		Goal g = api.findGoal(goalId);
		if (g == null) return false;
		int normalized = colorRgb < 0 ? -1 : (colorRgb & 0xFFFFFF);
		if (g.getCustomColorRgb() == normalized) return false;
		final int previousColor = g.getCustomColorRgb();
		final String name = g.getName();
		return api.executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply() { return setGoalColorInternal(goalId, normalized); }
			@Override public boolean revert() { return setGoalColorInternal(goalId, previousColor); }
			@Override public String getDescription() { return "Recolor: " + name; }
		});
	}

	boolean setGoalColorInternal(String goalId, int normalized)
	{
		Goal g = api.findGoal(goalId);
		if (g == null) return false;
		g.setCustomColorRgb(normalized);
		api.goalStore.save();
		return true;
	}
}
