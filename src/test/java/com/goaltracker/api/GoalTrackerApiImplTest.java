package com.goaltracker.api;

import com.goaltracker.data.WikiCaRepository;
import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalStatus;
import com.goaltracker.model.GoalType;
import com.goaltracker.model.ItemTag;
import com.goaltracker.model.Section;
import com.goaltracker.model.TagCategory;
import com.goaltracker.persistence.GoalStore;
import com.goaltracker.service.GoalReorderingService;
import com.goaltracker.testsupport.InMemoryConfigManager;
import net.runelite.api.Skill;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link GoalTrackerApiImpl}, the canonical mutation surface for
 * the plugin. Every other layer (panel, trackers, right-click menus) goes
 * through this class. Coverage focuses on:
 *
 * <ul>
 *   <li>The public API: addX, removeGoal, changeTarget, markGoal pair, setX</li>
 *   <li>The internal API: section CRUD, color overrides, selection,
 *       recordGoalProgress, moveGoalToSection</li>
 *   <li>onGoalsChanged callback firing semantics (only on actual change)</li>
 *   <li>Validation rules (built-ins immutable, no-op detection, type gates
 *       that survived Mission 17)</li>
 * </ul>
 *
 * <p>Uses a real GoalStore against an InMemoryConfigManager. ItemManager and
 * WikiCaRepository are mocked because the test class does not exercise item
 * lookups (those have their own coverage).
 */
class GoalTrackerApiImplTest
{
	private GoalStore store;
	private GoalTrackerApiImpl api;
	private AtomicInteger callbackFireCount;

	@BeforeEach
	void setUp()
	{
		ConfigManager configManager = InMemoryConfigManager.create();
		store = new GoalStore(configManager);
		store.load();

		ItemManager itemManager = mock(ItemManager.class);
		WikiCaRepository wikiCaRepository = mock(WikiCaRepository.class);
		GoalReorderingService reorderingService = new GoalReorderingService(store);
		api = new GoalTrackerApiImpl(store, reorderingService, itemManager, wikiCaRepository);

		callbackFireCount = new AtomicInteger(0);
		api.setOnGoalsChanged(callbackFireCount::incrementAndGet);
	}

	// ====================================================================
	// Public API: addSkillGoal / addCustomGoal
	// ====================================================================

	@Nested
	@DisplayName("addSkillGoal")
	class AddSkillGoalTests
	{
		@Test
		@DisplayName("creates a new SKILL goal and fires the callback")
		void createsNewSkillGoal()
		{
			String id = api.addSkillGoal(Skill.ATTACK, 13_034_431);
			assertNotNull(id);
			assertEquals(1, store.getGoals().size());
			assertEquals(1, callbackFireCount.get());

			Goal g = store.getGoals().get(0);
			assertEquals(GoalType.SKILL, g.getType());
			assertEquals("ATTACK", g.getSkillName());
			assertEquals(13_034_431, g.getTargetValue());
		}

		@Test
		@DisplayName("returns existing id on duplicate (skill + target match)")
		void duplicateReturnsExistingId()
		{
			String first = api.addSkillGoal(Skill.ATTACK, 13_034_431);
			String second = api.addSkillGoal(Skill.ATTACK, 13_034_431);
			assertEquals(first, second);
			assertEquals(1, store.getGoals().size());
		}

		@Test
		@DisplayName("auto-positions lower targets above higher targets in same section")
		void autoPositionsByTarget()
		{
			// Add 99 first, then 96 — 96 should bubble up above 99
			api.addSkillGoal(Skill.PRAYER, 13_034_431); // L99
			api.addSkillGoal(Skill.PRAYER, 9_684_577);  // L96

			List<Goal> goals = store.getGoals();
			int idx96 = -1, idx99 = -1;
			for (int i = 0; i < goals.size(); i++)
			{
				if (goals.get(i).getTargetValue() == 9_684_577) idx96 = i;
				if (goals.get(i).getTargetValue() == 13_034_431) idx99 = i;
			}
			assertTrue(idx96 < idx99, "L96 should be above L99 in the list");
		}
	}

