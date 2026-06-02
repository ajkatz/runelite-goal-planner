package com.goalplanner.api;

import com.goalplanner.model.Goal;
import com.goalplanner.model.Section;
import com.goalplanner.share.ShareBundle;
import com.goalplanner.share.ShareMapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Builds a {@link ShareBundle} from goals currently in the store (export side).
 * Thin wrapper over {@code ShareMapper} that gathers the goals for a section (or
 * an explicit id set) and supplies the tag lookup. The encode-to-string,
 * clipboard, and party-send steps live in the UI/plugin layer.
 */
class ShareExportService
{
	private final GoalPlannerApiImpl api;

	ShareExportService(GoalPlannerApiImpl api)
	{
		this.api = api;
	}

	/** Bundle of every goal in the given section, or null if no such section. */
	ShareBundle exportSection(String sectionId, String sharedBy)
	{
		Section section = api.goalStore.findSection(sectionId);
		if (section == null)
		{
			return null;
		}
		List<Goal> goals = new ArrayList<>();
		for (Goal g : api.goalStore.getGoals())
		{
			if (g != null && sectionId.equals(g.getSectionId()))
			{
				goals.add(g);
			}
		}
		return ShareMapper.toBundle(
			ShareBundle.Kind.SECTION, section.getName(), section.getColorRgb(),
			goals, api.goalStore::findTag, sharedBy);
	}

	/** Bundle of the given goals (in store order), or null if none resolve. */
	ShareBundle exportGoals(List<String> goalIds, String sharedBy)
	{
		if (goalIds == null || goalIds.isEmpty())
		{
			return null;
		}
		Set<String> wanted = new HashSet<>(goalIds);
		List<Goal> goals = new ArrayList<>();
		for (Goal g : api.goalStore.getGoals())
		{
			if (g != null && wanted.contains(g.getId()))
			{
				goals.add(g);
			}
		}
		if (goals.isEmpty())
		{
			return null;
		}
		return ShareMapper.toBundle(
			ShareBundle.Kind.GOALS, null, -1, goals, api.goalStore::findTag, sharedBy);
	}
}
