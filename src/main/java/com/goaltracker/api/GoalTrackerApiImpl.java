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

	/** Mission 26: undo/redo history. Session-only. Tracker-driven mutations
	 *  bypass this — only user actions routed through {@link #executeCommand}
	 *  appear in history. */
	private final com.goaltracker.command.CommandHistory commandHistory =
		new com.goaltracker.command.CommandHistory();

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

		final String goalId = goal.getId();
		final String displayName = goal.getName();
		executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				if (findGoal(goalId) != null) return false;
				goalStore.addGoal(goal);
				int insertBefore = reorderingService.findInsertionIndex(
					skill.name(), targetXp, goal.getSectionId());
				if (insertBefore >= 0)
				{
					goalStore.reorder(goalStore.getGoals().size() - 1, insertBefore);
				}
				return true;
			}
			@Override public boolean revert()
			{
				goalStore.removeGoal(goalId);
				selectedGoalIds.remove(goalId);
				return true;
			}
			@Override public String getDescription() { return "Add goal: " + displayName; }
		});
		log.info("addSkillGoal created: {} ({} → {} XP)", goalId, skill.getName(), targetXp);
		return goalId;
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

		final String goalId = goal.getId();
		final String displayName = goal.getName();
		executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				if (findGoal(goalId) != null) return false;
				goalStore.addGoal(goal);
				return true;
			}
			@Override public boolean revert() { goalStore.removeGoal(goalId); return true; }
			@Override public String getDescription() { return "Add quest: " + displayName; }
		});
		log.info("addQuestGoal created: {} ({})", goalId, quest.getName());
		return goalId;
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

		final String goalId = goal.getId();
		final String displayName = goal.getName();
		final String tierStr = internalTier.getDisplayName();
		executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				if (findGoal(goalId) != null) return false;
				goalStore.addGoal(goal);
				return true;
			}
			@Override public boolean revert() { goalStore.removeGoal(goalId); return true; }
			@Override public String getDescription() { return "Add diary: " + displayName + " " + tierStr; }
		});
		log.info("addDiaryGoal created: {} ({} {})", goalId, areaDisplayName, internalTier);
		return goalId;
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

		final String goalId = goal.getId();
		final String displayName = info.name;
		executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				if (findGoal(goalId) != null) return false;
				goalStore.addGoal(goal);
				return true;
			}
			@Override public boolean revert() { goalStore.removeGoal(goalId); return true; }
			@Override public String getDescription() { return "Add CA: " + displayName; }
		});
		log.info("addCombatAchievementGoal created: {} ({})", goalId, info.name);
		return goalId;
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
		// Mission 26: undoable. Capture the entire Goal entity + its priority
		// so revert can re-insert it where it was. We KEEP the same id so any
		// later commands referencing this goal still work after redo.
		final Goal snapshot = g;
		final int snapshotPriority = g.getPriority();
		final String name = g.getName();
		return executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				goalStore.removeGoal(goalId);
				selectedGoalIds.remove(goalId);
				return true;
			}
			@Override public boolean revert()
			{
				if (findGoal(goalId) != null) return false;
				goalStore.insertGoalAt(snapshot, snapshotPriority);
				return true;
			}
			@Override public String getDescription() { return "Remove: " + name; }
		});
	}

	@Override
	public boolean addTag(String goalId, String label)
	{
		log.debug("API.public addTag(goalId={}, label={})", goalId, label);
		if (goalId == null || label == null || label.trim().isEmpty()) return false;
		Goal g = findGoal(goalId);
		if (g == null) return false;
		Tag tag = goalStore.createUserTag(label.trim(), TagCategory.OTHER);
		if (tag == null) return false;
		if (g.getTagIds() != null && g.getTagIds().contains(tag.getId())) return false; // already has it
		final String tagId = tag.getId();
		final String label2 = label.trim();
		final String name = g.getName();
		return executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				Goal cg = findGoal(goalId);
				if (cg == null) return false;
				if (cg.getTagIds() == null) cg.setTagIds(new ArrayList<>());
				if (cg.getTagIds().contains(tagId)) return false;
				cg.getTagIds().add(tagId);
				goalStore.updateGoal(cg);
				return true;
			}
			@Override public boolean revert()
			{
				Goal cg = findGoal(goalId);
				if (cg == null || cg.getTagIds() == null) return false;
				boolean removed = cg.getTagIds().remove(tagId);
				if (removed) goalStore.updateGoal(cg);
				return removed;
			}
			@Override public String getDescription() { return "Add tag '" + label2 + "' to " + name; }
		});
	}

	@Override
	public boolean removeTag(String goalId, String label)
	{
		log.debug("API.public removeTag(goalId={}, label={})", goalId, label);
		if (goalId == null || label == null) return false;
		Goal g = findGoal(goalId);
		if (g == null || g.getTagIds() == null) return false;
		List<String> defaults = g.getDefaultTagIds() != null ? g.getDefaultTagIds() : java.util.Collections.emptyList();
		String toRemove = null;
		int idx = -1;
		for (int i = 0; i < g.getTagIds().size(); i++)
		{
			String id = g.getTagIds().get(i);
			if (defaults.contains(id)) continue;
			Tag tag = goalStore.findTag(id);
			if (tag != null && label.equals(tag.getLabel()))
			{
				toRemove = id;
				idx = i;
				break;
			}
		}
		if (toRemove == null) return false;
		final String tagId = toRemove;
		final int restoreIdx = idx;
		final String name = g.getName();
		return executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				Goal cg = findGoal(goalId);
				if (cg == null || cg.getTagIds() == null) return false;
				boolean removed = cg.getTagIds().remove(tagId);
				if (removed) goalStore.updateGoal(cg);
				return removed;
			}
			@Override public boolean revert()
			{
				Goal cg = findGoal(goalId);
				if (cg == null) return false;
				if (cg.getTagIds() == null) cg.setTagIds(new ArrayList<>());
				int safeIdx = Math.min(restoreIdx, cg.getTagIds().size());
				cg.getTagIds().add(safeIdx, tagId);
				goalStore.updateGoal(cg);
				return true;
			}
			@Override public String getDescription() { return "Remove tag '" + label + "' from " + name; }
		});
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
		// Mission 26: undoable. Snapshot previous target + name + description
		// since the display strings are auto-derived from the target.
		final int prevTarget = g.getTargetValue();
		final String prevName = g.getName();
		final String prevDescription = g.getDescription();
		final String label = g.getName();
		return executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply() { return changeTargetInternal(goalId, newTarget); }
			@Override public boolean revert()
			{
				Goal cg = findGoal(goalId);
				if (cg == null) return false;
				cg.setTargetValue(prevTarget);
				cg.setName(prevName);
				cg.setDescription(prevDescription);
				goalStore.updateGoal(cg);
				return true;
			}
			@Override public String getDescription() { return "Change target: " + label; }
		});
	}

	private boolean changeTargetInternal(String goalId, int newTarget)
	{
		Goal g = findGoal(goalId);
		if (g == null) return false;
		g.setTargetValue(newTarget);
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
		// Mission 26: undoable. Wrap in a Command that adds/removes the same
		// Goal entity (preserving id) so redo restores the exact same goal
		// and any later commands referencing it still resolve.
		final String goalId = goal.getId();
		executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				if (findGoal(goalId) != null) return false; // already there
				goalStore.addGoal(goal);
				return true;
			}
			@Override public boolean revert()
			{
				goalStore.removeGoal(goalId);
				selectedGoalIds.remove(goalId);
				return true;
			}
			@Override public String getDescription() { return "Add goal: " + trimmedName; }
		});
		log.info("addCustomGoal created: {} ({})", goalId, trimmedName);
		return goalId;
	}

	@Override
	public boolean editCustomGoal(String goalId, String newName, String newDescription)
	{
		log.debug("API.public editCustomGoal(goalId={}, newName={}, newDescription={})",
			goalId, newName, newDescription);
		if (goalId == null) return false;
		Goal g = findGoal(goalId);
		if (g == null || g.getType() != GoalType.CUSTOM) return false;
		final String prevName = g.getName();
		final String prevDesc = g.getDescription();
		final String resolvedName = newName != null && !newName.trim().isEmpty()
			? newName.trim() : prevName;
		final String resolvedDesc = newDescription != null ? newDescription.trim() : prevDesc;
		if (newName != null && newName.trim().isEmpty()) return false;
		if (resolvedName.equals(prevName) && resolvedDesc.equals(prevDesc)) return false;
		return executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				Goal cg = findGoal(goalId);
				if (cg == null) return false;
				cg.setName(resolvedName);
				cg.setDescription(resolvedDesc);
				goalStore.updateGoal(cg);
				return true;
			}
			@Override public boolean revert()
			{
				Goal cg = findGoal(goalId);
				if (cg == null) return false;
				cg.setName(prevName);
				cg.setDescription(prevDesc);
				goalStore.updateGoal(cg);
				return true;
			}
			@Override public String getDescription() { return "Edit: " + prevName; }
		});
	}

	@Override
	public boolean markGoalComplete(String goalId)
	{
		log.debug("API.public markGoalComplete(goalId={})", goalId);
		if (goalId == null) return false;
		Goal g = findGoal(goalId);
		if (g == null) return false;
		if (g.getType() != GoalType.CUSTOM && g.getType() != GoalType.ITEM_GRIND) return false;
		if (g.getStatus() == com.goaltracker.model.GoalStatus.COMPLETE) return false; // already
		// Mission 26: snapshot the previous current value + completedAt so the
		// undo Command can restore them exactly. The status flip is the obvious
		// piece; the timestamp is the subtle one (revert needs to clear it).
		final long prevCompletedAt = g.getCompletedAt();
		final String name = g.getName();
		return executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				return markCompleteInternal(goalId);
			}
			@Override public boolean revert()
			{
				return markIncompleteInternal(goalId, prevCompletedAt);
			}
			@Override public String getDescription() { return "Mark complete: " + name; }
		});
	}

	@Override
	public boolean markGoalIncomplete(String goalId)
	{
		log.debug("API.public markGoalIncomplete(goalId={})", goalId);
		if (goalId == null) return false;
		Goal g = findGoal(goalId);
		if (g == null) return false;
		if (g.getType() != GoalType.CUSTOM && g.getType() != GoalType.ITEM_GRIND) return false;
		if (g.getStatus() != com.goaltracker.model.GoalStatus.COMPLETE) return false;
		final long prevCompletedAt = g.getCompletedAt();
		final String name = g.getName();
		return executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				return markIncompleteInternal(goalId, 0L);
			}
			@Override public boolean revert()
			{
				return markCompleteInternalAt(goalId, prevCompletedAt);
			}
			@Override public String getDescription() { return "Mark incomplete: " + name; }
		});
	}

	/** Raw mutation primitive for marking complete — no command path, no
	 *  onGoalsChanged. Used by command apply()s and (when bypassing the
	 *  undo system entirely) by tracker code paths if needed. */
	private boolean markCompleteInternal(String goalId)
	{
		return markCompleteInternalAt(goalId, System.currentTimeMillis());
	}

	private boolean markCompleteInternalAt(String goalId, long completedAt)
	{
		Goal g = findGoal(goalId);
		if (g == null) return false;
		g.setCompletedAt(completedAt);
		g.setStatus(com.goaltracker.model.GoalStatus.COMPLETE);
		goalStore.updateGoal(g);
		goalStore.reconcileCompletedSection();
		return true;
	}

	private boolean markIncompleteInternal(String goalId, long restoredCompletedAt)
	{
		Goal g = findGoal(goalId);
		if (g == null) return false;
		g.setCompletedAt(restoredCompletedAt);
		g.setStatus(com.goaltracker.model.GoalStatus.ACTIVE);
		goalStore.updateGoal(g);
		goalStore.reconcileCompletedSection();
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
		final List<String> snapshotTagIds = new ArrayList<>(g.getTagIds() != null ? g.getTagIds() : java.util.Collections.emptyList());
		final String name = g.getName();
		return executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				Goal cg = findGoal(goalId);
				if (cg == null) return false;
				cg.setTagIds(new ArrayList<>(cg.getDefaultTagIds()));
				goalStore.updateGoal(cg);
				return true;
			}
			@Override public boolean revert()
			{
				Goal cg = findGoal(goalId);
				if (cg == null) return false;
				cg.setTagIds(new ArrayList<>(snapshotTagIds));
				goalStore.updateGoal(cg);
				return true;
			}
			@Override public String getDescription() { return "Restore defaults: " + name; }
		});
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
		// Mission 26: snapshot every changed goal's pre-state for revert.
		// Snapshot list is the source of truth for both apply (forward) and
		// revert — the Command operates on this exact set, not a re-derived one.
		final java.util.List<String[]> snapshots = new java.util.ArrayList<>(); // [goalId, prevColor, prevTagsCsv]
		for (String goalId : goalIds)
		{
			Goal g = findGoal(goalId);
			if (g == null) continue;
			if (!isGoalOverridden(goalId)) continue;
			String prevTagsCsv = g.getTagIds() != null ? String.join(",", g.getTagIds()) : "";
			snapshots.add(new String[]{ goalId,
				String.valueOf(g.getCustomColorRgb()),
				prevTagsCsv });
		}
		if (snapshots.isEmpty()) return 0;
		boolean ok = executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				for (String[] snap : snapshots)
				{
					Goal g = findGoal(snap[0]);
					if (g == null) continue;
					List<String> defaults = g.getDefaultTagIds() != null ? g.getDefaultTagIds() : java.util.Collections.emptyList();
					g.setTagIds(new ArrayList<>(defaults));
					g.setCustomColorRgb(-1);
					goalStore.updateGoal(g);
				}
				return true;
			}
			@Override public boolean revert()
			{
				for (String[] snap : snapshots)
				{
					Goal g = findGoal(snap[0]);
					if (g == null) continue;
					g.setCustomColorRgb(Integer.parseInt(snap[1]));
					java.util.List<String> tags = snap[2].isEmpty()
						? new ArrayList<>() : new ArrayList<>(java.util.Arrays.asList(snap[2].split(",")));
					g.setTagIds(tags);
					goalStore.updateGoal(g);
				}
				return true;
			}
			@Override public String getDescription()
			{
				return "Restore defaults (" + snapshots.size() + " goals)";
			}
		});
		return ok ? snapshots.size() : 0;
	}

	@Override
	public int bulkRemoveTagFromGoals(java.util.Set<String> goalIds, String tagId)
	{
		log.debug("API.internal bulkRemoveTagFromGoals({} goals, tagId={})",
			goalIds == null ? 0 : goalIds.size(), tagId);
		if (goalIds == null || goalIds.isEmpty() || tagId == null) return 0;
		// Mission 26: snapshot which goals will lose this tag and at what
		// index, so revert can re-insert at the same position.
		final String fTagId = tagId;
		final java.util.List<int[]> snapshots = new java.util.ArrayList<>(); // unused
		final java.util.List<String> goalIdsAffected = new java.util.ArrayList<>();
		final java.util.List<Integer> indices = new java.util.ArrayList<>();
		for (String goalId : goalIds)
		{
			Goal g = findGoal(goalId);
			if (g == null || g.getTagIds() == null) continue;
			if (!g.getTagIds().contains(tagId)) continue;
			boolean isCustom = g.getType() == GoalType.CUSTOM;
			List<String> defaults = g.getDefaultTagIds() != null ? g.getDefaultTagIds() : java.util.Collections.emptyList();
			if (!isCustom && defaults.contains(tagId)) continue;
			goalIdsAffected.add(goalId);
			indices.add(g.getTagIds().indexOf(tagId));
		}
		if (goalIdsAffected.isEmpty()) return 0;
		final Tag tagSnapshot = goalStore.findTag(tagId);
		final String tagLabel = tagSnapshot != null ? tagSnapshot.getLabel() : tagId;
		boolean ok = executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				for (String goalId : goalIdsAffected)
				{
					Goal g = findGoal(goalId);
					if (g == null || g.getTagIds() == null) continue;
					g.getTagIds().remove(fTagId);
					goalStore.updateGoal(g);
				}
				return true;
			}
			@Override public boolean revert()
			{
				for (int i = 0; i < goalIdsAffected.size(); i++)
				{
					Goal g = findGoal(goalIdsAffected.get(i));
					if (g == null) continue;
					if (g.getTagIds() == null) g.setTagIds(new ArrayList<>());
					int idx = Math.min(indices.get(i), g.getTagIds().size());
					g.getTagIds().add(idx, fTagId);
					goalStore.updateGoal(g);
				}
				return true;
			}
			@Override public String getDescription()
			{
				return "Remove tag '" + tagLabel + "' (" + goalIdsAffected.size() + " goals)";
			}
		});
		return ok ? goalIdsAffected.size() : 0;
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

	@Override
	public int bulkRemoveGoals(java.util.Set<String> goalIds)
	{
		log.debug("API.internal bulkRemoveGoals({} goals)", goalIds == null ? 0 : goalIds.size());
		if (goalIds == null || goalIds.isEmpty()) return 0;
		// Snapshot EVERY affected Goal entity + its priority BEFORE any removals.
		// This is the key fix: per-goal removeGoal commands would each snapshot
		// their priority AFTER prior removals reindexed the list, giving
		// inconsistent positions. By capturing atomically we can restore the
		// original layout exactly.
		final java.util.List<Goal> goalSnapshots = new ArrayList<>();
		final java.util.List<Integer> prioritySnapshots = new ArrayList<>();
		for (Goal g : goalStore.getGoals())
		{
			if (goalIds.contains(g.getId()))
			{
				goalSnapshots.add(g);
				prioritySnapshots.add(g.getPriority());
			}
		}
		if (goalSnapshots.isEmpty()) return 0;
		final java.util.Set<String> selectionSnapshot = new java.util.LinkedHashSet<>(selectedGoalIds);
		boolean ok = executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				for (Goal g : goalSnapshots)
				{
					goalStore.removeGoal(g.getId());
					selectedGoalIds.remove(g.getId());
				}
				return true;
			}
			@Override public boolean revert()
			{
				// Insert in ASCENDING priority order so each insert's target
				// index is valid relative to what's already been inserted.
				// Example: B (origPriority=1), C (origPriority=2). After A, D
				// remain as [A, D], inserting B at 1 → [A, B, D], then C at 2
				// → [A, B, C, D]. Exactly the original order.
				java.util.List<Integer> order = new ArrayList<>();
				for (int i = 0; i < goalSnapshots.size(); i++) order.add(i);
				order.sort((a, b) -> Integer.compare(prioritySnapshots.get(a), prioritySnapshots.get(b)));
				for (int idx : order)
				{
					goalStore.insertGoalAt(goalSnapshots.get(idx), prioritySnapshots.get(idx));
				}
				selectedGoalIds.addAll(selectionSnapshot);
				return true;
			}
			@Override public String getDescription()
			{
				return "Remove " + goalSnapshots.size() + " goals";
			}
		});
		return ok ? goalSnapshots.size() : 0;
	}

	@Override
	public int bulkMoveGoalsToSection(java.util.Set<String> goalIds, String targetSectionId)
	{
		log.debug("API.internal bulkMoveGoalsToSection({} goals → {})",
			goalIds == null ? 0 : goalIds.size(), targetSectionId);
		if (goalIds == null || goalIds.isEmpty() || targetSectionId == null) return 0;
		// Snapshot every affected goal's original section + priority BEFORE
		// any moves. Ensures undo restores the exact layout without collapse.
		final java.util.List<String> affectedIds = new ArrayList<>();
		final java.util.List<String> prevSections = new ArrayList<>();
		final java.util.List<Integer> prevPriorities = new ArrayList<>();
		for (Goal g : goalStore.getGoals())
		{
			if (!goalIds.contains(g.getId())) continue;
			if (targetSectionId.equals(g.getSectionId())) continue; // no-op
			affectedIds.add(g.getId());
			prevSections.add(g.getSectionId());
			prevPriorities.add(g.getPriority());
		}
		if (affectedIds.isEmpty()) return 0;
		boolean ok = executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				for (String gid : affectedIds) moveGoalToSectionInternal(gid, targetSectionId);
				return true;
			}
			@Override public boolean revert()
			{
				for (int i = 0; i < affectedIds.size(); i++)
				{
					String gid = affectedIds.get(i);
					goalStore.moveGoalToSection(gid, prevSections.get(i));
					Goal g = findGoal(gid);
					if (g != null) g.setPriority(prevPriorities.get(i));
				}
				goalStore.normalizeOrder();
				return true;
			}
			@Override public String getDescription()
			{
				return "Move " + affectedIds.size() + " goals";
			}
		});
		return ok ? affectedIds.size() : 0;
	}

	// ---------------------------------------------------------------------
	// Undo / redo (Mission 26)
	// ---------------------------------------------------------------------

	/**
	 * Internal entry point for user-mutation API methods. Runs the command
	 * via {@link CommandHistory#execute} so it lands on the undo stack.
	 * Tracker-driven mutations bypass this and call store primitives directly.
	 */
	public boolean executeCommand(com.goaltracker.command.Command cmd)
	{
		boolean ok = commandHistory.execute(cmd);
		if (ok) onGoalsChanged.run();
		return ok;
	}

	@Override public boolean canUndo() { return commandHistory.canUndo(); }
	@Override public boolean canRedo() { return commandHistory.canRedo(); }
	@Override public String peekUndoDescription() { return commandHistory.peekUndoDescription(); }
	@Override public String peekRedoDescription() { return commandHistory.peekRedoDescription(); }

	@Override
	public boolean undo()
	{
		boolean ok = commandHistory.undo();
		if (ok) onGoalsChanged.run();
		return ok;
	}

	@Override
	public boolean redo()
	{
		boolean ok = commandHistory.redo();
		if (ok) onGoalsChanged.run();
		return ok;
	}

	@Override public void beginCompound(String description) { commandHistory.beginCompound(description); }
	@Override public void endCompound() { commandHistory.endCompound(); }

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
		String sourceSectionId = goals.get(currentIndex).getSectionId();
		String targetSectionId = goals.get(newGlobalIndex).getSectionId();
		if (sourceSectionId == null || !sourceSectionId.equals(targetSectionId)) return false;

		// Mission 26: snapshot the FROM global index so revert can move it
		// back. Setting priority alone isn't enough — normalizeOrder has a
		// stable-sort tie-breaker that doesn't distinguish the moved goal
		// from its neighbors at the same priority. The inverse of "move from
		// X to Y" is "move from Y to X", so we re-invoke the reordering
		// service with the indices swapped.
		final int snapshotFromIndex = currentIndex;
		final String name = goals.get(currentIndex).getName();
		return executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				List<Goal> gs = goalStore.getGoals();
				int from = -1;
				for (int i = 0; i < gs.size(); i++)
				{
					if (gs.get(i).getId().equals(goalId)) { from = i; break; }
				}
				if (from < 0 || newGlobalIndex >= gs.size()) return false;
				int delta = Math.abs(newGlobalIndex - from);
				if (delta == 1) reorderingService.moveGoal(from, newGlobalIndex);
				else reorderingService.moveGoalTo(from, newGlobalIndex);
				return true;
			}
			@Override public boolean revert()
			{
				List<Goal> gs = goalStore.getGoals();
				int currentPos = -1;
				for (int i = 0; i < gs.size(); i++)
				{
					if (gs.get(i).getId().equals(goalId)) { currentPos = i; break; }
				}
				if (currentPos < 0) return false;
				if (currentPos == snapshotFromIndex) return true; // already there
				int delta = Math.abs(snapshotFromIndex - currentPos);
				if (delta == 1) reorderingService.moveGoal(currentPos, snapshotFromIndex);
				else reorderingService.moveGoalTo(currentPos, snapshotFromIndex);
				return true;
			}
			@Override public String getDescription() { return "Reorder: " + name; }
		});
	}

	@Override
	public boolean positionGoalInSection(String goalId, String sectionId, int positionInSection)
	{
		log.debug("API.internal positionGoalInSection(goalId={}, sectionId={}, pos={})",
			goalId, sectionId, positionInSection);
		if (goalId == null || sectionId == null) return false;
		Goal g = findGoal(goalId);
		if (g == null) return false;
		// Snapshot for revert: where the goal was before this call.
		final String prevSectionId = g.getSectionId();
		final int prevPriority = g.getPriority();
		final String name = g.getName();
		return executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				return positionGoalInSectionInternal(goalId, sectionId, positionInSection);
			}
			@Override public boolean revert()
			{
				Goal cg = findGoal(goalId);
				if (cg == null) return false;
				if (!prevSectionId.equals(cg.getSectionId()))
				{
					goalStore.moveGoalToSection(goalId, prevSectionId);
				}
				cg.setPriority(prevPriority);
				goalStore.normalizeOrder();
				return true;
			}
			@Override public String getDescription() { return "Reposition: " + name; }
		});
	}

	private boolean positionGoalInSectionInternal(String goalId, String sectionId, int positionInSection)
	{
		Goal g = findGoal(goalId);
		if (g == null) return false;
		boolean changed = false;
		if (!sectionId.equals(g.getSectionId()))
		{
			if (goalStore.moveGoalToSection(goalId, sectionId)) changed = true;
		}
		goalStore.normalizeOrder();
		List<Goal> goals = goalStore.getGoals();
		List<Integer> sectionIndices = new ArrayList<>();
		int sourceIdx = -1;
		for (int i = 0; i < goals.size(); i++)
		{
			if (sectionId.equals(goals.get(i).getSectionId()))
			{
				sectionIndices.add(i);
				if (goalId.equals(goals.get(i).getId())) sourceIdx = i;
			}
		}
		if (sourceIdx < 0) return changed;
		int sectionSize = sectionIndices.size();
		int clampedPos = Math.max(0, Math.min(positionInSection, sectionSize - 1));
		int targetGlobal = sectionIndices.get(clampedPos);
		if (sourceIdx != targetGlobal)
		{
			reorderingService.moveGoalTo(sourceIdx, targetGlobal);
			changed = true;
		}
		return changed;
	}

	@Override
	public void removeAllGoals()
	{
		log.debug("API.internal removeAllGoals()");
		// Mission 26: snapshot every goal so revert can re-add them all in
		// their original order. The Goal entities are kept by reference; this
		// is fine because removeGoal pops them out of the live list and we
		// stash them in a side collection.
		final java.util.List<Goal> snapshot = new ArrayList<>(goalStore.getGoals());
		if (snapshot.isEmpty()) return;
		final java.util.Set<String> selectionSnapshot = new java.util.LinkedHashSet<>(selectedGoalIds);
		executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				while (!goalStore.getGoals().isEmpty())
				{
					goalStore.removeGoal(goalStore.getGoals().get(0).getId());
				}
				selectedGoalIds.clear();
				return true;
			}
			@Override public boolean revert()
			{
				for (Goal g : snapshot)
				{
					if (findGoal(g.getId()) == null) goalStore.addGoal(g);
				}
				goalStore.normalizeOrder();
				selectedGoalIds.addAll(selectionSnapshot);
				return true;
			}
			@Override public String getDescription() { return "Remove all goals (" + snapshot.size() + ")"; }
		});
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
		if (created == null) return null;
		final String sectionId = created.getId();
		final String sectionName = created.getName();
		// Already created. Wrap in a Command for the undo path.
		executeCommand(new com.goaltracker.command.Command()
		{
			private boolean firstApply = true;
			@Override public boolean apply()
			{
				if (firstApply) { firstApply = false; return true; }
				if (goalStore.findSection(sectionId) != null) return false;
				goalStore.recreateUserSection(sectionId, sectionName);
				return true;
			}
			@Override public boolean revert()
			{
				return goalStore.deleteUserSection(sectionId);
			}
			@Override public String getDescription() { return "Add section: " + sectionName; }
		});
		return sectionId;
	}

	@Override
	public boolean renameSection(String sectionId, String newName)
	{
		log.debug("API.internal renameSection(sectionId={}, newName={})", sectionId, newName);
		Section sec = goalStore.findSection(sectionId);
		if (sec == null) return false;
		final String prevName = sec.getName();
		final String resolved = newName != null ? newName.trim() : "";
		if (resolved.isEmpty() || resolved.equals(prevName)) return false;
		return executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply() { return goalStore.renameUserSection(sectionId, resolved); }
			@Override public boolean revert() { return goalStore.renameUserSection(sectionId, prevName); }
			@Override public String getDescription() { return "Rename section: " + prevName + " → " + resolved; }
		});
	}

	@Override
	public boolean deleteSection(String sectionId)
	{
		log.debug("API.internal deleteSection(sectionId={})", sectionId);
		Section sec = goalStore.findSection(sectionId);
		if (sec == null) return false;
		final String name = sec.getName();
		final int order = sec.getOrder();
		final int colorRgb = sec.getColorRgb();
		// Snapshot which goals were in this section so revert can move them back.
		final java.util.List<String> displacedGoalIds = new ArrayList<>();
		for (Goal g : goalStore.getGoals())
		{
			if (sectionId.equals(g.getSectionId())) displacedGoalIds.add(g.getId());
		}
		return executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply() { return goalStore.deleteUserSection(sectionId); }
			@Override public boolean revert()
			{
				goalStore.recreateUserSection(sectionId, name);
				Section restored = goalStore.findSection(sectionId);
				if (restored != null)
				{
					restored.setOrder(order);
					restored.setColorRgb(colorRgb);
				}
				for (String gid : displacedGoalIds)
				{
					goalStore.moveGoalToSection(gid, sectionId);
				}
				goalStore.normalizeOrder();
				return true;
			}
			@Override public String getDescription() { return "Delete section: " + name; }
		});
	}

	@Override
	public boolean reorderSection(String sectionId, int newUserIndex)
	{
		log.debug("API.internal reorderSection(sectionId={}, newUserIndex={})", sectionId, newUserIndex);
		Section sec = goalStore.findSection(sectionId);
		if (sec == null) return false;
		// Compute current user index
		int prevUserIndex = -1;
		int idx = 0;
		for (Section s : goalStore.getSections())
		{
			if (s.getOrder() < Section.ORDER_INCOMPLETE) // skip built-ins (high order)
			{
				if (s.getId().equals(sectionId)) { prevUserIndex = idx; break; }
				idx++;
			}
		}
		final int snapshotPrev = prevUserIndex;
		final String name = sec.getName();
		return executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply() { return goalStore.reorderUserSection(sectionId, newUserIndex); }
			@Override public boolean revert()
			{
				if (snapshotPrev < 0) return false;
				return goalStore.reorderUserSection(sectionId, snapshotPrev);
			}
			@Override public String getDescription() { return "Reorder section: " + name; }
		});
	}

	@Override
	public boolean moveGoalToSection(String goalId, String sectionId)
	{
		log.debug("API.internal moveGoalToSection(goalId={}, sectionId={})", goalId, sectionId);
		Goal current = findGoal(goalId);
		if (current == null) return false;
		if (sectionId == null) return false;
		if (sectionId.equals(current.getSectionId())) return false;
		final String prevSectionId = current.getSectionId();
		final int prevPriority = current.getPriority();
		final String name = current.getName();
		return executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply() { return moveGoalToSectionInternal(goalId, sectionId); }
			@Override public boolean revert()
			{
				Goal g = findGoal(goalId);
				if (g == null) return false;
				goalStore.moveGoalToSection(goalId, prevSectionId);
				g.setPriority(prevPriority);
				goalStore.normalizeOrder();
				return true;
			}
			@Override public String getDescription() { return "Move: " + name; }
		});
	}

	private boolean moveGoalToSectionInternal(String goalId, String sectionId)
	{
		boolean moved = goalStore.moveGoalToSection(goalId, sectionId);
		if (moved)
		{
			Goal g = findGoal(goalId);
			if (g != null && g.getType() == com.goaltracker.model.GoalType.SKILL)
			{
				reorderingService.enforceSkillOrderingInSection(sectionId);
			}
		}
		return moved;
	}

	@Override
	public int removeAllUserSections()
	{
		log.debug("API.internal removeAllUserSections()");
		// Mission 26: snapshot user sections + which goals were in each so we
		// can recreate everything on revert.
		final java.util.List<Section> sectionSnapshots = new ArrayList<>();
		final java.util.Map<String, java.util.List<String>> goalsBySection = new java.util.HashMap<>();
		for (Section s : goalStore.getSections())
		{
			if (s.getBuiltInKind() == null)
			{
				sectionSnapshots.add(s);
				java.util.List<String> ids = new ArrayList<>();
				for (Goal g : goalStore.getGoals())
				{
					if (s.getId().equals(g.getSectionId())) ids.add(g.getId());
				}
				goalsBySection.put(s.getId(), ids);
			}
		}
		if (sectionSnapshots.isEmpty()) return 0;
		boolean ok = executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply() { return goalStore.removeAllUserSections() > 0; }
			@Override public boolean revert()
			{
				for (Section s : sectionSnapshots)
				{
					goalStore.recreateUserSection(s.getId(), s.getName());
					Section restored = goalStore.findSection(s.getId());
					if (restored != null)
					{
						restored.setOrder(s.getOrder());
						restored.setColorRgb(s.getColorRgb());
					}
					for (String gid : goalsBySection.get(s.getId()))
					{
						goalStore.moveGoalToSection(gid, s.getId());
					}
				}
				goalStore.normalizeOrder();
				return true;
			}
			@Override public String getDescription() { return "Remove all sections (" + sectionSnapshots.size() + ")"; }
		});
		return ok ? sectionSnapshots.size() : 0;
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
		final int prevColor = section.getColorRgb();
		final String name = section.getName();
		return executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				Section s = goalStore.findSection(sectionId);
				if (s == null) return false;
				s.setColorRgb(normalized);
				goalStore.save();
				return true;
			}
			@Override public boolean revert()
			{
				Section s = goalStore.findSection(sectionId);
				if (s == null) return false;
				s.setColorRgb(prevColor);
				goalStore.save();
				return true;
			}
			@Override public String getDescription() { return "Recolor section: " + name; }
		});
	}

	@Override
	public boolean setGoalColor(String goalId, int colorRgb)
	{
		log.debug("API.internal setGoalColor(goalId={}, colorRgb={})", goalId, colorRgb);
		Goal g = findGoal(goalId);
		if (g == null) return false;
		int normalized = colorRgb < 0 ? -1 : (colorRgb & 0xFFFFFF);
		if (g.getCustomColorRgb() == normalized) return false;
		// Mission 26: undoable. Snapshot the previous color so revert restores it.
		final int previousColor = g.getCustomColorRgb();
		final String name = g.getName();
		return executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply() { return setGoalColorInternal(goalId, normalized); }
			@Override public boolean revert() { return setGoalColorInternal(goalId, previousColor); }
			@Override public String getDescription() { return "Recolor: " + name; }
		});
	}

	private boolean setGoalColorInternal(String goalId, int normalized)
	{
		Goal g = findGoal(goalId);
		if (g == null) return false;
		g.setCustomColorRgb(normalized);
		goalStore.save();
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
		if (g.getTagIds() != null && g.getTagIds().contains(tag.getId())) return false;
		final String tagId = tag.getId();
		final String tagLabel = tag.getLabel();
		final String name = g.getName();
		return executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				Goal cg = findGoal(goalId);
				if (cg == null) return false;
				if (cg.getTagIds() == null) cg.setTagIds(new ArrayList<>());
				if (cg.getTagIds().contains(tagId)) return false;
				cg.getTagIds().add(tagId);
				goalStore.updateGoal(cg);
				return true;
			}
			@Override public boolean revert()
			{
				Goal cg = findGoal(goalId);
				if (cg == null || cg.getTagIds() == null) return false;
				boolean removed = cg.getTagIds().remove(tagId);
				if (removed) goalStore.updateGoal(cg);
				return removed;
			}
			@Override public String getDescription() { return "Add tag '" + tagLabel + "' to " + name; }
		});
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
		// Use the same find-or-create semantics. If it already existed, no
		// command — return existing id with no history entry.
		Tag existing = goalStore.findTagByLabel(label != null ? label.trim() : "", category);
		if (existing != null) return existing.getId();
		Tag tag = goalStore.createUserTag(label, category);
		if (tag == null) return null;
		final Tag captured = tag;
		final String tagLabel = tag.getLabel();
		executeCommand(new com.goaltracker.command.Command()
		{
			private boolean firstApply = true;
			@Override public boolean apply()
			{
				if (firstApply) { firstApply = false; return true; }
				goalStore.recreateTag(captured);
				return true;
			}
			@Override public boolean revert() { return goalStore.deleteTag(captured.getId()); }
			@Override public String getDescription() { return "Create tag: " + tagLabel; }
		});
		return tag.getId();
	}

	@Override
	public boolean renameTag(String tagId, String newLabel)
	{
		log.debug("API.internal renameTag(tagId={}, newLabel={})", tagId, newLabel);
		Tag t = goalStore.findTag(tagId);
		if (t == null) return false;
		final String prevLabel = t.getLabel();
		final String resolved = newLabel != null ? newLabel.trim() : "";
		if (resolved.isEmpty() || resolved.equals(prevLabel)) return false;
		return executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply() { return goalStore.renameTag(tagId, resolved); }
			@Override public boolean revert() { return goalStore.renameTag(tagId, prevLabel); }
			@Override public String getDescription() { return "Rename tag: " + prevLabel + " → " + resolved; }
		});
	}

	@Override
	public boolean recolorTag(String tagId, int colorRgb)
	{
		log.debug("API.internal recolorTag(tagId={}, colorRgb={})", tagId, colorRgb);
		Tag t = goalStore.findTag(tagId);
		if (t == null) return false;
		final int prevColor = t.getColorRgb();
		final TagCategory cat = t.getCategory();
		final int prevCategoryColor = goalStore.isCategoryColorOverridden(cat)
			? goalStore.getCategoryColor(cat) : -1;
		final String label = t.getLabel();
		return executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				if (cat == TagCategory.OTHER) return goalStore.recolorTag(tagId, colorRgb);
				return goalStore.setCategoryColor(cat, colorRgb);
			}
			@Override public boolean revert()
			{
				if (cat == TagCategory.OTHER) return goalStore.recolorTag(tagId, prevColor);
				return goalStore.setCategoryColor(cat, prevCategoryColor);
			}
			@Override public String getDescription() { return "Recolor tag: " + label; }
		});
	}

	@Override
	public boolean setCategoryColor(String categoryName, int colorRgb)
	{
		log.debug("API.internal setCategoryColor(category={}, colorRgb={})", categoryName, colorRgb);
		TagCategory category;
		try { category = TagCategory.valueOf(categoryName); }
		catch (IllegalArgumentException ex) { return false; }
		final int prevColor = goalStore.isCategoryColorOverridden(category)
			? goalStore.getCategoryColor(category) : -1;
		return executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply() { return goalStore.setCategoryColor(category, colorRgb); }
			@Override public boolean revert() { return goalStore.setCategoryColor(category, prevColor); }
			@Override public String getDescription() { return "Recolor category: " + category.name(); }
		});
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
		Tag t = goalStore.findTag(tagId);
		if (t == null) return false;
		final String prevIcon = t.getIconKey();
		final String label = t.getLabel();
		return executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply() { return goalStore.setTagIcon(tagId, iconKey); }
			@Override public boolean revert() { goalStore.setTagIcon(tagId, prevIcon); return true; }
			@Override public String getDescription() { return "Set icon: " + label; }
		});
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
		Tag t = goalStore.findTag(tagId);
		if (t == null || t.isSystem()) return false;
		// Snapshot the tag entity AND the per-goal references so revert can
		// restore both. We also snapshot defaultTagIds membership for goals
		// that had this tag in their defaults.
		final Tag captured = t;
		final java.util.List<String> tagOnGoals = new ArrayList<>();
		final java.util.List<String> tagOnDefaults = new ArrayList<>();
		for (Goal g : goalStore.getGoals())
		{
			if (g.getTagIds() != null && g.getTagIds().contains(tagId)) tagOnGoals.add(g.getId());
			if (g.getDefaultTagIds() != null && g.getDefaultTagIds().contains(tagId)) tagOnDefaults.add(g.getId());
		}
		final String label = t.getLabel();
		return executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply() { return goalStore.deleteTag(tagId); }
			@Override public boolean revert()
			{
				goalStore.recreateTag(captured);
				for (String gid : tagOnGoals)
				{
					Goal g = findGoal(gid);
					if (g == null) continue;
					if (g.getTagIds() == null) g.setTagIds(new ArrayList<>());
					if (!g.getTagIds().contains(tagId)) g.getTagIds().add(tagId);
				}
				for (String gid : tagOnDefaults)
				{
					Goal g = findGoal(gid);
					if (g == null) continue;
					if (g.getDefaultTagIds() == null) g.setDefaultTagIds(new ArrayList<>());
					if (!g.getDefaultTagIds().contains(tagId)) g.getDefaultTagIds().add(tagId);
				}
				return true;
			}
			@Override public String getDescription() { return "Delete tag: " + label; }
		});
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