	@Nested
	@DisplayName("addCustomGoal")
	class AddCustomGoalTests
	{
		@Test
		@DisplayName("creates a new CUSTOM goal")
		void createsNewCustomGoal()
		{
			String id = api.addCustomGoal("My Goal", "A description");
			assertNotNull(id);
			Goal g = store.getGoals().get(0);
			assertEquals(GoalType.CUSTOM, g.getType());
			assertEquals("My Goal", g.getName());
			assertEquals("A description", g.getDescription());
		}

		@Test
		@DisplayName("rejects null/empty name")
		void rejectsBlankName()
		{
			assertNull(api.addCustomGoal(null, "desc"));
			assertNull(api.addCustomGoal("", "desc"));
			assertNull(api.addCustomGoal("   ", "desc"));
			assertEquals(0, store.getGoals().size());
		}
	}

	// ====================================================================
	// Public API: removeGoal
	// ====================================================================

	@Test
	@DisplayName("removeGoal deletes the goal and fires the callback")
	void removeGoalSuccess()
	{
		String id = api.addCustomGoal("To Delete", "");
		callbackFireCount.set(0);

		assertTrue(api.removeGoal(id));
		assertEquals(0, store.getGoals().size());
		assertEquals(1, callbackFireCount.get());
	}

	@Test
	@DisplayName("removeGoal cleans up the selection set")
	void removeGoalCleansSelection()
	{
		String id = api.addCustomGoal("To Delete", "");
		api.addToGoalSelection(id);
		assertTrue(api.getSelectedGoalIds().contains(id));

		api.removeGoal(id);
		assertFalse(api.getSelectedGoalIds().contains(id));
	}

	@Test
	@DisplayName("removeGoal returns false for unknown id")
	void removeGoalUnknownId()
	{
		assertFalse(api.removeGoal("nonexistent"));
		assertFalse(api.removeGoal(null));
	}

	// ====================================================================
	// Public API: markGoalComplete / markGoalIncomplete
	// ====================================================================

	@Test
	@DisplayName("markGoalComplete works on CUSTOM goals")
	void markCustomGoalComplete()
	{
		String id = api.addCustomGoal("Custom", "");
		assertTrue(api.markGoalComplete(id));

		Goal g = store.getGoals().get(0);
		assertTrue(g.isComplete());
		assertEquals(GoalStatus.COMPLETE, g.getStatus());
		assertTrue(g.getCompletedAt() > 0);
	}

	@Test
	@DisplayName("markGoalComplete works on ITEM_GRIND goals (Mission 17)")
	void markItemGoalComplete()
	{
		Goal item = Goal.builder().type(GoalType.ITEM_GRIND).name("Cannonballs")
			.itemId(2).targetValue(200).currentValue(0).build();
		store.addGoal(item);

		assertTrue(api.markGoalComplete(item.getId()));
		assertTrue(item.isComplete());
	}

	@Test
	@DisplayName("markGoalComplete rejects non-CUSTOM/non-ITEM_GRIND goals")
	void markGoalCompleteRejectsTrackedTypes()
	{
		String skillId = api.addSkillGoal(Skill.ATTACK, 13_034_431);
		assertFalse(api.markGoalComplete(skillId));
	}

	@Test
	@DisplayName("markGoalIncomplete reverts a complete goal")
	void markGoalIncomplete()
	{
		String id = api.addCustomGoal("Custom", "");
		api.markGoalComplete(id);
		assertTrue(store.getGoals().get(0).isComplete());

		assertTrue(api.markGoalIncomplete(id));
		assertFalse(store.getGoals().get(0).isComplete());
		assertEquals(GoalStatus.ACTIVE, store.getGoals().get(0).getStatus());
	}

	// ====================================================================
	// Internal API: section CRUD
	// ====================================================================

