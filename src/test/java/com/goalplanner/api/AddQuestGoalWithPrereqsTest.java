package com.goalplanner.api;

import com.goalplanner.data.QuestRequirementResolver;
import com.goalplanner.data.WikiCaRepository;
import com.goalplanner.model.Goal;
import com.goalplanner.model.GoalType;
import com.goalplanner.model.Tag;
import com.goalplanner.model.TagCategory;
import com.goalplanner.persistence.GoalStore;
import com.goalplanner.service.GoalReorderingService;
import com.goalplanner.testsupport.InMemoryConfigManager;
import java.util.Arrays;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.Experience;
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link GoalPlannerApiImpl#addQuestGoalWithPrereqs}, the
 * compound-command entry point used by the "Add Goal with
 * Requirements" menu.
 *
 * <p>Covers the full vertical slice:
 * <ul>
 *   <li>Fresh add: quest goal + seeded skill prereqs + seeded quest
 *       prereqs are all created and linked as one compound.</li>
 *   <li>Existing-goal reuse: a pre-existing goal that satisfies a
 *       template (higher-target skill goal, same-name quest goal) is
 *       linked as the prerequisite rather than duplicated.</li>
 *   <li>Empty templates: equivalent to the plain {@code addQuestGoal}.</li>
 *   <li>Undo/redo: one {@code api.undo()} reverses the whole gesture
 *       (quest goal + every seed + every edge), and {@code api.redo()}
 *       replays it.</li>
 * </ul>
 */
class AddQuestGoalWithPrereqsTest
{
	private GoalStore store;
	private GoalPlannerApiImpl api;

	@BeforeEach
	void setUp()
	{
		ConfigManager configManager = InMemoryConfigManager.create();
		store = new GoalStore(configManager);
		store.load();

		ItemManager itemManager = mock(ItemManager.class);
		WikiCaRepository wikiCaRepository = mock(WikiCaRepository.class);
		GoalReorderingService reorderingService = new GoalReorderingService(store);
		api = new GoalPlannerApiImpl(store, reorderingService, itemManager, wikiCaRepository);
		api.setOnGoalsChanged(() -> {});
	}

	// ---- helpers -----------------------------------------------------------

	/** Build a SKILL goal template at the given level. */
	private static Goal skillTemplate(Skill skill, int level)
	{
		return Goal.builder()
			.type(GoalType.SKILL)
			.name(skill.getName() + " \u2192 Level " + level)
			.skillName(skill.name())
			.targetValue(Experience.getXpForLevel(level))
			.build();
	}

	/** Build a QUEST goal template for the given quest. */
	private static Goal questTemplate(Quest quest)
	{
		return Goal.builder()
			.type(GoalType.QUEST)
			.name(quest.getName())
			.description("Quest")
			.questName(quest.name())
			.targetValue(1)
			.build();
	}

	private Goal findById(String id)
	{
		return store.getGoals().stream()
			.filter(g -> g.getId().equals(id))
			.findFirst()
			.orElse(null);
	}

	private Goal findByType(GoalType type, String identity)
	{
		for (Goal g : store.getGoals())
		{
			if (g.getType() != type) continue;
			if (type == GoalType.SKILL && identity.equals(g.getSkillName())) return g;
			if (type == GoalType.QUEST && identity.equals(g.getQuestName())) return g;
		}
		return null;
	}

	// ---- fresh add ---------------------------------------------------------

	@Test
	@DisplayName("creates quest goal + seeds skill prereqs + wires edges")
	void freshAddSeedsSkillPrereqs()
	{
		List<Goal> templates = Arrays.asList(
			skillTemplate(Skill.AGILITY, 35));
		String questId = api.addQuestGoalWithPrereqs(Quest.HORROR_FROM_THE_DEEP, templates);

		assertNotNull(questId);
		// One quest goal + one seeded skill goal.
		assertEquals(2, store.getGoals().size());

		Goal quest = findByType(GoalType.QUEST, "HORROR_FROM_THE_DEEP");
		assertNotNull(quest);
		assertEquals(questId, quest.getId());

		Goal skill = findByType(GoalType.SKILL, "AGILITY");
		assertNotNull(skill);
		// Skill seeds now go through addSkillGoal (public API), which
		// does NOT set autoSeeded. That's OK — autoSeeded is only needed
		// for the future same-skill absorption rule, and skill goals
		// seeded via quest-requirements benefit more from being first-
		// class goals (chain-linked, proper name/sprite).
		assertEquals(Experience.getXpForLevel(35), skill.getTargetValue());

		// Edge wired from quest → skill.
		assertTrue(quest.getRequiredGoalIds().contains(skill.getId()),
			"quest should require the seeded skill goal");
	}

	@Test
	@DisplayName("seeds multiple skill + quest prereqs in one gesture")
	void freshAddMultipleTemplates()
	{
		List<Goal> templates = Arrays.asList(
			skillTemplate(Skill.MAGIC, 75),
			skillTemplate(Skill.SMITHING, 70),
			questTemplate(Quest.LEGENDS_QUEST));
		String questId = api.addQuestGoalWithPrereqs(Quest.DRAGON_SLAYER_II, templates);

		assertNotNull(questId);
		// 1 DS2 + 2 skill seeds + 1 quest seed = 4.
		assertEquals(4, store.getGoals().size());

		Goal quest = findByType(GoalType.QUEST, "DRAGON_SLAYER_II");
		assertEquals(3, quest.getRequiredGoalIds().size());
	}

