package com.goalplanner.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The per-type "same objective" predicate that defines a duplicate. Mirrors the
 * conditions the creation-layer duplicate guards used to inline.
 */
class GoalIdentityTest
{
	private static Goal skill(String name, int target)
	{
		return Goal.builder().type(GoalType.SKILL).skillName(name).targetValue(target).build();
	}

	@Test
	@DisplayName("different types are never the same identity")
	void differentTypesNeverMatch()
	{
		Goal s = skill("RANGED", 100);
		Goal b = Goal.builder().type(GoalType.BOSS).bossName("RANGED").build();
		assertFalse(GoalIdentity.sameIdentity(s, b));
		assertFalse(GoalIdentity.sameIdentity(null, s));
		assertFalse(GoalIdentity.sameIdentity(s, null));
	}

	@Test
	@DisplayName("SKILL: same skill name + target XP; differing skill or target do not match")
	void skillIdentity()
	{
		assertTrue(GoalIdentity.sameIdentity(skill("RANGED", 100), skill("RANGED", 100)));
		assertFalse(GoalIdentity.sameIdentity(skill("RANGED", 100), skill("RANGED", 200)));
		assertFalse(GoalIdentity.sameIdentity(skill("RANGED", 100), skill("ATTACK", 100)));
	}

	@Test
	@DisplayName("QUEST matches by exact name; BOSS by exact name")
	void questAndBossIdentity()
	{
		Goal q1 = Goal.builder().type(GoalType.QUEST).questName("SONG_OF_THE_ELVES").build();
		Goal q2 = Goal.builder().type(GoalType.QUEST).questName("SONG_OF_THE_ELVES").build();
		Goal q3 = Goal.builder().type(GoalType.QUEST).questName("DRAGON_SLAYER_II").build();
		assertTrue(GoalIdentity.sameIdentity(q1, q2));
		assertFalse(GoalIdentity.sameIdentity(q1, q3));

		Goal b1 = Goal.builder().type(GoalType.BOSS).bossName("Zulrah").build();
		Goal b2 = Goal.builder().type(GoalType.BOSS).bossName("Zulrah").build();
		Goal b3 = Goal.builder().type(GoalType.BOSS).bossName("Vorkath").build();
		assertTrue(GoalIdentity.sameIdentity(b1, b2));
		assertFalse(GoalIdentity.sameIdentity(b1, b3));
	}

	@Test
	@DisplayName("DIARY matches area name + tier description case-insensitively")
	void diaryIdentity()
	{
		Goal d1 = Goal.builder().type(GoalType.DIARY).name("Varrock").description("Hard Achievement Diary").build();
		Goal d2 = Goal.builder().type(GoalType.DIARY).name("varrock").description("hard achievement diary").build();
		Goal d3 = Goal.builder().type(GoalType.DIARY).name("Varrock").description("Elite Achievement Diary").build();
		assertTrue(GoalIdentity.sameIdentity(d1, d2));
		assertFalse(GoalIdentity.sameIdentity(d1, d3));
	}

	@Test
	@DisplayName("COMBAT_ACHIEVEMENT matches by task id, or by name when ids differ")
	void caIdentity()
	{
		Goal a = Goal.builder().type(GoalType.COMBAT_ACHIEVEMENT).caTaskId(5).name("Kill Zuk").build();
		Goal sameId = Goal.builder().type(GoalType.COMBAT_ACHIEVEMENT).caTaskId(5).name("different label").build();
		Goal sameName = Goal.builder().type(GoalType.COMBAT_ACHIEVEMENT).caTaskId(6).name("kill zuk").build();
		Goal neither = Goal.builder().type(GoalType.COMBAT_ACHIEVEMENT).caTaskId(6).name("Kill Jad").build();
		assertTrue(GoalIdentity.sameIdentity(a, sameId));
		assertTrue(GoalIdentity.sameIdentity(a, sameName));
		assertFalse(GoalIdentity.sameIdentity(a, neither));
	}

	@Test
	@DisplayName("ITEM_GRIND by item id; ACCOUNT by metric+target; CUSTOM by name (case-insensitive)")
	void itemAccountCustomIdentity()
	{
		Goal i1 = Goal.builder().type(GoalType.ITEM_GRIND).itemId(995).build();
		Goal i2 = Goal.builder().type(GoalType.ITEM_GRIND).itemId(995).targetValue(50).build();
		Goal i3 = Goal.builder().type(GoalType.ITEM_GRIND).itemId(1234).build();
		assertTrue(GoalIdentity.sameIdentity(i1, i2)); // qty ignored
		assertFalse(GoalIdentity.sameIdentity(i1, i3));

		Goal a1 = Goal.builder().type(GoalType.ACCOUNT).accountMetric("QUEST_POINTS").targetValue(100).build();
		Goal a2 = Goal.builder().type(GoalType.ACCOUNT).accountMetric("QUEST_POINTS").targetValue(100).build();
		Goal a3 = Goal.builder().type(GoalType.ACCOUNT).accountMetric("QUEST_POINTS").targetValue(200).build();
		assertTrue(GoalIdentity.sameIdentity(a1, a2));
		assertFalse(GoalIdentity.sameIdentity(a1, a3));

		Goal c1 = Goal.builder().type(GoalType.CUSTOM).name("Buy a bond").build();
		Goal c2 = Goal.builder().type(GoalType.CUSTOM).name("BUY A BOND").build();
		Goal c3 = Goal.builder().type(GoalType.CUSTOM).name("Sell a bond").build();
		assertTrue(GoalIdentity.sameIdentity(c1, c2));
		assertFalse(GoalIdentity.sameIdentity(c1, c3));
	}
}