	@Nested
	@DisplayName("section CRUD")
	class SectionCrudTests
	{
		@Test
		@DisplayName("createSection returns the new section's id")
		void createSectionReturnsId()
		{
			String id = api.createSection("Boss Tasks");
			assertNotNull(id);
			assertNotNull(store.findSection(id));
			assertEquals("Boss Tasks", store.findSection(id).getName());
		}

		@Test
		@DisplayName("createSection is idempotent on case-insensitive name")
		void createSectionIdempotent()
		{
			String first = api.createSection("Boss Tasks");
			String second = api.createSection("boss tasks");
			assertEquals(first, second);
		}

		@Test
		@DisplayName("createSection throws on invalid name")
		void createSectionRejectsInvalid()
		{
			assertThrows(IllegalArgumentException.class, () -> api.createSection(""));
			assertThrows(IllegalArgumentException.class, () -> api.createSection("Incomplete"));
		}

		@Test
		@DisplayName("renameSection succeeds for user sections")
		void renameSectionUser()
		{
			String id = api.createSection("Alpha");
			callbackFireCount.set(0);
			assertTrue(api.renameSection(id, "Apex"));
			assertEquals("Apex", store.findSection(id).getName());
			assertEquals(1, callbackFireCount.get());
		}

		@Test
		@DisplayName("renameSection rejects built-in sections")
		void renameSectionBuiltIn()
		{
			String incompleteId = store.getIncompleteSection().getId();
			assertFalse(api.renameSection(incompleteId, "Whatever"));
		}

		@Test
		@DisplayName("deleteSection reassigns goals to Incomplete")
		void deleteSectionReassignsGoals()
		{
			String sectionId = api.createSection("Boss Tasks");
			String goalId = api.addCustomGoal("Test", "");
			api.moveGoalToSection(goalId, sectionId);

			assertTrue(api.deleteSection(sectionId));
			assertNull(store.findSection(sectionId));
			Goal g = store.getGoals().get(0);
			assertEquals(store.getIncompleteSection().getId(), g.getSectionId());
		}

		@Test
		@DisplayName("moveGoalToSection enforces skill ordering on destination")
		void moveGoalToSectionEnforcesSkillOrdering()
		{
			String sectionId = api.createSection("Skill Goals");
			String prayer99 = api.addSkillGoal(Skill.PRAYER, 13_034_431);
			String prayer96 = api.addSkillGoal(Skill.PRAYER, 9_684_577);

			// Both currently in Incomplete. Move 99 to Skill Goals first.
			api.moveGoalToSection(prayer99, sectionId);
			// Then move 96 to Skill Goals — 96 should bubble above 99
			api.moveGoalToSection(prayer96, sectionId);

			// Find both in the goals list
			Goal g96 = store.getGoals().stream()
				.filter(g -> g.getId().equals(prayer96)).findFirst().orElseThrow();
			Goal g99 = store.getGoals().stream()
				.filter(g -> g.getId().equals(prayer99)).findFirst().orElseThrow();
			assertEquals(sectionId, g96.getSectionId());
			assertEquals(sectionId, g99.getSectionId());
			assertTrue(g96.getPriority() < g99.getPriority(),
				"L96 should be above L99 within the destination section");
		}
	}

	// ====================================================================
	// Internal API: color overrides
	// ====================================================================

	@Nested
	@DisplayName("color overrides")
	class ColorOverrideTests
	{
		@Test
		@DisplayName("setSectionColor stores override and fires callback")
		void setSectionColor()
		{
			String id = api.createSection("Custom");
			callbackFireCount.set(0);
			assertTrue(api.setSectionColor(id, 0xE74C3C));
			assertEquals(0xE74C3C, store.findSection(id).getColorRgb());
			assertEquals(1, callbackFireCount.get());
		}

		@Test
		@DisplayName("setSectionColor with -1 clears the override")
		void clearSectionColor()
		{
			String id = api.createSection("Custom");
			api.setSectionColor(id, 0xE74C3C);
			assertTrue(api.setSectionColor(id, -1));
			assertEquals(-1, store.findSection(id).getColorRgb());
		}

