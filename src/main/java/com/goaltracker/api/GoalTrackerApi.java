package com.goaltracker.api;

import net.runelite.api.Quest;
import net.runelite.api.Skill;

/**
 * Public API for the Goal Tracker plugin.
 *
 * Consumer plugins integrate by:
 * <ol>
 *   <li>Annotating their plugin class with
 *       {@code @PluginDependency(com.goaltracker.GoalTrackerPlugin.class)}</li>
 *   <li>Injecting this interface via {@code @Inject GoalTrackerApi goalTrackerApi}</li>
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
public interface GoalTrackerApi
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
	 * Add a combat achievement goal by wiki/in-game task id. Auto-tracks via the
	 * bit-packed CA_TASK_COMPLETED varplayers.
	 *
	 * @param caTaskId the wiki/in-game task id (0 .. 639)
	 * @return the created or existing goal's id, or {@code null} if validation failed
	 */
	String addCombatAchievementGoal(int caTaskId);

	/** Achievement diary tier; mirrors the internal AchievementDiaryData.Tier. */
	enum DiaryTier { EASY, MEDIUM, HARD, ELITE }
}
