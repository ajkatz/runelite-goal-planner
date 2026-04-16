package com.goalplanner.persistence;

import com.goalplanner.model.Goal;
import com.goalplanner.model.GoalStatus;
import com.goalplanner.model.GoalType;
import com.goalplanner.model.Section;
import com.goalplanner.testsupport.InMemoryConfigManager;
import net.runelite.client.config.ConfigManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the real {@link GoalStore} against an {@link InMemoryConfigManager}.
 * Covers persistence round-trip, built-in section creation, orphaned goal
 * migration, normalize/reconcile, and the user-section CRUD surface
 * (createUserSection, renameUserSection, deleteUserSection, moveGoalToSection).
 */
class GoalStoreTest
{
	private ConfigManager configManager;
	private GoalStore store;

	@BeforeEach
	void setUp()
	{
		configManager = InMemoryConfigManager.create();
		store = new GoalStore(configManager);
		store.load();
	}

	// ====================================================================
	// Built-in section setup
	// ====================================================================

	@Test
	@DisplayName("load() creates Incomplete + Completed sections from empty state")
	void loadCreatesBuiltInSections()
	{
		List<Section> sections = store.getSections();
		assertEquals(2, sections.size());
		assertNotNull(store.getIncompleteSection());
		assertNotNull(store.getCompletedSection());
		assertEquals(Section.BuiltInKind.INCOMPLETE, store.getIncompleteSection().getBuiltInKind());
		assertEquals(Section.BuiltInKind.COMPLETED, store.getCompletedSection().getBuiltInKind());
	}

	@Test
	@DisplayName("Incomplete section is pinned at MAX_VALUE - 1; Completed at MAX_VALUE")
	void builtInSectionOrdering()
	{
		assertEquals(Section.ORDER_INCOMPLETE, store.getIncompleteSection().getOrder());
		assertEquals(Section.ORDER_COMPLETED, store.getCompletedSection().getOrder());
		assertTrue(store.getIncompleteSection().getOrder() < store.getCompletedSection().getOrder());
	}

	// ====================================================================
	// addGoal: section assignment
	// ====================================================================

	@Test
	@DisplayName("addGoal with no sectionId places goal in Incomplete by default")
	void addGoalDefaultsToIncomplete()
	{
		Goal goal = Goal.builder().type(GoalType.CUSTOM).name("Test").build();
		store.addGoal(goal);

		assertEquals(store.getIncompleteSection().getId(), goal.getSectionId());
		assertEquals(0, goal.getPriority());
	}

	@Test
	@DisplayName("addGoal with explicit sectionId honors it")
	void addGoalRespectsExplicitSection()
	{
		Section custom = store.createUserSection("Test Section");
		Goal goal = Goal.builder().type(GoalType.CUSTOM).name("Test").sectionId(custom.getId()).build();
		store.addGoal(goal);

		assertEquals(custom.getId(), goal.getSectionId());
	}

	// ====================================================================
	// User-section CRUD
	// ====================================================================

	@Test
	@DisplayName("createUserSection creates a new section and renumbers user-band orders")
	void createUserSectionAssignsOrder()
	{
		Section a = store.createUserSection("Alpha");
		Section b = store.createUserSection("Bravo");
		Section c = store.createUserSection("Charlie");

		// User band starts at 1; built-in Incomplete is at MAX-1
		assertEquals(1, a.getOrder());
		assertEquals(2, b.getOrder());
		assertEquals(3, c.getOrder());
	}

	@Test
	@DisplayName("createUserSection is idempotent on case-insensitive name match")
	void createUserSectionIdempotent()
	{
		Section first = store.createUserSection("Boss Tasks");
		Section dup = store.createUserSection("boss tasks");
		assertEquals(first.getId(), dup.getId());
	}

	@Test
	@DisplayName("createUserSection rejects empty / overlong / reserved names")
	void createUserSectionValidation()
	{
		assertThrows(IllegalArgumentException.class, () -> store.createUserSection(""));
		assertThrows(IllegalArgumentException.class, () -> store.createUserSection("   "));
		assertThrows(IllegalArgumentException.class, () -> store.createUserSection("Incomplete"));
		assertThrows(IllegalArgumentException.class, () -> store.createUserSection("incomplete"));
		assertThrows(IllegalArgumentException.class, () -> store.createUserSection("Completed"));
		String tooLong = "x".repeat(50);
		assertThrows(IllegalArgumentException.class, () -> store.createUserSection(tooLong));
	}

	@Test
	@DisplayName("renameUserSection updates the name on success")
	void renameUserSectionSuccess()
	{
		Section a = store.createUserSection("Alpha");
		assertTrue(store.renameUserSection(a.getId(), "Apex"));
		assertEquals("Apex", a.getName());
	}

