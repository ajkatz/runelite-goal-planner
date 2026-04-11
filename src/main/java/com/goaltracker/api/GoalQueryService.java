package com.goaltracker.api;

import com.goaltracker.model.AccountMetric;
import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalType;
import com.goaltracker.model.Section;
import com.goaltracker.model.Tag;
import com.goaltracker.model.TagCategory;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Experience;

/**
 * Encapsulates goal/section query methods extracted from {@link GoalTrackerApiImpl}.
 * Package-private — only {@link GoalTrackerApiImpl} instantiates and delegates to this.
 */
@Slf4j
class GoalQueryService
{
	/** Neutral default section header color (matches SectionHeaderRow BORDER_COLOR). */
	static final int SECTION_DEFAULT_COLOR_RGB = (60 << 16) | (60 << 8) | 60;

	private final GoalTrackerApiImpl api;

	GoalQueryService(GoalTrackerApiImpl api)
	{
		this.api = api;
	}

	List<GoalView> queryAllGoals()
	{
		log.debug("API.public queryAllGoals()");
		// Ensure the flat list is in canonical (section.order, priority) order before snapshot.
		api.goalStore.normalizeOrder();
		List<Goal> source = api.goalStore.getGoals();
		List<GoalView> out = new ArrayList<>(source.size());
		for (Goal g : source)
		{
			out.add(toGoalView(g));
		}
		return out;
	}

	List<GoalView> searchGoals(String query)
	{
		log.debug("API.internal searchGoals(query={})", query);
		List<GoalView> all = queryAllGoals();
		if (query == null) return all;
		String needle = query.trim().toLowerCase();
		if (needle.isEmpty()) return all;

		// Phase 1: find direct matches.
		java.util.Set<String> matchedIds = new java.util.LinkedHashSet<>();
		java.util.Map<String, GoalView> viewById = new java.util.LinkedHashMap<>();
		for (GoalView gv : all)
		{
			viewById.put(gv.id, gv);
			if (matchesSearch(gv, needle)) matchedIds.add(gv.id);
		}

		// Phase 2: expand to include the full relation tree (both directions)
		// for every direct match. BFS from each matched goal.
		java.util.Set<String> expanded = new java.util.LinkedHashSet<>(matchedIds);
		java.util.ArrayDeque<String> queue = new java.util.ArrayDeque<>(matchedIds);
		while (!queue.isEmpty())
		{
			String id = queue.poll();
			Goal g = api.goalStore.findGoalById(id);
			if (g == null) continue;
			// Walk outgoing edges (requirements)
			if (g.getRequiredGoalIds() != null)
			{
				for (String reqId : g.getRequiredGoalIds())
				{
					if (expanded.add(reqId)) queue.add(reqId);
				}
			}
			// Walk incoming edges (dependents)
			for (String depId : api.goalStore.getDependents(id))
			{
				if (expanded.add(depId)) queue.add(depId);
			}
		}

		// Phase 3: return all expanded goals in canonical order.
		List<GoalView> out = new ArrayList<>();
		for (GoalView gv : all)
		{
			if (expanded.contains(gv.id)) out.add(gv);
		}
		return out;
	}

	/**
	 * Pure match check used by {@link #searchGoals(String)}. Pulled out for
	 * unit-testability and to keep the loop body trivial. The needle is
	 * pre-lowercased and pre-trimmed by the caller.
	 */
	private boolean matchesSearch(GoalView gv, String needle)
	{
		if (gv.name != null && gv.name.toLowerCase().contains(needle)) return true;
		if (gv.description != null && gv.description.toLowerCase().contains(needle)) return true;
		// GoalType display name (e.g. "Combat Achievement", "Skill")
		try
		{
			String typeDisplay = com.goaltracker.model.GoalType.valueOf(gv.type).getDisplayName();
			if (typeDisplay.toLowerCase().contains(needle)) return true;
		}
		catch (IllegalArgumentException ignored) {}
		// Section title
		if (gv.sectionId != null)
		{
			com.goaltracker.model.Section sec = api.goalStore.findSection(gv.sectionId);
			if (sec != null && sec.getName() != null
				&& sec.getName().toLowerCase().contains(needle)) return true;
		}
		// Tags: labels + category display names. defaultTags + customTags cover all.
		if (matchesAnyTag(gv.defaultTags, needle)) return true;
		if (matchesAnyTag(gv.customTags, needle)) return true;
		return false;
	}

