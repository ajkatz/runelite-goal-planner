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
		// Force-update built-in section orders to current constants. Existing
		// installs may have Incomplete persisted at order=0 from before the
		// pin-Incomplete-above-Completed change.
		boolean reordered = false;
		for (Section s : sections)
		{
			if (s.getBuiltInKind() == Section.BuiltInKind.INCOMPLETE
				&& s.getOrder() != Section.ORDER_INCOMPLETE)
			{
				s.setOrder(Section.ORDER_INCOMPLETE);
				reordered = true;
			}
			else if (s.getBuiltInKind() == Section.BuiltInKind.COMPLETED
				&& s.getOrder() != Section.ORDER_COMPLETED)
			{
				s.setOrder(Section.ORDER_COMPLETED);
				reordered = true;
			}
		}
		if (created || reordered)
		{
			log.info("Built-in sections normalized (created={}, reordered={})", created, reordered);
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

	// ---------------------------------------------------------------------
	// User-defined section CRUD (Phase 2)
	// ---------------------------------------------------------------------

	private static final int MAX_SECTION_NAME_LENGTH = 40;

	/**
	 * Validate a proposed section name. Returns null if valid, otherwise an
	 * error message describing why it's invalid. Built-in name collisions are
	 * checked here too.
	 */
	public String validateSectionName(String name)
	{
		if (name == null) return "Name is required";
		String trimmed = name.trim();
		if (trimmed.isEmpty()) return "Name cannot be empty";
		if (trimmed.length() > MAX_SECTION_NAME_LENGTH)
			return "Name must be " + MAX_SECTION_NAME_LENGTH + " characters or fewer";
		if (trimmed.equalsIgnoreCase("Incomplete") || trimmed.equalsIgnoreCase("Completed"))
			return "That name is reserved";
		return null;
	}

	/**
	 * Find a section by id. Returns null if not found.
	 */
	public Section findSection(String sectionId)
	{
		if (sectionId == null) return null;
		for (Section s : sections)
		{
			if (sectionId.equals(s.getId())) return s;
		}
		return null;
	}

	/**
	 * Find a user-defined section by case-insensitive name match. Returns null
	 * if no match (or if the match is a built-in).
	 */
	public Section findUserSectionByName(String name)
	{
		if (name == null) return null;
		String trimmed = name.trim();
		for (Section s : sections)
		{
			if (!s.isBuiltIn() && s.getName() != null && s.getName().equalsIgnoreCase(trimmed))
			{
				return s;
			}
		}
		return null;
	}

	/**
	 * Return user-defined sections in display order (excludes built-ins).
	 */
	public List<Section> getUserSections()
	{
		List<Section> result = new ArrayList<>();
		for (Section s : sections)
		{
			if (!s.isBuiltIn()) result.add(s);
		}
		result.sort(Comparator.comparingInt(Section::getOrder));
		return result;
	}

	/**
	 * Create a user-defined section, appended at the end of the user-section
	 * band (just above Completed). Idempotent: returns the existing section if
	 * a user section with the same name (case-insensitive) already exists.
	 *
	 * @return the created or existing section
	 * @throws IllegalArgumentException if the name is invalid
	 */
	public Section createUserSection(String name)
	{
		String error = validateSectionName(name);
		if (error != null) throw new IllegalArgumentException(error);
		String trimmed = name.trim();

		Section existing = findUserSectionByName(trimmed);
		if (existing != null) return existing;

		int nextOrder = nextUserSectionOrder();
		Section created = Section.builder()
			.name(trimmed)
			.order(nextOrder)
			.builtInKind(null)
			.build();
		sections.add(created);
		renumberUserSections();
		save();
		return created;
	}

	/**
	 * Rename a user-defined section. Returns false on: not found, built-in,
	 * invalid name, duplicate name, or no-op (same name).
	 */
	public boolean renameUserSection(String sectionId, String newName)
	{
		Section section = findSection(sectionId);
		if (section == null || section.isBuiltIn()) return false;
		if (validateSectionName(newName) != null) return false;
		String trimmed = newName.trim();
		if (trimmed.equals(section.getName())) return false;
		Section dup = findUserSectionByName(trimmed);
		if (dup != null && !dup.getId().equals(sectionId)) return false;
		section.setName(trimmed);
		save();
		return true;
	}

	/**
	 * Delete a user-defined section. All goals in the section are moved to the
	 * end of the Incomplete section (then reconcile may pull completed ones to
	 * Completed). Returns false on: not found or built-in.
	 */
	public boolean deleteUserSection(String sectionId)
	{
		Section section = findSection(sectionId);
		if (section == null || section.isBuiltIn()) return false;

		String incompleteId = getIncompleteSection().getId();
		for (Goal g : goals)
		{
			if (sectionId.equals(g.getSectionId()))
			{
				g.setSectionId(incompleteId);
			}
		}
		sections.removeIf(s -> sectionId.equals(s.getId()));
		renumberUserSections();
		normalizeOrder();
		reconcileCompletedSection();
		save();
		return true;
	}

	/**
	 * Reorder a user-defined section to a new index within the user-section
	 * band (0-based, excludes built-ins). Out-of-range values are clamped.
	 * Returns false on: not found, built-in, or no-op (same index).
	 */
	public boolean reorderUserSection(String sectionId, int newUserIndex)
	{
		Section section = findSection(sectionId);
		if (section == null || section.isBuiltIn()) return false;

		List<Section> userSections = getUserSections();
		int currentIndex = -1;
		for (int i = 0; i < userSections.size(); i++)
		{
			if (userSections.get(i).getId().equals(sectionId)) { currentIndex = i; break; }
		}
		if (currentIndex < 0) return false;

		int clamped = Math.max(0, Math.min(newUserIndex, userSections.size() - 1));
		if (clamped == currentIndex) return false;

		userSections.remove(currentIndex);
		userSections.add(clamped, section);
		// Reassign order ints in their new sequence
		for (int i = 0; i < userSections.size(); i++)
		{
			userSections.get(i).setOrder(i + 1); // 0 reserved for Incomplete
		}
		normalizeOrder();
		save();
		return true;
	}

	/**
	 * Move a goal to a different section, appended at the end. Returns false
	 * on: unknown goal id, unknown section id, no-op (same section), or the
	 * goal is complete and the destination is not the Completed section.
	 */
	public boolean moveGoalToSection(String goalId, String sectionId)
	{
		Goal goal = null;
		for (Goal g : goals)
		{
			if (g.getId().equals(goalId)) { goal = g; break; }
		}
		if (goal == null) return false;

		Section dest = findSection(sectionId);
		if (dest == null) return false;
		if (sectionId.equals(goal.getSectionId())) return false;

		boolean destIsCompleted = dest.getBuiltInKind() == Section.BuiltInKind.COMPLETED;
		if (goal.isComplete() && !destIsCompleted) return false;

		goal.setSectionId(sectionId);
		// Move the goal to the end of the goals list within its new section.
		// normalizeOrder groups by section.order; within a section we order by
		// current priority. Bumping this goal's priority above all others in the
		// destination section guarantees "appended at end" after normalize.
		int maxPriorityInDest = -1;
		for (Goal g : goals)
		{
			if (sectionId.equals(g.getSectionId()) && g != goal && g.getPriority() > maxPriorityInDest)
			{
				maxPriorityInDest = g.getPriority();
			}
		}
		goal.setPriority(maxPriorityInDest + 1);
		normalizeOrder();
		save();
		return true;
	}

	/**
	 * Renumber all user sections to consecutive ints starting at 1 (0 reserved
	 * for Incomplete, MAX_VALUE for Completed). Preserves their current relative
	 * order.
	 */
	private void renumberUserSections()
	{
		List<Section> userSections = getUserSections();
		for (int i = 0; i < userSections.size(); i++)
		{
			userSections.get(i).setOrder(i + 1);
		}
	}

	/**
	 * Delete every user-defined section. Goals in those sections are reassigned
	 * to Incomplete; reconcile then pulls completed ones to Completed.
	 * @return number of sections removed
	 */
	public int removeAllUserSections()
	{
		String incompleteId = getIncompleteSection().getId();
		java.util.Set<String> doomed = new java.util.HashSet<>();
		for (Section s : sections)
		{
			if (!s.isBuiltIn()) doomed.add(s.getId());
		}
		if (doomed.isEmpty()) return 0;

		for (Goal g : goals)
		{
			if (doomed.contains(g.getSectionId()))
			{
				g.setSectionId(incompleteId);
			}
		}
		sections.removeIf(s -> doomed.contains(s.getId()));
		renumberUserSections();
		normalizeOrder();
		reconcileCompletedSection();
		save();
		return doomed.size();
	}

	private int nextUserSectionOrder()
	{
		int max = 0;
		for (Section s : sections)
		{
			if (!s.isBuiltIn() && s.getOrder() > max) max = s.getOrder();
		}
		return max + 1;
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