	// ---- existing-goal reuse -----------------------------------------------

	@Test
	@DisplayName("existing skill goal with higher target is reused, not duplicated")
	void reusesExistingSkillGoalWithHigherTarget()
	{
		// Player already has a level-99 Agility goal.
		String existingAgilityId = api.addSkillGoalForLevel(Skill.AGILITY, 99);
		assertNotNull(existingAgilityId);
		assertEquals(1, store.getGoals().size());

		// Adding HFTD (Agility 35 req) should LINK to the existing 99 goal.
		List<Goal> templates = Arrays.asList(skillTemplate(Skill.AGILITY, 35));
		String questId = api.addQuestGoalWithPrereqs(Quest.HORROR_FROM_THE_DEEP, templates);

		// Still only 2 goals: existing 99 Agility + the new quest. No seed.
		assertEquals(2, store.getGoals().size());

		Goal quest = findByType(GoalType.QUEST, "HORROR_FROM_THE_DEEP");
		assertTrue(quest.getRequiredGoalIds().contains(existingAgilityId),
			"quest should require the pre-existing higher-target agility goal");

		// The existing goal wasn't mutated into autoSeeded.
		Goal agility = findById(existingAgilityId);
		assertFalse(agility.isAutoSeeded(),
			"pre-existing user goal must not be flipped to autoSeeded by find-or-create");
	}

	@Test
	@DisplayName("existing quest goal is reused by name equality")
	void reusesExistingQuestGoalByName()
	{
		// Player already has Legends' Quest on their list.
		String existingLegendsId = api.addQuestGoal(Quest.LEGENDS_QUEST);
		assertNotNull(existingLegendsId);

		List<Goal> templates = Arrays.asList(questTemplate(Quest.LEGENDS_QUEST));
		String questId = api.addQuestGoalWithPrereqs(Quest.DRAGON_SLAYER_II, templates);

		// Only 2 goals: the existing Legends' Quest + DS2. No duplicate seed.
		assertEquals(2, store.getGoals().size());

		Goal ds2 = findById(questId);
		assertTrue(ds2.getRequiredGoalIds().contains(existingLegendsId));
	}

	// ---- empty-templates degenerate case -----------------------------------

	@Test
	@DisplayName("empty templates list behaves like plain addQuestGoal")
	void emptyTemplatesIsPlainAdd()
	{
		String id = api.addQuestGoalWithPrereqs(Quest.COOKS_ASSISTANT, List.of());
		assertNotNull(id);
		assertEquals(1, store.getGoals().size());
		Goal quest = findByType(GoalType.QUEST, "COOKS_ASSISTANT");
		assertEquals(id, quest.getId());
		assertTrue(quest.getRequiredGoalIds().isEmpty());
	}

	// ---- compound undo -----------------------------------------------------

	@Test
	@DisplayName("one undo reverses the entire gesture (quest + seeds + edges)")
	void singleUndoReversesEntireGesture()
	{
		List<Goal> templates = Arrays.asList(
			skillTemplate(Skill.MAGIC, 75),
			skillTemplate(Skill.SMITHING, 70),
			questTemplate(Quest.LEGENDS_QUEST));
		String questId = api.addQuestGoalWithPrereqs(Quest.DRAGON_SLAYER_II, templates);
		assertEquals(4, store.getGoals().size());

		boolean undone = api.undo();
		assertTrue(undone, "undo should succeed");
		assertEquals(0, store.getGoals().size(),
			"undo should remove the quest goal AND all three seeded prereqs");

		// Redo replays the whole gesture.
		boolean redone = api.redo();
		assertTrue(redone, "redo should succeed");
		assertEquals(4, store.getGoals().size());
		Goal replayed = findByType(GoalType.QUEST, "DRAGON_SLAYER_II");
		assertNotNull(replayed);
		assertEquals(3, replayed.getRequiredGoalIds().size());
	}

	// ---- API: resolveQuestRequirements -------------------------------------

	@Test
	@DisplayName("api.resolveQuestRequirements routes to the resolver with the injected client")
	void resolveQuestRequirementsRoutesToResolver()
	{
		// Build a second api instance with a mocked Client — the shared
		// `api` field uses the null-client test constructor.
		Client client = mock(Client.class);
		when(client.getRealSkillLevel(Skill.AGILITY)).thenReturn(30);
		GoalPlannerApiImpl apiWithClient = new GoalPlannerApiImpl(
			store,
			new GoalReorderingService(store),
			mock(ItemManager.class),
			mock(WikiCaRepository.class),
			client);

		QuestRequirementResolver.Resolved out =
			apiWithClient.resolveQuestRequirements(Quest.HORROR_FROM_THE_DEEP);
		assertNotNull(out);
		// 1 unmet skill req (Agility 30 < 35) + 1 unmet quest prereq
		// (Alfred Grimhand's Barcrawl) + 1 optional combat recommendation = 3 templates.
		assertEquals(3, out.templates.size(),
			"HFTD should produce skill + Barcrawl + optional combat templates");
	}