		@Test
		@DisplayName("setSectionColor no-op returns false (no callback)")
		void setSectionColorNoop()
		{
			String id = api.createSection("Custom");
			api.setSectionColor(id, 0xE74C3C);
			callbackFireCount.set(0);
			assertFalse(api.setSectionColor(id, 0xE74C3C));
			assertEquals(0, callbackFireCount.get());
		}

		@Test
		@DisplayName("setSectionColor works on built-in sections (Mission 13)")
		void setSectionColorBuiltIn()
		{
			String id = store.getIncompleteSection().getId();
			assertTrue(api.setSectionColor(id, 0x3498DB));
			assertEquals(0x3498DB, store.findSection(id).getColorRgb());
		}

		@Test
		@DisplayName("setGoalColor works on all goal types")
		void setGoalColorAllTypes()
		{
			String customId = api.addCustomGoal("Custom", "");
			String skillId = api.addSkillGoal(Skill.ATTACK, 13_034_431);
			assertTrue(api.setGoalColor(customId, 0xE74C3C));
			assertTrue(api.setGoalColor(skillId, 0x2ECC71));
		}

		@Test
		@DisplayName("setTagColor recolors a non-skill tag entity (Mission 19)")
		void setTagColor()
		{
			// Mission 19: setTagColor delegates to recolorTag on the tag entity.
			// SKILLING category system tags are read-only, so use a BOSS tag for
			// this happy-path test.
			String goalId = api.addCustomGoal("Custom", "");
			com.goaltracker.model.Tag bossTag = store.findOrCreateSystemTag("Zulrah",
				com.goaltracker.model.TagCategory.BOSS);
			Goal g = store.getGoals().get(0);
			g.setTagIds(new java.util.ArrayList<>(List.of(bossTag.getId())));
			g.setDefaultTagIds(new java.util.ArrayList<>(List.of(bossTag.getId())));

			assertTrue(api.setTagColor(goalId, "Zulrah", 0xF1C40F));
			assertEquals(0xF1C40F, store.findTag(bossTag.getId()).getColorRgb());
		}

		@Test
		@DisplayName("setTagColor refuses skill-category system tags (Mission 19)")
		void setTagColorRejectsSkillSystemTag()
		{
			String goalId = api.addCustomGoal("Custom", "");
			com.goaltracker.model.Tag slayerTag = store.findOrCreateSystemTag("Slayer",
				com.goaltracker.model.TagCategory.SKILLING);
			Goal g = store.getGoals().get(0);
			g.setTagIds(new java.util.ArrayList<>(List.of(slayerTag.getId())));

			assertFalse(api.setTagColor(goalId, "Slayer", 0xF1C40F));
		}
	}

	// ====================================================================
	// Internal API: selection (Mission 15)
	// ====================================================================

	@Nested
	@DisplayName("selection")
	class SelectionTests
	{
		@Test
		@DisplayName("addToGoalSelection adds an id and fires callback")
		void addToGoalSelection()
		{
			String id = api.addCustomGoal("Test", "");
			callbackFireCount.set(0);

			assertTrue(api.addToGoalSelection(id));
			assertTrue(api.getSelectedGoalIds().contains(id));
			assertEquals(1, callbackFireCount.get());
		}

		@Test
		@DisplayName("addToGoalSelection no-op for already-selected")
		void addToGoalSelectionNoop()
		{
			String id = api.addCustomGoal("Test", "");
			api.addToGoalSelection(id);
			callbackFireCount.set(0);

			assertFalse(api.addToGoalSelection(id));
			assertEquals(0, callbackFireCount.get());
		}

		@Test
		@DisplayName("removeFromGoalSelection removes an id and fires callback")
		void removeFromGoalSelection()
		{
			String id = api.addCustomGoal("Test", "");
			api.addToGoalSelection(id);
			callbackFireCount.set(0);

			assertTrue(api.removeFromGoalSelection(id));
			assertFalse(api.getSelectedGoalIds().contains(id));
			assertEquals(1, callbackFireCount.get());
		}

