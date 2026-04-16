package com.goalplanner.data;

import com.goalplanner.api.GoalPlannerApiImpl;
import com.goalplanner.data.BossKillData.Alternative;
import com.goalplanner.data.BossKillData.BossPrereqs;
import com.goalplanner.data.BossKillData.BossReq;
import com.goalplanner.data.BossKillData.SkillReq;
import com.goalplanner.model.Goal;
import com.goalplanner.model.GoalType;
import com.goalplanner.persistence.GoalStore;
import com.goalplanner.service.GoalReorderingService;
import com.goalplanner.testsupport.InMemoryConfigManager;
import com.goalplanner.testsupport.MockClientFactory;
import com.goalplanner.testsupport.MockGameState;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.Experience;
import net.runelite.api.Skill;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * End-to-end test that {@code addBossGoal} seeds
 * {@link BossPrereqs#alternatives} as OR-prereqs on the boss goal,
 * mirroring the diary-unlock alternative path.
 *
 * <p>No production boss currently carries alternatives, so this test
 * swaps in a synthetic {@link BossPrereqs} for an existing boss via the
 * package-private {@link BossKillData#swapPrereqsForTest} hook and
 * restores the original in {@link #restoreOriginal} after each test.
 */
class BossAlternativeSeedingTest
{
	/** Target boss name used for swapping — picks a simple entry. */
	private static final String BOSS = "Zulrah";

	private GoalStore store;
	private GoalPlannerApiImpl api;
	private BossPrereqs originalPrereqs;

	@BeforeEach
	void setUp()
	{
		ConfigManager configManager = InMemoryConfigManager.create();
		store = new GoalStore(configManager);
		store.load();

		MockGameState freshState = new MockGameState();
		Client client = MockClientFactory.createClient(freshState);

		ItemManager itemManager = mock(ItemManager.class);
		WikiCaRepository wikiCaRepository = mock(WikiCaRepository.class);
		GoalReorderingService reorderingService = new GoalReorderingService(store);
		api = new GoalPlannerApiImpl(store, reorderingService, itemManager,
			wikiCaRepository, client);
	}

	@AfterEach
	void restoreOriginal()
	{
		// Always restore so cross-test state doesn't leak.
		BossKillData.swapPrereqsForTest(BOSS, originalPrereqs);
	}

	@Test
	@DisplayName("skill alternatives become OR-prereqs on the boss goal")
	void skillAlternativesBecomeOrPrereqs()
	{
		// Synthetic: BOSS gated by (Attack 99) OR (Strength 99).
		BossPrereqs synthetic = new BossPrereqs(
			List.of(),                // no AND skills
			List.of(),                // no unlocks
			List.of(),                // no AND quests
			List.of(),                // no AND bossKills
			List.of(),                // no itemReqs
			List.of(),                // no accountReqs
			List.of(
				new Alternative("99 Attack",
					List.of(new SkillReq(Skill.ATTACK, 99)),
					List.of()),
				new Alternative("99 Strength",
					List.of(new SkillReq(Skill.STRENGTH, 99)),
					List.of())));
		originalPrereqs = BossKillData.swapPrereqsForTest(BOSS, synthetic);

		String bossId = api.addBossGoal(BOSS, 10);
		assertNotNull(bossId);

		Goal boss = findById(bossId);
		assertNotNull(boss);
		List<String> orIds = boss.getOrRequiredGoalIds();
		assertNotNull(orIds, "boss should have OR-prereqs list");
		assertEquals(2, orIds.size(),
			"both skill alternatives should be linked as OR-prereqs");

		// The OR-children must be SKILL goals at 99.
		for (String id : orIds)
		{
			Goal g = findById(id);
			assertNotNull(g);
			assertEquals(GoalType.SKILL, g.getType());
			assertEquals(Experience.getXpForLevel(99), g.getTargetValue());
		}

		// AND-list should be empty for this synthetic entry.
		assertTrue(boss.getRequiredGoalIds() == null
			|| boss.getRequiredGoalIds().isEmpty(),
			"AND-prereqs should be empty for an alternatives-only entry");
	}

	@Test
	@DisplayName("mixed AND + OR: AND-prereqs stay on requiredGoalIds, alts on orRequiredGoalIds")
	void mixedAndOrSeparation()
	{
		// Synthetic: BOSS gated by (70 Ranged AND (99 Magic OR 99 Attack)).
		BossPrereqs synthetic = new BossPrereqs(
			List.of(new SkillReq(Skill.RANGED, 70)),    // AND
			List.of(), List.of(), List.of(), List.of(), List.of(),
			List.of(
				new Alternative("99 Magic",
					List.of(new SkillReq(Skill.MAGIC, 99)),
					List.of()),
				new Alternative("99 Attack",
					List.of(new SkillReq(Skill.ATTACK, 99)),
					List.of())));
		originalPrereqs = BossKillData.swapPrereqsForTest(BOSS, synthetic);

		String bossId = api.addBossGoal(BOSS, 10);
		Goal boss = findById(bossId);
		assertNotNull(boss);

		assertEquals(1, boss.getRequiredGoalIds().size(),
			"AND-prereqs should only contain the 70 Ranged goal");
		assertEquals(2, boss.getOrRequiredGoalIds().size(),
			"OR-prereqs should contain both alternatives");

		// Sanity: 70 Ranged on AND, 99 Magic / 99 Attack on OR
		Goal andChild = findById(boss.getRequiredGoalIds().get(0));
		assertEquals("RANGED", andChild.getSkillName());
		assertEquals(Experience.getXpForLevel(70), andChild.getTargetValue());
	}

	@Test
	@DisplayName("boss-kill alternatives become OR-prereqs (recursive addBossGoal)")
	void bossKillAlternativesBecomeOrPrereqs()
	{
		// Synthetic: BOSS unlocked by a Vorkath or Vardorvis kill.
		BossPrereqs synthetic = new BossPrereqs(
			List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
			List.of(
				new Alternative("Vorkath kill",
					List.of(), List.of(),
					List.of(new BossReq("Vorkath", 1))),
				new Alternative("Vardorvis kill",
					List.of(), List.of(),
					List.of(new BossReq("Vardorvis", 1)))));
		originalPrereqs = BossKillData.swapPrereqsForTest(BOSS, synthetic);

		String bossId = api.addBossGoal(BOSS, 10);
		Goal boss = findById(bossId);
		assertNotNull(boss);

		assertEquals(2, boss.getOrRequiredGoalIds().size());
		long bossChildren = boss.getOrRequiredGoalIds().stream()
			.map(this::findById)
			.filter(g -> g != null && g.getType() == GoalType.BOSS)
			.count();
		assertEquals(2, bossChildren,
			"both boss-kill alternatives should be linked as OR-prereq BOSS goals");
	}

	private Goal findById(String id)
	{
		for (Goal g : store.getGoals())
		{
			if (g.getId().equals(id)) return g;
		}
		return null;
	}
}
