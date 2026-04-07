package com.goaltracker.persistence;

import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalStatus;
import com.goaltracker.model.GoalType;
import com.goaltracker.model.Section;
import com.goaltracker.testsupport.InMemoryConfigManager;
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
		// Explicitly clear the sectionId to simulate pre-Mission-11 data
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
}
