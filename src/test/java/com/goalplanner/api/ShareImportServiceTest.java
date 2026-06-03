package com.goalplanner.api;

import com.goalplanner.data.WikiCaRepository;
import com.goalplanner.model.Goal;
import com.goalplanner.model.GoalType;
import com.goalplanner.model.Section;
import com.goalplanner.model.Tag;
import com.goalplanner.model.TagCategory;
import com.goalplanner.persistence.GoalStore;
import com.goalplanner.service.GoalReorderingService;
import com.goalplanner.share.GoalShareDto;
import com.goalplanner.share.ShareBundle;
import com.goalplanner.share.ShareCodec;
import com.goalplanner.share.ShareMapper;
import com.goalplanner.share.TagShareDto;
import com.goalplanner.testsupport.InMemoryConfigManager;
import com.google.gson.Gson;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Import side: a decoded {@link ShareBundle} → real {@link GoalStore}. Run
 * against a real store (not a mock) per the stateful-store testing rule.
 */
class ShareImportServiceTest
{
	private GoalStore store;
	private GoalPlannerApiImpl api;

	@BeforeEach
	void setUp()
	{
		ConfigManager configManager = InMemoryConfigManager.create();
		store = new GoalStore(configManager, new Gson());
		store.load();

		ItemManager itemManager = mock(ItemManager.class);
		WikiCaRepository wikiCaRepository = mock(WikiCaRepository.class);
		GoalReorderingService reorderingService = new GoalReorderingService(store);
		api = new GoalPlannerApiImpl(store, reorderingService, itemManager, wikiCaRepository);
		api.setOnGoalsChanged(() -> {});
	}

	private static ShareBundle sampleBundle()
	{
		TagShareDto tag = new TagShareDto();
		tag.setLabel("Inferno");
		tag.setCategory("OTHER");
		tag.setColorRgb(0xAA3322);

		GoalShareDto ranged = new GoalShareDto();
		ranged.setRef(0);
		ranged.setType("SKILL");
		ranged.setName("Ranged - Level 75");
		ranged.setSkillName("RANGED");
		ranged.setTargetValue(1_210_421);
		ranged.setTags(Collections.singletonList(tag));

		GoalShareDto zuk = new GoalShareDto();
		zuk.setRef(1);
		zuk.setType("BOSS");
		zuk.setName("TzKal-Zuk");
		zuk.setBossName("TzKal-Zuk");
		zuk.setTargetValue(1);
		zuk.setRequires(Collections.singletonList(0));

		ShareBundle bundle = new ShareBundle();
		bundle.setKind(ShareBundle.Kind.SECTION);
		bundle.setSectionName("Inferno Prep");
		bundle.setSharedBy("Andrew");
		bundle.setGoals(Arrays.asList(ranged, zuk));
		return bundle;
	}

	private Goal goalNamed(String name)
	{
		return store.getGoals().stream()
			.filter(g -> name.equals(g.getName())).findFirst().orElseThrow();
	}

	@Test
	void importsBundleIntoNewSectionWithGoalsTagsAndRelations()
	{
		String sectionId = api.importShareBundle(sampleBundle());

		assertNotNull(sectionId);
		Section section = store.getSections().stream()
			.filter(s -> sectionId.equals(s.getId())).findFirst().orElse(null);
		assertNotNull(section);
		assertEquals("Inferno Prep (from Andrew)", section.getName());
		// Imports land in their own user section, forced KEEP-INLINE (overriding
		// the global auto-archive default) so the recipient sees it as a checklist
		// against their account.
		assertFalse(section.isBuiltIn());
		assertEquals(Boolean.FALSE, section.getAutoArchiveOverride());

		Goal ranged = goalNamed("Ranged - Level 75");
		Goal zuk = goalNamed("TzKal-Zuk");

		assertEquals(GoalType.SKILL, ranged.getType());
		assertEquals("RANGED", ranged.getSkillName());
		assertEquals(1_210_421, ranged.getTargetValue());
		assertEquals(sectionId, ranged.getSectionId());
		// Fresh, unstarted goal — recipient's progress, not the sharer's.
		assertEquals(0, ranged.getCurrentValue());
		assertEquals(0L, ranged.getCompletedAt());

		// Tag find-or-created and attached.
		assertEquals(1, ranged.getTagIds().size());
		Tag tag = store.findTag(ranged.getTagIds().get(0));
		assertNotNull(tag);
		assertEquals("Inferno", tag.getLabel());

		// Relation remapped to the fresh ids.
		assertTrue(zuk.getRequiredGoalIds().contains(ranged.getId()));
	}

	@Test
	void importAppliesSharedSectionColor()
	{
		GoalShareDto g = new GoalShareDto();
		g.setRef(0);
		g.setType("CUSTOM");
		g.setName("X");
		ShareBundle bundle = new ShareBundle();
		bundle.setKind(ShareBundle.Kind.SECTION);
		bundle.setSectionName("Themed");
		bundle.setSectionColorRgb(0x3366CC);
		bundle.setGoals(java.util.Collections.singletonList(g));

		String sectionId = api.importShareBundle(bundle);
		Section section = store.findSection(sectionId);
		assertNotNull(section);
		assertEquals(0x3366CC, section.getColorRgb()); // shared colour carried over
	}