	@Test
	@DisplayName("renameUserSection rejects built-in, no-op, and duplicate names")
	void renameUserSectionRejections()
	{
		Section incomplete = store.getIncompleteSection();
		assertFalse(store.renameUserSection(incomplete.getId(), "Whatever"));

		Section a = store.createUserSection("Alpha");
		Section b = store.createUserSection("Bravo");
		assertFalse(store.renameUserSection(a.getId(), "Alpha")); // no-op
		assertFalse(store.renameUserSection(a.getId(), "Bravo")); // duplicate
	}

	@Test
	@DisplayName("deleteUserSection moves all goals to Incomplete and renumbers band")
	void deleteUserSectionReassignsGoals()
	{
		Section a = store.createUserSection("Alpha");
		Section b = store.createUserSection("Bravo");

		Goal g1 = Goal.builder().type(GoalType.CUSTOM).name("g1").sectionId(a.getId()).build();
		Goal g2 = Goal.builder().type(GoalType.CUSTOM).name("g2").sectionId(a.getId()).build();
		store.addGoal(g1);
		store.addGoal(g2);

		assertTrue(store.deleteUserSection(a.getId()));
		assertNull(store.findSection(a.getId()));
		assertEquals(store.getIncompleteSection().getId(), g1.getSectionId());
		assertEquals(store.getIncompleteSection().getId(), g2.getSectionId());
		// Bravo should be renumbered to position 1
		assertEquals(1, b.getOrder());
	}

	@Test
	@DisplayName("deleteUserSection rejects built-in sections")
	void deleteUserSectionRejectsBuiltIn()
	{
		assertFalse(store.deleteUserSection(store.getIncompleteSection().getId()));
		assertFalse(store.deleteUserSection(store.getCompletedSection().getId()));
	}

	@Test
	@DisplayName("moveGoalToSection moves an incomplete goal between sections")
	void moveGoalToSectionHappyPath()
	{
		Section custom = store.createUserSection("Boss Tasks");
		Goal g = Goal.builder().type(GoalType.CUSTOM).name("g").build();
		store.addGoal(g);
		assertEquals(store.getIncompleteSection().getId(), g.getSectionId());

		assertTrue(store.moveGoalToSection(g.getId(), custom.getId()));
		assertEquals(custom.getId(), g.getSectionId());
	}

	@Test
	@DisplayName("moveGoalToSection rejects no-op (same section)")
	void moveGoalToSectionRejectsNoop()
	{
		Goal g = Goal.builder().type(GoalType.CUSTOM).name("g").build();
		store.addGoal(g);
		assertFalse(store.moveGoalToSection(g.getId(), g.getSectionId()));
	}

	@Test
	@DisplayName("moveGoalToSection rejects complete goal moving away from Completed")
	void moveGoalToSectionRejectsCompleteEscapingCompleted()
	{
		Section custom = store.createUserSection("Custom");
		Goal g = Goal.builder().type(GoalType.CUSTOM).name("g")
			.completedAt(System.currentTimeMillis())
			.status(GoalStatus.COMPLETE)
			.sectionId(store.getCompletedSection().getId())
			.build();
		store.addGoal(g);
		// addGoal would override the section to Incomplete since g.sectionId was set,
		// but actually addGoal only overrides when null. Re-set explicitly.
		g.setSectionId(store.getCompletedSection().getId());

		assertFalse(store.moveGoalToSection(g.getId(), custom.getId()));
	}

	// ====================================================================
	// reconcileCompletedSection
	// ====================================================================

	@Test
	@DisplayName("reconcileCompletedSection moves COMPLETE goals to Completed")
	void reconcilePullsCompleteIntoCompleted()
	{
		Section custom = store.createUserSection("Custom");
		Goal g = Goal.builder().type(GoalType.CUSTOM).name("g").sectionId(custom.getId()).build();
		store.addGoal(g);

		// Mark complete by stamping completedAt
		g.setCompletedAt(System.currentTimeMillis());
		g.setStatus(GoalStatus.COMPLETE);

		assertTrue(store.reconcileCompletedSection());
		assertEquals(store.getCompletedSection().getId(), g.getSectionId());
	}

	@Test
	@DisplayName("reconcileCompletedSection moves un-completed goals out of Completed")
	void reconcilePushesIncompleteOutOfCompleted()
	{
		Goal g = Goal.builder().type(GoalType.CUSTOM).name("g")
			.completedAt(System.currentTimeMillis())
			.status(GoalStatus.COMPLETE)
			.build();
		store.addGoal(g);
		store.reconcileCompletedSection();
		assertEquals(store.getCompletedSection().getId(), g.getSectionId());

		// Un-complete it
		g.setCompletedAt(0);
		g.setStatus(GoalStatus.ACTIVE);

		assertTrue(store.reconcileCompletedSection());
		assertEquals(store.getIncompleteSection().getId(), g.getSectionId());
	}