	private boolean matchesAnyTag(List<TagView> tags, String needle)
	{
		if (tags == null) return false;
		for (TagView t : tags)
		{
			if (t.label != null && t.label.toLowerCase().contains(needle)) return true;
			if (t.category != null)
			{
				try
				{
					String catDisplay = TagCategory.valueOf(t.category).getDisplayName();
					if (catDisplay.toLowerCase().contains(needle)) return true;
				}
				catch (IllegalArgumentException ignored) {}
			}
		}
		return false;
	}

	List<SectionView> queryAllSections()
	{
		log.debug("API.public queryAllSections()");
		List<Section> source = new ArrayList<>(api.goalStore.getSections());
		source.sort(java.util.Comparator.comparingInt(Section::getOrder));
		List<SectionView> out = new ArrayList<>(source.size());
		for (Section s : source)
		{
			out.add(toSectionView(s));
		}
		return out;
	}

	GoalView toGoalView(Goal g)
	{
		GoalView v = new GoalView();
		v.id = g.getId();
		v.type = g.getType().name();
		v.name = g.getName();
		v.description = g.getDescription();
		v.currentValue = g.getCurrentValue();
		v.targetValue = g.getTargetValue();
		v.completedAt = g.getCompletedAt();
		v.sectionId = g.getSectionId();
		v.spriteId = g.getSpriteId();
		v.selected = api.selectedGoalIds.contains(g.getId());
		v.optional = g.isOptional();

		// Background color: type default + optional user override. DTO carries both
		// so consumers can show "reset to default" affordances with the right preview.
		java.awt.Color typeC = g.getType().getColor();
		int typeRgb = (typeC.getRed() << 16) | (typeC.getGreen() << 8) | typeC.getBlue();
		v.defaultBackgroundColorRgb = typeRgb;
		if (g.getCustomColorRgb() >= 0)
		{
			v.backgroundColorRgb = g.getCustomColorRgb();
			v.backgroundColorOverridden = true;
		}
		else
		{
			v.backgroundColorRgb = typeRgb;
			v.backgroundColorOverridden = false;
		}

		// Tag splitting: defaultTagIds is the snapshot from creation; the rest of
		// tagIds are user-added. Each id is dereferenced via the tag store.
		List<String> defaultIds = g.getDefaultTagIds() != null ? g.getDefaultTagIds() : java.util.Collections.emptyList();
		List<String> allIds = g.getTagIds() != null ? g.getTagIds() : java.util.Collections.emptyList();

		v.defaultTags = new ArrayList<>();
		for (String id : defaultIds)
		{
			Tag tag = api.goalStore.findTag(id);
			if (tag != null) v.defaultTags.add(api.tagService.toTagView(tag));
		}

		v.customTags = new ArrayList<>();
		for (String id : allIds)
		{
			if (defaultIds.contains(id)) continue;
			Tag tag = api.goalStore.findTag(id);
			if (tag != null) v.customTags.add(api.tagService.toTagView(tag));
		}

		// Resolve relations for the card hover tooltip.
		// Skill-chain edges (same skill, different level) are internal
		// bookkeeping and excluded from the display lists.
		v.requiresNames = new ArrayList<>();
		if (g.getRequiredGoalIds() != null)
		{
			for (String reqId : g.getRequiredGoalIds())
			{
				Goal req = api.findGoal(reqId);
				if (req == null || req.getName() == null) continue;
				if (isSkillChainEdge(g, req)) continue;
				v.requiresNames.add(toRelationView(req));
			}
		}
		v.requiredByNames = new ArrayList<>();
		for (Goal other : api.goalStore.getGoals())
		{
			if (other.getId().equals(g.getId())) continue;
			if (other.getRequiredGoalIds() != null
				&& other.getRequiredGoalIds().contains(g.getId())
				&& other.getName() != null)
			{
				if (isSkillChainEdge(other, g)) continue;
				v.requiredByNames.add(toRelationView(other));
			}
		}

		// Type-specific attributes
		v.attributes = new java.util.HashMap<>();
		switch (g.getType())
		{
			case SKILL:
				if (g.getSkillName() != null) v.attributes.put("skillName", g.getSkillName());
				break;
			case QUEST:
				if (g.getQuestName() != null) v.attributes.put("questName", g.getQuestName());
				if (g.getTooltip() != null) v.attributes.put("tooltip", g.getTooltip());
				break;
			case DIARY:
				v.attributes.put("area", g.getName());
				if (g.getDescription() != null && g.getDescription().endsWith(" Achievement Diary"))
				{
					String tier = g.getDescription().substring(
						0, g.getDescription().length() - " Achievement Diary".length()).toUpperCase();
					v.attributes.put("tier", tier);
				}
				if (g.getVarbitId() > 0) v.attributes.put("varbitId", g.getVarbitId());
				if (g.getTooltip() != null) v.attributes.put("tooltip", g.getTooltip());
				break;
			case ITEM_GRIND:
				if (g.getItemId() > 0) v.attributes.put("itemId", g.getItemId());
				break;
			case COMBAT_ACHIEVEMENT:
				if (g.getCaTaskId() >= 0) v.attributes.put("caTaskId", g.getCaTaskId());
				if (g.getDescription() != null && g.getDescription().endsWith(" Combat Achievement"))
				{
					String tier = g.getDescription().substring(
						0, g.getDescription().length() - " Combat Achievement".length()).toUpperCase();
					v.attributes.put("tier", tier);
				}
				// Monster name lives in the BOSS/RAID tag, not its own field
				for (String id : allIds)
				{
					Tag tag = api.goalStore.findTag(id);
					if (tag != null && (tag.getCategory() == TagCategory.BOSS || tag.getCategory() == TagCategory.RAID))
					{
						v.attributes.put("monster", tag.getLabel());
						break;
					}
				}
				if (g.getTooltip() != null) v.attributes.put("tooltip", g.getTooltip());
				break;
			case ACCOUNT:
				if (g.getAccountMetric() != null)
				{
					v.attributes.put("accountMetric", g.getAccountMetric());
					try
					{
						AccountMetric m = AccountMetric.valueOf(g.getAccountMetric());
						String resolvedIconKey = m.resolveIconKeyForTarget(g.getTargetValue());
						if (resolvedIconKey != null)
						{
							v.attributes.put("iconKey", resolvedIconKey);
						}
					}
					catch (IllegalArgumentException ignored) {}
				}
				break;
			case CUSTOM:
				if (g.getItemId() > 0) v.attributes.put("itemId", g.getItemId());
				break;
			default:
				break;
		}

		return v;
	}

