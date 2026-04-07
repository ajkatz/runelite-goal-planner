package com.goaltracker.api;

import com.goaltracker.data.AchievementDiaryData;
import com.goaltracker.data.CombatAchievementData;
import com.goaltracker.data.ItemSourceData;
import com.goaltracker.data.WikiCaRepository;
import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalType;
import com.goaltracker.model.ItemTag;
import com.goaltracker.model.TagCategory;
import com.goaltracker.persistence.GoalStore;
import com.goaltracker.service.GoalReorderingService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Experience;
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import net.runelite.client.game.ItemManager;

/**
 * Default implementation of {@link GoalTrackerApi}. Bound to the public
 * interface in {@link com.goaltracker.GoalTrackerPlugin#configure}.
 */
@Slf4j
@Singleton
public class GoalTrackerApiImpl implements GoalTrackerApi
{
	/** Max level supported (1-99 normal, 100-126 virtual). */
	private static final int MAX_LEVEL = 126;

	/** Max experience per skill in OSRS. */
	private static final int MAX_XP = 200_000_000;

	/** Sprite id for the blue quest book icon (mirrors GoalTrackerPlugin.QUEST_SPRITE_ID). */
	private static final int QUEST_SPRITE_ID = 899;

	private final GoalStore goalStore;
	private final GoalReorderingService reorderingService;
	private final ItemManager itemManager;
	private final WikiCaRepository wikiCaRepository;

	/** Optional UI-refresh hook the plugin sets after the panel is constructed. */
	private Runnable onGoalsChanged = () -> {};

	@Inject
	public GoalTrackerApiImpl(
		GoalStore goalStore,
		GoalReorderingService reorderingService,
		ItemManager itemManager,
		WikiCaRepository wikiCaRepository)
	{
		this.goalStore = goalStore;
		this.reorderingService = reorderingService;
		this.itemManager = itemManager;
		this.wikiCaRepository = wikiCaRepository;
	}

	/**
	 * Plugin-internal hook for the UI to register a refresh callback. Not part
	 * of the public API; called by GoalTrackerPlugin during startup.
	 */
	public void setOnGoalsChanged(Runnable callback)
	{
		this.onGoalsChanged = callback != null ? callback : () -> {};
	}

	@Override
	public String addSkillGoal(Skill skill, int targetXp)
	{
		if (skill == null)
		{
			log.warn("addSkillGoal: skill is null");
			return null;
		}
		if (targetXp < 1 || targetXp > MAX_XP)
		{
			log.warn("addSkillGoal: targetXp {} out of range [1, {}]", targetXp, MAX_XP);
			return null;
		}

		// Duplicate guard: same skill + same target XP = no new goal, return existing id.
		Goal existing = findExistingSkillGoal(skill, targetXp);
		if (existing != null)
		{
			log.info("addSkillGoal: duplicate of existing goal {} ({} → {} XP)",
				existing.getId(), skill.getName(), targetXp);
			return existing.getId();
		}

		int targetLevel = Experience.getLevelForXp(targetXp);
		Goal goal = Goal.builder()
			.type(GoalType.SKILL)
			.name(skill.getName() + " \u2192 Level " + targetLevel)
			.skillName(skill.name())
			.targetValue(targetXp)
			.build();

		goalStore.addGoal(goal);
		// Auto-position within the same-skill chain (lower targets above higher).
		int insertBefore = reorderingService.findInsertionIndex(skill.name(), targetXp);
		if (insertBefore >= 0)
		{
			goalStore.reorder(goalStore.getGoals().size() - 1, insertBefore);
		}
		onGoalsChanged.run();
		log.info("addSkillGoal created: {} ({} → {} XP)", goal.getId(), skill.getName(), targetXp);
		return goal.getId();
	}

	@Override
	public String addSkillGoalForLevel(Skill skill, int level)
	{
		if (skill == null)
		{
			log.warn("addSkillGoalForLevel: skill is null");
			return null;
		}
		if (level < 1 || level > MAX_LEVEL)
		{
			log.warn("addSkillGoalForLevel: level {} out of range [1, {}]", level, MAX_LEVEL);
			return null;
		}
		return addSkillGoal(skill, Experience.getXpForLevel(level));
	}