	@Test
	@DisplayName("reconcileCompletedSection no-ops when nothing needs moving")
	void reconcileNoopOnSteadyState()
	{
		Goal g = Goal.builder().type(GoalType.CUSTOM).name("g").build();
		store.addGoal(g);
		assertFalse(store.reconcileCompletedSection());
	}

	// ====================================================================
	// Persistence round-trip
	// ====================================================================

	@Test
	@DisplayName("save/load round-trips goals and sections")
	void persistenceRoundTrip()
	{
		Section custom = store.createUserSection("Custom Section");
		Goal g1 = Goal.builder().type(GoalType.CUSTOM).name("First").sectionId(custom.getId()).build();
		Goal g2 = Goal.builder().type(GoalType.SKILL).name("Attack").skillName("ATTACK")
			.targetValue(13034431).build();
		store.addGoal(g1);
		store.addGoal(g2);
		store.save();

		// Reload from the same backing config
		GoalStore reloaded = new GoalStore(configManager);
		reloaded.load();

		assertEquals(2, reloaded.getGoals().size());
		assertEquals(3, reloaded.getSections().size()); // Incomplete + Completed + Custom
		assertNotNull(reloaded.findUserSectionByName("Custom Section"));
	}

	@Test
	@DisplayName("load migrates orphaned goals (null sectionId) to Incomplete")
	void loadMigratesOrphanedGoals()
	{
		Goal orphan = Goal.builder().type(GoalType.CUSTOM).name("Orphan").build();
		// Explicitly clear the sectionId to simulate legacy data without sections
		orphan.setSectionId(null);
		store.getGoals().add(orphan);
		store.save();

		// Load fresh — should migrate the orphan
		GoalStore reloaded = new GoalStore(configManager);
		reloaded.load();
		Goal loaded = reloaded.getGoals().stream()
			.filter(g -> "Orphan".equals(g.getName())).findFirst().orElse(null);
		assertNotNull(loaded);
		assertEquals(reloaded.getIncompleteSection().getId(), loaded.getSectionId());
	}

	// ====================================================================
	// removeAllUserSections
	// ====================================================================

	@Test
	@DisplayName("removeAllUserSections deletes all user sections, preserves built-ins")
	void removeAllUserSectionsDeletesUserOnly()
	{
		store.createUserSection("Alpha");
		store.createUserSection("Bravo");
		Goal g = Goal.builder().type(GoalType.CUSTOM).name("g")
			.sectionId(store.findUserSectionByName("Alpha").getId()).build();
		store.addGoal(g);

		int removed = store.removeAllUserSections();
		assertEquals(2, removed);
		assertEquals(2, store.getSections().size()); // built-ins remain
		assertEquals(store.getIncompleteSection().getId(), g.getSectionId());
	}

	// ====================================================================
	// Relations — cycle detection, addRequirement, removeRequirement
	// ====================================================================

	/** Helper: create and store a minimal custom goal with the given name, return it. */
	private Goal custom(String name)
	{
		Goal g = Goal.builder().type(GoalType.CUSTOM).name(name).build();
		store.addGoal(g);
		return g;
	}

	@Test
	@DisplayName("addRequirement happy path: adds edge, appears in requiredGoalIds")
	void addRequirementHappyPath()
	{
		Goal a = custom("A");
		Goal b = custom("B");
		assertTrue(store.addRequirement(a.getId(), b.getId()));
		assertEquals(List.of(b.getId()), a.getRequiredGoalIds());
		assertTrue(b.getRequiredGoalIds().isEmpty());
	}

	@Test
	@DisplayName("addRequirement is idempotent: duplicate edge returns false, no change")
	void addRequirementIdempotent()
	{
		Goal a = custom("A");
		Goal b = custom("B");
		assertTrue(store.addRequirement(a.getId(), b.getId()));
		assertFalse(store.addRequirement(a.getId(), b.getId())); // already there
		assertEquals(1, a.getRequiredGoalIds().size());
	}

	@Test
	@DisplayName("addRequirement rejects self-loop")
	void addRequirementRejectsSelfLoop()
	{
		Goal a = custom("A");
		assertFalse(store.addRequirement(a.getId(), a.getId()));
		assertTrue(a.getRequiredGoalIds().isEmpty());
	}

	@Test
	@DisplayName("addRequirement rejects 2-node cycle: A→B then B→A")
	void addRequirementRejects2NodeCycle()
	{
		Goal a = custom("A");
		Goal b = custom("B");
		assertTrue(store.addRequirement(a.getId(), b.getId()));
		assertFalse(store.addRequirement(b.getId(), a.getId()));
		assertTrue(b.getRequiredGoalIds().isEmpty());
	}

