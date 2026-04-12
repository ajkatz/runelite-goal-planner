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

	// V2 per-entity persistence keys
	private static final String SCHEMA_KEY = "schema";
	private static final String SCHEMA_VERSION = "2";
	private static final String GOAL_ORDER_KEY = "goal_order";
	private static final String GOAL_PREFIX = "g.";
	private static final String TAG_IDS_KEY = "tag_ids";
	private static final String TAG_PREFIX = "t.";

	private static final Gson GSON = new GsonBuilder().create();
	private static final Type GOAL_LIST_TYPE = new TypeToken<List<Goal>>(){}.getType();
	private static final Type SECTION_LIST_TYPE = new TypeToken<List<Section>>(){}.getType();
	private static final Type TAG_LIST_TYPE = new TypeToken<List<Tag>>(){}.getType();
	private static final Type CATEGORY_COLOR_MAP_TYPE =
		new TypeToken<java.util.Map<String, Integer>>(){}.getType();
	private static final Type STRING_LIST_TYPE = new TypeToken<List<String>>(){}.getType();

	private final ConfigManager configManager;
	private List<Goal> goals = new ArrayList<>();
	private List<Section> sections = new ArrayList<>();
	private List<Tag> tags = new ArrayList<>();
	/** Per-category color overrides. Key = TagCategory.name(), value = packed 0xRRGGBB. */
	private java.util.Map<String, Integer> categoryColors = new java.util.HashMap<>();

	/** When true, granular saves are deferred. Call resumeSave() to persist. */
	private boolean saveSuspended = false;
	/** Granular dirty tracking for suspend/resume. */
	private final java.util.Set<String> dirtyGoalIds = new java.util.HashSet<>();
	private final java.util.Set<String> dirtyTagIds = new java.util.HashSet<>();
	private boolean goalOrderDirty = false;
	private boolean tagIdsDirty = false;
	private boolean sectionsDirty = false;
	private boolean categoryColorsDirty = false;

	/** O(1) goal lookup by id. Maintained on add/remove/load. */
	private final java.util.Map<String, Goal> goalIndex = new java.util.HashMap<>();
	/** O(1) section lookup by id. Maintained on create/delete/load. */
	private final java.util.Map<String, Section> sectionIndex = new java.util.HashMap<>();
	/** O(1) tag lookup by id. Maintained on create/delete/load. */
	private final java.util.Map<String, Tag> tagIndex = new java.util.HashMap<>();
	/** Reverse index: toGoalId → set of fromGoalIds that require it. O(1) getDependents. */
	private final java.util.Map<String, java.util.Set<String>> dependentIndex = new java.util.HashMap<>();

	@Inject
	public GoalStore(ConfigManager configManager)
	{
		this.configManager = configManager;
	}

	public void load()
	{
		String schema = configManager.getConfiguration(CONFIG_GROUP, SCHEMA_KEY);
		if (SCHEMA_VERSION.equals(schema))
		{
			loadV2();
		}
		else
		{
			loadV1();
			migrateToV2();
		}

		// Migrate any tags with a null category. Happens when an enum value is
		// removed (SPECIAL was dropped): Gson deserializes the unknown
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
		// Redirect goal references from "Pets" entities to "Pet"
		// entities and delete the "Pets" entries.
		tagsMigrated |= mergeTagsByLabelRename("Pets", "Pet", TagCategory.OTHER);

		// Generic dedupe pass: any (lowercase label, category) collision is
		// merged into a single canonical entity. Catches accidental duplicates
		// from buggy seed iterations or pre-dedupe goal creation flows.
		tagsMigrated |= dedupeTagsByLabelCategory();

		ensureBuiltInSections();
		migrateOrphanedGoals();
		normalizeOrder();

		// Scrub any relation edges that violate the DAG invariant
		// or point at missing goals. Forgiving on load — we drop the offending
		// edges with a log warning rather than failing to load the whole save.
		// Normal flows never persist invalid edges (addRequirement rejects
		// cycles and missing targets up-front), so this is defensive against
		// external JSON edits or a bug slipping past the API layer.
		boolean relationsScrubbed = scrubInvalidRelationEdges();

		if (tagsMigrated || relationsScrubbed)
		{
			// Migration touched tags and/or goal relation edges — persist affected entities.
			for (Goal g : goals) saveGoal(g);
			saveGoalOrder();
			for (Tag t : tags) saveTag(t);
			saveTagIds();
			saveSections();
			saveCategoryColors();
		}

		rebuildIndexes();
	}

	/** Load from V1 monolithic format (goals/tags as single JSON blobs). */
	private void loadV1()
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
					log.info("Loaded {} goals (v1)", goals.size());
				}
			}
			catch (Exception e)
			{
				log.error("Failed to load goals (v1)", e);
				goals = new ArrayList<>();
			}
		}

		// Tags (monolithic)
		String tagsJson = configManager.getConfiguration(CONFIG_GROUP, TAGS_KEY);
		if (tagsJson != null && !tagsJson.isEmpty())
		{
			try
			{
				List<Tag> loaded = GSON.fromJson(tagsJson, TAG_LIST_TYPE);
				if (loaded != null)
				{
					tags = new ArrayList<>(loaded);
					log.info("Loaded {} tags (v1)", tags.size());
				}
			}
			catch (Exception e)
			{
				log.error("Failed to load tags (v1)", e);
				tags = new ArrayList<>();
			}
		}

		// Sections (same format in both versions)
		loadSectionsAndCategoryColors();
	}

	/** Load from V2 per-entity format (individual goal/tag keys). */
	private void loadV2()
	{
		// Goals: read order list, then each goal individually
		String orderJson = configManager.getConfiguration(CONFIG_GROUP, GOAL_ORDER_KEY);
		List<String> goalIds = orderJson != null
			? GSON.fromJson(orderJson, STRING_LIST_TYPE)
			: new ArrayList<>();
		goals = new ArrayList<>();
		for (String id : goalIds)
		{
			String goalJson = configManager.getConfiguration(CONFIG_GROUP, GOAL_PREFIX + id);
			if (goalJson != null)
			{
				try
				{
					Goal g = GSON.fromJson(goalJson, Goal.class);
					if (g != null) goals.add(g);
				}
				catch (Exception e)
				{
					log.error("Failed to load goal {}", id, e);
				}
			}
		}
		log.info("Loaded {} goals (v2)", goals.size());

		// Tags: read ID list, then each tag individually
		String tagIdsJson = configManager.getConfiguration(CONFIG_GROUP, TAG_IDS_KEY);
		List<String> tagIdList = tagIdsJson != null
			? GSON.fromJson(tagIdsJson, STRING_LIST_TYPE)
			: new ArrayList<>();
		tags = new ArrayList<>();
		for (String id : tagIdList)
		{
			String tagJson = configManager.getConfiguration(CONFIG_GROUP, TAG_PREFIX + id);
			if (tagJson != null)
			{
				try
				{
					Tag t = GSON.fromJson(tagJson, Tag.class);
					if (t != null) tags.add(t);
				}
				catch (Exception e)
				{
					log.error("Failed to load tag {}", id, e);
				}
			}
		}
		log.info("Loaded {} tags (v2)", tags.size());

		// Sections + categoryColors: same format as v1
		loadSectionsAndCategoryColors();
	}

	/** Load sections and category colors (same format in both v1 and v2). */
	private void loadSectionsAndCategoryColors()
	{
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

		// Category colors
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
	}

	/** Migrate from V1 monolithic to V2 per-entity persistence. */
	private void migrateToV2()
	{
		log.info("Migrating persistence from v1 (monolithic) to v2 (per-entity)");
		// Write each goal individually
		for (Goal g : goals) saveGoal(g);
		saveGoalOrder();
		// Write each tag individually
		for (Tag t : tags) saveTag(t);
		saveTagIds();
		// Write sections and categoryColors
		saveSections();
		saveCategoryColors();
		// Mark as migrated
		configManager.setConfiguration(CONFIG_GROUP, SCHEMA_KEY, SCHEMA_VERSION);
		// Delete old monolithic keys
		configManager.unsetConfiguration(CONFIG_GROUP, GOALS_KEY);
		configManager.unsetConfiguration(CONFIG_GROUP, TAGS_KEY);
		log.info("Migration complete: {} goals, {} tags", goals.size(), tags.size());
	}

	/** Rebuild all lookup indexes from the current lists. */
	private void rebuildIndexes()
	{
		goalIndex.clear();
		for (Goal g : goals) goalIndex.put(g.getId(), g);
		sectionIndex.clear();
		for (Section s : sections) sectionIndex.put(s.getId(), s);
		tagIndex.clear();
		for (Tag t : tags) tagIndex.put(t.getId(), t);
		dependentIndex.clear();
		for (Goal g : goals)
		{
			if (g.getRequiredGoalIds() != null)
			{
				for (String reqId : g.getRequiredGoalIds())
				{
					dependentIndex.computeIfAbsent(reqId, k -> new java.util.HashSet<>())
						.add(g.getId());
				}
			}
		}
	}

	/**
	 * Walk the goal graph and drop any outgoing edges that:
	 * <ul>
	 *   <li>point at a goal that no longer exists (dangling reference)</li>
	 *   <li>are self-loops</li>
	 *   <li>close a cycle in the graph (any edge in a strongly-connected
	 *       component of size &gt; 1)</li>
	 * </ul>
	 *
	 * <p>Self-loops and dangling edges are trivial to detect. For cycles we
	 * run a DFS-based scrub: walk each goal's outgoing edges, and if adding
	 * any edge creates a cycle given the edges already accepted, drop it.
	 * This is deterministic but biased — the first edges encountered "win"
	 * and later cycle-forming edges are dropped. That's fine for a
	 * corruption-recovery pass; normal flows never hit this code.
	 *
	 * @return true if any edge was removed
	 */
	private boolean scrubInvalidRelationEdges()
	{
		boolean changed = false;
		java.util.Set<String> validIds = new java.util.HashSet<>();
		for (Goal g : goals) validIds.add(g.getId());

		// Build a scratch graph where we re-accept edges one-by-one and drop
		// any that would close a cycle in the scratch graph.
		java.util.Map<String, java.util.List<String>> accepted = new java.util.HashMap<>();
		for (Goal g : goals) accepted.put(g.getId(), new ArrayList<>());

		for (Goal g : goals)
		{
			List<String> reqs = g.getRequiredGoalIds();
			if (reqs == null || reqs.isEmpty()) continue;
			java.util.Iterator<String> it = reqs.iterator();
			while (it.hasNext())
			{
				String target = it.next();
				if (target == null || target.equals(g.getId()))
				{
					log.warn("Scrubbed self-loop or null edge on goal {}", g.getId());
					it.remove();
					changed = true;
					continue;
				}
				if (!validIds.contains(target))
				{
					log.warn("Scrubbed dangling requirement edge {} → {} (target missing)",
						g.getId(), target);
					it.remove();
					changed = true;
					continue;
				}
				// Would accepting this edge close a cycle in the scratch graph?
				if (scratchPathExists(accepted, target, g.getId()))
				{
					log.warn("Scrubbed cycle-forming requirement edge {} → {}",
						g.getId(), target);
					it.remove();
					changed = true;
					continue;
				}
				accepted.get(g.getId()).add(target);
			}
		}
		return changed;
	}

	/** DFS on the scratch adjacency map used by {@link #scrubInvalidRelationEdges}. */
	private boolean scratchPathExists(java.util.Map<String, java.util.List<String>> adj,
		String from, String to)
	{
		if (from.equals(to)) return true;
		java.util.Set<String> visited = new java.util.HashSet<>();
		java.util.Deque<String> stack = new java.util.ArrayDeque<>();
		stack.push(from);
		while (!stack.isEmpty())
		{
			String cur = stack.pop();
			if (!visited.add(cur)) continue;
			if (cur.equals(to)) return true;
			List<String> neighbors = adj.get(cur);
			if (neighbors != null)
			{
				for (String n : neighbors) stack.push(n);
			}
		}
		return false;
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
			tagIndex.remove(old.getId());
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
			// Pick canonical: prefer one with a color override (
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
				tagIndex.remove(dup.getId());
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
				java.util.Set<String> seen = new java.util.LinkedHashSet<>();
				for (String id : g.getTagIds())
				{
					seen.add(fromTagId.equals(id) ? toTagId : id);
				}
				g.setTagIds(new ArrayList<>(seen));
			}
			if (g.getDefaultTagIds() != null)
			{
				java.util.Set<String> seen = new java.util.LinkedHashSet<>();
				for (String id : g.getDefaultTagIds())
				{
					seen.add(fromTagId.equals(id) ? toTagId : id);
				}
				g.setDefaultTagIds(new ArrayList<>(seen));
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
			saveSections();
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
			boolean known = sid != null && sectionIndex.containsKey(sid);
			if (!known)
			{
				goal.setSectionId(goal.isComplete() ? completedId : incompleteId);
				anyChanged = true;
			}
		}
		if (anyChanged)
		{
			log.info("Migrated orphaned goals into built-in sections");
			for (Goal g : goals) saveGoal(g);
			saveGoalOrder();
		}
	}

	// -----------------------------------------------------------------
	// Granular per-entity save methods (V2 persistence)
	// -----------------------------------------------------------------

	private void saveGoal(Goal g)
	{
		configManager.setConfiguration(CONFIG_GROUP, GOAL_PREFIX + g.getId(), GSON.toJson(g));
	}

	private void deleteGoalKey(String id)
	{
		configManager.unsetConfiguration(CONFIG_GROUP, GOAL_PREFIX + id);
	}

	private void saveGoalOrder()
	{
		List<String> ids = new ArrayList<>();
		for (Goal g : goals) ids.add(g.getId());
		configManager.setConfiguration(CONFIG_GROUP, GOAL_ORDER_KEY, GSON.toJson(ids));
	}

	private void saveTag(Tag t)
	{
		configManager.setConfiguration(CONFIG_GROUP, TAG_PREFIX + t.getId(), GSON.toJson(t));
	}

	private void deleteTagKey(String id)
	{
		configManager.unsetConfiguration(CONFIG_GROUP, TAG_PREFIX + id);
	}

	private void saveTagIds()
	{
		List<String> ids = new ArrayList<>();
		for (Tag t : tags) ids.add(t.getId());
		configManager.setConfiguration(CONFIG_GROUP, TAG_IDS_KEY, GSON.toJson(ids));
	}

	private void saveSections()
	{
		configManager.setConfiguration(CONFIG_GROUP, SECTIONS_KEY, GSON.toJson(sections));
	}

	private void saveCategoryColors()
	{
		configManager.setConfiguration(CONFIG_GROUP, CATEGORY_COLORS_KEY, GSON.toJson(categoryColors));
	}

	// -----------------------------------------------------------------
	// IfNotSuspended helpers — defer writes during compound operations
	// -----------------------------------------------------------------

	private void saveGoalIfNotSuspended(Goal g)
	{
		if (saveSuspended) { dirtyGoalIds.add(g.getId()); return; }
		saveGoal(g);
	}

	private void deleteGoalKeyIfNotSuspended(String id)
	{
		if (saveSuspended) { dirtyGoalIds.add(id); return; }
		deleteGoalKey(id);
	}

	private void saveGoalOrderIfNotSuspended()
	{
		if (saveSuspended) { goalOrderDirty = true; return; }
		saveGoalOrder();
	}

	private void saveTagIfNotSuspended(Tag t)
	{
		if (saveSuspended) { dirtyTagIds.add(t.getId()); return; }
		saveTag(t);
	}

	private void deleteTagKeyIfNotSuspended(String id)
	{
		if (saveSuspended) { dirtyTagIds.add(id); return; }
		deleteTagKey(id);
	}

	private void saveTagIdsIfNotSuspended()
	{
		if (saveSuspended) { tagIdsDirty = true; return; }
		saveTagIds();
	}

	private void saveSectionsIfNotSuspended()
	{
		if (saveSuspended) { sectionsDirty = true; return; }
		saveSections();
	}

	private void saveCategoryColorsIfNotSuspended()
	{
		if (saveSuspended) { categoryColorsDirty = true; return; }
		saveCategoryColors();
	}

	/**
	 * Convenience save for external callers that mutate Goal/Section/Tag
	 * objects directly (outside GoalStore mutation methods) and then ask
	 * the store to persist. Saves all entities, respecting suspension.
	 */
	public void save()
	{
		if (saveSuspended)
		{
			// Mark everything dirty so resumeSave() flushes it all.
			for (Goal g : goals) dirtyGoalIds.add(g.getId());
			goalOrderDirty = true;
			for (Tag t : tags) dirtyTagIds.add(t.getId());
			tagIdsDirty = true;
			sectionsDirty = true;
			categoryColorsDirty = true;
			return;
		}
		saveNow();
	}

	/**
	 * Mark a goal as dirty for the tracker flush path. Called by
	 * recordGoalProgress so only changed goals are written.
	 */
	public void markGoalDirty(String goalId)
	{
		dirtyGoalIds.add(goalId);
	}

	/**
	 * Save only goals that were marked dirty (by recordGoalProgress).
	 * Much cheaper than saveNow() when only a few goals changed.
	 */
	public void saveDirtyGoals()
	{
		if (dirtyGoalIds.isEmpty()) return;
		long start = System.currentTimeMillis();
		int count = dirtyGoalIds.size();
		for (String id : dirtyGoalIds)
		{
			Goal g = findGoalById(id);
			if (g != null) saveGoal(g);
		}
		dirtyGoalIds.clear();
		saveSections();
		long elapsed = System.currentTimeMillis() - start;
		if (elapsed > 50)
		{
			log.warn("saveDirtyGoals took {}ms ({} goals)", elapsed, count);
		}
	}

	/** Force an immediate save of everything (e.g. on plugin shutdown). */
	public void saveNow()
	{
		for (Goal g : goals) saveGoal(g);
		saveGoalOrder();
		for (Tag t : tags) saveTag(t);
		saveTagIds();
		saveSections();
		saveCategoryColors();
		dirtyGoalIds.clear();
		dirtyTagIds.clear();
		goalOrderDirty = false;
		tagIdsDirty = false;
		sectionsDirty = false;
		categoryColorsDirty = false;
	}

	/** Suspend automatic saves. Call resumeSave() to flush. */
	public void suspendSave()
	{
		saveSuspended = true;
	}

	/** Resume saves and flush any deferred writes. */
	public void resumeSave()
	{
		saveSuspended = false;
		long start = System.currentTimeMillis();
		// Flush dirty goals
		for (String id : dirtyGoalIds)
		{
			Goal g = findGoalById(id);
			if (g != null) saveGoal(g);
			else deleteGoalKey(id);
		}
		dirtyGoalIds.clear();
		if (goalOrderDirty) { saveGoalOrder(); goalOrderDirty = false; }
		// Flush dirty tags
		for (String id : dirtyTagIds)
		{
			Tag t = findTag(id);
			if (t != null) saveTag(t);
			else deleteTagKey(id);
		}
		dirtyTagIds.clear();
		if (tagIdsDirty) { saveTagIds(); tagIdsDirty = false; }
		if (sectionsDirty) { saveSections(); sectionsDirty = false; }
		if (categoryColorsDirty) { saveCategoryColors(); categoryColorsDirty = false; }
		long elapsed = System.currentTimeMillis() - start;
		if (elapsed > 50)
		{
			log.warn("resumeSave took {}ms ({} dirty goals, {} dirty tags)",
				elapsed, dirtyGoalIds.size(), dirtyTagIds.size());
		}
	}

	// ---------------------------------------------------------------------
	// Category color overrides
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
		saveCategoryColorsIfNotSuspended();
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
		goalIndex.put(goal.getId(), goal);
		addToDependentIndex(goal);
		saveGoalIfNotSuspended(goal);
		saveGoalOrderIfNotSuspended();
	}

	/** Register a goal's outgoing edges in the reverse dependentIndex. */
	private void addToDependentIndex(Goal goal)
	{
		if (goal.getRequiredGoalIds() != null)
		{
			for (String reqId : goal.getRequiredGoalIds())
			{
				dependentIndex.computeIfAbsent(reqId, k -> new java.util.HashSet<>())
					.add(goal.getId());
			}
		}
	}

	/**
	 * Insert a goal at a specific index in the flat list and reindex
	 * priorities. Used by the undo path for bulk-remove so
	 * restored goals land at their exact original positions. No-op if a
	 * goal with the same id is already present.
	 */
	public void insertGoalAt(Goal goal, int index)
	{
		if (goal == null) return;
		for (Goal g : goals)
		{
			if (g.getId().equals(goal.getId())) return; // already there
		}
		if (goal.getSectionId() == null)
		{
			goal.setSectionId(getIncompleteSection().getId());
		}
		int clamped = Math.max(0, Math.min(index, goals.size()));
		goals.add(clamped, goal);
		goalIndex.put(goal.getId(), goal);
		addToDependentIndex(goal);
		reindex();
		saveGoalIfNotSuspended(goal);
		saveGoalOrderIfNotSuspended();
	}

	public void removeGoal(String goalId)
	{
		if (goalId == null) return;
		// Scrub any incoming edges before removing the node so
		// other goals don't hold dangling references to the deleted id. This
		// is safe for all existing callers because:
		//   - revert paths for freshly-added goals: the goal has no incoming
		//     edges (no other goal points at it), so this is a no-op
		//   - bulk removals via iteration: scrubbing is incremental — later
		//     batch members get cleaned up as earlier ones are removed
		// Bypass-bridging (connecting predecessors directly to successors
		// around the deleted node) is a separate opt-in via
		// {@link #removeGoalWithBypass}. Plain removeGoal just breaks the
		// chain cleanly.
		// Scrub incoming edges from the dependentIndex and the goals themselves.
		List<Goal> edgeScrubbed = new ArrayList<>();
		for (Goal g : goals)
		{
			if (g.getRequiredGoalIds() != null && g.getRequiredGoalIds().remove(goalId))
			{
				edgeScrubbed.add(g);
				// Also clean reverse index
				java.util.Set<String> deps = dependentIndex.get(goalId);
				if (deps != null) deps.remove(g.getId());
			}
		}
		// Remove outgoing edges from the dependentIndex.
		Goal removed = findGoalById(goalId);
		if (removed != null && removed.getRequiredGoalIds() != null)
		{
			for (String reqId : removed.getRequiredGoalIds())
			{
				java.util.Set<String> deps = dependentIndex.get(reqId);
				if (deps != null) deps.remove(goalId);
			}
		}
		dependentIndex.remove(goalId);
		goals.removeIf(g -> g.getId().equals(goalId));
		goalIndex.remove(goalId);
		reindex();
		for (Goal g : edgeScrubbed) saveGoalIfNotSuspended(g);
		deleteGoalKeyIfNotSuspended(goalId);
		saveGoalOrderIfNotSuspended();
	}

	// ---------------------------------------------------------------------
	// Relations. Goals form a DAG via Goal.requiredGoalIds.
	// Outgoing edges are stored on each Goal; incoming edges ("dependents")
	// are derived at query time by scanning all goals.
	// ---------------------------------------------------------------------

	/**
	 * Add an edge: {@code fromGoalId} requires {@code toGoalId}. Rejects
	 * self-loops, missing goals, duplicate edges, and any edge that would
	 * create a cycle in the DAG.
	 *
	 * @return true if the edge was added, false otherwise
	 */
	public boolean addRequirement(String fromGoalId, String toGoalId)
	{
		if (fromGoalId == null || toGoalId == null) return false;
		if (fromGoalId.equals(toGoalId)) return false; // self-loop
		Goal from = findGoalById(fromGoalId);
		Goal to = findGoalById(toGoalId);
		if (from == null || to == null) return false;
		if (from.getRequiredGoalIds() == null)
		{
			from.setRequiredGoalIds(new ArrayList<>());
		}
		if (from.getRequiredGoalIds().contains(toGoalId)) return false; // idempotent
		if (wouldCreateCycle(fromGoalId, toGoalId)) return false;
		from.getRequiredGoalIds().add(toGoalId);
		dependentIndex.computeIfAbsent(toGoalId, k -> new java.util.HashSet<>()).add(fromGoalId);
		saveGoalIfNotSuspended(from);
		return true;
	}

	/**
	 * Remove the {@code fromGoalId → toGoalId} edge. Returns true iff the
	 * edge existed and was removed.
	 */
	public boolean removeRequirement(String fromGoalId, String toGoalId)
	{
		if (fromGoalId == null || toGoalId == null) return false;
		Goal from = findGoalById(fromGoalId);
		if (from == null || from.getRequiredGoalIds() == null) return false;
		boolean removed = from.getRequiredGoalIds().remove(toGoalId);
		if (removed)
		{
			java.util.Set<String> deps = dependentIndex.get(toGoalId);
			if (deps != null) deps.remove(fromGoalId);
			saveGoalIfNotSuspended(from);
		}
		return removed;
	}

	/**
	 * @return IDs of goals that require the given goal (incoming edges).
	 *   O(1) via the reverse dependentIndex.
	 */
	public List<String> getDependents(String goalId)
	{
		if (goalId == null) return new ArrayList<>();
		java.util.Set<String> deps = dependentIndex.get(goalId);
		return deps != null ? new ArrayList<>(deps) : new ArrayList<>();
	}

	/**
	 * Would adding {@code fromGoalId → toGoalId} create a cycle? A cycle
	 * exists iff there's already a path from {@code toGoalId} back to
	 * {@code fromGoalId} in the current graph. Standard DFS.
	 *
	 * <p>Does NOT check whether the edge already exists — that's a separate
	 * concern handled in {@link #addRequirement}. A duplicate edge is not
	 * a cycle.
	 */
	public boolean wouldCreateCycle(String fromGoalId, String toGoalId)
	{
		if (fromGoalId == null || toGoalId == null) return false;
		if (fromGoalId.equals(toGoalId)) return true; // self-loop IS a cycle
		// DFS from `to`. If we can reach `from`, adding from→to closes a cycle.
		java.util.Set<String> visited = new java.util.HashSet<>();
		java.util.Deque<String> stack = new java.util.ArrayDeque<>();
		stack.push(toGoalId);
		while (!stack.isEmpty())
		{
			String cur = stack.pop();
			if (!visited.add(cur)) continue;
			if (cur.equals(fromGoalId)) return true;
			Goal g = findGoalById(cur);
			if (g != null && g.getRequiredGoalIds() != null)
			{
				for (String next : g.getRequiredGoalIds()) stack.push(next);
			}
		}
		return false;
	}

	/** Internal lookup used by the relations API. Separate from
	 *  {@link #findTag} — different collection. */
	/**
	 * O(1) goal lookup by id.
	 */
	public Goal findGoalById(String id)
	{
		if (id == null) return null;
		return goalIndex.get(id);
	}

	/**
	 * Structural-match search with "satisfies" semantics. Given a Goal
	 * {@code template}, finds an existing goal (across ALL sections) whose
	 * identity fields match and whose target is at least as ambitious as
	 * the template's. Returns the first match or null.
	 *
	 * <p>Satisfies rules per type:
	 * <ul>
	 *   <li><b>SKILL</b>: same {@code skillName}, existing {@code targetValue ≥}
	 *       template's</li>
	 *   <li><b>ITEM_GRIND</b>: same {@code itemId}, existing {@code targetValue ≥}
	 *       template's</li>
	 *   <li><b>QUEST</b> / <b>DIARY</b> / <b>CUSTOM</b>: case-insensitive
	 *       trimmed {@code name} equality</li>
	 *   <li><b>COMBAT_ACHIEVEMENT</b>: {@code caTaskId} equality (if both
	 *       ≥ 0), otherwise falls back to case-insensitive name equality</li>
	 * </ul>
	 *
	 * <p>Used by the find-or-create requirement flow so adding
	 * "HFTD requires 35 Agility" links to an existing 99 Agility goal if
	 * the user already has one, rather than creating a redundant 35 Agility.
	 *
	 * @param template a partially-filled Goal describing WHAT is needed;
	 *                 only identity + target fields are consulted
	 * @return the first matching goal, or null if none
	 */
	public Goal findMatchingGoal(Goal template)
	{
		if (template == null || template.getType() == null) return null;
		for (Goal g : goals)
		{
			if (g.getType() != template.getType()) continue;
			if (matches(g, template)) return g;
		}
		return null;
	}

	private boolean matches(Goal existing, Goal template)
	{
		switch (existing.getType())
		{
			case SKILL:
				if (existing.getSkillName() == null || template.getSkillName() == null) return false;
				if (!existing.getSkillName().equalsIgnoreCase(template.getSkillName())) return false;
				// Satisfies: existing target must be ≥ template's target.
				return existing.getTargetValue() >= template.getTargetValue();
			case ITEM_GRIND:
				if (existing.getItemId() != template.getItemId()) return false;
				return existing.getTargetValue() >= template.getTargetValue();
			case COMBAT_ACHIEVEMENT:
				// Prefer caTaskId when both are set; fall back to name.
				if (existing.getCaTaskId() >= 0 && template.getCaTaskId() >= 0)
				{
					return existing.getCaTaskId() == template.getCaTaskId();
				}
				return namesEqual(existing.getName(), template.getName());
			case QUEST:
			case DIARY:
			case CUSTOM:
				return namesEqual(existing.getName(), template.getName());
			default:
				return false;
		}
	}

	private static boolean namesEqual(String a, String b)
	{
		if (a == null || b == null) return false;
		return a.trim().equalsIgnoreCase(b.trim());
	}

	/**
	 * Snapshot of state captured by {@link #removeGoalWithBypass} before the
	 * delete, sufficient for a caller to fully restore the graph via undo.
	 *
	 * <p>Revert procedure:
	 * <ol>
	 *   <li>For each {@code [from, to]} in {@link #addedBypassEdges}, call
	 *       {@link #removeRequirement}</li>
	 *   <li>Call {@link #insertGoalAt}({@link #goal}, {@link #originalIndex})</li>
	 *   <li>The deleted goal's OWN outgoing edges are preserved in
	 *       {@code goal.requiredGoalIds} because we capture the snapshot
	 *       BEFORE deleting — re-inserting the goal restores them automatically</li>
	 *   <li>For each id in {@link #predecessors}, call
	 *       {@link #addRequirement}(predId, goal.id) to rewire incoming edges</li>
	 * </ol>
	 */
	public static final class RemoveGoalBypassSnapshot
	{
		/** The deleted goal, with its full state including {@code requiredGoalIds}
		 *  captured at delete time. */
		public final Goal goal;
		/** Original index in the flat {@code goals} list (for {@link #insertGoalAt}
		 *  to preserve exact ordering through the stable-sort tiebreak). */
		public final int originalIndex;
		/** Goal IDs that had the deleted goal in their {@code requiredGoalIds}
		 *  at delete time (incoming edges). */
		public final List<String> predecessors;
		/** [from, to] pairs of edges ACTUALLY added as bypasses — excludes
		 *  edges that already existed independently (dedupe). Revert must
		 *  remove exactly these, no more, no less. */
		public final List<String[]> addedBypassEdges;

		RemoveGoalBypassSnapshot(Goal goal, int originalIndex, List<String> predecessors,
			List<String[]> addedBypassEdges)
		{
			this.goal = goal;
			this.originalIndex = originalIndex;
			this.predecessors = predecessors;
			this.addedBypassEdges = addedBypassEdges;
		}
	}

	/**
	 * Remove a goal using doubly-linked-list-style bridging: before deleting
	 * the node, every predecessor is connected directly to every successor
	 * so the chain isn't broken. Returns a snapshot sufficient for undo.
	 *
	 * <p>For the DAG {@code P1,P2 → D → S1,S2}, deleting {@code D} adds
	 * bypass edges {@code P1→S1, P1→S2, P2→S1, P2→S2} (Cartesian product),
	 * then removes {@code D}. Edges that already existed independently are
	 * not duplicated.
	 *
	 * <p>Bypass-edge addition is proven not to create cycles: any resulting
	 * cycle would require a path {@code Si → ... → Pj} in the existing
	 * graph, which combined with {@code Pj → D → Si} would have been a
	 * cycle before the delete. Since the invariant is a DAG, no such path
	 * exists.
	 *
	 * @return a snapshot for undo, or null if the goal id isn't found
	 */
	public RemoveGoalBypassSnapshot removeGoalWithBypass(String goalId)
	{
		if (goalId == null) return null;
		Goal deleted = findGoalById(goalId);
		if (deleted == null) return null;

		// 1. Snapshot the deleted goal's index in the flat list.
		int originalIndex = -1;
		for (int i = 0; i < goals.size(); i++)
		{
			if (goalId.equals(goals.get(i).getId())) { originalIndex = i; break; }
		}

		// 2. Collect predecessors via the reverse index.
		List<String> predecessors = getDependents(goalId);

		// 3. Successors are the deleted goal's own outgoing edges.
		List<String> successors = deleted.getRequiredGoalIds() != null
			? new ArrayList<>(deleted.getRequiredGoalIds())
			: new ArrayList<>();

		// 4. Compute and apply bypass edges (Cartesian product, deduped).
		List<String[]> addedBypassEdges = new ArrayList<>();
		for (String predId : predecessors)
		{
			Goal pred = findGoalById(predId);
			if (pred == null) continue;
			if (pred.getRequiredGoalIds() == null)
			{
				pred.setRequiredGoalIds(new ArrayList<>());
			}
			for (String succId : successors)
			{
				if (pred.getRequiredGoalIds().contains(succId)) continue;
				if (predId.equals(succId)) continue;
				pred.getRequiredGoalIds().add(succId);
				dependentIndex.computeIfAbsent(succId, k -> new java.util.HashSet<>()).add(predId);
				addedBypassEdges.add(new String[]{predId, succId});
			}
		}

		// 5. Scrub incoming edges: remove the deleted id from each predecessor's
		//    requiredGoalIds and the dependentIndex.
		for (String predId : predecessors)
		{
			Goal pred = findGoalById(predId);
			if (pred != null && pred.getRequiredGoalIds() != null)
			{
				pred.getRequiredGoalIds().remove(goalId);
			}
		}
		dependentIndex.remove(goalId);

		// 6. Remove outgoing edges from the dependentIndex.
		for (String succId : successors)
		{
			java.util.Set<String> deps = dependentIndex.get(succId);
			if (deps != null) deps.remove(goalId);
		}

		Goal snapshotGoal = deleted;

		// 7. Remove the goal from the flat list.
		goals.removeIf(g -> g.getId().equals(goalId));
		goalIndex.remove(goalId);
		reindex();
		// Save affected predecessors (bypass edges + scrubbed edges)
		for (String predId : predecessors)
		{
			Goal pred = findGoalById(predId);
			if (pred != null) saveGoalIfNotSuspended(pred);
		}
		deleteGoalKeyIfNotSuspended(goalId);
		saveGoalOrderIfNotSuspended();

		return new RemoveGoalBypassSnapshot(snapshotGoal, originalIndex, predecessors, addedBypassEdges);
	}

	public void updateGoal(Goal goal)
	{
		for (int i = 0; i < goals.size(); i++)
		{
			if (goals.get(i).getId().equals(goal.getId()))
			{
				goals.set(i, goal);
				goalIndex.put(goal.getId(), goal);
				break;
			}
		}
		saveGoalIfNotSuspended(goal);
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
		List<Goal> movedGoals = new ArrayList<>();
		for (Goal goal : goals)
		{
			boolean isComplete = goal.isComplete();
			String currentSid = goal.getSectionId();
			if (isComplete && !completedId.equals(currentSid))
			{
				goal.setSectionId(completedId);
				anyMoved = true;
				movedGoals.add(goal);
			}
			else if (!isComplete && completedId.equals(currentSid))
			{
				// Defensive: if a goal un-completes (e.g. value changes via custom toggle),
				// return it to the Incomplete section.
				goal.setSectionId(incompleteId);
				anyMoved = true;
				movedGoals.add(goal);
			}
		}
		if (anyMoved)
		{
			normalizeOrder();
			for (Goal g : movedGoals) saveGoalIfNotSuspended(g);
			saveGoalOrderIfNotSuspended();
		}
		return anyMoved;
	}

	public void setGoals(List<Goal> newGoals)
	{
		goals = new ArrayList<>(newGoals);
		reindex();
		for (Goal g : goals) saveGoalIfNotSuspended(g);
		saveGoalOrderIfNotSuspended();
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
		return sectionIndex.get(sectionId);
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
		sectionIndex.put(created.getId(), created);
		renumberUserSections();
		saveSectionsIfNotSuspended();
		return created;
	}

	/**
	 * Re-create a user section with a SPECIFIC id. Used by the undo path
	 * for deleteUserSection so the section comes back with the same id any
	 * existing references (selected goals, command history) used. No-op if
	 * a section with that id already exists.
	 */
	public Section recreateUserSection(String sectionId, String name)
	{
		if (findSection(sectionId) != null) return findSection(sectionId);
		String trimmed = name != null ? name.trim() : "";
		Section recreated = Section.builder()
			.id(sectionId)
			.name(trimmed)
			.order(nextUserSectionOrder())
			.builtInKind(null)
			.build();
		sections.add(recreated);
		sectionIndex.put(recreated.getId(), recreated);
		renumberUserSections();
		saveSectionsIfNotSuspended();
		return recreated;
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
		saveSectionsIfNotSuspended();
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
		List<Goal> movedGoals = new ArrayList<>();
		for (Goal g : goals)
		{
			if (sectionId.equals(g.getSectionId()))
			{
				g.setSectionId(incompleteId);
				movedGoals.add(g);
			}
		}
		sections.removeIf(s -> sectionId.equals(s.getId()));
		sectionIndex.remove(sectionId);
		renumberUserSections();
		normalizeOrder();
		reconcileCompletedSection();
		for (Goal g : movedGoals) saveGoalIfNotSuspended(g);
		saveSectionsIfNotSuspended();
		saveGoalOrderIfNotSuspended();
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
		saveSectionsIfNotSuspended();
		saveGoalOrderIfNotSuspended();
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
		saveGoalIfNotSuspended(goal);
		saveGoalOrderIfNotSuspended();
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
		for (String id : doomed) sectionIndex.remove(id);
		renumberUserSections();
		normalizeOrder();
		reconcileCompletedSection();
		// Goals may have been reassigned by both the section delete and reconcile
		for (Goal g : goals) saveGoalIfNotSuspended(g);
		saveSectionsIfNotSuspended();
		saveGoalOrderIfNotSuspended();
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
	// Tag entity CRUD
	// ---------------------------------------------------------------------

	private static final int MAX_TAG_LABEL_LENGTH = 30;

	public List<Tag> getTags()
	{
		return tags;
	}

	public Tag findTag(String tagId)
	{
		if (tagId == null) return null;
		return tagIndex.get(tagId);
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
		tagIndex.put(created.getId(), created);
		saveTagIfNotSuspended(created);
		saveTagIdsIfNotSuspended();
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
		tagIndex.put(created.getId(), created);
		saveTagIfNotSuspended(created);
		saveTagIdsIfNotSuspended();
		return created;
	}

	/**
	 * One-time migration primitive: move a system tag from one category to
	 * another, rewriting all goal references so existing goals end up pointing
	 * at the new (label, toCategory) Tag entity.
	 *
	 * <p>Used by {@code seedCanonicalSystemTags} to apply category
	 * reclassifications (e.g. Tempoross + Wintertodt BOSS → MINIGAME without
	 * leaving orphaned BOSS entities behind).
	 *
	 * <p>Merge semantics when a destination tag already exists:
	 * <ul>
	 *   <li>All goals referencing the old tag id are rewritten to reference
	 *       the destination tag id (both {@code tagIds} and {@code defaultTagIds}).</li>
	 *   <li>User customizations on the old tag (iconKey, colorRgb) are
	 *       preserved only if the destination tag doesn't already have them
	 *       set — the destination always wins a conflict.</li>
	 *   <li>The old tag entity is deleted.</li>
	 * </ul>
	 *
	 * <p>If no tag with ({@code label}, {@code fromCategory}) exists, this is
	 * a no-op and returns false. If a destination doesn't exist yet, it's
	 * created with customizations copied from the source.
	 *
	 * <p>Idempotent: safe to call on every plugin start. After the first run,
	 * subsequent calls find no source tag and no-op.
	 *
	 * @return true if any change was made (tag recategorized and/or goals rewritten)
	 */
	public boolean recategorizeSystemTag(String label, TagCategory fromCategory, TagCategory toCategory)
	{
		if (label == null || fromCategory == null || toCategory == null) return false;
		if (fromCategory == toCategory) return false;
		Tag source = findTagByLabel(label, fromCategory);
		if (source == null) return false; // already migrated, or never existed

		Tag dest = findTagByLabel(label, toCategory);
		if (dest == null)
		{
			// No destination exists — just flip the category in place and
			// preserve all customizations.
			source.setCategory(toCategory);
			saveTagIfNotSuspended(source);
			return true;
		}

		// Destination exists. Merge: preserve source customizations only where
		// destination is unset, then rewrite references and drop the source.
		if ((dest.getIconKey() == null || dest.getIconKey().isEmpty())
			&& source.getIconKey() != null && !source.getIconKey().isEmpty())
		{
			dest.setIconKey(source.getIconKey());
		}
		if (dest.getColorRgb() < 0 && source.getColorRgb() >= 0)
		{
			dest.setColorRgb(source.getColorRgb());
		}

		String sourceId = source.getId();
		String destId = dest.getId();
		for (Goal g : goals)
		{
			if (g.getTagIds() != null)
			{
				java.util.List<String> ids = g.getTagIds();
				for (int i = 0; i < ids.size(); i++)
				{
					if (sourceId.equals(ids.get(i)))
					{
						// Dedupe: if destId is already present, just remove
						// the source entry instead of creating a duplicate.
						if (ids.contains(destId)) ids.remove(i--);
						else ids.set(i, destId);
					}
				}
			}
			if (g.getDefaultTagIds() != null)
			{
				java.util.List<String> ids = g.getDefaultTagIds();
				for (int i = 0; i < ids.size(); i++)
				{
					if (sourceId.equals(ids.get(i)))
					{
						if (ids.contains(destId)) ids.remove(i--);
						else ids.set(i, destId);
					}
				}
			}
		}
		tags.remove(source);
		tagIndex.remove(source.getId());
		// Save affected goals (tag references rewritten) + tags
		for (Goal g : goals) saveGoalIfNotSuspended(g);
		saveTagIfNotSuspended(dest);
		deleteTagKeyIfNotSuspended(sourceId);
		saveTagIdsIfNotSuspended();
		return true;
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
		saveTagIfNotSuspended(t);
		return true;
	}

	/**
	 * Recolor an individual tag. Only meaningful for tags in the
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
		saveTagIfNotSuspended(t);
		return true;
	}

	/**
	 * Set or clear an icon on any tag. System tags can have
	 * icons set — used by the seed to attach skill icons to the canonical
	 * SKILLING tags. Pass null or empty to clear.
	 *
	 * @return true if the icon changed
	 */
	public boolean setTagIcon(String tagId, String iconKey)
	{
		Tag t = findTag(tagId);
		if (t == null) return false;
		String normalized = (iconKey == null || iconKey.trim().isEmpty()) ? null : iconKey.trim();
		if (java.util.Objects.equals(t.getIconKey(), normalized)) return false;
		t.setIconKey(normalized);
		saveTagIfNotSuspended(t);
		return true;
	}

	/** Equivalent to {@link #setTagIcon(String, String)} with null. */
	public boolean clearTagIcon(String tagId)
	{
		return setTagIcon(tagId, null);
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
		List<Goal> affectedGoals = new ArrayList<>();
		for (Goal g : goals)
		{
			boolean changed = false;
			if (g.getTagIds() != null) changed |= g.getTagIds().remove(tagId);
			if (g.getDefaultTagIds() != null) changed |= g.getDefaultTagIds().remove(tagId);
			if (changed) affectedGoals.add(g);
		}
		tags.removeIf(x -> tagId.equals(x.getId()));
		tagIndex.remove(tagId);
		for (Goal g : affectedGoals) saveGoalIfNotSuspended(g);
		deleteTagKeyIfNotSuspended(tagId);
		saveTagIdsIfNotSuspended();
		return true;
	}

	/**
	 * Re-add an already-built Tag entity (preserving its id). Used by the
	 * undo path for deleteTag so the restored tag matches any still-cached
	 * references. No-op if a tag with that id already exists.
	 */
	public void recreateTag(Tag tag)
	{
		if (tag == null || findTag(tag.getId()) != null) return;
		tags.add(tag);
		tagIndex.put(tag.getId(), tag);
		saveTagIfNotSuspended(tag);
		saveTagIdsIfNotSuspended();
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
		saveGoalOrderIfNotSuspended();
	}

	private void reindex()
	{
		for (int i = 0; i < goals.size(); i++)
		{
			goals.get(i).setPriority(i);
		}
	}
}