	/**
	 * True when both goals are implicit chain links that should not
	 * clutter the tooltip: same-skill edges (40 Prayer → 60 Prayer)
	 * or same-metric account edges (96 Combat → 126 Combat).
	 */
	static boolean isSkillChainEdge(Goal a, Goal b)
	{
		if (a.getType() == GoalType.SKILL && b.getType() == GoalType.SKILL)
		{
			return a.getSkillName() != null && a.getSkillName().equals(b.getSkillName());
		}
		if (a.getType() == GoalType.ACCOUNT && b.getType() == GoalType.ACCOUNT)
		{
			return a.getAccountMetric() != null && a.getAccountMetric().equals(b.getAccountMetric());
		}
		return false;
	}

	static GoalView.RelationView toRelationView(Goal g)
	{
		String skillName = null;
		int targetLevel = 0;
		if (g.getType() == GoalType.SKILL && g.getSkillName() != null)
		{
			skillName = g.getSkillName();
			targetLevel = Experience.getLevelForXp(g.getTargetValue());
		}
		return new GoalView.RelationView(g.getName(), skillName, targetLevel, g.isOptional());
	}

	static SectionView toSectionView(Section s)
	{
		SectionView v = new SectionView();
		v.id = s.getId();
		v.name = s.getName();
		v.order = s.getOrder();
		v.collapsed = s.isCollapsed();
		v.builtIn = s.isBuiltIn();
		v.kind = s.getBuiltInKind() != null ? s.getBuiltInKind().name() : null;
		v.defaultColorRgb = SECTION_DEFAULT_COLOR_RGB;
		if (s.getColorRgb() >= 0)
		{
			v.colorRgb = s.getColorRgb();
			v.colorOverridden = true;
		}
		else
		{
			v.colorRgb = SECTION_DEFAULT_COLOR_RGB;
			v.colorOverridden = false;
		}
		return v;
	}

