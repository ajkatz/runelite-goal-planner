package com.goalplanner.api;

import com.goalplanner.data.WikiCaRepository;
import com.goalplanner.model.Goal;
import com.goalplanner.persistence.GoalStore;
import com.goalplanner.service.GoalReorderingService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import net.runelite.client.game.ItemManager;

/**
 * Default implementation of {@link GoalPlannerApi}. Bound to the public
 * interface in {@link com.goalplanner.GoalPlannerPlugin#configure}.
 *
 * <p>This is a thin facade: every method delegates to a focused service class.
 * Only undo/redo, selection, and infrastructure (constructors, findGoal,
 * executeCommand) live here.
 */
@Slf4j
@Singleton
public class GoalPlannerApiImpl implements GoalPlannerApi, GoalPlannerInternalApi
{
	static final int QUEST_SPRITE_ID = 899;
	static final int F2P_TAG_COLOR_RGB = (160 << 16) | (160 << 8) | 160;
	static final String LAMP_ICON_KEY = "lamp";

	final GoalStore goalStore;
	final GoalReorderingService reorderingService;
	final ItemManager itemManager;
	final WikiCaRepository wikiCaRepository;
	/** Used by {@link RelationService#resolveQuestRequirements} to filter requirements
	 *  against live player state. May be null in tests that don't touch
	 *  that method. */
	final net.runelite.api.Client client;

	/** Optional UI-refresh hook the plugin sets after the panel is constructed. */
	Runnable onGoalsChanged = () -> {};
	/** Lightweight selection-only refresh — avoids full rebuild for selection changes. */
	Runnable onSelectionChanged = () -> {};

	/** Ephemeral selection set — not persisted, lost on plugin restart. */
	final java.util.Set<String> selectedGoalIds = new java.util.LinkedHashSet<>();

	/** Undo/redo history. Session-only. Tracker-driven mutations
	 *  bypass this — only user actions routed through {@link #executeCommand}
	 *  appear in history. */
	private final com.goalplanner.command.CommandHistory commandHistory =
		new com.goalplanner.command.CommandHistory();

	private final GoalCreationService creationService;
	final TagService tagService;
	private final GoalMutationService mutationService;
	private final GoalQueryService queryService;
	final SectionService sectionService;
	private final RelationService relationService;

	@Inject
	public GoalPlannerApiImpl(
		GoalStore goalStore,
		GoalReorderingService reorderingService,
		ItemManager itemManager,
		WikiCaRepository wikiCaRepository,
		net.runelite.api.Client client)
	{
		this.goalStore = goalStore;
		this.reorderingService = reorderingService;
		this.itemManager = itemManager;
		this.wikiCaRepository = wikiCaRepository;
		this.client = client;
		this.creationService = new GoalCreationService(this);
		this.tagService = new TagService(this);
		this.mutationService = new GoalMutationService(this);
		this.queryService = new GoalQueryService(this);
		this.sectionService = new SectionService(this);
		this.relationService = new RelationService(this);
	}

	/**
	 * Test-friendly constructor that omits the {@link net.runelite.api.Client}.
	 */
	public GoalPlannerApiImpl(
		GoalStore goalStore,
		GoalReorderingService reorderingService,
		ItemManager itemManager,
		WikiCaRepository wikiCaRepository)
	{
		this(goalStore, reorderingService, itemManager, wikiCaRepository, null);
	}

	/**
	 * Plugin-internal hook for the UI to register a refresh callback. Not part
	 * of the public API; called by GoalPlannerPlugin during startup.
	 */
	public void setOnGoalsChanged(Runnable callback)
	{
		this.onGoalsChanged = callback != null ? callback : () -> {};
	}

	public void setOnSelectionChanged(Runnable callback)
	{
		this.onSelectionChanged = callback != null ? callback : () -> {};
	}

	// =====================================================================
	// Goal creation delegations
	// =====================================================================

