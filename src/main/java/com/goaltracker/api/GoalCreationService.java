package com.goaltracker.api;

import com.goaltracker.data.AchievementDiaryData;
import com.goaltracker.data.CombatAchievementData;
import com.goaltracker.data.ItemSourceData;
import com.goaltracker.data.WikiCaRepository;
import com.goaltracker.model.AccountMetric;
import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalType;
import com.goaltracker.model.ItemTag;
import com.goaltracker.model.Tag;
import com.goaltracker.model.TagCategory;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Experience;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

/**
 * Encapsulates all goal-creation methods extracted from {@link GoalTrackerApiImpl}.
 * Package-private — only {@link GoalTrackerApiImpl} instantiates and delegates to this.
 */
@Slf4j
class GoalCreationService
{
	/** Max level supported (1-99 normal, 100-126 virtual). */
	private static final int MAX_LEVEL = 126;

	/** Max experience per skill in OSRS. */
	private static final int MAX_XP = 200_000_000;

	private final GoalTrackerApiImpl api;

	GoalCreationService(GoalTrackerApiImpl api)
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
			api.executeCommand(new com.goaltracker.command.Command()
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

		api.goalStore.addGoal(goal);
		api.onGoalsChanged.run();
		log.info("addItemGoal created: {} ({} x {})", goal.getId(), targetQuantity, itemName);
		return goal.getId();
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

		int qpReward = com.goaltracker.data.QuestRequirements.questPointReward(quest);
		String description = qpReward > 0
			? qpReward + " Quest Point" + (qpReward != 1 ? "s" : "")
			: "Quest";

		Goal goal = Goal.builder()
			.type(GoalType.QUEST)
			.name(quest.getName())
			.description(description)
			.questName(questName)
			.targetValue(1)
			.currentValue(0)
			.spriteId(GoalTrackerApiImpl.QUEST_SPRITE_ID)
			.build();

		final String goalId = goal.getId();
		final String displayName = goal.getName();
		api.beginCompound("Add quest: " + displayName);
		try
		{
			api.executeCommand(new com.goaltracker.command.Command()
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
			if (com.goaltracker.data.QuestRequirements.isF2P(quest))
			{
				api.addTagWithCategory(goalId, "F2P", TagCategory.OTHER.name());
				Tag f2pTag = api.goalStore.findTagByLabel("F2P", TagCategory.OTHER);
				if (f2pTag != null && f2pTag.getColorRgb() < 0)
				{
					api.goalStore.recolorTag(f2pTag.getId(), GoalTrackerApiImpl.F2P_TAG_COLOR_RGB);
				}
			}
			// Auto-tag XP reward skills as SKILLING icons.
			for (net.runelite.api.Skill rewardSkill : com.goaltracker.data.QuestRequirements.xpRewards(quest))
			{
				api.addTagWithCategory(goalId, rewardSkill.getName(), TagCategory.SKILLING.name());
			}
			// Auto-tag lamp-reward quests with a lamp icon.
			if (com.goaltracker.data.QuestRequirements.rewardsLamp(quest))
			{
				api.addTagWithCategory(goalId, "Lamp", TagCategory.OTHER.name());
				Tag lampTag = api.goalStore.findTagByLabel("Lamp", TagCategory.OTHER);
				if (lampTag != null && lampTag.getIconKey() == null)
				{
					api.goalStore.setTagIcon(lampTag.getId(), GoalTrackerApiImpl.LAMP_ICON_KEY);
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
						com.goaltracker.data.QuestRequirements.Reqs reqs =
							com.goaltracker.data.QuestRequirements.lookup(q);
						if (reqs != null && !com.goaltracker.data.QuestRequirements.hasRequirements(q))
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
		final Quest parentQuest;
		final Goal template;
		SeedEntry(String parentGoalId, Quest parentQuest, Goal template)
		{
			this.parentGoalId = parentGoalId;
			this.parentQuest = parentQuest;
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
	private void seedPrereqsInto(
		String rootGoalId,
		Quest rootQuest,
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

		// Seed initial templates into the appropriate queue
		for (Goal t : rootTemplates)
		{
			if (t == null) continue;
			SeedEntry entry = new SeedEntry(rootGoalId, rootQuest, t);
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
			final String parentTagLabel = entry.parentQuest.getName();
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
			else
			{
				GoalTrackerInternalApi.FindOrCreateResult result = api.findOrCreateRequirement(template, sectionId);
				if (result == null)
				{
					log.warn("seedPrereqsInto: findOrCreateRequirement returned null for template type={}",
						template.getType());
					continue;
				}
				seedGoalId = result.goalId;
			}

			// Propagate optional flag from template to created goal.
			if (template.isOptional())
			{
				api.setGoalOptional(seedGoalId, true);
			}

			gestureGoalIds.add(seedGoalId);
			api.addRequirement(entry.parentGoalId, seedGoalId);
			api.addTagWithCategory(seedGoalId, parentTagLabel, TagCategory.QUEST.name());

			// Discover child quest's prereqs and route to appropriate queue.
			if (childQuestForNextLevel != null)
			{
				if (!visited.add(childQuestForNextLevel))
				{
					continue;
				}
				com.goaltracker.data.QuestRequirementResolver.Resolved childResolved =
					api.resolveQuestRequirements(childQuestForNextLevel);
				// QP and combat level requirements are now seeded as ACCOUNT
				// goal templates by the resolver — no stub logging needed.
				for (Goal childTemplate : childResolved.templates)
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

	String addDiaryGoal(String areaDisplayName, GoalTrackerApi.DiaryTier tier)
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
		api.executeCommand(new com.goaltracker.command.Command()
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

	private static AchievementDiaryData.Tier mapDiaryTier(GoalTrackerApi.DiaryTier tier)
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
			if (com.goaltracker.data.SourceAttributes.isSlayerTask(info.monster))
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
		api.executeCommand(new com.goaltracker.command.Command()
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
			api.executeCommand(new com.goaltracker.command.Command()
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
		api.executeCommand(new com.goaltracker.command.Command()
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