	@Test
	void undoReversesTheEntireImportAndRedoReplaysIt()
	{
		int goalsBefore = store.getGoals().size();
		int sectionsBefore = store.getSections().size();

		api.importShareBundle(sampleBundle());
		assertEquals(2, store.getGoals().size());

		assertTrue(api.undo());
		assertEquals(goalsBefore, store.getGoals().size(), "undo removes the imported goals");
		assertEquals(sectionsBefore, store.getSections().size(), "undo removes the imported section");

		assertTrue(api.redo());
		assertEquals(2, store.getGoals().size(), "redo re-imports");
	}

	@Test
	void skipsGoalsWithAnUnknownType()
	{
		GoalShareDto bad = new GoalShareDto();
		bad.setRef(0);
		bad.setType("TOTALLY_NOT_A_TYPE");
		bad.setName("hostile");

		GoalShareDto good = new GoalShareDto();
		good.setRef(1);
		good.setType("SKILL");
		good.setName("Cooking - Level 50");
		good.setSkillName("COOKING");
		good.setTargetValue(101_333);

		ShareBundle bundle = new ShareBundle();
		bundle.setKind(ShareBundle.Kind.GOALS);
		bundle.setGoals(Arrays.asList(bad, good));

		api.importShareBundle(bundle);

		assertEquals(1, store.getGoals().size());
		assertEquals("Cooking - Level 50", store.getGoals().get(0).getName());
	}

	@Test
	void roundTripsFromRealGoalsViaMapperAndCodec()
	{
		Tag inferno = Tag.builder()
			.id("t1").label("Inferno").category(TagCategory.OTHER).colorRgb(0xAA3322).build();
		Map<String, Tag> tags = Collections.singletonMap("t1", inferno);

		Goal ranged = Goal.builder().id("a").type(GoalType.SKILL).name("Ranged")
			.skillName("RANGED").targetValue(1_210_421)
			.tagIds(Collections.singletonList("t1")).build();
		Goal zuk = Goal.builder().id("b").type(GoalType.BOSS).name("TzKal-Zuk")
			.bossName("TzKal-Zuk").targetValue(1)
			.requiredGoalIds(Collections.singletonList("a")).build();

		ShareBundle bundle = ShareMapper.toBundle(
			ShareBundle.Kind.SECTION, "Inferno Prep", -1,
			Arrays.asList(ranged, zuk), tags::get, "Andrew");

		ShareCodec codec = new ShareCodec(new Gson());
		api.importShareBundle(codec.decode(codec.encode(bundle)));

		assertEquals(2, store.getGoals().size());
		Goal importedRanged = goalNamed("Ranged");
		Goal importedZuk = goalNamed("TzKal-Zuk");
		assertTrue(importedZuk.getRequiredGoalIds().contains(importedRanged.getId()));
	}

	@Test
	void ignoresNullOrEmptyBundle()
	{
		assertNull(api.importShareBundle(null));
		ShareBundle empty = new ShareBundle();
		empty.setKind(ShareBundle.Kind.GOALS);
		assertNull(api.importShareBundle(empty));
		assertEquals(0, store.getGoals().size());
	}

	@Test
	void looseGoalsImportIntoANewUserSectionStartingIncomplete()
	{
		GoalShareDto g = new GoalShareDto();
		g.setRef(0);
		g.setType("SKILL");
		g.setName("Cooking - Level 50");
		g.setSkillName("COOKING");
		g.setTargetValue(101_333);
		ShareBundle bundle = new ShareBundle();
		bundle.setKind(ShareBundle.Kind.GOALS);
		bundle.setGoals(java.util.Collections.singletonList(g));

		String landedSectionId = api.importShareBundle(bundle);

		// A loose selection lands in its own NEW user section (not Incomplete),
		// so the recipient sees it as a checklist against their account.
		assertNotEquals(store.getIncompleteSection().getId(), landedSectionId);
		Section landed = store.findSection(landedSectionId);
		assertNotNull(landed);
		assertFalse(landed.isBuiltIn());

		Goal imported = goalNamed("Cooking - Level 50");
		assertEquals(landedSectionId, imported.getSectionId());
		assertFalse(imported.isComplete());
		assertEquals(0, imported.getCurrentValue());
	}

	@Test
	void longSectionNameStillImports()
	{
		// Regression: "<name> (from <sharer>)" can exceed the 40-char section cap,
		// which createUserSection rejects (and CommandHistory swallows) — the import
		// must fit the name instead of silently returning null.
		GoalShareDto g = new GoalShareDto();
		g.setRef(0);
		g.setType("SKILL");
		g.setName("Ranged");
		g.setSkillName("RANGED");
		g.setTargetValue(1_210_421);

		ShareBundle bundle = new ShareBundle();
		bundle.setKind(ShareBundle.Kind.SECTION);
		bundle.setSectionName("Inferno Prep (Postie demo)");   // 26 chars
		bundle.setSharedBy("Postie demo");                      // "(from …)" → 45 total, over 40
		bundle.setGoals(java.util.Collections.singletonList(g));

		String sectionId = api.importShareBundle(bundle);

		assertNotNull(sectionId, "long section name must not silently fail the import");
		Section section = store.getSections().stream()
			.filter(s -> sectionId.equals(s.getId())).findFirst().orElseThrow();
		assertTrue(section.getName().length() <= 40);
		assertEquals(1, store.getGoals().size());
	}
}