	@Override public String addSkillGoal(Skill skill, int targetXp) { return creationService.addSkillGoal(skill, targetXp); }
	@Override public String addSkillGoalForLevel(Skill skill, int level) { return creationService.addSkillGoalForLevel(skill, level); }
	@Override public String addItemGoal(int itemId, int targetQuantity) { return creationService.addItemGoal(itemId, targetQuantity); }
	@Override public String addQuestGoal(Quest quest) { return creationService.addQuestGoal(quest); }
	// Not part of the published GoalPlannerApi — the public addQuestGoal / addDiaryGoal
	// now auto-resolve prereqs internally. Kept public on the impl for tests and
	// internal callers that want to supply pre-computed templates directly.
	public String addQuestGoalWithPrereqs(Quest quest, java.util.List<Goal> prereqTemplates) { return creationService.addQuestGoalWithPrereqs(quest, prereqTemplates); }
	@Override public String addDiaryGoal(String areaDisplayName, DiaryTier tier) { return creationService.addDiaryGoal(areaDisplayName, tier); }
	public String addDiaryGoalWithPrereqs(String areaDisplayName, DiaryTier tier, com.goalplanner.data.DiaryRequirementResolver.Resolved resolved) { return creationService.addDiaryGoalWithPrereqs(areaDisplayName, tier, resolved); }
	@Override public String addCombatAchievementGoal(int caTaskId) { return creationService.addCombatAchievementGoal(caTaskId); }
	@Override public String addBossGoal(String bossName, int targetKills) { return creationService.addBossGoal(bossName, targetKills); }
	@Override public String addAccountGoal(String metricName, int target) { return creationService.addAccountGoal(metricName, target); }
	public String addCustomGoal(String name, String description) { return creationService.addCustomGoal(name, description); }

	// =====================================================================
	// Query delegations
	// =====================================================================

	@Override public List<GoalView> queryAllGoals() { return queryService.queryAllGoals(); }
	public GoalView queryGoalView(String goalId) { return queryService.queryGoalView(goalId); }
	@Override public List<GoalView> searchGoals(String query) { return queryService.searchGoals(query); }
	@Override public List<SectionView> queryAllSections() { return queryService.queryAllSections(); }
	@Override public java.util.List<GoalView> queryGoalsTopologicallySorted(String sectionId) { return queryService.queryGoalsTopologicallySorted(sectionId); }
	@Override public java.util.Map<String, java.util.List<GoalView>> queryAllGoalsTopologicallySorted() { return queryService.queryAllGoalsTopologicallySorted(); }

	// =====================================================================
	// Mutation delegations
	// =====================================================================

	@Override public boolean removeGoal(String goalId) { return mutationService.removeGoal(goalId); }
	@Override public boolean editCustomGoal(String goalId, String newName, String newDescription) { return mutationService.editCustomGoal(goalId, newName, newDescription); }
	@Override public boolean markGoalComplete(String goalId) { return mutationService.markGoalComplete(goalId); }
	@Override public boolean markGoalIncomplete(String goalId) { return mutationService.markGoalIncomplete(goalId); }
	@Override public boolean changeTarget(String goalId, int newTarget) { return mutationService.changeTarget(goalId, newTarget); }
	@Override public boolean recordGoalProgress(String goalId, int newValue) { return mutationService.recordGoalProgress(goalId, newValue); }
	@Override public boolean isGoalOverridden(String goalId) { return mutationService.isGoalOverridden(goalId); }
	@Override public int bulkRestoreDefaults(java.util.Set<String> goalIds) { return mutationService.bulkRestoreDefaults(goalIds); }
	@Override public int bulkRemoveGoals(java.util.Set<String> goalIds) { return mutationService.bulkRemoveGoals(goalIds); }
	@Override public int bulkMoveGoalsToSection(java.util.Set<String> goalIds, String targetSectionId) { return mutationService.bulkMoveGoalsToSection(goalIds, targetSectionId); }
	@Override public void removeAllGoals() { mutationService.removeAllGoals(); }
	@Override public boolean moveGoal(String goalId, int newGlobalIndex) { return mutationService.moveGoal(goalId, newGlobalIndex); }
	@Override public boolean positionGoalInSection(String goalId, String sectionId, int positionInSection) { return mutationService.positionGoalInSection(goalId, sectionId, positionInSection); }
	@Override public boolean setGoalOptional(String goalId, boolean optional) { return mutationService.setGoalOptional(goalId, optional); }

	// =====================================================================
	// Tag delegations
	// =====================================================================

