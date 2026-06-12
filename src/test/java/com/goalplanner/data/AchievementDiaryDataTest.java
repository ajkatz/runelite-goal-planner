package com.goalplanner.data;

import net.runelite.api.gameval.VarbitID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntUnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link AchievementDiaryData} tier-count helpers used by
 * the DIARY_TIERS_COMPLETED account metric.
 */
class AchievementDiaryDataTest
{
	/** Varbit reader backed by a plain map; unset varbits read 0. */
	private static IntUnaryOperator reader(Map<Integer, Integer> varbits)
	{
		return id -> varbits.getOrDefault(id, 0);
	}

	@Nested
	@DisplayName("Tier enumeration")
	class TierEnumeration
	{
		@Test
		@DisplayName("covers exactly 48 tiers (12 areas x 4 tiers)")
		void totalTierCount()
		{
			assertEquals(48, AchievementDiaryData.TOTAL_TIER_COUNT);
			assertEquals(12, AchievementDiaryData.AREA_KEYS.length);
		}

		@Test
		@DisplayName("every area/tier pair resolves a tracking spec")
		void everyPairHasTracking()
		{
			for (String area : AchievementDiaryData.AREA_KEYS)
			{
				for (AchievementDiaryData.Tier tier : AchievementDiaryData.Tier.values())
				{
					assertNotNull(AchievementDiaryData.tracking(area, tier),
						"missing tracking for " + area + " " + tier);
				}
			}
		}
	}

	@Nested
	@DisplayName("countCompletedTiers")
	class CountCompletedTiers
	{
		@Test
		@DisplayName("returns 0 when no varbits are set")
		void zeroWhenNothingSet()
		{
			assertEquals(0, AchievementDiaryData.countCompletedTiers(reader(new HashMap<>())));
		}

		@Test
		@DisplayName("counts boolean COMPLETE varbits set to 1")
		void countsBooleanCompletes()
		{
			Map<Integer, Integer> varbits = new HashMap<>();
			varbits.put(VarbitID.ARDOUGNE_DIARY_EASY_COMPLETE, 1);
			varbits.put(VarbitID.LUMBRIDGE_DIARY_ELITE_COMPLETE, 1);
			varbits.put(VarbitID.WILDERNESS_DIARY_HARD_COMPLETE, 1);

			assertEquals(3, AchievementDiaryData.countCompletedTiers(reader(varbits)));
		}

		@Test
		@DisplayName("counts Karamja count varbits only at or above the tier task total")
		void countsKaramjaByTaskTotal()
		{
			Map<Integer, Integer> varbits = new HashMap<>();
			varbits.put(VarbitID.KARAMJA_EASY_COUNT, 10);  // complete (10 tasks)
			varbits.put(VarbitID.KARAMJA_MED_COUNT, 18);   // 18/19 — incomplete
			varbits.put(VarbitID.KARAMJA_HARD_COUNT, 10);  // complete (10 tasks)

			assertEquals(2, AchievementDiaryData.countCompletedTiers(reader(varbits)));
		}

		@Test
		@DisplayName("counts all 48 tiers when every tracking varbit is at its required value")
		void countsAllTiers()
		{
			Map<Integer, Integer> varbits = new HashMap<>();
			for (String area : AchievementDiaryData.AREA_KEYS)
			{
				for (AchievementDiaryData.Tier tier : AchievementDiaryData.Tier.values())
				{
					AchievementDiaryData.Tracking t = AchievementDiaryData.tracking(area, tier);
					varbits.put(t.varbitId, t.requiredValue);
				}
			}

			assertEquals(AchievementDiaryData.TOTAL_TIER_COUNT,
				AchievementDiaryData.countCompletedTiers(reader(varbits)));
		}

		@Test
		@DisplayName("ignores stray varbit values below the required completion value")
		void ignoresBelowRequired()
		{
			Map<Integer, Integer> varbits = new HashMap<>();
			// Boolean COMPLETE varbits are 0/1; 0 stays incomplete.
			varbits.put(VarbitID.DESERT_DIARY_MEDIUM_COMPLETE, 0);
			varbits.put(VarbitID.KARAMJA_MED_COUNT, 5);

			assertEquals(0, AchievementDiaryData.countCompletedTiers(reader(varbits)));
		}
	}
}