	@Test
	@DisplayName("api.resolveQuestRequirements returns empty for null quest")
	void resolveQuestRequirementsNullQuest()
	{
		QuestRequirementResolver.Resolved out = api.resolveQuestRequirements(null);
		assertNotNull(out);
		assertTrue(out.isEmpty());
	}

	// ---- tagging -----------------------------------------------------------

	/** Find the QUEST-category tag with the given label, or null. */
	private Tag findQuestTag(String label)
	{
		return store.getTags().stream()
			.filter(t -> t.getCategory() == TagCategory.QUEST)
			.filter(t -> label.equalsIgnoreCase(t.getLabel()))
			.findFirst()
			.orElse(null);
	}

	/** True iff the goal with {@code goalId} carries the given tag id. */
	private boolean goalHasTag(String goalId, String tagId)
	{
		Goal g = findById(goalId);
		return g != null && g.getTagIds() != null && g.getTagIds().contains(tagId);
	}

	@Test
	@DisplayName("every prereq gets the quest-name tag; the quest goal itself does NOT")
	void questTagAppliedToPrereqsOnly()
	{
		List<Goal> templates = Arrays.asList(
			skillTemplate(Skill.MAGIC, 75),
			skillTemplate(Skill.SMITHING, 70),
			questTemplate(Quest.LEGENDS_QUEST));
		String questId = api.addQuestGoalWithPrereqs(Quest.DRAGON_SLAYER_II, templates);

		Tag tag = findQuestTag(Quest.DRAGON_SLAYER_II.getName());
		assertNotNull(tag, "QUEST-category tag named after the quest must exist");

		// The quest goal itself is NOT tagged — it already identifies
		// itself by name, the tag would just be noise.
		assertFalse(goalHasTag(questId, tag.getId()),
			"quest goal must NOT carry its own quest-name tag");

		// Every seeded prereq is tagged.
		for (Goal g : store.getGoals())
		{
			if (g.getId().equals(questId)) continue;
			assertTrue(g.getTagIds().contains(tag.getId()),
				"seeded prereq " + g.getName() + " must carry the quest tag");
		}
	}

	@Test
	@DisplayName("pre-existing reused goals also receive the quest tag")
	void questTagAppliedToReusedGoals()
	{
		// Pre-existing Agility 99 goal — will be reused for HFTD's 35 req.
		String existingAgilityId = api.addSkillGoalForLevel(Skill.AGILITY, 99);

		api.addQuestGoalWithPrereqs(Quest.HORROR_FROM_THE_DEEP,
			Arrays.asList(skillTemplate(Skill.AGILITY, 35)));

		Tag tag = findQuestTag(Quest.HORROR_FROM_THE_DEEP.getName());
		assertNotNull(tag);
		assertTrue(goalHasTag(existingAgilityId, tag.getId()),
			"pre-existing reused goal must carry the new quest tag");
	}

	@Test
	@DisplayName("undo reverses tag attachments along with everything else")
	void undoReversesTagging()
	{
		List<Goal> templates = Arrays.asList(skillTemplate(Skill.AGILITY, 35));
		String questId = api.addQuestGoalWithPrereqs(Quest.HORROR_FROM_THE_DEEP, templates);

		Tag tag = findQuestTag(Quest.HORROR_FROM_THE_DEEP.getName());
		assertNotNull(tag);

		api.undo();

		// Quest goal is gone; skill seed is gone; no goal should carry the tag.
		for (Goal g : store.getGoals())
		{
			assertFalse(g.getTagIds().contains(tag.getId()),
				"after undo, no goal should carry the quest tag");
		}
		// The Tag entity itself may still exist (createUserTag is not
		// command-wrapped) — we don't assert on it. Orphan tags are
		// tolerated by the existing infra.
	}

	@Test
	@DisplayName("undo preserves pre-existing goals when reuse matched them")
	void undoPreservesPreExistingMatches()
	{
		// Pre-existing 99 Agility — a USER goal, not autoSeeded.
		String existingAgilityId = api.addSkillGoalForLevel(Skill.AGILITY, 99);
		assertEquals(1, store.getGoals().size());

		// Add HFTD with the Agility 35 template — should reuse the 99 goal.
		api.addQuestGoalWithPrereqs(Quest.HORROR_FROM_THE_DEEP,
			Arrays.asList(skillTemplate(Skill.AGILITY, 35)));
		assertEquals(2, store.getGoals().size());

		// Undo ONLY the quest-add gesture.
		boolean undone = api.undo();
		assertTrue(undone);

		// The pre-existing 99 Agility goal must survive — the compound
		// should only revert what it actually created (quest goal + edge),
		// not the find-or-create match.
		assertEquals(1, store.getGoals().size());
		Goal survivor = findById(existingAgilityId);
		assertNotNull(survivor, "pre-existing agility goal must survive undo of quest-add");
	}
}