	List<GoalView> queryGoalsTopologicallySorted(String sectionId)
	{
		log.debug("API.internal queryGoalsTopologicallySorted(sectionId={})", sectionId);
		List<GoalView> out = new ArrayList<>();
		if (sectionId == null) return out;

		// 1. Collect the section's goals in priority order.
		List<Goal> sectionGoals = new ArrayList<>();
		for (Goal g : api.goalStore.getGoals())
		{
			if (sectionId.equals(g.getSectionId())) sectionGoals.add(g);
		}
		sectionGoals.sort(java.util.Comparator.comparingInt(Goal::getPriority));
		if (sectionGoals.isEmpty()) return out;

		java.util.Set<String> sectionIds = new java.util.HashSet<>();
		for (Goal g : sectionGoals) sectionIds.add(g.getId());

		// 2. Compute in-degree of each goal (count of in-section requirements).
		java.util.Map<String, Integer> inDegree = new java.util.HashMap<>();
		for (Goal g : sectionGoals)
		{
			int count = 0;
			if (g.getRequiredGoalIds() != null)
			{
				for (String req : g.getRequiredGoalIds())
				{
					if (sectionIds.contains(req)) count++;
				}
			}
			inDegree.put(g.getId(), count);
		}

		// 3. Reverse-lookup: for each goal, which OTHER in-section goals
		//    have this one in their requiredGoalIds?
		java.util.Map<String, java.util.List<String>> dependents = new java.util.HashMap<>();
		for (Goal g : sectionGoals) dependents.put(g.getId(), new ArrayList<>());
		for (Goal g : sectionGoals)
		{
			if (g.getRequiredGoalIds() == null) continue;
			for (String req : g.getRequiredGoalIds())
			{
				if (sectionIds.contains(req)) dependents.get(req).add(g.getId());
			}
		}

		// 4. Stable local-repair sort.
		List<Goal> ordered = new ArrayList<>(sectionGoals);
		int maxIter = sectionGoals.size() * sectionGoals.size() + 1;
		int iter = 0;
		boolean converged = false;
		while (!converged && iter++ < maxIter)
		{
			converged = true;
			java.util.Map<String, Integer> pos = new java.util.HashMap<>();
			for (int i = 0; i < ordered.size(); i++) pos.put(ordered.get(i).getId(), i);

			for (int i = 0; i < ordered.size(); i++)
			{
				Goal g = ordered.get(i);
				List<String> reqs = g.getRequiredGoalIds();
				if (reqs == null || reqs.isEmpty()) continue;
				int maxReqPos = -1;
				for (String reqId : reqs)
				{
					if (!sectionIds.contains(reqId)) continue;
					Integer p = pos.get(reqId);
					if (p != null && p > maxReqPos) maxReqPos = p;
				}
				if (maxReqPos > i)
				{
					ordered.remove(i);
					ordered.add(maxReqPos, g);
					converged = false;
					break;
				}
			}
		}
		if (!converged)
		{
			log.warn("queryGoalsTopologicallySorted: local-repair hit iteration bound "
				+ "in section {} — graph may contain a cycle", sectionId);
		}

		for (Goal g : ordered)
		{
			GoalView v = toGoalView(g);
			out.add(v);
		}
		return out;
	}
}
