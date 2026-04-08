package com.goaltracker.api;

import com.goaltracker.data.AchievementDiaryData;
import com.goaltracker.data.CombatAchievementData;
import com.goaltracker.data.ItemSourceData;
import com.goaltracker.data.WikiCaRepository;
import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalType;
import com.goaltracker.model.ItemTag;
import com.goaltracker.model.Section;
import com.goaltracker.model.Tag;
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

	/** Ephemeral selection set — not persisted, lost on plugin restart. */
	private final java.util.Set<String> selectedGoalIds = new java.util.LinkedHashSet<>();

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
		// Section-scoped: only considers goals in the new goal's own section so
		// the returned index never crosses a section boundary.
		int insertBefore = reorderingService.findInsertionIndex(
			skill.name(), targetXp, goal.getSectionId());
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

		// Resolve item-source ItemTag specs into Tag entity ids via findOrCreateSystemTag
		List<String> tagIds = new ArrayList<>();
		for (ItemTag spec : ItemSourceData.getTags(itemId))
		{
			Tag tag = goalStore.findOrCreateSystemTag(spec.getLabel(), spec.getCategory());
			if (tag != null) tagIds.add(tag.getId());
		}
		Goal goal = Goal.builder()
			.type(GoalType.ITEM_GRIND)
			.name(itemName)
			.description(targetQuantity + " total")
			.itemId(itemId)
			.targetValue(targetQuantity)
			.currentValue(-1)
			.tagIds(new ArrayList<>(tagIds))
			.defaultTagIds(new ArrayList<>(tagIds))
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

		List<String> tagIds = new ArrayList<>();
		if (info.monster != null && !info.monster.isEmpty())
		{
			boolean isRaid = CombatAchievementData.isRaidBoss(info.monster);
			String tagLabel = isRaid ? CombatAchievementData.abbreviateRaid(info.monster) : info.monster;
			Tag bossTag = goalStore.findOrCreateSystemTag(tagLabel,
				isRaid ? TagCategory.RAID : TagCategory.BOSS);
			if (bossTag != null) tagIds.add(bossTag.getId());
			// Inherit Slayer skill tag if the monster is a known slayer task target
			if (com.goaltracker.data.SourceAttributes.isSlayerTask(info.monster))
			{
				Tag slayerTag = goalStore.findOrCreateSystemTag("Slayer", TagCategory.SKILLING);
				if (slayerTag != null) tagIds.add(slayerTag.getId());
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
			.tagIds(new ArrayList<>(tagIds))
			.defaultTagIds(new ArrayList<>(tagIds))
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
	public List<GoalView> searchGoals(String query)
	{
		log.debug("API.internal searchGoals(query={})", query);
		List<GoalView> all = queryAllGoals();
		if (query == null) return all;
		String needle = query.trim().toLowerCase();
		if (needle.isEmpty()) return all;
		List<GoalView> out = new ArrayList<>();
		for (GoalView gv : all)
		{
			if (matchesSearch(gv, needle)) out.add(gv);
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
			com.goaltracker.model.Section sec = goalStore.findSection(gv.sectionId);
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

	private GoalView toGoalView(Goal g)
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
		v.selected = selectedGoalIds.contains(g.getId());

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
			Tag tag = goalStore.findTag(id);
			if (tag != null) v.defaultTags.add(toTagView(tag));
		}

		v.customTags = new ArrayList<>();
		for (String id : allIds)
		{
			if (defaultIds.contains(id)) continue;
			Tag tag = goalStore.findTag(id);
			if (tag != null) v.customTags.add(toTagView(tag));
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
					Tag tag = goalStore.findTag(id);
					if (tag != null && (tag.getCategory() == TagCategory.BOSS || tag.getCategory() == TagCategory.RAID))
					{
						v.attributes.put("monster", tag.getLabel());
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

	/** Neutral default section header color (matches SectionHeaderRow BORDER_COLOR). */
	private static final int SECTION_DEFAULT_COLOR_RGB = (60 << 16) | (60 << 8) | 60;

	private static SectionView toSectionView(Section s)
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

	private TagView toTagView(Tag t)
	{
		// Defensive: null category fallback to OTHER (handles enum removal migration).
		TagCategory cat = t.getCategory() != null ? t.getCategory() : TagCategory.OTHER;
		int defaultRgb = goalStore.getCategoryDefaultColor(cat);

		int currentRgb;
		boolean overridden;
		if (cat == TagCategory.OTHER)
		{
			// OTHER is special: per-tag colors. Each Other tag carries its own.
			if (t.getColorRgb() >= 0)
			{
				currentRgb = t.getColorRgb();
				overridden = true;
			}
			else
			{
				currentRgb = defaultRgb;
				overridden = false;
			}
		}
		else
		{
			// BOSS/RAID/CLUE/MINIGAME/SKILLING: shared category color. The
			// per-tag colorRgb field is ignored for these categories.
			currentRgb = goalStore.getCategoryColor(cat);
			overridden = goalStore.isCategoryColorOverridden(cat);
		}

		TagView v = new TagView(t.getLabel(), cat.name(),
			currentRgb, defaultRgb, overridden);
		v.id = t.getId();
		v.system = t.isSystem();
		v.iconKey = t.getIconKey();
		return v;
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
		// Drop the removed goal from the ephemeral selection set so callers
		// don't end up with a stale id pointing at nothing.
		selectedGoalIds.remove(goalId);
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
		// via the internal addTagWithCategory API. Tag is created (or reused) as a
		// User tag entity, then referenced by id from this goal.
		Tag tag = goalStore.createUserTag(label.trim(), TagCategory.OTHER);
		if (tag == null) return false;
		if (g.getTagIds() == null) g.setTagIds(new ArrayList<>());
		if (!g.getTagIds().contains(tag.getId()))
		{
			g.getTagIds().add(tag.getId());
		}
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
		if (g == null || g.getTagIds() == null) return false;
		List<String> defaults = g.getDefaultTagIds() != null ? g.getDefaultTagIds() : java.util.Collections.emptyList();
		// Find a removable tag id matching the label whose id is NOT in defaults.
		String toRemove = null;
		for (String id : g.getTagIds())
		{
			if (defaults.contains(id)) continue;
			Tag tag = goalStore.findTag(id);
			if (tag != null && label.equals(tag.getLabel()))
			{
				toRemove = id;
				break;
			}
		}
		if (toRemove == null) return false;
		g.getTagIds().remove(toRemove);
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
		// Regenerate the display string from the new target. Both SKILL name
		// and ITEM_GRIND description are mechanically derived from the target,
		// so the API owns this — callers no longer need to mutate the display
		// string and re-save behind our back.
		if (g.getType() == GoalType.SKILL && g.getSkillName() != null)
		{
			try
			{
				net.runelite.api.Skill skill = net.runelite.api.Skill.valueOf(g.getSkillName());
				int level = Experience.getLevelForXp(newTarget);
				g.setName(skill.getName() + " \u2192 Level " + level);
			}
			catch (IllegalArgumentException ignored) {}
		}
		else if (g.getType() == GoalType.ITEM_GRIND)
		{
			g.setDescription(com.goaltracker.util.FormatUtil.formatNumber(newTarget) + " total");
		}
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
		if (g == null) return false;
		// CUSTOM and ITEM_GRIND can be manually marked complete. ITEM_GRIND is
		// "sticky": the next ItemTracker pass will revert via recordGoalProgress
		// if the actual inventory+bank count is below target. CUSTOM stays
		// permanently. Other types (skill/quest/diary/CA) are purely game-driven.
		if (g.getType() != GoalType.CUSTOM && g.getType() != GoalType.ITEM_GRIND) return false;
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
		if (g == null) return false;
		if (g.getType() != GoalType.CUSTOM && g.getType() != GoalType.ITEM_GRIND) return false;
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
		List<String> defaults = g.getDefaultTagIds();
		if (defaults == null || defaults.isEmpty()) return false;
		g.setTagIds(new ArrayList<>(defaults));
		goalStore.updateGoal(g);
		onGoalsChanged.run();
		return true;
	}

	// ---------------------------------------------------------------------
	// Bulk multi-selection actions (Mission 24)
	// ---------------------------------------------------------------------

	@Override
	public boolean isGoalOverridden(String goalId)
	{
		Goal g = findGoal(goalId);
		if (g == null) return false;
		if (g.getCustomColorRgb() >= 0) return true;
		List<String> defaults = g.getDefaultTagIds() != null ? g.getDefaultTagIds() : java.util.Collections.emptyList();
		List<String> current = g.getTagIds() != null ? g.getTagIds() : java.util.Collections.emptyList();
		return !new java.util.HashSet<>(current).equals(new java.util.HashSet<>(defaults));
	}

	@Override
	public int bulkRestoreDefaults(java.util.Set<String> goalIds)
	{
		log.debug("API.internal bulkRestoreDefaults({} goals)", goalIds == null ? 0 : goalIds.size());
		if (goalIds == null || goalIds.isEmpty()) return 0;
		int changed = 0;
		for (String goalId : goalIds)
		{
			Goal g = findGoal(goalId);
			if (g == null) continue;
			if (!isGoalOverridden(goalId)) continue;
			List<String> defaults = g.getDefaultTagIds() != null ? g.getDefaultTagIds() : java.util.Collections.emptyList();
			g.setTagIds(new ArrayList<>(defaults));
			g.setCustomColorRgb(-1);
			goalStore.updateGoal(g);
			changed++;
		}
		if (changed > 0) onGoalsChanged.run();
		return changed;
	}

	@Override
	public int bulkRemoveTagFromGoals(java.util.Set<String> goalIds, String tagId)
	{
		log.debug("API.internal bulkRemoveTagFromGoals({} goals, tagId={})",
			goalIds == null ? 0 : goalIds.size(), tagId);
		if (goalIds == null || goalIds.isEmpty() || tagId == null) return 0;
		int removed = 0;
		for (String goalId : goalIds)
		{
			Goal g = findGoal(goalId);
			if (g == null || g.getTagIds() == null) continue;
			if (!g.getTagIds().contains(tagId)) continue;
			boolean isCustom = g.getType() == GoalType.CUSTOM;
			List<String> defaults = g.getDefaultTagIds() != null ? g.getDefaultTagIds() : java.util.Collections.emptyList();
			if (!isCustom && defaults.contains(tagId)) continue; // not removable
			g.getTagIds().remove(tagId);
			goalStore.updateGoal(g);
			removed++;
		}
		if (removed > 0) onGoalsChanged.run();
		return removed;
	}

	@Override
	public List<TagRemovalOption> getRemovableTagsForSelection(java.util.Set<String> goalIds)
	{
		if (goalIds == null || goalIds.isEmpty()) return java.util.Collections.emptyList();
		// tagId → count of selected goals where it's both present and removable
		java.util.Map<String, Integer> counts = new java.util.HashMap<>();
		for (String goalId : goalIds)
		{
			Goal g = findGoal(goalId);
			if (g == null || g.getTagIds() == null) continue;
			boolean isCustom = g.getType() == GoalType.CUSTOM;
			List<String> defaults = g.getDefaultTagIds() != null ? g.getDefaultTagIds() : java.util.Collections.emptyList();
			for (String tid : g.getTagIds())
			{
				if (!isCustom && defaults.contains(tid)) continue;
				counts.merge(tid, 1, Integer::sum);
			}
		}
		List<TagRemovalOption> out = new ArrayList<>(counts.size());
		for (java.util.Map.Entry<String, Integer> e : counts.entrySet())
		{
			Tag tag = goalStore.findTag(e.getKey());
			if (tag == null) continue;
			out.add(new TagRemovalOption(tag.getId(), tag.getLabel(),
				tag.getCategory() != null ? tag.getCategory().name() : "OTHER", e.getValue()));
		}
		// Sort: count desc, then label asc (case-insensitive)
		out.sort((a, b) -> {
			if (a.count != b.count) return Integer.compare(b.count, a.count);
			return a.label.compareToIgnoreCase(b.label);
		});
		return out;
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
	public boolean positionGoalInSection(String goalId, String sectionId, int positionInSection)
	{
		log.debug("API.internal positionGoalInSection(goalId={}, sectionId={}, pos={})",
			goalId, sectionId, positionInSection);
		if (goalId == null || sectionId == null) return false;
		Goal g = findGoal(goalId);
		if (g == null) return false;

		boolean changed = false;
		// Step 1: move to the target section if needed
		if (!sectionId.equals(g.getSectionId()))
		{
			if (goalStore.moveGoalToSection(goalId, sectionId)) changed = true;
		}

		// Step 2: collect goals in the target section in canonical order, find
		// the current index of the goal, and reorder if it's not where we want.
		goalStore.normalizeOrder();
		List<Goal> goals = goalStore.getGoals();
		List<Integer> sectionIndices = new ArrayList<>();
		int sourceIdx = -1;
		for (int i = 0; i < goals.size(); i++)
		{
			if (sectionId.equals(goals.get(i).getSectionId()))
			{
				sectionIndices.add(i);
				if (goalId.equals(goals.get(i).getId()))
				{
					sourceIdx = i;
				}
			}
		}
		if (sourceIdx < 0)
		{
			if (changed) onGoalsChanged.run();
			return changed;
		}

		// Clamp the position to the section's range
		int sectionSize = sectionIndices.size();
		int clampedPos = Math.max(0, Math.min(positionInSection, sectionSize - 1));
		int targetGlobal = sectionIndices.get(clampedPos);
		if (sourceIdx != targetGlobal)
		{
			reorderingService.moveGoalTo(sourceIdx, targetGlobal);
			changed = true;
		}

		if (changed) onGoalsChanged.run();
		return changed;
	}

	@Override
	public void removeAllGoals()
	{
		log.debug("API.internal removeAllGoals()");
		while (!goalStore.getGoals().isEmpty())
		{
			goalStore.removeGoal(goalStore.getGoals().get(0).getId());
		}
		selectedGoalIds.clear();
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

	// ---------------------------------------------------------------------
	// User-defined section CRUD (Phase 2)
	// ---------------------------------------------------------------------

	@Override
	public String createSection(String name)
	{
		log.debug("API.internal createSection(name={})", name);
		Section created = goalStore.createUserSection(name);
		onGoalsChanged.run();
		return created.getId();
	}

	@Override
	public boolean renameSection(String sectionId, String newName)
	{
		log.debug("API.internal renameSection(sectionId={}, newName={})", sectionId, newName);
		boolean changed = goalStore.renameUserSection(sectionId, newName);
		if (changed) onGoalsChanged.run();
		return changed;
	}

	@Override
	public boolean deleteSection(String sectionId)
	{
		log.debug("API.internal deleteSection(sectionId={})", sectionId);
		boolean deleted = goalStore.deleteUserSection(sectionId);
		if (deleted) onGoalsChanged.run();
		return deleted;
	}

	@Override
	public boolean reorderSection(String sectionId, int newUserIndex)
	{
		log.debug("API.internal reorderSection(sectionId={}, newUserIndex={})", sectionId, newUserIndex);
		boolean changed = goalStore.reorderUserSection(sectionId, newUserIndex);
		if (changed) onGoalsChanged.run();
		return changed;
	}

	@Override
	public boolean moveGoalToSection(String goalId, String sectionId)
	{
		log.debug("API.internal moveGoalToSection(goalId={}, sectionId={})", goalId, sectionId);
		// Mission 25: reject no-op moves where the goal is already in the
		// target section. Stops UI from offering "Move to <current section>".
		Goal current = findGoal(goalId);
		if (current != null && sectionId != null && sectionId.equals(current.getSectionId()))
		{
			return false;
		}
		boolean moved = goalStore.moveGoalToSection(goalId, sectionId);
		if (moved)
		{
			// Skill chain ordering applies in every section, not just Incomplete.
			// After a SKILL goal lands in a new section, bubble it to the right
			// position relative to other same-skill goals already there.
			Goal g = findGoal(goalId);
			if (g != null && g.getType() == com.goaltracker.model.GoalType.SKILL)
			{
				reorderingService.enforceSkillOrderingInSection(sectionId);
			}
			onGoalsChanged.run();
		}
		return moved;
	}

	@Override
	public int removeAllUserSections()
	{
		log.debug("API.internal removeAllUserSections()");
		int removed = goalStore.removeAllUserSections();
		if (removed > 0) onGoalsChanged.run();
		return removed;
	}

	// ---------------------------------------------------------------------
	// Color overrides (Phase 3)
	// ---------------------------------------------------------------------

	@Override
	public boolean setSectionColor(String sectionId, int colorRgb)
	{
		log.debug("API.internal setSectionColor(sectionId={}, colorRgb={})", sectionId, colorRgb);
		Section section = goalStore.findSection(sectionId);
		if (section == null) return false;
		int normalized = colorRgb < 0 ? -1 : (colorRgb & 0xFFFFFF);
		if (section.getColorRgb() == normalized) return false;
		section.setColorRgb(normalized);
		goalStore.save();
		onGoalsChanged.run();
		return true;
	}

	@Override
	public boolean setGoalColor(String goalId, int colorRgb)
	{
		log.debug("API.internal setGoalColor(goalId={}, colorRgb={})", goalId, colorRgb);
		Goal g = findGoal(goalId);
		if (g == null) return false;
		int normalized = colorRgb < 0 ? -1 : (colorRgb & 0xFFFFFF);
		if (g.getCustomColorRgb() == normalized) return false;
		g.setCustomColorRgb(normalized);
		goalStore.save();
		onGoalsChanged.run();
		return true;
	}

	@Override
	public boolean setTagColor(String goalId, String tagLabel, int colorRgb)
	{
		// Mission 20: per-tag colors are OTHER-only. For OTHER tags this
		// stores the color on the tag entity; for other categories it
		// delegates to setCategoryColor which affects every tag in the
		// category. SKILLING is rejected.
		log.debug("API.internal setTagColor(goalId={}, tagLabel={}, colorRgb={})",
			goalId, tagLabel, colorRgb);
		Goal g = findGoal(goalId);
		if (g == null || tagLabel == null || g.getTagIds() == null) return false;
		for (String id : g.getTagIds())
		{
			Tag tag = goalStore.findTag(id);
			if (tag != null && tagLabel.equals(tag.getLabel()))
			{
				boolean changed;
				if (tag.getCategory() == TagCategory.OTHER)
				{
					changed = goalStore.recolorTag(id, colorRgb);
				}
				else
				{
					changed = goalStore.setCategoryColor(tag.getCategory(), colorRgb);
				}
				if (changed) onGoalsChanged.run();
				return changed;
			}
		}
		return false;
	}

	// ---------------------------------------------------------------------
	// Tracker write path (Phase 4)
	// ---------------------------------------------------------------------

	@Override
	public boolean recordGoalProgress(String goalId, int newValue)
	{
		// Intentionally NO save/reconcile/onGoalsChanged here — see Javadoc on
		// the interface. Trackers batch via the plugin's GameTick handler.
		Goal g = findGoal(goalId);
		if (g == null) return false;
		if (g.getCurrentValue() == newValue) return false;

		g.setCurrentValue(newValue);

		boolean meetsTarget = g.meetsTarget();
		boolean wasComplete = g.isComplete();

		if (meetsTarget && !wasComplete)
		{
			g.setCompletedAt(System.currentTimeMillis());
			g.setStatus(com.goaltracker.model.GoalStatus.COMPLETE);
			log.info("API.internal recordGoalProgress: goal complete {} ({})",
				g.getId(), g.getName());
		}
		else if (!meetsTarget && wasComplete)
		{
			// Rare: target was raised or value decreased. Revert to ACTIVE so
			// the reconcile-on-tick pulls the card back out of the Completed
			// section on the next flush.
			g.setCompletedAt(0);
			g.setStatus(com.goaltracker.model.GoalStatus.ACTIVE);
			log.info("API.internal recordGoalProgress: goal un-completed {} ({})",
				g.getId(), g.getName());
		}
		return true;
	}

	// ---------------------------------------------------------------------
	// Selection (Phase 5) — ephemeral, not persisted
	// ---------------------------------------------------------------------

	@Override
	public boolean replaceGoalSelection(java.util.Collection<String> goalIds)
	{
		log.debug("API.internal replaceGoalSelection(size={})", goalIds == null ? 0 : goalIds.size());
		java.util.Set<String> next = new java.util.LinkedHashSet<>();
		if (goalIds != null) next.addAll(goalIds);
		if (next.equals(selectedGoalIds)) return false;
		selectedGoalIds.clear();
		selectedGoalIds.addAll(next);
		onGoalsChanged.run();
		return true;
	}

	@Override
	public boolean addToGoalSelection(String goalId)
	{
		log.debug("API.internal addToGoalSelection(goalId={})", goalId);
		if (goalId == null) return false;
		if (!selectedGoalIds.add(goalId)) return false;
		onGoalsChanged.run();
		return true;
	}

	@Override
	public boolean removeFromGoalSelection(String goalId)
	{
		log.debug("API.internal removeFromGoalSelection(goalId={})", goalId);
		if (goalId == null) return false;
		if (!selectedGoalIds.remove(goalId)) return false;
		onGoalsChanged.run();
		return true;
	}

	@Override
	public boolean clearGoalSelection()
	{
		log.debug("API.internal clearGoalSelection()");
		if (selectedGoalIds.isEmpty()) return false;
		selectedGoalIds.clear();
		onGoalsChanged.run();
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
		if (added > 0) onGoalsChanged.run();
		return added;
	}

	@Override
	public boolean addTagWithCategory(String goalId, String label, String categoryName)
	{
		log.debug("API.internal addTagWithCategory(goalId={}, label={}, category={})",
			goalId, label, categoryName);
		if (goalId == null || label == null || label.trim().isEmpty()) return false;
		Goal g = findGoal(goalId);
		if (g == null) return false;
		TagCategory category;
		try
		{
			category = TagCategory.valueOf(categoryName);
		}
		catch (IllegalArgumentException ex)
		{
			log.warn("addTagWithCategory: unknown category {}", categoryName);
			return false;
		}
		// SKILLING is system-only: only allow attaching an existing skill tag,
		// never creating a new one. Look up via findTag-by-label; null = reject.
		Tag tag;
		if (category == TagCategory.SKILLING)
		{
			tag = goalStore.findTagByLabel(label.trim(), category);
			if (tag == null)
			{
				log.warn("addTagWithCategory: SKILLING tag '{}' does not exist", label);
				return false;
			}
		}
		else
		{
			tag = goalStore.createUserTag(label.trim(), category);
		}
		if (tag == null) return false;
		if (g.getTagIds() == null) g.setTagIds(new ArrayList<>());
		if (!g.getTagIds().contains(tag.getId()))
		{
			g.getTagIds().add(tag.getId());
		}
		goalStore.updateGoal(g);
		onGoalsChanged.run();
		return true;
	}

	// ---------------------------------------------------------------------
	// Tag entity CRUD (Mission 19)
	// ---------------------------------------------------------------------

	@Override
	public List<TagView> queryAllTags()
	{
		log.debug("API.internal queryAllTags()");
		List<TagView> out = new ArrayList<>();
		for (Tag t : goalStore.getTags())
		{
			out.add(toTagView(t));
		}
		return out;
	}

	@Override
	public String createUserTag(String label, String categoryName)
	{
		log.debug("API.internal createUserTag(label={}, category={})", label, categoryName);
		TagCategory category;
		try
		{
			category = TagCategory.valueOf(categoryName);
		}
		catch (IllegalArgumentException ex)
		{
			log.warn("createUserTag: unknown category {}", categoryName);
			return null;
		}
		if (category == TagCategory.SKILLING)
		{
			log.warn("createUserTag: SKILLING category is reserved for system tags");
			return null;
		}
		Tag tag = goalStore.createUserTag(label, category);
		onGoalsChanged.run();
		return tag != null ? tag.getId() : null;
	}

	@Override
	public boolean renameTag(String tagId, String newLabel)
	{
		log.debug("API.internal renameTag(tagId={}, newLabel={})", tagId, newLabel);
		boolean changed = goalStore.renameTag(tagId, newLabel);
		if (changed) onGoalsChanged.run();
		return changed;
	}

	@Override
	public boolean recolorTag(String tagId, int colorRgb)
	{
		// Mission 20: per-tag color is OTHER-only. For non-OTHER tags this
		// auto-delegates to the category color so the call still works for
		// callers (panel right-click bridge, tests) — but only OTHER actually
		// stores per-tag.
		log.debug("API.internal recolorTag(tagId={}, colorRgb={})", tagId, colorRgb);
		Tag t = goalStore.findTag(tagId);
		if (t == null) return false;
		boolean changed;
		if (t.getCategory() == TagCategory.OTHER)
		{
			changed = goalStore.recolorTag(tagId, colorRgb);
		}
		else
		{
			changed = goalStore.setCategoryColor(t.getCategory(), colorRgb);
		}
		if (changed) onGoalsChanged.run();
		return changed;
	}

	@Override
	public boolean setCategoryColor(String categoryName, int colorRgb)
	{
		log.debug("API.internal setCategoryColor(category={}, colorRgb={})", categoryName, colorRgb);
		TagCategory category;
		try { category = TagCategory.valueOf(categoryName); }
		catch (IllegalArgumentException ex) { return false; }
		boolean changed = goalStore.setCategoryColor(category, colorRgb);
		if (changed) onGoalsChanged.run();
		return changed;
	}

	@Override
	public boolean resetCategoryColor(String categoryName)
	{
		log.debug("API.internal resetCategoryColor(category={})", categoryName);
		return setCategoryColor(categoryName, -1);
	}

	@Override
	public int getCategoryColor(String categoryName)
	{
		try { return goalStore.getCategoryColor(TagCategory.valueOf(categoryName)); }
		catch (IllegalArgumentException ex) { return 0; }
	}

	@Override
	public int getCategoryDefaultColor(String categoryName)
	{
		try
		{
			java.awt.Color c = TagCategory.valueOf(categoryName).getColor();
			return (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();
		}
		catch (IllegalArgumentException ex) { return 0; }
	}

	@Override
	public boolean isCategoryColorOverridden(String categoryName)
	{
		try { return goalStore.isCategoryColorOverridden(TagCategory.valueOf(categoryName)); }
		catch (IllegalArgumentException ex) { return false; }
	}

	@Override
	public boolean setTagIcon(String tagId, String iconKey)
	{
		log.debug("API.internal setTagIcon(tagId={}, iconKey={})", tagId, iconKey);
		boolean changed = goalStore.setTagIcon(tagId, iconKey);
		if (changed) onGoalsChanged.run();
		return changed;
	}

	@Override
	public boolean clearTagIcon(String tagId)
	{
		log.debug("API.internal clearTagIcon(tagId={})", tagId);
		return setTagIcon(tagId, null);
	}

	@Override
	public boolean deleteTag(String tagId)
	{
		log.debug("API.internal deleteTag(tagId={})", tagId);
		boolean deleted = goalStore.deleteTag(tagId);
		if (deleted) onGoalsChanged.run();
		return deleted;
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
		if (removed > 0) onGoalsChanged.run();
		return removed;
	}
}