	@Test
	@DisplayName("addRequirement rejects 3-node cycle: A→B, B→C, C→A")
	void addRequirementRejects3NodeCycle()
	{
		Goal a = custom("A");
		Goal b = custom("B");
		Goal c = custom("C");
		assertTrue(store.addRequirement(a.getId(), b.getId()));
		assertTrue(store.addRequirement(b.getId(), c.getId()));
		assertFalse(store.addRequirement(c.getId(), a.getId()));
		assertTrue(c.getRequiredGoalIds().isEmpty());
	}

	@Test
	@DisplayName("addRequirement rejects unknown goal ids")
	void addRequirementRejectsUnknownGoal()
	{
		Goal a = custom("A");
		assertFalse(store.addRequirement(a.getId(), "bogus-id"));
		assertFalse(store.addRequirement("bogus-id", a.getId()));
		assertFalse(store.addRequirement("bogus-id-1", "bogus-id-2"));
	}

	@Test
	@DisplayName("addRequirement allows cross-section edges")
	void addRequirementAllowsCrossSection()
	{
		Section quests = store.createUserSection("Quests");
		Section skills = store.createUserSection("Skills");
		Goal hftd = Goal.builder().type(GoalType.QUEST).name("HFTD")
			.sectionId(quests.getId()).build();
		Goal agility = Goal.builder().type(GoalType.SKILL).name("35 Agility")
			.skillName("AGILITY").sectionId(skills.getId()).build();
		store.addGoal(hftd);
		store.addGoal(agility);

		assertTrue(store.addRequirement(hftd.getId(), agility.getId()));
		assertEquals(List.of(agility.getId()), hftd.getRequiredGoalIds());
	}

	@Test
	@DisplayName("removeRequirement removes an existing edge")
	void removeRequirementHappyPath()
	{
		Goal a = custom("A");
		Goal b = custom("B");
		store.addRequirement(a.getId(), b.getId());
		assertTrue(store.removeRequirement(a.getId(), b.getId()));
		assertTrue(a.getRequiredGoalIds().isEmpty());
	}

	@Test
	@DisplayName("removeRequirement returns false when edge doesn't exist")
	void removeRequirementMissingEdge()
	{
		Goal a = custom("A");
		Goal b = custom("B");
		assertFalse(store.removeRequirement(a.getId(), b.getId()));
	}

	@Test
	@DisplayName("wouldCreateCycle detects direct 2-node cycle")
	void wouldCreateCycle2Node()
	{
		Goal a = custom("A");
		Goal b = custom("B");
		store.addRequirement(a.getId(), b.getId());
		assertTrue(store.wouldCreateCycle(b.getId(), a.getId()));
		assertFalse(store.wouldCreateCycle(a.getId(), b.getId())); // already exists, not a cycle
	}

	@Test
	@DisplayName("wouldCreateCycle detects transitive cycle through intermediate nodes")
	void wouldCreateCycleTransitive()
	{
		Goal a = custom("A");
		Goal b = custom("B");
		Goal c = custom("C");
		Goal d = custom("D");
		store.addRequirement(a.getId(), b.getId());
		store.addRequirement(b.getId(), c.getId());
		store.addRequirement(c.getId(), d.getId());
		// Adding D→A would create a 4-cycle
		assertTrue(store.wouldCreateCycle(d.getId(), a.getId()));
		// Adding D→B would create a 3-cycle
		assertTrue(store.wouldCreateCycle(d.getId(), b.getId()));
		// Adding A→D is fine (linear extension)
		assertFalse(store.wouldCreateCycle(a.getId(), d.getId()));
	}

	@Test
	@DisplayName("getDependents returns goals that require the given one")
	void getDependentsReturnsIncomingEdges()
	{
		Goal leaf = custom("35 Agility");
		Goal hftd = custom("HFTD");
		Goal mm2  = custom("MM2");
		store.addRequirement(hftd.getId(), leaf.getId());
		store.addRequirement(mm2.getId(),  leaf.getId());

		List<String> deps = store.getDependents(leaf.getId());
		assertEquals(2, deps.size());
		assertTrue(deps.contains(hftd.getId()));
		assertTrue(deps.contains(mm2.getId()));
	}

	@Test
	@DisplayName("Diamond DAG: two paths through different nodes is allowed")
	void diamondDagAllowed()
	{
		Goal top = custom("top");
		Goal left = custom("left");
		Goal right = custom("right");
		Goal bottom = custom("bottom");
		// top depends on both left and right; both depend on bottom.
		assertTrue(store.addRequirement(top.getId(), left.getId()));
		assertTrue(store.addRequirement(top.getId(), right.getId()));
		assertTrue(store.addRequirement(left.getId(), bottom.getId()));
		assertTrue(store.addRequirement(right.getId(), bottom.getId()));
		// Adding bottom→top would close the diamond into a cycle.
		assertFalse(store.addRequirement(bottom.getId(), top.getId()));
	}

	// ====================================================================
	// Relations — findMatchingGoal (structural match + satisfies semantics)
	// ====================================================================

