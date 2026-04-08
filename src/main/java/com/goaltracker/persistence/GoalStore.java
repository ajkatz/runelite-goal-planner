package com.goaltracker.persistence;

import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalStatus;
import com.goaltracker.model.Section;
import com.goaltracker.model.Tag;
import com.goaltracker.model.TagCategory;
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
	private static final String TAGS_KEY = "tags";
	private static final String CATEGORY_COLORS_KEY = "categoryColors";

	private static final Gson GSON = new GsonBuilder().create();
	private static final Type GOAL_LIST_TYPE = new TypeToken<List<Goal>>(){}.getType();
	private static final Type SECTION_LIST_TYPE = new TypeToken<List<Section>>(){}.getType();
	private static final Type TAG_LIST_TYPE = new TypeToken<List<Tag>>(){}.getType();
	private static final Type CATEGORY_COLOR_MAP_TYPE =
		new TypeToken<java.util.Map<String, Integer>>(){}.getType();

	private final ConfigManager configManager;
	private List<Goal> goals = new ArrayList<>();
	private List<Section> sections = new ArrayList<>();
	private List<Tag> tags = new ArrayList<>();
	/** Per-category color overrides (Mission 20). Key = TagCategory.name(), value = packed 0xRRGGBB. */
	private java.util.Map<String, Integer> categoryColors = new java.util.HashMap<>();

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

		// Tags (Mission 19: first-class tag entities)
		String tagsJson = configManager.getConfiguration(CONFIG_GROUP, TAGS_KEY);
		if (tagsJson != null && !tagsJson.isEmpty())
		{
			try
			{
				List<Tag> loaded = GSON.fromJson(tagsJson, TAG_LIST_TYPE);
				if (loaded != null)
				{
					tags = new ArrayList<>(loaded);
					log.info("Loaded {} tags", tags.size());
				}
			}
			catch (Exception e)
			{
				log.error("Failed to load tags", e);
				tags = new ArrayList<>();
			}
		}

		// Category colors (Mission 20)
		String categoryColorsJson = configManager.getConfiguration(CONFIG_GROUP, CATEGORY_COLORS_KEY);
		if (categoryColorsJson != null && !categoryColorsJson.isEmpty())
		{
			try
			{
				java.util.Map<String, Integer> loaded = GSON.fromJson(categoryColorsJson, CATEGORY_COLOR_MAP_TYPE);
				if (loaded != null)
				{
					categoryColors = new java.util.HashMap<>(loaded);
					log.info("Loaded {} category color overrides", categoryColors.size());
				}
			}
			catch (Exception e)
			{
				log.error("Failed to load category colors", e);
				categoryColors = new java.util.HashMap<>();
			}
		}

		// Migrate any tags with a null category. Happens when an enum value is
		// removed (Mission 19 dropped SPECIAL): Gson deserializes the unknown
		// category name as null. Reassign to OTHER and re-save so subsequent
		// operations have a valid category.
		boolean tagsMigrated = false;
		for (Tag t : tags)
		{
			if (t.getCategory() == null)
			{
				log.info("Migrating tag {} (label={}) with null category to OTHER",
					t.getId(), t.getLabel());
				t.setCategory(TagCategory.OTHER);
				tagsMigrated = true;
			}
		}

		// Specific historical rename: "Pets" → "Pet" (canonical label as of
		// Mission 19). Redirect goal references from "Pets" entities to "Pet"
		// entities and delete the "Pets" entries.
		tagsMigrated |= mergeTagsByLabelRename("Pets", "Pet", TagCategory.OTHER);

		// Generic dedupe pass: any (lowercase label, category) collision is
		// merged into a single canonical entity. Catches accidental duplicates
		// from buggy seed iterations or pre-dedupe goal creation flows.
		tagsMigrated |= dedupeTagsByLabelCategory();

		ensureBuiltInSections();
		migrateOrphanedGoals();
		normalizeOrder();

		if (tagsMigrated) save();
	}

	/**
	 * Merge all tags with {@code oldLabel} into a tag with {@code newLabel} in
	 * the same category. Used for historical label renames (e.g. Pets → Pet).
	 * Goal references are redirected; the old tags are removed from the store.
	 *
	 * @return true if anything was merged
	 */
	private boolean mergeTagsByLabelRename(String oldLabel, String newLabel, TagCategory category)
	{
		Tag canonical = findTagByLabel(newLabel, category);
		java.util.List<Tag> toMerge = new ArrayList<>();
		for (Tag t : tags)
		{
			if (t.getCategory() == category && oldLabel.equalsIgnoreCase(t.getLabel()))
			{
				toMerge.add(t);
			}
		}
		if (toMerge.isEmpty()) return false;
		// If there's no canonical "newLabel" tag, promote the first old one by
		// renaming it in place.
		if (canonical == null)
		{
			canonical = toMerge.remove(0);
			canonical.setLabel(newLabel);
		}
		final String canonicalId = canonical.getId();
		for (Tag old : toMerge)
		{
			redirectGoalTagReferences(old.getId(), canonicalId);
			tags.removeIf(x -> x.getId().equals(old.getId()));
			log.info("Merged tag {} ({}) → {} ({})", old.getId(), oldLabel, canonicalId, newLabel);
		}
		return true;
	}

	/**
	 * Generic dedupe: any group of tags sharing the same (lowercase label,
	 * category) is collapsed to a single canonical entity. The canonical is
	 * preferred by: has-color-override first, then first-in-list.
	 *
	 * @return true if any duplicates were merged
	 */
	private boolean dedupeTagsByLabelCategory()
	{
		java.util.Map<String, java.util.List<Tag>> groups = new java.util.LinkedHashMap<>();
		for (Tag t : tags)
		{
			if (t.getCategory() == null || t.getLabel() == null) continue;
			String key = t.getCategory().name() + "|" + t.getLabel().toLowerCase();
			groups.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
		}
		boolean anyMerged = false;
		for (java.util.List<Tag> group : groups.values())
		{
			if (group.size() <= 1) continue;
			// Pick canonical: prefer one with a color override (Mission 20:
			// OTHER tags can have per-tag color overrides; for other categories
			// the field is unused so getColorRgb is always -1, and the first
			// entry wins).
			Tag canonical = group.get(0);
			for (Tag t : group)
			{
				if (t.getColorRgb() >= 0) { canonical = t; break; }
			}
			final String canonicalId = canonical.getId();
			for (Tag dup : group)
			{
				if (dup == canonical) continue;
				redirectGoalTagReferences(dup.getId(), canonicalId);
				tags.removeIf(x -> x.getId().equals(dup.getId()));
				log.info("Deduped tag {} ({}, {}) → {} (canonical)",
					dup.getId(), dup.getLabel(), dup.getCategory(), canonicalId);
				anyMerged = true;
			}
		}
		return anyMerged;
	}

	/**
	 * Replace every occurrence of {@code fromTagId} with {@code toTagId} in
	 * every goal's {@code tagIds} and {@code defaultTagIds} lists. Used by
	 * the merge / dedupe migrations to redirect references before deleting
	 * the source tag.
	 */
	private void redirectGoalTagReferences(String fromTagId, String toTagId)
	{
		for (Goal g : goals)
		{
			if (g.getTagIds() != null)
			{
				java.util.List<String> updated = new ArrayList<>();
				for (String id : g.getTagIds())
				{
					String mapped = fromTagId.equals(id) ? toTagId : id;
					if (!updated.contains(mapped)) updated.add(mapped);
				}
				g.setTagIds(updated);
			}
			if (g.getDefaultTagIds() != null)
			{
				java.util.List<String> updated = new ArrayList<>();
				for (String id : g.getDefaultTagIds())
				{
					String mapped = fromTagId.equals(id) ? toTagId : id;
					if (!updated.contains(mapped)) updated.add(mapped);
				}
				g.setDefaultTagIds(updated);
			}
		}
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

			String tagsJson = GSON.toJson(tags);
			configManager.setConfiguration(CONFIG_GROUP, TAGS_KEY, tagsJson);

			String categoryColorsJson = GSON.toJson(categoryColors);
			configManager.setConfiguration(CONFIG_GROUP, CATEGORY_COLORS_KEY, categoryColorsJson);

			log.debug("Saved {} goals / {} sections / {} tags / {} category colors",
				goals.size(), sections.size(), tags.size(), categoryColors.size());
		}
		catch (Exception e)
		{
			log.error("Failed to save goals/sections/tags", e);
		}
	}

	// ---------------------------------------------------------------------
	// Category color overrides (Mission 20)
	// ---------------------------------------------------------------------

	/**
	 * Get the effective color for a tag category as a packed 0xRRGGBB int.
	 * Returns the user override if set, else the {@link TagCategory#getColor()}
	 * default packed.
	 */
	public int getCategoryColor(TagCategory category)
	{
		if (category == null) return 0;
		Integer override = categoryColors.get(category.name());
		if (override != null && override >= 0) return override;
		java.awt.Color c = category.getColor();
		return (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();
	}

	/** @return packed 0xRRGGBB default color for the category (no override). */
	public int getCategoryDefaultColor(TagCategory category)
	{
		if (category == null) return 0;
		java.awt.Color c = category.getColor();
		return (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();
	}

	/** @return true when the category has a user color override (not the default). */
	public boolean isCategoryColorOverridden(TagCategory category)
	{
		if (category == null) return false;
		Integer override = categoryColors.get(category.name());
		return override != null && override >= 0;
	}

	/**
	 * Set a user color override on a tag category. Affects every tag in that
	 * category. SKILLING is read-only (returns false) — skill icon tags ignore
	 * the category color anyway, and locking the API surface keeps the
	 * read-only contract uniform.
	 *
	 * @return true if the color changed
	 */
	public boolean setCategoryColor(TagCategory category, int colorRgb)
	{
		// OTHER is special: it uses per-tag colors instead of a category-wide
		// color, so the category-level setter is meaningless for it.
		// SKILLING accepts a category color — system skill tags render as
		// skill icons (color ignored) but user-created SKILLING tags fall
		// through to colored pills, where the category color does apply.
		if (category == null || category == TagCategory.OTHER) return false;
		int normalized = colorRgb < 0 ? -1 : (colorRgb & 0xFFFFFF);
		Integer existing = categoryColors.get(category.name());
		if (existing != null && existing == normalized) return false;
		if (normalized < 0)
		{
			if (existing == null) return false;
			categoryColors.remove(category.name());
		}
		else
		{
			categoryColors.put(category.name(), normalized);
		}
		save();
		return true;
	}

	/** Equivalent to setCategoryColor(category, -1). */
	public boolean resetCategoryColor(TagCategory category)
	{
		return setCategoryColor(category, -1);
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

	// ---------------------------------------------------------------------
	// Tag entity CRUD (Mission 19)
	// ---------------------------------------------------------------------

	private static final int MAX_TAG_LABEL_LENGTH = 30;

	public List<Tag> getTags()
	{
		return tags;
	}

	public Tag findTag(String tagId)
	{
		if (tagId == null) return null;
		for (Tag t : tags)
		{
			if (tagId.equals(t.getId())) return t;
		}
		return null;
	}

	/**
	 * Look up a tag by case-insensitive (label, category) match. Returns the
	 * first match (system OR user) — used by the find-or-create flow so the
	 * same logical tag is shared across goals.
	 */
	public Tag findTagByLabel(String label, TagCategory category)
	{
		if (label == null || category == null) return null;
		String trimmed = label.trim();
		for (Tag t : tags)
		{
			if (t.getCategory() == category
				&& t.getLabel() != null
				&& t.getLabel().equalsIgnoreCase(trimmed))
			{
				return t;
			}
		}
		return null;
	}

	/** Validate a proposed tag label. Returns null on valid, error message otherwise. */
	public String validateTagLabel(String label)
	{
		if (label == null) return "Label is required";
		String trimmed = label.trim();
		if (trimmed.isEmpty()) return "Label cannot be empty";
		if (trimmed.length() > MAX_TAG_LABEL_LENGTH)
			return "Label must be " + MAX_TAG_LABEL_LENGTH + " characters or fewer";
		return null;
	}

	/**
	 * Create a user tag, or return the existing one if a (label, category)
	 * match already exists. Idempotent.
	 *
	 * @throws IllegalArgumentException if label is invalid
	 */
	public Tag createUserTag(String label, TagCategory category)
	{
		String error = validateTagLabel(label);
		if (error != null) throw new IllegalArgumentException(error);
		Tag existing = findTagByLabel(label, category);
		if (existing != null) return existing;
		Tag created = Tag.builder()
			.label(label.trim())
			.category(category)
			.system(false)
			.build();
		tags.add(created);
		save();
		return created;
	}

	/**
	 * Find a system tag by (label, category), or create one if missing.
	 * System tags are auto-generated by goal creation flows (Boss/Raid/Tier
	 * on CA goals, Slayer on slayer-task CA goals, etc).
	 */
	public Tag findOrCreateSystemTag(String label, TagCategory category)
	{
		if (label == null || category == null) return null;
		Tag existing = findTagByLabel(label, category);
		if (existing != null) return existing;
		Tag created = Tag.builder()
			.label(label.trim())
			.category(category)
			.system(true)
			.build();
		tags.add(created);
		save();
		return created;
	}

	/**
	 * Rename a user tag. System tags are read-only for name (returns false).
	 * Validates the new label and rejects no-op changes.
	 */
	public boolean renameTag(String tagId, String newLabel)
	{
		Tag t = findTag(tagId);
		if (t == null || t.isSystem()) return false;
		if (validateTagLabel(newLabel) != null) return false;
		String trimmed = newLabel.trim();
		if (trimmed.equals(t.getLabel())) return false;
		// Reject duplicate (label, category) within the same category
		Tag dup = findTagByLabel(trimmed, t.getCategory());
		if (dup != null && !dup.getId().equals(tagId)) return false;
		t.setLabel(trimmed);
		save();
		return true;
	}

	/**
	 * Recolor an individual tag. Mission 20: only meaningful for tags in the
	 * OTHER category — every other category uses a category-wide color set
	 * via {@link #setCategoryColor(TagCategory, int)}. Returns false for any
	 * non-OTHER tag (the call is a no-op rather than an error so the caller
	 * can decide whether to surface it).
	 */
	public boolean recolorTag(String tagId, int colorRgb)
	{
		Tag t = findTag(tagId);
		if (t == null) return false;
		if (t.getCategory() != TagCategory.OTHER) return false;
		int normalized = colorRgb < 0 ? -1 : (colorRgb & 0xFFFFFF);
		if (t.getColorRgb() == normalized) return false;
		t.setColorRgb(normalized);
		save();
		return true;
	}

	/**
	 * Delete a user tag and cascade-remove the reference from every goal.
	 * System tags cannot be deleted (returns false) — they're auto-attached
	 * by goal creation and trackers depend on their existence.
	 */
	public boolean deleteTag(String tagId)
	{
		Tag t = findTag(tagId);
		if (t == null || t.isSystem()) return false;
		for (Goal g : goals)
		{
			if (g.getTagIds() != null) g.getTagIds().remove(tagId);
			if (g.getDefaultTagIds() != null) g.getDefaultTagIds().remove(tagId);
		}
		tags.removeIf(x -> tagId.equals(x.getId()));
		save();
		return true;
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
