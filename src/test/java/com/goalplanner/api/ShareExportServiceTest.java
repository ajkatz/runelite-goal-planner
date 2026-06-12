package com.goalplanner.api;

import com.goalplanner.data.WikiCaRepository;
import com.goalplanner.model.Goal;
import com.goalplanner.model.GoalType;
import com.goalplanner.model.Section;
import com.goalplanner.model.Tag;
import com.goalplanner.model.TagCategory;
import com.goalplanner.persistence.GoalStore;
import com.goalplanner.service.GoalReorderingService;
import com.goalplanner.share.ShareBundle;
import com.goalplanner.share.ShareCodec;
import com.goalplanner.testsupport.InMemoryConfigManager;
import com.google.gson.Gson;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Export side: store goals → {@link ShareBundle}, plus a full cross-store
 * round-trip (export → encode → decode → import into a second store) that
 * exercises the entire share pipeline end to end.
 */
class ShareExportServiceTest
{
	/** A fresh, isolated store + api. */
	private static final class Env
	{
		final GoalStore store;
		final GoalPlannerApiImpl api;

		Env()
		{
			ConfigManager cm = InMemoryConfigManager.create();
			store = new GoalStore(cm, new Gson());
			store.load();
			ItemManager itemManager = mock(ItemManager.class);
			WikiCaRepository wikiCaRepository = mock(WikiCaRepository.class);
			GoalReorderingService reordering = new GoalReorderingService(store);
			api = new GoalPlannerApiImpl(store, reordering, itemManager, wikiCaRepository);
			api.setOnGoalsChanged(() -> {});
		}
	}

	/** Seed a section "Inferno Prep" with a tagged Ranged goal that Zuk requires. */
	private static String seedInfernoSection(Env env)
	{
		Section section = env.store.createUserSection("Inferno Prep");
		Tag tag = env.store.createUserTag("Inferno", TagCategory.OTHER);

		Goal ranged = Goal.builder().type(GoalType.SKILL).name("Ranged - Level 75")
			.skillName("RANGED").targetValue(1_210_421).sectionId(section.getId())
			.build();
		ranged.getTagIds().add(tag.getId());
		Goal zuk = Goal.builder().type(GoalType.BOSS).name("TzKal-Zuk")
			.bossName("TzKal-Zuk").targetValue(1).sectionId(section.getId())
			.build();
		env.store.addGoal(ranged);
		env.store.addGoal(zuk);
		env.store.addRequirement(zuk.getId(), ranged.getId());
		return section.getId();
	}

	@Test
	void exportSectionReflectsItsGoalsTagsAndRelations()
	{
		Env env = new Env();
		String sectionId = seedInfernoSection(env);

		ShareBundle bundle = env.api.exportSectionBundle(sectionId, "Andrew");

		assertNotNull(bundle);
		assertEquals(ShareBundle.Kind.SECTION, bundle.getKind());
		assertEquals("Inferno Prep", bundle.getSectionName());
		assertEquals("Andrew", bundle.getSharedBy());
		assertEquals(2, bundle.getGoals().size());
		// The Ranged goal carries its resolved tag.
		assertTrue(bundle.getGoals().stream()
			.anyMatch(g -> "Ranged - Level 75".equals(g.getName())
				&& g.getTags().size() == 1
				&& "Inferno".equals(g.getTags().get(0).getLabel())));
		// Zuk's requirement is encoded as an in-bundle ref.
		assertTrue(bundle.getGoals().stream()
			.anyMatch(g -> "TzKal-Zuk".equals(g.getName()) && g.getRequires().size() == 1));
	}

	@Test
	void fullRoundTripExportEncodeDecodeImportIntoASecondStore()
	{
		Env sender = new Env();
		String sectionId = seedInfernoSection(sender);

		ShareCodec codec = new ShareCodec(new Gson());
		String code = codec.encode(sender.api.exportSectionBundle(sectionId, "Andrew"));

		Env receiver = new Env();
		int before = receiver.store.getGoals().size();
		receiver.api.importShareBundle(codec.decode(code));

		assertEquals(before + 2, receiver.store.getGoals().size());
		Goal importedRanged = receiver.store.getGoals().stream()
			.filter(g -> "Ranged - Level 75".equals(g.getName())).findFirst().orElseThrow();
		Goal importedZuk = receiver.store.getGoals().stream()
			.filter(g -> "TzKal-Zuk".equals(g.getName())).findFirst().orElseThrow();
		// Relation survived the round-trip, rewired to the receiver's fresh ids.
		assertTrue(importedZuk.getRequiredGoalIds().contains(importedRanged.getId()));
		// Tag came across.
		assertEquals(1, importedRanged.getTagIds().size());
	}

	@Test
	void exportGoalsBundleCoversExplicitIdsOnly()
	{
		Env env = new Env();
		String sectionId = seedInfernoSection(env);
		Goal zuk = env.store.getGoals().stream()
			.filter(g -> "TzKal-Zuk".equals(g.getName())).findFirst().orElseThrow();

		ShareBundle bundle = env.api.exportGoalsBundle(
			java.util.Collections.singletonList(zuk.getId()), "Andrew");

		assertNotNull(bundle);
		assertEquals(ShareBundle.Kind.GOALS, bundle.getKind());
		assertNull(bundle.getSectionName());
		assertEquals(1, bundle.getGoals().size());
		assertEquals("TzKal-Zuk", bundle.getGoals().get(0).getName());
		// The dropped Ranged goal means Zuk's requirement edge is dropped too.
		assertTrue(bundle.getGoals().get(0).getRequires().isEmpty());
	}

	@Test
	void exportSectionReturnsNullForUnknownSection()
	{
		Env env = new Env();
		assertNull(env.api.exportSectionBundle("no-such-section", "Andrew"));
	}

	@Test
	void exportAllSectionsCountsDroppedCrossSectionEdges()
	{
		Env env = new Env();
		Section skills = env.store.createUserSection("Skills");
		Section quests = env.store.createUserSection("Quests");

		Goal agility = Goal.builder().type(GoalType.SKILL).name("Agility - Level 70")
			.skillName("AGILITY").targetValue(737_627).sectionId(skills.getId()).build();
		Goal sote = Goal.builder().type(GoalType.QUEST).name("Song of the Elves")
			.questName("SONG_OF_THE_ELVES").sectionId(quests.getId()).build();
		env.store.addGoal(agility);
		env.store.addGoal(sote);
		// Cross-section dependency: the wire format can't carry it.
		env.store.addRequirement(sote.getId(), agility.getId());

		ShareBundle bundle = env.api.exportAllSectionsBundle("Andrew");

		assertNotNull(bundle);
		assertEquals(2, bundle.totalGoalCount());
		assertEquals(1, bundle.getDroppedCrossSectionEdges());
		// The edge really is absent from the wire payload.
		assertTrue(bundle.effectiveSections().stream()
			.flatMap(s -> s.getGoals().stream())
			.allMatch(g -> g.getRequires().isEmpty() && g.getOrRequires().isEmpty()));
	}

	@Test
	void exportAllSectionsReportsZeroDroppedEdgesWhenRelationsAreSectionLocal()
	{
		Env env = new Env();
		seedInfernoSection(env); // Zuk requires Ranged, both in one section

		ShareBundle bundle = env.api.exportAllSectionsBundle("Andrew");

		assertNotNull(bundle);
		assertEquals(0, bundle.getDroppedCrossSectionEdges());
		assertEquals(2, bundle.totalGoalCount());
	}
}