	/** Template: a fresh Goal object used as a "spec" to match against existing goals. */
	private Goal skillTemplate(String skillName, int targetXp)
	{
		return Goal.builder().type(GoalType.SKILL).skillName(skillName).targetValue(targetXp).build();
	}

	private Goal itemTemplate(int itemId, int targetQty)
	{
		return Goal.builder().type(GoalType.ITEM_GRIND).itemId(itemId).targetValue(targetQty).build();
	}

	private Goal questTemplate(String name)
	{
		return Goal.builder().type(GoalType.QUEST).name(name).build();
	}

	@Test
	@DisplayName("findMatchingGoal: skill exact match")
	void findMatchingGoalSkillExact()
	{
		Goal existing = Goal.builder().type(GoalType.SKILL).name("Prayer 50")
			.skillName("PRAYER").targetValue(101333).build();
		store.addGoal(existing);

		Goal match = store.findMatchingGoal(skillTemplate("PRAYER", 101333));
		assertNotNull(match);
		assertEquals(existing.getId(), match.getId());
	}

	@Test
	@DisplayName("findMatchingGoal: skill satisfies match — 99 covers 35 request")
	void findMatchingGoalSkillSatisfies()
	{
		// 99 agility = 13,034,431 xp; 35 agility = ~22,406 xp
		Goal existing99 = Goal.builder().type(GoalType.SKILL).name("Agility 99")
			.skillName("AGILITY").targetValue(13_034_431).build();
		store.addGoal(existing99);

		Goal match = store.findMatchingGoal(skillTemplate("AGILITY", 22_406));
		assertNotNull(match);
		assertEquals(existing99.getId(), match.getId());
	}

	@Test
	@DisplayName("findMatchingGoal: skill no match — existing target lower than required")
	void findMatchingGoalSkillNotSatisfied()
	{
		Goal existing35 = Goal.builder().type(GoalType.SKILL).name("Agility 35")
			.skillName("AGILITY").targetValue(22_406).build();
		store.addGoal(existing35);

		// Looking for 99 agility; 35 doesn't satisfy
		assertNull(store.findMatchingGoal(skillTemplate("AGILITY", 13_034_431)));
	}

	@Test
	@DisplayName("findMatchingGoal: skill no match — wrong skill")
	void findMatchingGoalSkillWrongSkill()
	{
		Goal prayer = Goal.builder().type(GoalType.SKILL).name("Prayer 99")
			.skillName("PRAYER").targetValue(13_034_431).build();
		store.addGoal(prayer);

		assertNull(store.findMatchingGoal(skillTemplate("AGILITY", 13_034_431)));
	}

	@Test
	@DisplayName("findMatchingGoal: item exact match")
	void findMatchingGoalItemExact()
	{
		Goal existing = Goal.builder().type(GoalType.ITEM_GRIND).name("Wrath rune")
			.itemId(21880).targetValue(50_000).build();
		store.addGoal(existing);

		Goal match = store.findMatchingGoal(itemTemplate(21880, 50_000));
		assertNotNull(match);
		assertEquals(existing.getId(), match.getId());
	}

	@Test
	@DisplayName("findMatchingGoal: item satisfies match — 10k covers 100 request")
	void findMatchingGoalItemSatisfies()
	{
		Goal existing = Goal.builder().type(GoalType.ITEM_GRIND).name("Wrath rune")
			.itemId(21880).targetValue(10_000).build();
		store.addGoal(existing);

		Goal match = store.findMatchingGoal(itemTemplate(21880, 100));
		assertNotNull(match);
		assertEquals(existing.getId(), match.getId());
	}

	@Test
	@DisplayName("findMatchingGoal: item no match — wrong itemId")
	void findMatchingGoalItemWrongId()
	{
		Goal existing = Goal.builder().type(GoalType.ITEM_GRIND).name("Wrath rune")
			.itemId(21880).targetValue(10_000).build();
		store.addGoal(existing);

		assertNull(store.findMatchingGoal(itemTemplate(554, 10_000))); // fire rune
	}

	@Test
	@DisplayName("findMatchingGoal: quest match by name (case-insensitive, trimmed)")
	void findMatchingGoalQuestNameMatch()
	{
		Goal existing = Goal.builder().type(GoalType.QUEST).name("Dragon Slayer I").build();
		store.addGoal(existing);

		assertEquals(existing.getId(), store.findMatchingGoal(questTemplate("Dragon Slayer I")).getId());
		assertEquals(existing.getId(), store.findMatchingGoal(questTemplate("dragon slayer i")).getId());
		assertEquals(existing.getId(), store.findMatchingGoal(questTemplate("  Dragon Slayer I  ")).getId());
		assertNull(store.findMatchingGoal(questTemplate("Dragon Slayer II")));
	}