	@Override public boolean addTag(String goalId, String label) { return tagService.addTag(goalId, label); }
	@Override public boolean removeTag(String goalId, String label) { return tagService.removeTag(goalId, label); }
	@Override public boolean restoreDefaultTags(String goalId) { return tagService.restoreDefaultTags(goalId); }
	@Override public boolean addTagWithCategory(String goalId, String label, String categoryName) { return tagService.addTagWithCategory(goalId, label, categoryName); }
	@Override public List<TagView> queryAllTags() { return tagService.queryAllTags(); }
	@Override public String createUserTag(String label, String categoryName) { return tagService.createUserTag(label, categoryName); }
	@Override public boolean renameTag(String tagId, String newLabel) { return tagService.renameTag(tagId, newLabel); }
	@Override public boolean recolorTag(String tagId, int colorRgb) { return tagService.recolorTag(tagId, colorRgb); }
	@Override public boolean deleteTag(String tagId) { return tagService.deleteTag(tagId); }
	@Override public boolean setCategoryColor(String categoryName, int colorRgb) { return tagService.setCategoryColor(categoryName, colorRgb); }
	@Override public boolean resetCategoryColor(String categoryName) { return tagService.resetCategoryColor(categoryName); }
	@Override public int getCategoryColor(String categoryName) { return tagService.getCategoryColor(categoryName); }
	@Override public int getCategoryDefaultColor(String categoryName) { return tagService.getCategoryDefaultColor(categoryName); }
	@Override public boolean isCategoryColorOverridden(String categoryName) { return tagService.isCategoryColorOverridden(categoryName); }
	@Override public boolean setTagIcon(String tagId, String iconKey) { return tagService.setTagIcon(tagId, iconKey); }
	@Override public boolean clearTagIcon(String tagId) { return tagService.clearTagIcon(tagId); }
	@Override public boolean setTagColor(String goalId, String tagLabel, int colorRgb) { return tagService.setTagColor(goalId, tagLabel, colorRgb); }
	@Override public int bulkRemoveTagFromGoals(java.util.Set<String> goalIds, String tagId) { return tagService.bulkRemoveTagFromGoals(goalIds, tagId); }
	@Override public List<TagRemovalOption> getRemovableTagsForSelection(java.util.Set<String> goalIds) { return tagService.getRemovableTagsForSelection(goalIds); }

	// =====================================================================
	// Section delegations
	// =====================================================================

	@Override public String createSection(String name) { return sectionService.createSection(name); }
	@Override public boolean renameSection(String sectionId, String newName) { return sectionService.renameSection(sectionId, newName); }
	@Override public boolean deleteSection(String sectionId) { return sectionService.deleteSection(sectionId); }
	@Override public boolean reorderSection(String sectionId, int newUserIndex) { return sectionService.reorderSection(sectionId, newUserIndex); }
	@Override public boolean moveGoalToSection(String goalId, String sectionId) { return sectionService.moveGoalToSection(goalId, sectionId); }
	@Override public int removeAllUserSections() { return sectionService.removeAllUserSections(); }
	@Override public boolean setSectionCollapsed(String sectionId, boolean collapsed) { return sectionService.setSectionCollapsed(sectionId, collapsed); }
	@Override public boolean toggleSectionCollapsed(String sectionId) { return sectionService.toggleSectionCollapsed(sectionId); }
	@Override public boolean setSectionColor(String sectionId, int colorRgb) { return sectionService.setSectionColor(sectionId, colorRgb); }
	@Override public boolean setGoalColor(String goalId, int colorRgb) { return sectionService.setGoalColor(goalId, colorRgb); }

	// =====================================================================
	// Relation delegations
	// =====================================================================

	@Override public boolean addRequirement(String fromGoalId, String toGoalId) { return relationService.addRequirement(fromGoalId, toGoalId); }
	@Override public boolean removeRequirement(String fromGoalId, String toGoalId) { return relationService.removeRequirement(fromGoalId, toGoalId); }
	public boolean addOrRequirement(String fromGoalId, String toGoalId) { return relationService.addOrRequirement(fromGoalId, toGoalId); }
	@Override public java.util.List<String> getRequirements(String goalId) { return relationService.getRequirements(goalId); }
	@Override public java.util.List<String> getDependents(String goalId) { return relationService.getDependents(goalId); }
	@Override public FindOrCreateResult findOrCreateRequirement(Goal template, String preferredSectionId) { return relationService.findOrCreateRequirement(template, preferredSectionId); }
	@Override public com.goalplanner.data.QuestRequirementResolver.Resolved resolveQuestRequirements(Quest quest) { return relationService.resolveQuestRequirements(quest); }

	// =====================================================================
	// Undo / redo
	// =====================================================================

	/**
	 * Internal entry point for user-mutation API methods. Runs the command
	 * via {@link com.goalplanner.command.CommandHistory#execute} so it lands on the undo stack.
	 */
	public boolean executeCommand(com.goalplanner.command.Command cmd)
	{
		boolean ok = commandHistory.execute(cmd);
		if (ok) fireIfNotInCompound();
		return ok;
	}