	private Goal findExistingSkillGoal(Skill skill, int targetXp)
	{
		for (Goal g : goalStore.getGoals())
		{
			if (g.getType() == GoalType.SKILL
				&& skill.name().equals(g.getSkillName())
				&& g.getTargetValue() == targetXp)
			{
				return g;
			}
		}
		return null;
	}

	@Override
	public String addItemGoal(int itemId, int targetQuantity)
	{
		if (itemId <= 0 || targetQuantity <= 0)
		{
			log.warn("addItemGoal: invalid input itemId={} qty={}", itemId, targetQuantity);
			return null;
		}

		// Duplicate guard: same item id (any qty) → existing wins
		for (Goal g : goalStore.getGoals())
		{
			if (g.getType() == GoalType.ITEM_GRIND && g.getItemId() == itemId)
			{
				log.info("addItemGoal: duplicate of existing goal {} (item {})", g.getId(), itemId);
				return g.getId();
			}
		}

		String itemName;
		try
		{
			itemName = itemManager.getItemComposition(itemId).getName();
		}
		catch (Exception e)
		{
			log.warn("addItemGoal: unknown item id {}", itemId);
			return null;
		}

		List<ItemTag> tags = new ArrayList<>(ItemSourceData.getTags(itemId));
		Goal goal = Goal.builder()
			.type(GoalType.ITEM_GRIND)
			.name(itemName)
			.description(targetQuantity + " total")
			.itemId(itemId)
			.targetValue(targetQuantity)
			.currentValue(-1)
			.tags(tags)
			.defaultTags(new ArrayList<>(tags))
			.build();

		goalStore.addGoal(goal);
		onGoalsChanged.run();
		log.info("addItemGoal created: {} ({} x {})", goal.getId(), targetQuantity, itemName);
		return goal.getId();
	}

	@Override
	public String addQuestGoal(Quest quest)
	{
		if (quest == null)
		{
			log.warn("addQuestGoal: quest is null");
			return null;
		}

		// Duplicate guard: same questName
		String questName = quest.name();
		for (Goal g : goalStore.getGoals())
		{
			if (g.getType() == GoalType.QUEST && questName.equals(g.getQuestName()))
			{
				log.info("addQuestGoal: duplicate of existing goal {} ({})", g.getId(), questName);
				return g.getId();
			}
		}

		Goal goal = Goal.builder()
			.type(GoalType.QUEST)
			.name(quest.getName())
			.description("Quest")
			.questName(questName)
			.targetValue(1)
			.currentValue(0)
			.spriteId(QUEST_SPRITE_ID)
			.build();

		goalStore.addGoal(goal);
		onGoalsChanged.run();
		log.info("addQuestGoal created: {} ({})", goal.getId(), quest.getName());
		return goal.getId();
	}

	@Override
	public String addDiaryGoal(String areaDisplayName, DiaryTier tier)
	{
		if (areaDisplayName == null || areaDisplayName.isEmpty() || tier == null)
		{
			log.warn("addDiaryGoal: invalid input area={} tier={}", areaDisplayName, tier);
			return null;
		}

		AchievementDiaryData.Tier internalTier = mapDiaryTier(tier);
		String description = internalTier.getDisplayName() + " Achievement Diary";

		// Duplicate guard: same area + same tier description
		for (Goal g : goalStore.getGoals())
		{
			if (g.getType() == GoalType.DIARY
				&& areaDisplayName.equalsIgnoreCase(g.getName())
				&& description.equalsIgnoreCase(g.getDescription()))
			{
				log.info("addDiaryGoal: duplicate of existing goal {} ({} {})",
					g.getId(), areaDisplayName, internalTier);
				return g.getId();
			}
		}

		int varbitId = AchievementDiaryData.completionVarbit(areaDisplayName, internalTier);
		Goal goal = Goal.builder()
			.type(GoalType.DIARY)
			.name(areaDisplayName)
			.description(description)
			.targetValue(1)
			.currentValue(0)
			.spriteId(AchievementDiaryData.DIARY_SPRITE_ID)
			.varbitId(varbitId)
			.build();

		goalStore.addGoal(goal);
		onGoalsChanged.run();
		log.info("addDiaryGoal created: {} ({} {})", goal.getId(), areaDisplayName, internalTier);
		return goal.getId();
	}