	@Test
	@DisplayName("findMatchingGoal: CA match by caTaskId preferred over name")
	void findMatchingGoalCaByTaskId()
	{
		Goal existing = Goal.builder().type(GoalType.COMBAT_ACHIEVEMENT)
			.name("Dragon Slayer Killer").caTaskId(42).build();
		store.addGoal(existing);

		Goal template = Goal.builder().type(GoalType.COMBAT_ACHIEVEMENT).caTaskId(42).build();
		assertEquals(existing.getId(), store.findMatchingGoal(template).getId());
		Goal otherIdTemplate = Goal.builder().type(GoalType.COMBAT_ACHIEVEMENT).caTaskId(99).build();
		assertNull(store.findMatchingGoal(otherIdTemplate));
	}

	@Test
	@DisplayName("findMatchingGoal: different goal types never match")
	void findMatchingGoalDifferentTypes()
	{
		Goal skill = Goal.builder().type(GoalType.SKILL).name("Agility 99")
			.skillName("AGILITY").targetValue(13_034_431).build();
		store.addGoal(skill);

		Goal itemTmpl = itemTemplate(21880, 1);
		assertNull(store.findMatchingGoal(itemTmpl));
	}

	@Test
	@DisplayName("findMatchingGoal: searches across all sections globally")
	void findMatchingGoalCrossSection()
	{
		Section skills = store.createUserSection("Skills");
		Goal agility = Goal.builder().type(GoalType.SKILL).name("Agility 99")
			.skillName("AGILITY").targetValue(13_034_431).sectionId(skills.getId()).build();
		store.addGoal(agility);

		Goal match = store.findMatchingGoal(skillTemplate("AGILITY", 22_406));
		assertNotNull(match);
		assertEquals(agility.getId(), match.getId());
	}

	@Test
	@DisplayName("findMatchingGoal: returns null when nothing matches")
	void findMatchingGoalNoMatch()
	{
		assertNull(store.findMatchingGoal(skillTemplate("AGILITY", 13_034_431)));
	}

	// ====================================================================
	// Relations — removeGoalWithBypass (doubly-linked-list-style removal)
	// ====================================================================

	@Test
	@DisplayName("removeGoalWithBypass: linear chain P→D→S bridges to P→S")
	void bypassLinearChain()
	{
		Goal p = custom("P");
		Goal d = custom("D");
		Goal s = custom("S");
		store.addRequirement(p.getId(), d.getId()); // P requires D
		store.addRequirement(d.getId(), s.getId()); // D requires S

		GoalStore.RemoveGoalBypassSnapshot snap = store.removeGoalWithBypass(d.getId());

		assertNotNull(snap);
		// D is gone
		assertNull(store.getGoals().stream().filter(g -> g.getId().equals(d.getId()))
			.findFirst().orElse(null));
		// P now requires S directly (bypass edge)
		assertTrue(p.getRequiredGoalIds().contains(s.getId()));
		// P no longer requires D (scrubbed)
		assertFalse(p.getRequiredGoalIds().contains(d.getId()));
		// Snapshot reports what it did
		assertEquals(1, snap.addedBypassEdges.size());
		assertEquals(p.getId(), snap.addedBypassEdges.get(0)[0]);
		assertEquals(s.getId(), snap.addedBypassEdges.get(0)[1]);
		assertEquals(List.of(p.getId()), snap.predecessors);
	}

	@Test
	@DisplayName("removeGoalWithBypass: fan-out P→D→{S1,S2} bridges to {P→S1, P→S2}")
	void bypassFanOut()
	{
		Goal p = custom("P");
		Goal d = custom("D");
		Goal s1 = custom("S1");
		Goal s2 = custom("S2");
		store.addRequirement(p.getId(), d.getId());
		store.addRequirement(d.getId(), s1.getId());
		store.addRequirement(d.getId(), s2.getId());

		GoalStore.RemoveGoalBypassSnapshot snap = store.removeGoalWithBypass(d.getId());

		assertTrue(p.getRequiredGoalIds().contains(s1.getId()));
		assertTrue(p.getRequiredGoalIds().contains(s2.getId()));
		assertEquals(2, snap.addedBypassEdges.size());
	}

	@Test
	@DisplayName("removeGoalWithBypass: fan-in {P1,P2}→D→S bridges to {P1→S, P2→S}")
	void bypassFanIn()
	{
		Goal p1 = custom("P1");
		Goal p2 = custom("P2");
		Goal d = custom("D");
		Goal s = custom("S");
		store.addRequirement(p1.getId(), d.getId());
		store.addRequirement(p2.getId(), d.getId());
		store.addRequirement(d.getId(), s.getId());

		GoalStore.RemoveGoalBypassSnapshot snap = store.removeGoalWithBypass(d.getId());

		assertTrue(p1.getRequiredGoalIds().contains(s.getId()));
		assertTrue(p2.getRequiredGoalIds().contains(s.getId()));
		assertEquals(2, snap.addedBypassEdges.size());
		assertEquals(2, snap.predecessors.size());
	}