		@Test
		@DisplayName("replaceGoalSelection swaps the entire selection")
		void replaceGoalSelection()
		{
			String a = api.addCustomGoal("A", "");
			String b = api.addCustomGoal("B", "");
			String c = api.addCustomGoal("C", "");

			api.addToGoalSelection(a);
			api.addToGoalSelection(b);
			assertTrue(api.replaceGoalSelection(Set.of(c)));

			assertEquals(Set.of(c), api.getSelectedGoalIds());
		}

		@Test
		@DisplayName("clearGoalSelection wipes everything and fires once")
		void clearGoalSelection()
		{
			String a = api.addCustomGoal("A", "");
			String b = api.addCustomGoal("B", "");
			api.addToGoalSelection(a);
			api.addToGoalSelection(b);
			callbackFireCount.set(0);

			assertTrue(api.clearGoalSelection());
			assertTrue(api.getSelectedGoalIds().isEmpty());
			assertEquals(1, callbackFireCount.get());
		}

		@Test
		@DisplayName("clearGoalSelection on empty set is a no-op")
		void clearGoalSelectionNoop()
		{
			assertFalse(api.clearGoalSelection());
		}

		@Test
		@DisplayName("selectAllInSection adds all goals in the section")
		void selectAllInSection()
		{
			String sectionId = api.createSection("Custom");
			String a = api.addCustomGoal("A", "");
			String b = api.addCustomGoal("B", "");
			api.moveGoalToSection(a, sectionId);
			api.moveGoalToSection(b, sectionId);

			int added = api.selectAllInSection(sectionId);
			assertEquals(2, added);
			assertEquals(Set.of(a, b), api.getSelectedGoalIds());
		}

		@Test
		@DisplayName("deselectAllInSection removes all section goals from selection")
		void deselectAllInSection()
		{
			String sectionId = api.createSection("Custom");
			String a = api.addCustomGoal("A", "");
			String b = api.addCustomGoal("B", "");
			api.moveGoalToSection(a, sectionId);
			api.moveGoalToSection(b, sectionId);
			api.selectAllInSection(sectionId);

			int removed = api.deselectAllInSection(sectionId);
			assertEquals(2, removed);
			assertTrue(api.getSelectedGoalIds().isEmpty());
		}
	}

	// ====================================================================
	// Internal API: recordGoalProgress (Mission 14 + Mission 17 fix)
	// ====================================================================

	@Nested
	@DisplayName("recordGoalProgress")
	class RecordGoalProgressTests
	{
		@Test
		@DisplayName("updates currentValue and returns true on change")
		void updatesCurrentValue()
		{
			String id = api.addSkillGoal(Skill.ATTACK, 13_034_431);
			assertTrue(api.recordGoalProgress(id, 5_000_000));
			assertEquals(5_000_000, store.getGoals().get(0).getCurrentValue());
		}

		@Test
		@DisplayName("no-op when newValue equals currentValue")
		void noopOnSameValue()
		{
			String id = api.addSkillGoal(Skill.ATTACK, 13_034_431);
			api.recordGoalProgress(id, 5_000_000);
			assertFalse(api.recordGoalProgress(id, 5_000_000));
		}

		@Test
		@DisplayName("flips to COMPLETE when target reached")
		void flipsToComplete()
		{
			String id = api.addSkillGoal(Skill.ATTACK, 13_034_431);
			api.recordGoalProgress(id, 13_034_431);
			Goal g = store.getGoals().get(0);
			assertTrue(g.isComplete());
			assertTrue(g.getCompletedAt() > 0);
		}

		@Test
		@DisplayName("symmetric un-complete when value drops below target")
		void unCompletesOnDrop()
		{
			Goal g = Goal.builder().type(GoalType.ITEM_GRIND).name("Coal")
				.itemId(1).targetValue(100).currentValue(0).build();
			store.addGoal(g);

			api.recordGoalProgress(g.getId(), 100);
			assertTrue(g.isComplete());

			api.recordGoalProgress(g.getId(), 50);
			assertFalse(g.isComplete());
			assertEquals(0, g.getCompletedAt());
			assertEquals(GoalStatus.ACTIVE, g.getStatus());
		}

