package com.goalplanner.api;

import com.goalplanner.data.AchievementDiaryData;
import com.goalplanner.data.CombatAchievementData;
import com.goalplanner.data.ItemSourceData;
import com.goalplanner.data.WikiCaRepository;
import com.goalplanner.model.AccountMetric;
import com.goalplanner.model.Goal;
import com.goalplanner.model.GoalType;
import com.goalplanner.model.ItemTag;
import com.goalplanner.model.Tag;
import com.goalplanner.model.TagCategory;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Experience;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

/**
 * Encapsulates all goal-creation methods extracted from {@link GoalPlannerApiImpl}.
 * Package-private — only {@link GoalPlannerApiImpl} instantiates and delegates to this.
 */
@Slf4j
class GoalCreationService
{
	/** Max level supported (1-99 normal, 100-126 virtual). */
	private static final int MAX_LEVEL = 126;

	/** Max experience per skill in OSRS. */
	private static final int MAX_XP = 200_000_000;

	private final GoalPlannerApiImpl api;

	GoalCreationService(GoalPlannerApiImpl api)
	{
		this.api = api;
	}

	String addSkillGoal(Skill skill, int targetXp)
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
			.name(skill.getName() + " - Level " + targetLevel)
			.skillName(skill.name())
			.targetValue(targetXp)
			.build();