	@Test
	@DisplayName("removeGoalWithBypass: dense {P1,P2,P3}→D→{S1,S2,S3,S4} adds 12 bypass edges")
	void bypassDense()
	{
		Goal[] ps = {custom("P1"), custom("P2"), custom("P3")};
		Goal d = custom("D");
		Goal[] ss = {custom("S1"), custom("S2"), custom("S3"), custom("S4")};
		for (Goal p : ps) store.addRequirement(p.getId(), d.getId());
		for (Goal s : ss) store.addRequirement(d.getId(), s.getId());

		GoalStore.RemoveGoalBypassSnapshot snap = store.removeGoalWithBypass(d.getId());

		assertEquals(12, snap.addedBypassEdges.size()); // 3 * 4
		for (Goal p : ps)
		{
			for (Goal s : ss)
			{
				assertTrue(p.getRequiredGoalIds().contains(s.getId()),
					"Expected " + p.getName() + " to require " + s.getName());
			}
		}
	}

	@Test
	@DisplayName("removeGoalWithBypass: dedupe — existing P→S is not re-added as a bypass")
	void bypassDedupe()
	{
		Goal p = custom("P");
		Goal d = custom("D");
		Goal s = custom("S");
		store.addRequirement(p.getId(), d.getId());
		store.addRequirement(d.getId(), s.getId());
		store.addRequirement(p.getId(), s.getId()); // P already requires S directly

		GoalStore.RemoveGoalBypassSnapshot snap = store.removeGoalWithBypass(d.getId());

		// P still requires S, but we didn't add a duplicate
		assertTrue(p.getRequiredGoalIds().contains(s.getId()));
		assertEquals(1, p.getRequiredGoalIds().stream().filter(s.getId()::equals).count());
		// No bypass edge recorded (it was already there)
		assertEquals(0, snap.addedBypassEdges.size());
	}

	@Test
	@DisplayName("removeGoalWithBypass: no predecessors (leaf root delete) — no bypasses, successors lose inbound")
	void bypassNoPredecessors()
	{
		Goal d = custom("D");
		Goal s = custom("S");
		store.addRequirement(d.getId(), s.getId());

		GoalStore.RemoveGoalBypassSnapshot snap = store.removeGoalWithBypass(d.getId());

		assertEquals(0, snap.addedBypassEdges.size());
		assertEquals(0, snap.predecessors.size());
		// S still exists as a leaf, nothing referencing it now
		assertEquals(0, store.getDependents(s.getId()).size());
	}

	@Test
	@DisplayName("removeGoalWithBypass: no successors (true leaf delete) — no bypasses, predecessors lose outbound")
	void bypassNoSuccessors()
	{
		Goal p = custom("P");
		Goal d = custom("D");
		store.addRequirement(p.getId(), d.getId());

		GoalStore.RemoveGoalBypassSnapshot snap = store.removeGoalWithBypass(d.getId());

		assertEquals(0, snap.addedBypassEdges.size());
		assertEquals(1, snap.predecessors.size());
		assertEquals(p.getId(), snap.predecessors.get(0));
		assertFalse(p.getRequiredGoalIds().contains(d.getId()));
	}

	@Test
	@DisplayName("removeGoalWithBypass: isolated goal (no edges at all) — clean no-op")
	void bypassIsolated()
	{
		Goal d = custom("D");
		GoalStore.RemoveGoalBypassSnapshot snap = store.removeGoalWithBypass(d.getId());

		assertEquals(0, snap.addedBypassEdges.size());
		assertEquals(0, snap.predecessors.size());
		assertNotNull(snap.goal);
		assertEquals("D", snap.goal.getName());
	}

	@Test
	@DisplayName("removeGoalWithBypass: snapshot captures enough state to fully restore")
	void bypassRevertRoundTrip()
	{
		Goal p1 = custom("P1");
		Goal p2 = custom("P2");
		Goal d = custom("D");
		Goal s1 = custom("S1");
		Goal s2 = custom("S2");
		store.addRequirement(p1.getId(), d.getId());
		store.addRequirement(p2.getId(), d.getId());
		store.addRequirement(d.getId(), s1.getId());
		store.addRequirement(d.getId(), s2.getId());

		// Snapshot the state we want to restore
		List<String> dOriginalRequires = new java.util.ArrayList<>(d.getRequiredGoalIds());

		GoalStore.RemoveGoalBypassSnapshot snap = store.removeGoalWithBypass(d.getId());

		// Simulate revert:
		// 1. Remove the bypass edges we added
		for (String[] edge : snap.addedBypassEdges)
		{
			assertTrue(store.removeRequirement(edge[0], edge[1]));
		}
		// 2. Re-insert the deleted goal at its original index
		store.insertGoalAt(snap.goal, snap.originalIndex);
		// 3. Goal's own outgoing edges are already on the snapshotted Goal object.
		// 4. Restore incoming edges
		for (String predId : snap.predecessors)
		{
			assertTrue(store.addRequirement(predId, d.getId()));
		}

		// Verify final state matches original
		assertEquals(dOriginalRequires, d.getRequiredGoalIds());
		assertTrue(p1.getRequiredGoalIds().contains(d.getId()));
		assertTrue(p2.getRequiredGoalIds().contains(d.getId()));
		assertFalse(p1.getRequiredGoalIds().contains(s1.getId())); // bypass gone
		assertFalse(p1.getRequiredGoalIds().contains(s2.getId()));
		assertFalse(p2.getRequiredGoalIds().contains(s1.getId()));
		assertFalse(p2.getRequiredGoalIds().contains(s2.getId()));
	}