	private static AchievementDiaryData.Tier mapDiaryTier(DiaryTier tier)
	{
		switch (tier)
		{
			case EASY:   return AchievementDiaryData.Tier.EASY;
			case MEDIUM: return AchievementDiaryData.Tier.MEDIUM;
			case HARD:   return AchievementDiaryData.Tier.HARD;
			case ELITE:  return AchievementDiaryData.Tier.ELITE;
			default:     throw new IllegalArgumentException("Unknown DiaryTier " + tier);
		}
	}

	@Override
	public String addCombatAchievementGoal(int caTaskId)
	{
		if (caTaskId < 0 || caTaskId > 639)
		{
			log.warn("addCombatAchievementGoal: caTaskId {} out of range [0, 639]", caTaskId);
			return null;
		}

		WikiCaRepository.CaInfo info = wikiCaRepository.getById(caTaskId);
		if (info == null)
		{
			log.warn("addCombatAchievementGoal: no wiki entry for task id {}", caTaskId);
			return null;
		}

		// Duplicate guard: same caTaskId or same name
		for (Goal g : goalStore.getGoals())
		{
			if (g.getType() != GoalType.COMBAT_ACHIEVEMENT) continue;
			if (g.getCaTaskId() == caTaskId
				|| (info.name != null && info.name.equalsIgnoreCase(g.getName())))
			{
				log.info("addCombatAchievementGoal: duplicate of existing goal {} ({})",
					g.getId(), info.name);
				return g.getId();
			}
		}

		// Tier sprite: SpriteID.CaTierSwordsSmall._0..5 = 3399..3404
		CombatAchievementData.Tier ctier = parseCaTier(info.tier);
		int tierSpriteId = ctier != null ? 3399 + ctier.ordinal() : 0;
		String description = ctier != null
			? ctier.getDisplayName() + " Combat Achievement"
			: "Combat Achievement";
		String tooltip = info.task != null && !info.task.isEmpty()
			? info.name + " \u2014 " + info.task
			: null;

		List<ItemTag> tags = new ArrayList<>();
		if (info.monster != null && !info.monster.isEmpty())
		{
			boolean isRaid = CombatAchievementData.isRaidBoss(info.monster);
			String tagLabel = isRaid ? CombatAchievementData.abbreviateRaid(info.monster) : info.monster;
			tags.add(new ItemTag(tagLabel, isRaid ? TagCategory.RAID : TagCategory.BOSS));
		}

		Goal goal = Goal.builder()
			.type(GoalType.COMBAT_ACHIEVEMENT)
			.name(info.name)
			.description(description)
			.tooltip(tooltip)
			.targetValue(1)
			.currentValue(0)
			.spriteId(tierSpriteId)
			.caTaskId(caTaskId)
			.tags(tags)
			.defaultTags(new ArrayList<>(tags))
			.build();

		goalStore.addGoal(goal);
		onGoalsChanged.run();
		log.info("addCombatAchievementGoal created: {} ({})", goal.getId(), info.name);
		return goal.getId();
	}

	private static CombatAchievementData.Tier parseCaTier(String wikiTier)
	{
		if (wikiTier == null) return null;
		switch (wikiTier.toUpperCase())
		{
			case "EASY":        return CombatAchievementData.Tier.EASY;
			case "MEDIUM":      return CombatAchievementData.Tier.MEDIUM;
			case "HARD":        return CombatAchievementData.Tier.HARD;
			case "ELITE":       return CombatAchievementData.Tier.ELITE;
			case "MASTER":      return CombatAchievementData.Tier.MASTER;
			case "GRANDMASTER": return CombatAchievementData.Tier.GRANDMASTER;
			default:            return null;
		}
	}
}
