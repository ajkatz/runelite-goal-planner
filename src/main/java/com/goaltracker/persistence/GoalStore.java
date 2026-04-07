package com.goaltracker.persistence;

import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalStatus;
import com.goaltracker.model.Section;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Persists goals and sections using RuneLite's ConfigManager.
 *
 * Goals are stored as a flat list with an embedded {@code sectionId}. Sections
 * are stored separately. On first load without sections, the two built-ins
 * (Incomplete and Completed) are created and existing goals are assigned based
 * on their status (COMPLETE → Completed, else → Incomplete).
 *
 * Global priority ordering on Goal is preserved; the panel groups by section at
 * render time. This keeps {@link com.goaltracker.service.GoalReorderingService}
 * unchanged during the sections refactor.
 */
@Slf4j
@Singleton
public class GoalStore
{
	private static final String CONFIG_GROUP = "goaltracker";
	private static final String GOALS_KEY = "goals";
	private static final String SECTIONS_KEY = "sections";

	private static final Gson GSON = new GsonBuilder().create();
	private static final Type GOAL_LIST_TYPE = new TypeToken<List<Goal>>(){}.getType();
	private static final Type SECTION_LIST_TYPE = new TypeToken<List<Section>>(){}.getType();

	private final ConfigManager configManager;
	private List<Goal> goals = new ArrayList<>();
	private List<Section> sections = new ArrayList<>();

	@Inject
	public GoalStore(ConfigManager configManager)
	{
		this.configManager = configManager;
	}

	public void load()
	{
		// Goals
		String goalsJson = configManager.getConfiguration(CONFIG_GROUP, GOALS_KEY);
		if (goalsJson != null && !goalsJson.isEmpty())
		{
			try
			{
				List<Goal> loaded = GSON.fromJson(goalsJson, GOAL_LIST_TYPE);
				if (loaded != null)
				{
					goals = new ArrayList<>(loaded);
					goals.sort(Comparator.comparingInt(Goal::getPriority));
					log.info("Loaded {} goals", goals.size());
				}
			}
			catch (Exception e)
			{
				log.error("Failed to load goals", e);
				goals = new ArrayList<>();
			}
		}

		// Sections
		String sectionsJson = configManager.getConfiguration(CONFIG_GROUP, SECTIONS_KEY);
		if (sectionsJson != null && !sectionsJson.isEmpty())
		{
			try
			{
				List<Section> loaded = GSON.fromJson(sectionsJson, SECTION_LIST_TYPE);
				if (loaded != null)
				{
					sections = new ArrayList<>(loaded);
					log.info("Loaded {} sections", sections.size());
				}
			}
			catch (Exception e)
			{
				log.error("Failed to load sections", e);
				sections = new ArrayList<>();
			}
		}

		ensureBuiltInSections();
		migrateOrphanedGoals();
		normalizeOrder();
	}

	/**
	 * Re-sort the flat goals list so that all goals in the same section are
	 * contiguous, sections are in order, and priorities are re-indexed to match.
	 * Called after load, migration, and whenever a goal changes sections.
	 */
	public void normalizeOrder()
	{
		// Build a lookup from sectionId to section.order
		java.util.Map<String, Integer> sectionOrders = new java.util.HashMap<>();
		for (Section s : sections)
		{
			sectionOrders.put(s.getId(), s.getOrder());
		}
		// Sort by (section.order, current priority) — stable so relative order within
		// section is preserved.
		goals.sort((a, b) -> {
			int aso = sectionOrders.getOrDefault(a.getSectionId(), Integer.MAX_VALUE);
			int bso = sectionOrders.getOrDefault(b.getSectionId(), Integer.MAX_VALUE);
			if (aso != bso) return Integer.compare(aso, bso);
			return Integer.compare(a.getPriority(), b.getPriority());
		});
		reindex();
	}

	/**
	 * Create the Incomplete and Completed built-in sections if missing.
	 */
	private void ensureBuiltInSections()
	{
		boolean hasIncomplete = sections.stream()
			.anyMatch(s -> s.getBuiltInKind() == Section.BuiltInKind.INCOMPLETE);
		boolean hasCompleted = sections.stream()
			.anyMatch(s -> s.getBuiltInKind() == Section.BuiltInKind.COMPLETED);

		boolean created = false;
		if (!hasIncomplete)
		{
			sections.add(Section.builder()
				.name("Incomplete")
				.order(Section.ORDER_INCOMPLETE)
				.builtInKind(Section.BuiltInKind.INCOMPLETE)
				.build());
			created = true;
		}
		if (!hasCompleted)
		{
			sections.add(Section.builder()
				.name("Completed")
				.order(Section.ORDER_COMPLETED)
				.builtInKind(Section.BuiltInKind.COMPLETED)
				.build());
			created = true;
		}
		if (created)
		{
			log.info("Created built-in sections (incomplete/completed as needed)");
			sections.sort(Comparator.comparingInt(Section::getOrder));
			save();
		}
	}