		@Test
		@DisplayName("does NOT save or fire onGoalsChanged (silent mutation)")
		void silentMutation()
		{
			String id = api.addSkillGoal(Skill.ATTACK, 13_034_431);
			callbackFireCount.set(0);
			api.recordGoalProgress(id, 5_000_000);
			assertEquals(0, callbackFireCount.get(),
				"recordGoalProgress must not fire onGoalsChanged — trackers batch via the GameTick handler");
		}
	}

	// ====================================================================
	// Mission 19: changeTarget regenerates display strings
	// ====================================================================

	@Nested
	@DisplayName("changeTarget display-string regeneration")
	class ChangeTargetDisplayStringTests
	{
		@Test
		@DisplayName("regenerates SKILL goal name from new XP target")
		void regeneratesSkillName()
		{
			String id = api.addSkillGoal(Skill.PRAYER, 13_034_431);
			assertEquals("Prayer \u2192 Level 99", store.getGoals().get(0).getName());

			api.changeTarget(id, 9_684_577); // L96
			assertEquals("Prayer \u2192 Level 96", store.getGoals().get(0).getName());
		}

		@Test
		@DisplayName("regenerates ITEM_GRIND description from new quantity")
		void regeneratesItemDescription()
		{
			Goal g = Goal.builder().type(GoalType.ITEM_GRIND).name("Cannonballs")
				.itemId(2).targetValue(100).description("100 total").build();
			store.addGoal(g);

			api.changeTarget(g.getId(), 5000);
			assertEquals("5K total", g.getDescription());
		}

		@Test
		@DisplayName("rejects target change for non-SKILL/non-ITEM_GRIND types")
		void rejectsNonEditableTypes()
		{
			Goal quest = Goal.builder().type(GoalType.QUEST).name("Quest")
				.questName("DRAGON_SLAYER_II").targetValue(1).build();
			store.addGoal(quest);
			assertFalse(api.changeTarget(quest.getId(), 2));
		}
	}

	// ====================================================================
	// Mission 19: addTagWithCategory (legacy panel direct-mutation removal)
	// ====================================================================

	@Nested
	@DisplayName("addTagWithCategory")
	class AddTagWithCategoryTests
	{
		@Test
		@DisplayName("adds a tag with the requested category and fires callback")
		void addsTagWithCategory()
		{
			String id = api.addCustomGoal("Custom", "");
			callbackFireCount.set(0);

			assertTrue(api.addTagWithCategory(id, "Zulrah", "BOSS"));

			Goal g = store.getGoals().get(0);
			assertEquals(1, g.getTagIds().size());
			com.goaltracker.model.Tag tag = store.findTag(g.getTagIds().get(0));
			assertNotNull(tag);
			assertEquals("Zulrah", tag.getLabel());
			assertEquals(TagCategory.BOSS, tag.getCategory());
			assertEquals(1, callbackFireCount.get());
		}

		@Test
		@DisplayName("rejects unknown category names")
		void rejectsUnknownCategory()
		{
			String id = api.addCustomGoal("Custom", "");
			assertFalse(api.addTagWithCategory(id, "Test", "NONEXISTENT_CATEGORY"));
		}

		@Test
		@DisplayName("rejects null/blank label")
		void rejectsBlankLabel()
		{
			String id = api.addCustomGoal("Custom", "");
			assertFalse(api.addTagWithCategory(id, null, "BOSS"));
			assertFalse(api.addTagWithCategory(id, "", "BOSS"));
			assertFalse(api.addTagWithCategory(id, "   ", "BOSS"));
		}

		@Test
		@DisplayName("rejects unknown goal id")
		void rejectsUnknownGoalId()
		{
			assertFalse(api.addTagWithCategory("nonexistent", "Test", "BOSS"));
		}