	@Test
	@DisplayName("removeGoalWithBypass: returns null for unknown goal id")
	void bypassUnknownGoal()
	{
		assertNull(store.removeGoalWithBypass("bogus-id"));
	}

	// ====================================================================
	// Relations — load-time cycle / dangling edge scrub
	// ====================================================================

	@Test
	@DisplayName("Load scrub: drops dangling requirement edges pointing at missing goals")
	void scrubDanglingEdges()
	{
		Goal a = Goal.builder().type(GoalType.CUSTOM).name("A")
			.requiredGoalIds(new java.util.ArrayList<>(java.util.List.of("ghost-id"))).build();
		store.addGoal(a);
		store.save();

		GoalStore reloaded = new GoalStore(configManager);
		reloaded.load();
		Goal loaded = reloaded.getGoals().stream()
			.filter(g -> "A".equals(g.getName())).findFirst().orElseThrow();
		assertTrue(loaded.getRequiredGoalIds().isEmpty());
	}

	@Test
	@DisplayName("Load scrub: drops self-loop edges")
	void scrubSelfLoops()
	{
		Goal a = Goal.builder().type(GoalType.CUSTOM).name("A").build();
		// Use builder's id for the self-loop reference
		a.getRequiredGoalIds().add(a.getId());
		store.addGoal(a);
		store.save();

		GoalStore reloaded = new GoalStore(configManager);
		reloaded.load();
		Goal loaded = reloaded.getGoals().stream()
			.filter(g -> "A".equals(g.getName())).findFirst().orElseThrow();
		assertTrue(loaded.getRequiredGoalIds().isEmpty());
	}

	@Test
	@DisplayName("Load scrub: drops cycle-forming edges, keeps acyclic ones")
	void scrubCycleEdges()
	{
		Goal a = Goal.builder().type(GoalType.CUSTOM).name("A").build();
		Goal b = Goal.builder().type(GoalType.CUSTOM).name("B").build();
		Goal c = Goal.builder().type(GoalType.CUSTOM).name("C").build();
		// Build a 3-cycle A→B→C→A by directly manipulating the fields.
		a.getRequiredGoalIds().add(b.getId());
		b.getRequiredGoalIds().add(c.getId());
		c.getRequiredGoalIds().add(a.getId());
		store.addGoal(a);
		store.addGoal(b);
		store.addGoal(c);
		store.save();

		GoalStore reloaded = new GoalStore(configManager);
		reloaded.load();
		Goal la = reloaded.getGoals().stream()
			.filter(g -> "A".equals(g.getName())).findFirst().orElseThrow();
		Goal lb = reloaded.getGoals().stream()
			.filter(g -> "B".equals(g.getName())).findFirst().orElseThrow();
		Goal lc = reloaded.getGoals().stream()
			.filter(g -> "C".equals(g.getName())).findFirst().orElseThrow();
		// Exactly one of the three cycle-forming edges was dropped; the graph
		// is now a 2-edge chain with no cycle. Which specific edge gets
		// dropped depends on iteration order, but the total count is fixed.
		int totalEdges = la.getRequiredGoalIds().size()
			+ lb.getRequiredGoalIds().size()
			+ lc.getRequiredGoalIds().size();
		assertEquals(2, totalEdges);
		// After scrub, no pair of goals can close a cycle via a new edge
		// unless it would re-create the one that was dropped. Verify
		// acyclicity by confirming that adding ANY new edge to the graph
		// requires no more than 1 attempt to succeed (i.e. there IS at least
		// one ordered pair that wouldn't close a cycle). This is a weaker
		// but sound acyclicity check.
		String[] ids = {la.getId(), lb.getId(), lc.getId()};
		boolean atLeastOneAcyclicAdd = false;
		for (String from : ids)
		{
			for (String to : ids)
			{
				if (from.equals(to)) continue;
				if (!reloaded.wouldCreateCycle(from, to))
				{
					atLeastOneAcyclicAdd = true;
					break;
				}
			}
			if (atLeastOneAcyclicAdd) break;
		}
		assertTrue(atLeastOneAcyclicAdd);
	}
}
