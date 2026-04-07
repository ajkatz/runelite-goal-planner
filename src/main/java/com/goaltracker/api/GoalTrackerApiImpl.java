package com.goaltracker.api;

import com.goaltracker.data.AchievementDiaryData;
import com.goaltracker.data.CombatAchievementData;
import com.goaltracker.data.ItemSourceData;
import com.goaltracker.data.WikiCaRepository;
import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalType;
import com.goaltracker.model.ItemTag;
import com.goaltracker.model.Section;
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
public class GoalTrackerApiImpl implements GoalTrackerApi, GoalTrackerInternalApi
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
		log.debug("API.public addSkillGoal(skill={}, targetXp={})", skill, targetXp);
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
		log.debug("API.public addSkillGoalForLevel(skill={}, level={})", skill, level);
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
		log.debug("API.public addItemGoal(itemId={}, qty={})", itemId, targetQuantity);
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
		log.debug("API.public addQuestGoal(quest={})", quest);
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
		log.debug("API.public addDiaryGoal(area={}, tier={})", areaDisplayName, tier);
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
		log.debug("API.public addCombatAchievementGoal(caTaskId={})", caTaskId);
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
			// Inherit Slayer skill tag if the monster is a known slayer task target
			if (com.goaltracker.data.SourceAttributes.isSlayerTask(info.monster))
			{
				tags.add(new ItemTag("Slayer", TagCategory.SKILLING));
			}
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

	// ===== Read API =====

	@Override
	public List<GoalView> queryAllGoals()
	{
		log.debug("API.public queryAllGoals()");
		// Ensure the flat list is in canonical (section.order, priority) order before snapshot.
		goalStore.normalizeOrder();
		List<Goal> source = goalStore.getGoals();
		List<GoalView> out = new ArrayList<>(source.size());
		for (Goal g : source)
		{
			out.add(toGoalView(g));
		}
		return out;
	}

	@Override
	public List<SectionView> queryAllSections()
	{
		log.debug("API.public queryAllSections()");
		List<Section> source = new ArrayList<>(goalStore.getSections());
		source.sort(java.util.Comparator.comparingInt(Section::getOrder));
		List<SectionView> out = new ArrayList<>(source.size());
		for (Section s : source)
		{
			out.add(toSectionView(s));
		}
		return out;
	}

	private static GoalView toGoalView(Goal g)
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

		// Background color from goal type, packed as 0xRRGGBB
		java.awt.Color c = g.getType().getColor();
		v.backgroundColorRgb = (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();

		// Tag splitting: defaultTags is the snapshot from creation; customTags is
		// whatever is in `tags` but NOT in defaultTags. Matches the existing
		// "removable tags" rule used by the panel and Remove Tag dialog.
		List<ItemTag> defaults = g.getDefaultTags() != null ? g.getDefaultTags() : java.util.Collections.emptyList();
		List<ItemTag> all = g.getTags() != null ? g.getTags() : java.util.Collections.emptyList();

		v.defaultTags = new ArrayList<>(defaults.size());
		for (ItemTag t : defaults) v.defaultTags.add(toTagView(t));

		v.customTags = new ArrayList<>();
		for (ItemTag t : all)
		{
			if (!defaults.contains(t)) v.customTags.add(toTagView(t));
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
				for (ItemTag t : all)
				{
					if (t.getCategory() == TagCategory.BOSS || t.getCategory() == TagCategory.RAID)
					{
						v.attributes.put("monster", t.getLabel());
						break;
					}
				}
				if (g.getTooltip() != null) v.attributes.put("tooltip", g.getTooltip());
				break;
			case CUSTOM:
			default:
				break;
		}

		return v;
	}

	private static SectionView toSectionView(Section s)
	{
		SectionView v = new SectionView();
		v.id = s.getId();
		v.name = s.getName();
		v.order = s.getOrder();
		v.collapsed = s.isCollapsed();
		v.builtIn = s.isBuiltIn();
		v.kind = s.getBuiltInKind() != null ? s.getBuiltInKind().name() : null;
		return v;
	}

	private static TagView toTagView(ItemTag t)
	{
		java.awt.Color c = t.getCategory().getColor();
		int rgb = (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();
		return new TagView(t.getLabel(), t.getCategory().name(), rgb);
	}

	// ===== Mutation API =====

	@Override
	public boolean removeGoal(String goalId)
	{
		log.debug("API.public removeGoal(goalId={})", goalId);
		if (goalId == null) return false;
		Goal g = findGoal(goalId);
		if (g == null) return false;
		goalStore.removeGoal(goalId);
		onGoalsChanged.run();
		return true;
	}

	@Override
	public boolean addTag(String goalId, String label)
	{
		log.debug("API.public addTag(goalId={}, label={})", goalId, label);
		if (goalId == null || label == null || label.trim().isEmpty()) return false;
		Goal g = findGoal(goalId);
		if (g == null) return false;
		// Non-custom goals: force OTHER category. CUSTOM goals can use any category
		// (this method is the public API entry for tagging — the panel's add-tag
		// dialog with category dropdown is internal/UI and remains separate).
		TagCategory cat = TagCategory.OTHER;
		if (g.getTags() == null) g.setTags(new ArrayList<>());
		g.getTags().add(new ItemTag(label.trim(), cat));
		goalStore.updateGoal(g);
		onGoalsChanged.run();
		return true;
	}

	@Override
	public boolean removeTag(String goalId, String label)
	{
		log.debug("API.public removeTag(goalId={}, label={})", goalId, label);
		if (goalId == null || label == null) return false;
		Goal g = findGoal(goalId);
		if (g == null || g.getTags() == null) return false;
		List<ItemTag> defaults = g.getDefaultTags() != null ? g.getDefaultTags() : java.util.Collections.emptyList();
		// Find a removable tag matching the label that is NOT in defaults.
		ItemTag toRemove = null;
		for (ItemTag t : g.getTags())
		{
			if (label.equals(t.getLabel()) && !defaults.contains(t))
			{
				toRemove = t;
				break;
			}
		}
		if (toRemove == null) return false;
		g.getTags().remove(toRemove);
		goalStore.updateGoal(g);
		onGoalsChanged.run();
		return true;
	}

	@Override
	public boolean changeTarget(String goalId, int newTarget)
	{
		log.debug("API.public changeTarget(goalId={}, newTarget={})", goalId, newTarget);
		if (goalId == null || newTarget < 1) return false;
		Goal g = findGoal(goalId);
		if (g == null) return false;
		if (g.getType() == GoalType.SKILL)
		{
			if (newTarget > MAX_XP) return false;
		}
		else if (g.getType() != GoalType.ITEM_GRIND)
		{
			return false; // CA/quest/diary targets are immutable
		}
		g.setTargetValue(newTarget);
		goalStore.updateGoal(g);
		onGoalsChanged.run();
		return true;
	}

	@Override
	public String addCustomGoal(String name, String description)
	{
		log.debug("API.public addCustomGoal(name={}, description={})", name, description);
		if (name == null || name.trim().isEmpty()) return null;
		String trimmedName = name.trim();
		// Duplicate guard: same name + same type = no new goal
		Goal existing = null;
		for (Goal g : goalStore.getGoals())
		{
			if (g.getType() == GoalType.CUSTOM && trimmedName.equalsIgnoreCase(g.getName()))
			{
				existing = g;
				break;
			}
		}
		if (existing != null)
		{
			log.info("addCustomGoal: duplicate of existing goal {} ({})", existing.getId(), trimmedName);
			return existing.getId();
		}
		Goal goal = Goal.builder()
			.type(GoalType.CUSTOM)
			.name(trimmedName)
			.description(description != null ? description.trim() : "")
			.targetValue(1)
			.currentValue(0)
			.build();
		goalStore.addGoal(goal);
		onGoalsChanged.run();
		log.info("addCustomGoal created: {} ({})", goal.getId(), trimmedName);
		return goal.getId();
	}

	@Override
	public boolean editCustomGoal(String goalId, String newName, String newDescription)
	{
		log.debug("API.public editCustomGoal(goalId={}, newName={}, newDescription={})",
			goalId, newName, newDescription);
		if (goalId == null) return false;
		Goal g = findGoal(goalId);
		if (g == null || g.getType() != GoalType.CUSTOM) return false;
		if (newName != null)
		{
			String trimmed = newName.trim();
			if (trimmed.isEmpty()) return false;
			g.setName(trimmed);
		}
		if (newDescription != null)
		{
			g.setDescription(newDescription.trim());
		}
		goalStore.updateGoal(g);
		onGoalsChanged.run();
		return true;
	}

	@Override
	public boolean markGoalComplete(String goalId)
	{
		log.debug("API.public markGoalComplete(goalId={})", goalId);
		if (goalId == null) return false;
		Goal g = findGoal(goalId);
		if (g == null || g.getType() != GoalType.CUSTOM) return false;
		g.setCompletedAt(System.currentTimeMillis());
		g.setStatus(com.goaltracker.model.GoalStatus.COMPLETE);
		goalStore.updateGoal(g);
		goalStore.reconcileCompletedSection();
		onGoalsChanged.run();
		return true;
	}

	@Override
	public boolean markGoalIncomplete(String goalId)
	{
		log.debug("API.public markGoalIncomplete(goalId={})", goalId);
		if (goalId == null) return false;
		Goal g = findGoal(goalId);
		if (g == null || g.getType() != GoalType.CUSTOM) return false;
		g.setCompletedAt(0);
		g.setStatus(com.goaltracker.model.GoalStatus.ACTIVE);
		goalStore.updateGoal(g);
		goalStore.reconcileCompletedSection();
		onGoalsChanged.run();
		return true;
	}

	@Override
	public boolean restoreDefaultTags(String goalId)
	{
		log.debug("API.public restoreDefaultTags(goalId={})", goalId);
		if (goalId == null) return false;
		Goal g = findGoal(goalId);
		if (g == null) return false;
		List<ItemTag> defaults = g.getDefaultTags();
		if (defaults == null || defaults.isEmpty()) return false;
		g.setTags(new ArrayList<>(defaults));
		goalStore.updateGoal(g);
		onGoalsChanged.run();
		return true;
	}

	private Goal findGoal(String goalId)
	{
		for (Goal g : goalStore.getGoals())
		{
			if (g.getId().equals(goalId)) return g;
		}
		return null;
	}

	// ===== Internal API =====

	@Override
	public boolean moveGoal(String goalId, int newGlobalIndex)
	{
		log.debug("API.internal moveGoal(goalId={}, newGlobalIndex={})", goalId, newGlobalIndex);
		if (goalId == null) return false;
		List<Goal> goals = goalStore.getGoals();
		int currentIndex = -1;
		for (int i = 0; i < goals.size(); i++)
		{
			if (goals.get(i).getId().equals(goalId))
			{
				currentIndex = i;
				break;
			}
		}
		if (currentIndex < 0) return false;
		if (newGlobalIndex < 0 || newGlobalIndex >= goals.size()) return false;
		if (currentIndex == newGlobalIndex) return false;

		// Section bounds check: target index must be in the same section as the source
		String sourceSectionId = goals.get(currentIndex).getSectionId();
		String targetSectionId = goals.get(newGlobalIndex).getSectionId();
		if (sourceSectionId == null || !sourceSectionId.equals(targetSectionId))
		{
			return false;
		}

		// Dispatch by move distance:
		//  - single-step (arrow up/down) → moveGoal: skill-chain partner aware
		//  - multi-step  (Move to Top/Bottom) → moveGoalTo: direct + enforce
		int delta = Math.abs(newGlobalIndex - currentIndex);
		if (delta == 1)
		{
			reorderingService.moveGoal(currentIndex, newGlobalIndex);
		}
		else
		{
			reorderingService.moveGoalTo(currentIndex, newGlobalIndex);
		}
		onGoalsChanged.run();
		return true;
	}

	@Override
	public void removeAllGoals()
	{
		log.debug("API.internal removeAllGoals()");
		while (!goalStore.getGoals().isEmpty())
		{
			goalStore.removeGoal(goalStore.getGoals().get(0).getId());
		}
		onGoalsChanged.run();
	}

	@Override
	public boolean setSectionCollapsed(String sectionId, boolean collapsed)
	{
		log.debug("API.internal setSectionCollapsed(sectionId={}, collapsed={})", sectionId, collapsed);
		if (sectionId == null) return false;
		for (Section s : goalStore.getSections())
		{
			if (sectionId.equals(s.getId()))
			{
				if (s.isCollapsed() == collapsed) return false;
				s.setCollapsed(collapsed);
				goalStore.save();
				onGoalsChanged.run();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean toggleSectionCollapsed(String sectionId)
	{
		log.debug("API.internal toggleSectionCollapsed(sectionId={})", sectionId);
		if (sectionId == null) return false;
		for (Section s : goalStore.getSections())
		{
			if (sectionId.equals(s.getId()))
			{
				boolean next = !s.isCollapsed();
				s.setCollapsed(next);
				goalStore.save();
				onGoalsChanged.run();
				return next;
			}
		}
		return false;
	}
}
