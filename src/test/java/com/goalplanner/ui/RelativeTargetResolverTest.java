package com.goalplanner.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pure-function tests for the relative goal target math. Cover the
 * resolution from "gain X" → absolute target without touching Swing
 * or the RuneLite client.
 */
class RelativeTargetResolverTest
{
	@Nested
	@DisplayName("resolveSkillXp")
	class SkillXp
	{
		@Test
		@DisplayName("adds delta to current XP")
		void simpleAdd()
		{
			assertEquals(150_000, RelativeTargetResolver.resolveSkillXp(50_000, 100_000));
		}

		@Test
		@DisplayName("clamps at 200M")
		void clampsAtCap()
		{
			assertEquals(200_000_000,
				RelativeTargetResolver.resolveSkillXp(199_000_000, 5_000_000));
		}

		@Test
		@DisplayName("zero current XP is fine")
		void zeroCurrent()
		{
			assertEquals(1000, RelativeTargetResolver.resolveSkillXp(0, 1000));
		}

		@Test
		@DisplayName("zero delta returns -1 (invalid)")
		void zeroDeltaInvalid()
		{
			assertEquals(-1, RelativeTargetResolver.resolveSkillXp(50_000, 0));
		}

		@Test
		@DisplayName("negative delta returns -1 (invalid)")
		void negativeDeltaInvalid()
		{
			assertEquals(-1, RelativeTargetResolver.resolveSkillXp(50_000, -1));
		}

		@Test
		@DisplayName("at max XP, any positive delta still resolves to max")
		void atMaxXp()
		{
			assertEquals(200_000_000,
				RelativeTargetResolver.resolveSkillXp(200_000_000, 100));
		}
	}

	@Nested
	@DisplayName("resolveItemCount")
	class ItemCount
	{
		@Test
		@DisplayName("adds delta to current count")
		void simpleAdd()
		{
			assertEquals(150, RelativeTargetResolver.resolveItemCount(50, 100));
		}

		@Test
		@DisplayName("zero current count is fine")
		void zeroCurrent()
		{
			assertEquals(100, RelativeTargetResolver.resolveItemCount(0, 100));
		}

		@Test
		@DisplayName("zero delta returns -1 (invalid)")
		void zeroDeltaInvalid()
		{
			assertEquals(-1, RelativeTargetResolver.resolveItemCount(50, 0));
		}

		@Test
		@DisplayName("negative delta returns -1 (invalid)")
		void negativeDeltaInvalid()
		{
			assertEquals(-1, RelativeTargetResolver.resolveItemCount(50, -10));
		}

		@Test
		@DisplayName("does not overflow on huge inputs")
		void noOverflow()
		{
			int result = RelativeTargetResolver.resolveItemCount(
				Integer.MAX_VALUE - 10, 100);
			assertEquals(Integer.MAX_VALUE, result);
		}
	}
}