		final String goalId = goal.getId();
		final String displayName = goal.getName();
		// Wrap create + auto-link in a compound so one undo
		// reverses the whole gesture (the new goal AND all the chain
		// edges that got added to existing same-skill goals).
		//
		// The old GoalReorderingService.findInsertionIndex + goalStore.reorder
		// call that used to run here is gone — autoLinkSkillOrItemChain creates
		// explicit requirement edges between same-skill goals, and the
		// per-section topological sort in queryGoalsTopologicallySorted handles
		// the visual ordering based on those edges. No more implicit
		// priority-based chain sort.
		api.beginCompound("Add goal: " + displayName);
		try
		{
			api.executeCommand(new com.goalplanner.command.Command()
			{
				@Override public boolean apply()
				{
					if (api.findGoal(goalId) != null) return false;
					api.goalStore.addGoal(goal);
					return true;
				}
				@Override public boolean revert()
				{
					api.goalStore.removeGoal(goalId);
					api.selectedGoalIds.remove(goalId);
					return true;
				}
				@Override public String getDescription() { return "Add goal: " + displayName; }
			});
			autoLinkSkillOrItemChain(goal);
		}
		finally
		{
			api.endCompound();
		}
		log.info("addSkillGoal created: {} ({} → {} XP)", goalId, skill.getName(), targetXp);
		return goalId;
	}

	/**
	 * When a new SKILL or ITEM_GRIND goal is added, scan all
	 * existing goals globally for same-identity same-type goals and
	 * auto-link them based on target ordering:
	 * <ul>
	 *   <li>If an existing goal has a LOWER target → new goal requires it
	 *       (the existing one is a prerequisite in the chain)</li>
	 *   <li>If an existing goal has a HIGHER target → it requires the new
	 *       goal (the new one is a prerequisite in the chain)</li>
	 *   <li>Equal target → no edge (they're either duplicates and one will
	 *       lose, or the caller has a reason to keep both)</li>
	 * </ul>
	 *
	 * <p>Cross-section: auto-link is global by design — the user flagged
	 * that the relation graph is the source of truth, and topo sort is a
	 * per-section projection. A 50 Prayer goal in the Skills section is
	 * still a prerequisite of a 90 Prayer goal in the Quest Cape section.
	 *
	 * <p>Quiet on failure: cycle detection (via addRequirement) silently
	 * skips edges that would close a cycle, which shouldn't happen in
	 * practice for same-identity numeric chains but is defended against.
	 *
	 * <p>Caller MUST be inside a beginCompound/endCompound block so the
	 * auto-linked edges collapse into the same undo entry as the goal
	 * creation.
	 */
	private void autoLinkSkillOrItemChain(Goal newGoal)
	{
		if (newGoal == null) return;
		GoalType type = newGoal.getType();
		if (type != GoalType.SKILL && type != GoalType.ITEM_GRIND && type != GoalType.ACCOUNT) return;

		String newId = newGoal.getId();
		int newTarget = newGoal.getTargetValue();
		// Copy the goal list so we're iterating a stable snapshot — addRequirement
		// doesn't mutate the list but future code might, and copying is cheap.
		List<Goal> snapshot = new ArrayList<>(api.goalStore.getGoals());
		for (Goal other : snapshot)
		{
			if (other.getId().equals(newId)) continue;
			if (other.getType() != type) continue;
			// Identity check per type
			if (type == GoalType.SKILL)
			{
				if (newGoal.getSkillName() == null
					|| other.getSkillName() == null
					|| !newGoal.getSkillName().equals(other.getSkillName())) continue;
			}
			else if (type == GoalType.ACCOUNT)
			{
				if (newGoal.getAccountMetric() == null
					|| other.getAccountMetric() == null
					|| !newGoal.getAccountMetric().equals(other.getAccountMetric())) continue;
			}
			else // ITEM_GRIND
			{
				if (newGoal.getItemId() != other.getItemId()) continue;
			}
			int otherTarget = other.getTargetValue();
			if (otherTarget < newTarget)
			{
				// other is a prereq of new → edge new → other
				api.addRequirement(newId, other.getId());
			}
			else if (otherTarget > newTarget)
			{
				// new is a prereq of other → edge other → new
				api.addRequirement(other.getId(), newId);
			}
		}
	}

	String addSkillGoalForLevel(Skill skill, int level)
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
		for (Goal g : api.goalStore.getGoals())
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

	String addItemGoal(int itemId, int targetQuantity)
	{
		log.debug("API.public addItemGoal(itemId={}, qty={})", itemId, targetQuantity);
		if (itemId <= 0 || targetQuantity <= 0)
		{
			log.warn("addItemGoal: invalid input itemId={} qty={}", itemId, targetQuantity);
			return null;
		}

		// Duplicate guard: same item id (any qty) → existing wins
		for (Goal g : api.goalStore.getGoals())
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
			itemName = api.itemManager.getItemComposition(itemId).getName();
			// Strip the "(Members)" suffix that some items have in-game
			if (itemName != null && itemName.endsWith("(Members)"))
			{
				itemName = itemName.substring(0, itemName.length() - "(Members)".length()).trim();
			}
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
			Tag tag = api.goalStore.findOrCreateSystemTag(spec.getLabel(), spec.getCategory());
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

		final String goalId = goal.getId();
		final String displayName = itemName;
		api.executeCommand(new com.goalplanner.command.Command()
		{
			@Override public boolean apply()
			{
				if (api.findGoal(goalId) != null) return false;
				api.goalStore.addGoal(goal);
				return true;
			}
			@Override public boolean revert()
			{
				api.goalStore.removeGoal(goalId);
				return true;
			}
			@Override public String getDescription() { return "Add item goal: " + displayName; }
		});
		log.info("addItemGoal created: {} ({} x {})", goalId, targetQuantity, itemName);
		return goalId;
	}

	String addQuestGoal(Quest quest)
	{
		log.debug("API.public addQuestGoal(quest={})", quest);
		if (quest == null)
		{
			log.warn("addQuestGoal: quest is null");
			return null;
		}
		api.clearGoalSelection();

		// Duplicate guard: same questName
		String questName = quest.name();
		for (Goal g : api.goalStore.getGoals())
		{
			if (g.getType() == GoalType.QUEST && questName.equals(g.getQuestName()))
			{
				log.info("addQuestGoal: duplicate of existing goal {} ({})", g.getId(), questName);
				return g.getId();
			}
		}

		int qpReward = com.goalplanner.data.QuestRequirements.questPointReward(quest);
		String description;
		if (qpReward > 0)
		{
			description = qpReward + " Quest Point" + (qpReward != 1 ? "s" : "");
		}
		else if (com.goalplanner.data.QuestRequirements.isMiniquest(quest))
		{
			description = "Miniquest";
		}
		else
		{
			description = "Quest";
		}

		Goal goal = Goal.builder()
			.type(GoalType.QUEST)
			.name(quest.getName())
			.description(description)
			.questName(questName)
			.targetValue(1)
			.currentValue(0)
			.spriteId(GoalPlannerApiImpl.QUEST_SPRITE_ID)
			.build();

		final String goalId = goal.getId();
		final String displayName = goal.getName();
		api.beginCompound("Add quest: " + displayName);
		try
		{
			api.executeCommand(new com.goalplanner.command.Command()
			{
				@Override public boolean apply()
				{
					if (api.findGoal(goalId) != null) return false;
					api.goalStore.addGoal(goal);
					return true;
				}
				@Override public boolean revert() { api.goalStore.removeGoal(goalId); return true; }
				@Override public String getDescription() { return "Add quest: " + displayName; }
			});
			// Auto-tag F2P quests with a gray "F2P" pill.
			if (com.goalplanner.data.QuestRequirements.isF2P(quest))
			{
				api.addTagWithCategory(goalId, "F2P", TagCategory.OTHER.name());
				Tag f2pTag = api.goalStore.findTagByLabel("F2P", TagCategory.OTHER);
				if (f2pTag != null && f2pTag.getColorRgb() < 0)
				{
					api.goalStore.recolorTag(f2pTag.getId(), GoalPlannerApiImpl.F2P_TAG_COLOR_RGB);
				}
			}
			// Auto-tag XP reward skills as SKILLING icons.
			for (net.runelite.api.Skill rewardSkill : com.goalplanner.data.QuestRequirements.xpRewards(quest))
			{
				api.addTagWithCategory(goalId, rewardSkill.getName(), TagCategory.SKILLING.name());
			}
			// Auto-tag lamp-reward quests with a lamp icon.
			if (com.goalplanner.data.QuestRequirements.rewardsLamp(quest))
			{
				api.addTagWithCategory(goalId, "Lamp", TagCategory.OTHER.name());
				Tag lampTag = api.goalStore.findTagByLabel("Lamp", TagCategory.OTHER);
				if (lampTag != null && lampTag.getIconKey() == null)
				{
					api.goalStore.setTagIcon(lampTag.getId(), GoalPlannerApiImpl.LAMP_ICON_KEY);
				}
			}
		}
		finally
		{
			api.endCompound();
		}
		log.info("addQuestGoal created: {} ({})", goalId, quest.getName());
		return goalId;
	}

	String addQuestGoalWithPrereqs(Quest quest, java.util.List<Goal> prereqTemplates)
	{
		log.debug("API.public addQuestGoalWithPrereqs(quest={}, prereqs={})",
			quest, prereqTemplates == null ? 0 : prereqTemplates.size());
		if (quest == null)
		{
			log.warn("addQuestGoalWithPrereqs: quest is null");
			return null;
		}
		api.clearGoalSelection();
		if (prereqTemplates == null)
		{
			prereqTemplates = java.util.Collections.emptyList();
		}

		// Degenerate case — no templates means no compound is needed, just
		// delegate to the single-goal path. Preserves existing behavior
		// exactly for callers who hit this branch unexpectedly.
		if (prereqTemplates.isEmpty())
		{
			return addQuestGoal(quest);
		}

		// Wrap the whole gesture in a compound so one undo reverses the
		// quest goal, every seeded prereq (direct + transitive), every
		// requirement edge, and every tag attachment.
		api.beginCompound("Add quest with requirements: " + quest.getName());
		try
		{
			String questGoalId = addQuestGoal(quest);
			if (questGoalId == null)
			{
				log.warn("addQuestGoalWithPrereqs: addQuestGoal returned null for {}", quest);
				return null;
			}

			// Cycle guard — questlines are DAGs in practice, but guard
			// anyway so a bad data entry can't infinite-loop the plugin.
			// Seed with the root quest so we don't recurse back into it.
			java.util.Set<Quest> visited = new java.util.HashSet<>();
			visited.add(quest);

			// Snapshot goal IDs that existed BEFORE this gesture so the
			// hybrid skill helper only reuses pre-existing user goals,
			// not goals created during the same gesture.
			java.util.Set<String> preExistingGoalIds = new java.util.HashSet<>();
			for (Goal g : api.goalStore.getGoals())
			{
				preExistingGoalIds.add(g.getId());
			}

			// Track all goal IDs touched during the BFS for selection.
			java.util.List<String> gestureGoalIds = new java.util.ArrayList<>();
			gestureGoalIds.add(questGoalId);

			seedPrereqsInto(questGoalId, quest, prereqTemplates, visited, preExistingGoalIds, gestureGoalIds);

			// Assign priorities so the topo sort renders correctly:
			// 1. Zero-dependency QUEST goals at the very top (these are
			//    leaf quests like Rune Mysteries, Death Plateau — do first)
			// 2. Everything else in reversed creation order (deepest leaves
			//    near top, root quest at bottom)
			java.util.Set<String> gestureSet = new java.util.HashSet<>(gestureGoalIds);
			java.util.List<String> zeroDegQuests = new java.util.ArrayList<>();
			java.util.List<String> others = new java.util.ArrayList<>();
			for (String id : gestureGoalIds)
			{
				Goal g = api.findGoal(id);
				if (g != null && g.getType() == GoalType.QUEST && g.getQuestName() != null)
				{
					boolean isKnownLeaf = false;
					try
					{
						Quest q = Quest.valueOf(g.getQuestName());
						com.goalplanner.data.QuestRequirements.Reqs reqs =
							com.goalplanner.data.QuestRequirements.lookup(q);
						if (reqs != null && !com.goalplanner.data.QuestRequirements.hasRequirements(q))
						{
							isKnownLeaf = true;
						}
					}
					catch (IllegalArgumentException ignored) {}

					if (isKnownLeaf)
					{
						boolean hasInGestureReqs = false;
						if (g.getRequiredGoalIds() != null)
						{
							for (String reqId : g.getRequiredGoalIds())
							{
								if (gestureSet.contains(reqId))
								{
									hasInGestureReqs = true;
									break;
								}
							}
						}
						if (!hasInGestureReqs)
						{
							zeroDegQuests.add(id);
							continue;
						}
					}
				}
				others.add(id);
			}
			java.util.Collections.reverse(others);

			int p = 0;
			for (String id : zeroDegQuests)
			{
				Goal g = api.findGoal(id);
				if (g != null) { g.setPriority(p++); api.goalStore.updateGoal(g); }
			}
			for (String id : others)
			{
				Goal g = api.findGoal(id);
				if (g != null) { g.setPriority(p++); api.goalStore.updateGoal(g); }
			}

			// Promote leaf quests (zero active requirements) to the top
			// so the user sees "do first" goals at the top of the list.
			api.reorderingService.promoteLeafGoalsToTop();

			// Select all goals created by the gesture. Add directly to
			// the ephemeral set (no per-goal callback) — the compound's
			// endCompound fires onGoalsChanged once for the whole batch.
			api.selectedGoalIds.addAll(gestureGoalIds);

			return questGoalId;
		}
		finally
		{
			api.endCompound();
		}
	}

	/**
	 * Individual queue entry for {@link #seedPrereqsInto}: one template
	 * paired with its parent goal/quest context. Skills and quests from
	 * the same parent are split into separate entries so the priority
	 * queue can interleave them globally.
	 */
	private static final class SeedEntry
	{
		final String parentGoalId;
		final Quest parentQuest;   // null for non-quest parents (diary, unlock)
		final String tagLabel;     // tag to apply to seeded goals
		final Goal template;

		SeedEntry(String parentGoalId, Quest parentQuest, Goal template)
		{
			this.parentGoalId = parentGoalId;
			this.parentQuest = parentQuest;
			this.tagLabel = parentQuest != null
				? com.goalplanner.data.QuestRequirements.displayName(parentQuest) : null;
			this.template = template;
		}

		SeedEntry(String parentGoalId, String tagLabel, Goal template)
		{
			this.parentGoalId = parentGoalId;
			this.parentQuest = null;
			this.tagLabel = tagLabel;
			this.template = template;
		}
	}

	/**
	 * Priority-queue BFS: seed prereq templates with skills always
	 * processed before quests. Whenever a quest's children are
	 * discovered, skill children jump to the front of the queue
	 * ahead of any pending quests. This produces a creation order
	 * where all reachable skill goals are created first, followed
	 * by quest goals.
	 *
	 * <p><b>QUEST templates</b> go through the public
	 * {@link #addQuestGoal(Quest)} API (canonical sprite, duplicate
	 * guard, future-proof). <b>SKILL templates</b> go through
	 * {@link #findOrCreateSkillGoalForSeed} which reuses pre-existing
	 * USER goals (non-{@code autoSeeded}) but always creates a new
	 * goal for each distinct target — so "15 Agility" and "32 Agility"
	 * in the same gesture both get their own card.
	 *
	 * <p>Caller must already be inside a {@code beginCompound}/
	 * {@code endCompound} block.
	 */
	/** Quest-rooted overload for backward compatibility. */
	private void seedPrereqsInto(
		String rootGoalId,
		Quest rootQuest,
		java.util.List<Goal> rootTemplates,
		java.util.Set<Quest> visited,
		java.util.Set<String> preExistingGoalIds,
		java.util.List<String> gestureGoalIds)
	{
		seedPrereqsInto(rootGoalId, rootQuest,
			com.goalplanner.data.QuestRequirements.displayName(rootQuest),
			rootTemplates, visited, preExistingGoalIds, gestureGoalIds);
	}

	/**
	 * Generalized prereq seeding with 3-queue priority BFS.
	 * Works for both quest prereqs and diary prereqs.
	 *
	 * @param rootGoalId   the parent goal to link prereqs to
	 * @param rootQuest    the quest (null for diary/unlock parents)
	 * @param rootTagLabel tag to apply to seeded goals
	 * @param rootTemplates templates to seed
	 * @param visited      cycle guard for recursive quest resolution
	 * @param preExistingGoalIds goal IDs that existed before this gesture
	 * @param gestureGoalIds accumulator for all created goal IDs
	 */
	private void seedPrereqsInto(
		String rootGoalId,
		Quest rootQuest,
		String rootTagLabel,
		java.util.List<Goal> rootTemplates,
		java.util.Set<Quest> visited,
		java.util.Set<String> preExistingGoalIds,
		java.util.List<String> gestureGoalIds)
	{
		// Three-queue priority system: optional recommendations (combat level,
		// recommended skills) process first, then required skills/account goals,
		// then quests. This puts optional goals at the top of the card list so
		// the user sees them prominently.
		java.util.ArrayDeque<SeedEntry> optionalPriority = new java.util.ArrayDeque<>();
		java.util.ArrayDeque<SeedEntry> highPriority = new java.util.ArrayDeque<>();
		java.util.ArrayDeque<SeedEntry> lowPriority = new java.util.ArrayDeque<>();

		// Sort skill templates by target value descending so higher-level
		// skills are processed (and thus positioned) first in the card list.
		java.util.List<Goal> sortedTemplates = new java.util.ArrayList<>(rootTemplates);
		sortedTemplates.sort((a, b) -> {
			boolean aSkill = a != null && a.getType() == GoalType.SKILL;
			boolean bSkill = b != null && b.getType() == GoalType.SKILL;
			if (aSkill && bSkill) return Integer.compare(a.getTargetValue(), b.getTargetValue());
			return 0; // preserve original order for non-skill types
		});

		// Seed initial templates into the appropriate queue
		for (Goal t : sortedTemplates)
		{
			if (t == null) continue;
			SeedEntry entry = new SeedEntry(rootGoalId, rootTagLabel, t);
			if (t.isOptional())
			{
				optionalPriority.add(entry);
			}
			else if (t.getType() == GoalType.QUEST)
			{
				lowPriority.add(entry);
			}
			else
			{
				highPriority.add(entry);
			}
		}

		while (!optionalPriority.isEmpty() || !highPriority.isEmpty() || !lowPriority.isEmpty())
		{
			// Drain optional first, then skills/account, then quests
			SeedEntry entry;
			if (!optionalPriority.isEmpty())
			{
				entry = optionalPriority.poll();
			}
			else if (!highPriority.isEmpty())
			{
				entry = highPriority.poll();
			}
			else
			{
				entry = lowPriority.poll();
			}

			Goal template = entry.template;
			final String parentTagLabel = entry.tagLabel;
			Goal parentGoal = api.findGoal(entry.parentGoalId);
			String sectionId = parentGoal == null ? null : parentGoal.getSectionId();

			String seedGoalId;
			Quest childQuestForNextLevel = null;

			if (template.getType() == GoalType.QUEST && template.getQuestName() != null)
			{
				Quest childQuest;
				try
				{
					childQuest = Quest.valueOf(template.getQuestName());
				}
				catch (IllegalArgumentException e)
				{
					log.warn("seedPrereqsInto: unknown Quest enum name '{}'", template.getQuestName());
					continue;
				}
				seedGoalId = addQuestGoal(childQuest);
				if (seedGoalId == null)
				{
					log.warn("seedPrereqsInto: addQuestGoal returned null for {}", childQuest);
					continue;
				}
				if (sectionId != null)
				{
					Goal created = api.findGoal(seedGoalId);
					if (created != null && !sectionId.equals(created.getSectionId()))
					{
						api.moveGoalToSection(seedGoalId, sectionId);
					}
				}
				childQuestForNextLevel = childQuest;
			}
			else if (template.getType() == GoalType.SKILL && template.getSkillName() != null)
			{
				net.runelite.api.Skill skill;
				try
				{
					skill = net.runelite.api.Skill.valueOf(template.getSkillName());
				}
				catch (IllegalArgumentException e)
				{
					log.warn("seedPrereqsInto: unknown Skill enum name '{}'", template.getSkillName());
					continue;
				}
				seedGoalId = findOrCreateSkillGoalForSeed(skill, template.getTargetValue(), preExistingGoalIds);
				if (seedGoalId == null)
				{
					log.warn("seedPrereqsInto: findOrCreateSkillGoalForSeed returned null for {}", skill);
					continue;
				}
			}
			else if (template.getType() == GoalType.ACCOUNT && template.getAccountMetric() != null)
			{
				seedGoalId = addAccountGoal(template.getAccountMetric(), template.getTargetValue());
				if (seedGoalId == null)
				{
					log.warn("seedPrereqsInto: addAccountGoal returned null for {} {}",
						template.getAccountMetric(), template.getTargetValue());
					continue;
				}
			}
			else if (template.getType() == GoalType.BOSS && template.getBossName() != null)
			{
				seedGoalId = addBossGoal(template.getBossName(), template.getTargetValue());
				if (seedGoalId == null)
				{
					log.warn("seedPrereqsInto: addBossGoal returned null for {}", template.getBossName());
					continue;
				}
			}
			else if (template.getType() == GoalType.ITEM_GRIND && template.getItemId() > 0)
			{
				seedGoalId = addItemGoal(template.getItemId(), template.getTargetValue());
				if (seedGoalId == null)
				{
					log.warn("seedPrereqsInto: addItemGoal returned null for itemId={}", template.getItemId());
					continue;
				}
			}
			else
			{
				GoalPlannerInternalApi.FindOrCreateResult result = api.findOrCreateRequirement(template, sectionId);
				if (result == null)
				{
					log.warn("seedPrereqsInto: findOrCreateRequirement returned null for template type={}",
						template.getType());
					continue;
				}
				seedGoalId = result.goalId;
			}

			// Skip completed goals — they're already done, no need to
			// link them as requirements in the tree.
			Goal seedGoal = api.findGoal(seedGoalId);
			if (seedGoal != null && seedGoal.isComplete())
			{
				continue;
			}

			// Propagate optional flag from template to created goal.
			if (template.isOptional())
			{
				api.setGoalOptional(seedGoalId, true);
			}

			gestureGoalIds.add(seedGoalId);
			api.addRequirement(entry.parentGoalId, seedGoalId);
			try
			{
				api.addTagWithCategory(seedGoalId, parentTagLabel, TagCategory.QUEST.name());
			}
			catch (Exception e)
			{
				log.warn("seedPrereqsInto: failed to tag {} with '{}': {}", seedGoalId, parentTagLabel, e.getMessage());
			}

			// Discover child quest's prereqs and route to appropriate queue.
			if (childQuestForNextLevel != null)
			{
				if (!visited.add(childQuestForNextLevel))
				{
					continue;
				}
				com.goalplanner.data.QuestRequirementResolver.Resolved childResolved =
					api.resolveQuestRequirements(childQuestForNextLevel);
				// Sort child skill templates highest-level-first.
				java.util.List<Goal> sortedChildTemplates = new java.util.ArrayList<>(childResolved.templates);
				sortedChildTemplates.sort((a, b) -> {
					boolean aSkill = a != null && a.getType() == GoalType.SKILL;
					boolean bSkill = b != null && b.getType() == GoalType.SKILL;
					if (aSkill && bSkill) return Integer.compare(a.getTargetValue(), b.getTargetValue());
					return 0;
				});
				for (Goal childTemplate : sortedChildTemplates)
				{
					if (childTemplate == null) continue;
					SeedEntry childEntry = new SeedEntry(seedGoalId, childQuestForNextLevel, childTemplate);
					if (childTemplate.isOptional())
					{
						optionalPriority.add(childEntry);
					}
					else if (childTemplate.getType() == GoalType.QUEST)
					{
						lowPriority.add(childEntry);
					}
					else
					{
						highPriority.add(childEntry);
					}
				}
			}
		}
	}

	/**
	 * Hybrid skill-goal seeder: reuses a pre-existing USER goal
	 * (non-{@code autoSeeded}) if one already satisfies the target,
	 * otherwise creates a new goal via the public
	 * {@link #addSkillGoal(net.runelite.api.Skill, int)} API. This
	 * means each distinct target level gets its own card (15 Agility
	 * and 32 Agility coexist), but a pre-existing 99 Agility goal
	 * that the user manually created is reused and tagged rather than
	 * spawning a redundant lower-level seed.
	 *
	 * @param skill    the skill
	 * @param targetXp target XP (use {@code Experience.getXpForLevel})
	 * @return goal id (existing or newly created), or null on error
	 */
	private String findOrCreateSkillGoalForSeed(
		net.runelite.api.Skill skill, int targetXp, java.util.Set<String> preExistingGoalIds)
	{
		// 1. Check for a pre-existing USER goal (one that existed before
		//    this gesture started) that satisfies the requirement. Goals
		//    created during the same gesture are NOT eligible — otherwise
		//    Lunar's "61 Crafting" would swallow Lost City's "31 Crafting",
		//    making Lost City display the wrong requirement.
		for (Goal g : api.goalStore.getGoals())
		{
			if (!preExistingGoalIds.contains(g.getId())) continue;
			if (g.getType() != GoalType.SKILL) continue;
			if (g.getSkillName() == null
				|| !skill.name().equalsIgnoreCase(g.getSkillName())) continue;
			if (g.getTargetValue() >= targetXp)
			{
				return g.getId();
			}
		}
		// 2. No match — create via the public API. addSkillGoal handles
		//    its own exact-target dedupe (same skill + same XP → returns
		//    existing id) and auto-chains same-skill goals in the topo
		//    sort. Its executeCommand calls join the active compound.
		return addSkillGoal(skill, targetXp);
	}

	String addDiaryGoal(String areaDisplayName, GoalPlannerApi.DiaryTier tier)
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
		for (Goal g : api.goalStore.getGoals())
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
		api.executeCommand(new com.goalplanner.command.Command()
		{
			@Override public boolean apply()
			{
				if (api.findGoal(goalId) != null) return false;
				api.goalStore.addGoal(goal);
				return true;
			}
			@Override public boolean revert() { api.goalStore.removeGoal(goalId); return true; }
			@Override public String getDescription() { return "Add diary: " + displayName + " " + tierStr; }
		});
		log.info("addDiaryGoal created: {} ({} {})", goalId, areaDisplayName, internalTier);
		return goalId;
	}

	private static AchievementDiaryData.Tier mapDiaryTier(GoalPlannerApi.DiaryTier tier)
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

	/**
	 * Add a diary goal with all unmet skill and quest requirements seeded
	 * as prerequisite goals. Parallel to addQuestGoalWithPrereqs.
	 */
	String addDiaryGoalWithPrereqs(String areaDisplayName, GoalPlannerApi.DiaryTier tier,
		com.goalplanner.data.DiaryRequirementResolver.Resolved resolved)
	{
		log.debug("API.public addDiaryGoalWithPrereqs(area={}, tier={}, templates={}, unlocks={})",
			areaDisplayName, tier,
			resolved == null ? 0 : resolved.templates.size(),
			resolved == null ? 0 : resolved.unlocks.size());
		if (areaDisplayName == null || tier == null) return null;
		if (resolved == null || resolved.isEmpty())
		{
			return addDiaryGoal(areaDisplayName, tier);
		}
		java.util.List<Goal> prereqTemplates = resolved.templates;

		AchievementDiaryData.Tier internalTier = mapDiaryTier(tier);
		String tierStr = internalTier.getDisplayName();
		String compoundDesc = "Add diary with requirements: " + areaDisplayName + " " + tierStr;

		api.beginCompound(compoundDesc);
		try
		{
			String diaryGoalId = addDiaryGoal(areaDisplayName, tier);
			if (diaryGoalId == null)
			{
				log.warn("addDiaryGoalWithPrereqs: addDiaryGoal returned null");
				return null;
			}

			Goal diaryGoal = api.findGoal(diaryGoalId);
			String sectionId = diaryGoal != null ? diaryGoal.getSectionId() : null;

			String tagLabel = areaDisplayName + " " + tierStr;
			if (tagLabel.length() > 30)
			{
				tagLabel = areaDisplayName;
			}

			java.util.List<String> gestureGoalIds = new java.util.ArrayList<>();
			gestureGoalIds.add(diaryGoalId);

			// Use the same BFS priority queue as quest prereq seeding.
			java.util.Set<Quest> visited = new java.util.HashSet<>();
			java.util.Set<String> preExisting = new java.util.HashSet<>();
			for (Goal g : api.goalStore.getGoals()) preExisting.add(g.getId());
			seedPrereqsInto(diaryGoalId, null, tagLabel,
				prereqTemplates, visited, preExisting, gestureGoalIds);

			// Goals with their own prereq trees (boss goals with skill reqs,
			// account goals with quest prereqs). Boss prereqs are now auto-
			// seeded by addBossGoal via BossKillData.getPrereqs().
			for (com.goalplanner.data.DiaryRequirementResolver.ResolvedBossReq entry : resolved.bossReqs)
			{
				Goal bt = entry.bossTemplate;
				String goalId;
				if (bt.getType() == GoalType.BOSS && bt.getBossName() != null)
				{
					// addBossGoal auto-seeds skill/unlock prereqs from BossKillData
					goalId = addBossGoal(bt.getBossName(), bt.getTargetValue());
				}
				else if (bt.getType() == GoalType.ACCOUNT && bt.getAccountMetric() != null)
				{
					goalId = addAccountGoal(bt.getAccountMetric(), bt.getTargetValue());
				}
				else
				{
					continue;
				}
				if (goalId == null) continue;
				gestureGoalIds.add(goalId);
				api.addRequirement(diaryGoalId, goalId);
				try
				{
					api.addTagWithCategory(goalId, tagLabel, TagCategory.QUEST.name());
				}
				catch (Exception e)
				{
					log.warn("addDiaryGoalWithPrereqs: failed to tag: {}", e.getMessage());
				}

				// Seed any quest prereqs on the goal (e.g. Throne of Miscellania
				// for MISC_APPROVAL account metric)
				// Use boss name or the created goal's display name for tagging
				// (not the raw template name which may be an enum like "TOG_MAX_TEARS")
				Goal createdGoal = api.findGoal(goalId);
				String entryTagLabel = bt.getBossName() != null
					? bt.getBossName()
					: (createdGoal != null ? createdGoal.getName() : bt.getName());
				if (entryTagLabel != null && entryTagLabel.length() > 30)
					entryTagLabel = entryTagLabel.substring(0, 30);
				for (Goal prereqTemplate : entry.skillTemplates)
				{
					if (prereqTemplate.getType() == GoalType.QUEST && prereqTemplate.getQuestName() != null)
					{
						try
						{
							Quest quest = Quest.valueOf(prereqTemplate.getQuestName());
							String questGoalId = addQuestGoal(quest);
							if (questGoalId == null) continue;
							gestureGoalIds.add(questGoalId);
							api.addRequirement(goalId, questGoalId);
							try
							{
								api.addTagWithCategory(questGoalId, entryTagLabel, TagCategory.QUEST.name());
							}
							catch (Exception ex)
							{
								log.warn("addDiaryGoalWithPrereqs: failed to tag quest prereq: {}", ex.getMessage());
							}
							// Recursively seed quest's own prereqs
							com.goalplanner.data.QuestRequirementResolver.Resolved qr =
								api.resolveQuestRequirements(quest);
							if (qr != null && !qr.isEmpty())
							{
								java.util.Set<Quest> qVisited = new java.util.HashSet<>();
								qVisited.add(quest);
								seedPrereqsInto(questGoalId, quest, qr.templates,
									qVisited, preExisting, gestureGoalIds);
							}
						}
						catch (IllegalArgumentException e)
						{
							log.warn("addDiaryGoalWithPrereqs: unknown quest {}", prereqTemplate.getQuestName());
						}
					}
				}
			}

			// Unlocks: create CUSTOM goal → link quest prereqs to it → link to diary.
			for (com.goalplanner.data.DiaryRequirementResolver.ResolvedUnlock unlock : resolved.unlocks)
			{
				String unlockGoalId = addCustomGoal(unlock.name, "Requirement");
				if (unlockGoalId == null) continue;
				// Set item icon on the unlock goal.
				if (unlock.itemId > 0)
				{
					Goal unlockGoal = api.findGoal(unlockGoalId);
					if (unlockGoal != null)
					{
						unlockGoal.setItemId(unlock.itemId);
						api.goalStore.save();
					}
				}
				gestureGoalIds.add(unlockGoalId);
				api.addRequirement(diaryGoalId, unlockGoalId);
				try
				{
					api.addTagWithCategory(unlockGoalId, tagLabel, TagCategory.QUEST.name());
				}
				catch (Exception e)
				{
					log.warn("addDiaryGoalWithPrereqs: failed to tag unlock: {}", e.getMessage());
				}

				// Link the unlock's quest prereqs to the unlock goal.
				for (Goal questTemplate : unlock.questTemplates)
				{
					if (questTemplate.getType() != GoalType.QUEST || questTemplate.getQuestName() == null)
						continue;
					try
					{
						Quest quest = Quest.valueOf(questTemplate.getQuestName());
						String questGoalId = addQuestGoal(quest);
						if (questGoalId == null) continue;
						gestureGoalIds.add(questGoalId);
						api.addRequirement(unlockGoalId, questGoalId);
						try
						{
							api.addTagWithCategory(questGoalId, tagLabel, TagCategory.QUEST.name());
						}
						catch (Exception e2)
						{
							log.warn("addDiaryGoalWithPrereqs: failed to tag quest: {}", e2.getMessage());
						}
						// Seed this quest's own requirements recursively.
						com.goalplanner.data.QuestRequirementResolver.Resolved qr =
							api.resolveQuestRequirements(quest);
						if (qr != null && !qr.isEmpty())
						{
							java.util.Set<Quest> vis = new java.util.HashSet<>();
							vis.add(quest);
							java.util.Set<String> pre = new java.util.HashSet<>();
							for (Goal g2 : api.goalStore.getGoals())
								pre.add(g2.getId());
							seedPrereqsInto(questGoalId, quest, qr.templates,
								vis, pre, gestureGoalIds);
						}
					}
					catch (IllegalArgumentException e)
					{
						log.warn("addDiaryGoalWithPrereqs: unknown quest {}", questTemplate.getQuestName());
					}
				}

				// Link the unlock's skill prereqs to the unlock goal.
				// These are optional (suggested, not strictly required).
				for (Goal skillTemplate : unlock.skillTemplates)
				{
					if (skillTemplate.getType() != GoalType.SKILL || skillTemplate.getSkillName() == null)
						continue;
					String skillGoalId = addSkillGoal(
						Skill.valueOf(skillTemplate.getSkillName()),
						skillTemplate.getTargetValue());
					if (skillGoalId == null) continue;
					Goal skillGoal = api.findGoal(skillGoalId);
					if (skillGoal != null) skillGoal.setOptional(true);
					gestureGoalIds.add(skillGoalId);
					api.addRequirement(unlockGoalId, skillGoalId);
					try
					{
						api.addTagWithCategory(skillGoalId, tagLabel, TagCategory.QUEST.name());
					}
					catch (Exception e)
					{
						log.warn("addDiaryGoalWithPrereqs: failed to tag unlock skill: {}", e.getMessage());
					}
				}

				// Link the unlock's account metric prereqs to the unlock goal.
				for (Goal accountTemplate : unlock.accountTemplates)
				{
					if (accountTemplate.getType() != GoalType.ACCOUNT || accountTemplate.getAccountMetric() == null)
						continue;
					String accountGoalId = addAccountGoal(
						accountTemplate.getAccountMetric(),
						accountTemplate.getTargetValue());
					if (accountGoalId == null) continue;
					gestureGoalIds.add(accountGoalId);
					api.addRequirement(unlockGoalId, accountGoalId);
					try
					{
						api.addTagWithCategory(accountGoalId, tagLabel, TagCategory.QUEST.name());
					}
					catch (Exception e)
					{
						log.warn("addDiaryGoalWithPrereqs: failed to tag unlock account: {}", e.getMessage());
					}
				}

				// Seed alternatives as OR-prereqs on the unlock goal.
				// Tag each with the unlock name (e.g. "Warriors Guild Entry").
				String unlockTagLabel = unlock.name;
				if (unlockTagLabel != null && unlockTagLabel.length() > 30)
				{
					unlockTagLabel = unlockTagLabel.substring(0, 30);
				}
				for (com.goalplanner.data.DiaryRequirementResolver.ResolvedAlternative alt : unlock.alternatives)
				{
					for (Goal skillTemplate : alt.skillTemplates)
					{
						if (skillTemplate.getType() != GoalType.SKILL || skillTemplate.getSkillName() == null)
							continue;
						String skillGoalId = addSkillGoal(
							Skill.valueOf(skillTemplate.getSkillName()),
							skillTemplate.getTargetValue());
						if (skillGoalId == null) continue;
						gestureGoalIds.add(skillGoalId);
						api.addOrRequirement(unlockGoalId, skillGoalId);
						try
						{
							api.addTagWithCategory(skillGoalId, unlockTagLabel, TagCategory.QUEST.name());
						}
						catch (Exception e)
						{
							log.warn("addDiaryGoalWithPrereqs: failed to tag alt skill: {}", e.getMessage());
						}
					}
					for (Goal accountTemplate : alt.accountTemplates)
					{
						if (accountTemplate.getType() != GoalType.ACCOUNT || accountTemplate.getAccountMetric() == null)
							continue;
						String accountGoalId = addAccountGoal(
							accountTemplate.getAccountMetric(),
							accountTemplate.getTargetValue());
						if (accountGoalId == null) continue;
						gestureGoalIds.add(accountGoalId);
						api.addOrRequirement(unlockGoalId, accountGoalId);
						try
						{
							api.addTagWithCategory(accountGoalId, unlockTagLabel, TagCategory.QUEST.name());
						}
						catch (Exception e)
						{
							log.warn("addDiaryGoalWithPrereqs: failed to tag alt account: {}", e.getMessage());
						}
					}
					for (Goal bossTemplate : alt.bossTemplates)
					{
						if (bossTemplate.getType() != GoalType.BOSS || bossTemplate.getBossName() == null)
							continue;
						String bossGoalId = addBossGoal(
							bossTemplate.getBossName(),
							bossTemplate.getTargetValue());
						if (bossGoalId == null) continue;
						gestureGoalIds.add(bossGoalId);
						api.addOrRequirement(unlockGoalId, bossGoalId);
						try
						{
							api.addTagWithCategory(bossGoalId, unlockTagLabel, TagCategory.QUEST.name());
						}
						catch (Exception e)
						{
							log.warn("addDiaryGoalWithPrereqs: failed to tag alt boss: {}", e.getMessage());
						}
					}
				}
			}

			// Promote leaf quests (zero active requirements) to the top.
			api.reorderingService.promoteLeafGoalsToTop();

			// Select all goals created in this gesture.
			api.replaceGoalSelection(gestureGoalIds);
		}
		finally
		{
			api.endCompound();
		}
		return areaDisplayName;
	}

	String addCombatAchievementGoal(int caTaskId)
	{
		log.debug("API.public addCombatAchievementGoal(caTaskId={})", caTaskId);
		if (caTaskId < 0 || caTaskId > 639)
		{
			log.warn("addCombatAchievementGoal: caTaskId {} out of range [0, 639]", caTaskId);
			return null;
		}

		WikiCaRepository.CaInfo info = api.wikiCaRepository.getById(caTaskId);
		if (info == null)
		{
			log.warn("addCombatAchievementGoal: no wiki entry for task id {}", caTaskId);
			return null;
		}

		// Duplicate guard: same caTaskId or same name
		for (Goal g : api.goalStore.getGoals())
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
			Tag bossTag = api.goalStore.findOrCreateSystemTag(tagLabel,
				isRaid ? TagCategory.RAID : TagCategory.BOSS);
			if (bossTag != null) tagIds.add(bossTag.getId());
			// Inherit Slayer skill tag if the monster is a known slayer task target
			if (com.goalplanner.data.SourceAttributes.isSlayerTask(info.monster))
			{
				Tag slayerTag = api.goalStore.findOrCreateSystemTag("Slayer", TagCategory.SKILLING);
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
		api.executeCommand(new com.goalplanner.command.Command()
		{
			@Override public boolean apply()
			{
				if (api.findGoal(goalId) != null) return false;
				api.goalStore.addGoal(goal);
				return true;
			}
			@Override public boolean revert() { api.goalStore.removeGoal(goalId); return true; }
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

	String addAccountGoal(String metricName, int target)
	{
		log.debug("API.public addAccountGoal(metric={}, target={})", metricName, target);
		if (metricName == null || target <= 0) return null;
		AccountMetric metric;
		try
		{
			metric = AccountMetric.valueOf(metricName);
		}
		catch (IllegalArgumentException e)
		{
			log.warn("addAccountGoal: unknown metric {}", metricName);
			return null;
		}
		// Clamp to valid range
		int clampedTarget = Math.max(metric.getMinTarget(),
			Math.min(metric.getMaxTarget(), target));
		api.clearGoalSelection();

		// Duplicate guard: same metric + same target
		for (Goal g : api.goalStore.getGoals())
		{
			if (g.getType() == GoalType.ACCOUNT
				&& metricName.equals(g.getAccountMetric())
				&& g.getTargetValue() == clampedTarget)
			{
				log.info("addAccountGoal: duplicate of existing goal {} ({} {})",
					g.getId(), metricName, clampedTarget);
				return g.getId();
			}
		}

		String goalName;
		if (metric == AccountMetric.CA_POINTS)
		{
			goalName = clampedTarget + " CA (" + AccountMetric.caTierLabel(clampedTarget) + ")";
		}
		else if (metric == AccountMetric.MISC_APPROVAL)
		{
			// 127 = 100% approval. Display as percentage.
			int pct = Math.round(clampedTarget * 100f / 127f);
			goalName = pct + "% " + metric.getDisplayName();
		}
		else
		{
			goalName = clampedTarget + " " + metric.getDisplayName();
		}

		Goal goal = Goal.builder()
			.type(GoalType.ACCOUNT)
			.name(goalName)
			.description(metric.getDisplayName())
			.accountMetric(metricName)
			.targetValue(clampedTarget)
			.currentValue(0)
			.spriteId(metric.resolveSpriteForTarget(clampedTarget))
			.customColorRgb(metric.getColorRgb())
			.build();

		final String goalId = goal.getId();
		final String displayName = goalName;
		api.beginCompound("Add goal: " + displayName);
		try
		{
			api.executeCommand(new com.goalplanner.command.Command()
			{
				@Override public boolean apply()
				{
					if (api.findGoal(goalId) != null) return false;
					api.goalStore.addGoal(goal);
					return true;
				}
				@Override public boolean revert() { api.goalStore.removeGoal(goalId); return true; }
				@Override public String getDescription() { return "Add account goal: " + displayName; }
			});
			autoLinkSkillOrItemChain(goal);
		}
		finally
		{
			api.endCompound();
		}
		log.info("addAccountGoal created: {} ({})", goalId, goalName);
		return goalId;
	}

	String addBossGoal(String bossName, int targetKills)
	{
		log.debug("API.public addBossGoal(boss={}, target={})", bossName, targetKills);
		if (bossName == null || !com.goalplanner.data.BossKillData.isKnownBoss(bossName))
		{
			log.warn("addBossGoal: unknown boss {}", bossName);
			return null;
		}
		if (targetKills < 1) return null;

		// Duplicate guard: same boss name
		for (Goal g : api.goalStore.getGoals())
		{
			if (g.getType() == GoalType.BOSS && bossName.equals(g.getBossName()))
			{
				log.info("addBossGoal: duplicate of existing goal {} ({})", g.getId(), bossName);
				return g.getId();
			}
		}

		int petItemId = com.goalplanner.data.BossKillData.getPetItemId(bossName);
		Goal goal = Goal.builder()
			.type(GoalType.BOSS)
			.name(bossName)
			.description(targetKills + " kills")
			.bossName(bossName)
			.targetValue(targetKills)
			.currentValue(0)
			.itemId(petItemId)
			.build();

		final String goalId = goal.getId();
		final String displayName = bossName;
		api.beginCompound("Add boss goal: " + displayName);
		try
		{
			api.executeCommand(new com.goalplanner.command.Command()
			{
				@Override public boolean apply()
				{
					if (api.findGoal(goalId) != null) return false;
					api.goalStore.addGoal(goal);
					return true;
				}
				@Override public boolean revert()
				{
					api.goalStore.removeGoal(goalId);
					return true;
				}
				@Override public String getDescription() { return "Add boss goal: " + displayName; }
			});
			// Auto-tag with BOSS category.
			api.addTagWithCategory(goalId, bossName, TagCategory.BOSS.name());

			// Auto-seed boss prereqs (e.g. 70 Ranged + Mith Grapple for Kree'arra).
			com.goalplanner.data.BossKillData.BossPrereqs prereqs =
				com.goalplanner.data.BossKillData.getPrereqs(bossName);
			if (prereqs != null)
			{
				// Gesture-level accumulators for the priority-queue BFS.
				// Mirrors addDiaryGoalWithPrereqs / addQuestGoalWithPrereqs so
				// boss prereqs render with the same ordering (optional →
				// skills/item/account → quests) and quests chain their own
				// prereqs transitively.
				java.util.Set<net.runelite.api.Quest> bossVisited = new java.util.HashSet<>();
				java.util.Set<String> bossPreExistingGoalIds = new java.util.HashSet<>();
				for (Goal g : api.goalStore.getGoals()) bossPreExistingGoalIds.add(g.getId());
				java.util.List<String> bossGestureGoalIds = new java.util.ArrayList<>();
				bossGestureGoalIds.add(goalId);

				// Build templates for every direct prereq type EXCEPT
				// UnlockRef (which has a unique CUSTOM + optional-skill
				// subtree shape and stays in its own loop below).
				java.util.List<Goal> templates = new java.util.ArrayList<>();

				for (com.goalplanner.data.BossKillData.SkillReq sr : prereqs.skills)
				{
					templates.add(Goal.builder()
						.type(GoalType.SKILL)
						.name(sr.skill.getName() + " - Level " + sr.level)
						.skillName(sr.skill.name())
						.targetValue(net.runelite.api.Experience.getXpForLevel(sr.level))
						.build());
				}
				for (net.runelite.api.Quest q : prereqs.quests)
				{
					templates.add(Goal.builder()
						.type(GoalType.QUEST)
						.name(q.getName())
						.description("Quest")
						.questName(q.name())
						.targetValue(1)
						.spriteId(GoalPlannerApiImpl.QUEST_SPRITE_ID)
						.build());
				}
				for (com.goalplanner.data.BossKillData.ItemReq ir : prereqs.itemReqs)
				{
					templates.add(Goal.builder()
						.type(GoalType.ITEM_GRIND)
						.name(ir.displayName)
						.itemId(ir.itemId)
						.targetValue(ir.quantity)
						.build());
				}
				for (com.goalplanner.data.BossKillData.AccountReq ar : prereqs.accountReqs)
				{
					templates.add(Goal.builder()
						.type(GoalType.ACCOUNT)
						.name(ar.metricName)
						.accountMetric(ar.metricName)
						.targetValue(ar.target)
						.build());
				}
				for (com.goalplanner.data.BossKillData.BossReq br : prereqs.bossKills)
				{
					templates.add(Goal.builder()
						.type(GoalType.BOSS)
						.name(br.bossName)
						.description(br.killCount + " kills")
						.bossName(br.bossName)
						.targetValue(br.killCount)
						.itemId(com.goalplanner.data.BossKillData.getPetItemId(br.bossName))
						.build());
				}

				// Priority-queue BFS: orders optional → skill/item/account →
				// quest, and transitively chains each quest's own prereqs.
				seedPrereqsInto(goalId, null, bossName, templates,
					bossVisited, bossPreExistingGoalIds, bossGestureGoalIds);

				// Unlock prereqs (e.g. Mith Grapple) — CUSTOM goals with an
				// optional skill subtree; unique shape, not templated.
				for (com.goalplanner.data.BossKillData.UnlockRef unlock : prereqs.unlocks)
				{
					String unlockId = addCustomGoal(unlock.name, "Requirement");
					if (unlockId == null) continue;
					Goal unlockGoal = api.findGoal(unlockId);
					if (unlockGoal != null && unlock.itemId > 0)
					{
						unlockGoal.setItemId(unlock.itemId);
					}
					api.addRequirement(goalId, unlockId);
					try
					{
						api.addTagWithCategory(unlockId, bossName, TagCategory.QUEST.name());
					}
					catch (Exception e)
					{
						log.warn("addBossGoal: failed to tag unlock prereq: {}", e.getMessage());
					}
					// Unlock's optional skill prereqs
					for (com.goalplanner.data.BossKillData.SkillReq usr : unlock.optionalSkills)
					{
						String usId = addSkillGoal(usr.skill,
							net.runelite.api.Experience.getXpForLevel(usr.level));
						if (usId == null) continue;
						Goal sg = api.findGoal(usId);
						if (sg != null) sg.setOptional(true);
						api.addRequirement(unlockId, usId);
						try
						{
							api.addTagWithCategory(usId, unlock.name, TagCategory.QUEST.name());
						}
						catch (Exception e)
						{
							log.warn("addBossGoal: failed to tag unlock skill: {}", e.getMessage());
						}
					}
				}
				// Seed alternatives (OR-groups) as OR-prereqs on the boss
				// goal. Each Alternative contributes skill / quest /
				// account / boss-kill goals, all linked via
				// addOrRequirement so the boss unlocks when ANY alt is
				// satisfied (in addition to AND-prereqs above). Mirrors
				// the diary-unlock alternative path.
				for (com.goalplanner.data.BossKillData.Alternative alt : prereqs.alternatives)
				{
					String altLabel = alt.label;
					if (altLabel != null && altLabel.length() > 30)
					{
						altLabel = altLabel.substring(0, 30);
					}
					// Skill alternatives
					for (com.goalplanner.data.BossKillData.SkillReq sr : alt.skills)
					{
						String skillGoalId = addSkillGoal(sr.skill,
							net.runelite.api.Experience.getXpForLevel(sr.level));
						if (skillGoalId == null) continue;
						bossGestureGoalIds.add(skillGoalId);
						api.addOrRequirement(goalId, skillGoalId);
						try
						{
							api.addTagWithCategory(skillGoalId,
								altLabel != null ? altLabel : bossName,
								TagCategory.QUEST.name());
						}
						catch (Exception e)
						{
							log.warn("addBossGoal: failed to tag alt skill: {}", e.getMessage());
						}
					}
					// Account metric alternatives
					for (com.goalplanner.data.BossKillData.AccountReq ar : alt.accounts)
					{
						String accountGoalId = addAccountGoal(ar.metricName, ar.target);
						if (accountGoalId == null) continue;
						bossGestureGoalIds.add(accountGoalId);
						api.addOrRequirement(goalId, accountGoalId);
						try
						{
							api.addTagWithCategory(accountGoalId,
								altLabel != null ? altLabel : bossName,
								TagCategory.QUEST.name());
						}
						catch (Exception e)
						{
							log.warn("addBossGoal: failed to tag alt account: {}", e.getMessage());
						}
					}
					// Boss-kill alternatives
					for (com.goalplanner.data.BossKillData.BossReq br : alt.bosses)
					{
						String altBossId = addBossGoal(br.bossName, br.killCount);
						if (altBossId == null) continue;
						bossGestureGoalIds.add(altBossId);
						api.addOrRequirement(goalId, altBossId);
						try
						{
							api.addTagWithCategory(altBossId,
								altLabel != null ? altLabel : bossName,
								TagCategory.BOSS.name());
						}
						catch (Exception e)
						{
							log.warn("addBossGoal: failed to tag alt boss: {}", e.getMessage());
						}
					}
					// Quest alternatives — chain transitively like AND-quests
					for (net.runelite.api.Quest q : alt.quests)
					{
						if (!bossVisited.add(q)) continue;
						String questGoalId = addQuestGoal(q);
						if (questGoalId == null) continue;
						bossGestureGoalIds.add(questGoalId);
						api.addOrRequirement(goalId, questGoalId);
						try
						{
							api.addTagWithCategory(questGoalId,
								altLabel != null ? altLabel : bossName,
								TagCategory.QUEST.name());
						}
						catch (Exception e)
						{
							log.warn("addBossGoal: failed to tag alt quest: {}", e.getMessage());
						}
						// Recursively seed this quest's own requirements
						// via the AND-edge path (prereq chain inside an
						// OR-branch is still AND-gated — you need the
						// quest's own skills to do the quest).
						com.goalplanner.data.QuestRequirementResolver.Resolved qr =
							api.resolveQuestRequirements(q);
						if (qr != null && !qr.isEmpty())
						{
							seedPrereqsInto(questGoalId, q, qr.templates,
								bossVisited, bossPreExistingGoalIds, bossGestureGoalIds);
						}
					}
				}
			}
		}
		finally
		{
			api.endCompound();
		}
		log.info("addBossGoal created: {} ({} x {})", goalId, bossName, targetKills);
		return goalId;
	}

	String addCustomGoal(String name, String description)
	{
		log.debug("API.public addCustomGoal(name={}, description={})", name, description);
		if (name == null || name.trim().isEmpty()) return null;
		String trimmedName = name.trim();
		// Duplicate guard: same name + same type = no new goal
		Goal existing = null;
		for (Goal g : api.goalStore.getGoals())
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
		// Undoable. Wrap in a Command that adds/removes the same
		// Goal entity (preserving id) so redo restores the exact same goal
		// and any later commands referencing it still resolve.
		final String goalId = goal.getId();
		api.executeCommand(new com.goalplanner.command.Command()
		{
			@Override public boolean apply()
			{
				if (api.findGoal(goalId) != null) return false; // already there
				api.goalStore.addGoal(goal);
				return true;
			}
			@Override public boolean revert()
			{
				api.goalStore.removeGoal(goalId);
				api.selectedGoalIds.remove(goalId);
				return true;
			}
			@Override public String getDescription() { return "Add goal: " + trimmedName; }
		});
		log.info("addCustomGoal created: {} ({})", goalId, trimmedName);
		return goalId;
	}
}