	/**
	 * Fire the UI refresh callback unless we're inside a compound
	 * transaction. All mutation paths should use this instead of calling
	 * onGoalsChanged.run() directly to avoid per-sub-command rebuilds.
	 */
	void fireIfNotInCompound()
	{
		if (!commandHistory.isInCompound()) onGoalsChanged.run();
	}

	/** Fire the UI refresh callback (goes through the debounce timer). */
	public void fireGoalsChanged()
	{
		onGoalsChanged.run();
	}

	@Override public boolean canUndo() { return commandHistory.canUndo(); }
	@Override public boolean canRedo() { return commandHistory.canRedo(); }
	@Override public String peekUndoDescription() { return commandHistory.peekUndoDescription(); }
	@Override public String peekRedoDescription() { return commandHistory.peekRedoDescription(); }

	@Override
	public boolean undo()
	{
		goalStore.suspendSave();
		boolean ok = commandHistory.undo();
		goalStore.resumeSave();
		if (ok) onGoalsChanged.run();
		return ok;
	}

	@Override
	public boolean redo()
	{
		goalStore.suspendSave();
		boolean ok = commandHistory.redo();
		goalStore.resumeSave();
		if (ok) onGoalsChanged.run();
		return ok;
	}

	@Override public void beginCompound(String description)
	{
		commandHistory.beginCompound(description);
		goalStore.suspendSave();
	}
	@Override public void endCompound()
	{
		commandHistory.endCompound();
		if (!commandHistory.isInCompound())
		{
			goalStore.resumeSave();
			onGoalsChanged.run();
		}
	}

	// =====================================================================
	// Selection (Phase 5) — ephemeral, not persisted
	// =====================================================================

	@Override
	public boolean replaceGoalSelection(java.util.Collection<String> goalIds)
	{
		log.debug("API.internal replaceGoalSelection(size={})", goalIds == null ? 0 : goalIds.size());
		java.util.Set<String> next = new java.util.LinkedHashSet<>();
		if (goalIds != null) next.addAll(goalIds);
		if (next.equals(selectedGoalIds)) return false;
		selectedGoalIds.clear();
		selectedGoalIds.addAll(next);
		if (!commandHistory.isInCompound()) onSelectionChanged.run();
		return true;
	}

	@Override
	public boolean addToGoalSelection(String goalId)
	{
		log.debug("API.internal addToGoalSelection(goalId={})", goalId);
		if (goalId == null) return false;
		if (!selectedGoalIds.add(goalId)) return false;
		if (!commandHistory.isInCompound()) onSelectionChanged.run();
		return true;
	}

	@Override
	public boolean removeFromGoalSelection(String goalId)
	{
		log.debug("API.internal removeFromGoalSelection(goalId={})", goalId);
		if (goalId == null) return false;
		if (!selectedGoalIds.remove(goalId)) return false;
		if (!commandHistory.isInCompound()) onSelectionChanged.run();
		return true;
	}

	@Override
	public boolean clearGoalSelection()
	{
		log.debug("API.internal clearGoalSelection()");
		if (selectedGoalIds.isEmpty()) return false;
		selectedGoalIds.clear();
		if (!commandHistory.isInCompound()) onSelectionChanged.run();
		return true;
	}

	@Override
	public java.util.Set<String> getSelectedGoalIds()
	{
		return java.util.Collections.unmodifiableSet(selectedGoalIds);
	}

	@Override
	public int selectAllInSection(String sectionId)
	{
		log.debug("API.internal selectAllInSection(sectionId={})", sectionId);
		if (sectionId == null) return 0;
		int added = 0;
		for (Goal g : goalStore.getGoals())
		{
			if (sectionId.equals(g.getSectionId()))
			{
				if (selectedGoalIds.add(g.getId())) added++;
			}
		}
		if (added > 0 && !commandHistory.isInCompound()) onSelectionChanged.run();
		return added;
	}

	@Override
	public int deselectAllInSection(String sectionId)
	{
		log.debug("API.internal deselectAllInSection(sectionId={})", sectionId);
		if (sectionId == null) return 0;
		int removed = 0;
		for (Goal g : goalStore.getGoals())
		{
			if (sectionId.equals(g.getSectionId()))
			{
				if (selectedGoalIds.remove(g.getId())) removed++;
			}
		}
		if (removed > 0 && !commandHistory.isInCompound()) onSelectionChanged.run();
		return removed;
	}

	// =====================================================================
	// Internal helpers
	// =====================================================================

	Goal findGoal(String goalId)
	{
		return goalStore.findGoalById(goalId);
	}
}
