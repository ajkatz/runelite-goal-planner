package com.goalplanner.api;

import java.util.List;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

/**
 * Public API for the Goal Planner plugin.
 *
 * Consumer plugins integrate by:
 * <ol>
 *   <li>Annotating their plugin class with
 *       {@code @PluginDependency(com.goalplanner.GoalPlannerPlugin.class)}</li>
 *   <li>Injecting this interface via {@code @Inject GoalPlannerApi goalTrackerApi}</li>
 *   <li>Calling {@code add*Goal(...)} methods to create goals programmatically</li>
 * </ol>
 *
 * <p>All {@code add*Goal} methods return the created goal's id (a UUID string)
 * on success, or {@code null} if the inputs failed validation. If a duplicate
 * goal already exists (same skill, item, quest, etc.) the existing goal's id is
 * returned and no new goal is created. Calls are idempotent and safe to retry.
 *
 * <p>This interface is intentionally minimal and doesn't expose the internal
 * {@code Goal} model, so future model changes don't break consumers.
 */
public interface GoalPlannerApi
{
	/**
	 * Add a skill goal targeting a raw XP amount. Canonical form — all skill
	 * goals are stored internally as XP targets.
	 *
	 * @param skill    the skill to track (must not be null)
	 * @param targetXp the target XP (1 .. 200,000,000)
	 * @return the created or existing goal's id, or {@code null} if validation failed
	 */
	String addSkillGoal(Skill skill, int targetXp);

	/**
	 * Convenience: add a skill goal targeting a specific level. Resolves to the
	 * XP threshold via {@link net.runelite.api.Experience#getXpForLevel(int)} and
	 * delegates to {@link #addSkillGoal(Skill, int)}.
	 *
	 * @param skill the skill to track (must not be null)
	 * @param level the target level (1 .. 126; 1-99 normal, 100-126 virtual)
	 * @return the created or existing goal's id, or {@code null} if validation failed
	 */
	String addSkillGoalForLevel(Skill skill, int level);

	/**
	 * Add an item goal by item id and target quantity. The item id corresponds
	 * to OSRS item ids (see {@link net.runelite.api.gameval.ItemID}).
	 *
	 * @param itemId         the item id (must be &gt; 0)
	 * @param targetQuantity the target count (must be &gt; 0)
	 * @return the created or existing goal's id, or {@code null} if validation failed
	 */
	String addItemGoal(int itemId, int targetQuantity);

	/**
	 * Add a quest goal. Auto-tracks completion via {@code Quest.getState(client)}.
	 *
	 * @param quest the quest (must not be null)
	 * @return the created or existing goal's id, or {@code null} if validation failed
	 */
	String addQuestGoal(Quest quest);

	/**
	 * Add a quest goal along with a batch of prerequisite-goal templates,
	 * all under a single undo entry.
	 *
	 * <p>For each template the API calls the internal
	 * {@code findOrCreateRequirement} flow: if an existing goal
	 * structurally satisfies the template (same skill at or above the
	 * target level, same quest name, etc.) the existing goal is linked
	 * as a requirement; otherwise a new seed goal is created with
	 * {@code autoSeeded=true} and linked. The whole gesture — quest
	 * goal creation + each find-or-create + each requirement edge —
	 * collapses into one undoable entry.
	 *
	 * <p>Templates should already be filtered against live player state
	 * (see {@code QuestRequirementResolver}); this method does not
	 * inspect the {@link Client}.
	 *
	 * @param quest             the quest to add (must not be null)
	 * @param prereqTemplates   pre-filtered goal templates to seed as
	 *                          requirements. Only identity and target
	 *                          fields are consulted. Empty list is
	 *                          equivalent to {@link #addQuestGoal(Quest)}.
	 * @return the created or existing quest goal's id, or {@code null}
	 *         if validation failed
	 */
	String addQuestGoalWithPrereqs(Quest quest, java.util.List<com.goalplanner.model.Goal> prereqTemplates);

	/**
	 * Add an achievement diary goal by area display name and tier. Auto-tracks
	 * via the per-area-per-tier completion varbits.
	 *
	 * @param areaDisplayName the area name as displayed in-game (e.g. "Ardougne",
	 *                        "Falador", "Karamja", "Kourend &amp; Kebos", etc.)
	 * @param tier            the tier
	 * @return the created or existing goal's id, or {@code null} if validation failed
	 */
	String addDiaryGoal(String areaDisplayName, DiaryTier tier);