	/**
	 * Assign any goal with a null or unknown sectionId to a built-in section based on
	 * its status (COMPLETE → Completed, else → Incomplete).
	 */
	private void migrateOrphanedGoals()
	{
		String incompleteId = getIncompleteSection().getId();
		String completedId = getCompletedSection().getId();

		boolean anyChanged = false;
		for (Goal goal : goals)
		{
			String sid = goal.getSectionId();
			boolean known = sid != null && sections.stream().anyMatch(s -> s.getId().equals(sid));
			if (!known)
			{
				goal.setSectionId(goal.isComplete() ? completedId : incompleteId);
				anyChanged = true;
			}
		}
		if (anyChanged)
		{
			log.info("Migrated orphaned goals into built-in sections");
			save();
		}
	}

	public void save()
	{
		try
		{
			String goalsJson = GSON.toJson(goals);
			configManager.setConfiguration(CONFIG_GROUP, GOALS_KEY, goalsJson);

			String sectionsJson = GSON.toJson(sections);
			configManager.setConfiguration(CONFIG_GROUP, SECTIONS_KEY, sectionsJson);

			log.debug("Saved {} goals / {} sections", goals.size(), sections.size());
		}
		catch (Exception e)
		{
			log.error("Failed to save goals/sections", e);
		}
	}

	public List<Goal> getGoals()
	{
		return goals;
	}

	/**
	 * True if any existing goal matches the given predicate. Used as a unified
	 * duplicate-guard for goal types that have a natural identity check (quest
	 * name, diary area+tier, combat achievement task id, etc.).
	 */
	public boolean exists(java.util.function.Predicate<Goal> predicate)
	{
		for (Goal g : goals)
		{
			if (predicate.test(g)) return true;
		}
		return false;
	}

	public List<Section> getSections()
	{
		return sections;
	}

	/**
	 * Return the Incomplete built-in section. Guaranteed non-null after load().
	 */
	public Section getIncompleteSection()
	{
		for (Section s : sections)
		{
			if (s.getBuiltInKind() == Section.BuiltInKind.INCOMPLETE) return s;
		}
		// Shouldn't happen post-load; be defensive.
		Section created = Section.builder()
			.name("Incomplete")
			.order(Section.ORDER_INCOMPLETE)
			.builtInKind(Section.BuiltInKind.INCOMPLETE)
			.build();
		sections.add(created);
		return created;
	}

	public Section getCompletedSection()
	{
		for (Section s : sections)
		{
			if (s.getBuiltInKind() == Section.BuiltInKind.COMPLETED) return s;
		}
		Section created = Section.builder()
			.name("Completed")
			.order(Section.ORDER_COMPLETED)
			.builtInKind(Section.BuiltInKind.COMPLETED)
			.build();
		sections.add(created);
		return created;
	}

	public void addGoal(Goal goal)
	{
		if (goal.getSectionId() == null)
		{
			goal.setSectionId(getIncompleteSection().getId());
		}
		goal.setPriority(goals.size());
		goals.add(goal);
		save();
	}

	public void removeGoal(String goalId)
	{
		goals.removeIf(g -> g.getId().equals(goalId));
		reindex();
		save();
	}

	public void updateGoal(Goal goal)
	{
		for (int i = 0; i < goals.size(); i++)
		{
			if (goals.get(i).getId().equals(goal.getId()))
			{
				goals.set(i, goal);
				break;
			}
		}
		save();
	}

	/**
	 * Scan all goals and move any that have flipped to COMPLETE into the Completed
	 * section (unless they're already there). Returns true if any goal was moved.
	 * Call this after tracker updates.
	 */
	public boolean reconcileCompletedSection()
	{
		String completedId = getCompletedSection().getId();
		String incompleteId = getIncompleteSection().getId();
		boolean anyMoved = false;
		for (Goal goal : goals)
		{
			boolean isComplete = goal.isComplete();
			String currentSid = goal.getSectionId();
			if (isComplete && !completedId.equals(currentSid))
			{
				goal.setSectionId(completedId);
				anyMoved = true;
			}
			else if (!isComplete && completedId.equals(currentSid))
			{
				// Defensive: if a goal un-completes (e.g. value changes via custom toggle),
				// return it to the Incomplete section.
				goal.setSectionId(incompleteId);
				anyMoved = true;
			}
		}
		if (anyMoved)
		{
			normalizeOrder();
			save();
		}
		return anyMoved;
	}

	public void setGoals(List<Goal> newGoals)
	{
		goals = new ArrayList<>(newGoals);
		reindex();
		save();
	}

	public void reorder(int fromIndex, int toIndex)
	{
		if (fromIndex < 0 || fromIndex >= goals.size() || toIndex < 0 || toIndex >= goals.size())
		{
			return;
		}
		Goal moved = goals.remove(fromIndex);
		goals.add(toIndex, moved);
		reindex();
		save();
	}

	private void reindex()
	{
		for (int i = 0; i < goals.size(); i++)
		{
			goals.get(i).setPriority(i);
		}
	}
}