		@Test
		@DisplayName("trims whitespace from the label")
		void trimsWhitespace()
		{
			String id = api.addCustomGoal("Custom", "");
			api.addTagWithCategory(id, "  Vorkath  ", "BOSS");
			Goal g = store.getGoals().get(0);
			com.goaltracker.model.Tag tag = store.findTag(g.getTagIds().get(0));
			assertEquals("Vorkath", tag.getLabel());
		}
	}

	// ====================================================================
	// Mission 19: Tag entity CRUD
	// ====================================================================

	@Nested
	@DisplayName("tag entity CRUD")
	class TagEntityTests
	{
		@Test
		@DisplayName("createUserTag returns the new tag id")
		void createUserTag()
		{
			String id = api.createUserTag("Pets", "OTHER");
			assertNotNull(id);
			assertEquals("Pets", store.findTag(id).getLabel());
			assertFalse(store.findTag(id).isSystem());
		}

		@Test
		@DisplayName("createUserTag is idempotent on case-insensitive (label, category)")
		void createUserTagIdempotent()
		{
			String first = api.createUserTag("Pets", "OTHER");
			String second = api.createUserTag("pets", "OTHER");
			assertEquals(first, second);
		}

		@Test
		@DisplayName("renameTag updates the label and propagates via the entity")
		void renameTag()
		{
			String id = api.createUserTag("Pets", "OTHER");
			assertTrue(api.renameTag(id, "All Pets"));
			assertEquals("All Pets", store.findTag(id).getLabel());
		}

		@Test
		@DisplayName("renameTag rejects system tags")
		void renameTagRejectsSystem()
		{
			com.goaltracker.model.Tag system = store.findOrCreateSystemTag("Slayer",
				com.goaltracker.model.TagCategory.SKILLING);
			assertFalse(api.renameTag(system.getId(), "Slayer Task"));
		}

		@Test
		@DisplayName("recolorTag works on non-skill system tags")
		void recolorNonSkillSystemTag()
		{
			com.goaltracker.model.Tag bossTag = store.findOrCreateSystemTag("Zulrah",
				com.goaltracker.model.TagCategory.BOSS);
			assertTrue(api.recolorTag(bossTag.getId(), 0xE74C3C));
			assertEquals(0xE74C3C, store.findTag(bossTag.getId()).getColorRgb());
		}

		@Test
		@DisplayName("recolorTag rejects system tags in SKILLING category")
		void recolorSkillSystemTagRejected()
		{
			com.goaltracker.model.Tag slayer = store.findOrCreateSystemTag("Slayer",
				com.goaltracker.model.TagCategory.SKILLING);
			assertFalse(api.recolorTag(slayer.getId(), 0xE74C3C));
		}

		@Test
		@DisplayName("deleteTag removes the entity and cascades to goal references")
		void deleteTagCascades()
		{
			String tagId = api.createUserTag("ToDelete", "OTHER");
			String goalId = api.addCustomGoal("Custom", "");
			api.addTagWithCategory(goalId, "ToDelete", "OTHER");
			Goal g = store.getGoals().get(0);
			assertEquals(1, g.getTagIds().size());

			assertTrue(api.deleteTag(tagId));
			assertNull(store.findTag(tagId));
			assertEquals(0, g.getTagIds().size());
		}

		@Test
		@DisplayName("deleteTag rejects system tags")
		void deleteTagRejectsSystem()
		{
			com.goaltracker.model.Tag system = store.findOrCreateSystemTag("Slayer",
				com.goaltracker.model.TagCategory.SKILLING);
			assertFalse(api.deleteTag(system.getId()));
			assertNotNull(store.findTag(system.getId()));
		}

		@Test
		@DisplayName("queryAllTags returns every tag in the store")
		void queryAllTags()
		{
			api.createUserTag("Tag1", "OTHER");
			api.createUserTag("Tag2", "BOSS");
			store.findOrCreateSystemTag("Slayer", com.goaltracker.model.TagCategory.SKILLING);
			assertEquals(3, api.queryAllTags().size());
		}
	}
}