	/**
	 * Add a diary goal with all unmet skill and quest requirements seeded
	 * as prerequisite goals.
	 */
	String addDiaryGoalWithPrereqs(String areaDisplayName, DiaryTier tier,
		com.goalplanner.data.DiaryRequirementResolver.Resolved resolved);

	/**
	 * Add a combat achievement goal by wiki/in-game task id. Auto-tracks via the
	 * bit-packed CA_TASK_COMPLETED varplayers.
	 *
	 * @param caTaskId the wiki/in-game task id (0 .. 639)
	 * @return the created or existing goal's id, or {@code null} if validation failed
	 */
	String addCombatAchievementGoal(int caTaskId);

	/**
	 * Add a boss kill count goal. Auto-tracks via VarPlayerID.
	 *
	 * @param bossName   display name matching BossKillData
	 * @param targetKills target kill count
	 */
	String addBossGoal(String bossName, int targetKills);

	/** Achievement diary tier; mirrors the internal AchievementDiaryData.Tier. */
	enum DiaryTier { EASY, MEDIUM, HARD, ELITE }

	// ===== Read API =====

	/**
	 * Return all goals as DTOs in canonical render order (sections sorted by their
	 * {@code order}, goals within each section sorted by priority). Includes both
	 * incomplete and completed goals; consumers can filter by {@code completedAt > 0}.
	 *
	 * <p>The returned list is a snapshot — mutating it has no effect on the plugin's
	 * internal state. Call again after mutations to get a fresh view.
	 */
	List<GoalView> queryAllGoals();

	/**
	 * Return all sections (built-in and user-defined) as DTOs, sorted by their
	 * {@code order} field.
	 */
	List<SectionView> queryAllSections();

	// ===== Mutation API =====

	/**
	 * Remove a single goal by id. Idempotent — returns false if no such goal exists.
	 */
	boolean removeGoal(String goalId);

	/**
	 * Add a custom-category tag to a goal. For non-CUSTOM goals, the tag is forced
	 * into the OTHER category regardless of inputs (the default tags from creation
	 * are protected and not modifiable). Returns false if the goal doesn't exist or
	 * the label is empty.
	 */
	boolean addTag(String goalId, String label);

	/**
	 * Remove a user-added tag from a goal. Default tags (auto-generated at goal
	 * creation) are protected and cannot be removed via this method. Returns true
	 * if a tag was removed, false otherwise.
	 */
	boolean removeTag(String goalId, String label);

	/**
	 * Change the target value of a SKILL or ITEM_GRIND goal. CA/quest/diary goals
	 * are binary and have immutable targets. Returns false on type mismatch or
	 * out-of-range value.
	 */
	boolean changeTarget(String goalId, int newTarget);

	/**
	 * Create an account-wide goal. Returns the new goal's id, or the
	 * existing id if a goal with the same metric and target already exists.
	 * Returns null if validation failed (unknown metric, non-positive target).
	 *
	 * @param metricName an {@link com.goalplanner.model.AccountMetric} enum name
	 * @param target     the target value (e.g. 200 for 200 Quest Points)
	 */
	String addAccountGoal(String metricName, int target);

	/**
	 * Create a custom goal. Returns the new goal's id, or null if validation failed
	 * (empty name).
	 */
	String addCustomGoal(String name, String description);

	/**
	 * Edit a CUSTOM goal's name and/or description. Either may be null to leave
	 * unchanged. Returns false if the goal isn't CUSTOM or doesn't exist.
	 */
	boolean editCustomGoal(String goalId, String newName, String newDescription);

	/**
	 * Mark a CUSTOM goal complete. Sets completedAt to now. Returns false if the
	 * goal isn't CUSTOM (other types are auto-tracked). Idempotent — calling on an
	 * already-complete goal updates the timestamp.
	 */
	boolean markGoalComplete(String goalId);

	/**
	 * Mark a CUSTOM goal incomplete. Clears completedAt. Returns false if the goal
	 * isn't CUSTOM.
	 */
	boolean markGoalIncomplete(String goalId);

	/**
	 * Restore a goal's tags to its default snapshot from creation. Returns false
	 * if the goal has no defaults or doesn't exist.
	 */
	boolean restoreDefaultTags(String goalId);
}
